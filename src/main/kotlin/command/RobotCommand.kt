package com.sakurawald.plum.reloaded.command

import com.sakurawald.framework.MessageManager
import com.sakurawald.plum.reloaded.Plum
import com.sakurawald.plum.reloaded.config.PlumConfig
import net.mamoe.mirai.contact.MemberPermission.*
import net.mamoe.mirai.message.data.MessageChain
import java.util.regex.Pattern

abstract class RobotCommand(
    val rule: String,
    // 表示该指令的使用范围，允许什么类型的聊天使用
    val ranges: MutableList<RobotCommandChatType> = mutableListOf(),
    val users: MutableList<RobotCommandUser> = mutableListOf()
) {
    val pattern = Pattern.compile(rule)
    fun isThisCommand(msg: String): Boolean = pattern.matcher(msg).matches()
    fun runCheckUp(msgType: Int, time: Int, fromGroup: Long, fromQQ: Long, messageChain: MessageChain): Boolean {
        if (!ranges.any { it.type == msgType }) {
            MessageManager.sendMessageBySituation(fromGroup, fromQQ, "很抱歉，该指令不能在当前聊天类型中使用。");
            return false
        }
        val authority = RobotCommandUser.getAuthority(fromGroup, fromQQ)
        if (!users.any { it.userPermission == authority }) {
            MessageManager.sendMessageBySituation(fromGroup, fromQQ, "很抱歉，您没有权限使用该指令~")
            return false
        }
        return true
    }

    abstract fun runCommand(msgType: Int, time: Int, fromGroup: Long, fromQQ: Long, messageChain: MessageChain)
}


//用于描述机器人的指令支持的使用者
/*
 * 一旦Robot和其他人成为Friend，则一定为FRIEND_CHAT。
 * 即使其他人从群里打开和Robot的聊天界面，聊天类型也是FRIEND_CHAT。
 *
 * 群里聊天的类型为GROUP_CHAT
 *
 * 与Robot不是好友关系的人，从群里打开与Robot的聊天界面，则聊天类型为GROUP_TEMP_CHAT
 *
 * */
enum class RobotCommandChatType(var type: Int) {
    FRIEND_CHAT(1000), GROUP_CHAT(2000), GROUP_TEMP_CHAT(3000), DISCUSS_MSG(4000), STRANGER_CHAT(5000);
}


enum class RobotCommandUser(var userPermission: Int) {
    NORMAL_USER(1), GROUP_ADMINISTRATOR(2), GROUP_OWNER(3), BOT_ADMINISTRATOR(4);

    companion object {
        /** 判断用户的权限.  */
        fun getAuthority(fromGroup: Long, fromQQ: Long): Int {
            val authority: Int = if (fromGroup != -1L) getAuthorityByQQ(fromGroup, fromQQ) else getAuthorityByQQ(fromQQ)
            Plum.logger.debug(
                "Permission fromGroup = $fromGroup, fromQQ = $fromQQ, authority：$authority"
            )
            return authority
        }

        /** 单纯通过QQ，判断对方是不是超级管理  */
        fun getAuthorityByQQ(QQ: Long): Int {
            if (PlumConfig.admin.botAdministrators.any { it == QQ }) return BOT_ADMINISTRATOR.userPermission
            return NORMAL_USER.userPermission
        }

        /** 判断群聊中，对方是不是管理（管理员或群主）  */
        fun getAuthorityByQQ(fromGroup: Long, fromQQ: Long): Int {
            val m = Plum.CURRENT_BOT?.getGroup(fromGroup)?.get(fromQQ) ?: return NORMAL_USER.userPermission
            // [!] 首先判断是不是管理员
            // 防止自己是超级管理员，但又是普通群员
            if (getAuthorityByQQ(fromQQ) == BOT_ADMINISTRATOR.userPermission) {
                return BOT_ADMINISTRATOR.userPermission
            }
            return when (m.permission) {
                MEMBER -> NORMAL_USER.userPermission
                OWNER -> GROUP_OWNER.userPermission
                ADMINISTRATOR -> GROUP_ADMINISTRATOR.userPermission
                else -> NORMAL_USER.userPermission
            }
        }
    }
}
