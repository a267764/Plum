package com.sakurawald.plum.reloaded.api

import com.sakurawald.plum.reloaded.Plum
import com.sakurawald.plum.reloaded.config.PlumConfig
import com.sakurawald.utils.NetworkUtil
import java.util.regex.Pattern

object ApiBaiDuHanYu {
    private const val SINGLE_SENTENCE_PUNCTUATION = "[,，。、?？!！；：]"
    private var retryCount = 0

    /**
     * 输入一句诗句，通过标点符号，对诗句进行再一次细分，返回细分部分的最长句子
     */
    private fun getMaxKeySentencePart(singleSentence: String?): String {
        var result = ""
        val sentences = singleSentence!!.split(SINGLE_SENTENCE_PUNCTUATION.toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        for (s in sentences) {
            if (result.length < s.trim { it <= ' ' }.length) {
                result = s.trim { it <= ' ' }
            }
        }
        return result
    }

    private fun formatNotes(n: String): String {
        /** 将诗歌网页编者写的一些奇形怪状的索引稍做处理  */
        val notes = n.replace("⒈", "(1)").replace("⒉", "(2)")
            .replace("⒊", "(3)").replace("⒋", "(4)").replace("⒌", "(5)")
            .replace("⒍", "(6)").replace("⒎", "(7)").replace("⒏", "(8)")
            .replace("⒐", "(9)").replace("⒑", "(0)").replace("⒒", "(11)")
            .replace("⒓", "(12)").replace("⒔", "(13)").replace("⒕", "(14)")
            .replace("⒖", "(15)").replace("⒗", "(16)").replace("⒘", "(17)")
            .replace("⒙", "(18)").replace("⒚", "(19)").replace("⒛", "(20)")
            .replace("⑴", "(1)").replace("⑵", "(2)").replace("⑶", "(3)")
            .replace("⑷", "(4)").replace("⑸", "(5)").replace("⑹", "(6)")
            .replace("⑺", "(7)").replace("⑻", "(8)").replace("⑼", "(9)")
            .replace("⑽", "(10)").replace("⑾", "(11)").replace("⑿", "(12)")
            .replace("⒀", "(13)").replace("⒁", "(14)").replace("⒂", "(15)")
            .replace("⒃", "(16)").replace("⒄", "(17)").replace("⒅", "(18)")
            .replace("⒆", "(19)").replace("⒇", "(20)").replace("①", "(1)")
            .replace("②", "(2)").replace("③", "(3)").replace("④", "(4)")
            .replace("⑤", "(5)").replace("⑥", "(6)").replace("⑦", "(7)")
            .replace("⑧", "(8)").replace("⑨", "(9)").replace("⑩", "(10)")
            .replace("㈠", "(1)").replace("㈡", "(2)").replace("㈢", "(3)")
            .replace("㈣", "(4)").replace("㈤", "(5)").replace("㈥", "(6)")
            .replace("㈦", "(7)").replace("㈧", "(8)").replace("㈨", "(9)")
            .replace("㈩", "(10)")

        /** Generate FormatedNote.  */
        var index = 1
        val ns = ArrayList(
            listOf(
                *notes
                    .split("\\(\\d{1,3}\\)|\\[\\d{1,3}\\]|〔\\d{1,3}〕|\\d{1,3}\\.|\\d{1,3}、".toRegex())
                    .dropLastWhile { it.isEmpty() }.toTypedArray()
            )
        )
        // NOTE: 当只有1条注释的时候, 不加任何序号.
        if (ns.size == 1) return ns[0]

        val result = StringBuilder()
        for (s in ns.map { it.trim { it <= ' ' } }) {
            // [!] 此处对单条注释进行trim，防止本来百度文库的每条注释，结尾都有换行符，
            // 最终导致换行符过多，格式难看！
            // s = s.trim { it <= ' ' }

            // 如果分割到的单个文本是空的，则忽略
            if (s == "") continue
            if (index == ns.size) result.append("$index. $s")
            else result.append("$index. $s\n")
            index++
        }
        return result.toString()
    }

    private fun getAuthor(HTML: String): String {
        val rule =
            "<a class=\"poem-detail-header-author\"[\\s\\S]*?>[\\s\\S]*?<span class=\"poem-info-gray\"> 【作者】 </span>([\\s\\S]*?)</a>"
        val p = Pattern.compile(rule, Pattern.DOTALL)
        val m = p.matcher(HTML)
        return if (m.find()) {
            NetworkUtil.deleteHTMLTags(m.group(1)).trim { it <= ' ' }
        } else {
            "佚名"
        }
    }

    private fun getAuthorIntroduction(HTML: String): String {
        val rule = "<div class=\"poem-author-intro\"[\\s\\S]*?>([\\s\\S]*?)</div>"
        val p = Pattern.compile(rule, Pattern.DOTALL)
        val m = p.matcher(HTML)
        return if (m.find()) {
            NetworkUtil.deleteHTMLTags(
                NetworkUtil
                    .decodeHTML(m.group(1))
            )
                // 针对处理
                .replace("来源：古诗文网", "")
                .replace("百科详情", "")
                .trim { it <= ' ' }
                // [!] 防止有些偷懒的小编，直接写个百科详情跳转，什么简介都不写
                .ifBlank { "无" }
        } else {
            "无"
        }
    }

    private fun getBaiduHanYuURLByKeySentence(keySentence: String?): String {
        val result: String

        // [!] 对关键句子，再次分解，按标点符号
        val keySentencePart = getMaxKeySentencePart(keySentence)
        Plum.logger.debug("BaiDuHanYu >> getBaiduHanYuURLByKeySentence -> KeySentence's KeyPart: $keySentencePart")
        // [!] 此处要进行URL转码，否则会获取HTTP失败!
        result = ("https://hanyu.baidu.com/s?wd=+"
                + NetworkUtil.encodeURL(keySentencePart)
                + NetworkUtil.encodeURL("+诗歌") + "&from=poem")
        Plum.logger.debug("BaiDuHanYu >> BaiDuHanYu Poetry URL: $result")
        return result
    }

    private fun getContent(HTML: String): String {
        val rule = "<(div|p) class=\"poem-detail-main-text\"[\\s\\S]*?>([\\s\\S]*?)</\\1>"
        val p = Pattern.compile(rule, Pattern.DOTALL)
        val m = p.matcher(HTML)
        var result = ""

        // [!] 本处大量运用正则表达式， 警惕繁琐的正则表达式，导致程序卡死，出现奇怪的BUG
        while (m.find()) {
            result += NetworkUtil.decodeHTML(
                NetworkUtil.deleteHTMLTags(
                    m.group(2)
                )
            ) + "\n"
        }

        return NetworkUtil.deleteHTMLTags(NetworkUtil.decodeHTML(result))
            .trim { it <= ' ' }
            .replace(" ", "").ifBlank { "BLANK CONTENT" }
    }

    private fun getDynasty(HTML: String): String {
        val rule =
            "<span class=\"poem-detail-header-author\">[\\s\\S]*?<span class=\"poem-info-gray\">[\\s\\S]*?【朝代】[\\s\\S]*?</span>([\\s\\S]*?)</span>"
        val p = Pattern.compile(rule, Pattern.DOTALL)
        val m = p.matcher(HTML)
        return if (m.find()) {
            m.group(1).trim { it <= ' ' }
        } else {
            "不详"
        }
    }

    private fun getTranslation(HTML: String): String {
        val rule = "<div class=\"poem-detail-item-content means-fold\">([\\s\\S]*?)</div>"
        val p = Pattern.compile(rule, Pattern.DOTALL)
        val m = p.matcher(HTML)
        return if (m.find()) {
            m.group(1).trim { it <= ' ' }
        } else {
            "无"
        }
    }

    private fun getNotes(HTML: String): String {
        val rule =
            "<b>[\\s\\S]*?注释[\\s\\S]*?</b>[\\s\\S]*?</a>[\\s\\S]*?</div>[\\s\\S]*?<div class=\"poem-detail-separator\">[\\s\\S]*?</div>[\\s\\S]*?<div class=\"poem-detail-item-content\">([\\s\\S]*?)</div>"
        val p = Pattern.compile(rule, Pattern.DOTALL)
        val m = p.matcher(HTML)
        var notes: String? = null
        return if (m.find()) {
            formatNotes(
                NetworkUtil.deleteHTMLTags(
                    NetworkUtil.decodeHTML(m.group(1))
                ).replace("来源：古诗文网", "")
            ).trim { it <= ' ' }
        } else {
            "无"
        }
    }

    private fun getTitle(HTML: String): String {
        val rule = "<div class=\"poem-detail-item\" id=\"poem-detail-header\">[\\s\\S]*?<h1>([\\s\\S]*?)</h1>"
        val p = Pattern.compile(rule, Pattern.DOTALL)
        val m = p.matcher(HTML)
        return if (m.find()) {
            m.group(1).trim { it <= ' ' }
        } else {
            "TITLE ERROR"
        }
    }

    /**
     * 输入百度汉语的HTTP,判断该HTTP是否为某一个具体诗歌的详细页面.
     */
    private fun isValidHTTP(HTML: String): Boolean {
        return HTML.contains("poem-detail-item-content")
    }

    private val baiduHanYuRandomURL: String
        get() {
            val result: String
            val keySentence = ApiJinRiShiCi.keySentenceByToken
            Plum.logger.debug("BaiDuHanYu >> getKeySentenceByToken > Key Sentence: $keySentence")
            result = getBaiduHanYuURLByKeySentence(keySentence)
            return result
        }// 若几次重试后，还是失败，则将retryCount设为0，然后放弃重试，终止递归

    /** 若获取到的是无效的URL网页，则重新获取  */
    @JvmStatic
    fun getRandomPoetry(): ApiJinRiShiCi.Poetry {
        val baiduHanYuPoetryHTML = NetworkUtil.getDynamicHTML(baiduHanYuRandomURL)
        /** 若获取到的是无效的URL网页，则重新获取  */
        if (!isValidHTTP(baiduHanYuPoetryHTML)) {

            // 若几次重试后，还是失败，则将retryCount设为0，然后放弃重试，终止递归
            if (retryCount >= PlumConfig.functions.DailyPoetry.maxRetryLimit) {
                retryCount = 0
                Plum.logger.debug("BaiDuHanYu >> RetryCount has run out -> Abandon!")
                return ApiJinRiShiCi.Poetry.defaultPoetry
            }
            retryCount++
            Plum.logger.debug(
                "BaiDuHanYu >> Failed, Try to Retry -> Currently RetryCount: $retryCount"
            )
            return getRandomPoetry()
        }
        val poetry = ApiJinRiShiCi.Poetry(
            null,
            getTitle(baiduHanYuPoetryHTML),
            getDynasty(baiduHanYuPoetryHTML),
            getAuthor(baiduHanYuPoetryHTML),
            getContent(baiduHanYuPoetryHTML),
            getTranslation(baiduHanYuPoetryHTML),
        ).also {
            it.authorIntroduction = getAuthorIntroduction(baiduHanYuPoetryHTML)
            it.note = getNotes(baiduHanYuPoetryHTML)
        }
        poetry.html = baiduHanYuPoetryHTML
        return poetry
    }
}