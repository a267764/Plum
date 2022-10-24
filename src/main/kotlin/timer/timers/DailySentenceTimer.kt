package com.sakurawald.plum.reloaded.timer.timers

import com.sakurawald.api.PowerWord_API
import com.sakurawald.api.PowerWord_API.Motto
import Countdown
import com.sakurawald.plum.reloaded.Plum
import com.sakurawald.framework.MessageManager
import com.sakurawald.plum.reloaded.config.PlumConfig
import com.sakurawald.plum.reloaded.timer.DefaultPlan
import com.sakurawald.utils.DateUtil
import com.sakurawald.utils.NetworkUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage

object DailySentenceTimer : DailyTimer(
    "DailyPoetry", 1000 * 10, 1000 * 60
), DefaultPlan {
    private var todayMotto: Motto? = null
    override fun isPrepareStage(): Boolean {
        val nowDay = DateUtil.getNowDay()
        if (nowDay != lastPrepareDay) {
            val nowHour = DateUtil.getNowHour()
            // 判断是否是4点，即5点之前
            if (nowHour == 4) {
                val nowMinute = DateUtil.getNowMinute()
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

    override fun isSendStage(): Boolean {
        val nowDay = DateUtil.getNowDay()
        if (nowDay != lastSendDay) {
            val nowHour = DateUtil.getNowHour()
            val nowMinute = DateUtil.getNowMinute()
            if (nowHour == 5 && nowMinute <= 10) {
                lastSendDay = nowDay
                return true
            }
        }
        return false
    }

    override fun prepareStage() {
        sendMsg = ("早安，" + DateUtil.getNowYear() + "年" + DateUtil.getNowMonth()
                + "月" + DateUtil.getNowDay() + "日！")

        /** Function: Countdown. **/
        val cda = Countdown.getCountdownsByCommands()
        if (PlumConfig.functions.DailyCountdown.enable && cda.size != 0) {
            sendMsg += "\n\n●倒计时："
            /** Append all countdowns.  */
            for (cd in cda) {
                sendMsg = """
                    $sendMsg
                    ${cd.todayCountdownMsg}
                    """.trimIndent()
            }
        }
        /** Function: DailySentence.  */
        defaultPlan()
        try {
            todayMotto = PowerWord_API.todayMotto
        } catch (e: Exception) {
            // Do nothing.
        }

        sendMsg = """${sendMsg?.trim { it <= ' ' }}
            
            ●今日格言：
            ${todayMotto?.content_cn}( ${todayMotto?.content_en} )"""

        // Has Translation ?
        if (todayMotto?.translation != null) {
            sendMsg += """
                〖解读〗${todayMotto!!.translation}
                        """.trimIndent()
        }
        runBlocking(Dispatchers.IO) {
            try {
                // Add Sentence's ShareImg.
                val uploadImage = NetworkUtil.getInputStream(todayMotto!!.fenxiang_img).uploadAsImage(
                    PluginMain.getCurrentBot().groups.stream().findAny().get()
                )
                sendMsg += """
                [mirai:image:${uploadImage.imageId}]
                """.trimIndent()
            } catch (e: Exception) {
                // Do nothing.
            }

            Plum.logger.debug("TimerSystem >> Daily Sentence: \n$sendMsg")
        }
    }

    override fun sendStage() {
        MessageManager.sendMessageToAllQQFriends(sendMsg)
        MessageManager.sendMessageToAllQQGroups(sendMsg)

    }

    override fun defaultPlan() {
        todayMotto = PowerWord_API.Motto.defaultMotto

    }

}