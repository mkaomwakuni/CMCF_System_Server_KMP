package cnc.coop.milkcreamies.domain.models

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Represents a cow in the cooperative
 */
@Serializable
data class Cow(
    val cowId: String? = null,
    val entryDate: LocalDate,
    val ownerId: String,
    val name: String,
    val breed: String,
    val age: Int,
    val weight: Double,
    val status: CowStatus,
    val isActive: Boolean = true,
    val archiveReason: String? = null,
    val archiveDate: LocalDate? = null,
    val note: String? = null
)

/**
 * Represents the cow's health and action statuses
 */
@Serializable
data class CowStatus(
    val healthStatus: HealthStatus,
    val actionStatus: ActionStatus,
    val dewormingDue: LocalDate? = null,
    val dewormingLast: LocalDate? = null,
    val calvingDate: LocalDate? = null,
    val vaccinationDue: LocalDate? = null,
    val vaccinationLast: LocalDate? = null,
    val antibioticTreatment: LocalDate? = null
)

/**
 * Cow with calculated milk production statistics
 */
@Serializable
data class CowWithStats(
    val cow: Cow,
    val averageDailyMilkProduction: Double,
    val lastMilkingDate: LocalDate? = null
)

/**
 * Enhanced cow summary with active/archived filtering
 */
@Serializable
data class CowSummary(
    val totalActiveCows: Int,
    val totalArchivedCows: Int,
    val healthyCows: Int,
    val needsAttention: Int
)

@Serializable
enum class HealthStatus {
    HEALTHY,
    SICK,
    NEEDS_ATTENTION,
    UNDER_TREATMENT,
    GESTATION,
    VACCINATED,
    ANTIBIOTICS
}

@Serializable
enum class ActionStatus {
    ACTIVE,
    SOLD,
    WORMED,
    VACCINATED,
    DECEASED
}