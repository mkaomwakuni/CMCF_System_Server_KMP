package cnc.coop.milkcreamies.domain.models

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Represents a member/owner of cows
 */
@Serializable
data class Member(
    val memberId: String,
    val name: String,
    val isActive: Boolean = true,
    val archiveDate: LocalDate? = null,
    val archiveReason: String? = null
)

/**
 * Enhanced member with cow information and milk production stats
 */
@Serializable
data class MemberWithStats(
    val member: Member,
    val cows: List<CowWithStats>,
    val averageDailyMilkProduction: Double
)

/**
 * Member summary with active/archived filtering
 */
@Serializable
data class MemberSummary(
    val totalActiveMembers: Int,
    val totalArchivedMembers: Int,
    val membersWithActiveCows: Int
)
