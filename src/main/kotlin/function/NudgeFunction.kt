package com.sakurawald.plum.reloaded.function

import com.sakurawald.api.HitoKoto_API
import com.sakurawald.api.ThirdPartyRandomImage_API
import com.sakurawald.debug.LoggerManager
import com.sakurawald.framework.MessageManager
import com.sakurawald.function.NudgeFunction
import com.sakurawald.plum.reloaded.config.PlumConfig
import com.sakurawald.utils.NetworkUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.NudgeEvent
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage

object NudgeFunction : FunctionManager() {

    fun handleEvent(event: NudgeEvent) {
        if (!PlumConfig.functions.NudgeFunction.enable) return

        val fromGroup = if (event.subject is Group) event.subject.id else -1
        val fromQQ = event.from.id
        val targetQQ = event.target.id

        // Has Nudge Bot ?
        if (targetQQ == PluginMain.getCurrentBot().id) {
            /** 调用间隔合法检测.  */
            if (!NudgeFunction.getInstance().canUse(fromGroup)) {
                MessageManager.sendMessageBySituation(
                    fromGroup,
                    fromQQ,
                    PlumConfig.functions.FunctionManager.callTooOftenMsg
                )
                LoggerManager.logDebug("NudgeFunction", "Call too often. Cancel!", true)
                return
            }
            NudgeFunction.getInstance().updateUseTime(fromGroup)
            var sendMsg = HitoKoto_API.getRandomSentence().formatedString
            runBlocking(Dispatchers.IO) {
                try {
                    // Add RandomImage.
                    val randomImageURL = ThirdPartyRandomImage_API.getInstance().randomImageURL
                    val uploadImage = NetworkUtil.getInputStream(randomImageURL).uploadAsImage(
                        PluginMain.getCurrentBot().groups.stream().findAny().get()
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