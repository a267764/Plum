package com.sakurawald.plum.reloaded.command.commands

import com.sakurawald.plum.reloaded.Plum
import com.sakurawald.plum.reloaded.SongInformation
import com.sakurawald.plum.reloaded.api.AbstractApiMusicPlat
import com.sakurawald.plum.reloaded.api.ApiKugouMusic
import com.sakurawald.plum.reloaded.api.ApiNeteaseCloudMusic
import com.sakurawald.plum.reloaded.api.ApiTencentMusic
import com.sakurawald.plum.reloaded.command.RobotCommand
import com.sakurawald.plum.reloaded.command.RobotCommandChatType
import com.sakurawald.plum.reloaded.command.RobotCommandUser
import com.sakurawald.plum.reloaded.config.PlumConfig
import com.sakurawald.plum.reloaded.function.SingManager
import com.sakurawald.plum.reloaded.utils.*
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.data.MessageChain
import java.util.*

object SingSongCommand : RobotCommand(
    "^(?:(?:唱歌)|(?:唱)|(?:点歌)|(?:听歌)|(?:我想听)|(?:来首)|(?:想听)|(?:给我唱))[\\s\\S]*$",
    mutableListOf(
        RobotCommandChatType.GROUP_CHAT
    ),
    mutableListOf(
        RobotCommandUser.NORMAL_USER,
        RobotCommandUser.GROUP_ADMINISTRATOR,
        RobotCommandUser.GROUP_OWNER,
        RobotCommandUser.BOT_ADMINISTRATOR
    )
) {
    const val RANDOM_SING_FLAG = "-random"
    override suspend fun runCommand(msgType: Int, time: Int, fromGroup: Group?, fromQQ: User, messageChain: MessageChain) {
        /** 功能开关判断 **/
        if (!PlumConfig.functions.singSongFunction.enable) return

        /** 引导式帮助 **/
        var msg = messageChain.contentToString()
        if (msg.matches(Regex("^(?:(?:唱歌)|(?:唱)|(?:点歌)|(?:听歌)|(?:我想听)|(?:来首)|(?:想听)|(?:给我唱))\\s*$"))) {
            val help = """用法示例：
"唱歌 霜雪千年"
"唱歌 霜雪千年 $RANDOM_SING_FLAG"
"唱歌 霜雪千年 网易云/酷狗/QQ""""
            sendMessageBySituation(fromGroup, fromQQ, help)
            return
        }
        msg = msg.lowercase()

        /** 唱歌指令判断逻辑 **/
        val matcher = pattern.matcher(msg)

        // 判断 是否符合 唱歌指令要求
        if (matcher.find()) {
            /** Function系统  */
            val finalMsg = msg
            Plum.launch {
                Plum.logger.debug("SingSong >> 收到唱歌指令，开始执行核心代码")
                /** 判断唱歌间隔是否合法 **/
                if (!SingManager.canUse(fromGroup?.id ?: 0)) {
                        sendMessageBySituation(
                            fromGroup,
                            fromQQ,
                            PlumConfig.functions.functionManager.callTooOftenMsg
                        )

                    Plum.logger.debug("SingSongFunction >> Call too often. Cancel!")
                    return@launch
                }
                /**
                 * 更新唱歌间隔 [!] 只要执行了唱歌核心代码，无论最后是否成功发送语音文件，都更新lastSingTime
                 */
                SingManager.updateUseTime(fromGroup?.id ?: 0)
                /** SongInformation获取逻辑  \**/
                var inputMusicName = matcher.group(1)
                Plum.logger.debug("SingSong >> : input_music_name = $inputMusicName")

                // [!] 使用用户输入的歌曲名在网络上找歌曲
                var info: SongInformation? = null

                // 点歌 -> 以卡片形式分享.
                var sendCardFlag = finalMsg.contains("点歌") || PlumConfig.functions.singSongFunction.forceSendCard
                val isRandomMusic = SingManager.isRandomSing(finalMsg)

                // 获得干净的音乐名
                inputMusicName = SingManager.deleteParams(inputMusicName)
                /** Construct MusicPlats.  */
                val apiList = listOf(
                    ApiNeteaseCloudMusic,
                    ApiKugouMusic,
                    ApiTencentMusic
                )
                // Select MusicPlat.
                for (i in apiList.indices) {
                    // Has Any SelectCodes ?
                    for (selectCode: String in apiList[i].selectCodes) {
                        if (finalMsg.contains(" $selectCode")) {
                            Collections.swap(apiList, 0, i)
                            finalMsg.replace(" $selectCode", "")
                            break
                        }
                    }
                }
                /** Try All MusicPlats.  */
                Plum.logger.debug("SingSong >> input_music_name = $inputMusicName")
                var api: AbstractApiMusicPlat? = null
                for (i in apiList.indices) {
                    Plum.logger.debug("SingSong >> 尝试第 ${i + 1} 乐库: ${apiList[i].logTypeName}")
                    api = apiList[i]
                    info = api.checkAndGetSongInformation(inputMusicName, isRandomMusic)
                    if (info != null) {
                        // FIX: 当音乐平台是QQ音乐时, 强制以卡片形式发送.
                        if (api is ApiTencentMusic) sendCardFlag = true
                        break
                    }
                }
                /** 搜索不到指定的音乐, 结束代码  */
                if (api == null || info == null) {
                    Plum.logger.debug("SingSong >> 所唱的歌曲搜索不到, 结束代码: input_music_name = $inputMusicName")
                    sendMessageBySituation(
                        fromGroup, fromQQ,
                        transObjectX(
                            1,
                            PlumConfig.functions.singSongFunction.msgNotFoundMusic,
                            inputMusicName
                        )
                    )
                    return@launch
                }
                /** 音乐发送逻辑  */
                if (!sendCardFlag) {
                    /** 音乐文件下载逻辑  */
                    // 若音乐文件不存在时，尝试下载音乐
                    api.getDownloadPath(info.name, info.id)
                    try {
                        api.downloadMusic(info)
                    } catch (e: CanNotDownloadFileException) {
                        sendMessageBySituation(
                            fromGroup,
                            fromQQ,
                            PlumConfig.functions.singSongFunction.msgMusicNeedPaid
                        )
                        return@launch
                    } catch (e: FileTooBigException) {
                        sendMessageBySituation(
                            fromGroup,
                            fromQQ,
                            PlumConfig.functions.singSongFunction.msgDownloadMusicFileTooBig
                        )
                        return@launch
                    }

                    fromGroup?.uploadMusic(api.getDownloadFileName(info.name, info.id))?.let {
                        fromGroup.sendMessage(it)
                    }
                } else {
                    // 将 命令调用者 的信息 附加到SongInformation上.
                    val orderMember = fromQQ.nameCardOrNick
                    info.summary = "[点歌] $orderMember"
                    info.imageUrl = fromQQ.avatarUrl
                    sendMessageBySituation(fromGroup, fromQQ, api.getCardCode(info))
                }
            }
        }
    }
}