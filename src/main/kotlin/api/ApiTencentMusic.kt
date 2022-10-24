package com.sakurawald.plum.reloaded.api

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.sakurawald.plum.reloaded.SongInformation
import com.sakurawald.plum.reloaded.Plum
import com.sakurawald.utils.NetworkUtil
import net.mamoe.mirai.message.data.MusicKind
import net.mamoe.mirai.message.data.MusicShare
import net.mamoe.mirai.message.data.toMessageChain
import okhttp3.*
import java.io.IOException
import java.util.*

object ApiTencentMusic : AbstractApiMusicPlat(
    "QQ Music - API",
    "tencent_music"
) {
    /**
     * 传入QQ音乐的一首歌曲的JSON对象, 根据JSON判断该首歌曲能否访问
     */
    private fun canAccess(one_song: JsonElement): Boolean {
        /** 解析JSON  */
        val pay = one_song.asJsonObject["pay"]
            .asJsonObject
        val payplay = pay["payplay"].asInt
        /** 输出反馈结果  */
        return payplay == 0
    }

    override fun getCardCode(si: SongInformation): String {
        return MusicShare(
            MusicKind.QQMusic, si.music_Name,
            si.summary, si.music_Page_URL, si.img_URL,
            si.music_File_URL,
            "[点歌] " + si.music_Name
        ).toMessageChain().serializeToMiraiCode()
    }

    override val selectCodes: ArrayList<String>
        get() = ArrayList(Arrays.asList("qq音乐", "qq", "腾讯音乐", "腾讯"))

    override fun getMusicListByMusicName_JSON(music_name: String): String? {
        Plum.logger.debug("QQ Music - API >> 搜索音乐列表 - 请求: music_name = "
                    + music_name
        )
        var result: String? = null
        val client = OkHttpClient()
        val URL = ("https://c.y.qq.com/soso/fcgi-bin/client_search_cp?aggr=1&cr=1&flag_qc=0&p=1&n=20&w="
                + NetworkUtil.encodeURL(music_name))
        val request: Request = Request.Builder().url(URL).get().build()
        var response: Response? = null
        var JSON: String? = null
        try {
            response = client.newCall(request).execute()
            JSON = response.body!!.string()
            result = JSON
            result = deleteBadCode(result)
        } catch (e: IOException) {
            Plum.logger.error(e)
        } finally {
            Plum.logger.debug("QQ Music - API >> 搜索音乐列表 - 结果: Code = "
                        + response!!.message + ", Response = " + JSON
            )
        }
        /** 关闭Response的body  */
        response.body!!.close()
        return result
    }

    private fun getDownloadURL_JSON(song_mid: String?): String? {
        Plum.logger.debug("QQ Music - API >> getDownloadURL_JSON -> Run")
        var result: String? = null
        val client = OkHttpClient()
        val URL = "http://localhost:3300/song/url?type=320&id=$song_mid"
        Plum.logger.debug("QQ Music - API >> Request URL >> $URL")
        val request = Request.Builder().url(URL).get().build()
        var response: Response? = null
        var json: String? = null
        try {
            response = client.newCall(request).execute()
            Plum.logger.debug("QQ Music - API >> Request Response >> $response")
            json = response.body?.string()
            result = json
        } catch (e: IOException) {
            Plum.logger.error(e)
        }
        Plum.logger.debug(
            "QQ Music - API >> Get Random Sentence >> Response: JSON = $json"
        )
        /** 关闭Response的body  */
        response?.body?.close()
        return result
    }

    private fun getDownloadURL(song_mid: String?): String {
        /** 获取JSON数据  */
        /** 获取JSON数据  */
        val JSON = getDownloadURL_JSON(song_mid) ?: return ""

        // 若未找到结果，则返回null
        /** 解析JSON数据  */
        val jo = JsonParser.parseString(JSON) as JsonObject
        val response = jo.asJsonObject
        val data = response["data"].asString
        Plum.logger.debug("QQ Music - API >> Get MusicFileURL >> $data")
        return data
    }

    override fun getSongInformationByJSON(
        getMusicListByMusicName_JSON: String?, index: Int
    ): SongInformation? {
        // 若未找到结果，则返回0
        var index = index
        if (getMusicListByMusicName_JSON == null || getMusicListByMusicName_JSON.isBlank()) return null
        val jo = JsonParser.parseString(getMusicListByMusicName_JSON).asJsonObject // 构造JsonObject对象
        val data = jo.getAsJsonObject("data")
        if (!validSongList(jo)) return null

        var result: SongInformation? = null
        var i = 1
        for (je in data.getAsJsonObject("song").getAsJsonArray("list")) {
            // 获取Si的各个属性
            val music_name = je.asJsonObject["songname"].asString
            val music_ID = je.asJsonObject["songid"].asInt
            val mid = je.asJsonObject["songmid"].asString

            // 新建Si对象
            result = SongInformation(
                music_name, music_ID.toLong(),
                music_MID = mid
            ).also {
                it.sourceType = "QQ音乐"
                it.music_Page_URL = "http://y.qq.com/#type=song&id=$music_ID"
                it.music_File_URL = getDownloadURL(mid)
            }
            if (i >= index) {
                Plum.logger.debug("QQ Music - API >> 获取的音乐信息(指定首) - 成功获取到指定首(第${index}首)的音乐的信息: $result")
                // [!] 判断获取到的歌曲能不能下载, 若该首音乐不能下载, 则向下选择下一首
                if (!canAccess(je)) {
                    Plum.logger.debug("QQ Music - API >> 获取的音乐信息(指定首) - 检测到指定首(第${index}首)的音乐无法下载, 即将自动匹配下一首")
                    index++
                    i++
                    result = null
                    continue
                }
                return result
            }
            i++
        }
        /** 若输入的index超出音乐列表，则返回最后一次成功匹配到的音乐ID  */
        return if (result == null) null
        else {
            Plum.logger.debug("QQ Music - API >> 获取音乐信息(指定首) - 未获取到指定首(第${index}首)的音乐，默认返回最后一次成功获取的音乐信息: $result")
            result
        }
    }

    override fun validSongList(getMusicListByMusicName_JSON_OBJECT: JsonObject): Boolean {
        val totalNumber = getMusicListByMusicName_JSON_OBJECT
            .getAsJsonObject("data").getAsJsonObject("song")["totalnum"].asInt
        return totalNumber != 0
    }
}