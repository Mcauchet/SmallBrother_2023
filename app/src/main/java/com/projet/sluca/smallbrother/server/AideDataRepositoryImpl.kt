package com.projet.sluca.smallbrother.server

import com.projet.sluca.smallbrother.AideData
import io.ktor.client.statement.*

class AideDataRepositoryImpl( private val clientApi: ClientAPI): AideDataRepository {

    override suspend fun getAideData(): Result<HttpResponse> {
        return try {
            Result.success(clientApi.getAideData())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addAideData(aideData: AideData): Result<Unit> {
        return try {
            Result.success(clientApi.addAideData(aideData))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}