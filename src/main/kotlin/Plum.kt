package com.sakurawald.plum.reloaded

import com.sakurawald.plum.reloaded.command.RobotCommandManager
import com.sakurawald.plum.reloaded.config.PlumConfig
import com.sakurawald.plum.reloaded.function.NudgeFunction
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import net.mamoe.mirai.event.events.NudgeEvent
import net.mamoe.mirai.event.globalEventChannel

object Plum : KotlinPlugin(
    JvmPluginDescription(
        id = "com.sakurawald.plum.reloaded",
        name = "PlumReloaded",
        version = "0.1.0",
    ) {
        author("MrXiaoM")
    }
) {
    private var bot_: Bot? = null
    val CURRENT_BOT get() = bot_

    override fun onEnable() {
        logger.debug("PlumReloaded >> Enabling.")
        logger.debug("Start init...")

        // Init FileSystem.
        logger.debug("FileSystem >> Init FileSystem.");
        PlumConfig.reload()

        logger.debug("Start to subscribe events")
        val channel = globalEventChannel().exceptionHandler { logger.error(it) }

        // Init CommandSystem.
        logger.debug("CommandSystem >> Init CommandSystem.")
        /** 接收群/好友/陌生人/群临时消息事件 **/
        RobotCommandManager.initialize(channel)

        channel.subscribeAlways<BotOnlineEvent> {
            if (bot_ == null) bot_ = it.bot
        }
        channel.subscribeAlways<NudgeEvent> { NudgeFunction.handleEvent(it) }
        channel.subscribeAlways<NewFriendRequestEvent> {
            // 自动处理好友邀请
            if (PlumConfig.admin.InvitationManager.QQFriendInvitation.autoAcceptAddQQFriend) {
                // 同意 -> 好友添加请求
                logger.debug(
                    "ContactSystem >> Accept -> FriendAddRequest: $fromId"
                )
                accept()
            }
            // 不自动拒绝
            // else reject(false)
        }
        channel.subscribeAlways<BotInvitedJoinGroupRequestEvent> {
            if (PlumConfig.admin.InvitationManager.QQGroupInvitation.autoAcceptAddQQGroup) {
                accept()
                logger.debug(
                    "ContactSystem >> Accept -> InvitedJoinGroupRequest: $groupId"
                )
            }
        }

        logger.debug("TimerSystem >> Start TimerSystem.")
        RobotCommandManager

        logger.debug("End Init...")
    }

    override fun onDisable() {
        logger.info("PlumReloaded >> Disable.")
    }
}