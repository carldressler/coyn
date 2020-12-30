package de.carldressler.coyn.handlers

import de.carldressler.coyn.utils.Constants
import de.carldressler.coyn.utils.Logger
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

object ProcessHandler {
    private var ongoingProcesses: MutableSet<String> = HashSet()

    fun userMayStartProcess(event: GuildMessageReceivedEvent): Boolean {
        val userId = event.author.id
        return if (ongoingProcesses.contains(userId)) {
            event.channel.sendMessage(processOngoingError).queue()
            false
        } else {
            true
        }
    }

    fun engageUser(userId: String) {
        if (ongoingProcesses.contains(userId)) {
            Logger.error("Cannot engage user in process when user is engaged process already (user was not released from process?)")
        } else {
            ongoingProcesses.add(userId)
            Logger.info("User was engaged in process")
        }
    }
    fun releaseUser(userId: String) {
        if (!ongoingProcesses.contains(userId)) {
            Logger.error("Cannot release user from process when user is not engaged in process already (user was not engaged in process?)")
        } else {
            ongoingProcesses.remove(userId)
            Logger.info("User was released from process")
        }
    }

    fun timeout(event: GuildMessageReceivedEvent) {
        event.channel.sendMessage(timeoutWarning).queue()
        event.channel.sendMessage(event.author.asMention).queue()
        releaseUser(event.author.id)
    }

    private val processOngoingError = EmbedBuilder()
        .setColor(Constants.ERROR_COLOR)
        .setTitle("There is a process going on already")
        .setDescription("""
                        It is not possible to run two multi-step processes simultaneously as this could lead to conflicts. Please finish the one going on or wait for it to run out (5 minutes) and then start a new one.
                    """.trimIndent())
        .build()

    private val timeoutWarning = EmbedBuilder()
        .setColor(Constants.WARNING_COLOR)
        .setTitle("Process stopped due to inactivity")
        .setDescription("The process was closed due to your inactivity. You can restart it at any time.")
        .build()
    }