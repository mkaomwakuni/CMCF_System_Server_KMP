package cnc.coop.milkcreamies.repositoryImpl

import cnc.coop.milkcreamies.data.DataAccess
import cnc.coop.milkcreamies.data.DatabaseConfig
import cnc.coop.milkcreamies.domain.models.MilkInEntry
import cnc.coop.milkcreamies.domain.repository.MilkInEntryRepository
import org.slf4j.LoggerFactory

class MilkInEntryRepositoryImpl : MilkInEntryRepository {
    override suspend fun getMilkInEntryById(entryId: String): MilkInEntry? {
        return DataAccess.getMilkInEntryById(entryId)
    }

    override suspend fun getAllMilkInEntries(): List<MilkInEntry> {
        return DataAccess.getAllMilkInEntries()
    }

    override suspend fun addMilkInEntry(entry: MilkInEntry): MilkInEntry {
        val logger = LoggerFactory.getLogger("MilkInEntryRepositoryImpl")
        logger.info("Repository: Adding milk entry: $entry")

        // Generate an ID if one isn't provided
        val entryId = entry.entryId ?: DatabaseConfig.generateNextEntryId()
        logger.info("Repository: Using entryId: $entryId")

        val newEntry = entry.copy(entryId = entryId)
        DataAccess.addMilkInEntry(newEntry)

        // Update inventory when milk is added
        DatabaseConfig.updateInventoryOnMilkIn(entry.liters)

        logger.info("Repository: Successfully added milk entry with ID: $entryId")
        return newEntry
    }

    override suspend fun deleteMilkInEntry(entryId: String): Boolean {
        return DataAccess.deleteMilkInEntry(entryId)
    }
}