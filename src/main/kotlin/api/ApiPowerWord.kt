package com.sakurawald.plum.reloaded.api

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.sakurawald.plum.reloaded.Plum
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import utils.DateUtil.getDateSimple
import java.io.IOException
import java.util.*

object ApiPowerWord {
    private val requestURL: String
        get() = generateRequestURL(Calendar.getInstance())

    private fun generateRequestURL(c: Calendar): String = "http://open.iciba.com/dsapi/?date=${c.getDateSimple()}"

    /** 封装JSON数据  */
    /** 解析JSON数据  */
    /** 获取JSON数据  */
    @JvmStatic
    val todayMotto: Motto
        get() {
            /** 获取JSON数据  */
            /** 获取JSON数据  */
            val json = todayMotto_JSON ?: return Motto.NULL_MOTTO

            // 若未找到结果，则返回null
            /** 解析JSON数据  */
            val jo = JsonParser.parseString(json) as JsonObject
            val response = jo.asJsonObject
            val dateline = response["dateline"].asString
            val tts = response["tts"].asString
            val contentEN = response["content"].asString
            val contentCN = response["note"].asString
            var translation = response["translation"].asString
            if (translation!!.trim { it <= ' ' } == "新版每日一句") {
                translation = null
            }
            val picture = response["picture"].asString
            val picture2 = response["picture2"].asString
            val picture3 = response["picture3"].asString
            val picture4 = response["picture4"].asString
            val shareImage = response["fenxiang_img"].asString

            /** 封装JSON数据  */
            val result = Motto()
            result.dateline = dateline
            result.tts = tts
            result.contentEN = contentEN
            result.contentCN = contentCN
            result.translation = translation
            result.picture = picture
            result.picture2 = picture2
            result.picture3 = picture3
            result.picture4 = picture4
            result.shareImage = shareImage
            Plum.logger.debug("PowerWord >> Get Motto >> $result")
            return result
        }

    /** Close.  */
    private val todayMotto_JSON: String?
        get() {
            Plum.logger.debug("PowerWord >> Get Random Motto -> Run")
            var json: String? = null
            val client = OkHttpClient()
            val request: Request
            val url = requestURL
            Plum.logger.debug("PowerWord >> Request URL >> $url")
            request = Request.Builder().url(url).get().build()
            var response: Response? = null
            try {
                response = client.newCall(request).execute()
                Plum.logger.debug("PowerWord >> Request Response >> $response")
                json = response.body?.string()
            } catch (e: IOException) {
                Plum.logger.error(e)
            }
            Plum.logger.debug("PowerWord >> Get Random Motto >> Response: JSON = $json")
            /** Close.  */
            response?.body?.close()

            return json
        }

    class Motto {
        var dateline: String? = null
        var tts: String? = null
        var contentEN: String? = null
        var contentCN: String? = null
        var translation: String? = null
        var picture: String? = null
        var picture2: String? = null
        var picture3: String? = null
        var picture4: String? = null
        var shareImage: String? = null
        override fun toString(): String {
            return "Motto{" +
                    "dateline='" + dateline + '\'' +
                    ", tts='" + tts + '\'' +
                    ", content_en='" + contentEN + '\'' +
                    ", content_cn='" + contentCN + '\'' +
                    ", translation='" + translation + '\'' +
                    ", picture='" + picture + '\'' +
                    ", picture2='" + picture2 + '\'' +
                    ", picture3='" + picture3 + '\'' +
                    ", picture4='" + picture4 + '\'' +
                    ", fenxiang_img='" + shareImage + '\'' +
                    '}'
        }

        companion object {
            val NULL_MOTTO = Motto()

            @JvmStatic
            val defaultMotto: Motto
                get() {
                    val result = Motto()
                    val contentEn =
                        "No matter what happens, or how bad it seems today, life does go on, and it will be better tomorrow."
                    val contentCN = "不管发生什么，不管今天看起来多么糟糕，生活都会继续，明天会更好。"
                    val translation = """
        小编的话：怀揣着对明天的美好期盼，时刻鼓舞自己笑着面对生活，我们的每一天都会过的阳光灿烂。
        
        【警告】在获取句子的时候发生了一些预期之外的问题
        """.trimIndent()
                    result.contentEN = contentEn
                    result.contentCN = contentCN
                    result.translation = translation
                    return result
                }
        }
    }
}