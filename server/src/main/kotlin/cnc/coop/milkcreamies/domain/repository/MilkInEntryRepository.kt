package cnc.coop.milkcreamies.domain.repository

import cnc.coop.milkcreamies.domain.models.MilkInEntry

interface MilkInEntryRepository {
    suspend fun getMilkInEntryById(entryId: String): MilkInEntry?
    suspend fun getAllMilkInEntries(): List<MilkInEntry>
    suspend fun addMilkInEntry(entry: MilkInEntry): MilkInEntry
    suspend fun deleteMilkInEntry(entryId: String): Boolean
}