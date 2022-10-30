package com.sakurawald.plum.reloaded.timer

import com.sakurawald.plum.reloaded.Plum
import com.sakurawald.plum.reloaded.timer.timers.TimerDailyPoetry
import com.sakurawald.plum.reloaded.timer.timers.TimerDailySentence
import java.util.*

object RobotTimerManager : Timer() {
    val tasks = mutableListOf<RobotAbstractTimer>(
        TimerDailySentence,
        TimerDailyPoetry
    )

    init {
        Plum.logger.debug("TimerSystem >> Start Timers.")
        registerAll()
    }

    // 注册/开始所有任务
    fun registerAll() {
        tasks.forEach { task ->
            schedule(task, task.firstTime, task.delayTime)
        }
    }
}