package de.carldressler.coyn.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.*
import java.util.*
import kotlin.system.exitProcess

/**
 * This singleton class includes several methods to work with the coyn database.
 * It is especially handy as it catches errors. This results in less boilerplate and more code - yay!
 *
 * Attention! Caught SQLExceptions in this object lead to immediate process termination. That's unfortunate, but life.
 */
object DBHandler {
    private val dataSource: HikariDataSource

    init {
        val config = HikariConfig("src/main/resources/hikari.properties")
        dataSource = HikariDataSource(config)
    }

    private fun getConnection(): Connection {
        return dataSource.connection
    }

    fun getStmt(sql: String): PreparedStatement {
        val con = this.getConnection()
        return try {
            con.prepareStatement(sql)
        } catch (err: SQLException) {
            err.printStackTrace()
            exitProcess(102)
        }
    }

    fun execute(prepStmt: PreparedStatement): Int {
        return try {
            prepStmt.executeUpdate()
        } catch (err: SQLException) {
            err.printStackTrace()
            exitProcess(103)
        } finally {
            prepStmt.connection.close()
        }
    }

    fun query(prepStmt: PreparedStatement): ResultSet {
        return try {
            prepStmt.executeQuery()
        } catch (err: SQLException) {
            err.printStackTrace()
            exitProcess(104)
        } finally {
            prepStmt.connection.close()
        }
    }

    fun verifyDatabaseConnectivity(): Boolean {
        val con = this.getConnection()
        return true
    }
}