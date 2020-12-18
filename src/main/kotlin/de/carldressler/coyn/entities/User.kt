package de.carldressler.coyn.entities

/**
 * User object represents a coyn user.
 *
 * You are automatically registered once you interact with coyn.
 */
class User(
    val userId: String,
    val isAuthRequired: Boolean,
    val password: String?,
    val recoveryQuestion: String?,
    val recoveryAnswer: String?,
    val isRestricted: Boolean,
    val isBanned: Boolean
    ) {

    fun changeAuthRequired(toggleOn: Boolean) {
        TODO("Change whether authorization is required prior to every transaction")
    }

    fun setPassword(newPassword: String) {
        TODO("Password needs to be hashed prior to storing it in the database")
    }

    fun replacePassword(oldPassword: String, newPassword: String) {
        TODO("Password needs to be hashed prior to storing it in the database")
    }

    fun recoverPassword(recAnswer: String) {
        TODO("The recovery answer needs to checked against the recovery answer provided. If it is correct, this.setPassword() may be called")
    }
}