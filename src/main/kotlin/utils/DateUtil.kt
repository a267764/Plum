package utils

import java.util.*

object DateUtil {
    val unixTimeS: Long
        get() = unixTimeMS / 1000
    val unixTimeMS: Long
        get() = System.currentTimeMillis()

    /**
     * 将该时间与某一时间比较
     *
     * @return 输入的日期在现在之后，则返回 1
     *
     * 输入的日期在现在之前，则返回 -1
     *
     * 若输入日期与现在一样，则返回 0
     */
    fun Calendar.compareDate(date2: Calendar = Calendar.getInstance()): Int =
        (clone() as Calendar).setZero().compareTo((date2.clone() as Calendar).setZero())

    /**
     * 获取两个Date的时间差
     */
    fun differentDaysByMillisecond(c1: Calendar, c2: Calendar): Int {
        /** 防止前后一天计算错误  */
        if (c1.time.time < c2.time.time) {
            c1.setZero()
            c2.setMid()
        } else {
            c2.setZero()
            c1.setMid()
        }
        return ((c2.time.time - c1.time.time) / (1000 * 3600 * 24)).toInt()
    }

    // 计算两个日期相差多少秒
    fun diffSeconds(big: Calendar, small: Calendar): Long {
        val nm: Long = 1000
        val diff = big.time.time - small.time.time // 获得两个时间的毫秒时间差异
        return diff / nm
    }

    val now: Date
        get() = Calendar.getInstance().time
    val nowDay: Int
        get() = Calendar.getInstance()[Calendar.DAY_OF_MONTH]
    val nowHour: Int
        get() = Calendar.getInstance()[Calendar.HOUR_OF_DAY]
    val nowMinute: Int
        get() = Calendar.getInstance()[Calendar.MINUTE]
    val nowMonth: Int
        get() = Calendar.getInstance()[Calendar.MONTH] + 1
    val nowSecond: Int
        get() = Calendar.getInstance()[Calendar.SECOND]
    val nowYear: Int
        get() = Calendar.getInstance()[Calendar.YEAR]

    fun Calendar.setMid(): Calendar = also {
        it[Calendar.HOUR_OF_DAY] = 12
        it[Calendar.MINUTE] = 0
    }

    fun Calendar.setZero(): Calendar = also {
        it[Calendar.HOUR_OF_DAY] = 0
        it[Calendar.MINUTE] = 0
    }

    fun Date.toCalendar(): Calendar = Calendar.getInstance().also {
        it.time = this
    }

    fun Long.toCalendar(): Calendar = toDate().toCalendar()
    fun Long.toDate(): Date = Date(this)
}