package com.projet.sluca.smallbrother.server

import com.projet.sluca.smallbrother.AideData
import io.ktor.client.statement.*

interface AideDataRepository {
    suspend fun getAideData(): Result<HttpResponse>

    suspend fun addAideData(aideData: AideData): Result<Unit>
}