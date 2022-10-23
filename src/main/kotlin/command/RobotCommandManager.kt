package com.sakurawald.plum.reloaded.command

import com.sakurawald.plum.reloaded.Plum
import com.sakurawald.plum.reloaded.command.commands.HelpCommand
import com.sakurawald.plum.reloaded.command.commands.ReloadCommand
import com.sakurawald.plum.reloaded.config.PlumConfig
import net.mamoe.mirai.message.data.MessageChain

object RobotCommandManager {
    val commands = mutableListOf(
        HelpCommand,
        ReloadCommand
    )

    // TODO: 判断某条信息是否为指令，快速判断，防止网络攻击，优化性能
    fun isCommand(msg: String): Boolean {
        return true
    }

    fun receiveMessage(msgType: Int, time: Int, fromGroup: Long, fromQQ: Long, messageChain: MessageChain) {
        val content = messageChain.contentToString()

        if (content.length == 0) return
        if (PlumConfig.debug.enable) {
            Plum.logger.debug("CommandSystem >> Receive Msg -> {$content}")
        }

        // [!] 不处理非命令的消息，优化性能
        // 不处理正常的聊天信息
        // [!] 注意！当@人的时候，电脑QQ发送收到的msg是 “@某人”，但手机发送收到的msg会转成CQ码
        if (!isCommand(content)) {
            return
        }

        commands.firstOrNull {
            it.isThisCommand(content) && it.runCheckUp(msgType, time, fromGroup, fromQQ, messageChain)
        }?.runCommand(msgType, time, fromGroup, fromQQ, messageChain)
    }
}