package de.carldressler.coyn.entities

import de.carldressler.coyn.database.DBHandler
import de.carldressler.coyn.database.GuildHandler
import de.carldressler.coyn.utils.Logger
import kotlin.system.exitProcess

class CoynGuild(private val guildId: String) {
    var currency: CoynCurrency

    init {
        // TODO: GuildHandler.isValidId(guildId)
        GuildHandler.validateGuild(guildId)

        val sql = "SELECT (active_currency_id) FROM coyn.guilds WHERE guild_id = ?"
        val stmt = DBHandler.getStmt(sql)
        stmt.setString(1, guildId)
        val rs = DBHandler.query(stmt)
        if (!rs.next()) {
            Logger.error("CoynGuild/init -> Could not find user record for '$guildId' in database. Shutting down...")
            exitProcess(3306)
        }

        this.currency = CoynCurrency(rs.getString("active_currency_id"))
    }

    fun updateCurrency(newCurrency: CoynCurrency) {
        val sql = """
            UPDATE coyn.guilds
            SET active_currency_id = ?
            WHERE guild_id = ?
        """.trimIndent()
        val stmt = DBHandler.getStmt(sql)
        stmt.setString(1, newCurrency.id)
        stmt.setString(2, guildId)
        DBHandler.execute(stmt)

        this.currency = newCurrency
    }
}