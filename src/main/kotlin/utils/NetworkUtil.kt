package utils

import com.gargoylesoftware.htmlunit.BrowserVersion
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.sakurawald.plum.reloaded.Plum
import com.sakurawald.plum.reloaded.config.PlumConfig
import com.sakurawald.plum.reloaded.utils.CanNotDownloadFileException
import com.sakurawald.plum.reloaded.utils.FileTooBigException
import org.apache.commons.logging.LogFactory
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.logging.Level
import java.util.logging.Logger
import java.util.regex.Pattern
import javax.net.ssl.HttpsURLConnection

object NetworkUtil {
    // [!] 这里设置与URL有关的变量，ENCODE表示目标网页的编码
    private const val ENCODE = "UTF-8"
    const val userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36"

    private val patternScript = Pattern.compile("<script[^>]*?>[\\s\\S]*?</script>", Pattern.CASE_INSENSITIVE)
    private val patternStyle = Pattern.compile("<style[^>]*?>[\\s\\S]*?</style>", Pattern.CASE_INSENSITIVE)
    private val patternHtml = Pattern.compile("<[^>]+>", Pattern.CASE_INSENSITIVE)
    private val patternW = Pattern.compile("<w[^>]*?>[\\s\\S]*?</w[^>]*?>", Pattern.CASE_INSENSITIVE)
    fun deleteHTMLTags(htmlStr: String): String {
        var html = patternW.matcher(htmlStr).replaceAll("")
        html =  patternScript.matcher(html).replaceAll("")
        html = patternStyle.matcher(html).replaceAll("")
        html = patternHtml.matcher(html).replaceAll("")

        return html.trim { it <= ' ' } // 返回文本字符串
    }

    fun getStaticHTML(URL: String?): String {
        val conn = Jsoup.connect(URL)
        // 修改http包中的header,伪装成浏览器进行抓取
        conn.header("User-Agent", userAgent)
        var doc: Document? = null
        try {
            doc = conn.get()
        } catch (e: IOException) {
            Plum.logger.error(e)
        }
        return doc?.toString() ?: ""
    }

    fun betweenString(text: String, left: String?, right: String): String {
        val zLen = if (left.isNullOrEmpty()) 0
        else text.indexOf(left).let {
            if(it > -1) it + left.length else 0
        }
        val yLen = text.indexOf(right, zLen).let {
            if (it < 0 || right.isEmpty()) text.length else it
        }
        return text.substring(zLen, yLen)
    }

    fun getInputStream(file: File): InputStream? {
        try {
            return FileInputStream(file)
        } catch (e: FileNotFoundException) {
            Plum.logger.error(e)
        }
        return null
    }

    fun getInputStream(url: String?): InputStream? {
        try {
            val conn = URL(url).openConnection() as HttpsURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 10 * 1000
            return conn.inputStream
        } catch (e: IOException) {
            Plum.logger.error(e)
        }
        return null
    }

    fun decodeURL(str: String): String {
        try {
            return URLDecoder.decode(str, ENCODE)
        } catch (e: UnsupportedEncodingException) {
            Plum.logger.error(e)
        }
        return str
    }

    fun encodeURL(str: String): String {
        try {
            return URLEncoder.encode(str, ENCODE)
        } catch (e: UnsupportedEncodingException) {
            Plum.logger.error(e)
        }
        return str
    }

    fun decodeHTML(htmlStr: String): String {
        return htmlStr.replace("<br>", "\n", true)
            .replace("&nbsp;", " ", true)
            .replace("&gt;", "", true)
    }

    fun getDynamicHTML(URL: String?): String? {
        try {
            return WebClient(BrowserVersion.CHROME).use { webClient ->
                /** WebClient Configs.  */
                // 关闭WebClient错误警告.
                LogFactory.getFactory().setAttribute(
                    "org.apache.commons.logging.Log",
                    "org.apache.commons.logging.impl.NoOpLog"
                )
                Logger.getLogger("com.gargoylesoftware").level = Level.OFF
                Logger.getLogger("org.apache.http.client").level = Level.OFF

                //ajax
                webClient.ajaxController = NicelyResynchronizingAjaxController()
                //忽略css错误
                webClient.cssErrorHandler = SilentCssErrorHandler()
                webClient.options.run {
                    //支持 js
                    isJavaScriptEnabled = true
                    //忽略 js 错误
                    isThrowExceptionOnScriptError = false
                    //不执行 CSS 渲染
                    isCssEnabled = false
                    //超时时间
                    timeout = 10 * 1000
                    //允许重定向
                    isRedirectEnabled = true
                }
                //允许cookie
                webClient.cookieManager.isCookiesEnabled = true

                //开始请求网站
                val page = webClient.getPage<HtmlPage>(URL)
                val doc = Jsoup.parse(page.asXml(), URL)
                return@use doc.toString()
            }
        } catch (e: Exception) {
            Plum.logger.error(e)
        }
        return null
    }

    fun downloadImageFile(image_URL: String, path: String) {
        try {
            val dataInputStream = DataInputStream(
                URL(image_URL).openStream()
            )
            val fileOutputStream = FileOutputStream(path)
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var length: Int
            while (dataInputStream.read(buffer).also { length = it } > 0) {
                output.write(buffer, 0, length)
            }
            fileOutputStream.write(output.toByteArray())
            dataInputStream.close()
            fileOutputStream.close()
        } catch (e: IOException) {
            Plum.logger.error(e)
        }
    }

    @Throws(FileTooBigException::class, CanNotDownloadFileException::class)
    fun downloadVoiceFile(urlPath: String, downloadDir: String) {
        return downloadFile(
            urlPath,
            downloadDir,
            PlumConfig.functions.singSongFunction.maxVoiceFileSize
        )
    }

    @Throws(CanNotDownloadFileException::class, FileTooBigException::class)
    fun downloadFile(urlPath: String, downloadDir: String, acceptFileMaxLength: Int) {
        try {
            val urlConnection = URL(urlPath).openConnection()
            val httpURLConnection = urlConnection as HttpURLConnection
            httpURLConnection.requestMethod = "GET"
            httpURLConnection.setRequestProperty("Charset", "UTF-8")
            httpURLConnection.connect()
            val fileLength = httpURLConnection.contentLength
            Plum.logger.debug("NetworkSystem >> Download: URL_path = $urlPath, fileLength = $fileLength")
            /**
             * [!] 判断是否为付费歌曲。 若一首歌曲是付费歌曲，则网易云的音乐下载链接会404
             */
            if (fileLength == 0) {
                throw CanNotDownloadFileException()
            }
            if (fileLength > acceptFileMaxLength) {
                throw FileTooBigException()
            }
            val input = httpURLConnection.inputStream
            val file = File(downloadDir)
            if (!file.parentFile.exists()) {
                file.parentFile.mkdirs()
            }
            val output: OutputStream = FileOutputStream(file)
            output.write(input.readBytes().also { input.close() }).also { output.close() }
        } catch (e: Exception) {
            Plum.logger.error(e)
        }
    }
}