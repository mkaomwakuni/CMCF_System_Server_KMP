package cnc.coop.milkcreamies.repositoryImpl

import cnc.coop.milkcreamies.data.DataAccess
import cnc.coop.milkcreamies.data.DatabaseConfig
import cnc.coop.milkcreamies.domain.models.MilkSpoiltEntry
import cnc.coop.milkcreamies.domain.repository.MilkSpoiltEntryRepository

class MilkSpoiltEntryRepositoryImpl : MilkSpoiltEntryRepository {
    override suspend fun getMilkSpoiltEntryById(spoiltId: String): MilkSpoiltEntry? {
        return DataAccess.getMilkSpoiltEntryById(spoiltId)
    }

    override suspend fun getAllMilkSpoiltEntries(): List<MilkSpoiltEntry> {
        return DataAccess.getAllMilkSpoiltEntries()
    }

    override suspend fun addMilkSpoiltEntry(entry: MilkSpoiltEntry): MilkSpoiltEntry {
        val spoiltId = DatabaseConfig.generateNextSpoiltId()
        val newEntry = entry.copy(spoiltId = spoiltId)
        DataAccess.addMilkSpoiltEntry(newEntry)

        // Update inventory when milk is spoilt
        DatabaseConfig.updateInventoryOnSpoilage(entry.amountSpoilt)

        return newEntry
    }

    override suspend fun deleteMilkSpoiltEntry(spoiltId: String): Boolean {
        return DataAccess.deleteMilkSpoiltEntry(spoiltId)
    }
}