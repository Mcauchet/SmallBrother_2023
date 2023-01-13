package com.example.dao

import com.example.models.Admin
import com.example.models.AideData

/***
 * The interface of the DAO to access the database queries
 *
 * @author Maxime Caucheteur
 * @version 1 (Updated on 30-11-22)
 */
interface DAOFacade {
    //TODO dev purpose, delete before prod
    suspend fun allAideData() : List<AideData>
    suspend fun getAideData(uri: String): AideData?
    suspend fun addAideData(data: AideData)
    suspend fun editAideData(data: AideData): Boolean
    suspend fun deleteAideData(uri: String): Boolean
    suspend fun deleteAideDatas(): Boolean
    suspend fun allAdmin(): List<Admin>
    suspend fun addAdmin(admin: Admin)
    suspend fun getAdmin(email: String): Admin?
    //Add edit admin
    suspend fun editAdmin(admin: Admin): Boolean
}