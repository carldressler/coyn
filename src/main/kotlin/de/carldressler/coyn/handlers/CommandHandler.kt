package de.carldressler.coyn.handlers

import de.carldressler.coyn.commands.*
import de.carldressler.coyn.commands.transactions.AdminTransaction
import de.carldressler.coyn.commands.other.CreateCurrencyCommand
import de.carldressler.coyn.commands.help.AboutCommand
import de.carldressler.coyn.commands.help.HelpCommand
import de.carldressler.coyn.commands.help.QuickstartCommand
import de.carldressler.coyn.commands.other.SetupTask
import de.carldressler.coyn.commands.transactions.PayCommand
import de.carldressler.coyn.utils.Constants
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

object CommandHandler : ListenerAdapter() {
    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if (event.author.isBot) return
        if (!event.message.contentRaw.startsWith(Constants.PREFIX)) return

        val command = event.message.contentRaw
            .removePrefix(Constants.PREFIX)
            .split(" ")[0]
            .toLowerCase()
        if (command.isEmpty()) return
        val arguments = event.message.contentRaw
            .removePrefix(Constants.PREFIX)
            .split(" ")
            .drop(1)

        if (commandMap.contains(command)) {
            commandMap[command]?.run(event, arguments, command)
        }
        else
            ErrorHandler.sendInvalidCommandWarning(event.channel, command)
    }

    private val commandMap: Map<String, Command> = hashMapOf(

    )
}