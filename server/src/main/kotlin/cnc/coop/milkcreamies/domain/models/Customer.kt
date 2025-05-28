package cnc.coop.milkcreamies.domain.models

import kotlinx.serialization.Serializable

/**
 * Represents a customer or business buying milk
 */
@Serializable
data class Customer(
    val customerId: String,
    val name: String
)