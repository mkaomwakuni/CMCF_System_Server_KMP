package cnc.coop.milkcreamies.domain.models

import kotlinx.serialization.Serializable

/**
 * Response model for cow eligibility checks
 */
@Serializable
data class CowEligibilityResponse(
    val cowId: String?,
    val cowName: String,
    val healthStatus: String,
    val isEligible: Boolean,
    val reason: String? = null,
    val blockedUntil: String? = null,
    val isActive: Boolean
)

/**
 * Response model for bulk cow eligibility checks
 */
@Serializable
data class BulkEligibilityResponse(
    val cows: List<CowEligibilityResponse>,
    val totalCows: Int,
    val eligibleCows: Int,
    val blockedCows: Int
)

/**
 * Response model for detailed cow health information
 */
@Serializable
data class CowHealthDetailsResponse(
    val cowId: String?,
    val name: String,
    val healthStatus: String,
    val vaccinationLast: String? = null,
    val vaccinationWaitingPeriodEnd: String? = null,
    val antibioticTreatment: String? = null,
    val antibioticWaitingPeriodEnd: String? = null,
    val canCollectMilk: Boolean,
    val blockedReason: String? = null
)