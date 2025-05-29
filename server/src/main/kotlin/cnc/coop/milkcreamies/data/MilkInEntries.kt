package cnc.coop.milkcreamies.data

import cnc.coop.milkcreamies.domain.models.MilkingType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date

object MilkInEntries : Table("milk_in_entries") {
    val id = varchar("entry_id", 10)
    val cowId = varchar("cow_id", 10).references(Cows.id)
    val ownerId = varchar("owner_id", 10).references(Members.id)
    val liters = double("liters")
    val date = date("date")
    val milkingType = enumerationByName<MilkingType>("milking_type", 50)

    override val primaryKey = PrimaryKey(id)
}