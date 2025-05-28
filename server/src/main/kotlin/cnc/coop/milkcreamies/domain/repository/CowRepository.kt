package cnc.coop.milkcreamies.domain.repository

import cnc.coop.milkcreamies.domain.models.ArchiveCowRequest
import cnc.coop.milkcreamies.domain.models.Cow
import cnc.coop.milkcreamies.domain.models.CowWithStats

interface CowRepository {
    suspend fun getCowById(cowId: String): Cow?
    suspend fun getAllCows(): List<Cow>
    suspend fun getActiveCows(): List<Cow>
    suspend fun getArchivedCows(): List<Cow>
    suspend fun addCow(cow: Cow): Cow
    suspend fun updateCow(cow: Cow): Boolean
    suspend fun deleteCow(cowId: String): Boolean
    suspend fun archiveCow(request: ArchiveCowRequest): Boolean
    suspend fun getCowsWithStats(): List<CowWithStats>
    suspend fun getCowsByMember(
        memberId: String,
        includeArchived: Boolean = false
    ): List<CowWithStats>
}



















