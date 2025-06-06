package cnc.coop.milkcreamies.domain.models

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Archive request for cows
 */
@Serializable
data class ArchiveCowRequest(
    val cowId: String,
    val reason: String,
    val archiveDate: LocalDate
)

/**
 * Archive request for members
 */
@Serializable
data class ArchiveMemberRequest(
    val memberId: String,
    val reason: String,
    val archiveDate: LocalDate
)