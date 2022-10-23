package com.sakurawald.plum.reloaded.timer

import java.util.*

abstract class RobotAbstractTimer(
    val timerName: String,
    val firstTime: Long,
    val delayTime: Long
) : TimerTask()

interface DefaultPlan {
    fun defaultPlan()
}

interface TimerController {
    val isPrepareStage: Boolean
    val isSendStage: Boolean

    fun prepareStage()
    fun sendStage()
    fun autoControlTimer() {
        Thread {
            if (isPrepareStage) {
                prepareStage()
            }
            if (isSendStage) {
                sendStage()
            }
        }.start()
    }
}

