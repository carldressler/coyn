package de.carldressler.coyn.database

object UserHandler {
    fun validateUser(userId: String) {
        val sql = "INSERT IGNORE INTO coyn.users (user_id) VALUES (?);"
        val stmt = DBHandler.getStmt(sql)
        stmt.setString(1, userId)
        DBHandler.execute(stmt)
    }

    // TODO: fun isValidId(userId: String): Boolean {}
}
