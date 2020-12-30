package de.carldressler.coyn.handlers

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.time.LocalDateTime

object AwardHandler : ListenerAdapter() {
    private val cooldownMap = mutableMapOf<String, Long>()

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val id = event.author.id
        val timeNowInSeconds = System.currentTimeMillis() / 1000L

        if (cooldownMap[id]!! > timeNowInSeconds - 60) {

        }
    }
}