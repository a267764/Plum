package com.sakurawald.plum.reloaded

import com.sakurawald.plum.reloaded.command.RobotCommandManager
import com.sakurawald.plum.reloaded.config.PlumConfig
import com.sakurawald.plum.reloaded.function.NudgeFunction
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.*
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

        // Init CommandSystem.
        logger.debug("CommandSystem >> Init CommandSystem.")
        RobotCommandManager

        // Init FileSystem.
        logger.debug("FileSystem >> Init FileSystem.");
        PlumConfig.reload()

        logger.debug("Start to subscribe events")
        val channel = globalEventChannel().exceptionHandler { logger.error(it) }

        channel.subscribeAlways<GroupMessageEvent> {

        }
        channel.subscribeAlways<FriendMessageEvent> {

        }
        channel.subscribeAlways<StrangerMessageEvent> {

        }
        channel.subscribeAlways<GroupTempMessageEvent> {

        }
        channel.subscribeAlways<BotOnlineEvent> {
            if (bot_ == null) bot_ = it.bot
        }
        channel.subscribeAlways<NudgeEvent> { NudgeFunction.handleEvent(it) }
        channel.subscribeAlways<NewFriendRequestEvent> {

        }
        channel.subscribeAlways<BotInvitedJoinGroupRequestEvent> {

        }

        logger.debug("TimerSystem >> Start TimerSystem.")
        RobotCommandManager

        logger.debug("End Init...")
    }

    override fun onDisable() {
        logger.info("PlumReloaded >> Disable.")
    }
}