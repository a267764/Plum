package com.sakurawald.plum.reloaded.command.commands

import com.sakurawald.plum.reloaded.command.RobotCommand
import com.sakurawald.plum.reloaded.command.RobotCommandChatType
import com.sakurawald.plum.reloaded.command.RobotCommandUser
import com.sakurawald.plum.reloaded.utils.sendMessageBySituation
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.MessageChain

object HelpCommand : RobotCommand(
    "#帮助.*",
    mutableListOf(
        RobotCommandChatType.FRIEND_CHAT,
        RobotCommandChatType.GROUP_TEMP_CHAT,
        RobotCommandChatType.GROUP_CHAT,
        RobotCommandChatType.STRANGER_CHAT
    ),
    mutableListOf(
        RobotCommandUser.NORMAL_USER,
        RobotCommandUser.GROUP_ADMINISTRATOR,
        RobotCommandUser.GROUP_OWNER,
        RobotCommandUser.BOT_ADMINISTRATOR
    )
) {
    override suspend fun runCommand(
        msgType: Int,
        time: Int,
        fromGroup: Group?,
        fromQQ: User,
        messageChain: MessageChain
    ) {
        val result = """
            --> Help
            #解读      查看今天的每日诗词的解读
            """.trimIndent()
        // 处理完文本后，最后发送文本
        sendMessageBySituation(fromGroup, fromQQ, result)
    }
}