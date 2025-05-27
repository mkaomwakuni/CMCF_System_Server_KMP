package cnc.coop.milkcreamies.domain.models

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Represents milk from a cow (Milk In)
 */
@Serializable
data class MilkInEntry(
    val entryId: String?,
    val cowId: String?,
    val ownerId: String,
    val liters: Double,
    val date: LocalDate,
    val milkingType: MilkingType
)

/**
 * Represents a milk sale to a customer or business (Milk Out)
 */
@Serializable
data class MilkOutEntry(
    val saleId: String,
    val customerId: String,
    val customerName: String,
    val date: LocalDate,
    val quantitySold: Double,
    val pricePerLiter: Double,
    val paymentMode: PaymentMode
)

/**
 * Represents spoiled milk entry
 */
@Serializable
data class MilkSpoiltEntry(
    val spoiltId: String,
    val date: LocalDate,
    val amountSpoilt: Double,
    val lossAmount: Double,
    val cause: SpoilageCause? = null
)

/**
 * Real-time inventory tracking
 */
@Serializable
data class MilkInventory(
    val currentStock: Double,
    val lastUpdated: LocalDate
)

@Serializable
enum class MilkingType {
    MORNING,
    EVENING
}

@Serializable
enum class PaymentMode {
    CASH,
    MPESA
}

@Serializable
enum class SpoilageCause {
    CONTAMINATION,
    TEMPERATURE,
    SOUR,
    IMPROPER_STORAGE,
    EQUIPMENT_FAILURE,
    EXPIRED,
    OTHER
}