package de.carldressler.coyn

import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import de.carldressler.coyn.database.DBHandler
import de.carldressler.coyn.handlers.CommandHandler
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import java.util.*
import kotlin.system.exitProcess

object Bot {
    val jda: JDA
    val eventWaiter = EventWaiter()

    private fun getToken(): String {
        val fis = javaClass.getResourceAsStream("/private.properties")
        val prop = Properties()
        prop.load(fis)
        return prop.getProperty("token")
    }

    init {
        if (!DBHandler.verifyDatabaseConnectivity()) {
            throw Error("Cannot establish connection to database, aborting")
            exitProcess(3306)
        }

          jda = JDABuilder.createDefault(getToken())
            .addEventListeners(
                eventWaiter,
                CommandHandler
            )
            .setActivity(Activity.watching("you procrastinate..."))
            .build()
    }
}
