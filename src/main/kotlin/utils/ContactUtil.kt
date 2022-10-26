package com.sakurawald.plum.reloaded.utils

import com.sakurawald.plum.reloaded.Plum
import com.sakurawald.plum.reloaded.api.AbstractApiMusicPlat
import com.sakurawald.plum.reloaded.config.PlumConfig
import kotlinx.coroutines.delay
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import java.io.File

fun checkLengthAndModifySendMsg(
    sendMsg: String, defaultValue: (String) -> String = {
        "很抱歉，本次发送的字数超过上限，已取消发送！\n字数：${it.length}"
    }
): String {
    Plum.logger.debug("SendSystem >> checkSendMsgLength() -> length = ${sendMsg.length}")
    return if (sendMsg.length >= PlumConfig.system.SendSystem.sendMsgMaxLength) defaultValue(sendMsg) else sendMsg
}

fun transSendMsgSpecialCode(message: String): String = message
    .replace("#space", " ", true)
    .replace("#enter", "\n", true)

suspend fun sendMessageBySituation(fromGroup: Group?, fromQQ: User?, msg: String) =
    sendMessageBySituation(fromGroup, fromQQ, PlainText(msg).toMessageChain())

suspend fun sendMessageBySituation(fromGroup: Group?, fromQQ: User?, msg: MessageChain) {
    Plum.logger.debug("SendSystem >> sendMessageBySituation(): fromGroup = ${fromGroup?.id ?: "null"}, fromQQ = ${fromQQ?.id ?: "null"}")
    if (fromGroup == null && fromQQ == null) return
    fromGroup?.sendMessage(buildMessageChain {
        if (fromQQ != null) add(At(fromQQ.id))
        add(msg)
    }) ?: run {
        if (fromQQ == null) {
            Plum.logger.debug("SendSystem >> sendMessageBySituation(): can't find send target ->  fromGroup = ${fromGroup?.id ?: "null"}, fromQQ = null}")
            return
        }
        fromQQ.sendMessage(msg)
    }
}

suspend fun sendDelay(isSendToGroup: Boolean) {
    val delayTimeMS = if (isSendToGroup && PlumConfig.system.SendSystem.SendDelay.SendToGroup.enable)
        PlumConfig.system.SendSystem.SendDelay.SendToGroup.delayTimeMS
    else if (!isSendToGroup && PlumConfig.system.SendSystem.SendDelay.SendToFriend.enable)
        PlumConfig.system.SendSystem.SendDelay.SendToFriend.delayTimeMS
    else return
    Plum.logger.debug("GuardSystem >> Send Delay ${delayTimeMS}MS~")
    delay(delayTimeMS)
}

suspend fun Bot.sendToAllFriends(msg: MessageChain) {
    Plum.logger.debug("SendSystem >> sendToAllQQFriends: totally ${friends.size} friends.")
    if (PlumConfig.admin.RobotControl.forceCancel_FriendMessage) {
        Plum.logger.debug("SendSystem >> Force Cancel Send!")
        return
    }

    for (friend in friends) {
        sendDelay(false)
        friend.sendMessage(msg)
    }
}

suspend fun Bot.sendToAllStrangers(msg: MessageChain) {
    Plum.logger.debug("SendSystem >> sendToAllStrangers: totally ${strangers.size} strangers.")
    if (PlumConfig.admin.RobotControl.forceCancel_FriendMessage) {
        Plum.logger.debug("SendSystem >> Force Cancel Send!")
        return
    }

    for (stranger in strangers) {
        sendDelay(false)
        stranger.sendMessage(msg)
    }
}

suspend fun Bot.sendToAllGroups(msg: MessageChain) {
    Plum.logger.debug("SendSystem >> sendToAllGroups: totally ${groups.size} groups.")
    if (PlumConfig.admin.RobotControl.forceCancel_GroupMessage) {
        Plum.logger.debug("SendSystem >> Force Cancel Send!")
        return
    }

    for (group in groups) {
        sendDelay(true)
        group.sendMessage(msg)
    }
}

suspend fun Group.uploadMusic(fileName: String): SingleMessage {
    Plum.logger.debug("SendSystem >> uploadMusic() -> voice_file_name = $fileName")
    val uploadVoiceFile = File(AbstractApiMusicPlat.voicesPath + fileName)
    return uploadAudio(uploadVoiceFile.toExternalResource())
    // 过时类型 Voice 在新版本已被隐藏，无需处理
}