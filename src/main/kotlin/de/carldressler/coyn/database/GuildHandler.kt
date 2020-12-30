package de.carldressler.coyn.database

object GuildHandler {
    fun validateGuild(guildId: String) {
        val sql = """
            INSERT IGNORE INTO coyn.guilds (guild_id)
            VALUES (?)
        """.trimIndent()
        val stmt = DBHandler.getStmt(sql)
        stmt.setString(1, guildId)
        DBHandler.execute(stmt)
    }

    // TODO: fun isValidId(guildId: String): Boolean {}
}