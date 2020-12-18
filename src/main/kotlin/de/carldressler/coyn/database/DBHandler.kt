package de.carldressler.coyn.database

import java.io.FileInputStream
import java.sql.*
import java.util.*

/**
 * This singleton class includes several methods to work with the coyn database.
 * It is especially handy as it catches errors. This results in less boilerplate and more code - yay!
 */
object DBHandler {
    private var server: String
    private var user: String
    private var password: String

    init {
        val fis = javaClass.getResourceAsStream("/private.properties")
        val prop = Properties()
        prop.load(fis)

        server = prop.getProperty("dbServer")
        user = prop.getProperty("dbUser")
        password = prop.getProperty("dbPassword")

        println("server: $server")
        println("user: $user")
        println("pass: $password")
    }

    /**
     * Establishes a connection to the database and returns it.
     *
     * This is the only valid option to receive a Connection.
     *
     * @return Connection
     */
    private fun getConnection(): Connection? {
        return try {
            Class.forName("org.mariadb.jdbc.Driver")
            DriverManager.getConnection(
                "jdbc:mariadb://$server", user, password
            )
        } catch (err: SQLException) {
            err.printStackTrace()
            return null
        }
    }

    /**
     * Executes a SQL statement after establishing a connection first. The connection is closed afterwards
     */
    fun executeSQL(sql: String): Boolean {
        try {
            val con = this.getConnection() ?: return false
            val stmt =  con.createStatement()
            stmt.execute(sql)
            con.close()
            return true
        } catch (err: SQLException) {
            err.printStackTrace()
            return false
        }
    }

    /**
     * This method returns a PreparedStatement.
     *
     * @param sql The SQL statement you want to execute including placeholder '?'s
     * @return PreparedStatement
     */
    fun getPreparedStatement(sql: String): PreparedStatement? {
        val con = this.getConnection() ?: return null
        return try {
            con.prepareStatement(sql)
        } catch (err: SQLException) {
            err.printStackTrace()
            null
        }
    }

    /**
     * This method executes a PreparedStatement but does not return a ResultSet.
     * See queryPreparedStatement() if you need the ResultSet.
     *
     * @see de.carldressler.coyn.database.DBHandler.queryPreparedStatement
     */
    fun executePreparedStatement(prepStatement: PreparedStatement): Boolean {
        return try {
            prepStatement.execute()
            prepStatement.connection.close()
            true
        } catch (err: SQLException) {
            err.printStackTrace()
            false
        }
    }

    /**
     * This method executes a PreparedStatement and returns its corresponding ResultSet.
     * See executePreparedStatement() if you don't need the ResultSet.
     *
     * @see de.carldressler.coyn.database.DBHandler.executePreparedStatement
     */
    fun queryPreparedStatement(prepStatement: PreparedStatement): ResultSet? {
        return try {
            val rs = prepStatement.executeQuery()
            prepStatement.connection.close()
            rs
        } catch (err: SQLException) {
            err.printStackTrace()
            null
        }
    }
}