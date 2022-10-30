package com.sakurawald.plum.reloaded.utils

import com.sakurawald.plum.reloaded.Plum
import java.io.File
import java.lang.Character.UnicodeBlock
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


private fun bytesToHex1(md5Array: ByteArray): String {
    val strBuilder = StringBuilder()
    for (b in md5Array) {
        val temp = 0xff and b.toInt() // TODO:此处为什么添加 0xff & ？
        val hexString = Integer.toHexString(temp)
        if (hexString.length == 1) { // 如果是十六进制的0f，默认只显示f，此时要补上0
            strBuilder.append("0").append(hexString)
        } else {
            strBuilder.append(hexString)
        }
    }
    return strBuilder.toString()
}

/**
 * 获取String的MD5值
 *
 * @param info
 * 字符串
 * @return 该字符串的MD5值
 */
fun getMD5(info: String): String {
    return try {
        // 获取 MessageDigest 对象，参数为 MD5 字符串，表示这是一个 MD5 算法（其他还有 SHA1 算法等）：
        val md5 = MessageDigest.getInstance("MD5")
        // update(byte[])方法，输入原数据
        // 类似StringBuilder对象的append()方法，追加模式，属于一个累计更改的过程
        md5.update(info.toByteArray(StandardCharsets.UTF_8))
        // digest()被调用后,MessageDigest对象就被重置，即不能连续再次调用该方法计算原数据的MD5值。可以手动调用reset()方法重置输入源。
        // digest()返回值16位长度的哈希值，由byte[]承接
        val md5Array = md5.digest()
        // byte[]通常我们会转化为十六进制的32位长度的字符串来使用,本文会介绍三种常用的转换方法
        bytesToHex1(md5Array)
    } catch (e: NoSuchAlgorithmException) {
        ""
    }
}


/** 输入double, 保留2位小数  */
fun formatDigit(number: Double): Double {
    return formatDigit(number, 2)
}

/** 输入double, 保留指定digit位小数  */
fun formatDigit(number: Double, dight: Int): Double {
    return String.format("%." + dight + "f", number).toDouble()
}

/** 输入小数, 返回百分数文本  */
fun getFormattedPercentage(number: Double): String {
    return formatDigit(number * 100).toString() + "%"
}

/** 随机获取1~30000的整数  */
fun getRandomNumber(): Int {
    return getRandomNumber(1, 30000)
}

/** 取随机数  */
fun getRandomNumber(min: Double, max: Double): Int {
    val random = Random()
    return (random.nextInt((max - min + 1).toInt()) + min).toInt()
}

/** 取随机数  */
fun getRandomNumber(min: Int, max: Int): Int {
    val random = Random()
    return random.nextInt(max - min + 1) + min
}

/** 该方法只能判断 正整数  */
fun isNumber(str: String): Boolean {
    var i = str.length
    while (--i >= 0) {
        if (!Character.isDigit(str[i])) {
            return false
        }
    }
    return true
}


/**
 * 对输入字符串的每一个字符进行Unicode Block区间检测, 若发现非 汉语, 英文, 符号之外的Unicode,
 * 则直接以Unicode码的转移形式表示
 */
private val translateValidUnicodeBlocks: Set<UnicodeBlock> = setOf(
    UnicodeBlock.KATAKANA,
        UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS,
        UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS,
        UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A,
        UnicodeBlock.GENERAL_PUNCTUATION,
        UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION,
        UnicodeBlock.BASIC_LATIN
)

private val mJapaneseUnicodeBlocks: Set<UnicodeBlock> = setOf(
        UnicodeBlock.HIRAGANA,
        UnicodeBlock.KATAKANA
)



fun hasJapaneseChar(str: String): Boolean = str.any { mJapaneseUnicodeBlocks.contains(UnicodeBlock.of(it)) }

/** 判断是否全部为韩文  */
fun hasKoreaChar(inputStr: String): Boolean = inputStr.any {
    (it.code in 0x3131..0x318e || it.code >= 0xAC00) && (it.code <= 0xD7A3)
}

/** 输出每个字符的UnicodeBlock  */
fun printAllCharsUnicodeBlock(str: String) {
    val arr = str.toCharArray()
    for (c in arr) {
        Plum.logger.debug("c = " + UnicodeBlock.of(c))
    }
}

/** 将文件名中对Windows不合法的成分都进行转化  */
fun translateFileName(fileName: String): String {
    return fileName.replace("/", "or")
        .replace("\\", "or")
        .replace(File.separator, "")
        .replace(File.pathSeparator, "")
        .replace("&", "and")
        .replace(":", "Colon")
        .replace("*", "Star")
        .replace("?", "QM")
        .replace("|", "DIV")
        .replace("<", "LSM")
        .replace(">", "RSM")
}

fun translateValidUnicode(str: String): String {
    val sb = StringBuffer()
    /** 对字符串中每个字符进行检测  */
    for (c in str.toCharArray()) {
        if (!translateValidUnicodeBlocks.contains(UnicodeBlock.of(c))) {
            sb.append("U=").append(c.code).append(";")
        } else {
            sb.append(c)
        }
    }
    return sb.toString()
}

fun translateValidUnicodeSimple(str: String): String {
    val sb = StringBuilder()
    /** 对字符串中每个字符进行检测  */
    for (c in str.toCharArray()) {
        if (!translateValidUnicodeBlocks.contains(UnicodeBlock.of(c))) {
            sb.append("U")
        } else {
            sb.append(c)
        }
    }
    return sb.toString()
}
fun transObjectX(obj: Int, message: String, newStr: String): String {
    return message.replace("[OBJECT$obj]", newStr)
}