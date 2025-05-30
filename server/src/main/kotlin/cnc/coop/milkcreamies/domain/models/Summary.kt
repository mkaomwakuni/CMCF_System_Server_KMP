package cnc.coop.milkcreamies.domain.models

import kotlinx.serialization.Serializable

/**
 * Enhanced stock summary with real-time inventory
 */
@Serializable
data class StockSummary(
    val currentStock: Double,
    val dailyProduce: Double,
    val dailyTotalLitersSold: Double,
    val weeklySold: Double,
    val weeklySpoilt: Double,
    val monthlySold: Double
)

/**
 * Enhanced earnings summary
 */
@Serializable
data class EarningsSummary(
    val todayEarnings: Double,
    val weeklyEarnings: Double,
    val monthlyEarnings: Double
)