package cnc.coop.milkcreamies.validators

import cnc.coop.milkcreamies.domain.models.Cow
import cnc.coop.milkcreamies.domain.models.HealthStatus
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/**
 * Service to validate if a cow is eligible for milk collection based on health status
 * and treatment history
 */
class CowHealthValidationService {

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null,
        val blockedUntil: LocalDate? = null
    )

    /**
     * Validates if milk can be collected from a cow based on its health status and treatment history
     *
     * @param cow The cow to validate
     * @param requestDate The date for which milk collection is requested
     * @return A validation result with eligibility status and error information if blocked
     */
    fun validateMilkCollection(cow: Cow, requestDate: LocalDate): ValidationResult {
        return when (cow.status.healthStatus) {
            HealthStatus.HEALTHY,
            HealthStatus.NEEDS_ATTENTION,
            HealthStatus.GESTATION -> {
                // These statuses allow milk collection
                ValidationResult(isValid = true)
            }

            HealthStatus.UNDER_TREATMENT,
            HealthStatus.SICK -> {
                ValidationResult(
                    isValid = false,
                    errorMessage = "Cannot collect milk from cow ${cow.name} - cow is under treatment/sick"
                )
            }

            HealthStatus.VACCINATED -> {
                val vaccinationDate = cow.status.vaccinationLast
                if (vaccinationDate != null) {
                    val blockedUntil = vaccinationDate.plus(DatePeriod(days = 2)) // 48 hours
                    if (requestDate <= blockedUntil) {
                        ValidationResult(
                            isValid = false,
                            errorMessage = "Cannot collect milk from cow ${cow.name} - vaccination waiting period (blocked until ${blockedUntil})",
                            blockedUntil = blockedUntil
                        )
                    } else {
                        ValidationResult(isValid = true)
                    }
                } else {
                    // No vaccination date recorded, block collection
                    ValidationResult(
                        isValid = false,
                        errorMessage = "Cannot collect milk from cow ${cow.name} - vaccination date not recorded"
                    )
                }
            }

            HealthStatus.ANTIBIOTICS -> {
                val treatmentDate = cow.status.antibioticTreatment
                if (treatmentDate != null) {
                    val blockedUntil = treatmentDate.plus(DatePeriod(days = 3)) // 72 hours
                    if (requestDate <= blockedUntil) {
                        ValidationResult(
                            isValid = false,
                            errorMessage = "Cannot collect milk from cow ${cow.name} - antibiotic waiting period (blocked until ${blockedUntil})",
                            blockedUntil = blockedUntil
                        )
                    } else {
                        ValidationResult(isValid = true)
                    }
                } else {
                    // No treatment date recorded, block collection
                    ValidationResult(
                        isValid = false,
                        errorMessage = "Cannot collect milk from cow ${cow.name} - antibiotic treatment date not recorded"
                    )
                }
            }

            // Safety else branch for future enum additions
            else -> {
                ValidationResult(
                    isValid = false,
                    errorMessage = "Cannot collect milk from cow ${cow.name} - unknown health status: ${cow.status.healthStatus}"
                )
            }
        }
    }
}
