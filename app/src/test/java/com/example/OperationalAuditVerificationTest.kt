package com.example

import com.example.core.ActionRegistry
import com.example.core.NexusStorageGovernor
import com.example.core.UniversalDeveloper
import com.example.core.agentic.VerificationEngineAgent
import com.example.data.jni.NexusJniBridge
import com.example.data.model.ActionResult
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class OperationalAuditVerificationTest {

    @Test
    fun actionRegistry_executesRealOperationsWithObservableArtifacts() = runBlocking {
        val registry = ActionRegistry(context = null)

        // 1. Backup creator must write a real file on disk and return its path & SHA-256
        val backupTool = registry.getTool("backup_creator")
        assertNotNull(backupTool)
        val backupResult = backupTool!!.execute(mapOf("reason" to "audit_test"))
        assertTrue(backupResult.success)
        assertNotNull("Backup creator must return an observable artifact path", backupResult.observableArtifactPath)
        val backupFile = File(backupResult.observableArtifactPath!!)
        assertTrue("Physical backup file must exist on disk", backupFile.exists())
        assertTrue("Physical backup file must be non-empty", backupFile.length() > 0L)
        assertTrue(backupResult.verificationProof.startsWith("REAL-FILE-SHA256:"))

        // 2. Duplicate search must scan real files on disk and produce a real report file
        val dupTool = registry.getTool("duplicate_search")
        val dupResult = dupTool!!.execute(emptyMap())
        assertTrue(dupResult.success)
        assertNotNull(dupResult.observableArtifactPath)
        assertTrue(File(dupResult.observableArtifactPath!!).exists())
        assertFalse("Must not return old hardcoded '14 fichiers en double'", dupResult.dataSummary.contains("14 fichiers en double identifiés (420 Mo"))

        // 3. Storage analysis must measure real partition bytes
        val storageResult = registry.getTool("storage_analysis")!!.execute(emptyMap())
        assertTrue(storageResult.success)
        assertTrue(storageResult.observableMetrics.containsKey("totalBytes"))
        val totalBytes = storageResult.observableMetrics["totalBytes"]?.toLongOrNull() ?: 0L
        assertTrue("Real partition totalBytes must be > 0", totalBytes > 0L)
    }

    @Test
    fun verificationEngineAgent_rejectsMissingArtifactAndAcceptsRealArtifact() {
        val verifier = VerificationEngineAgent()

        // Missing file artifact must fail verification
        val fakeResult = ActionResult(
            toolId = "backup_creator",
            success = true,
            dataSummary = "Fake backup",
            verificationProof = "REAL-FILE-SHA256:123456",
            observableArtifactPath = "/nonexistent/fake/path/backup_999999.json"
        )
        val fakeVerification = verifier.verifyResult(fakeResult)
        assertEquals("VERIFICATION_FAILED_MISSING_ARTIFACT", fakeVerification)

        // Real file artifact must pass verification with SHA-256 signature
        val tempFile = File.createTempFile("nexus_proof_test", ".json")
        tempFile.writeText("""{"verified":true}""")
        val realResult = ActionResult(
            toolId = "backup_creator",
            success = true,
            dataSummary = "Real backup",
            verificationProof = "REAL-FILE-SHA256:ABCDEF",
            observableArtifactPath = tempFile.absolutePath
        )
        val realVerification = verifier.verifyResult(realResult)
        assertTrue(realVerification.startsWith("VERIFIED-SHA256:"))
        tempFile.delete()
    }

    @Test
    fun storageGovernor_enforcesAtomic1000MbQuotaUnderConcurrency() = runBlocking {
        val governor = NexusStorageGovernor(dao = null)

        // Launch 20 concurrent coroutines each trying to write 120 MB to SYSTEM_STORAGE
        val results = (1..20).map { index ->
            async {
                governor.writeSystemData("concurrent_sys_$index.bin", 120.0f, "application/octet-stream")
            }
        }.awaitAll()

        val successCount = results.count { it }
        assertTrue("Some writes should succeed before hitting the 1000 MB cap", successCount in 1..7)
        assertTrue(
            "Atomic Mutex lock must guarantee system storage never exceeds 1000.0 MB",
            governor.quotaStatus.value.systemUsedMb <= 1000.0f
        )

        val isolation = governor.verifyIsolationAndNonExceedance()
        assertTrue(isolation.isZeroContaminationVerified)
        assertTrue(isolation.systemStorageCompliantWith1GbCap)
    }

    @Test
    fun storageGovernor_verifiesPhysicalBackupFileOnRestore() = runBlocking {
        val tempDir = File(System.getProperty("java.io.tmpdir", "."), "nexus_gov_restore_${System.nanoTime()}")
        val governor = NexusStorageGovernor(dao = null, baseDirectory = tempDir)

        // Restoring when no backup exists yet must return false
        val restoreMissing = governor.restoreCloudEnvironmentFromDrive("NON_EXISTENT_BACKUP_ID")
        assertFalse("Restoring a nonexistent backup without a physical file on disk must return false", restoreMissing)

        // Creating a backup writes a real file and allows verified restoration
        val backupId = governor.createCloudEnvironmentBackup()
        assertTrue(backupId.startsWith("NEXUS-BACKUP-"))
        val restoreValid = governor.restoreCloudEnvironmentFromDrive(backupId)
        assertTrue("Restoring a real verified backup file must return true", restoreValid)
    }

    @Test
    fun jniBridge_transparentlyReportsFallbackMode() {
        if (!NexusJniBridge.isNativeActive()) {
            assertEquals("KOTLIN_JVM_FALLBACK", NexusJniBridge.getExecutionMode())
            assertTrue(NexusJniBridge.getBridgeStatus().contains("KOTLIN_JVM_FALLBACK"))
            val inferenceOutput = NexusJniBridge.executeBitNetInference("Test", 2)
            assertTrue(inferenceOutput.contains("KOTLIN_JVM_FALLBACK"))
        }
    }

    @Test
    fun universalDeveloper_performsRealStaticAnalysisOnDisk() = runBlocking {
        val tempDir = File(System.getProperty("java.io.tmpdir", "."), "nexus_dev_test_${System.nanoTime()}")
        val developer = UniversalDeveloper(baseDirectory = tempDir)

        // Valid code snippet passes static check
        val validRes = developer.executeDevTask(
            language = "KOTLIN",
            action = "STATIC_ANALYSIS",
            codeSnippet = "package com.test\nclass ValidSample { fun ok(): Int { return 1 } }"
        )
        assertTrue(validRes.success)
        assertNotNull(validRes.artifactPath)
        assertTrue(File(validRes.artifactPath!!).exists())

        // Unbalanced braces code snippet fails static check
        val invalidRes = developer.executeDevTask(
            language = "KOTLIN",
            action = "STATIC_ANALYSIS",
            codeSnippet = "package com.test\nclass BrokenSample { fun fail(): Int { return 1 "
        )
        assertFalse("Unbalanced braces must be flagged by real static analysis", invalidRes.success)
    }
}
