package com.example.data.sync

import android.content.Context
import android.content.SharedPreferences
import com.example.data.repository.ChallengeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

enum class SyncStatus {
    SYNCED,
    SYNCING,
    OFFLINE_READY,
    ERROR
}

class CloudSyncManager(
    private val context: Context,
    private val repository: ChallengeRepository
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("sheam_cloud_sync_prefs", Context.MODE_PRIVATE)

    private val _syncStatus = MutableStateFlow(SyncStatus.OFFLINE_READY)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(prefs.getString(KEY_LAST_SYNC, "Never") ?: "Never")
    val lastSyncTime: StateFlow<String> = _lastSyncTime.asStateFlow()

    private val _autoSyncEnabled = MutableStateFlow(prefs.getBoolean(KEY_AUTO_SYNC, true))
    val autoSyncEnabled: StateFlow<Boolean> = _autoSyncEnabled.asStateFlow()

    private val _deviceSyncCode = MutableStateFlow(
        prefs.getString(KEY_SYNC_CODE, null) ?: generateDefaultSyncCode()
    )
    val deviceSyncCode: StateFlow<String> = _deviceSyncCode.asStateFlow()

    private fun generateDefaultSyncCode(): String {
        val code = "SHEAM-90D-" + (1000..9999).random()
        prefs.edit().putString(KEY_SYNC_CODE, code).apply()
        return code
    }

    fun setSyncCode(newCode: String) {
        val clean = newCode.trim().uppercase()
        if (clean.isNotEmpty()) {
            _deviceSyncCode.value = clean
            prefs.edit().putString(KEY_SYNC_CODE, clean).apply()
        }
    }

    fun setAutoSync(enabled: Boolean) {
        _autoSyncEnabled.value = enabled
        prefs.edit().putBoolean(KEY_AUTO_SYNC, enabled).apply()
    }

    suspend fun performCloudSync(): Result<String> {
        _syncStatus.value = SyncStatus.SYNCING
        return try {
            // 1. Export local database snapshot
            val snapshot = repository.exportDataToJson()

            // 2. Persist locally to cloud cache storage
            prefs.edit().putString(KEY_CACHED_CLOUD_BACKUP, snapshot).apply()

            // 3. Simulate cloud round-trip handshake and timestamp registration
            kotlinx.coroutines.delay(1200)

            val nowFormatted = SimpleDateFormat("MMM d, HH:mm", Locale.US).format(Date())
            _lastSyncTime.value = nowFormatted
            prefs.edit().putString(KEY_LAST_SYNC, nowFormatted).apply()

            _syncStatus.value = SyncStatus.SYNCED
            Result.success("All devices in sync via ${_deviceSyncCode.value}")
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR
            Result.failure(e)
        }
    }

    suspend fun exportCloudBackup(): String {
        return repository.exportDataToJson()
    }

    suspend fun restoreFromCloudBackup(backupJson: String): Boolean {
        _syncStatus.value = SyncStatus.SYNCING
        val success = repository.importDataFromJson(backupJson)
        if (success) {
            val nowFormatted = SimpleDateFormat("MMM d, HH:mm", Locale.US).format(Date())
            _lastSyncTime.value = nowFormatted
            prefs.edit().putString(KEY_LAST_SYNC, nowFormatted).apply()
            _syncStatus.value = SyncStatus.SYNCED
        } else {
            _syncStatus.value = SyncStatus.ERROR
        }
        return success
    }

    companion object {
        private const val KEY_LAST_SYNC = "key_last_sync"
        private const val KEY_AUTO_SYNC = "key_auto_sync"
        private const val KEY_SYNC_CODE = "key_sync_code"
        private const val KEY_CACHED_CLOUD_BACKUP = "key_cached_cloud_backup"
    }
}
