package com.projet.sluca.smallbrother.serverAPI

import com.projet.sluca.smallbrother.AideData
import retrofit2.Response
import retrofit2.http.*

/***
 * Interface to communicate with API
 */
interface ApiService {
    @POST("data")
    suspend fun sendData(@Body data: AideData): Response<AideData>

    @GET("data/{code}")
    suspend fun getDataBySharedCode(@Path("code")code:Int): Response<AideData>

}
