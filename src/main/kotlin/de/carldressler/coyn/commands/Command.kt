package de.carldressler.coyn.commands

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

interface Command {
    fun run(event: GuildMessageReceivedEvent, args: List<String>, command: String)
}