package com.sakurawald.plum.reloaded.api

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.sakurawald.plum.reloaded.Plum
import okhttp3.*
import okhttp3.FormBody.Builder.build
import okhttp3.OkHttpClient.Builder.build
import okhttp3.Request.Builder.build
import okhttp3.Request.Builder.get
import okhttp3.Request.Builder.url
import okhttp3.Request.url
import java.io.IOException
import java.util.*

object ApiHitoKoto {
    private val requestURL: String
        private get() = "https://v1.hitokoto.cn" + FileManager.applicationConfig_File.getSpecificDataInstance().Functions.NudgeFunction.HitoKoto.get_URL_Params
    /** 封装JSON数据  */
    /** 解析JSON数据  */
    /** 获取JSON数据  */
    @JvmStatic
    val randomSentence: Sentence
        get() {
            /** 获取JSON数据  */
            /** 获取JSON数据  */
            val JSON = randomSentence_JSON ?: return Sentence.NULL_SENTENCE

            // 若未找到结果，则返回null
            /** 解析JSON数据  */
            val jo = JsonParser.parseString(JSON) as JsonObject
            val response = jo.asJsonObject
            val id = response["id"].asInt
            val content = response["hitokoto"].asString
            val type = response["type"].asString
            val from = response["from"].asString
            val creator = response["creator"].asString
            val created_at = response["created_at"].asString
            /** 封装JSON数据  */
            val result = Sentence(
                id, content, type, from, creator,
                created_at
            )
            Plum.logger.debug("HitoKoto >> Get Sentence >> $result")
            return result
        }

    /** 关闭Response的body  */
    private val randomSentence_JSON: String?
        private get() {
            Plum.logger.debug("HitoKoto >> Get Random Sentence -> Run")
            var result: String? = null
            val client = OkHttpClient()
            var request: Request? = null
            val URL = requestURL
            Plum.logger.debug("HitoKoto >> Request URL >> $URL")
            request = Builder().url(URL).get().build()
            var response: Response? = null
            var JSON: String? = null
            try {
                response = client.newCall(request).execute()
                Plum.logger.debug("HitoKoto >> Request Response >> $response")
                JSON = response.body!!.string()
                result = JSON
            } catch (e: IOException) {
                LoggerManager.logError(e)
            }
            Plum.logger.debug("HitoKoto >> Get Random Sentence >> Response: JSON = $JSON"
            )
            /** 关闭Response的body  */
            if (response != null) {
                Objects.requireNonNull(response.body).close()
            }
            return result
        }

    class Sentence(
        id: Int, content: String?, type: String?, from: String?,
        creator: String?, created_at: String?
    ) {
        val id = 0
        val content: String? = null
        val type: String? = null
        val from: String? = null
        val creator: String? = null
        val created_at: String? = null

        init {
            this.id = id
            this.content = content
            this.type = type
            this.from = from
            this.creator = creator
            this.created_at = created_at
        }

        /**
         * @return 格式化后的文本, 可用于快速展示. 本身为空则返回null.
         */
        val formatedString: String?
            get() = if (content == null && from == null) {
                null
            } else "『" + content + "』" + "-「" + from + "」"

        override fun toString(): String {
            return ("Sentence [id=" + id + ", content=" + content + ", type=" + type
                    + ", from=" + from + ", creator=" + creator + ", created_at="
                    + created_at + "]")
        }

        companion object {
            val NULL_SENTENCE = Sentence(0, null, null, null, null, null)
        }
    }
}