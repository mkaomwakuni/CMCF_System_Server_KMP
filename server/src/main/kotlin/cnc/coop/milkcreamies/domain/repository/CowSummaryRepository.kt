package cnc.coop.milkcreamies.domain.repository

import cnc.coop.milkcreamies.domain.models.CowSummary

interface CowSummaryRepository {
    suspend fun getCowSummary(): CowSummary
}