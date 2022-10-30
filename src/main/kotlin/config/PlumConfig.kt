package com.sakurawald.plum.reloaded.config

import net.mamoe.mirai.console.data.ReadOnlyPluginConfig
import net.mamoe.mirai.console.data.value

/**
 * Plum 配置文件
 * src/main/java/com/sakurawald/files/ApplicationConfig_Data.java
 */
object PlumConfig : ReadOnlyPluginConfig("config") {
    class PlumConfigDebug {
        val enable by value(false)
    }

    class PlumConfigAdmin {
        class AdminInvitationManager {
            class IMQQFriendInvitation {
                val autoAcceptAddQQFriend by value(false)
            }

            class IMQQGroupInvitation {
                val autoAcceptAddQQGroup by value(false)
            }

            val friendInvitation by value(IMQQFriendInvitation())
            val groupInvitation by value(IMQQGroupInvitation())
        }

        class AdminRobotControl {
            var forceCancelGroupMessage by value(false)
            var forceCancelFriendMessage by value(true)
        }

        val invitationManager by value(AdminInvitationManager())
        val robotControl by value(AdminRobotControl())
        val botAdministrators by value(listOf(3172906506L))
    }

    class PlumConfigFunctions {
        class FunctionsFunctionManager {
            var callTooOftenMsg by value("调用该功能过于频繁，请稍后再试试吧~")
            var functionDisableMsg by value("很抱歉，该功能已被禁用。")
        }

        class FunctionsDailyCountdown {
            var enable by value(true)
            var countdownCommands by value(
                listOf(
                    "◆距离2021年高考还有\$diff_days天！|1623027804000|高考加油！&高考加油！&2021年高考已结束~",
                    "◆距离2020年考研还有\$diff_days天！|1608512604000|考研加油&考研加油&考研加油&2020年考研已结束~",
                    "◆距离2020年四六级考试还有\$diff_days天！|1600477404000|四六级考试加油！&四六级考试已结束~"
                )
            )
        }

        class FunctionsDailyPoetry {
            class DPJinRiShiCi {
                var token by value("paOa0DqOdpLn4FVVHNtEDgU5Imk89kXZ")
            }

            val jinRiShiCi by value(DPJinRiShiCi())
            var explanationEnable by value(true)
            var maxRetryLimit by value(3)
        }

        class FunctionsAtFunction {
            val enable by value(false)

            class AFRandomImage {
                var randomImageURLs by value(
                    listOf(
                        "http://www.dmoe.cc/random.php",
                        "http://api.mtyqx.cn/tapi/random.php",
                        "http://api.mtyqx.cn/api/random.php"
                    )
                )
            }

            val randomImage by value(AFRandomImage())
        }

        class FunctionsNudgeFunction {
            class NFHitoKoto {
                var getURLParams by value("")
            }

            var enable by value(true)
            var perUseIntervalSecond by value(3)
            val hitoKoto by value(NFHitoKoto())
        }

        class FunctionsSingSongFunction {
            var enable by value(true)
            var forceSendCard by value(false)
            var perUseIntervalSecond by value(5)
            var maxVoiceFileSize by value(15000000)
            var msgMusicNeedPaid by value("很抱歉，这可能是一首付费歌曲。")
            var msgDownloadMusicFileTooBig by value("这首歌太长啦！")
            var msgNotFoundMusic by value("哎呀，咱还不会《[OBJECT1]》这首歌！")
        }

        val functionManager by value(FunctionsFunctionManager())
        val dailyCountdown by value(FunctionsDailyCountdown())
        val dailyPoetry by value(FunctionsDailyPoetry())
        val atFunction by value(FunctionsAtFunction())
        val nudgeFunction by value(FunctionsNudgeFunction())
        val singSongFunction by value(FunctionsSingSongFunction())
    }

    class PlumConfigSystem {
        class SystemSendSystem {
            class SSendDelay {
                class SDSendToFriend {
                    var enable by value(false)
                    var delayTimeMS by value(0L)
                }

                class SDSendToGroup {
                    var enable by value(true)
                    var delayTimeMS by value(1000L)
                }

                val sendToFriend by value(SDSendToFriend())
                val sendToGroup by value(SDSendToGroup())
            }

            val sendDelay by value(SSendDelay())
            val sendMsgMaxLength by value(4500)
        }

        val sendSystem by value(SystemSendSystem())
    }

    val debug by value(PlumConfigDebug())
    val admin by value(PlumConfigAdmin())
    val functions by value(PlumConfigFunctions())
    val system by value(PlumConfigSystem())
}