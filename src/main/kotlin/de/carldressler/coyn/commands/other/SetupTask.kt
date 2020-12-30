package de.carldressler.coyn.commands.other

import de.carldressler.coyn.commands.Command
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.guild.GenericGuildEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

object SetupTask : Command, ListenerAdapter() {
    lateinit var guildEvent: GenericGuildEvent
    lateinit var targetChannel: TextChannel

    private fun runSetup() {
        guildEvent.guild.createTextChannel("coyn-setup").queue()
        targetChannel = guildEvent.guild.getTextChannelsByName("coyn-setup", false)[0]
        targetChannel.sendMessage("Test")
    }

    override fun run(event: GuildMessageReceivedEvent, args: List<String>, command: String) {
        // TODO: Verify whether the invoker has permission to call this command
        guildEvent = event
        runSetup()
    }

    override fun onGuildJoin(event: GuildJoinEvent) {
        guildEvent = event
        runSetup()
    }
}