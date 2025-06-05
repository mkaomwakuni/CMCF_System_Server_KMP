package cnc.coop.milkcreamies.plugins

import cnc.coop.milkcreamies.data.DatabaseConfig
import cnc.coop.milkcreamies.domain.models.Cow
import cnc.coop.milkcreamies.domain.models.Member
import cnc.coop.milkcreamies.domain.models.MilkOutEntry
import cnc.coop.milkcreamies.domain.models.MilkSpoiltEntry
import cnc.coop.milkcreamies.domain.models.PaymentMode
import cnc.coop.milkcreamies.domain.models.SpoilageCause
import cnc.coop.milkcreamies.domain.repository.CowRepository
import cnc.coop.milkcreamies.domain.repository.CowSummaryRepository
import cnc.coop.milkcreamies.domain.repository.CustomerRepository
import cnc.coop.milkcreamies.domain.repository.EarningsSummaryRepository
import cnc.coop.milkcreamies.domain.repository.MemberRepository
import cnc.coop.milkcreamies.domain.repository.MilkInEntryRepository
import cnc.coop.milkcreamies.domain.repository.MilkOutEntryRepository
import cnc.coop.milkcreamies.domain.repository.MilkSpoiltEntryRepository
import cnc.coop.milkcreamies.domain.repository.StockSummaryRepository
import cnc.coop.milkcreamies.repositoryImpl.CowRepositoryImpl
import cnc.coop.milkcreamies.repositoryImpl.CowSummaryRepositoryImpl
import cnc.coop.milkcreamies.repositoryImpl.CustomerRepositoryImpl
import cnc.coop.milkcreamies.repositoryImpl.EarningsSummaryRepositoryImpl
import cnc.coop.milkcreamies.repositoryImpl.MemberRepositoryImpl
import cnc.coop.milkcreamies.repositoryImpl.MilkInEntryRepositoryImpl
import cnc.coop.milkcreamies.repositoryImpl.MilkOutEntryRepositoryImpl
import cnc.coop.milkcreamies.repositoryImpl.MilkSpoiltEntryRepositoryImpl
import cnc.coop.milkcreamies.repositoryImpl.StockSummaryRepositoryImpl
import cnc.coop.milkcreamies.routes.cowEligibilityRoutes
import cnc.coop.milkcreamies.routes.milkInRoutes
import cnc.coop.milkcreamies.routes.userRoutes
import cnc.coop.milkcreamies.validators.CowHealthValidationService
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory

@Serializable
data class MemberRequest(val name: String)

@Serializable
data class MilkInEntryRequest(
    val cowId: String?, // Reference to the cow, nullable if not from specific cow
    val ownerId: String, // Reference to the owner (Member)
    val liters: Double, // Liters of milk produced
    val date: String, // Date of milk collection in YYYY-MM-DD format
    val milkingType: String // Morning or Evening
)

@Serializable
data class MilkOutEntryRequest(
    val customerName: String, // Name of the customer
    val date: String, // Date of sale in YYYY-MM-DD format
    val quantitySold: Double, // Liters sold
    val pricePerLiter: Double, // Price per liter in KSh
    val paymentMode: String // Cash or M-Pesa
)

@Serializable
data class MilkSpoiltEntryRequest(
    val date: String, // Date the milk was recorded as spoilt in YYYY-MM-DD format
    val amountSpoilt: Double, // Liters spoilt
    val lossAmount: Double, // Monetary loss in KES
    val cause: SpoilageCause? = null // Reason for spoilage
)

private val logger = LoggerFactory.getLogger("Plugins")

fun Application.configurePlugins() {
    // Configure JSON serialization
    install(ContentNegotiation) {
        json(kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            isLenient = true
            coerceInputValues = true
            useAlternativeNames = true
        })
    }

    // Configure routing
    configureRouting()
}

fun Application.configureRouting() {
    // Initialize repositories
    val cowRepository: CowRepository = CowRepositoryImpl()
    val memberRepository: MemberRepository = MemberRepositoryImpl()
    val milkInEntryRepository: MilkInEntryRepository = MilkInEntryRepositoryImpl()
    val milkOutEntryRepository: MilkOutEntryRepository = MilkOutEntryRepositoryImpl()
    val milkSpoiltEntryRepository: MilkSpoiltEntryRepository = MilkSpoiltEntryRepositoryImpl()
    val stockSummaryRepository: StockSummaryRepository = StockSummaryRepositoryImpl()
    val cowSummaryRepository: CowSummaryRepository = CowSummaryRepositoryImpl()
    val earningsSummaryRepository: EarningsSummaryRepository = EarningsSummaryRepositoryImpl()
    val customerRepository: CustomerRepository = CustomerRepositoryImpl()
    val logger = LoggerFactory.getLogger("MilkSpoiltRoutes")
    
    // Initialize cow health validation service
    val cowHealthValidationService = CowHealthValidationService()


    routing {
        // User authentication and management routes (no authentication required for signup/signin)
        userRoutes()

        // Get all milk spoilage entries
        get("/api/milkSpoilt") {
            try {
                val entries = milkSpoiltEntryRepository.getAllMilkSpoiltEntries()
                call.respond(HttpStatusCode.OK, entries)
            } catch (e: Exception) {
                logger.error("Failed to get milk spoilage entries", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    "Failed to retrieve milk spoilage entries"
                )
            }
        }

        // Get a specific milk spoilage entry by ID
        get("/api/milkSpoilt/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                "Missing ID parameter"
            )

            try {
                val entry = milkSpoiltEntryRepository.getMilkSpoiltEntryById(id)
                if (entry != null) {
                    call.respond(HttpStatusCode.OK, entry)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Milk spoilage entry not found")
                }
            } catch (e: Exception) {
                logger.error("Failed to get milk spoilage entry with ID: $id", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    "Failed to retrieve milk spoilage entry"
                )
            }
        }

        // Add a new milk spoilage entry
        post("/api/milkSpoilt") {
            try {
                val request = call.receive<MilkSpoiltEntryRequest>()


                val entry = MilkSpoiltEntry(
                    spoiltId = "",
                    amountSpoilt = request.amountSpoilt,
                    date = LocalDate.parse(request.date),
                    lossAmount = request.lossAmount,
                    cause = request.cause
                )

                // Add the entry through the repository
                val created = milkSpoiltEntryRepository.addMilkSpoiltEntry(entry)

                // Respond with the created entry
                call.respond(HttpStatusCode.Created, created)
            } catch (e: Exception) {
                logger.error("Failed to add milk spoilage entry", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    "Failed to add milk spoilage entry: ${e.message}"
                )
            }
        }

        // Delete a milk spoilage entry
        delete("/api/milkSpoilt/{id}") {
            val id = call.parameters["id"] ?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                "Missing ID parameter"
            )

            try {
                val success = milkSpoiltEntryRepository.deleteMilkSpoiltEntry(id)
                if (success) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Milk spoilage entry not found")
                }
            } catch (e: Exception) {
                logger.error("Failed to delete milk spoilage entry with ID: $id", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    "Failed to delete milk spoilage entry"
                )
            }
        }

        // Health check (no authentication)
        get("/health") {
            logger.info("Health check requested")
            call.respondText("Server is running!")
        }

        // Authenticated routes
        route("/") {
            // Authentication middleware
            intercept(ApplicationCallPipeline.Call) {
                // Get API key from header
                val apiKey = call.request.headers["X-API-Key"]

                // For demonstration purposes, accept any non-null API key
                // In a real application, this would validate against stored API keys
                if (apiKey == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
                    return@intercept finish()
                }
            }

            route("/cows") {
                get {
                    try {
                        logger.debug("GET /cows")
                        val cows = cowRepository.getAllCows()
                        call.respond(cows)
                    } catch (e: Exception) {
                        logger.error("Error in GET /cows: ${e.message}", e)
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Internal server error")
                        )
                    }
                }

                get("/{id}") {
                    try {
                        val cowId = call.parameters["id"] ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Missing cow ID")
                        )
                        logger.debug("GET /cows/$cowId")
                        if (!isValidCowId(cowId)) {
                            logger.warn("Invalid cowId format: $cowId")
                            return@get call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Invalid cow ID format")
                            )
                        }
                        val cow = cowRepository.getCowById(cowId)
                        if (cow == null) {
                            logger.info("Cow not found: $cowId")
                            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Cow not found"))
                        } else {
                            call.respond(cow)
                        }
                    } catch (e: Exception) {
                        val cowId = call.parameters["id"] ?: "unknown"
                        logger.error("Error in GET /cows/$cowId: ${e.message}", e)
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Internal server error")
                        )
                    }
                }

                post {
                    try {
                        val cow = call.receive<Cow>()
                        logger.debug("POST /cows: $cow")
                        // Validate ownerId
                        if (!isValidOwnerId(cow.ownerId)) {
                            logger.warn("Invalid ownerId format: ${cow.ownerId}")
                            return@post call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Invalid ownerId format")
                            )
                        }
                        val member = memberRepository.getMemberById(cow.ownerId)
                        if (member == null) {
                            logger.warn("Invalid ownerId: ${cow.ownerId}")
                            return@post call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Invalid ownerId")
                            )
                        }
                        // Validate data
                        if (cow.age <= 0 || cow.weight <= 0) {
                            logger.warn("Invalid cow data: age=${cow.age}, weight=${cow.weight}")
                            return@post call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Invalid cow data: age and weight must be positive")
                            )
                        }

                        // Create a new cow object with server-generated ID if not provided
                        val cowToAdd = if (cow.cowId == null) {
                            cow.copy(cowId = DatabaseConfig.generateNextCowId())
                        } else {
                            cow
                        }

                        val newCows = cowRepository.addCow(cowToAdd)
                        call.respond(HttpStatusCode.Created, newCows)
                    } catch (e: Exception) {
                        logger.error("Error in POST /cows: ${e.message}", e)
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Internal server error")
                        )
                    }
                }

                put("/{id}") {
                    try {
                        val cowId = call.parameters["id"] ?: return@put call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Missing cow ID")
                        )
                        logger.debug("PUT /cows/$cowId")
                        if (!isValidCowId(cowId)) {
                            logger.warn("Invalid cowId format: $cowId")
                            return@put call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Invalid cow ID format")
                            )
                        }
                        val updatedCow = call.receive<Cow>()
                        if (updatedCow.cowId != cowId) {
                            logger.warn("Cow ID mismatch: URL=$cowId, body=${updatedCow.cowId}")
                            return@put call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Cow ID mismatch")
                            )
                        }
                        if (!isValidOwnerId(updatedCow.ownerId)) {
                            logger.warn("Invalid ownerId format: ${updatedCow.ownerId}")
                            return@put call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Invalid ownerId format")
                            )
                        }
                        val member = memberRepository.getMemberById(updatedCow.ownerId)
                        if (member == null) {
                            logger.warn("Invalid ownerId: ${updatedCow.ownerId}")
                            return@put call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Invalid ownerId")
                            )
                        }
                        if (updatedCow.age <= 0 || updatedCow.weight <= 0) {
                            logger.warn("Invalid cow data: age=${updatedCow.age}, weight=${updatedCow.weight}")
                            return@put call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Invalid cow data: age and weight must be positive")
                            )
                        }
                        val success = cowRepository.updateCow(updatedCow)
                        if (success) {
                            call.respond(updatedCow)
                        } else {
                            logger.info("Cow not found: $cowId")
                            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Cow not found"))
                        }
                    } catch (e: Exception) {
                        val cowId = call.parameters["id"] ?: "unknown"
                        logger.error("Error in PUT /cows/$cowId: ${e.message}", e)
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Internal server error")
                        )
                    }
                }

                delete("/{id}") {
                    try {
                        val cowId = call.parameters["id"] ?: return@delete call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Missing cow ID")
                        )
                        logger.debug("DELETE /cows/$cowId")
                        if (!isValidCowId(cowId)) {
                            logger.warn("Invalid cowId format: $cowId")
                            return@delete call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Invalid cow ID format")
                            )
                        }
                        val success = cowRepository.deleteCow(cowId)
                        if (success) {
                            call.respond(HttpStatusCode.NoContent)
                        } else {
                            logger.info("Cow not found: $cowId")
                            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Cow not found"))
                        }
                    } catch (e: Exception) {
                        val cowId = call.parameters["id"] ?: "unknown"
                        logger.error("Error in DELETE /cows/$cowId: ${e.message}", e)
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Internal server error")
                        )
                    }
                }
            }

            route("/members") {
                get {
                    try {
                        logger.debug("GET /members")
                        val members = memberRepository.getAllMembers()
                        call.respond(members)
                    } catch (e: Exception) {
                        val memberId = call.parameters["id"] ?: "unknown"
                        logger.error("Error in GET /members/$memberId: ${e.message}", e)
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Internal server error")
                        )
                    }
                }

                get("/{id}") {
                    try {
                        val memberId = call.parameters["id"] ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Missing member ID")
                        )
                        logger.debug("GET /members/$memberId")
                        if (!isValidOwnerId(memberId)) {
                            logger.warn("Invalid memberId format: $memberId")
                            return@get call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Invalid member ID format")
                            )
                        }
                        val member = memberRepository.getMemberById(memberId)
                        if (member == null) {
                            logger.info("Member not found: $memberId")
                            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Member not found"))
                        } else {
                            call.respond(member)
                        }
                    } catch (e: Exception) {
                        val memberId = call.parameters["id"] ?: "unknown"
                        logger.error("Error in GET /members/$memberId: ${e.message}", e)
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Internal server error")
                        )
                    }
                }

                post {
                    try {
                        val request = call.receive<MemberRequest>()
                        logger.debug("POST /members: $request")
                        if (request.name.isBlank()) {
                            logger.warn("Invalid member name: ${request.name}")
                            return@post call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Member name cannot be empty")
                            )
                        }
                        // Server generates the ID
                        val member = Member(
                            memberId = DatabaseConfig.generateNextOwnerId(),
                            name = request.name
                        )
                        val newMember = memberRepository.addMember(member)
                        call.respond(HttpStatusCode.Created, newMember)
                    } catch (e: Exception) {
                        logger.error("Error in POST /members: ${e.message}", e)
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Internal server error")
                        )
                    }
                }
            }

            route("/customers") {
                get {
                    try {
                        logger.debug("GET /customers")
                        val customers = customerRepository.getAllCustomers()
                        call.respond(customers)
                    } catch (e: Exception) {
                        logger.error("Error in GET /customers: ${e.message}", e)
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Internal server error")
                        )
                    }
                }

                get("/{id}") {
                    try {
                        val customerId = call.parameters["id"] ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Missing customer ID")
                        )
                        logger.debug("GET /customers/$customerId")
                        if (!isValidCustomerId(customerId)) {
                            logger.warn("Invalid customerId format: $customerId")
                            return@get call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Invalid customer ID format")
                            )
                        }
                        val customer = customerRepository.getCustomerById(customerId)
                        if (customer == null) {
                            logger.info("Customer not found: $customerId")
                            call.respond(
                                HttpStatusCode.NotFound,
                                mapOf("error" to "Customer not found")
                            )
                        } else {
                            call.respond(customer)
                        }
                    } catch (e: Exception) {
                        val customerId = call.parameters["id"] ?: "unknown"
                        logger.error("Error in GET /customers/$customerId: ${e.message}", e)
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Internal server error")
                        )
                    }
                }
            }

            // Register custom routes with cow health validation
            milkInRoutes(milkInEntryRepository, cowRepository, cowHealthValidationService)
            cowEligibilityRoutes(cowRepository, cowHealthValidationService)

            route("/milk-out") {
                get {
                    try {
                        logger.debug("GET /milk-out")
                        val entries = milkOutEntryRepository.getAllMilkOutEntries()
                        call.respond(entries)
                    } catch (e: Exception) {
                        val saleId = call.parameters["id"] ?: "unknown"
                        logger.error("Error in GET /milk-out/$saleId: ${e.message}", e)
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Internal server error")
                        )
                    }
                }

                get("/{id}") {
                    try {
                        val saleId = call.parameters["id"] ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Missing sale ID")
                        )
                        logger.debug("GET /milk-out/$saleId")
                        if (!isValidSaleId(saleId)) {
                            logger.warn("Invalid saleId format: $saleId")
                            return@get call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Invalid sale ID format")
                            )
                        }
                        val entry = milkOutEntryRepository.getMilkOutEntryById(saleId)
                        if (entry == null) {
                            logger.info("MilkOutEntry not found: $saleId")
                            call.respond(HttpStatusCode.NotFound, mapOf("error" to "MilkOutEntry not found"))
                        } else {
                            call.respond(entry)
                        }
                    } catch (e: Exception) {
                        val saleId = call.parameters["id"] ?: "unknown"
                        logger.error("Error in GET /milk-out/$saleId: ${e.message}", e)
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Internal server error")
                        )
                    }
                }

                post {
                    try {
                        val request = call.receive<MilkOutEntryRequest>()
                        logger.debug("POST /milk-out: $request")

                        if (request.customerName.isBlank()) {
                            logger.warn("Empty customer name: ${request.customerName}")
                            return@post call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Customer name cannot be empty")
                            )
                        }

                        if (request.quantitySold <= 0 || request.pricePerLiter <= 0) {
                            logger.warn("Invalid data: quantitySold=${request.quantitySold}, pricePerLiter=${request.pricePerLiter}")
                            return@post call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Quantity sold and price per liter must be greater than zero")
                            )
                        }

                        // Convert request to domain object
                        val entry = try {

                            val saleId = DatabaseConfig.generateNextSaleId()
                            val customerId =
                                DatabaseConfig.addCustomerIfNotExists(request.customerName)

                            MilkOutEntry(
                                saleId = saleId,
                                customerId = customerId,
                                customerName = request.customerName,
                                date = LocalDate.parse(request.date),
                                quantitySold = request.quantitySold,
                                pricePerLiter = request.pricePerLiter,
                                paymentMode = PaymentMode.valueOf(request.paymentMode)
                            )
                        } catch (e: Exception) {
                            logger.warn("Invalid request data: ${e.message}")
                            return@post call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Invalid request data: ${e.message}")
                            )
                        }

                        val newEntry = milkOutEntryRepository.addMilkOutEntry(entry)
                        call.respond(HttpStatusCode.Created, newEntry)
                    } catch (e: Exception) {
                        logger.error("Error in POST /milk-out: ${e.message}", e)
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Internal server error")
                        )
                    }
                }

                delete("/{id}") {
                    try {
                        val saleId = call.parameters["id"] ?: return@delete call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Missing sale ID")
                        )
                        logger.debug("DELETE /milk-out/$saleId")
                        if (!isValidSaleId(saleId)) {
                            logger.warn("Invalid saleId format: $saleId")
                            return@delete call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Invalid sale ID format")
                            )
                        }
                        val success = milkOutEntryRepository.deleteMilkOutEntry(saleId)
                        if (success) {
                            call.respond(HttpStatusCode.NoContent)
                        } else {
                            logger.info("MilkOutEntry not found: $saleId")
                            call.respond(HttpStatusCode.NotFound, mapOf("error" to "MilkOutEntry not found"))
                        }
                    } catch (e: Exception) {
                        val saleId = call.parameters["id"] ?: "unknown"
                        logger.error("Error in DELETE /milk-out/$saleId: ${e.message}", e)
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Internal server error")
                        )
                    }
                }
            }

            route("/milk-spoilt") {
                get {
                    try {
                        logger.debug("GET /milk-spoilt")
                        val entries = milkSpoiltEntryRepository.getAllMilkSpoiltEntries()
                        call.respond(entries)
                    } catch (e: Exception) {
                        val spoiltId = call.parameters["id"] ?: "unknown"
                        logger.error("Error in GET /milk-spoilt/$spoiltId: ${e.message}", e)
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Internal server error")
                        )
                    }
                }

                get("/{id}") {
                    try {
                        val spoiltId = call.parameters["id"] ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Missing spoilt entry ID")
                        )
                        logger.debug("GET /milk-spoilt/$spoiltId")
                        if (!isValidMilkSpoiltEntryId(spoiltId)) {
                            logger.warn("Invalid spoiltId format: $spoiltId")
                            return@get call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Invalid spoilt entry ID format")
                            )
                        }
                        val entry = milkSpoiltEntryRepository.getMilkSpoiltEntryById(spoiltId)
                        if (entry == null) {
                            logger.info("MilkSpoiltEntry not found: $spoiltId")
                            call.respond(HttpStatusCode.NotFound, mapOf("error" to "MilkSpoiltEntry not found"))
                        } else {
                            call.respond(entry)
                        }
                    } catch (e: Exception) {
                        val spoiltId = call.parameters["id"] ?: "unknown"
                        logger.error("Error in GET /milk-spoilt/$spoiltId: ${e.message}", e)
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Internal server error")
                        )
                    }
                }

                post {
                    try {
                        val request = call.receive<MilkSpoiltEntryRequest>()
                        logger.debug("POST /milk-spoilt: $request")

                        // Convert request to domain object
                        val entry = try {
                            MilkSpoiltEntry(
                                spoiltId = DatabaseConfig.generateNextSpoiltId(),
                                date = LocalDate.parse(request.date),
                                amountSpoilt = request.amountSpoilt,
                                lossAmount = request.lossAmount,
                                cause = request.cause
                            )
                        } catch (e: Exception) {
                            logger.warn("Invalid request data: ${e.message}")
                            return@post call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Invalid request data: ${e.message}")
                            )
                        }

                        if (entry.amountSpoilt <= 0) {
                            logger.warn("Invalid amountSpoilt: ${entry.amountSpoilt}")
                            return@post call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Amount spoilt must be greater than zero")
                            )
                        }

                        if (entry.lossAmount < 0) {
                            logger.warn("Invalid lossAmount: ${entry.lossAmount}")
                            return@post call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Loss amount cannot be negative")
                            )
                        }

                        val newEntry = milkSpoiltEntryRepository.addMilkSpoiltEntry(entry)
                        call.respond(HttpStatusCode.Created, newEntry)
                    } catch (e: Exception) {
                        logger.error("Error in POST /milk-spoilt: ${e.message}", e)
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Internal server error")
                        )
                    }
                }

                delete("/{id}") {
                    try {
                        val spoiltId = call.parameters["id"] ?: return@delete call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Missing spoilt entry ID")
                        )
                        logger.debug("DELETE /milk-spoilt/$spoiltId")
                        if (!isValidMilkSpoiltEntryId(spoiltId)) {
                            logger.warn("Invalid spoiltId format: $spoiltId")
                            return@delete call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Invalid spoilt entry ID format")
                            )
                        }
                        val success = milkSpoiltEntryRepository.deleteMilkSpoiltEntry(spoiltId)
                        if (success) {
                            call.respond(HttpStatusCode.NoContent)
                        } else {
                            logger.info("MilkSpoiltEntry not found: $spoiltId")
                            call.respond(HttpStatusCode.NotFound, mapOf("error" to "MilkSpoiltEntry not found"))
                        }
                    } catch (e: Exception) {
                        val spoiltId = call.parameters["id"] ?: "unknown"
                        logger.error("Error in DELETE /milk-spoilt/$spoiltId: ${e.message}", e)
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Internal server error")
                        )
                    }
                }
            }

            get("/stock-summary") {
                try {
                    val dateParam = call.parameters["date"] ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Missing date parameter (format: YYYY-MM-DD)")
                    )
                    logger.debug("GET /stock-summary?date=$dateParam")
                    val currentDate = try {
                        LocalDate.parse(dateParam)
                    } catch (e: Exception) {
                        logger.warn("Invalid date format: $dateParam")
                        return@get call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid date format, use YYYY-MM-DD")
                        )
                    }
                    val summary = stockSummaryRepository.getStockSummary(currentDate)
                    call.respond(summary)
                } catch (e: Exception) {
                    logger.error("Error in GET /stock-summary: ${e.message}", e)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Internal server error")
                    )
                }
            }

            get("/earnings-summary") {
                try {
                    val dateParam = call.parameters["date"] ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Missing date parameter (format: YYYY-MM-DD)")
                    )
                    logger.debug("GET /earnings-summary?date=$dateParam")
                    val currentDate = try {
                        LocalDate.parse(dateParam)
                    } catch (e: Exception) {
                        logger.warn("Invalid date format: $dateParam")
                        return@get call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid date format, use YYYY-MM-DD")
                        )
                    }
                    val summary = earningsSummaryRepository.getEarningsSummary(currentDate)
                    call.respond(summary)
                } catch (e: Exception) {
                    logger.error("Error in GET /earnings-summary: ${e.message}", e)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Internal server error")
                    )
                }
            }

            get("/cow-summary") {
                try {
                    logger.debug("GET /cow-summary")
                    val summary = cowSummaryRepository.getCowSummary()
                    call.respond(summary)
                } catch (e: Exception) {
                    logger.error("Error in GET /cows-summary: ${e.message}", e)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Internal server error")
                    )
                }
            }
        }
    }
}


private fun isValidCowId(id: String): Boolean {
    return id.matches(Regex("^CW\\d{2,3}\$"))
}

private fun isValidSaleId(id: String): Boolean {
    return id.matches(Regex("^SL\\d{2,3}\$"))
}

private fun isValidCustomerId(id: String): Boolean {
    return id.matches(Regex("^CST\\d{3,4}\$"))
}

private fun isValidMilkSpoiltEntryId(id: String): Boolean {
    return id.matches(Regex("^SPL\\d{3,4}\$"))
}

private fun isValidOwnerId(id: String): Boolean {
    return id.matches(Regex("^OON\\d{1,3}\$"))
}
