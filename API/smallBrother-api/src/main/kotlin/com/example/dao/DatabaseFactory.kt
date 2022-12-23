package com.example.dao

import com.example.models.*
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.*
import org.jetbrains.exposed.sql.transactions.experimental.*

/***
 * creates an instance of the database if not existent
 *
 * @see "https://ktor.io/docs/interactive-website-add-persistence.html"
 */
object DatabaseFactory {
    fun init() {
        val driverClassName = "org.h2.Driver"
        val jdbcURL = "jdbc:h2:file:./build/db"
        val database = Database.connect(jdbcURL, driverClassName)
        transaction(database) {
            SchemaUtils.create(AideDatas)
        }

        //Adds a trigger to delete rows from database that are more than 24 hours old
        transaction {
            val sql = """
            CREATE TRIGGER IF NOT EXISTS delete_old_rows 
            AFTER 
            INSERT 
            ON aidedatas FOR EACH ROW
            CALL "com.example.DeleteTrigger"
        """
            val con = TransactionManager.current().connection
            val statement = con.prepareStatement(sql, false)
            statement.executeUpdate()
        }
    }

    suspend fun <T> dbQuery(block: suspend()->T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}

