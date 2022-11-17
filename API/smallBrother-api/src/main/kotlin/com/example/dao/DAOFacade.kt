package com.example.dao

import com.example.models.AideData

/***
 * The interface of the DAO to access the database queries
 *
 * @author Maxime Caucheteur (Updated on 04-11-22)
 */
interface DAOFacade {
    //TODO dev purpose, delete before prod
    suspend fun allAideData() : List<AideData>
    suspend fun getAideData(key: String): AideData?
    suspend fun addAideData(data: AideData)
    suspend fun editAideData(data: AideData): Boolean
    suspend fun deleteAideData(key: String): Boolean
    suspend fun deleteAideDatas(): Boolean
}