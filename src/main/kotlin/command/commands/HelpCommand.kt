package com.sakurawald.plum.reloaded.command.commands

import com.sakurawald.framework.MessageManager
import com.sakurawald.plum.reloaded.command.RobotCommand
import com.sakurawald.plum.reloaded.command.RobotCommandChatType
import com.sakurawald.plum.reloaded.command.RobotCommandUser
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
    fun addSuperAdministratorHelp(result: String): String {
        return """$result
             --> Bot Administrator
             #重载配置      重新加载配置文件
             """.trimIndent()
    }

    override fun runCommand(msgType: Int, time: Int, fromGroup: Long, fromQQ: Long, messageChain: MessageChain) {
        var result = """
            --> Help
            #解读      查看今天的每日诗词的解读
            """.trimIndent()
        val authority = RobotCommandUser.getAuthority(fromGroup, fromQQ)

        if (authority == RobotCommandUser.BOT_ADMINISTRATOR.userPermission) {
            result = addSuperAdministratorHelp(result)
        }

        // 处理完文本后，最后发送文本
        MessageManager.sendMessageBySituation(fromGroup, fromQQ, result)
    }
}