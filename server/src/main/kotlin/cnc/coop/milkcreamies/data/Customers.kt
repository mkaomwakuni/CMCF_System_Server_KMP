package cnc.coop.milkcreamies.data
import org.jetbrains.exposed.sql.Table

object Customers : Table("customers") {
    val id = varchar("customer_id", 10)
    val name = varchar("name", 255)

    override val primaryKey = PrimaryKey(id)
}