package de.carldressler.coyn.handlers

import com.sun.java.accessibility.util.GUIInitializedListener
import de.carldressler.coyn.utils.Constants
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

object ErrorHandler {
    fun sendInvalidCommandWarning(channel: TextChannel, command: String) {
        val embed = EmbedBuilder()
            .setColor(Constants.WARNING_COLOR)
            .setTitle("Invalid Command Warning")
            .setDescription("""
                `$command` is not a valid command.
                Check your spelling or see all available commands with `${Constants.PREFIX}help`.
            """.trimIndent())
            .build()
        channel.sendMessage(embed).queue()
    }

    fun sendInvalidArgumentsError(channel: TextChannel, command: String, validUsage: String) {
        val embed = EmbedBuilder()
            .setColor(Constants.ERROR_COLOR)
            .setTitle("Those arguments ain't good!")
            .setDescription("""
                The arguments (the ones after the command) are incorrect. The arguments should look like this and in this order:

                $validUsage

                Arguments marked with <> can be omitted. If you need a detailed explanation, use ${Constants.PREFIX}help $command.
            """.trimIndent())
            .build()

        channel.sendMessage(embed).queue()
    }

    fun sendDMsClosed(channel: TextChannel, name: String) {
        val embed = EmbedBuilder()
            .setColor(Constants.WARNING_COLOR)
            .setTitle("Could not notify $name")
            .setDescription("$name could not be informed because their Direct Messages are closed. If it is important to you, you can try to inform the user personally.")
            .build()

        channel.sendMessage(embed).queue()
    }

    fun sendCompetitiveProhibited(channel: TextChannel, command: String) {
        val embed = EmbedBuilder()
            .setColor(Constants.ERROR_COLOR)
            .setTitle("Action not allowed due to competitive mode")
            .setDescription("""
                The invoked command is not available for this currency because it is in competitive mode.
                This ensures that no changes can be made to balances and users can rely on the integrity of the currency.

                > Learn more about competitive mode with `c competitive`

                You can disable the competitive mode at any time in the currency settings (`c settings`).
            """.trimIndent())
            .build()
        channel.sendMessage(embed)
    }

    fun sendBannedProhibited(channel: TextChannel) {
        val embed = EmbedBuilder()
            .setColor(Constants.ERROR_COLOR)
            .setTitle("You are banned from interacting with coyn")
            .setDescription("""
                As you should already know, you have been banned from the coyn network and may not interact with it anymore.
            """.trimIndent())
            .build()
        channel.sendMessage(embed)
    }
}