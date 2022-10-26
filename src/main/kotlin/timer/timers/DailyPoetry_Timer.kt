package com.sakurawald.plum.reloaded.timer.timers

import com.sakurawald.api.BaiDuHanYu_API
import com.sakurawald.api.JinRiShiCi_API.Poetry
import com.sakurawald.framework.MessageManager
import com.sakurawald.plum.reloaded.Plum
import utils.DateUtil

object DailyPoetry_Timer : DailyTimer(
    "DailyPoetry", 1000 * 5, 1000 * 60
) {
    var todayPoetry: Poetry? = null
    override fun isPrepareStage(): Boolean {
        val nowDay = DateUtil.nowDay
        if (nowDay != lastPrepareDay) {
            val nowHour = DateUtil.nowHour
            if (nowHour == 20) {
                if (DateUtil.nowMinute in 55..59) {
                    lastPrepareDay = nowDay
                    return true
                }
            }
            if (nowHour == 21) {
                lastPrepareDay = nowDay
                return true
            }
        }
        return false
    }

    override fun isSendStage(): Boolean {
        val nowDay = DateUtil.nowDay
        if (nowDay != lastSendDay) {
            val nowHour = DateUtil.nowHour
            val nowMinute = DateUtil.nowMinute
            if (nowHour == 21 && nowMinute <= 10) {
                lastSendDay = nowDay
                return true
            }
        }
        return false
    }

    override fun prepareStage() {

        /** 准备sendMsg  */
        BaiDuHanYu_API.randomPoetry?.also {
            todayPoetry = it
            sendMsg = """
                晚安，${DateUtil.nowYear}年${DateUtil.nowMonth}月${DateUtil.nowDay}日~
                
                ●今日诗词
                〖标题〗${it.title}
                〖作者〗（${it.dynasty}） ${it.author}
                〖诗词〗
                ${it.content}
            """.trimIndent()
        }
        Plum.logger.debug("TimerSystem >> DailyPoetry: \n$sendMsg")
    }

    override fun sendStage() {
        MessageManager.sendMessageToAllQQFriends(sendMsg)
        MessageManager.sendMessageToAllQQGroups(sendMsg)
    }
}