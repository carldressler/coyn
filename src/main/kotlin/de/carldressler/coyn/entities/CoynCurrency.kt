package de.carldressler.coyn.entities

import de.carldressler.coyn.database.DBHandler
import de.carldressler.coyn.utils.Logger
import kotlin.system.exitProcess

class CoynCurrency(currencyId: String) {
    var id = currencyId
    var name: String
    var symbol: String
    var isActive: Boolean = true
    val isCompetitive: Boolean
        get() {
            val sql = "SELECT (is_competitive) FROM coyn.currencies WHERE currency_id = ?"
            val stmt = DBHandler.getStmt(sql)
            stmt.setString(1, id)
            val rs = DBHandler.query(stmt)
            if (!rs.next()) Logger.error("CoynCurrency.isCompetitive get(): Result Set is empty")
            return rs.getInt("is_competitive") == 1
        }
    val wasCompetitive: Boolean
        get() {
            val sql = "SELECT (was_competitive) FROM coyn.currencies WHERE currency_id = ?"
            val stmt = DBHandler.getStmt(sql)
            stmt.setString(1, id)
            val rs = DBHandler.query(stmt)
            if (!rs.next()) Logger.error("CoynCurrency.wasCompetitive get(): Result Set is empty")
            return rs.getInt("was_competitive") == 1
        }

    init {
        // TODO: CurrencyHandler.isValidId(currencyId)

        val sql = "SELECT * FROM coyn.currencies WHERE currency_id = ?"
        val stmt = DBHandler.getStmt(sql)
        stmt.setString(1, currencyId)
        val rs = DBHandler.query(stmt)
        if (!rs.next()) {
            Logger.error("Could not find currency record for '$currencyId' in database. Shutting down...")
            exitProcess(3306)
        }

        this.id = rs.getString("currency_id")
        this.name = rs.getString("currency_name")
        this.symbol = rs.getString("currency_symbol")
        this.isActive = rs.getInt("is_active") == 1
    }
}