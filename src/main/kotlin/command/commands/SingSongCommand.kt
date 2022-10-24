package com.sakurawald.plum.reloaded.command.commands

import com.sakurawald.api.KugouMusicAPI
import com.sakurawald.api.MusicPlatAPI
import com.sakurawald.api.NeteaseCloudMusicAPI
import com.sakurawald.api.TencentMusicAPI
import com.sakurawald.plum.reloaded.SongInformation
import com.sakurawald.exception.CanNotDownloadFileException
import com.sakurawald.exception.FileTooBigException
import com.sakurawald.framework.BotManager
import com.sakurawald.framework.MessageManager
import com.sakurawald.plum.reloaded.Plum
import com.sakurawald.plum.reloaded.command.RobotCommand
import com.sakurawald.plum.reloaded.command.RobotCommandChatType
import com.sakurawald.plum.reloaded.command.RobotCommandUser
import com.sakurawald.plum.reloaded.config.PlumConfig
import com.sakurawald.plum.reloaded.function.SingManager
import com.sakurawald.utils.LanguageUtil
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
    override fun runCommand(msgType: Int, time: Int, fromGroup: Long, fromQQ: Long, messageChain: MessageChain) {
        /** 功能开关判断 **/
        if (!PlumConfig.functions.SingSongFunction.enable) return

        /** 引导式帮助 **/
        var msg = messageChain.contentToString()
        if (msg.matches(Regex("^(?:(?:唱歌)|(?:唱)|(?:点歌)|(?:听歌)|(?:我想听)|(?:来首)|(?:想听)|(?:给我唱))[\\s]*$"))) {
            val help = """用法示例：
"唱歌 霜雪千年"
"唱歌 霜雪千年 $RANDOM_SING_FLAG"
"唱歌 霜雪千年 网易云/酷狗/QQ""""
            MessageManager.sendMessageBySituation(fromGroup, fromQQ, help)
            return
        }
        msg = msg.lowercase()

        /** 唱歌指令判断逻辑 **/
        val matcher = pattern.matcher(msg)

        // 判断 是否符合 唱歌指令要求
        if (matcher.find()) {
            /** Function系统  */
            val finalMsg = msg
            Thread(Runnable {
                Plum.logger.debug("SingSong >> 收到唱歌指令，开始执行核心代码")
                /** 判断唱歌间隔是否合法 **/
                if (!SingManager.canUse(fromGroup)) {
                    MessageManager.sendMessageBySituation(
                        fromGroup,
                        fromQQ,
                        PlumConfig.functions.FunctionManager.callTooOftenMsg
                    )
                    Plum.logger.debug("SingSongFunction >> Call too often. Cancel!")
                    return@Runnable
                }
                /**
                 * 更新唱歌间隔 [!] 只要执行了唱歌核心代码，无论最后是否成功发送语音文件，都更新lastSingTime
                 */
                SingManager.updateUseTime(fromGroup)
                /** SongInformation获取逻辑  \**/
                var input_music_name = matcher.group(1)
                Plum.logger.debug("SingSong >> : input_music_name = $input_music_name")

                // [!] 使用用户输入的歌曲名在网络上找歌曲
                var si: SongInformation? = null

                // 点歌 -> 以卡片形式分享.
                var send_card_flag = finalMsg.contains("点歌") || PlumConfig.functions.SingSongFunction.forceSendCard
                val random_music_flag = SingManager.isRandomSing(finalMsg)

                // 获得干净的音乐名
                input_music_name = SingManager.deleteParams(input_music_name)
                /** Construct MusicPlats.  */
                val musicPlatAPIS = listOf(
                    NeteaseCloudMusicAPI.instance,
                    KugouMusicAPI.instance,
                    TencentMusicAPI.instance
                )
                // Select MusicPlat.
                for (i in musicPlatAPIS.indices) {
                    // Has Any SelectCodes ?
                    for (selectCode: String in musicPlatAPIS[i].selectCodes) {
                        if (finalMsg.contains(" $selectCode")) {
                            Collections.swap(musicPlatAPIS, 0, i)
                            finalMsg.replace(" $selectCode", "")
                            break
                        }
                    }
                }
                /** Try All MusicPlats.  */
                Plum.logger.debug("SingSong >> input_music_name = $input_music_name")
                var mpa: MusicPlatAPI? = null
                for (i in musicPlatAPIS.indices) {
                    Plum.logger.debug("SingSong >> 尝试第 ${i + 1} 乐库: ${musicPlatAPIS[i]!!.logTypeName}")
                    mpa = musicPlatAPIS[i]
                    si = mpa!!.checkAndGetSongInformation(input_music_name, random_music_flag)
                    if (si != null) {
                        // FIX: 当音乐平台是QQ音乐时, 强制以卡片形式发送.
                        if (mpa is TencentMusicAPI) send_card_flag = true
                        break
                    }
                }
                /** 搜索不到指定的音乐, 结束代码  */
                /** 搜索不到指定的音乐, 结束代码  */
                if (si == null) {
                    Plum.logger.debug("SingSong >> 所唱的歌曲搜索不到, 结束代码: input_music_name = $input_music_name")
                    MessageManager.sendMessageBySituation(
                        fromGroup, fromQQ,
                        LanguageUtil.transObject_X(
                            1,
                            PlumConfig.functions.SingSongFunction.not_found_music_msg,
                            input_music_name
                        )
                    )
                    return@Runnable
                }
                /** 音乐发送逻辑  */
                if (!send_card_flag) {
                    /** 音乐文件下载逻辑  */
                    // 若音乐文件不存在时，尝试下载音乐
                    mpa!!.getDownloadPath(si.music_Name, si.music_ID)
                    try {
                        mpa.downloadMusic(si)
                    } catch (e: CanNotDownloadFileException) {
                        MessageManager.sendMessageBySituation(
                            fromGroup,
                            fromQQ,
                            PlumConfig.functions.SingSongFunction.music_need_paid_msg
                        )
                        return@Runnable
                    } catch (e: FileTooBigException) {
                        MessageManager.sendMessageBySituation(
                            fromGroup,
                            fromQQ,
                            PlumConfig.functions.SingSongFunction.download_music_file_too_big_msg
                        )
                        return@Runnable
                    }
                    MessageManager.sendVoiceToQQGroup(
                        fromGroup, mpa.getDownloadFileName(si.music_Name, si.music_ID)
                    )
                } else {
                    // 将 命令调用者 的信息 附加到SongInformation上.
                    val orderMember = BotManager.getGroupMemberCard(fromGroup, fromQQ)
                    si.summary = "[点歌] " + BotManager.getGroupMemberName(orderMember)
                    si.img_URL = orderMember.avatarUrl
                    MessageManager.sendMessageBySituation(
                        fromGroup, fromQQ,
                        mpa!!.getCardCode(si)
                    )
                }
            }).start()
        }
    }
}