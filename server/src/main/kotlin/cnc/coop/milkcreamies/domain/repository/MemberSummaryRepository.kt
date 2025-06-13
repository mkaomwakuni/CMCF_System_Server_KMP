package cnc.coop.milkcreamies.domain.repository

import cnc.coop.milkcreamies.domain.models.MemberSummary

interface MemberSummaryRepository {
    suspend fun getMemberSummary(): MemberSummary
}