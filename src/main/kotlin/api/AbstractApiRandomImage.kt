package com.sakurawald.plum.reloaded.api

import com.sakurawald.plum.reloaded.Plum
import utils.NetworkUtil
import java.io.File
import java.net.MalformedURLException
import java.net.URL

abstract class AbstractApiRandomImage {
    /**
     * 获取随机图片的URL在线地址
     */
    abstract val randomImageURL: String?

    companion object {
        /**
         * 通过图片的URL来获取图片的保存名称
         */
        fun getSaveImageFileName(image_URL: String?): String {
            var url: URL? = null
            try {
                url = URL(image_URL)
            } catch (e: MalformedURLException) {
                Plum.logger.error(e)
            }
            val file = File(url!!.file)
            return url.host + "#" + file.name
        }

        fun saveImage(image_URL: String, save_path: String) {
            Plum.logger.debug("RandomImage >> Download -> image_URL = $image_URL, save_path = $save_path")
            NetworkUtil.downloadImageFile(image_URL, save_path)
        }
    }
}