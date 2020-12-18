package de.carldressler.coyn

import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import java.util.*

object Bot {
    fun getToken(): String {
        val fis = javaClass.getResourceAsStream("/private.properties")
        val prop = Properties()
        prop.load(fis)
        return prop.getProperty("token")
    }

    fun instantiate(token: String) {
        JDABuilder.createDefault(token)
            .addEventListeners()
            .setActivity(Activity.watching("the NASDAQ"))
            .build()
    }
}

fun main() {
    val token = Bot.getToken()
    Bot.instantiate(token)
}
