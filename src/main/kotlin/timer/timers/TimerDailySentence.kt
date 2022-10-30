package com.sakurawald.plum.reloaded.timer.timers

import com.sakurawald.plum.reloaded.Countdown
import com.sakurawald.plum.reloaded.Plum
import com.sakurawald.plum.reloaded.api.ApiPowerWord
import com.sakurawald.plum.reloaded.config.PlumConfig
import com.sakurawald.plum.reloaded.timer.DefaultPlan
import com.sakurawald.plum.reloaded.utils.sendToAllFriends
import com.sakurawald.plum.reloaded.utils.sendToAllGroups
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.message.data.toMessageChain
import net.mamoe.mirai.message.data.toPlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import utils.DateUtil
import utils.NetworkUtil

object TimerDailySentence : DailyTimer(
    "DailyPoetry", 1000 * 10, 1000 * 60
), DefaultPlan {
    private var todayMotto: ApiPowerWord.Motto? = null
    override val isPrepareStage: Boolean
        get() {
        val nowDay = DateUtil.nowDay
        if (nowDay != lastPrepareDay) {
            val nowHour = DateUtil.nowHour
            // 判断是否是4点，即5点之前
            if (nowHour == 4) {
                val nowMinute = DateUtil.nowMinute
                if (nowMinute in 55..59) {
                    lastPrepareDay = nowDay
                    return true
                }
            }
            // 判断是否已经5点了，但是自己还没准备。也就是说，程序是在5点的时候临时运行的
            // 那么就赶快return一个true，临时准备，临时发送。两个阶段一起做
            if (nowHour == 5) {
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
            if (nowHour == 5 && nowMinute <= 10) {
                lastSendDay = nowDay
                return true
            }
        }
        return false
    }

    override fun prepareStage() {
        var send = ("早安，" + DateUtil.nowYear + "年" + DateUtil.nowMonth
                + "月" + DateUtil.nowDay + "日！")

        /** Function: Countdown. **/
        val cda = Countdown.countdownsByCommands
        if (PlumConfig.functions.dailyCountdown.enable && cda.isNotEmpty()) {
            send += "\n\n●倒计时："
            /** Append all countdowns.  */
            for (cd in cda) {
                send = """
                    $send
                    ${cd.todayCountdownMsg}
                    """.trimIndent()
            }
        }
        /** Function: DailySentence.  */
        defaultPlan()
        try {
            todayMotto = ApiPowerWord.todayMotto
        } catch (e: Exception) {
            // Do nothing.
        }

        send = """${send.trim { it <= ' ' }}
            
            ●今日格言：
            ${todayMotto?.contentCN}( ${todayMotto?.contentEN} )"""

        // Has Translation ?
        if (todayMotto?.translation != null) {
            send += """
                〖解读〗${todayMotto!!.translation}
                        """.trimIndent()
        }
        runBlocking(Dispatchers.IO) {
            try {
                sendMsg = send.toPlainText().toMessageChain()
                // Add Sentence's ShareImg.
                Plum.CURRENT_BOT?.groups?.firstOrNull()?.let {
                    NetworkUtil.getInputStream(todayMotto?.shareImage)?.uploadAsImage(it)
                }?.let { sendMsg = sendMsg?.plus(it) }
            } catch (e: Exception) {
                // Do nothing.
            }
            Plum.logger.debug("TimerSystem >> Daily Sentence: \n$send")
        }
    }

    override suspend fun sendStage() {
        Plum.CURRENT_BOT?.let {
            it.sendToAllFriends(sendMsg)
            it.sendToAllGroups(sendMsg)
        }
    }

    override fun defaultPlan() {
        todayMotto = ApiPowerWord.Motto.defaultMotto

    }

}