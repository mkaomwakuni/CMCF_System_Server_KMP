package cnc.coop.milkcreamies.data
import cnc.coop.milkcreamies.domain.models.ActionStatus
import cnc.coop.milkcreamies.domain.models.HealthStatus
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date

object Members : Table("members") {
    val id = varchar("member_id", 10)
    val name = varchar("name", 255)
    val isActive = bool("is_active").default(true)
    val archiveDate = date("archive_date").nullable()
    val archiveReason = varchar("archive_reason", 255).nullable()

    override val primaryKey = PrimaryKey(id)
}