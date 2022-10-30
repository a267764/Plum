package com.sakurawald.plum.reloaded.function

import com.sakurawald.plum.reloaded.Plum
import utils.DateUtil
import java.util.*

abstract class FunctionManager {
    /** 功能使用的历史记录者  */
    val functionUseHistoryManager = HistoryManager<Long>(
        "功能使用历史记录者", 0 - 1000000
    )

    /** 判断发送间隔是否合法  */
    abstract fun canUse(group: Long): Boolean
    fun updateUseTime(group: Long) {
        functionUseHistoryManager.update(group)
    }
}


/**
 * 用于记录调用历史的对象
 */
class HistoryManager<E>(
    /**
     * 该调用历史管理者的名称
     */
    val name: String,
    /**
     * 表示当第一次调用时, 默认设置的last_call_date与当前时间的时间偏移(sec)
     */
    val firstCallSecIncrement: Int
) {
    /**
     * 要求每次调用至少需要间隔多少时间, 防止恶意调用
     */
    private val history = mutableMapOf<E, Calendar>()

    private fun distanceLastCallSeconds(user: E): Long = DateUtil.diffSeconds(Calendar.getInstance(), getCallHistory(user))

    private fun getCallHistory(user: E): Calendar {
        Plum.logger.debug("Get Call History: user = $user")

        // 获取某个user号的调用历史，若获取的是Null，则说明这是该用户第一次调用，应返回一个很老的日期，让该用户可以正常发车。
        return history[user] ?: run {
            val time = Calendar.getInstance()
            time[Calendar.SECOND] += firstCallSecIncrement
            // [!] 若不存在, 则手动设置一个
            history[user] = time
            time
        }
    }

    /**
     * 输入时间间隔, 判断某个用户的两次调用间隔是否合法
     */
    fun isCallSuccessIntervalLegal(user: E, limitSec: Int): Boolean {
        val diff = distanceLastCallSeconds(user)
        val limit = limitSec.toLong()
        Plum.logger.debug("判断当前时间的调用是否时间间隔合法: diff = $diff, limit = $limit")
        return diff >= limit
    }

    fun setCallHistory(user: E, calendar: Calendar) {
        Plum.logger.debug("设置调用历史: user = $user, calendar = $calendar")
        history[user] = calendar
    }

    fun update(user: E) {
        val calendar = Calendar.getInstance()
        Plum.logger.debug("更新调用历史: user = $user, calendar = $calendar")
        history[user] = calendar
    }
}

