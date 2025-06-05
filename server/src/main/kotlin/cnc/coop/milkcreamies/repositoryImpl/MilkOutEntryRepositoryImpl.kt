package cnc.coop.milkcreamies.repositoryImpl

import cnc.coop.milkcreamies.data.DataAccess
import cnc.coop.milkcreamies.data.DatabaseConfig
import cnc.coop.milkcreamies.domain.models.MilkOutEntry
import cnc.coop.milkcreamies.domain.repository.MilkOutEntryRepository

class MilkOutEntryRepositoryImpl : MilkOutEntryRepository {
    override suspend fun getMilkOutEntryById(saleId: String): MilkOutEntry? {
        return DataAccess.getMilkOutEntryById(saleId)
    }

    override suspend fun getAllMilkOutEntries(): List<MilkOutEntry> {
        return DataAccess.getAllMilkOutEntries()
    }

    override suspend fun addMilkOutEntry(entry: MilkOutEntry): MilkOutEntry {
        val saleId = DatabaseConfig.generateNextSaleId()
        // Find or create customer by name
        val customerId = DatabaseConfig.addCustomerIfNotExists(entry.customerName)
        val newEntry = entry.copy(saleId = saleId, customerId = customerId)
        DataAccess.addMilkOutEntry(newEntry)

        // Update inventory when milk is sold
        DatabaseConfig.updateInventoryOnMilkOut(entry.quantitySold)

        return newEntry
    }

    override suspend fun deleteMilkOutEntry(saleId: String): Boolean {
        return DataAccess.deleteMilkOutEntry(saleId)
    }
}