package cnc.coop.milkcreamies.domain.repository

import cnc.coop.milkcreamies.domain.models.MilkSpoiltEntry

interface MilkSpoiltEntryRepository {
    suspend fun getMilkSpoiltEntryById(spoiltId: String): MilkSpoiltEntry?
    suspend fun getAllMilkSpoiltEntries(): List<MilkSpoiltEntry>
    suspend fun addMilkSpoiltEntry(entry: MilkSpoiltEntry): MilkSpoiltEntry
    suspend fun deleteMilkSpoiltEntry(spoiltId: String): Boolean
}