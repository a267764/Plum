package com.sakurawald.plum.reloaded.api

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.sakurawald.plum.reloaded.Plum
import com.sakurawald.utils.NetworkUtil
import okhttp3.*
import java.io.IOException

object ApiQingYunKe {
    private fun getRequestURL(question: String): String {
        return "http://api.qingyunke.com/api.php?key=free&appid=0&msg=" + NetworkUtil.encodeURL(question)
    }

    private fun decodeAnswer(answer: String): String {
        return answer.replace("{br}", "\n")
    }

    @JvmStatic
    fun getAnswer(question: String): String {
        /** 获取JSON数据  */
        /** 获取JSON数据  */
        val JSON = getAnswer_JSON(question) ?: return "ERROR, RETRY PLEASE."

        // 若未找到结果，则返回null
        /** 解析JSON数据  */
        val jo = JsonParser.parseString(JSON) as JsonObject
        val response = jo.asJsonObject
        var content = response["content"].asString
        /** 封装JSON数据  */
        content = decodeAnswer(content)
        Plum.logger.debug("QingYunKe >> Get Answer >> $content")
        return content
    }

    private fun getAnswer_JSON(question: String): String? {
        Plum.logger.debug("QingYunKe >> Get Answer -> Run")
        var result: String? = null
        val client = OkHttpClient()
        val URL = getRequestURL(question)
        Plum.logger.debug("QingYunKe >> Request URL >> $URL")
        val request = Request.Builder().url(URL).get().build()
        var response: Response? = null
        var json: String? = null
        try {
            response = client.newCall(request).execute()
            Plum.logger.debug("QingYunKe >> Request Response >> $response")
            json = response.body?.string()
            result = json
        } catch (e: IOException) {
            Plum.logger.error(e)
        }
        Plum.logger.debug("QingYunKe >> Get Answer >> Response: JSON = $json"
        )
        /** 关闭Response的body  */
        response?.body?.close()
        return result
    }
}