package com.projet.sluca.smallbrother.server

import com.projet.sluca.smallbrother.AideData
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

class ClientAPI(private val client: HttpClient) {

    val END_POINT_KTOR = "http://127.0.0.1:8080/aideData"
    val SUB_END_POINT_GET_DATA_KTOR="?key="
    //val END_POINT_POST_USER_KTOR=""

    suspend fun getAideData(): HttpResponse =
        client.get("$END_POINT_KTOR$SUB_END_POINT_GET_DATA_KTOR")

    suspend fun addAideData(aideData: AideData) {
        client.post(END_POINT_KTOR) {
            setBody(aideData)
        }
    }

}