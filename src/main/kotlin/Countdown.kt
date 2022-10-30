package com.sakurawald.plum.reloaded

import com.sakurawald.plum.reloaded.config.PlumConfig
import utils.DateUtil
import utils.DateUtil.getDateSimple
import utils.DateUtil.toCalendar
import java.util.*
import kotlin.math.abs

/**
 * 描述一个具体的Countdown项目
 */
class Countdown(private val countdownCommand: String) {
    private val msgSpecialCountdown = ArrayList<String>()
    private var msgBasicCountdown: String
    private var countdownTimestampMs: Long
    private var countdownCalendar: Calendar
    private var msgSpecialCountdownString: String

    init {
        /** 对单条Countdown_Command进行分割  */
        val temp = countdownCommand.split("\\|".toRegex(), limit = 3).toTypedArray()
        msgBasicCountdown = temp[0]
        countdownTimestampMs = temp[1].toLong()
        countdownCalendar = countdownTimestampMs.toCalendar()
        msgSpecialCountdownString = temp[2]
        msgSpecialCountdown.addAll(msgSpecialCountdownString.split("&".toRegex()).dropLastWhile { it.isEmpty() })
    }

    /**
     * 获取今天距离倒计时日期还有几天, 若今天为倒计时当天, 则返回0. 超过倒计时当天, 则返回负数
     */
    private val distanceDays: Int
        get() = DateUtil.differentDaysByMillisecond(Calendar.getInstance(), countdownCalendar)

    /** 判断超出天数是否超过了<特殊文本的数量> </特殊文本的数量> */
    private val specialCountdownMsg: String
        get() {
            var index = abs(distanceDays)
            /** 判断超出天数是否超过了<特殊文本的数量> </特殊文本的数量> */
            if (index >= msgSpecialCountdown.size) {
                index = msgSpecialCountdown.size - 1
            }
            return msgSpecialCountdown[index]
        }// 使用<基础语句>

    // 替换$diff_days
    /** 若已经到达<倒计时日期>, 则获取<特殊的倒计时文本> </特殊的倒计时文本></倒计时日期> */
    /** 判断是否已经到达<倒计时日期> </倒计时日期> */
    /**
     * 获取当前Countdown任务要发送的倒计时文本
     */
    val todayCountdownMsg: String
        get() {
            /** 判断是否已经到达<倒计时日期> </倒计时日期> */
            if (distanceDays > 0) {
                return msgBasicCountdown.replace("\$diff_days", distanceDays.toString())
            }
            /** 若已经到达<倒计时日期>, 则获取<特殊的倒计时文本> </特殊的倒计时文本></倒计时日期> */
            return specialCountdownMsg
        }

    override fun toString(): String {
        return ("Countdown [countdown_Command=$countdownCommand"
                + ", countdown_Name=$msgBasicCountdown"
                + ", countdown_Timestamp_Ms=$countdownTimestampMs"
                + ", countdown_Calendar=${countdownCalendar.getDateSimple()}"
                + ", countdown_Msg=$msgSpecialCountdown"
                + ", countdown_Msgs=$msgSpecialCountdownString]")
    }

    companion object {
        val countdownsByCommands: List<Countdown>
            get() = getCountdownsByCommands(PlumConfig.functions.dailyCountdown.countdownCommands)

        /**
         * 传入countdown_commands, 解析并构造出所有的countdown
         */
        fun getCountdownsByCommands(
            countdownCommands: List<String>
        ): List<Countdown> = countdownCommands.map { single ->
            // 逐个构造countdown对象
            Countdown(single).also {
                Plum.logger.debug("Countdown >> Get Countdown: $it")
            }
        }
    }
}