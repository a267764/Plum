package com.sakurawald.plum.reloaded.api

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.sakurawald.plum.reloaded.Plum
import com.sakurawald.utils.DateUtil
import okhttp3.*
import java.io.IOException
import java.util.*

object ApiPowerWord {
    private val requestURL: String
        get() = generateRequestURL(Calendar.getInstance())

    private fun generateRequestURL(c: Calendar): String {
        return ("http://open.iciba.com/dsapi/?date="
                + DateUtil.getDateSimple(c))
    }
    /** 封装JSON数据  */
    /** 解析JSON数据  */
    /** 获取JSON数据  */
    @JvmStatic
    val todayMotto: Motto
        get() {
            /** 获取JSON数据  */
            /** 获取JSON数据  */
            val JSON = todayMotto_JSON ?: return Motto.NULL_MOTTO

            // 若未找到结果，则返回null
            /** 解析JSON数据  */
            val jo = JsonParser.parseString(JSON) as JsonObject
            val response = jo.asJsonObject
            val dateline = response["dateline"].asString
            val tts = response["tts"].asString
            val content_en = response["content"].asString
            val content_cn = response["note"].asString
            var translation = response["translation"].asString
            if (translation!!.trim { it <= ' ' } == "新版每日一句") {
                translation = null
            }
            val picture = response["picture"].asString
            val picture2 = response["picture2"].asString
            val picture3 = response["picture3"].asString
            val picture4 = response["picture4"].asString
            val fenxiang_img = response["fenxiang_img"].asString
            /** 封装JSON数据  */
            val result = Motto()
            result.dateline = dateline
            result.tTS = tts
            result.content_en = content_en
            result.content_cn = content_cn
            result.translation = translation
            result.picture = picture
            result.picture2 = picture2
            result.picture3 = picture3
            result.picture4 = picture4
            result.fenxiang_img = fenxiang_img
            Plum.logger.debug("PowerWord >> Get Motto >> $result")
            return result
        }

    /** Close.  */
    private val todayMotto_JSON: String?
        get() {
            Plum.logger.debug("PowerWord >> Get Random Motto -> Run")
            var result: String? = null
            val client = OkHttpClient()
            val request: Request
            val URL = requestURL
            Plum.logger.debug("PowerWord >> Request URL >> $URL")
            request = Request.Builder().url(URL).get().build()
            var response: Response? = null
            var JSON: String? = null
            try {
                response = client.newCall(request).execute()
                Plum.logger.debug("PowerWord >> Request Response >> $response")
                JSON = response.body!!.string()
                result = JSON
            } catch (e: IOException) {
                Plum.logger.error(e)
            }
            Plum.logger.debug("PowerWord >> Get Random Motto >> Response: JSON = $JSON"
            )
            /** Close.  */
            response?.body?.close()

            return result
        }

    class Motto {
        var dateline: String? = null
        var tTS: String? = null
        var content_en: String? = null
        var content_cn: String? = null
        var translation: String? = null
        var picture: String? = null
        var picture2: String? = null
        var picture3: String? = null
        var picture4: String? = null
        var fenxiang_img: String? = null
        override fun toString(): String {
            return "Motto{" +
                    "dateline='" + dateline + '\'' +
                    ", tts='" + tTS + '\'' +
                    ", content_en='" + content_en + '\'' +
                    ", content_cn='" + content_cn + '\'' +
                    ", translation='" + translation + '\'' +
                    ", picture='" + picture + '\'' +
                    ", picture2='" + picture2 + '\'' +
                    ", picture3='" + picture3 + '\'' +
                    ", picture4='" + picture4 + '\'' +
                    ", fenxiang_img='" + fenxiang_img + '\'' +
                    '}'
        }

        companion object {
            val NULL_MOTTO = Motto()
            @JvmStatic
            val defaultMotto: Motto
                get() {
                    val result = Motto()
                    val content_en =
                        "No matter what happens, or how bad it seems today, life does go on, and it will be better tomorrow."
                    val content_cn = "不管发生什么，不管今天看起来多么糟糕，生活都会继续，明天会更好。"
                    val translation = """
        小编的话：怀揣着对明天的美好期盼，时刻鼓舞自己笑着面对生活，我们的每一天都会过的阳光灿烂。
        
        【警告】在获取句子的时候发生了一些预期之外的问题
        """.trimIndent()
                    result.content_en = content_en
                    result.content_cn = content_cn
                    result.translation = translation
                    return result
                }
        }
    }
}