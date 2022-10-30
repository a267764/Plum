package com.sakurawald.plum.reloaded.api

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.sakurawald.plum.reloaded.Plum
import com.sakurawald.plum.reloaded.SongInformation
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MusicKind
import net.mamoe.mirai.message.data.MusicShare
import net.mamoe.mirai.message.data.toMessageChain
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import utils.NetworkUtil
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

object ApiNeteaseCloudMusic : AbstractApiMusicPlat(
    "NeteaseMusic - API",
    "netease_cloud_music"
) {
    override val selectCodes: ArrayList<String> = arrayListOf("网易云音乐", "网易云", "网易")

    private fun canAccess(keyContent: String): Boolean {
        try {
            val urlConnection = URL(keyContent).openConnection()
            val httpURLConnection = urlConnection as HttpURLConnection
            httpURLConnection.requestMethod = "POST"
            httpURLConnection.setRequestProperty("Charset", "UTF-8")
            httpURLConnection.connect()
            val fileLength = httpURLConnection.contentLength
            Plum.logger.debug("Download >> 所要下载的文件: URL_path = $keyContent, fileLength = $fileLength")
            /**
             * [!] 判断是否为付费歌曲。 若一首歌曲是付费歌曲，则网易云的音乐下载链接会404
             */
            return fileLength != 0
        } catch (e: Exception) {
            // do nothing!
        }
        return true
    }

    private fun getDownloadMusicURL(music_ID: Long): String = "http://music.163.com/song/media/outer/url?id=$music_ID.mp3"

    /**
     * 通过音乐名称，获取网易云音乐的音乐列表JSON
     */
    override fun getMusicListJson(name: String): String? {
        Plum.logger.debug("$logTypeName >> 搜索音乐列表 - 请求: music_name = $name")
        var json: String? = null
        val client = OkHttpClient()
        val body = FormBody.Builder().add("s", name)
            .add("type", "1")
            .add("offset", "0")
            .add("total", "true")
            .add("limit", "20").build()
        val request = Request.Builder()
            .url("http://music.163.com/api/search/get")
            .post(body)
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
        val jo = JsonParser.parseString(jsonText ?: return null) as JsonObject
        if (!isValidSongList(jo)) return null

        /** 注意：网易api的songCount参数似乎有问题，有时候有歌曲，也返回0  */
        var result: SongInformation? = null
        var i = 1
        for (je in jo.getAsJsonObject("result").getAsJsonArray("songs")){
            // 获取Si的各种信息
            val name = je.asJsonObject["name"].asString
            val id = je.asJsonObject["id"].asInt.toLong()

            // 新建Si对象
            result = SongInformation(name, id).also {
                it.sourceType = "网易云音乐"
                it.fileUrl = getDownloadMusicURL(id)
                it.pageUrl = "http://music.163.com/song/$id"
            }
            if (i >= index) {
                Plum.logger.debug("$logTypeName >> 获取的音乐信息(指定首) - 成功获取到指定首(第${index}首)的音乐的信息: $result")

                // [!] 判断获取到的歌曲能不能下载, 若该首音乐不能下载, 则向下选择下一首
                if (!canAccess(getDownloadMusicURL(id))) {
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
        if (result != null)
            Plum.logger.debug("$logTypeName >> 获取音乐信息(指定首) - 未获取到指定首(第${index}首)的音乐，默认返回最后一次成功获取的音乐信息: $result")
        return result
    }

    /**
     * 用于判断音乐搜索结果中是否有符合结果的音乐
     */
    override fun isValidSongList(
        jsonObj: JsonObject
    ): Boolean {
        val jo1 = jsonObj.getAsJsonObject("result")
        return jo1 != null && jo1["songs"] != null && !jo1["songs"].isJsonNull
    }

    override fun getCardCode(si: SongInformation): MessageChain {
        return MusicShare(
            MusicKind.NeteaseCloudMusic, si.name,
            si.summary, si.pageUrl, si.imageUrl,
            si.fileUrl,
            "[点歌] " + si.name
        ).toMessageChain()
    }
}