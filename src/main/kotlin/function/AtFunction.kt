package com.sakurawald.plum.reloaded.function

import com.sakurawald.plum.reloaded.Plum
import com.sakurawald.plum.reloaded.api.ApiQingYunKe
import com.sakurawald.plum.reloaded.config.PlumConfig
import com.sakurawald.plum.reloaded.utils.sendMessageBySituation
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At

object AtFunction {
    suspend fun handleEvent(event: GroupMessageEvent) {
        if (!PlumConfig.functions.atFunction.enable || event.bot != Plum.CURRENT_BOT) return

        // Has @Bot ?
        if (event.message.any { it is At && it.target == event.bot.id }) {
            val fromGroup = event.group
            val fromQQ = event.sender
            val receiveMsg = event.message.contentToString()

            // Get Answer And SendMsg.
            val sendMsg = ApiQingYunKe.getAnswer(receiveMsg)
            sendMessageBySituation(fromGroup, fromQQ, sendMsg)
        }
    }
}