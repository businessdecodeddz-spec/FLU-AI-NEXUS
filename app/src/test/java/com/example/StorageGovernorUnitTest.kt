package com.example

import com.example.core.NexusStorageGovernor
import com.example.core.StorageSpaceType
import com.example.data.cloud.DriveConnectionState
import com.example.data.cloud.DriveOperationResult
import com.example.data.cloud.GoogleDriveStorageService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class StorageGovernorUnitTest {

    @Test
    fun storageGovernor_enforces1GbSystemCapAndAllowsUserStorage(): Unit = runBlocking {
        val governor = NexusStorageGovernor(dao = null)

        // 1. Initial status check (Default system usage ~142.5 MB)
        val initialStatus = governor.quotaStatus.value
        assertEquals(1000.0f, initialStatus.systemCapMb)
        assertFalse("System cap should not be exceeded initially", initialStatus.isSystemCapExceeded)

        // 2. Write valid system data (e.g. 200 MB)
        val validWrite = governor.writeSystemData("snapshot_v2.db", 200.0f, "application/sqlite")
        assertTrue("Valid system write should succeed", validWrite)

        // 3. Attempt to write system data exceeding 1,000 MB ceiling (e.g. 700 MB -> total > 1,000 MB)
        val excessiveWrite = governor.writeSystemData("huge_log.bin", 700.0f, "application/octet-stream")
        assertFalse("Write exceeding 1,000 MB system cap MUST be rejected by governor", excessiveWrite)
        assertTrue("Quota status should flag system cap exceeded", governor.quotaStatus.value.isSystemCapExceeded)

        // 4. Write user personal file to Google Drive (e.g. 500 MB)
        val userFile = governor.writeUserPersonalFile("Projet_3D.cad", 500.0f, "application/zip")
        assertNotNull("User personal file write should succeed independently", userFile)
        assertEquals(StorageSpaceType.USER_STORAGE, userFile.spaceType)
        assertEquals("drive/NEXUS_USER_STORAGE", userFile.driveSpaceTarget)

        // 5. Verify isolation and non-exceedance audit
        val audit = governor.verifyIsolationAndNonExceedance()
        assertTrue("Isolation audit must verify zero cross-contamination", audit.isZeroContaminationVerified)
        assertTrue("Audit signature must contain SHA256 proof", audit.proofSignature.contains("SHA256"))
    }

    @Test
    fun googleDriveStorageService_enforcesSpaceSeparationAndTruthfulAuthState(): Unit = runBlocking {
        val driveService = GoogleDriveStorageService()

        // 1. Unauthenticated by default (no simulated HTTP 200)
        assertFalse(driveService.isAuthenticated())
        assertEquals(DriveConnectionState.UNAUTHENTICATED_OAUTH_REQUIRED, driveService.connectionState.value)

        val unauthQuota = driveService.fetchDriveQuota()
        assertTrue("Must return Unauthenticated when no OAuth token is configured", unauthQuota is DriveOperationResult.Unauthenticated)

        // 2. Verify metadata JSON strictly separates SYSTEM_STORAGE (appDataFolder) and USER_STORAGE (user folder)
        val sysMeta = driveService.buildFileMetadataJson(
            fileName = "nexus_config.json",
            mimeType = "application/json",
            spaceType = StorageSpaceType.SYSTEM_STORAGE
        )
        assertEquals("appDataFolder", sysMeta.getJSONArray("parents").getString(0))
        assertEquals("SYSTEM_STORAGE", sysMeta.getJSONObject("appProperties").getString("nexusSpaceType"))

        val usrMeta = driveService.buildFileMetadataJson(
            fileName = "Rapport_Medical.pdf",
            mimeType = "application/pdf",
            spaceType = StorageSpaceType.USER_STORAGE,
            userFolderId = "FOLDER-USR-123"
        )
        assertEquals("FOLDER-USR-123", usrMeta.getJSONArray("parents").getString(0))
        assertEquals("USER_STORAGE", usrMeta.getJSONObject("appProperties").getString("nexusSpaceType"))

        // 3. Reject cross-contamination attempt (placing USER_STORAGE file inside appDataFolder)
        var crossContaminationRejected = false
        try {
            driveService.buildFileMetadataJson(
                fileName = "leak.pdf",
                mimeType = "application/pdf",
                spaceType = StorageSpaceType.USER_STORAGE,
                userFolderId = "appDataFolder"
            )
        } catch (_: IllegalArgumentException) {
            crossContaminationRejected = true
        }
        assertTrue("Placing USER_STORAGE inside appDataFolder must throw IllegalArgumentException", crossContaminationRejected)

        // 4. Reject system upload exceeding 1,000 MB cap before network call
        val tempFile = File.createTempFile("nexus_sys_test", ".bin").apply { writeText("test-payload") }
        val quotaResult = driveService.uploadSystemFile(
            localFile = tempFile,
            declaredSizeMb = 250f,
            mimeType = "application/octet-stream",
            currentSystemUsedMb = 800f
        )
        assertTrue("Upload exceeding 1,000 MB system cap must return QuotaExceeded", quotaResult is DriveOperationResult.QuotaExceeded)
        tempFile.delete()
    }
}
