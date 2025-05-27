package cnc.coop.milkcreamies.domain.repository

import cnc.coop.milkcreamies.domain.models.ArchiveMemberRequest
import cnc.coop.milkcreamies.domain.models.Member
import cnc.coop.milkcreamies.domain.models.MemberWithStats

interface MemberRepository {
    suspend fun getMemberById(memberId: String): Member?
    suspend fun getAllMembers(): List<Member>
    suspend fun getActiveMembers(): List<Member>
    suspend fun getArchivedMembers(): List<Member>
    suspend fun addMember(member: Member): Member
    suspend fun updateMember(member: Member): Boolean
    suspend fun archiveMember(request: ArchiveMemberRequest): Boolean
    suspend fun getMembersWithStats(): List<MemberWithStats>
}