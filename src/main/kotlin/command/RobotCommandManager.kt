package com.sakurawald.plum.reloaded.command

import com.sakurawald.plum.reloaded.Plum
import com.sakurawald.plum.reloaded.command.RobotCommandChatType.Companion.toMessageType
import com.sakurawald.plum.reloaded.command.commands.HelpCommand
import com.sakurawald.plum.reloaded.command.commands.ReloadCommand
import com.sakurawald.plum.reloaded.config.PlumConfig
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.EventChannel
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.event.events.GroupAwareMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain

object RobotCommandManager {
    val commands = mutableListOf(
        HelpCommand,
        ReloadCommand
    )

    fun initialize(channel: EventChannel<Event>) {
        channel.subscribeAlways<MessageEvent> {
            val group = if (it is GroupAwareMessageEvent) it.group else null
            val user = it.sender
            toMessageType()?.run {
                receiveMessage(type, time, group, user, message)
            }
        }
    }

    // TODO: 判断某条信息是否为指令，快速判断，防止网络攻击，优化性能
    fun isCommand(msg: String): Boolean {
        return true
    }

    suspend fun receiveMessage(msgType: Int, time: Int, fromGroup: Group?, fromQQ: User, messageChain: MessageChain) {
        val content = messageChain.contentToString()

        if (content.isEmpty()) return
        if (PlumConfig.debug.enable) {
            Plum.logger.debug("CommandSystem >> Receive Msg -> {$content}")
        }

        // [!] 不处理非命令的消息，优化性能
        // 不处理正常的聊天信息
        // [!] 注意！当@人的时候，电脑QQ发送收到的msg是 “@某人”，但手机发送收到的msg会转成CQ码
        if (!isCommand(content)) return

        commands.firstOrNull {
            it.isThisCommand(content) && it.runCheckUp(msgType, time, fromGroup, fromQQ, messageChain)
        }?.runCommand(msgType, time, fromGroup, fromQQ, messageChain)
    }
}