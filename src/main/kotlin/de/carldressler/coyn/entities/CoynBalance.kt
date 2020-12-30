package de.carldressler.coyn.entities

import de.carldressler.coyn.database.BalanceHandler
import de.carldressler.coyn.database.DBHandler
import de.carldressler.coyn.utils.Logger
import java.lang.UnsupportedOperationException

/**
 * A balance object representing a users balance specific to a currency.
 *
 * Balance is initialized using the user the balance belongs to and the currency the balance is of.
 *
 * @param userId unique Discord id of user the balance is associated to
 * @param currencyId unique coyn id of currency the balance is associated to
 */
class CoynBalance(var userId: String, var currencyId: String) {
    var id: String
    val user: CoynUser = CoynUser(userId)
    val currency: CoynCurrency = CoynCurrency(currencyId)
    val amount: Int
        get() {
            val sql = "SELECT (amount) FROM coyn.balances WHERE balance_id = ?"
            val stmt = DBHandler.getStmt(sql)
            stmt.setString(1, id)
            val rs = DBHandler.query(stmt)
            if (!rs.next()) Logger.error("CoynBalance.amount get(): Result Set is empty")

            return rs.getInt("amount")
        }

    init {
        BalanceHandler.validateBalance(userId, currencyId)

        val sql = "SELECT (balance_id) FROM coyn.balances WHERE user_id = ? AND currency_id = ?"
        val stmt = DBHandler.getStmt(sql)
        stmt.setString(1, userId)
        stmt.setString(2, currencyId)
        val rs = DBHandler.query(stmt)
        if (!rs.next()) Logger.error("CoynBalance.id get(): Result Set is empty")

        id = rs.getString("balance_id")
    }

    fun addCoins(amountToAdd: Int) {
        val sql = """
            UPDATE coyn.balances
            SET amount = amount + ?
            WHERE balance_id = ?
        """.trimIndent()
        val stmt = DBHandler.getStmt(sql)
        stmt.setInt(1, amountToAdd)
        stmt.setString(2, id)
        DBHandler.execute(stmt)
    }

    fun removeCoins(amountToRemove: Int) {
        if (amountToRemove > this.amount) {
            throw UnsupportedOperationException("The amount to remove is higher than the current amount and negative values are not allowed.")
        }

        val sql = """
            UPDATE coyn.balances
            SET amount = amount - ?
            WHERE balance_id = ?
        """.trimIndent()
        val stmt = DBHandler.getStmt(sql)
        stmt.setInt(1, amountToRemove)
        stmt.setString(2, this.id)
        DBHandler.execute(stmt)
    }

    fun setCoins(amountToSet: Int) {
        if (amountToSet < 0) {
            throw UnsupportedOperationException("Cannot set balance amount to a value lower than 0.")
        }

        val sql = """
            UPDATE coyn.balances
            SET amount = ?
            WHERE balance_id = ?
        """.trimIndent()
        val stmt = DBHandler.getStmt(sql)
        stmt.setInt(1, amountToSet)
        stmt.setString(2, this.id)
        DBHandler.execute(stmt)
    }
}