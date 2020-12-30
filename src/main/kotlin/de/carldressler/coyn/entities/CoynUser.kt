package de.carldressler.coyn.entities

import de.carldressler.coyn.database.DBHandler
import de.carldressler.coyn.database.UserHandler
import de.carldressler.coyn.utils.Logger
import kotlin.system.exitProcess

/**
 * User object represents a coyn user.
 *
 * You are automatically registered once you interact with coyn.
 */
class CoynUser(userId: String) {
    var id: String
    var passwordHashed: String? = null
    var recoveryQuestion: String? = null
    var recoveryAnswer: String? = null
    var authEnabled: Boolean = false
    var isBanned: Boolean = false

    init {
        // Step 1 - Ensure the user has a record
        // TODO: UserHandler.isValidId(userId)
        // If no, throw Exception
        UserHandler.validateUser(userId)

        // Step 2 - Fetch the user data
        val sql = "SELECT * FROM coyn.users WHERE user_id = ?"
        val stmt = DBHandler.getStmt(sql)
        stmt.setString(1, userId)
        val rs = DBHandler.query(stmt)
        if (!rs.next()) {
            Logger.error("CoynUser/init -> Could not find user record for '$userId' in database. Shutting down...")
            exitProcess(3306)
        }

        // Step 3 - Fill in the fields
        this.id = rs.getString("user_id")
        this.passwordHashed = rs.getString("user_password")
        this.recoveryQuestion = rs.getString("recovery_question")
        this.recoveryAnswer = rs.getString("recovery_answer")
        this.authEnabled = rs.getInt("auth_enabled") == 1
        this.isBanned = rs.getInt("is_banned") == 1
    }
}