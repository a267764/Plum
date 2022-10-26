package com.sakurawald.plum.reloaded.api

import com.google.gson.JsonObject
import com.sakurawald.plum.reloaded.SongInformation
import com.sakurawald.exception.CanNotDownloadFileException
import com.sakurawald.exception.FileTooBigException
import com.sakurawald.framework.BotManager
import com.sakurawald.plum.reloaded.Plum
import com.sakurawald.utils.LanguageUtil
import utils.NetworkUtil
import com.sakurawald.utils.NumberUtil
import java.io.File

/**
 * 用于描述一个音乐平台的对象
 */
abstract class AbstractApiMusicPlat(
    val logTypeName: String,
    private var download_music_file_prefix: String,
) {
    abstract fun getCardCode(si: SongInformation): String
    /**
     * 传入音乐平台, 自动根据配置获取SongInformation
     */
    fun checkAndGetSongInformation(
        input_music_name: String,
        random_music_flag: Boolean
    ): SongInformation? {
        val result = if (random_music_flag)
            getRandomSongInformationByMusicName(input_music_name)
        else
            getFirstSongInformationByMusicName(input_music_name)

        Plum.logger.debug("$logTypeName >> 获取到的SongInformation: $result")
        return result
    }

    /**
     * 删除返回JSON文本中可能的乱码和干扰数据
     */
    protected fun deleteBadCode(response: String): String {
        return response.replaceFirst("callback\\(([\\s\\S]*)\\)".toRegex(), "$1")
    }

    /**
     * 封装给CoolQ使用的超简方法
     */
    @Throws(CanNotDownloadFileException::class, FileTooBigException::class)
    fun downloadMusic(si: SongInformation): String {
        return downloadMusic(
            getDownloadPath(si.music_Name, si.music_ID),
            si.music_File_URL
        )
    }

    @Throws(CanNotDownloadFileException::class, FileTooBigException::class)
    fun downloadMusic(download_path: String, si: SongInformation): String {
        return downloadMusic(download_path, si.music_File_URL)
    }

    /**
     * [!] 请注意保存路径是否有写入权限，否则会导致下载失败 [!] 若指定路径已有同名文件，则会直接跳过下载
     *
     * @throws CanNotDownloadFileException
     * @throws FileTooBigException
     */
    @Throws(CanNotDownloadFileException::class, FileTooBigException::class)
    fun downloadMusic(
        download_path: String,
        download_music_URL: String?
    ): String {
        if (download_music_URL == null) {
            return "NOT_FOUND_MUSIC"
        }
        val URL: String = download_music_URL
        if (File(download_path).exists()) {
            Plum.logger.debug("$logTypeName >> 检测到所要下载音乐文件已存在，跳过下载: file_path = $download_path")
            return "MUSIC_FILE_HAS_EXIST"
        }
        Plum.logger.debug("$logTypeName >> 开始下载音乐文件: URL = $URL")
        try {
            NetworkUtil.downloadVoiceFile(URL, download_path)
        } catch (e: CanNotDownloadFileException) {
            Plum.logger.debug("$logTypeName >> 检测到所要下载的音乐文件为付费音乐，无法下载: URL = $URL")
            throw e
        } catch (e: FileTooBigException) {
            Plum.logger.debug("$logTypeName >> 检测到所要下载的音乐文件太大，拒绝下载: URL = $URL")
            throw e
        }
        Plum.logger.debug("$logTypeName >> 完成下载音乐文件: URL = $URL")
        return "OK"
    }

    /**
     * 指定当前音乐平台的代码.
     */
    abstract val selectCodes: ArrayList<String>
    fun getDownloadFileName(music_name: String, music_ID: Long): String {
        /** 常规获取歌曲文件名  */
        var music_file_name = (download_music_file_prefix + "#" + music_name + "#" + music_ID)

        // [!] 有些歌曲带有 目录分隔符，要替换掉，否则会出现错误
        music_file_name = LanguageUtil.translateFileName(music_file_name)

        // [!] 有些外文歌曲, 会导致文件名出现?, 所以使用Unicode进行统一转码
        music_file_name = LanguageUtil
            .translateValidUnicodeSimple(music_file_name)
        /** 歌曲文件名特殊处理  */
        // [!] 为了避免以后再出现奇怪的歌曲字符, 一律不用歌曲名作为文件名!
        music_file_name = (download_music_file_prefix + "#"
                + "DEFAULT_MUSIC_NAME" + "#" + music_ID)
        return music_file_name
    }

    fun getDownloadPath(music_name: String, music_ID: Long): String {
        return voicesPath + getDownloadFileName(music_name, music_ID)
    }

    /**
     * 通过音乐名称，获取音乐的ID 返回：若没找到ID，则返回0
     */
    fun getFirstSongInformationByMusicName(music_name: String): SongInformation? {
        return getSongInformationByMusicName(music_name, 1)
    }

    /**
     * 通过音乐名称，获取音乐列表JSON
     */
    protected abstract fun getMusicListByMusicName_JSON(music_name: String): String?

    /**
     * 通过音乐名称，获取音乐的ID 返回：若没找到ID，则返回0
     */
    fun getRandomSongInformationByMusicName(music_name: String): SongInformation? {
        /**
         * 随机抽取指定首的音乐 [!] 网易云音乐API，最多返回20首音乐！！！
         */
        val index = NumberUtil.getRandomNumber(1, 20)
        Plum.logger.debug("$logTypeName >> 获取随机首的音乐信息: index = $index")
        return getSongInformationByMusicName(music_name, index)
    }

    /**
     * 1. 若搜索不到音乐, 则返回null 2. 若搜索到的是付费音乐, 则自动选择下一首 3. 若所有结果都不匹配, 则返回最后一次满足匹配的结果
     */
    protected abstract fun getSongInformationByJSON(
        getMusicListByMusicName_JSON: String?, index: Int
    ): SongInformation?

    /**
     * 通过音乐名称，获取音乐的ID 返回：若没找到ID，则返回0
     * 输入：index表示所获取到的音乐列表的第几首，若输入的index大于音乐列表，则返回音乐列表的最后一首
     */
    fun getSongInformationByMusicName(
        music_name: String,
        index: Int
    ): SongInformation? {
        val JSON = getMusicListByMusicName_JSON(music_name)
        return getSongInformationByJSON(JSON, index)
    }

    /**
     * 用于判断音乐搜索结果中是否有符合结果的音乐
     */
    protected abstract fun validSongList(
        getMusicListByMusicName_JSON_OBJECT: JsonObject
    ): Boolean

    companion object {
        fun getMusicLengthStrByMillSec(length: Int): String {
            return getMusicLengthStrBySec(length / 1000)
        }

        fun getMusicLengthStrBySec(length: Int): String {
            val minute = length / 60
            val second = length % 60
            return if (minute == 0) {
                second.toString() + "秒"
            } else minute.toString() + "分" + second + "秒"
        }

        @JvmStatic
        val voicesPath: String
            get() = BotManager.getVoicesPath()
    }
}