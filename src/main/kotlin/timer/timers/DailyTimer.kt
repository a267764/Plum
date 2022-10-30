package com.sakurawald.plum.reloaded.timer.timers

import com.sakurawald.plum.reloaded.timer.RobotAbstractTimer
import com.sakurawald.plum.reloaded.timer.TimerController
import net.mamoe.mirai.message.data.MessageChain

abstract class DailyTimer(timerName: String, firstTime: Long, delayTime: Long) :
    RobotAbstractTimer(timerName, firstTime, delayTime), TimerController {
    protected var lastPrepareDay = 0
    protected var lastSendDay = 0
    protected var sendMsg: MessageChain? = null
    override fun run() {
        autoControlTimer()
    }
}
