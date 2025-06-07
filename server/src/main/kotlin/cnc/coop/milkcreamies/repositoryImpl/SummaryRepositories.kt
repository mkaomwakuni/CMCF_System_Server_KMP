package cnc.coop.milkcreamies.repositoryImpl

import cnc.coop.milkcreamies.data.Cows
import cnc.coop.milkcreamies.data.MilkInEntries
import cnc.coop.milkcreamies.data.MilkOutEntries
import cnc.coop.milkcreamies.data.MilkSpoiltEntries
import cnc.coop.milkcreamies.data.Members
import cnc.coop.milkcreamies.data.MilkInventoryTable
import cnc.coop.milkcreamies.data.DatabaseConfig
import cnc.coop.milkcreamies.domain.models.*
import cnc.coop.milkcreamies.domain.repository.CowSummaryRepository
import cnc.coop.milkcreamies.domain.repository.EarningsSummaryRepository
import cnc.coop.milkcreamies.domain.repository.MemberSummaryRepository
import cnc.coop.milkcreamies.domain.repository.MilkInventoryRepository
import cnc.coop.milkcreamies.domain.repository.StockSummaryRepository
import kotlinx.datetime.LocalDate
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import kotlinx.datetime.minus
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class StockSummaryRepositoryImpl : StockSummaryRepository {
    override suspend fun getStockSummary(currentDate: LocalDate): StockSummary {
        return transaction {
            val dayOfWeek = currentDate.dayOfWeek.value
            val startOfWeek = currentDate.minus(DatePeriod(days = dayOfWeek - 1))
            val endOfWeek = startOfWeek.plus(DatePeriod(days = 6))
            val startOfMonth = LocalDate(currentDate.year, currentDate.month, 1)
            val endOfMonth = if (currentDate.month.value == 12) {
                LocalDate(currentDate.year + 1, 1, 1).minus(DatePeriod(days = 1))
            } else {
                LocalDate(
                    currentDate.year,
                    currentDate.month.value + 1,
                    1
                ).minus(DatePeriod(days = 1))
            }

            val currentStock = DatabaseConfig.getCurrentStock()

            val dailyProduce = MilkInEntries
                .slice(MilkInEntries.liters.sum())
                .select { MilkInEntries.date eq currentDate }
                .map { it[MilkInEntries.liters.sum()] ?: 0.0 }
                .firstOrNull() ?: 0.0

            val dailyTotalLitersSold = MilkOutEntries
                .slice(MilkOutEntries.quantitySold.sum())
                .select { MilkOutEntries.date eq currentDate }
                .map { it[MilkOutEntries.quantitySold.sum()] ?: 0.0 }
                .firstOrNull() ?: 0.0

            val weeklySold = MilkOutEntries
                .slice(MilkOutEntries.quantitySold.sum())
                .select {
                    (MilkOutEntries.date greaterEq startOfWeek) and
                            (MilkOutEntries.date lessEq endOfWeek)
                }
                .map { it[MilkOutEntries.quantitySold.sum()] ?: 0.0 }
                .firstOrNull() ?: 0.0

            val weeklySpoilt = MilkSpoiltEntries
                .slice(MilkSpoiltEntries.amountSpoilt.sum())
                .select {
                    (MilkSpoiltEntries.date greaterEq startOfWeek) and
                            (MilkSpoiltEntries.date lessEq endOfWeek)
                }
                .map { it[MilkSpoiltEntries.amountSpoilt.sum()] ?: 0.0 }
                .firstOrNull() ?: 0.0

            val monthlySold = MilkOutEntries
                .slice(MilkOutEntries.quantitySold.sum())
                .select {
                    (MilkOutEntries.date greaterEq startOfMonth) and
                            (MilkOutEntries.date lessEq endOfMonth)
                }
                .map { it[MilkOutEntries.quantitySold.sum()] ?: 0.0 }
                .firstOrNull() ?: 0.0

            StockSummary(
                currentStock = currentStock,
                dailyProduce = dailyProduce,
                dailyTotalLitersSold = dailyTotalLitersSold,
                weeklySold = weeklySold,
                weeklySpoilt = weeklySpoilt,
                monthlySold = monthlySold
            )
        }
    }
}

class EarningsSummaryRepositoryImpl : EarningsSummaryRepository {
    override suspend fun getEarningsSummary(currentDate: LocalDate): EarningsSummary {
        return transaction {
            val dayOfWeek = currentDate.dayOfWeek.value
            val startOfWeek = currentDate.minus(DatePeriod(days = dayOfWeek - 1))
            val endOfWeek = startOfWeek.plus(DatePeriod(days = 6))
            val startOfMonth = LocalDate(currentDate.year, currentDate.month, 1)
            val endOfMonth = if (currentDate.month.value == 12) {
                LocalDate(currentDate.year + 1, 1, 1).minus(DatePeriod(days = 1))
            } else {
                LocalDate(
                    currentDate.year,
                    currentDate.month.value + 1,
                    1
                ).minus(DatePeriod(days = 1))
            }

            val todayEarnings = MilkOutEntries
                .slice(MilkOutEntries.quantitySold, MilkOutEntries.pricePerLiter)
                .select { MilkOutEntries.date eq currentDate }
                .sumOf { it[MilkOutEntries.quantitySold] * it[MilkOutEntries.pricePerLiter] }

            val weeklyEarnings = MilkOutEntries
                .slice(MilkOutEntries.quantitySold, MilkOutEntries.pricePerLiter)
                .select {
                    (MilkOutEntries.date greaterEq startOfWeek) and
                            (MilkOutEntries.date lessEq endOfWeek)
                }
                .sumOf { it[MilkOutEntries.quantitySold] * it[MilkOutEntries.pricePerLiter] }

            val monthlyEarnings = MilkOutEntries
                .slice(MilkOutEntries.quantitySold, MilkOutEntries.pricePerLiter)
                .select {
                    (MilkOutEntries.date greaterEq startOfMonth) and
                            (MilkOutEntries.date lessEq endOfMonth)
                }
                .sumOf { it[MilkOutEntries.quantitySold] * it[MilkOutEntries.pricePerLiter] }

            EarningsSummary(
                todayEarnings = todayEarnings,
                weeklyEarnings = weeklyEarnings,
                monthlyEarnings = monthlyEarnings
            )
        }
    }
}

class CowSummaryRepositoryImpl : CowSummaryRepository {
    override suspend fun getCowSummary(): CowSummary {
        return transaction {
            val totalActiveCows = Cows
                .select { Cows.isActive eq true }
                .count().toInt()

            val totalArchivedCows = Cows
                .select { Cows.isActive eq false }
                .count().toInt()

            val healthyCows = Cows
                .select {
                    (Cows.isActive eq true) and (Cows.healthStatus eq HealthStatus.HEALTHY)
                }
                .count().toInt()

            val needsAttention = Cows
                .select {
                    (Cows.isActive eq true) and
                            ((Cows.healthStatus eq HealthStatus.NEEDS_ATTENTION) or
                                    (Cows.healthStatus eq HealthStatus.UNDER_TREATMENT))
                }
                .count().toInt()

            CowSummary(
                totalActiveCows = totalActiveCows,
                totalArchivedCows = totalArchivedCows,
                healthyCows = healthyCows,
                needsAttention = needsAttention
            )
        }
    }
}

class MemberSummaryRepositoryImpl : MemberSummaryRepository {
    override suspend fun getMemberSummary(): MemberSummary {
        return transaction {
            val totalActiveMembers = Members
                .select { Members.isActive eq true }
                .count().toInt()

            val totalArchivedMembers = Members
                .select { Members.isActive eq false }
                .count().toInt()

            val membersWithActiveCows = Members.innerJoin(Cows)
                .select {
                    (Members.isActive eq true) and (Cows.isActive eq true)
                }
                .withDistinct()
                .count().toInt()

            MemberSummary(
                totalActiveMembers = totalActiveMembers,
                totalArchivedMembers = totalArchivedMembers,
                membersWithActiveCows = membersWithActiveCows
            )
        }
    }
}

class MilkInventoryRepositoryImpl : MilkInventoryRepository {
    override suspend fun getCurrentInventory(): MilkInventory {
        return transaction {
            val record = MilkInventoryTable.selectAll().singleOrNull()
            if (record != null) {
                MilkInventory(
                    currentStock = record[MilkInventoryTable.currentStock],
                    lastUpdated = record[MilkInventoryTable.lastUpdated]
                )
            } else {
                MilkInventory(currentStock = 0.0, lastUpdated = LocalDate(2025, 6, 7))
            }
        }
    }

    override suspend fun updateInventory(currentStock: Double, date: LocalDate): Boolean {
        return transaction {
            try {
                val existingCount = MilkInventoryTable.selectAll().count()
                if (existingCount > 0) {
                    MilkInventoryTable.update {
                        it[MilkInventoryTable.currentStock] = currentStock
                        it[MilkInventoryTable.lastUpdated] = date
                    }
                } else {
                    MilkInventoryTable.insert {
                        it[MilkInventoryTable.currentStock] = currentStock
                        it[MilkInventoryTable.lastUpdated] = date
                    }
                }
                true
            } catch (e: Exception) {
                false
            }
        }
    }
}
