package de.carldressler.coyn.commands.help

import de.carldressler.coyn.commands.Command
import de.carldressler.coyn.utils.Constants
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

object AboutCommand : Command {
    private val embed = EmbedBuilder()
        .setColor(Constants.ACCENT_COLOR)
        .setTitle("Introducing coyn")
        .setDescription("""
            An easy to use yet feature strong currency bot for use of user made currencies across multiple servers. Every server can have its own shop where earned coins can be spent on permissions, role colors and special nicknames. Coins can be acquired by just chatting and "being active", trading (for example) in-game goods with pals or winning it in server competitions.
            
            **Features that make coyn unique**
              • Shared currencies between servers 🔗
              • Powerful currency tools and a competitive mode 💥
              • Extensive shop and currency settings for admins 🔨
              • Strong security options for users 🔐

            > See all features with`${Constants.PREFIX}help`
              
            coyn is still under development and not all features will work as expected. I'm working on coyn with my heart and soul and hope you like the bot! You can find ways to send me feedback with `${Constants.PREFIX}feedback`.
        """.trimIndent())
        .build()

    override fun run(event: GuildMessageReceivedEvent, args: List<String>, command: String) {
        event.channel.sendMessage(embed).queue()
    }
}