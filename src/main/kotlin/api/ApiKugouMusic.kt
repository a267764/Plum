package com.sakurawald.plum.reloaded.api

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.sakurawald.plum.reloaded.Plum
import com.sakurawald.plum.reloaded.SongInformation
import com.sakurawald.plum.reloaded.utils.getMD5
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MusicKind
import net.mamoe.mirai.message.data.MusicShare
import net.mamoe.mirai.message.data.toMessageChain
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import utils.NetworkUtil
import java.io.IOException
import java.util.*

object ApiKugouMusic : AbstractApiMusicPlat(
    "KuGouMusic - API",
    "kugou_music"
) {
    override val selectCodes: ArrayList<String> = arrayListOf("酷狗音乐", "酷狗")

    override fun getCardCode(si: SongInformation): MessageChain {
        return MusicShare(
            MusicKind.MiguMusic, si.name,
            si.summary, si.pageUrl, si.imageUrl,
            si.fileUrl,
            "[点歌] " + si.name
        ).toMessageChain()
    }

    private fun canAccess(keyContent: String): Boolean {
        return keyContent.trim { it <= ' ' } != ""
    }

    /**
     * 传入Si来获取音乐下载地址
     */
    private fun getDownloadMusicURL(info: SongInformation): SongInformation? {
        /** 获取JSON  */
        val json = getDownloadUrlJson(info.hash)

        /** 解析JSON  */
        val jo = JsonParser.parseString(json) as JsonObject
        val status = jo["status"].asInt
        // [!] 只有成功获取到音乐下载地址, 才会返回1
        if (status != 1) return null

        // 固定的歌曲图片
        val imageUrl = "https://www.kugou.com/yy/static/images/play/logo.png"
        // 取一下歌曲下载地址
        val musicUrl: String = jo.getAsJsonArray("url").firstOrNull()?.asString ?: ""
        val musicLength = jo["timeLength"].asInt


        /** 完善SongInformation  */
        info.imageUrl = imageUrl
        info.fileUrl = musicUrl
        info.length = musicLength
        return info
    }

    /**
     * 解析音乐的下载地址
     */
    fun getDownloadUrlJson(hash: String): String? {
        Plum.logger.debug("$logTypeName >> 解析音乐下载地址 - 请求: hash = $hash")
        var json: String? = null
        val builder = OkHttpClient.Builder()
        val client: OkHttpClient = builder.build()
        // 计算出key
        val key = getMD5((hash + "kgcloudv2").lowercase(Locale.getDefault()))
        val request = Request.Builder()
            .url("http://trackercdn.kugou.com/i/v2/?cmd=25&key=$key&hash=$hash&pid=1&behavior=download")
            .get()
            .addHeader("Upgrade-Insecure-Requests", "1")
            .addHeader("User-Agent", NetworkUtil.userAgent)
            .addHeader("Cookie", "kg_mid=justtrustme").build()
        var response: Response? = null
        try {
            response = client.newCall(request).execute()
            json = response.body!!.string()
        } catch (e: IOException) {
            Plum.logger.error(e)
        } finally {
            Plum.logger.debug("$logTypeName >> 解析音乐下载地址 - 结果: Code = ${response?.message}, Response = $json")
        }
        /** 关闭Response的body  */
        response?.body?.close()
        return json
    }

    /**
     * 通过音乐名称，获取酷狗音乐的音乐列表JSON
     */
    override fun getMusicListJson(name: String): String? {
        Plum.logger.debug(
            "$logTypeName >> 搜索音乐列表 - 请求: music_name = $name"
        )
        var json: String? = null
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://msearchcdn.kugou.com/api/v3/search/song?&pagesize=20&keyword=${NetworkUtil.encodeURL(name)}&page=1")
            .get()
            .addHeader("User-Agent", NetworkUtil.userAgent)
            .build()
        var response: Response? = null
        try {
            response = client.newCall(request).execute()
            json = response.body?.string()
        } catch (e: IOException) {
            Plum.logger.error(e)
        } finally {
            Plum.logger.debug("$logTypeName >> 搜索音乐列表 - 结果: Code = ${response?.message}, Response = $json")
        }
        /** 关闭Response的body  */
        response?.body?.close()
        return json
    }

    /**
     * 1. 若搜索不到音乐, 则返回null 2. 若搜索到的是付费音乐, 则自动选择下一首 3. 若所有结果都不匹配, 则返回最后一次满足匹配的结果
     */
    override fun getSongInfoByJson(
        jsonText: String?, targetIndex: Int
    ): SongInformation? {
        // 若未找到结果，则返回0
        var index = targetIndex
        if (jsonText == null) return null

        val jo = JsonParser.parseString(jsonText).asJsonObject // 构造JsonObject对象
        if (!isValidSongList(jo)) return null

        val data = jo.getAsJsonObject("data")

        /** 注意：网易api的songCount参数似乎有问题，有时候有歌曲，也返回0  */
        var result: SongInformation? = null
        var i = 1
        for (je in data.getAsJsonArray("info")) {

            // 获取某首歌曲的各个属性
            val name = je.asJsonObject["songname"].asString
            val id = je.asJsonObject["album_audio_id"].asInt
            val hash = je.asJsonObject["hash"].asString
            val author = je.asJsonObject["singername"].asString
            val pageUrl = ("https://www.kugou.com/song/#hash=$hash&album_id=$id")

            // 新建Si对象
            result = SongInformation(
                name, id.toLong(),
                author = author,
                hash = hash
            ).also {
                it.sourceType = "酷狗音乐"
                it.pageUrl = pageUrl
            }

            // [!] 调用方法, 利用hash和id解析出URL
            result = getDownloadMusicURL(result)
            if (result == null) {
                i++
                continue
            }
            if (i >= index) {
                Plum.logger.debug("$logTypeName >> 获取的音乐信息(指定首) - 成功获取到指定首(第${index}首)的音乐的信息: $result")

                // [!] 判断获取到的歌曲能不能下载, 若该首音乐不能下载, 则向下选择下一首
                if (!canAccess(result.fileUrl)) {
                    Plum.logger.debug("$logTypeName >> 获取的音乐信息(指定首) - 检测到指定首(第${index}首)的音乐无法下载, 即将自动匹配下一首")
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
        if (result != null) {
            Plum.logger.debug("$logTypeName >> 获取音乐信息(指定首) - 未获取到指定首(第${index}首)的音乐，默认返回最后一次成功获取的音乐信息: $result")
        }
        return result
    }

    /**
     * 用于判断音乐搜索结果中是否有符合结果的音乐
     */
    override fun isValidSongList(
        jsonObj: JsonObject
    ): Boolean {
        // 判断错误码
        val code = jsonObj["errcode"].asInt
        if (code != 0) return false
        // 判断音乐列表
        val jo1 = jsonObj.getAsJsonObject("data")
        return jo1 != null && jo1["info"].asJsonArray.size() != 0
    }
}