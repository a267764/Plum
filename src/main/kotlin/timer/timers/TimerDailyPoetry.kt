package com.sakurawald.plum.reloaded.timer.timers

import com.sakurawald.plum.reloaded.Plum
import com.sakurawald.plum.reloaded.api.ApiBaiDuHanYu
import com.sakurawald.plum.reloaded.api.ApiJinRiShiCi
import com.sakurawald.plum.reloaded.utils.sendToAllFriends
import com.sakurawald.plum.reloaded.utils.sendToAllGroups
import net.mamoe.mirai.message.data.toMessageChain
import net.mamoe.mirai.message.data.toPlainText
import utils.DateUtil

object TimerDailyPoetry : DailyTimer(
    "DailyPoetry", 1000 * 5, 1000 * 60
) {
    var todayPoetry: ApiJinRiShiCi.Poetry? = null
    override val isPrepareStage: Boolean
        get() {
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

    override val isSendStage: Boolean
        get() {
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
        ApiBaiDuHanYu.getRandomPoetry()?.also {
            todayPoetry = it
            sendMsg = """
                晚安，${DateUtil.nowYear}年${DateUtil.nowMonth}月${DateUtil.nowDay}日~
                
                ●今日诗词
                〖标题〗${it.title}
                〖作者〗（${it.dynasty}） ${it.author}
                〖诗词〗
                ${it.content}
            """.trimIndent().toPlainText().toMessageChain()
            Plum.logger.debug("TimerSystem >> DailyPoetry: \n$sendMsg")
        }
    }

    override suspend fun sendStage() {
        Plum.CURRENT_BOT?.let {
            it.sendToAllFriends(sendMsg)
            it.sendToAllGroups(sendMsg)
        }
    }
}