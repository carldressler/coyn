package de.carldressler.coyn

import de.carldressler.coyn.database.UserHandler
import java.security.SecureRandom

fun getUserWithID() {
    UserHandler.getUser("730190870011183271")
    println("Successfully fetched user with ID")
}

fun verifyUserWithID() {
    UserHandler.validateUser("730190870011183271")
    println("Successfully verified user with ID")
}