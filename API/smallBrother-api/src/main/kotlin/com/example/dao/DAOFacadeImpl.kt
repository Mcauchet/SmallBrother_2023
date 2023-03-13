package com.example.dao

import com.example.dao.DatabaseFactory.dbQuery
import com.example.models.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.mindrot.jbcrypt.BCrypt

/***
 * DAOFacadeImpl implements the DAOFacade methods.
 *
 * @author Maxime Caucheteur
 * @version 1 (Updated on 13-03-2023)
 */
class DAOFacadeImpl : DAOFacade {
    private fun resultRowToAideData(row: ResultRow) = AideData(
        uri = row[AideDatas.uri],
        aesKey = row[AideDatas.aesKey],
        signature = row[AideDatas.signature],
        iv = row[AideDatas.iv]
    )

    private fun resultRowToAdmin(row: ResultRow) = Admin(
        email = row[Admins.email],
        encPwd = row[Admins.encPwd],
        phoneNumber = row[Admins.phoneNumber],
    )

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
                it[signature] = data.signature
                it[createdAt] = CurrentDateTime
                it[iv] = data.iv
            }
        }
    }

    override suspend fun editAideData(data: AideData): Boolean = dbQuery {
        AideDatas.update({AideDatas.uri eq data.uri}) {
            it[uri] = data.uri
            it[aesKey] = data.aesKey
            it[signature] = data.signature
            it[iv] = data.iv
        } > 0
    }

    override suspend fun deleteAideData(uri: String): Boolean = dbQuery {
        AideDatas.deleteWhere { AideDatas.uri eq uri } > 0
    }

    override suspend fun deleteAideDatas(): Boolean = dbQuery {
        AideDatas.deleteAll() > 0
    }

    override suspend fun allAdmin(): List<Admin> = dbQuery {
        Admins.selectAll().map(::resultRowToAdmin)
    }

    override suspend fun addAdmin(admin: Admin): Unit = dbQuery {
        val notExists = Admins.select {Admins.email eq admin.email}.empty()
        if (notExists) {
            Admins.insert {
                it[email] = admin.email
                it[encPwd] = admin.encPwd
                it[phoneNumber] = admin.phoneNumber
            }
        } else {
            editAdmin(admin)
        }
    }

    override suspend fun getAdmin(email: String): Admin? = dbQuery {
        Admins
            .select {Admins.email eq email}
            .map(::resultRowToAdmin)
            .singleOrNull()
    }

    override suspend fun editAdmin(admin: Admin): Boolean = dbQuery {
        Admins.update({Admins.email eq admin.email}) {
            it[email] = admin.email
            it[encPwd] = admin.encPwd
            it[phoneNumber] = admin.phoneNumber
        } > 0
    }
}

val dao: DAOFacade = DAOFacadeImpl().apply {
    runBlocking {
        if(allAdmin().isEmpty()) {
            addAdmin(
                Admin(
                    "adminSB@hotmail.com",
                    BCrypt.hashpw("123456", BCrypt.gensalt(12)),
                    ""
                )
            )
        }
    }
}