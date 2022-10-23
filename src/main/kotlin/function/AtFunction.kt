package com.sakurawald.plum.reloaded.function

import com.sakurawald.api.QingYunKe_API
import com.sakurawald.framework.MessageManager
import com.sakurawald.plum.reloaded.Plum
import com.sakurawald.plum.reloaded.config.PlumConfig
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At

object AtFunction {

    fun handleEvent(event: GroupMessageEvent) {
        if (!PlumConfig.functions.AtFunction.enable || event.bot != Plum.CURRENT_BOT) return

        // Has @Bot ?
        if (event.message.contains(At(Plum.CURRENT_BOT?.id ?: return))) {
            val fromGroup = event.group.id
            val fromQQ = event.sender.id
            val receiveMsg = event.message.contentToString()

            // Get Answer And SendMsg.
            val sendMsg = QingYunKe_API.getAnswer(receiveMsg)
            MessageManager.sendMessageBySituation(fromGroup, fromQQ, sendMsg)
        }
    }
}