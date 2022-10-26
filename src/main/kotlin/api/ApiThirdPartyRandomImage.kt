package com.sakurawald.plum.reloaded.api

import com.sakurawald.plum.reloaded.Plum
import com.sakurawald.plum.reloaded.config.PlumConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.*

object ApiThirdPartyRandomImage : AbstractApiRandomImage() {
    /** 此处获取该Random Page返回的随机图片URL  */
    /** 关闭Response的body  */
    /**
     * 获取随机图片的URL在线地址
     */
    override val randomImageURL: String?
        get() {
            Plum.logger.debug("RandomImage (ThirdParty) >> getRandomImageURL()")
            var result: String? = null
            val client = OkHttpClient()
            val request = Request.Builder().url(randomImageWebsiteURL).get().build()
            var response: Response? = null
            try {
                response = client.newCall(request).execute()
                /** 此处获取该Random Page返回的随机图片URL  */
                result = response.request.url.toString()
            } catch (e: IOException) {
                Plum.logger.error(e)
            } finally {
                Plum.logger.debug("RandomImage (ThirdParty) >> getRandomImageURL() -> Image_URL = $result")
            }
            /** 关闭Response的body  */
            response?.body?.close()
            return result
        }

    private val random_Image_Website_URLs
        get() = PlumConfig.functions.AtFunction.RandomImage.Random_Image_URLs

    private val randomImageWebsiteURL: String
        get() {
            val n = Random().nextInt(random_Image_Website_URLs.size)
            return random_Image_Website_URLs[n].also {
                Plum.logger.debug("RandomImage (ThirdParty) >> Currently Use Image-Site: $it")
            }
        }
}