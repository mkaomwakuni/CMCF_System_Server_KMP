package cnc.coop.milkcreamies.domain.repository

import cnc.coop.milkcreamies.domain.models.StockSummary
import kotlinx.datetime.LocalDate

interface StockSummaryRepository {
    suspend fun getStockSummary(currentDate: LocalDate): StockSummary
}