package com.sakurawald.plum.reloaded.api

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.sakurawald.plum.reloaded.Plum
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import utils.NetworkUtil
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
        val json = getAnswerJson(question) ?: return "ERROR, RETRY PLEASE."

        // 若未找到结果，则返回null
        /** 解析JSON数据  */
        val jo = JsonParser.parseString(json) as JsonObject
        val response = jo.asJsonObject
        var content = response["content"].asString
        /** 封装JSON数据  */
        content = decodeAnswer(content)
        Plum.logger.debug("QingYunKe >> Get Answer >> $content")
        return content
    }

    private fun getAnswerJson(question: String): String? {
        Plum.logger.debug("QingYunKe >> Get Answer -> Run")
        var json: String? = null
        val client = OkHttpClient()
        val url = getRequestURL(question)
        Plum.logger.debug("QingYunKe >> Request URL >> $url")
        val request = Request.Builder().url(url).get().build()
        var response: Response? = null
        try {
            response = client.newCall(request).execute()
            Plum.logger.debug("QingYunKe >> Request Response >> $response")
            json = response.body?.string()
        } catch (e: IOException) {
            Plum.logger.error(e)
        }
        Plum.logger.debug("QingYunKe >> Get Answer >> Response: JSON = $json")
        /** 关闭Response的body  */
        response?.body?.close()
        return json
    }
}