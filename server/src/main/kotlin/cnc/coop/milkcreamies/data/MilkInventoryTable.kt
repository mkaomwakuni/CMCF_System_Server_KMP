package cnc.coop.milkcreamies.data

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date

// Real-time milk inventory tracking
object MilkInventoryTable : Table("milk_inventory") {
    val id = integer("id").autoIncrement()
    val currentStock = double("current_stock")
    val lastUpdated = date("last_updated")

    override val primaryKey = PrimaryKey(id)
}