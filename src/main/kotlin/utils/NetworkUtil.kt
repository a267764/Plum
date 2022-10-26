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
    fun deleteHTMLTags(htmlStr: String): String {
        var htmlStr = htmlStr
        val regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>" // 定义script的正则表达式
        val regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>" // 定义style的正则表达式
        val regEx_html = "<[^>]+>" // 定义HTML标签的正则表达式
        // final String regEx_space = "\\s*|\t|\r|\n";// 定义空格回车换行符
        val regEx_w = "<w[^>]*?>[\\s\\S]*?<\\/w[^>]*?>" // 定义所有w标签
        val p_w = Pattern.compile(regEx_w, Pattern.CASE_INSENSITIVE)
        val m_w = p_w.matcher(htmlStr)
        htmlStr = m_w.replaceAll("") // 过滤script标签
        val p_script = Pattern.compile(
            regEx_script,
            Pattern.CASE_INSENSITIVE
        )
        val m_script = p_script.matcher(htmlStr)
        htmlStr = m_script.replaceAll("") // 过滤script标签
        val p_style = Pattern
            .compile(regEx_style, Pattern.CASE_INSENSITIVE)
        val m_style = p_style.matcher(htmlStr)
        htmlStr = m_style.replaceAll("") // 过滤style标签
        val p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE)
        val m_html = p_html.matcher(htmlStr)
        htmlStr = m_html.replaceAll("") // 过滤html标签

        // 为了不破坏原始数据中的空白和换行，这里要注释掉
        // Pattern p_space = Pattern.compile(regEx_space,
        // Pattern.CASE_INSENSITIVE);
        // Matcher m_space = p_space.matcher(htmlStr);
        // htmlStr = m_space.replaceAll(""); // 过滤空格回车标签
        // htmlStr = htmlStr.replaceAll(" ", ""); // 过滤
        return htmlStr.trim { it <= ' ' } // 返回文本字符串
    }

    fun getStaticHTML(URL: String?): String {
        val user_agent =
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36"
        val conn = Jsoup.connect(URL)
        // 修改http包中的header,伪装成浏览器进行抓取
        conn.header("User-Agent", user_agent)
        var doc: Document? = null
        try {
            doc = conn.get()
        } catch (e: IOException) {
            Plum.logger.error(e)
        }
        return doc.toString()
    }

    fun betweenString(text: String, left: String?, right: String): String {
        var result = ""
        var zLen: Int
        if (left == null || left.isEmpty()) {
            zLen = 0
        } else {
            zLen = text.indexOf(left)
            if (zLen > -1) {
                zLen += left.length
            } else {
                zLen = 0
            }
        }
        var yLen = text.indexOf(right, zLen)
        if (yLen < 0 || right.isEmpty()) {
            yLen = text.length
        }
        result = text.substring(zLen, yLen)
        return result
    }

    fun getInputStream(file: File?): InputStream? {
        try {
            return FileInputStream(file)
        } catch (e: FileNotFoundException) {
            Plum.logger.error(e)
        }
        return null
    }

    fun getInputStream(URL: String?): InputStream? {
        val URL_Object: URL
        try {
            URL_Object = URL(URL)
            val conn = URL_Object.openConnection() as HttpsURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 10 * 1000
            return conn.inputStream
        } catch (e: IOException) {
            Plum.logger.error(e)
        }
        return null
    }

    fun decodeURL(str: String?): String {
        var result = ""
        if (null == str) {
            return ""
        }
        try {
            result = URLDecoder.decode(str, ENCODE)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return result
    }

    fun encodeURL(str: String?): String {
        var result = ""
        if (null == str) {
            return ""
        }
        try {
            result = URLEncoder.encode(str, ENCODE)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return result
    }

    fun decodeHTML(htmlStr: String): String {
        var htmlStr = htmlStr
        htmlStr = htmlStr.replace("<br>", "\n").replace("&nbsp;", " ")
            .replace("&gt;", "")
        return htmlStr
    }

    fun getDynamicHTML(URL: String?): String? {
        try {
            WebClient(BrowserVersion.CHROME).use { webClient ->
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
                    //支持js
                    isJavaScriptEnabled = true

                    //忽略js错误
                    isThrowExceptionOnScriptError = false
                    //不执行CSS渲染
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
                return doc.toString()
            }
        } catch (e: Exception) {
            Plum.logger.error(e)
        }
        return null
    }

    fun downloadImageFile(image_URL: String?, path: String?) {
        val url: URL
        try {
            url = URL(image_URL)
            val dataInputStream = DataInputStream(
                url.openStream()
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
            e.printStackTrace()
        }
    }

    @Throws(FileTooBigException::class, CanNotDownloadFileException::class)
    fun downloadVoiceFile(urlPath: String, downloadDir: String?): String? {
        return downloadFile(
            urlPath,
            downloadDir,
            PlumConfig.functions.SingSongFunction.maxVoiceFileSize
        )
    }

    @Throws(CanNotDownloadFileException::class, FileTooBigException::class)
    fun downloadFile(urlPath: String, downloadDir: String?, acceptFileMaxLength: Int): String? {
        var file: File? = null
        var path: String? = null
        try {
            val url = URL(urlPath)
            val urlConnection = url.openConnection()
            val httpURLConnection = urlConnection as HttpURLConnection
            httpURLConnection.requestMethod = "GET"
            httpURLConnection.setRequestProperty("Charset", "UTF-8")
            httpURLConnection.connect()
            val fileLength = httpURLConnection.contentLength
            Plum.logger.debug(
                "NetworkSystem >> Download: URL_path = " + urlPath
                        + ", fileLength = " + fileLength
            )
            /**
             * [!] 判断是否为付费歌曲。 若一首歌曲是付费歌曲，则网易云的音乐下载链接会404
             */
            if (fileLength == 0) {
                throw CanNotDownloadFileException()
            }
            if (fileLength > acceptFileMaxLength) {
                throw FileTooBigException()
            }
            val bin = BufferedInputStream(
                httpURLConnection.inputStream
            )
            path = downloadDir
            println(path)
            file = File(path)
            if (!file.parentFile.exists()) {
                file.parentFile.mkdirs()
            }
            val out: OutputStream = FileOutputStream(file)
            var size = 0
            var len = 0
            val buf = ByteArray(1024)
            while (bin.read(buf).also { size = it } != -1) {
                len += size
                out.write(buf, 0, size)
            }
            bin.close()
            out.close()
        } catch (e: Exception) {
            Plum.logger.error(e)
        }
        return null
    }
}