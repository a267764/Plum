package com.sakurawald.plum.reloaded.command.commands

import com.sakurawald.framework.MessageManager
import com.sakurawald.plum.reloaded.Plum.reload
import com.sakurawald.plum.reloaded.command.RobotCommand
import com.sakurawald.plum.reloaded.command.RobotCommandChatType
import com.sakurawald.plum.reloaded.command.RobotCommandUser
import com.sakurawald.plum.reloaded.config.PlumConfig
import net.mamoe.mirai.message.data.MessageChain

object ReloadCommand : RobotCommand(
    "#重载配置.*",
    mutableListOf(
        RobotCommandChatType.FRIEND_CHAT,
        RobotCommandChatType.GROUP_TEMP_CHAT,
        RobotCommandChatType.GROUP_CHAT,
        RobotCommandChatType.STRANGER_CHAT
    ),
    mutableListOf(
        RobotCommandUser.BOT_ADMINISTRATOR
    )
) {
    override fun runCommand(msgType: Int, time: Int, fromGroup: Long, fromQQ: Long, messageChain: MessageChain) {
        PlumConfig.reload()
        MessageManager.sendMessageBySituation(fromGroup, fromQQ, "Reload Configs Successfully!")
    }
}