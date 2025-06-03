package cnc.coop.milkcreamies.domain

import kotlinx.serialization.Serializable


/**
 * Client model for cow eligibility response
 */
@Serializable
data class CowEligibilityResponse(
    val cowId: String?,
    val cowName: String,
    val healthStatus: String,
    val isEligible: Boolean,
    val reason: String?,
    val blockedUntil: String?,
    val isActive: Boolean
)

/**
 * Client model for cow health details with waiting periods
 */
@Serializable
data class CowHealthDetailsResponse(
    val cowId: String?,
    val name: String,
    val healthStatus: String,
    val vaccinationLast: String?,
    val vaccinationWaitingPeriodEnd: String?,
    val antibioticTreatment: String?,
    val antibioticWaitingPeriodEnd: String?,
    val canCollectMilk: Boolean,
    val blockedReason: String?
)

/**
 * Client model for bulk eligibility check
 */
@Serializable
data class BulkEligibilityResponse(
    val cows: List<CowEligibilityResponse>,
    val totalCows: Int,
    val eligibleCows: Int,
    val blockedCows: Int
)

/**
 * Client model for milk collection validation request
 */
@Serializable
data class MilkCollectionValidationRequest(
    val cowId: String,
    val date: String // In YYYY-MM-DD format
)