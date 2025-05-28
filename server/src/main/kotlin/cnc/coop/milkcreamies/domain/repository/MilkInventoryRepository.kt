package cnc.coop.milkcreamies.domain.repository

import cnc.coop.milkcreamies.domain.models.MilkInventory
import kotlinx.datetime.LocalDate

interface MilkInventoryRepository {
    suspend fun getCurrentInventory(): MilkInventory
    suspend fun updateInventory(currentStock: Double, date: LocalDate): Boolean
}