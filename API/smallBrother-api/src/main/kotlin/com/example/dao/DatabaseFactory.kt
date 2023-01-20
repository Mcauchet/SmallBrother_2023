package com.example.dao

import com.example.models.*
import io.ktor.server.config.*
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
    fun init(config: ApplicationConfig) {
        val driverClassName = config.property("storage.driverClassName").getString()
        val jdbcURL = config.property("storage.jdbcURL").getString()
        val username = config.property("storage.user").getString()
        val password = System.getenv()["PASSWORD"]
        if(password.isNullOrEmpty()) return
        val database = Database.connect(jdbcURL, driverClassName, username, password)
        transaction(database) {
            SchemaUtils.create(AideDatas)
            SchemaUtils.create(Admins)
        }

        //TODO switch to POSTGRESQL TRIGGER DEF
        /*transaction {
            val con = TransactionManager.current().connection
            val statement = con.prepareStatement(sql, false)
            statement.executeUpdate()
        }*/
    }

    suspend fun <T> dbQuery(block: suspend()->T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}

