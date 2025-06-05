package cnc.coop.milkcreamies.routes

import cnc.coop.milkcreamies.data.DatabaseConfig
import cnc.coop.milkcreamies.data.DataAccess
import cnc.coop.milkcreamies.domain.models.Cow
import cnc.coop.milkcreamies.domain.MilkCollectionError
import cnc.coop.milkcreamies.domain.models.MilkInEntry
import cnc.coop.milkcreamies.domain.models.MilkingType
import cnc.coop.milkcreamies.domain.CowEligibilityResponse
import cnc.coop.milkcreamies.domain.CowHealthDetailsResponse
import cnc.coop.milkcreamies.domain.BulkEligibilityResponse
import cnc.coop.milkcreamies.plugins.MilkInEntryRequest
import cnc.coop.milkcreamies.domain.repository.CowRepository
import cnc.coop.milkcreamies.domain.repository.MilkInEntryRepository
import cnc.coop.milkcreamies.validators.CowHealthValidationService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

/**
 * Defines routes for milk-in operations with health validation
 */
fun Route.milkInRoutes(
    milkInRepository: MilkInEntryRepository,
    cowRepository: CowRepository,
    validationService: CowHealthValidationService
) {
    val logger = LoggerFactory.getLogger("MilkInRoutes")

    route("/milk-in") {
        post {
            try {
                val request = call.receive<MilkInEntryRequest>()

                // Validate request
                if (request.liters <= 0) {
                    logger.warn("Invalid liters: ${request.liters}")
                    call.respond(HttpStatusCode.BadRequest, "Milk quantity must be positive")
                    return@post
                }

                if (request.ownerId.isBlank()) {
                    logger.warn("Empty owner ID")
                    call.respond(HttpStatusCode.BadRequest, "Owner ID is required")
                    return@post
                }

                // Get cow information if cowId is provided
                var cow: Cow? = null
                if (!request.cowId.isNullOrBlank()) {
                    // Use runBlocking to handle suspending function
                    cow = runBlocking { cowRepository.getCowById(request.cowId) }
                    if (cow == null) {
                        logger.warn("Invalid cowId: ${request.cowId}")
                        call.respond(
                            HttpStatusCode.BadRequest,
                            "Cow with ID ${request.cowId} not found"
                        )
                        return@post
                    }

                    // Validate cow ownership
                    if (cow.ownerId != request.ownerId) {
                        logger.warn("Cow ownership mismatch: cowId=${request.cowId}, ownerId=${request.ownerId}, cowOwnerId=${cow.ownerId}")
                        call.respond(
                            HttpStatusCode.BadRequest,
                            "Cow ${request.cowId} does not belong to owner ${request.ownerId}"
                        )
                        return@post
                    }

                    // Validate cow is active
                    if (!cow.isActive) {
                        logger.warn("Cow is archived: cowId=${request.cowId}")
                        call.respond(
                            HttpStatusCode.BadRequest,
                            "Cannot collect milk from archived cow ${cow.name}"
                        )
                        return@post
                    }

                    // HEALTH STATUS VALIDATION
                    val requestDate = LocalDate.parse(request.date)
                    val validationResult =
                        validationService.validateMilkCollection(cow, requestDate)
                    
                    // Alternative approach using DataAccess directly:
                    val validationResultDirect = DataAccess.validateCowForMilkCollection(cow.cowId!!, requestDate)

                    if (!validationResult.isValid || !validationResultDirect.isValid) {
                        logger.info("Milk collection rejected: cowId=${cow.cowId}, healthStatus=${cow.status.healthStatus}, reason=${validationResult.errorMessage}")
                        call.respond(
                            HttpStatusCode.BadRequest,
                            MilkCollectionError(
                                error = validationResult.errorMessage
                                    ?: "Cannot collect milk from cow",
                                cowId = cow.cowId,
                                cowName = cow.name,
                                healthStatus = cow.status.healthStatus.toString(),
                                blockedUntil = validationResult.blockedUntil?.toString(),
                                suggestions = listOf(
                                    "Wait until the health restriction period ends",
                                    "Select another cow"
                                )
                            )
                        )
                        return@post
                    }
                }

                // If validation passes, create the milk entry
                val milkEntry = MilkInEntry(
                    entryId = DatabaseConfig.generateNextEntryId(),
                    cowId = request.cowId,
                    ownerId = request.ownerId,
                    liters = request.liters,
                    date = LocalDate.parse(request.date),
                    milkingType = MilkingType.valueOf(request.milkingType)
                )

                // Use runBlocking to handle suspending function
                val createdEntry = runBlocking { milkInRepository.addMilkInEntry(milkEntry) }
                logger.info("MilkInEntry created: entryId=${createdEntry.entryId}, cowId=${createdEntry.cowId}, ownerId=${createdEntry.ownerId}")
                call.respond(HttpStatusCode.Created, createdEntry)

            } catch (e: ContentTransformationException) {
                logger.error("Invalid request format", e)
                call.respond(HttpStatusCode.BadRequest, "Invalid request format")
            } catch (e: IllegalArgumentException) {
                logger.error("Invalid data: ${e.message}", e)
                call.respond(HttpStatusCode.BadRequest, "Invalid data: ${e.message}")
            } catch (e: Exception) {
                logger.error("Server error: ${e.message}", e)
                call.respond(HttpStatusCode.InternalServerError, "Server error: ${e.message}")
            }
        }

        get {
            try {
                logger.debug("Getting all milk-in entries")
                // Use runBlocking to handle suspending function
                val entries = runBlocking { milkInRepository.getAllMilkInEntries() }
                call.respond(entries)
            } catch (e: Exception) {
                logger.error("Failed to fetch milk entries", e)
                call.respond(HttpStatusCode.InternalServerError, "Failed to fetch milk entries")
            }
        }

        get("/{id}") {
            try {
                val entryId = call.parameters["id"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest, "Missing entry ID"
                )
                logger.debug("GET /milk-in/$entryId")

                // Validate ID format (implementation depends on your ID pattern)
                if (!isValidEntryId(entryId)) {
                    logger.warn("Invalid entryId format: $entryId")
                    return@get call.respond(
                        HttpStatusCode.BadRequest, "Invalid entry ID format"
                    )
                }

                // Use runBlocking to handle suspending function
                val entry = runBlocking { milkInRepository.getMilkInEntryById(entryId) }
                if (entry == null) {
                    logger.info("MilkInEntry not found: $entryId")
                    call.respond(HttpStatusCode.NotFound, "MilkInEntry not found")
                } else {
                    call.respond(entry)
                }
            } catch (e: Exception) {
                val entryId = call.parameters["id"] ?: "unknown"
                logger.error("Error in GET /milk-in/$entryId: ${e.message}", e)
                call.respond(
                    HttpStatusCode.InternalServerError, "Server error: ${e.message}"
                )
            }
        }

        delete("/{id}") {
            try {
                val entryId = call.parameters["id"] ?: return@delete call.respond(
                    HttpStatusCode.BadRequest, "Missing entry ID"
                )
                logger.debug("DELETE /milk-in/$entryId")

                if (!isValidEntryId(entryId)) {
                    logger.warn("Invalid entryId format: $entryId")
                    return@delete call.respond(
                        HttpStatusCode.BadRequest, "Invalid entry ID format"
                    )
                }

                // Use runBlocking to handle suspending function
                val success = runBlocking { milkInRepository.deleteMilkInEntry(entryId) }
                if (success) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    logger.info("MilkInEntry not found: $entryId")
                    call.respond(HttpStatusCode.NotFound, "MilkInEntry not found")
                }
            } catch (e: Exception) {
                val entryId = call.parameters["id"] ?: "unknown"
                logger.error("Error in DELETE /milk-in/$entryId: ${e.message}", e)
                call.respond(
                    HttpStatusCode.InternalServerError, "Server error: ${e.message}"
                )
            }
        }
    }
}

// Helper method to validate entry ID format
private fun isValidEntryId(id: String): Boolean {
    return id.matches(Regex("^ETR\\d{3,4}\$"))
}

/**
 * Additional endpoint to check cow milk collection eligibility
 */
fun Route.cowEligibilityRoutes(
    cowRepository: CowRepository,
    validationService: CowHealthValidationService
) {
    val logger = LoggerFactory.getLogger("CowEligibilityRoutes")

    route("/cows") {
        get("/{cowId}/milk-eligibility") {
            try {
                val cowId = call.parameters["cowId"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest, "Cow ID is required"
                )

                logger.debug("Checking eligibility for cowId=$cowId")
                
                // You can use either the repository + validation service approach or direct DataAccess
                // Approach 1: Using repository and validation service
                // Use runBlocking to handle suspending function
                val cow = runBlocking { cowRepository.getCowById(cowId) } ?: return@get call.respond(
                    HttpStatusCode.NotFound, "Cow not found"
                )
                
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                val validationResult = validationService.validateMilkCollection(cow, today)
                
                // Approach 2: Using DataAccess directly (simpler)
                val validationResultDirect = DataAccess.validateCowForMilkCollection(cowId, today)
                if (validationResultDirect.errorMessage == "Cow not found") {
                    return@get call.respond(HttpStatusCode.NotFound, "Cow not found")
                }

                call.respond(
                    CowEligibilityResponse(
                        cowId = cow.cowId,
                        cowName = cow.name,
                        healthStatus = cow.status.healthStatus.toString(),
                        isEligible = validationResult.isValid,
                        reason = validationResult.errorMessage,
                        blockedUntil = validationResult.blockedUntil?.toString(),
                        isActive = cow.isActive
                    )
                )

            } catch (e: Exception) {
                logger.error("Server error: ${e.message}", e)
                call.respond(HttpStatusCode.InternalServerError, "Server error: ${e.message}")
            }
        }
        
        // Add a new bulk eligibility endpoint for multiple cows
        get("/milk-eligibility") {
            try {
                // Get query parameters for filtering
                val ownerIdParam = call.parameters["ownerId"]
                val activeOnlyParam = call.parameters["activeOnly"]?.toLowerCase() == "true"
                
                logger.debug("Checking eligibility for multiple cows: ownerId=$ownerIdParam, activeOnly=$activeOnlyParam")
                
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

                // Use runBlocking to handle suspending functions
                val cows = runBlocking {
                    if (ownerIdParam != null) {
                        // Use getCowsByMember instead of getCowsByOwnerId
                        val cowsWithStats =
                            cowRepository.getCowsByMember(ownerIdParam, !activeOnlyParam)
                        // Extract just the cows from CowWithStats
                        cowsWithStats.map { it.cow }
                    } else {
                        if (activeOnlyParam) {
                            cowRepository.getActiveCows()
                        } else {
                            cowRepository.getAllCows()
                        }
                    }
                }
                
                // Validate each cow and create response
                val eligibilityResults = cows.map { cow ->
                    val validationResult = validationService.validateMilkCollection(cow, today)

                    CowEligibilityResponse(
                        cowId = cow.cowId,
                        cowName = cow.name,
                        healthStatus = cow.status.healthStatus.toString(),
                        isEligible = validationResult.isValid,
                        reason = validationResult.errorMessage,
                        blockedUntil = validationResult.blockedUntil?.toString(),
                        isActive = cow.isActive
                    )
                }

                val response = BulkEligibilityResponse(
                    cows = eligibilityResults,
                    totalCows = eligibilityResults.size,
                    eligibleCows = eligibilityResults.count { it.isEligible },
                    blockedCows = eligibilityResults.count { !it.isEligible }
                )
                call.respond(response)

            } catch (e: Exception) {
                logger.error("Server error: ${e.message}", e)
                call.respond(HttpStatusCode.InternalServerError, "Server error: ${e.message}")
            }
        }

        // Add an endpoint to get detailed cow health information with waiting periods
        get("/{cowId}/health-details") {
            try {
                val cowId = call.parameters["cowId"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest, "Cow ID is required"
                )

                logger.debug("Getting health details for cowId=$cowId")

                // Use runBlocking to handle suspending function
                val cow =
                    runBlocking { cowRepository.getCowById(cowId) } ?: return@get call.respond(
                        HttpStatusCode.NotFound, "Cow not found"
                    )

                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                val validationResult = validationService.validateMilkCollection(cow, today)

                // Calculate waiting period end dates if applicable
                val vaccinationWaitingPeriodEnd =
                    cow.status.vaccinationLast?.plus(DatePeriod(days = 2))
                val antibioticWaitingPeriodEnd =
                    cow.status.antibioticTreatment?.plus(DatePeriod(days = 3))

                call.respond(
                    CowHealthDetailsResponse(
                        cowId = cow.cowId,
                        name = cow.name,
                        healthStatus = cow.status.healthStatus.toString(),
                        vaccinationLast = cow.status.vaccinationLast?.toString(),
                        vaccinationWaitingPeriodEnd = vaccinationWaitingPeriodEnd?.toString(),
                        antibioticTreatment = cow.status.antibioticTreatment?.toString(),
                        antibioticWaitingPeriodEnd = antibioticWaitingPeriodEnd?.toString(),
                        canCollectMilk = validationResult.isValid,
                        blockedReason = validationResult.errorMessage
                    )
                )

            } catch (e: Exception) {
                logger.error("Server error: ${e.message}", e)
                call.respond(HttpStatusCode.InternalServerError, "Server error: ${e.message}")
            }
        }
    }
}
