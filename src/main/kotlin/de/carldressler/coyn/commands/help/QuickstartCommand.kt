package de.carldressler.coyn.commands.help

import de.carldressler.coyn.commands.Command
import de.carldressler.coyn.utils.Constants
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

object QuickstartCommand : Command {
    override fun run(event: GuildMessageReceivedEvent, args: List<String>, command: String) {
        val embed = EmbedBuilder()
            .setColor(Constants.ACCENT_COLOR)
            .setTitle("Welcome to coyn! \uD83D\uDC4B")
            .setDescription("""
                Welcome aboard! coyn is a currency bot like many others, but has some unique selling points.

                  - Shared currencies between servers 🔗
                  - Powerful currency tools and a competitive mode 💥
                  - Extensive store and currency settings for admins 🔨
                  - Strong security options for users 🔐

                Here is a small introduction to coyn.

                Q: How does coyn work?
                A: You earn coins automatically by being active (chatting). With the earned coins you can buy perks in the server store. You can also earn coins in other ways, but this varies from server to server: possible would be raffles or similar.

                Q: How can I get started?
                A: Very simple. You might want to familiarize yourself with the following commands: `${Constants.PREFIX}balance`, `${Constants.PREFIX}store`, `${Constants.PREFIX}settings`. Alternatively you can check the help page for all available commands: `${Constants.PREFIX} help`.

                Q: Nothing works here!
                A: A server administrator must first set a currency before it can be traded/worked with on the server. Server administrators can restart the setup using `${Constants.PREFIX}setup` or find more information about the matter here: https://bit.ly/37BQRaZ
            """.trimIndent())
            .build()

        event.channel.sendMessage(embed).queue()
    }
}