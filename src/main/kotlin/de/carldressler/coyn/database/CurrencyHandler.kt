package de.carldressler.coyn.database

import de.carldressler.coyn.entities.CoynCurrency
import de.carldressler.coyn.utils.Logger
import java.util.*

object CurrencyHandler {
    fun registerCurrency(name: String, symbol: String, isCompetitive: Boolean): CoynCurrency {
        val currencyId = UUID.randomUUID().toString()
        val sql = "INSERT INTO coyn.currencies (currency_id, currency_name, currency_symbol, is_competitive, was_competitive) VALUES (?, ?, ?, ?, ?)"
        val stmt = DBHandler.getStmt(sql)

        stmt.setString(1, currencyId)
        stmt.setString(2, name)
        stmt.setString(3, symbol)
        stmt.setInt(4, if (isCompetitive) 1 else 0)
        stmt.setInt(5, if (isCompetitive) 1 else 0)
        DBHandler.execute(stmt)
        Logger.info("A new currency ($name) was just created: $currencyId")
        return CoynCurrency(currencyId)
    }

    // TODO: fun isValidId(currencyId: String): Boolean {}
}