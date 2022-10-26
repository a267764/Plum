package com.sakurawald.plum.reloaded.function

import com.sakurawald.plum.reloaded.Plum
import com.sakurawald.plum.reloaded.api.ApiHitoKoto
import com.sakurawald.plum.reloaded.api.ApiThirdPartyRandomImage
import com.sakurawald.plum.reloaded.config.PlumConfig
import com.sakurawald.plum.reloaded.utils.sendMessageBySituation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.events.NudgeEvent
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import utils.NetworkUtil

object NudgeFunction : FunctionManager() {

    suspend fun handleEvent(event: NudgeEvent) {
        if (!PlumConfig.functions.NudgeFunction.enable || event.bot.id != Plum.CURRENT_BOT?.id) return

        val fromGroup = if (event.subject is Group) event.subject as Group else null
        val fromQQ = if (event.from is User) event.from as User else return
        val targetQQ = event.target.id

        // Has Nudge Bot ?
        if (targetQQ == event.bot.id) {
            /** 调用间隔合法检测.  */
            if (!canUse(fromGroup?.id ?: -1)) {
                sendMessageBySituation(
                    fromGroup,
                    fromQQ,
                    PlumConfig.functions.FunctionManager.callTooOftenMsg
                )
                Plum.logger.debug("NudgeFunction >> Call too often. Cancel!")
                return
            }
            updateUseTime(fromGroup?.id ?: -1)
            val sendMsg = ApiHitoKoto.randomSentence.formatedString
            runBlocking(Dispatchers.IO) {
                try {
                    // Add RandomImage.
                    val randomImageURL = ApiThirdPartyRandomImage.randomImageURL
                    val image = NetworkUtil.getInputStream(randomImageURL).uploadAsImage(
                        event.bot.groups.stream().findAny().get()
                    )
                    sendMessageBySituation(fromGroup, fromQQ, buildMessageChain {
                        sendMsg?.also { add(it) }
                        add(image)
                    })
                } catch (e: Exception) {
                    // Do nothing.
                }
            }
        }
    }

    override fun canUse(QQGroup: Long): Boolean {
        val interval = PlumConfig.functions.NudgeFunction.perUseIntervalSecond
        return functionUseHistoryManager.isCallSuccessIntervalLegal(
            QQGroup, interval
        )
    }
}