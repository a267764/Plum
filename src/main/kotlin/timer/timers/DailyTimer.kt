package com.sakurawald.plum.reloaded.timer.timers

import com.sakurawald.plum.reloaded.Plum
import com.sakurawald.timer.RobotAbstractTimer
import com.sakurawald.timer.TimerController
import com.sakurawald.utils.DateUtil

abstract class DailyTimer(timerName: String?, firstTime: Long, delayTime: Long) :
    RobotAbstractTimer(timerName, firstTime, delayTime), TimerController {
    protected var lastPrepareDay = 0
    protected var lastSendDay = 0
    protected var sendMsg: String? = null
    override fun logDebugTimerState() {
        Plum.logger.debug("TimerSystem >> $timerName: Run")
        Plum.logger.debug("TimerSystem >> $timerName: lastPrepareDay = $lastPrepareDay")
        Plum.logger.debug("TimerSystem >> $timerName: lastSendDay = $lastSendDay")
        Plum.logger.debug("TimerSystem >> $timerName: nowDay = ${DateUtil.getNowDay()}")
    }

    override fun run() {
        autoControlTimer()
    }
}
