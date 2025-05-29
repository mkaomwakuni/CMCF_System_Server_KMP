package cnc.coop.milkcreamies.data

import cnc.coop.milkcreamies.domain.models.PaymentMode
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date

object MilkOutEntries : Table("milk_out_entries") {
    val id = varchar("sale_id", 10)
    val customerId = varchar("customer_id", 10).references(Customers.id)
    val customerName = varchar("customer_name", 255)
    val date = date("date")
    val quantitySold = double("quantity_sold")
    val pricePerLiter = double("price_per_liter")
    val paymentMode = enumerationByName<PaymentMode>("payment_mode", 50)

    override val primaryKey = PrimaryKey(id)
}