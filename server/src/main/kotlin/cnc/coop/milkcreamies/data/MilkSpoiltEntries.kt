package cnc.coop.milkcreamies.data

import cnc.coop.milkcreamies.domain.models.SpoilageCause
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date

object MilkSpoiltEntries : Table("milk_spoilt_entries") {
    val id = varchar("spoilt_id", 10)
    val date = date("date")
    val amountSpoilt = double("amount_spoilt")
    val lossAmount =
        double("loss_amount")
    val cause = enumerationByName<SpoilageCause>("cause", 50).nullable()

    override val primaryKey = PrimaryKey(id)
}