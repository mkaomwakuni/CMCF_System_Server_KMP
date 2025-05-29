package cnc.coop.milkcreamies.domain.repository

import cnc.coop.milkcreamies.domain.models.MilkOutEntry

interface MilkOutEntryRepository {
    suspend fun getMilkOutEntryById(saleId: String): MilkOutEntry?
    suspend fun getAllMilkOutEntries(): List<MilkOutEntry>
    suspend fun addMilkOutEntry(entry: MilkOutEntry): MilkOutEntry
    suspend fun deleteMilkOutEntry(saleId: String): Boolean
}