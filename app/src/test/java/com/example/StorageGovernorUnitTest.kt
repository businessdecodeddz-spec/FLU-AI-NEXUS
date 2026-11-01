package com.example

import com.example.core.NexusStorageGovernor
import com.example.core.StorageSpaceType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

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

        // 5. Verify isolation and non-exceedance audit
        val audit = governor.verifyIsolationAndNonExceedance()
        assertTrue("Isolation audit must verify zero cross-contamination", audit.isZeroContaminationVerified)
        assertTrue("Audit signature must contain SHA256 proof", audit.proofSignature.contains("SHA256"))
    }
}
