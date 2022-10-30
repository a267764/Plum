package com.sakurawald.plum.reloaded.config

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.ReadOnlyPluginConfig
import net.mamoe.mirai.console.data.value

/**
 * Plum 配置文件
 * src/main/java/com/sakurawald/files/ApplicationConfig_Data.java
 */
object PlumConfig : ReadOnlyPluginConfig("config") {
    val debug by value(
        PlumConfigDebug(
            enable = false
        )
    )
    val admin by value(
        PlumConfigAdmin(
            invitationManager = AdminInvitationManager(
                friendInvitation = IMQQFriendInvitation(
                    autoAcceptAddQQFriend = false
                ),
                groupInvitation = IMQQGroupInvitation(
                    autoAcceptAddQQGroup = false
                )
            ),
            robotControl = AdminRobotControl(
                forceCancelGroupMessage = false,
                forceCancelFriendMessage = true
            ),
            botAdministrators = listOf(
                2431208142L
            )
        )
    )
    val functions by value(
        PlumConfigFunctions(
            functionManager = FunctionsFunctionManager(
                callTooOftenMsg = "调用该功能过于频繁，请稍后再试试吧~",
                functionDisableMsg = "很抱歉，该功能已被禁用。"
            ),
            dailyCountdown = FunctionsDailyCountdown(
                enable = true,
                countdownCommands = listOf(
                    "◆距离2021年高考还有\$diff_days天！|1623027804000|高考加油！&高考加油！&2021年高考已结束~",
                    "◆距离2020年考研还有\$diff_days天！|1608512604000|考研加油&考研加油&考研加油&2020年考研已结束~",
                    "◆距离2020年四六级考试还有\$diff_days天！|1600477404000|四六级考试加油！&四六级考试已结束~"
                )
            ),
            dailyPoetry = FunctionsDailyPoetry(
                jinRiShiCi = DPJinRiShiCi(
                    token = "paOa0DqOdpLn4FVVHNtEDgU5Imk89kXZ"
                ),
                explanationEnable = true,
                maxRetryLimit = 3
            ),
            atFunction = FunctionsAtFunction(
                enable = false,
                randomImage = AFRandomImage(
                    randomImageURLs = listOf(
                        "http://www.dmoe.cc/random.php",
                        "http://api.mtyqx.cn/tapi/random.php",
                        "http://api.mtyqx.cn/api/random.php"
                    )
                )
            ),
            nudgeFunction = FunctionsNudgeFunction(
                enable = true,
                perUseIntervalSecond = 3,
                hitoKoto = NFHitoKoto(
                    getURLParams = ""
                )
            ),
            singSongFunction = FunctionsSingSongFunction(
                enable = true,
                forceSendCard = false,
                perUseIntervalSecond = 5,
                maxVoiceFileSize = 15000000,
                msgMusicNeedPaid = "很抱歉，这可能是一首付费歌曲。",
                msgDownloadMusicFileTooBig = "这首歌太长啦！",
                msgNotFoundMusic = "哎呀，咱还不会《[OBJECT1]》这首歌！"
            )
        )
    )
    val system by value(
        PlumConfigSystem(
            sendSystem = SystemSendSystem(
                sendDelay = SSendDelay(
                    sendToFriend = SDSendToFriend(
                        enable = false,
                        delayTimeMS = 0L
                    ),
                    sendToGroup = SDSendToGroup(
                        enable = true,
                        delayTimeMS = 1000L
                    )
                ),
                sendMsgMaxLength = 4500
            )
        )
    )
}

@Serializable
data class PlumConfigDebug(
    val enable: Boolean
)

@Serializable
data class PlumConfigAdmin(
    val invitationManager: AdminInvitationManager,
    val robotControl: AdminRobotControl,
    val botAdministrators: List<Long>
)

@Serializable
data class AdminInvitationManager(
    val friendInvitation: IMQQFriendInvitation,
    val groupInvitation: IMQQGroupInvitation
)

@Serializable
data class IMQQFriendInvitation(
    val autoAcceptAddQQFriend: Boolean
)

@Serializable
data class IMQQGroupInvitation(
    val autoAcceptAddQQGroup: Boolean
)

@Serializable
data class AdminRobotControl(
    var forceCancelGroupMessage: Boolean,
    var forceCancelFriendMessage: Boolean
)

@Serializable
data class PlumConfigFunctions(
    val functionManager: FunctionsFunctionManager,
    val dailyCountdown: FunctionsDailyCountdown,
    val dailyPoetry: FunctionsDailyPoetry,
    val atFunction: FunctionsAtFunction,
    val nudgeFunction: FunctionsNudgeFunction,
    val singSongFunction: FunctionsSingSongFunction,
)

@Serializable
data class FunctionsFunctionManager(
    var callTooOftenMsg: String,
    var functionDisableMsg: String
)

@Serializable
data class FunctionsDailyCountdown(
    var enable: Boolean,
    var countdownCommands: List<String>
)

@Serializable
data class FunctionsDailyPoetry(
    val jinRiShiCi: DPJinRiShiCi,
    var explanationEnable: Boolean,
    var maxRetryLimit: Int
)

@Serializable
data class DPJinRiShiCi(
    var token: String
)

@Serializable
class FunctionsAtFunction(
    val enable: Boolean,
    val randomImage: AFRandomImage
)

@Serializable
data class AFRandomImage(
    var randomImageURLs: List<String>
)

@Serializable
data class FunctionsNudgeFunction(
    var enable: Boolean,
    var perUseIntervalSecond: Int,
    val hitoKoto: NFHitoKoto
)

@Serializable
data class NFHitoKoto(
    var getURLParams: String
)

@Serializable
data class FunctionsSingSongFunction(
    var enable: Boolean,
    var forceSendCard: Boolean,
    var perUseIntervalSecond: Int,
    var maxVoiceFileSize: Int,
    var msgMusicNeedPaid: String,
    var msgDownloadMusicFileTooBig: String,
    var msgNotFoundMusic: String
)

@Serializable
data class PlumConfigSystem(
    val sendSystem: SystemSendSystem
)

@Serializable
data class SystemSendSystem(
    val sendDelay: SSendDelay,
    val sendMsgMaxLength: Int
)

@Serializable
data class SSendDelay(
    val sendToFriend: SDSendToFriend,
    val sendToGroup: SDSendToGroup
)

@Serializable
data class SDSendToFriend(
    var enable: Boolean,
    var delayTimeMS: Long
)

@Serializable
data class SDSendToGroup(
    var enable: Boolean,
    var delayTimeMS: Long
)
