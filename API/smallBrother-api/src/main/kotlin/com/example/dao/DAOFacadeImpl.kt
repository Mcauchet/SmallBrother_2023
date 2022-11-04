package com.example.dao

import com.example.dao.DatabaseFactory.dbQuery
import com.example.models.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class DAOFacadeImpl : DAOFacade {
    private fun resultRowToAideData(row: ResultRow) = AideData(
        id = row[AideDatas.id],
        img1 = row[AideDatas.img1],
        img2 = row[AideDatas.img2],
        motion = row[AideDatas.motion],
        battery = row[AideDatas.battery],
        key = row[AideDatas.key],
    )

    override suspend fun allAideData(): List<AideData> = dbQuery {
        AideDatas.selectAll().map(::resultRowToAideData)
    }

    override suspend fun getAideData(key: String): AideData? = dbQuery {
        AideDatas
            .select {AideDatas.key eq key}
            .map(::resultRowToAideData)
            .singleOrNull()
    }

    override suspend fun addAideData(data: AideData): Unit = dbQuery {
        val notExists = AideDatas.select {AideDatas.key eq data.key}.empty()
        if (!notExists) {
            editAideData(data)
        } else {
            AideDatas.insert {
                it[id] = data.id
                it[img1] = data.img1
                it[img2] = data.img2
                it[motion] = data.motion
                it[battery] = data.battery
                it[key] = data.key
            }
        }
    }

    override suspend fun editAideData(data: AideData): Boolean = dbQuery {
        val id = data.id
        AideDatas.update({AideDatas.id eq id}) {
            it[img1] = data.img1
            it[img2] = data.img2
            it[motion] = data.motion
            it[battery] = data.battery
            it[key] = data.key
        } > 0
    }

    override suspend fun deleteAideData(key: String): Boolean = dbQuery {
        AideDatas.deleteWhere { AideDatas.key eq key } > 0
    }

    override suspend fun deleteAideDatas(): Boolean = dbQuery {
        AideDatas.deleteAll() > 0
    }
}

val dao: DAOFacade = DAOFacadeImpl().apply {
    runBlocking {
        if(allAideData().isEmpty()) {
            addAideData(AideData(1, "img1", "img2", false, 76, "idsjfgijgsd23432"))
        }
    }
}