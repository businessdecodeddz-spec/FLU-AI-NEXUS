package com.example

import com.example.core.adaptive.ExecutionProfile
import com.example.core.adaptive.NexusCompatibilityTestEngine
import org.junit.Assert.*
import org.junit.Test

class AdaptiveEngineUnitTest {

    @Test
    fun testCompatibilityMatrixEngineRun() {
        val testEngine = NexusCompatibilityTestEngine()
        val report = testEngine.runFullCompatibilityMatrix()

        assertNotNull(report)
        assertTrue(report.totalTests > 0)
        assertEquals(report.totalTests, report.passedTests)
        assertEquals(0, report.failedTests)
        assertEquals(100f, report.coveragePercentage, 0.01f)
        assertTrue(report.testCases.any { it.targetOsVersion == "Android 12" })
        assertTrue(report.testCases.any { it.targetOsVersion == "Android 16" })
        assertTrue(report.testCases.any { it.architecture == "arm64-v8a" })
    }

    @Test
    fun testExecutionProfilesEnumValues() {
        val profiles = ExecutionProfile.values()
        assertEquals(3, profiles.size)
        assertTrue(profiles.contains(ExecutionProfile.NEXUS_LITE))
        assertTrue(profiles.contains(ExecutionProfile.NEXUS_STANDARD))
        assertTrue(profiles.contains(ExecutionProfile.NEXUS_ADVANCED))
    }
}
