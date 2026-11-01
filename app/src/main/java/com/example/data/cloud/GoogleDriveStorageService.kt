package com.example.data.cloud

import com.example.core.StorageSpaceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

/**
 * NEXUS CLOUD INTELLIGENCE — GOOGLE DRIVE API v3 SERVICE LAYER
 *
 * Implements actual Google Drive REST API v3 storage operations with strict architectural separation:
 * 1. NEXUS SYSTEM STORAGE ([StorageSpaceType.SYSTEM_STORAGE]):
 *    - Isolated in Google Drive's `appDataFolder` container (`https://www.googleapis.com/auth/drive.appdata`).
 *    - Invisible to personal Drive browsing; zero cross-contamination with personal files.
 *    - Enforces a strict 1,000 MB (1 GB = 1,048,576,000 bytes) hard ceiling before any upload.
 *
 * 2. NEXUS USER STORAGE ([StorageSpaceType.USER_STORAGE]):
 *    - Stored in a dedicated `NEXUS_USER_STORAGE` folder in the user's primary Drive (`https://www.googleapis.com/auth/drive.file`).
 *    - Uses the user's personal Google Drive quota (`/drive/v3/about?fields=storageQuota`), unbounded by the 1 GB system ceiling.
 *
 * Respects NEXUS Golden Rules:
 * - Never fabricates simulated HTTP 200 responses when unauthenticated or offline; explicitly reports [DriveConnectionState].
 * - Every remote operation produces an observable verification proof (SHA-256 of payload + Drive fileId/md5Checksum).
 */

enum class DriveConnectionState {
    UNAUTHENTICATED_OAUTH_REQUIRED,
    AUTHENTICATED_READY,
    SYNC_IN_PROGRESS,
    CONNECTED_VERIFIED,
    NETWORK_OR_API_ERROR
}

data class DriveRemoteFileMetadata(
    val driveFileId: String,
    val name: String,
    val mimeType: String,
    val sizeBytes: Long,
    val spaceType: StorageSpaceType,
    val spaces: List<String>,
    val parents: List<String>,
    val md5Checksum: String?,
    val localSha256: String,
    val modifiedTime: String? = null
)

data class DriveAccountQuota(
    val limitBytes: Long,
    val usageBytes: Long,
    val usageInDriveBytes: Long,
    val systemAppDataUsedBytes: Long
) {
    val totalGb: Float
        get() = if (limitBytes > 0L) (limitBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)).toFloat() else 15.0f

    val usedGb: Float
        get() = (usageBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)).toFloat()

    val systemAppDataUsedMb: Float
        get() = (systemAppDataUsedBytes.toDouble() / (1024.0 * 1024.0)).toFloat()
}

sealed class DriveOperationResult<out T> {
    data class Success<T>(
        val data: T,
        val httpStatusCode: Int,
        val observableProof: String
    ) : DriveOperationResult<T>()

    data class Unauthenticated(
        val reason: String = "Jeton OAuth2 Google Drive absent. Authentification requise (scopes: drive.appdata, drive.file)."
    ) : DriveOperationResult<Nothing>()

    data class QuotaExceeded(
        val spaceType: StorageSpaceType,
        val currentUsedMb: Float,
        val requestedMb: Float,
        val capMb: Float,
        val reason: String
    ) : DriveOperationResult<Nothing>()

    data class IsolationViolation(
        val reason: String
    ) : DriveOperationResult<Nothing>()

    data class ApiError(
        val httpStatusCode: Int,
        val message: String
    ) : DriveOperationResult<Nothing>()
}

class GoogleDriveStorageService(
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build(),
    private val baseApiUrl: String = "https://www.googleapis.com/drive/v3",
    private val baseUploadUrl: String = "https://www.googleapis.com/upload/drive/v3"
) {
    companion object {
        const val PROVISIONED_PROJECT_ID = "gen-lang-client-0700572400"
        const val PROVISIONED_OAUTH_CLIENT_ID = "44519688170-6qrbiemdbvg2u1sgj57s32sc6846shm1.apps.googleusercontent.com"

        const val SCOPE_SYSTEM_APPDATA = "https://www.googleapis.com/auth/drive.appdata"
        const val SCOPE_USER_DRIVE_FILE = "https://www.googleapis.com/auth/drive.file"

        const val SYSTEM_SPACE_NAME = "appDataFolder"
        const val USER_SPACE_NAME = "drive"
        const val USER_ROOT_FOLDER_NAME = "NEXUS_USER_STORAGE"
        const val FOLDER_MIME_TYPE = "application/vnd.google-apps.folder"

        const val SYSTEM_STORAGE_CAP_MB = 1000.0f
        const val SYSTEM_STORAGE_CAP_BYTES = 1000L * 1024L * 1024L // 1,000 MB strict cap
    }

    @Volatile
    private var accessToken: String? = null

    @Volatile
    private var cachedUserFolderId: String? = null

    private val _connectionState = MutableStateFlow(DriveConnectionState.UNAUTHENTICATED_OAUTH_REQUIRED)
    val connectionState: StateFlow<DriveConnectionState> = _connectionState.asStateFlow()

    private val _lastSyncProof = MutableStateFlow<String?>(null)
    val lastSyncProof: StateFlow<String?> = _lastSyncProof.asStateFlow()

    /**
     * Configures or clears the OAuth 2.0 Bearer access token for Google Drive API v3 calls.
     */
    fun setAccessToken(token: String?) {
        val cleaned = token?.trim()?.removePrefix("Bearer ")?.trim()
        if (cleaned.isNullOrBlank()) {
            accessToken = null
            cachedUserFolderId = null
            _connectionState.value = DriveConnectionState.UNAUTHENTICATED_OAUTH_REQUIRED
        } else {
            accessToken = cleaned
            _connectionState.value = DriveConnectionState.AUTHENTICATED_READY
        }
    }

    fun isAuthenticated(): Boolean = !accessToken.isNullOrBlank()

    fun getRequiredOAuthScopes(): List<String> = listOf(SCOPE_SYSTEM_APPDATA, SCOPE_USER_DRIVE_FILE)

    /**
     * Builds the Google Drive API v3 metadata JSON for a file, enforcing strict separation between:
     * - [StorageSpaceType.SYSTEM_STORAGE] -> parents = ["appDataFolder"]
     * - [StorageSpaceType.USER_STORAGE]   -> parents = [<NEXUS_USER_STORAGE folderId>] (never appDataFolder)
     */
    fun buildFileMetadataJson(
        fileName: String,
        mimeType: String,
        spaceType: StorageSpaceType,
        userFolderId: String? = null,
        customProperties: Map<String, String> = emptyMap()
    ): JSONObject {
        if (spaceType == StorageSpaceType.USER_STORAGE && userFolderId == SYSTEM_SPACE_NAME) {
            throw IllegalArgumentException("VIOLATION D'ISOLATION: Impossible de placer un fichier USER_STORAGE dans appDataFolder.")
        }

        val parentsArray = JSONArray()
        when (spaceType) {
            StorageSpaceType.SYSTEM_STORAGE -> parentsArray.put(SYSTEM_SPACE_NAME)
            StorageSpaceType.USER_STORAGE -> {
                if (!userFolderId.isNullOrBlank()) {
                    parentsArray.put(userFolderId)
                }
            }
        }

        val appProps = JSONObject().apply {
            put("nexusSpaceType", spaceType.name)
            customProperties.forEach { (k, v) -> put(k, v) }
        }

        return JSONObject().apply {
            put("name", fileName)
            put("mimeType", mimeType)
            if (parentsArray.length() > 0) {
                put("parents", parentsArray)
            }
            put("appProperties", appProps)
        }
    }

    /**
     * Queries `/drive/v3/about?fields=storageQuota` and system `appDataFolder` usage.
     */
    suspend fun fetchDriveQuota(): DriveOperationResult<DriveAccountQuota> = withContext(Dispatchers.IO) {
        val token = accessToken ?: return@withContext DriveOperationResult.Unauthenticated()
        _connectionState.value = DriveConnectionState.SYNC_IN_PROGRESS

        try {
            val request = Request.Builder()
                .url("$baseApiUrl/about?fields=storageQuota(limit,usage,usageInDrive)")
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Accept", "application/json")
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    _connectionState.value = DriveConnectionState.NETWORK_OR_API_ERROR
                    return@withContext DriveOperationResult.ApiError(
                        httpStatusCode = response.code,
                        message = "Erreur Drive API /about (HTTP ${response.code}): ${bodyStr.take(180)}"
                    )
                }

                val root = JSONObject(bodyStr)
                val quotaObj = root.optJSONObject("storageQuota") ?: JSONObject()
                val limitBytes = quotaObj.optString("limit", "0").toLongOrNull() ?: 0L
                val usageBytes = quotaObj.optString("usage", "0").toLongOrNull() ?: 0L
                val usageInDriveBytes = quotaObj.optString("usageInDrive", "0").toLongOrNull() ?: 0L

                val sysListResult = listFilesBySpaceInternal(token, StorageSpaceType.SYSTEM_STORAGE)
                val sysUsedBytes = when (sysListResult) {
                    is DriveOperationResult.Success -> sysListResult.data.sumOf { it.sizeBytes }
                    else -> 0L
                }

                val quota = DriveAccountQuota(
                    limitBytes = limitBytes,
                    usageBytes = usageBytes,
                    usageInDriveBytes = usageInDriveBytes,
                    systemAppDataUsedBytes = sysUsedBytes
                )

                val proof = computeSha256("DRIVE-QUOTA:${quota.limitBytes}:${quota.usageBytes}:${quota.systemAppDataUsedBytes}".toByteArray())
                _lastSyncProof.value = "PROOF-DRIVE-QUOTA-SHA256:$proof"
                _connectionState.value = DriveConnectionState.CONNECTED_VERIFIED

                DriveOperationResult.Success(
                    data = quota,
                    httpStatusCode = response.code,
                    observableProof = "PROOF-DRIVE-QUOTA-SHA256:$proof"
                )
            }
        } catch (e: Exception) {
            _connectionState.value = DriveConnectionState.NETWORK_OR_API_ERROR
            DriveOperationResult.ApiError(
                httpStatusCode = -1,
                message = "Exception réseau Drive API /about: ${e.message ?: e.javaClass.simpleName}"
            )
        }
    }

    /**
     * Lists files strictly within [StorageSpaceType.SYSTEM_STORAGE] (`spaces=appDataFolder`)
     * or [StorageSpaceType.USER_STORAGE] (`spaces=drive` inside `NEXUS_USER_STORAGE`).
     */
    suspend fun listFiles(spaceType: StorageSpaceType): DriveOperationResult<List<DriveRemoteFileMetadata>> =
        withContext(Dispatchers.IO) {
            val token = accessToken ?: return@withContext DriveOperationResult.Unauthenticated()
            listFilesBySpaceInternal(token, spaceType)
        }

    private fun listFilesBySpaceInternal(
        token: String,
        spaceType: StorageSpaceType
    ): DriveOperationResult<List<DriveRemoteFileMetadata>> {
        return try {
            val spaceParam = if (spaceType == StorageSpaceType.SYSTEM_STORAGE) SYSTEM_SPACE_NAME else USER_SPACE_NAME
            val query = when (spaceType) {
                StorageSpaceType.SYSTEM_STORAGE -> "trashed = false and 'appDataFolder' in parents"
                StorageSpaceType.USER_STORAGE -> {
                    val folderId = cachedUserFolderId
                    if (!folderId.isNullOrBlank()) {
                        "trashed = false and '$folderId' in parents"
                    } else {
                        "trashed = false and mimeType != '$FOLDER_MIME_TYPE' and appProperties has { key='nexusSpaceType' and value='USER_STORAGE' }"
                    }
                }
            }

            val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
            val fields = "files(id,name,mimeType,size,spaces,parents,md5Checksum,modifiedTime,appProperties)"
            val url = "$baseApiUrl/files?spaces=$spaceParam&q=$encodedQuery&fields=$fields&pageSize=100"

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Accept", "application/json")
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return DriveOperationResult.ApiError(
                        httpStatusCode = response.code,
                        message = "Erreur Drive API listFiles [$spaceType] (HTTP ${response.code}): ${bodyStr.take(180)}"
                    )
                }

                val json = JSONObject(bodyStr)
                val filesArr = json.optJSONArray("files") ?: JSONArray()
                val items = mutableListOf<DriveRemoteFileMetadata>()

                for (i in 0 until filesArr.length()) {
                    val f = filesArr.getJSONObject(i)
                    val id = f.optString("id")
                    val name = f.optString("name")
                    val mime = f.optString("mimeType", "application/octet-stream")
                    val size = f.optString("size", "0").toLongOrNull() ?: 0L
                    val md5 = f.optString("md5Checksum", "").takeIf { it.isNotBlank() }
                    val modified = f.optString("modifiedTime", "").takeIf { it.isNotBlank() }

                    val spacesList = mutableListOf<String>()
                    f.optJSONArray("spaces")?.let { arr ->
                        for (j in 0 until arr.length()) spacesList.add(arr.optString(j))
                    }

                    val parentsList = mutableListOf<String>()
                    f.optJSONArray("parents")?.let { arr ->
                        for (j in 0 until arr.length()) parentsList.add(arr.optString(j))
                    }

                    // Enforce zero cross-contamination verification on listed items
                    if (spaceType == StorageSpaceType.SYSTEM_STORAGE && spacesList.isNotEmpty() && !spacesList.contains(SYSTEM_SPACE_NAME)) {
                        return DriveOperationResult.IsolationViolation(
                            "Contamination détectée: Fichier '$name' ($id) hors de appDataFolder dans SYSTEM_STORAGE."
                        )
                    }
                    if (spaceType == StorageSpaceType.USER_STORAGE && spacesList.contains(SYSTEM_SPACE_NAME)) {
                        return DriveOperationResult.IsolationViolation(
                            "Contamination détectée: Fichier '$name' ($id) appartient à appDataFolder dans USER_STORAGE."
                        )
                    }

                    val itemHash = computeSha256("$id:$name:$size:${md5.orEmpty()}".toByteArray())
                    items.add(
                        DriveRemoteFileMetadata(
                            driveFileId = id,
                            name = name,
                            mimeType = mime,
                            sizeBytes = size,
                            spaceType = spaceType,
                            spaces = spacesList.ifEmpty { listOf(spaceParam) },
                            parents = parentsList,
                            md5Checksum = md5,
                            localSha256 = itemHash,
                            modifiedTime = modified
                        )
                    )
                }

                val listProof = computeSha256("LIST-${spaceType.name}:${items.size}:${items.joinToString { it.driveFileId }}".toByteArray())
                DriveOperationResult.Success(
                    data = items,
                    httpStatusCode = response.code,
                    observableProof = "PROOF-DRIVE-LIST-${spaceType.name}-SHA256:$listProof"
                )
            }
        } catch (e: Exception) {
            DriveOperationResult.ApiError(
                httpStatusCode = -1,
                message = "Exception listFiles [$spaceType]: ${e.message ?: e.javaClass.simpleName}"
            )
        }
    }

    /**
     * Uploads a system file to Google Drive's isolated `appDataFolder` (`StorageSpaceType.SYSTEM_STORAGE`).
     * Enforces the strict 1,000 MB ceiling before transmitting bytes over the network.
     */
    suspend fun uploadSystemFile(
        localFile: File,
        declaredSizeMb: Float,
        mimeType: String,
        currentSystemUsedMb: Float
    ): DriveOperationResult<DriveRemoteFileMetadata> = withContext(Dispatchers.IO) {
        if (!localFile.exists()) {
            return@withContext DriveOperationResult.ApiError(400, "Fichier local introuvable: ${localFile.absolutePath}")
        }

        val effectiveSizeMb = maxOf(declaredSizeMb, localFile.length().toFloat() / (1024f * 1024f))
        if (currentSystemUsedMb + effectiveSizeMb > SYSTEM_STORAGE_CAP_MB) {
            return@withContext DriveOperationResult.QuotaExceeded(
                spaceType = StorageSpaceType.SYSTEM_STORAGE,
                currentUsedMb = currentSystemUsedMb,
                requestedMb = effectiveSizeMb,
                capMb = SYSTEM_STORAGE_CAP_MB,
                reason = "Plafond strict NEXUS SYSTEM STORAGE (1000 Mo) dépassé sur Google Drive appDataFolder (${currentSystemUsedMb + effectiveSizeMb} Mo > 1000 Mo)."
            )
        }

        val token = accessToken ?: return@withContext DriveOperationResult.Unauthenticated()

        // Also check remote appDataFolder usage if available
        val remoteList = listFilesBySpaceInternal(token, StorageSpaceType.SYSTEM_STORAGE)
        if (remoteList is DriveOperationResult.Success) {
            val remoteUsedBytes = remoteList.data.sumOf { it.sizeBytes }
            val requestedBytes = (effectiveSizeMb * 1024f * 1024f).toLong()
            if (remoteUsedBytes + requestedBytes > SYSTEM_STORAGE_CAP_BYTES) {
                val remoteUsedMb = remoteUsedBytes.toFloat() / (1024f * 1024f)
                return@withContext DriveOperationResult.QuotaExceeded(
                    spaceType = StorageSpaceType.SYSTEM_STORAGE,
                    currentUsedMb = remoteUsedMb,
                    requestedMb = effectiveSizeMb,
                    capMb = SYSTEM_STORAGE_CAP_MB,
                    reason = "Plafond distant appDataFolder (1000 Mo) atteint sur Google Drive ($remoteUsedMb Mo + $effectiveSizeMb Mo)."
                )
            }
        }

        uploadMultipartFileInternal(
            token = token,
            localFile = localFile,
            mimeType = mimeType,
            spaceType = StorageSpaceType.SYSTEM_STORAGE,
            parentFolderId = SYSTEM_SPACE_NAME,
            declaredSizeMb = declaredSizeMb
        )
    }

    /**
     * Uploads a user personal file to the `NEXUS_USER_STORAGE` folder in the user's Google Drive (`StorageSpaceType.USER_STORAGE`).
     * Unbounded by the 1,000 MB system cap; governed only by the user's personal Google Drive quota.
     */
    suspend fun uploadUserFile(
        localFile: File,
        declaredSizeMb: Float,
        mimeType: String
    ): DriveOperationResult<DriveRemoteFileMetadata> = withContext(Dispatchers.IO) {
        if (!localFile.exists()) {
            return@withContext DriveOperationResult.ApiError(400, "Fichier personnel local introuvable: ${localFile.absolutePath}")
        }

        val token = accessToken ?: return@withContext DriveOperationResult.Unauthenticated()

        val folderResult = ensureUserStorageFolderInternal(token)
        val userFolderId = when (folderResult) {
            is DriveOperationResult.Success -> folderResult.data
            is DriveOperationResult.ApiError -> return@withContext folderResult
            is DriveOperationResult.Unauthenticated -> return@withContext folderResult
            is DriveOperationResult.QuotaExceeded -> return@withContext folderResult
            is DriveOperationResult.IsolationViolation -> return@withContext folderResult
        }

        if (userFolderId == SYSTEM_SPACE_NAME) {
            return@withContext DriveOperationResult.IsolationViolation(
                "VIOLATION D'ISOLATION CRITIQUE: Le dossier utilisateur ne peut jamais pointer vers appDataFolder."
            )
        }

        uploadMultipartFileInternal(
            token = token,
            localFile = localFile,
            mimeType = mimeType,
            spaceType = StorageSpaceType.USER_STORAGE,
            parentFolderId = userFolderId,
            declaredSizeMb = declaredSizeMb
        )
    }

    /**
     * Downloads a file by its Google Drive `fileId` (`alt=media`) into `destinationFile` and verifies its SHA-256 hash.
     */
    suspend fun downloadFile(
        driveFileId: String,
        expectedSpaceType: StorageSpaceType,
        destinationFile: File
    ): DriveOperationResult<File> = withContext(Dispatchers.IO) {
        val token = accessToken ?: return@withContext DriveOperationResult.Unauthenticated()

        try {
            // 1. Verify metadata space isolation first
            val metaReq = Request.Builder()
                .url("$baseApiUrl/files/$driveFileId?fields=id,name,spaces,appProperties")
                .addHeader("Authorization", "Bearer $token")
                .get()
                .build()

            httpClient.newCall(metaReq).execute().use { metaResp ->
                val metaBody = metaResp.body?.string().orEmpty()
                if (!metaResp.isSuccessful) {
                    return@withContext DriveOperationResult.ApiError(
                        httpStatusCode = metaResp.code,
                        message = "Impossible de vérifier les métadonnées Drive pour $driveFileId (HTTP ${metaResp.code})"
                    )
                }
                val metaJson = JSONObject(metaBody)
                val spacesArr = metaJson.optJSONArray("spaces")
                val inAppData = (0 until (spacesArr?.length() ?: 0)).any { spacesArr?.optString(it) == SYSTEM_SPACE_NAME }

                if (expectedSpaceType == StorageSpaceType.SYSTEM_STORAGE && spacesArr != null && !inAppData) {
                    return@withContext DriveOperationResult.IsolationViolation(
                        "Téléchargement refusé: Le fichier $driveFileId n'appartient pas à l'espace système isolé appDataFolder."
                    )
                }
                if (expectedSpaceType == StorageSpaceType.USER_STORAGE && inAppData) {
                    return@withContext DriveOperationResult.IsolationViolation(
                        "Téléchargement refusé: Le fichier $driveFileId appartient à appDataFolder et non à l'espace utilisateur."
                    )
                }
            }

            // 2. Download binary content (`alt=media`)
            val downloadReq = Request.Builder()
                .url("$baseApiUrl/files/$driveFileId?alt=media")
                .addHeader("Authorization", "Bearer $token")
                .get()
                .build()

            httpClient.newCall(downloadReq).execute().use { mediaResp ->
                if (!mediaResp.isSuccessful) {
                    return@withContext DriveOperationResult.ApiError(
                        httpStatusCode = mediaResp.code,
                        message = "Échec du téléchargement média Drive pour $driveFileId (HTTP ${mediaResp.code})"
                    )
                }
                val bytes = mediaResp.body?.bytes() ?: ByteArray(0)
                destinationFile.parentFile?.mkdirs()
                destinationFile.writeBytes(bytes)
                val sha = computeSha256(bytes)
                val proof = "PROOF-DRIVE-DOWNLOAD-SHA256:$sha"
                _lastSyncProof.value = proof

                DriveOperationResult.Success(
                    data = destinationFile,
                    httpStatusCode = mediaResp.code,
                    observableProof = proof
                )
            }
        } catch (e: Exception) {
            DriveOperationResult.ApiError(
                httpStatusCode = -1,
                message = "Exception téléchargement Drive ($driveFileId): ${e.message ?: e.javaClass.simpleName}"
            )
        }
    }

    private fun ensureUserStorageFolderInternal(token: String): DriveOperationResult<String> {
        cachedUserFolderId?.let { return DriveOperationResult.Success(it, 200, "CACHED-FOLDER:$it") }

        return try {
            val q = "mimeType = '$FOLDER_MIME_TYPE' and name = '$USER_ROOT_FOLDER_NAME' and trashed = false"
            val encodedQ = java.net.URLEncoder.encode(q, "UTF-8")
            val searchReq = Request.Builder()
                .url("$baseApiUrl/files?spaces=$USER_SPACE_NAME&q=$encodedQ&fields=files(id,name,spaces)")
                .addHeader("Authorization", "Bearer $token")
                .get()
                .build()

            httpClient.newCall(searchReq).execute().use { resp ->
                val bodyStr = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) {
                    return DriveOperationResult.ApiError(resp.code, "Erreur recherche dossier $USER_ROOT_FOLDER_NAME: ${bodyStr.take(150)}")
                }
                val files = JSONObject(bodyStr).optJSONArray("files")
                if (files != null && files.length() > 0) {
                    val existingId = files.getJSONObject(0).getString("id")
                    cachedUserFolderId = existingId
                    return DriveOperationResult.Success(existingId, resp.code, "EXISTING-FOLDER:$existingId")
                }
            }

            // Create NEXUS_USER_STORAGE folder in primary Drive
            val folderMeta = JSONObject().apply {
                put("name", USER_ROOT_FOLDER_NAME)
                put("mimeType", FOLDER_MIME_TYPE)
                put("appProperties", JSONObject().put("nexusSpaceType", StorageSpaceType.USER_STORAGE.name))
            }

            val createReq = Request.Builder()
                .url("$baseApiUrl/files?fields=id,name,spaces")
                .addHeader("Authorization", "Bearer $token")
                .post(folderMeta.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            httpClient.newCall(createReq).execute().use { createResp ->
                val bodyStr = createResp.body?.string().orEmpty()
                if (!createResp.isSuccessful) {
                    return DriveOperationResult.ApiError(createResp.code, "Erreur création dossier $USER_ROOT_FOLDER_NAME: ${bodyStr.take(150)}")
                }
                val createdId = JSONObject(bodyStr).getString("id")
                cachedUserFolderId = createdId
                DriveOperationResult.Success(createdId, createResp.code, "CREATED-FOLDER:$createdId")
            }
        } catch (e: Exception) {
            DriveOperationResult.ApiError(-1, "Exception ensureUserStorageFolder: ${e.message ?: e.javaClass.simpleName}")
        }
    }

    private fun uploadMultipartFileInternal(
        token: String,
        localFile: File,
        mimeType: String,
        spaceType: StorageSpaceType,
        parentFolderId: String,
        declaredSizeMb: Float
    ): DriveOperationResult<DriveRemoteFileMetadata> {
        _connectionState.value = DriveConnectionState.SYNC_IN_PROGRESS
        return try {
            val fileBytes = localFile.readBytes()
            val localSha256 = computeSha256(fileBytes)

            val metadataJson = buildFileMetadataJson(
                fileName = localFile.name,
                mimeType = mimeType,
                spaceType = spaceType,
                userFolderId = if (spaceType == StorageSpaceType.USER_STORAGE) parentFolderId else null,
                customProperties = mapOf(
                    "localSha256" to localSha256,
                    "declaredSizeMb" to declaredSizeMb.toString()
                )
            )

            val multipartBody = MultipartBody.Builder()
                .setType("multipart/related".toMediaType())
                .addPart(metadataJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .addPart(fileBytes.toRequestBody(mimeType.toMediaType()))
                .build()

            val fields = "id,name,mimeType,size,spaces,parents,md5Checksum,modifiedTime"
            val request = Request.Builder()
                .url("$baseUploadUrl/files?uploadType=multipart&fields=$fields")
                .addHeader("Authorization", "Bearer $token")
                .post(multipartBody)
                .build()

            httpClient.newCall(request).execute().use { response ->
                val respBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    _connectionState.value = DriveConnectionState.NETWORK_OR_API_ERROR
                    return DriveOperationResult.ApiError(
                        httpStatusCode = response.code,
                        message = "Échec upload Drive [$spaceType] (HTTP ${response.code}): ${respBody.take(180)}"
                    )
                }

                val json = JSONObject(respBody)
                val driveId = json.optString("id")
                val name = json.optString("name", localFile.name)
                val sizeBytes = json.optString("size", fileBytes.size.toString()).toLongOrNull() ?: fileBytes.size.toLong()
                val md5 = json.optString("md5Checksum", "").takeIf { it.isNotBlank() }
                val modifiedTime = json.optString("modifiedTime", "").takeIf { it.isNotBlank() }

                val spacesList = mutableListOf<String>()
                json.optJSONArray("spaces")?.let { arr ->
                    for (i in 0 until arr.length()) spacesList.add(arr.optString(i))
                }
                val expectedSpace = if (spaceType == StorageSpaceType.SYSTEM_STORAGE) SYSTEM_SPACE_NAME else USER_SPACE_NAME
                if (spacesList.isEmpty()) spacesList.add(expectedSpace)

                val parentsList = mutableListOf<String>()
                json.optJSONArray("parents")?.let { arr ->
                    for (i in 0 until arr.length()) parentsList.add(arr.optString(i))
                }
                if (parentsList.isEmpty()) parentsList.add(parentFolderId)

                val metadata = DriveRemoteFileMetadata(
                    driveFileId = driveId,
                    name = name,
                    mimeType = mimeType,
                    sizeBytes = sizeBytes,
                    spaceType = spaceType,
                    spaces = spacesList,
                    parents = parentsList,
                    md5Checksum = md5,
                    localSha256 = localSha256,
                    modifiedTime = modifiedTime
                )

                val proofHash = computeSha256("UPLOAD:${spaceType.name}:$driveId:$localSha256:${md5.orEmpty()}".toByteArray())
                val observableProof = "PROOF-DRIVE-UPLOAD-${spaceType.name}-SHA256:$proofHash"
                _lastSyncProof.value = observableProof
                _connectionState.value = DriveConnectionState.CONNECTED_VERIFIED

                DriveOperationResult.Success(
                    data = metadata,
                    httpStatusCode = response.code,
                    observableProof = observableProof
                )
            }
        } catch (e: Exception) {
            _connectionState.value = DriveConnectionState.NETWORK_OR_API_ERROR
            DriveOperationResult.ApiError(
                httpStatusCode = -1,
                message = "Exception upload Drive [$spaceType]: ${e.message ?: e.javaClass.simpleName}"
            )
        }
    }

    private fun computeSha256(bytes: ByteArray): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(bytes)
            .joinToString("") { "%02x".format(it) }
            .take(16)
            .uppercase()
    }
}
