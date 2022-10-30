package com.sakurawald.plum.reloaded.timer

import com.sakurawald.plum.reloaded.Plum
import kotlinx.coroutines.launch
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
    suspend fun sendStage()
    fun autoControlTimer() {
        Plum.launch {
            if (isPrepareStage) {
                prepareStage()
            }
            if (isSendStage) {
                sendStage()
            }
        }
    }
}

