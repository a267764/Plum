package com.sakurawald.plum.reloaded.function

import com.sakurawald.api.HitoKoto_API
import com.sakurawald.api.ThirdPartyRandomImage_API
import com.sakurawald.plum.reloaded.Plum
import com.sakurawald.framework.MessageManager
import com.sakurawald.plum.reloaded.config.PlumConfig
import com.sakurawald.utils.NetworkUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.NudgeEvent
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage

object NudgeFunction : FunctionManager() {

    fun handleEvent(event: NudgeEvent) {
        if (!PlumConfig.functions.NudgeFunction.enable || event.bot.id != Plum.CURRENT_BOT?.id) return

        val fromGroup = if (event.subject is Group) event.subject.id else -1
        val fromQQ = event.from.id
        val targetQQ = event.target.id

        // Has Nudge Bot ?
        if (targetQQ == event.bot.id) {
            /** 调用间隔合法检测.  */
            if (!canUse(fromGroup)) {
                MessageManager.sendMessageBySituation(
                    fromGroup,
                    fromQQ,
                    PlumConfig.functions.FunctionManager.callTooOftenMsg
                )
                Plum.logger.debug("NudgeFunction >> Call too often. Cancel!")
                return
            }
            updateUseTime(fromGroup)
            var sendMsg = HitoKoto_API.randomSentence.formatedString
            runBlocking(Dispatchers.IO) {
                try {
                    // Add RandomImage.
                    val randomImageURL = ThirdPartyRandomImage_API.instance.randomImageURL
                    val uploadImage = NetworkUtil.getInputStream(randomImageURL).uploadAsImage(
                        event.bot.groups.stream().findAny().get()
                    )
                    sendMsg += """
                [mirai:image:${uploadImage.imageId}]
                """.trimIndent()
                } catch (e: Exception) {
                    // Do nothing.
                }
                MessageManager.sendMessageBySituation(fromGroup, fromQQ, sendMsg)
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