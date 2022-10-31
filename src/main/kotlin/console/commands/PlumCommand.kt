package com.sakurawald.plum.reloaded.console.commands

import com.sakurawald.plum.reloaded.Plum
import com.sakurawald.plum.reloaded.Plum.reload
import com.sakurawald.plum.reloaded.config.PlumConfig
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand

object PlumCommand : SimpleCommand(
    owner = Plum,
    primaryName = "PlumReloaded",
    secondaryNames = arrayOf("plum")
) {
    @Handler
    suspend fun CommandSender.handle(operation: String) {
        if (operation.equals("reload", true)) {
            PlumConfig.reload()
            sendMessage("Reload Configs Successfully!")
        }
    }
}