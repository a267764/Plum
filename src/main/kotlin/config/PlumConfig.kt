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

            val QQFriendInvitation by value(IMQQFriendInvitation())
            val QQGroupInvitation by value(IMQQFriendInvitation())
        }

        class AdminRobotControl {
            var forceCancel_GroupMessage by value(false)
            var forceCancel_FriendMessage by value(true)
        }

        val InvitationManager by value(AdminInvitationManager())
        val RobotControl by value(AdminRobotControl())
        val botAdministrators by value(listOf(3172906506L))
    }

    class PlumConfigFunctions {
        class FunctionsFunctionManager {
            var callTooOftenMsg by value("调用该功能过于频繁，请稍后再试试吧~")
            var functionDisableMsg by value("很抱歉，该功能已被禁用。")
        }

        class FunctionsDailyCountdown {
            var enable by value(true)
            var countdown_commands by value(
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

            val JinRiShiCi by value(DPJinRiShiCi())
            var explanation_Enable by value(true)
            var maxRetryLimit by value(3)
        }

        class FunctionsAtFunction {
            val enable by value(false)

            class AFRandomImage {
                var Random_Image_URLs by value(
                    listOf(
                        "http://www.dmoe.cc/random.php",
                        "http://api.mtyqx.cn/tapi/random.php",
                        "http://api.mtyqx.cn/api/random.php"
                    )
                )
            }

            val RandomImage by value(AFRandomImage())
        }

        class FunctionsNudgeFunction {
            class NFHitoKoto {
                var get_URL_Params by value("")
            }

            var enable by value(true)
            var perUseIntervalSecond by value(3)
            val HitoKoto by value(NFHitoKoto())
        }

        class FunctionsSingSongFunction {
            var enable by value(true)
            var forceSendCard by value(false)
            var perUseIntervalSecond by value(5)
            var maxVoiceFileSize by value(15000000)
            var music_need_paid_msg by value("很抱歉，这可能是一首付费歌曲。")
            var download_music_file_too_big_msg by value("这首歌太长啦！")
            var not_found_music_msg by value("哎呀，咱还不会《[OBJECT1]》这首歌！")
        }

        val FunctionManager by value(FunctionsFunctionManager())
        val DailyCountdown by value(FunctionsDailyCountdown())
        val DailyPoetry by value(FunctionsDailyPoetry())
        val AtFunction by value(FunctionsAtFunction())
        val NudgeFunction by value(FunctionsNudgeFunction())
        val SingSongFunction by value(FunctionsSingSongFunction())
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
                    var delayTimeMS by value(1000)
                }

                val SendToFriend by value(SDSendToFriend())
                val SendToGroup by value(SDSendToFriend())
            }

            val SendDelay by value(SSendDelay())
            val sendMsgMaxLength by value(4500)
        }

        val SendSystem by value(SystemSendSystem())
    }

    val debug by value(PlumConfigDebug())
    val admin by value(PlumConfigAdmin())
    val functions by value(PlumConfigFunctions())
    val system by value(PlumConfigSystem())
}