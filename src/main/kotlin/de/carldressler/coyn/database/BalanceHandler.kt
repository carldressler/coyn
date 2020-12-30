package de.carldressler.coyn.database

import de.carldressler.coyn.utils.Logger
import java.util.*

object BalanceHandler {
    fun validateBalance(userId: String, currencyId: String) {
        val validateBalanceSql = """
            SELECT * FROM coyn.balances
            WHERE user_id = ? AND currency_id = ?
        """.trimIndent()
        val validateBalanceStmt = DBHandler.getStmt(validateBalanceSql)
        validateBalanceStmt.setString(1, userId)
        validateBalanceStmt.setString(2, currencyId)
        val rs = DBHandler.query(validateBalanceStmt)

        if (!rs.next()) {
            // There is no record yet so one will be created
            val createBalanceSql = """
                INSERT IGNORE INTO coyn.balances
                (balance_id, user_id, currency_id, amount)
                VALUES (?, ?, ?, ?)
            """.trimIndent()
            val balanceId = UUID.randomUUID().toString()
            val createBalanceStmt = DBHandler.getStmt(createBalanceSql)
            createBalanceStmt.setString(1, balanceId)
            createBalanceStmt.setString(2, userId)
            createBalanceStmt.setString(3, currencyId)
            createBalanceStmt.setInt(4, 0)
            DBHandler.execute(createBalanceStmt)
            Logger.info("A new balance was just created, as there was none in place already: $balanceId")
        }
    }

    // TODO: fun isValidId(balanceId: String) {}
}