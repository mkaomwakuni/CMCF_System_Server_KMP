package cnc.coop.milkcreamies.repositoryImpl

import cnc.coop.milkcreamies.data.DataAccess
import cnc.coop.milkcreamies.domain.models.ArchiveMemberRequest
import cnc.coop.milkcreamies.domain.models.Member
import cnc.coop.milkcreamies.domain.models.MemberWithStats
import cnc.coop.milkcreamies.domain.repository.MemberRepository

class MemberRepositoryImpl : MemberRepository {
    override suspend fun getMemberById(memberId: String): Member? {
        return DataAccess.getMemberById(memberId)
    }

    override suspend fun getAllMembers(): List<Member> {
        return DataAccess.getAllMembers()
    }

    override suspend fun getActiveMembers(): List<Member> {
        return DataAccess.getActiveMembers()
    }

    override suspend fun getArchivedMembers(): List<Member> {
        return DataAccess.getArchivedMembers()
    }

    override suspend fun addMember(member: Member): Member {
        DataAccess.addMember(member)
        return member
    }

    override suspend fun updateMember(member: Member): Boolean {
        return DataAccess.updateMember(member)
    }

    override suspend fun archiveMember(request: ArchiveMemberRequest): Boolean {
        return DataAccess.archiveMember(request)
    }

    override suspend fun getMembersWithStats(): List<MemberWithStats> {
        return DataAccess.getMembersWithStats()
    }
}
