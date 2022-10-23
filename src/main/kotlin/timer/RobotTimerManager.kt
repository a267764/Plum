package com.sakurawald.plum.reloaded.timer

import com.sakurawald.plum.reloaded.Plum
import com.sakurawald.plum.reloaded.timer.timers.DailyPoetry_Timer
import com.sakurawald.plum.reloaded.timer.timers.DailySentenceTimer
import java.util.*

object RobotTimerManager : Timer() {
    val tasks = mutableListOf(
        DailySentenceTimer,
        DailyPoetry_Timer
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