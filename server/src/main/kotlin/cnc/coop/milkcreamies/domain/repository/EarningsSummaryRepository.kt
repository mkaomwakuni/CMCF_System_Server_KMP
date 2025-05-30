package cnc.coop.milkcreamies.domain.repository

import cnc.coop.milkcreamies.domain.models.EarningsSummary
import kotlinx.datetime.LocalDate

interface EarningsSummaryRepository {
    suspend fun getEarningsSummary(currentDate: LocalDate): EarningsSummary
}