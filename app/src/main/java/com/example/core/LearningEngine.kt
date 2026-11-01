package com.example.core

import com.example.data.model.ActionRecordEntity
import com.example.data.model.ActionState

/**
 * 9. NEXUS LEARNING ENGINE
 * Continuous optimization feedback loop improving decision weights and recording strategy metrics.
 */
class LearningEngine {

    data class StrategyMetric(
        val totalActions: Int,
        val successRate: Float,
        val averageExecutionTimeMs: Long,
        val optimizationScore: Int // 0 to 100
    )

    fun learnFromAction(history: List<ActionRecordEntity>): StrategyMetric {
        if (history.isEmpty()) {
            return StrategyMetric(0, 100f, 0L, 95)
        }

        val verifiedCount = history.count { it.state == ActionState.VERIFIED }
        val successRate = (verifiedCount.toFloat() / history.size.toFloat()) * 100f
        val avgTime = history.map { it.executionTimeMs }.average().toLong()

        val optScore = (successRate * 0.7f + (1000 - avgTime.coerceAtMost(1000)) / 10f * 0.3f).toInt().coerceIn(50, 100)

        return StrategyMetric(
            totalActions = history.size,
            successRate = successRate,
            averageExecutionTimeMs = avgTime,
            optimizationScore = optScore
        )
    }
}
