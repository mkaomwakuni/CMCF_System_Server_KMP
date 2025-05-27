package cnc.coop.milkcreamies.data

import cnc.coop.milkcreamies.domain.models.ActionStatus
import cnc.coop.milkcreamies.domain.models.HealthStatus
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date

object Cows : Table("cows") {
    val id = varchar("cow_id", 10)
    val entryDate = date("entry_date")
    val ownerId = varchar("owner_id", 10).references(Members.id)
    val name = varchar("name", 255)
    val breed = varchar("breed", 255)
    val age = integer("age")
    val weight = double("weight")
    val healthStatus = enumerationByName<HealthStatus>("health_status", 50)
    val actionStatus = enumerationByName<ActionStatus>("action_status", 50)
    val isActive = bool("is_active").default(true)
    val archiveReason = varchar("archive_reason", 255).nullable()
    val archiveDate = date("archive_date").nullable()
    val dewormingDue = date("deworming_due").nullable()
    val dewormingLast = date("deworming_last").nullable()
    val calvingDate = date("calving_date").nullable()
    val vaccinationDue = date("vaccination_due").nullable()
    val vaccinationLast = date("vaccination_last").nullable()
    val antibioticTreatment = date("antibiotic_treatment").nullable()
    val averageLitersPerDay = double("average_liters_per_day").default(0.0)
    val note = text("note").nullable()

    override val primaryKey = PrimaryKey(id)
}