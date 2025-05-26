package cnc.coop.milkcreamies.data

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.sum
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory


object DatabaseConfig {

    // Inventory management functions
    fun updateInventoryOnMilkIn(liters: Double) {
        transaction {
            val currentRecord = MilkInventoryTable.selectAll().singleOrNull()
            if (currentRecord != null) {
                val newStock = currentRecord[MilkInventoryTable.currentStock] + liters
                MilkInventoryTable.update {
                    it[currentStock] = newStock
                    it[lastUpdated] = LocalDate(2025, 6, 11) // Use current date when implementing
                }
            }
        }
    }

    fun updateInventoryOnMilkOut(liters: Double) {
        transaction {
            val currentRecord = MilkInventoryTable.selectAll().singleOrNull()
            if (currentRecord != null) {
                val newStock = maxOf(0.0, currentRecord[MilkInventoryTable.currentStock] - liters)
                MilkInventoryTable.update {
                    it[currentStock] = newStock
                    it[lastUpdated] = LocalDate(2025, 6, 11) // Use current date when implementing
                }
            }
        }
    }

    fun updateInventoryOnSpoilage(liters: Double) {
        transaction {
            val currentRecord = MilkInventoryTable.selectAll().singleOrNull()
            if (currentRecord != null) {
                val newStock = maxOf(0.0, currentRecord[MilkInventoryTable.currentStock] - liters)
                MilkInventoryTable.update {
                    it[currentStock] = newStock
                    it[lastUpdated] = LocalDate(2025, 6, 11) // Use current date when implementing
                }
            }
        }
    }

    fun getCurrentStock(): Double {
        return transaction {
            // Use the helper function to calculate current stock
            val currentStock = calculateCurrentStock()

            println("=== STOCK CALCULATION DEBUG ===")
            // We'll need to re-calculate the components for the debug output
            val totalMilkIn = MilkInEntries
                .slice(MilkInEntries.liters.sum())
                .selectAll()
                .map { it[MilkInEntries.liters.sum()] ?: 0.0 }
                .firstOrNull() ?: 0.0

            val totalMilkOut = MilkOutEntries
                .slice(MilkOutEntries.quantitySold.sum())
                .selectAll()
                .map { it[MilkOutEntries.quantitySold.sum()] ?: 0.0 }
                .firstOrNull() ?: 0.0

            val totalSpoilage = MilkSpoiltEntries
                .slice(MilkSpoiltEntries.amountSpoilt.sum())
                .selectAll()
                .map { it[MilkSpoiltEntries.amountSpoilt.sum()] ?: 0.0 }
                .firstOrNull() ?: 0.0

            println("Total Milk In: ${totalMilkIn}L")
            println("Total Milk Out: ${totalMilkOut}L")
            println("Total Spoilage: ${totalSpoilage}L")
            println("Current Stock: ${currentStock}L")
            println("Formula: ${totalMilkIn}L - ${totalMilkOut}L - ${totalSpoilage}L = ${currentStock}L")
            println("===============================")

            // Update the inventory table with the calculated stock
            val inventoryRecord = MilkInventoryTable.selectAll().singleOrNull()
            if (inventoryRecord != null) {
                MilkInventoryTable.update {
                    it[MilkInventoryTable.currentStock] = currentStock
                    it[lastUpdated] = LocalDate(2025, 6, 11)
                }
            }

            currentStock
        }
    }

    // Helper function to calculate stock without any side effects
    private fun calculateCurrentStock(): Double {
        return transaction {
            // Calculate real-time stock from actual data
            // Total milk in - Total milk out - Total spoilage
            val totalMilkIn = MilkInEntries
                .slice(MilkInEntries.liters.sum())
                .selectAll()
                .map { it[MilkInEntries.liters.sum()] ?: 0.0 }
                .firstOrNull() ?: 0.0

            val totalMilkOut = MilkOutEntries
                .slice(MilkOutEntries.quantitySold.sum())
                .selectAll()
                .map { it[MilkOutEntries.quantitySold.sum()] ?: 0.0 }
                .firstOrNull() ?: 0.0

            val totalSpoilage = MilkSpoiltEntries
                .slice(MilkSpoiltEntries.amountSpoilt.sum())
                .selectAll()
                .map { it[MilkSpoiltEntries.amountSpoilt.sum()] ?: 0.0 }
                .firstOrNull() ?: 0.0

            maxOf(0.0, totalMilkIn - totalMilkOut - totalSpoilage)
        }
    }

    fun generateNextCowId(): String {
        return transaction {
            val maxId = Cows.selectAll()
                .mapNotNull { it.getOrNull(Cows.id) }
                .filter { it.startsWith("CW") }
                .maxByOrNull { it.substring(2).toIntOrNull() ?: 0 }

            val nextNumber = if (maxId == null) {
                1 // Start from CW01
            } else {
                (maxId.substring(2).toIntOrNull() ?: 0) + 1
            }

            "CW%02d".format(nextNumber)
        }
    }

    fun generateNextCustomerId(): String {
        return transaction {
            val maxId = Customers.selectAll()
                .mapNotNull { it.getOrNull(Customers.id) }
                .filter { it.startsWith("CST") }
                .maxByOrNull { it.substring(3).toIntOrNull() ?: 0 }

            val nextNumber = if (maxId == null) {
                1 // Start from CST001
            } else {
                (maxId.substring(3).toIntOrNull() ?: 0) + 1
            }

            "CST%03d".format(nextNumber)
        }
    }

    fun generateNextSaleId(): String {
        return transaction {
            val maxId = MilkOutEntries.selectAll()
                .mapNotNull { it.getOrNull(MilkOutEntries.id) }
                .filter { it.startsWith("SL") }
                .maxByOrNull { it.substring(2).toIntOrNull() ?: 0 }

            val nextNumber = if (maxId == null) {
                1 // Start from SL01
            } else {
                (maxId.substring(2).toIntOrNull() ?: 0) + 1
            }

            "SL%02d".format(nextNumber)
        }
    }

    fun generateNextEntryId(): String {
        val logger = LoggerFactory.getLogger("DatabaseConfig")
        logger.info("Generating next entry ID")

        val result = transaction {
            val maxId = MilkInEntries.selectAll()
                .mapNotNull { it.getOrNull(MilkInEntries.id) }
                .filter { it.startsWith("ETR") }
                .maxByOrNull { it.substring(3).toIntOrNull() ?: 0 }

            val nextNumber = if (maxId == null) {
                logger.info("No existing entry IDs found, starting with ETR001")
                1 // Start from ETR001
            } else {
                val num = (maxId.substring(3).toIntOrNull() ?: 0) + 1
                logger.info("Found max entry ID: $maxId, next number will be: $num")
                num
            }

            "ETR%03d".format(nextNumber)
        }

        logger.info("Generated entry ID: $result")
        return result
    }

    fun generateNextSpoiltId(): String {
        return transaction {
            val maxId = MilkSpoiltEntries.selectAll()
                .mapNotNull { it.getOrNull(MilkSpoiltEntries.id) }
                .filter { it.startsWith("SPL") }
                .maxByOrNull { it.substring(3).toIntOrNull() ?: 0 }

            val nextNumber = if (maxId == null) {
                1 // Start from SPL001
            } else {
                (maxId.substring(3).toIntOrNull() ?: 0) + 1
            }

            "SPL%03d".format(nextNumber)
        }
    }

    fun generateNextOwnerId(): String {
        return transaction {
            val maxId = Members.selectAll()
                .mapNotNull { it.getOrNull(Members.id) }
                .filter { it.startsWith("OON") }
                .maxByOrNull { it.substring(3).toIntOrNull() ?: 0 }

            val nextNumber = if (maxId == null) {
                1 // Start from OON01
            } else {
                (maxId.substring(3).toIntOrNull() ?: 0) + 1
            }

            "OON%02d".format(nextNumber)
        }
    }

    fun addCustomerIfNotExists(customerName: String): String {
        return transaction {
            // Check if customer already exists by name
            val existingCustomer = Customers.select { Customers.name eq customerName }
                .map { it[Customers.id] }
                .singleOrNull()

            if (existingCustomer != null) {
                // Return existing customer ID
                existingCustomer
            } else {
                // Create new customer with auto-generated ID
                val customerId = generateNextCustomerId()
                Customers.insert {
                    it[id] = customerId
                    it[name] = customerName
                }
                customerId
            }
        }
    }

    // Helper function to ensure MilkInventory exists
    private fun ensureMilkInventoryExists() {
        transaction {
            val inventoryCount = MilkInventoryTable.selectAll().count()
            if (inventoryCount == 0L) {
                // Calculate the current stock
                val stock = calculateCurrentStock()

                // Create the inventory record
                MilkInventoryTable.insert {
                    it[currentStock] = stock
                    it[lastUpdated] = LocalDate(2025, 6, 11)
                }
            }
        }
    }

    // Helper function to create default superuser
    private fun createDefaultSuperuser() {
        transaction {
            // Check if any superuser exists
            val superuserExists = Users.select { Users.role eq "superuser" }.count() > 0L

            if (!superuserExists) {
                val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
                Users.insert {
                    it[username] = "admin"
                    it[email] = "admin@chonyimilk.coop"
                    it[passwordHash] = "admin_hashed" // Simple hash - password is "admin"
                    it[firstName] = "System"
                    it[lastName] = "Administrator"
                    it[role] = "superuser"
                    it[isActive] = true
                    it[createdAt] = now
                }
                println("Default superuser created: username=admin, password=admin")
            }
        }
    }

    fun init() {
        // Ensure the data directory exists
        java.io.File("./data").mkdirs()

        // Use file-based H2 database with AUTO_SERVER=TRUE to allow multiple connections
        Database.connect(
            url = "jdbc:h2:file:./data/chonyi_milk_cooperative;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        )

        transaction {
            // Create tables with updated schema
            SchemaUtils.createMissingTablesAndColumns(
                Members,
                Cows,
                Customers,
                MilkInEntries,
                MilkOutEntries,
                MilkSpoiltEntries,
                MilkInventoryTable,
                Users,
                UserData
            )

            // Only initialize with sample data if database is empty
            val hasData = Members.selectAll().count() > 0L ||
                    Cows.selectAll().count() > 0L ||
                    Customers.selectAll().count() > 0L

            if (!hasData) {
                // Initialize with sample data and create default superuser
                createDefaultSuperuser()
            } else {
                // Make sure MilkInventory exists even if data is already present
                ensureMilkInventoryExists()
                // Ensure default superuser exists
                createDefaultSuperuser()
            }
        }
    }
}
