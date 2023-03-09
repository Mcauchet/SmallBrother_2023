package com.example.dao

import com.example.models.*
import io.ktor.server.config.*
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.*
import org.jetbrains.exposed.sql.transactions.experimental.*

/**
 * creates an instance of the database if not existent
 *
 * @see "https://ktor.io/docs/interactive-website-add-persistence.html"
 */
object DatabaseFactory {
    fun init(config: ApplicationConfig) {
        val driverClassName = config.property("storage.driverClassName").getString()
        val jdbcURL = "jdbc:postgresql://${System.getenv()["DATABASE_HOST"]}:" +
                "${System.getenv()["DATABASE_PORT"]}/${System.getenv()["DATABASE_NAME"]}"
        val database = Database.connect(jdbcURL, driverClassName, System.getenv()["DATABASE_USERNAME"].toString(),
            System.getenv()["DATABASE_PASSWORD"].toString())
        transaction(database) {
            SchemaUtils.create(AideDatas, Admins)
        }
    }

    suspend fun <T> dbQuery(block: suspend()->T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}

