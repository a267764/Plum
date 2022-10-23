package com.sakurawald.plum.reloaded.function

import com.sakurawald.plum.reloaded.command.commands.SingSongCommand
import com.sakurawald.plum.reloaded.config.PlumConfig

object SingManager : FunctionManager() {

    override fun canUse(QQGroup: Long): Boolean {
        val interval: Int =
            PlumConfig.functions.SingSongFunction.perUseIntervalSecond
        return functionUseHistoryManager.isCallSuccessIntervalLegal(
            QQGroup, interval
        )
    }

    /** 清理掉所有的与"唱歌功能"有关的附加参数  */
    fun deleteParams(music_name_text: String): String? {
        return music_name_text.lowercase().replace(SingSongCommand.RANDOM_SING_FLAG, "")
    }

    /** 判断是否要从音乐列表中随机抽取音乐，而不是抽取第一首  */
    fun isRandomSing(music_name_text: String): Boolean {
        return music_name_text.lowercase().contains(SingSongCommand.RANDOM_SING_FLAG)
    }

}