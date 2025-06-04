package cnc.coop.milkcreamies.repositoryImpl

import cnc.coop.milkcreamies.data.DataAccess
import cnc.coop.milkcreamies.data.DatabaseConfig
import cnc.coop.milkcreamies.domain.models.ArchiveCowRequest
import cnc.coop.milkcreamies.domain.models.Cow
import cnc.coop.milkcreamies.domain.models.CowWithStats
import cnc.coop.milkcreamies.domain.repository.CowRepository

class CowRepositoryImpl : CowRepository {
    override suspend fun getCowById(cowId: String): Cow? {
        return DataAccess.getCowById(cowId)
    }

    override suspend fun getAllCows(): List<Cow> {
        return DataAccess.getAllCows()
    }

    override suspend fun getActiveCows(): List<Cow> {
        return DataAccess.getActiveCows()
    }

    override suspend fun getArchivedCows(): List<Cow> {
        return DataAccess.getArchivedCows()
    }

    override suspend fun addCow(cow: Cow): Cow {
        val cowId = DatabaseConfig.generateNextCowId()
        val newCow = cow.copy(cowId = cowId)
        DataAccess.addCow(newCow)
        return newCow
    }

    override suspend fun updateCow(cow: Cow): Boolean {
        return DataAccess.updateCow(cow)
    }

    override suspend fun deleteCow(cowId: String): Boolean {
        return DataAccess.deleteCow(cowId)
    }

    override suspend fun archiveCow(request: ArchiveCowRequest): Boolean {
        return DataAccess.archiveCow(request)
    }

    override suspend fun getCowsWithStats(): List<CowWithStats> {
        return DataAccess.getCowsWithStats()
    }

    override suspend fun getCowsByMember(
        memberId: String,
        includeArchived: Boolean
    ): List<CowWithStats> {
        return DataAccess.getCowsByMember(memberId, includeArchived)
    }
}






