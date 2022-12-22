package com.example.dao

import com.example.dao.DatabaseFactory.dbQuery
import com.example.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.CurrentDateTime

/***
 * DAOFacadeImpl implements the DAOFacade methods.
 *
 * @author Maxime Caucheteur
 * @version 1 (Updated on 22-12-2022)
 */
class DAOFacadeImpl : DAOFacade {
    private fun resultRowToAideData(row: ResultRow) = AideData(
        uri = row[AideDatas.uri],
        aesKey = row[AideDatas.aesKey],
    )

    //TODO this will have to be deleted, this access all users data, only for dev purpose.
    override suspend fun allAideData(): List<AideData> = dbQuery {
        AideDatas.selectAll().map(::resultRowToAideData)
    }

    override suspend fun getAideData(uri: String): AideData? = dbQuery {
        AideDatas
            .select {AideDatas.uri eq uri}
            .map(::resultRowToAideData)
            .singleOrNull()
    }

    override suspend fun addAideData(data: AideData): Unit = dbQuery {
        val notExists = AideDatas.select {AideDatas.uri eq data.uri}.empty()
        if (!notExists) {
            editAideData(data)
        } else {
            AideDatas.insert {
                it[uri] = data.uri
                it[aesKey] = data.aesKey
                it[createdAt] = CurrentDateTime
            }
        }
    }

    override suspend fun editAideData(data: AideData): Boolean = dbQuery {
        AideDatas.update({AideDatas.uri eq data.uri}) {
            it[uri] = data.uri
            it[aesKey] = data.aesKey
        } > 0
    }

    override suspend fun deleteAideData(uri: String): Boolean = dbQuery {
        AideDatas.deleteWhere { AideDatas.uri eq uri } > 0
    }

    override suspend fun deleteAideDatas(): Boolean = dbQuery {
        AideDatas.deleteAll() > 0
    }
}

val dao: DAOFacade = DAOFacadeImpl()