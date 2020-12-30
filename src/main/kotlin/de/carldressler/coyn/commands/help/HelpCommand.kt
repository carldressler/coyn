package de.carldressler.coyn.commands.help

import de.carldressler.coyn.commands.Command
import de.carldressler.coyn.utils.Constants
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

object HelpCommand : Command {
    override fun run(event: GuildMessageReceivedEvent, args: List<String>, command: String) {
        val embed = EmbedBuilder()
            .setColor(Constants.ACCENT_COLOR)
            .setTitle("coyn Help")
            .setDescription("""
                Please note that coyn is not nearly in a final stage yet and many functions are still missing.
                Listed below are all currently available commands. Arguments enclosed in <sharp brackets> are optional.
                
                👑: Currency admins only
                🛡️: Guild admins only
                🇳: Non-competitive currencies only
            """.trimIndent())
            .addBlankField(false)
            .addField(
                "${Constants.PREFIX}quickstart",
                "A short quick-start guide with ideas on how to get started with coyn",
                false
            )
            .addField(
                "${Constants.PREFIX}about",
                "Information about the developer of coyn, me.",
                false
            ).addField(
                "${Constants.PREFIX}help",
                "Help information on how to use coyn",
                false
            ).addField(
                "${Constants.PREFIX}createcurrency",
                """
                    Create a new coyn currency
                    🛡️
                """.trimIndent(),
                false
            ).addField(
                "${Constants.PREFIX}give @mention amount <reason>",
                """
                    Gives the mentioned user <amount> coins without affecting the invokers balance.
                    👑 🇳
                    Shortcut: g
                """.trimIndent(),
                false
            ).addField(
                "${Constants.PREFIX}remove @mention amount <reason>",
                """
                    Removes <amount> coins from the mentioned users balance (does not add it to invokers balance).
                    👑 🇳
                    Shortcut: rem, r
                """.trimIndent(),
                false
            )
            .build()

        event.channel.sendMessage(embed).queue()
    }
}

/*

*give @mention amount <reason>* [ADMIN]
Gives the mentioned user <amount> coins without affecting the command invocators balance. Informs the mentioned user.
(not available in competitive mode)

*remove @mention amount <reason> [ADMIN]
Removes <amount> from the mentioned users balance. Does not inform the user about it.
(not available in competitive mode)
 */