package de.carldressler.coyn.database

import de.carldressler.coyn.entities.User

object UserHandler {
    /**
     * Validates a user against the coyn database. If the user has no record yet, one will be created.
     * This method needs to be fired
     *
     * @param uid unique Discord identifier of the user
     */
    fun validateUser(uid: String) {
        val sql = "INSERT IGNORE INTO coyn.users (user_id, auth_required, restricted, banned) VALUES (? , 0, 0, 0);"
        val prepStmt = DBHandler.getPreparedStatement(sql) ?: return
        prepStmt.setString(1, uid)
        DBHandler.executePreparedStatement(prepStmt)
    }

    /**
     * Returns an object or null if none could be found using the unique identifier.
     *
     * @param uid unique Discord identifier of the user
     * @return User object
     * @see de.carldressler.coyn.entities.User
     */
    fun getUser(uid: String): User? {
        val sql = "SELECT * FROM coyn.users WHERE user_id = ?"
        val prepStmt = DBHandler.getPreparedStatement(sql) ?: return null
        prepStmt.setString(1, uid)
        val rs = DBHandler.queryPreparedStatement(prepStmt) ?: return null
        if (!rs.next()) return null

        val id: String = rs.getString("user_id")
        val isAuthRequired: Boolean = rs.getInt("auth_required") == 1
        val password: String = rs.getNString("user_password")
        val recoveryQuestion = rs.getNString("recovery_question")
        val recoveryAnswer = rs.getNString("recovery_answer")
        val isRestricted: Boolean = rs.getInt("restricted") == 1
        val isBanned: Boolean = rs.getInt("banned") == 1

        return User(id, isAuthRequired, password, recoveryQuestion, recoveryAnswer, isRestricted, isBanned)
    }
}