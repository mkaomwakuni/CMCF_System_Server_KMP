package cnc.coop.milkcreamies.data

import cnc.coop.milkcreamies.domain.models.*
import cnc.coop.milkcreamies.validators.CowHealthValidationService
import cnc.coop.milkcreamies.validators.CowHealthValidationService.ValidationResult
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory
import org.jetbrains.exposed.sql.ResultRow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and

// Data access object for working with the database
object DataAccess {
    // Helper function to map ResultRow to Cow
    private fun mapToCow(resultRow: ResultRow): Cow {
        return Cow(
            cowId = resultRow[Cows.id],
            entryDate = resultRow[Cows.entryDate],
            ownerId = resultRow[Cows.ownerId],
            name = resultRow[Cows.name],
            breed = resultRow[Cows.breed],
            age = resultRow[Cows.age],
            weight = resultRow[Cows.weight],
            status = CowStatus(
                healthStatus = resultRow[Cows.healthStatus],
                actionStatus = resultRow[Cows.actionStatus],
                dewormingDue = resultRow[Cows.dewormingDue],
                dewormingLast = resultRow[Cows.dewormingLast],
                calvingDate = resultRow[Cows.calvingDate],
                vaccinationDue = resultRow[Cows.vaccinationDue],
                vaccinationLast = resultRow[Cows.vaccinationLast],
                antibioticTreatment = resultRow[Cows.antibioticTreatment]
            ),
            isActive = resultRow[Cows.isActive],
            archiveReason = resultRow[Cows.archiveReason],
            archiveDate = resultRow[Cows.archiveDate],
            note = resultRow[Cows.note]
        )
    }

    // Helper function to map ResultRow to Member
    private fun mapToMember(resultRow: ResultRow): Member {
        return Member(
            memberId = resultRow[Members.id],
            name = resultRow[Members.name],
            isActive = resultRow[Members.isActive],
            archiveDate = resultRow[Members.archiveDate],
            archiveReason = resultRow[Members.archiveReason]
        )
    }

    // Cow operations
    fun getCowById(cowId: String): Cow? {
        return transaction {
            Cows.select { Cows.id eq cowId }
                .map { mapToCow(it) }
                .singleOrNull()
        }
    }

    fun getAllCows(): List<Cow> {
        return transaction {
            Cows.selectAll().map { mapToCow(it) }
        }
    }

    fun getActiveCows(): List<Cow> {
        return transaction {
            Cows.select { Cows.isActive eq true }
                .map { mapToCow(it) }
        }
    }

    fun getArchivedCows(): List<Cow> {
        return transaction {
            Cows.select { Cows.isActive eq false }
                .map { mapToCow(it) }
        }
    }

    fun addCow(cow: Cow) {
        transaction {
            Cows.insert {
                it[id] = cow.cowId ?: throw IllegalArgumentException("Cow ID cannot be null")
                it[entryDate] = cow.entryDate
                it[ownerId] = cow.ownerId
                it[name] = cow.name
                it[breed] = cow.breed
                it[age] = cow.age
                it[weight] = cow.weight
                it[healthStatus] = cow.status.healthStatus
                it[actionStatus] = cow.status.actionStatus
                it[dewormingDue] = cow.status.dewormingDue
                it[dewormingLast] = cow.status.dewormingLast
                it[calvingDate] = cow.status.calvingDate
                it[vaccinationDue] = cow.status.vaccinationDue
                it[vaccinationLast] = cow.status.vaccinationLast
                it[antibioticTreatment] = cow.status.antibioticTreatment
                it[isActive] = cow.isActive
                it[archiveReason] = cow.archiveReason
                it[archiveDate] = cow.archiveDate
                it[note] = cow.note
            }
        }
    }

    fun updateCow(cow: Cow): Boolean {
        return transaction {
            val rowsUpdated = Cows.update({
                Cows.id eq (cow.cowId ?: throw IllegalArgumentException("Cow ID cannot be null"))
            }) {
                it[entryDate] = cow.entryDate
                it[ownerId] = cow.ownerId
                it[name] = cow.name
                it[breed] = cow.breed
                it[age] = cow.age
                it[weight] = cow.weight
                it[healthStatus] = cow.status.healthStatus
                it[actionStatus] = cow.status.actionStatus
                it[dewormingDue] = cow.status.dewormingDue
                it[dewormingLast] = cow.status.dewormingLast
                it[calvingDate] = cow.status.calvingDate
                it[vaccinationDue] = cow.status.vaccinationDue
                it[vaccinationLast] = cow.status.vaccinationLast
                it[antibioticTreatment] = cow.status.antibioticTreatment
                it[isActive] = cow.isActive
                it[archiveReason] = cow.archiveReason
                it[archiveDate] = cow.archiveDate
                it[note] = cow.note
            }
            rowsUpdated > 0
        }
    }

    fun deleteCow(cowId: String): Boolean {
        return transaction {
            val rowsDeleted = Cows.deleteWhere { Cows.id eq cowId }
            rowsDeleted > 0
        }
    }

    // Member operations
    fun getMemberById(memberId: String): Member? {
        return transaction {
            Members.select { Members.id eq memberId }
                .map { mapToMember(it) }
                .singleOrNull()
        }
    }

    fun getAllMembers(): List<Member> {
        return transaction {
            Members.selectAll().map { mapToMember(it) }
        }
    }

    fun addMember(member: Member) {
        transaction {
            Members.insert {
                it[id] = member.memberId
                it[name] = member.name
                it[isActive] = member.isActive
                it[archiveDate] = member.archiveDate
                it[archiveReason] = member.archiveReason
            }
        }
    }

    fun getActiveMembers(): List<Member> {
        return transaction {
            Members.select { Members.isActive eq true }
                .map { mapToMember(it) }
        }
    }

    fun getArchivedMembers(): List<Member> {
        return transaction {
            Members.select { Members.isActive eq false }
                .map { mapToMember(it) }
        }
    }

    fun updateMember(member: Member): Boolean {
        return transaction {
            val rowsUpdated = Members.update({
                Members.id eq member.memberId
            }) {
                it[name] = member.name
                it[isActive] = member.isActive
                it[archiveDate] = member.archiveDate
                it[archiveReason] = member.archiveReason
            }
            rowsUpdated > 0
        }
    }

    // Customer operations
    fun getCustomerById(customerId: String): Customer? {
        return transaction {
            Customers.select { Customers.id eq customerId }
                .map { resultRow ->
                    Customer(
                        customerId = resultRow[Customers.id],
                        name = resultRow[Customers.name]
                    )
                }.singleOrNull()
        }
    }

    fun getAllCustomers(): List<Customer> {
        return transaction {
            Customers.selectAll().map { resultRow ->
                Customer(
                    customerId = resultRow[Customers.id],
                    name = resultRow[Customers.name]
                )
            }
        }
    }

    fun addCustomer(customer: Customer) {
        transaction {
            Customers.insert {
                it[id] = customer.customerId
                it[name] = customer.name
            }
        }
    }

    // MilkInEntry operations
    fun getMilkInEntryById(entryId: String?): MilkInEntry? {
        return transaction {
            if (entryId == null) return@transaction null
            MilkInEntries.select { MilkInEntries.id eq entryId }
                .map { resultRow ->
                    MilkInEntry(
                        entryId = resultRow[MilkInEntries.id],
                        cowId = resultRow[MilkInEntries.cowId],
                        ownerId = resultRow[MilkInEntries.ownerId],
                        liters = resultRow[MilkInEntries.liters],
                        date = resultRow[MilkInEntries.date],
                        milkingType = resultRow[MilkInEntries.milkingType]
                    )
                }.singleOrNull()
        }
    }

    fun getAllMilkInEntries(): List<MilkInEntry> {
        return transaction {
            MilkInEntries.selectAll().map { resultRow ->
                MilkInEntry(
                    entryId = resultRow[MilkInEntries.id],
                    cowId = resultRow[MilkInEntries.cowId],
                    ownerId = resultRow[MilkInEntries.ownerId],
                    liters = resultRow[MilkInEntries.liters],
                    date = resultRow[MilkInEntries.date],
                    milkingType = resultRow[MilkInEntries.milkingType]
                )
            }
        }
    }

    fun addMilkInEntry(entry: MilkInEntry) {
        val logger = LoggerFactory.getLogger("DataAccess")
        logger.info("Adding MilkInEntry: $entry")

        if (entry.entryId == null) {
            logger.error("MilkInEntry entryId is null! This should not happen at this stage.")
            throw IllegalArgumentException("Entry ID cannot be null")
        }

        transaction {
            try {
                MilkInEntries.insert {
                    it[id] = entry.entryId
                    it[cowId] =
                        entry.cowId ?: throw IllegalArgumentException("Cow ID cannot be null")
                    it[ownerId] = entry.ownerId
                    it[liters] = entry.liters
                    it[date] = entry.date
                    it[milkingType] = entry.milkingType
                }
                logger.info("Successfully added MilkInEntry with id: ${entry.entryId}")
            } catch (e: Exception) {
                logger.error("Error adding MilkInEntry: ${e.message}", e)
                throw e
            }
        }
    }

    fun deleteMilkInEntry(entryId: String): Boolean {
        return transaction {
            val rowsDeleted = MilkInEntries.deleteWhere { MilkInEntries.id eq entryId }
            rowsDeleted > 0
        }
    }

    // MilkOutEntry operations
    fun getMilkOutEntryById(saleId: String): MilkOutEntry? {
        return transaction {
            MilkOutEntries.select { MilkOutEntries.id eq saleId }
                .map { resultRow ->
                    MilkOutEntry(
                        saleId = resultRow[MilkOutEntries.id],
                        customerId = resultRow[MilkOutEntries.customerId],
                        customerName = resultRow[MilkOutEntries.customerName],
                        date = resultRow[MilkOutEntries.date],
                        quantitySold = resultRow[MilkOutEntries.quantitySold],
                        pricePerLiter = resultRow[MilkOutEntries.pricePerLiter],
                        paymentMode = resultRow[MilkOutEntries.paymentMode]
                    )
                }.singleOrNull()
        }
    }

    fun getAllMilkOutEntries(): List<MilkOutEntry> {
        return transaction {
            MilkOutEntries.selectAll().map { resultRow ->
                MilkOutEntry(
                    saleId = resultRow[MilkOutEntries.id],
                    customerId = resultRow[MilkOutEntries.customerId],
                    customerName = resultRow[MilkOutEntries.customerName],
                    date = resultRow[MilkOutEntries.date],
                    quantitySold = resultRow[MilkOutEntries.quantitySold],
                    pricePerLiter = resultRow[MilkOutEntries.pricePerLiter],
                    paymentMode = resultRow[MilkOutEntries.paymentMode]
                )
            }
        }
    }

    fun addMilkOutEntry(entry: MilkOutEntry) {
        transaction {
            MilkOutEntries.insert {
                it[id] = entry.saleId
                it[customerId] = entry.customerId
                it[customerName] = entry.customerName
                it[date] = entry.date
                it[quantitySold] = entry.quantitySold
                it[pricePerLiter] = entry.pricePerLiter
                it[paymentMode] = entry.paymentMode
            }
        }
    }

    fun deleteMilkOutEntry(saleId: String): Boolean {
        return transaction {
            val rowsDeleted = MilkOutEntries.deleteWhere { MilkOutEntries.id eq saleId }
            rowsDeleted > 0
        }
    }

    // MilkSpoiltEntry operations
    fun getMilkSpoiltEntryById(spoiltId: String): MilkSpoiltEntry? {
        return transaction {
            MilkSpoiltEntries.select { MilkSpoiltEntries.id eq spoiltId }
                .map { resultRow ->
                    MilkSpoiltEntry(
                        spoiltId = resultRow[MilkSpoiltEntries.id],
                        date = resultRow[MilkSpoiltEntries.date],
                        amountSpoilt = resultRow[MilkSpoiltEntries.amountSpoilt],
                        lossAmount = resultRow[MilkSpoiltEntries.lossAmount],
                        cause = resultRow[MilkSpoiltEntries.cause]
                    )
                }.singleOrNull()
        }
    }

    fun getAllMilkSpoiltEntries(): List<MilkSpoiltEntry> {
        return transaction {
            MilkSpoiltEntries.selectAll().map { resultRow ->
                MilkSpoiltEntry(
                    spoiltId = resultRow[MilkSpoiltEntries.id],
                    date = resultRow[MilkSpoiltEntries.date],
                    amountSpoilt = resultRow[MilkSpoiltEntries.amountSpoilt],
                    lossAmount = resultRow[MilkSpoiltEntries.lossAmount],
                    cause = resultRow[MilkSpoiltEntries.cause]
                )
            }
        }
    }

    fun addMilkSpoiltEntry(entry: MilkSpoiltEntry) {
        transaction {
            MilkSpoiltEntries.insert {
                it[id] = entry.spoiltId
                it[date] = entry.date
                it[amountSpoilt] = entry.amountSpoilt
                it[lossAmount] = entry.lossAmount
                it[cause] = entry.cause
            }
        }
    }

    fun deleteMilkSpoiltEntry(spoiltId: String): Boolean {
        return transaction {
            val rowsDeleted = MilkSpoiltEntries.deleteWhere { MilkSpoiltEntries.id eq spoiltId }
            rowsDeleted > 0
        }
    }

    fun archiveCow(request: ArchiveCowRequest): Boolean {
        return transaction {
            val rowsUpdated = Cows.update({
                Cows.id eq request.cowId
            }) {
                it[isActive] = false
                it[archiveReason] = request.reason
                it[archiveDate] = request.archiveDate
            }
            rowsUpdated > 0
        }
    }

    fun archiveMember(request: ArchiveMemberRequest): Boolean {
        return transaction {
            // First archive the member
            val memberUpdated = Members.update({
                Members.id eq request.memberId
            }) {
                it[isActive] = false
                it[archiveReason] = request.reason
                it[archiveDate] = request.archiveDate
            }

            // Then archive all cows owned by this member
            if (memberUpdated > 0) {
                Cows.update({
                    Cows.ownerId eq request.memberId
                }) {
                    it[isActive] = false
                    it[archiveReason] = "Owner archived: ${request.reason}"
                    it[archiveDate] = request.archiveDate
                }
            }

            memberUpdated > 0
        }
    }

    fun getCowsWithStats(): List<CowWithStats> {
        return transaction {
            val cows = Cows.select { Cows.isActive eq true }.map { mapToCow(it) }
            cows.map { cow ->
                val averageMilkProduction = calculateCowAverageProduction(cow.cowId!!)
                val lastMilkingDate = getLastMilkingDate(cow.cowId)
                CowWithStats(
                    cow = cow,
                    averageDailyMilkProduction = averageMilkProduction,
                    lastMilkingDate = lastMilkingDate
                )
            }
        }
    }

    fun getCowsByMember(memberId: String, includeArchived: Boolean): List<CowWithStats> {
        return transaction {
            val cows = if (includeArchived) {
                Cows.select { Cows.ownerId eq memberId }.map { mapToCow(it) }
            } else {
                Cows.select {
                    Cows.ownerId eq memberId and (Cows.isActive eq true)
                }.map { mapToCow(it) }
            }
            cows.map { cow ->
                val averageMilkProduction = calculateCowAverageProduction(cow.cowId!!)
                val lastMilkingDate = getLastMilkingDate(cow.cowId)
                CowWithStats(
                    cow = cow,
                    averageDailyMilkProduction = averageMilkProduction,
                    lastMilkingDate = lastMilkingDate
                )
            }
        }
    }

    fun getMembersWithStats(): List<MemberWithStats> {
        return transaction {
            val activeMembers = Members.select { Members.isActive eq true }.map { mapToMember(it) }
            activeMembers.map { member ->
                val cowsWithStats = getCowsByMember(member.memberId, false)
                val averageDailyProduction = cowsWithStats.sumOf { it.averageDailyMilkProduction }
                MemberWithStats(
                    member = member,
                    cows = cowsWithStats,
                    averageDailyMilkProduction = averageDailyProduction
                )
            }
        }
    }

    private fun calculateCowAverageProduction(cowId: String): Double {
        return transaction {
            // Get milk entries for the last 30 days
            val thirtyDaysAgo = LocalDate(2025, 6, 7).minus(DatePeriod(days = 30))
            val currentDate = LocalDate(2025, 6, 7)

            val entries = MilkInEntries.select {
                MilkInEntries.cowId eq cowId and
                        (MilkInEntries.date greaterEq thirtyDaysAgo) and
                        (MilkInEntries.date lessEq currentDate)
            }.map {
                Pair(it[MilkInEntries.date], it[MilkInEntries.liters])
            }

            if (entries.isEmpty()) return@transaction 0.0

            // Group by date and sum morning + evening production
            val dailyTotals = entries.groupBy { it.first }
                .mapValues { (_, milkEntries) -> milkEntries.sumOf { it.second } }

            // Calculate average
            dailyTotals.values.average()
        }
    }

    private fun getLastMilkingDate(cowId: String): LocalDate? {
        return transaction {
            MilkInEntries.select { MilkInEntries.cowId eq cowId }
                .orderBy(MilkInEntries.date to SortOrder.DESC)
                .limit(1)
                .map { it[MilkInEntries.date] }
                .firstOrNull()
        }
    }
    
    /**
     * Validates if a cow is eligible for milk collection based on its health status and treatment history
     * 
     * @param cowId The ID of the cow to validate
     * @param requestDate The date for which milk collection is requested
     * @return A validation result with eligibility status and error information if blocked
     */
    fun validateCowForMilkCollection(cowId: String, requestDate: LocalDate): ValidationResult {
        return transaction {
            val cow = Cows.select { Cows.id eq cowId }
                .singleOrNull()
                ?.let { mapToCow(it) }
                ?: return@transaction ValidationResult(
                    isValid = false, 
                    errorMessage = "Cow not found"
                )
            
            if (!cow.isActive) {
                return@transaction ValidationResult(
                    isValid = false,
                    errorMessage = "Cow is archived and cannot produce milk"
                )
            }
            
            CowHealthValidationService().validateMilkCollection(cow, requestDate)
        }
    }
}
