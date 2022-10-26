package com.sakurawald.plum.reloaded

import com.sakurawald.plum.reloaded.config.PlumConfig
import utils.DateUtil
import java.util.*
import kotlin.math.abs

/**
 * 描述一个具体的Countdown项目
 */
class Countdown(countdown_command: String) {
    private val special_Countdown_Msg = ArrayList<String>()
    private var countdown_Command: String
    private var countdown_BasicCountdownMsg: String
    private var countdown_Timestamp_Ms: Long
    private var countdown_Calendar: Calendar
    private var special_Countdown_Msgs: String

    init {
        /** 对单条Countdown_Command进行分割  */
        val temp = countdown_command.split("\\|".toRegex(), limit = 3).toTypedArray()
        countdown_Command = countdown_command
        countdown_BasicCountdownMsg = temp[0]
        countdown_Timestamp_Ms = temp[1].toLong()
        countdown_Calendar = DateUtil
            .translate_TimeStamp_Ms_To_Calendar(countdown_Timestamp_Ms)
        special_Countdown_Msgs = temp[2]
        special_Countdown_Msg.addAll(
            Arrays.asList(
                *special_Countdown_Msgs.split("&".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()))
    }

    /**
     * 获取今天距离倒计时日期还有几天, 若今天为倒计时当天, 则返回0. 超过倒计时当天, 则返回负数
     */
    private val distanceDays: Int
        get() = DateUtil.differentDaysByMillisecond(
            Calendar.getInstance(), countdown_Calendar
        )

    /** 判断超出天数是否超过了<特殊文本的数量> </特殊文本的数量> */
    private val specialCountdownMsg: String
        get() {
            val distanceDays = distanceDays
            var index = abs(distanceDays)
            /** 判断超出天数是否超过了<特殊文本的数量> </特殊文本的数量> */
            if (index >= special_Countdown_Msg.size) {
                index = special_Countdown_Msg.size - 1
            }
            return special_Countdown_Msg[index]
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
                return countdown_BasicCountdownMsg.replace("\$diff_days", distanceDays.toString())
            }
            /** 若已经到达<倒计时日期>, 则获取<特殊的倒计时文本> </特殊的倒计时文本></倒计时日期> */
            return specialCountdownMsg
        }

    override fun toString(): String {
        return ("Countdown [countdown_Command=$countdown_Command"
                + ", countdown_Name=$countdown_BasicCountdownMsg"
                + ", countdown_Timestamp_Ms=$countdown_Timestamp_Ms"
                + ", countdown_Calendar=${DateUtil.getDateSimple(countdown_Calendar)}"
                + ", countdown_Msg=$special_Countdown_Msg"
                + ", countdown_Msgs=$special_Countdown_Msgs]")
    }

    companion object {
        val countdownsByCommands: ArrayList<Countdown>
            get() = getCountdownsByCommands(PlumConfig.functions.DailyCountdown.countdown_commands)

        /**
         * 传入countdown_commands, 解析并构造出所有的countdown
         */
        fun getCountdownsByCommands(
            countdown_commands: List<String>
        ): ArrayList<Countdown> {
            val result = ArrayList<Countdown>()
            /** 分离出单条的countdown_command  */
            for (single_command in countdown_commands) {
                // 逐个构造countdown对象
                val cd = Countdown(single_command)
                result.add(cd)
                Plum.logger.debug("Countdown >> Get Countdown: $cd")
            }
            return result
        }
    }
}