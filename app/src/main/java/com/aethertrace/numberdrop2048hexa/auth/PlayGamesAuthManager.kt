package com.aethertrace.numberdrop2048hexa.auth

import android.app.Activity
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.SnapshotsClient
import com.google.android.gms.games.snapshot.SnapshotMetadataChange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object PlayGamesConstants {
    // Leaderboard IDs (replace with IDs from Google Play Console)
    const val LEADERBOARD_CLASSIC_4X4 = "CgkI_xxxxxxxx_4x4"
    const val LEADERBOARD_CLASSIC_5X5 = "CgkI_xxxxxxxx_5x5"
    const val LEADERBOARD_HEXAGON_MERGE = "CgkI_xxxxxxxx_hex"
    const val LEADERBOARD_COLUMN_DROP = "CgkI_xxxxxxxx_col"

    // Achievement IDs (replace with IDs from Google Play Console)
    const val ACHIEVEMENT_REACH_1024 = "CgkI_xxxxxxxx_1024"
    const val ACHIEVEMENT_REACH_2048 = "CgkI_xxxxxxxx_2048"
    const val ACHIEVEMENT_REACH_4096 = "CgkI_xxxxxxxx_4096"
    const val ACHIEVEMENT_HEX_MASTER = "CgkI_xxxxxxxx_hex_master"
    const val ACHIEVEMENT_COLUMN_MASTER = "CgkI_xxxxxxxx_col_master"
}

class PlayGamesAuthManager(private val activity: Activity? = null) {

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _playerName = MutableStateFlow<String?>(null)
    val playerName: StateFlow<String?> = _playerName.asStateFlow()

    init {
        checkAuthentication()
    }

    fun checkAuthentication() {
        val currentActivity = activity ?: return
        try {
            val gamesSignInClient = PlayGames.getGamesSignInClient(currentActivity)
            gamesSignInClient.isAuthenticated.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result?.isAuthenticated == true) {
                    _isAuthenticated.value = true
                    fetchPlayerInfo()
                } else {
                    _isAuthenticated.value = false
                    _playerName.value = null
                }
            }
        } catch (_: Exception) {
            _isAuthenticated.value = false
            _playerName.value = null
        }
    }

    private fun fetchPlayerInfo() {
        val currentActivity = activity ?: return
        try {
            val playersClient = PlayGames.getPlayersClient(currentActivity)
            playersClient.currentPlayer.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val player = task.result
                    _playerName.value = player?.displayName
                }
            }
        } catch (_: Exception) {
            // Keep default/null
        }
    }

    fun signIn() {
        val currentActivity = activity ?: return
        try {
            val gamesSignInClient = PlayGames.getGamesSignInClient(currentActivity)
            gamesSignInClient.signIn().addOnCompleteListener { task ->
                if (task.isSuccessful && task.result?.isAuthenticated == true) {
                    _isAuthenticated.value = true
                    fetchPlayerInfo()
                } else {
                    checkAuthentication()
                }
            }
        } catch (_: Exception) {
            checkAuthentication()
        }
    }

    // --- 1. Leaderboards ---
    fun submitScore(leaderboardId: String, score: Long) {
        val currentActivity = activity ?: return
        if (!_isAuthenticated.value) return
        try {
            val leaderboardsClient = PlayGames.getLeaderboardsClient(currentActivity)
            leaderboardsClient.submitScore(leaderboardId, score)
        } catch (_: Exception) {}
    }

    fun showLeaderboardsUI(leaderboardId: String? = null) {
        val currentActivity = activity ?: return
        if (!_isAuthenticated.value) {
            signIn()
            return
        }
        try {
            val leaderboardsClient = PlayGames.getLeaderboardsClient(currentActivity)
            val intentTask = if (leaderboardId.isNullOrBlank()) {
                leaderboardsClient.allLeaderboardsIntent
            } else {
                leaderboardsClient.getLeaderboardIntent(leaderboardId)
            }
            intentTask.addOnSuccessListener { intent ->
                currentActivity.startActivityForResult(intent, 9002)
            }
        } catch (_: Exception) {}
    }

    // --- 2. Achievements ---
    fun unlockAchievement(achievementId: String) {
        val currentActivity = activity ?: return
        if (!_isAuthenticated.value) return
        try {
            val achievementsClient = PlayGames.getAchievementsClient(currentActivity)
            achievementsClient.unlock(achievementId)
        } catch (_: Exception) {}
    }

    fun incrementAchievement(achievementId: String, steps: Int) {
        val currentActivity = activity ?: return
        if (!_isAuthenticated.value) return
        try {
            val achievementsClient = PlayGames.getAchievementsClient(currentActivity)
            achievementsClient.increment(achievementId, steps)
        } catch (_: Exception) {}
    }

    fun showAchievementsUI() {
        val currentActivity = activity ?: return
        if (!_isAuthenticated.value) {
            signIn()
            return
        }
        try {
            val achievementsClient = PlayGames.getAchievementsClient(currentActivity)
            achievementsClient.achievementsIntent.addOnSuccessListener { intent ->
                currentActivity.startActivityForResult(intent, 9003)
            }
        } catch (_: Exception) {}
    }

    // --- 3. Cloud Save (Snapshots API) ---
    fun saveToCloud(snapshotName: String, dataJson: String) {
        val currentActivity = activity ?: return
        if (!_isAuthenticated.value) return
        try {
            val snapshotsClient = PlayGames.getSnapshotsClient(currentActivity)
            snapshotsClient.open(snapshotName, true, SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED)
                .addOnSuccessListener { conflictResult ->
                    val snapshot = conflictResult.data ?: return@addOnSuccessListener
                    snapshot.snapshotContents.writeBytes(dataJson.toByteArray(Charsets.UTF_8))
                    val metadataChange = SnapshotMetadataChange.Builder()
                        .setDescription("Saved game state for $snapshotName")
                        .build()
                    snapshotsClient.commitAndClose(snapshot, metadataChange)
                }
        } catch (_: Exception) {}
    }

    fun loadFromCloud(snapshotName: String, onLoaded: (String?) -> Unit) {
        val currentActivity = activity ?: return
        if (!_isAuthenticated.value) {
            onLoaded(null)
            return
        }
        try {
            val snapshotsClient = PlayGames.getSnapshotsClient(currentActivity)
            snapshotsClient.open(snapshotName, false, SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED)
                .addOnSuccessListener { conflictResult ->
                    val snapshot = conflictResult.data
                    if (snapshot != null) {
                        val bytes = snapshot.snapshotContents.readFully()
                        val json = String(bytes, Charsets.UTF_8)
                        onLoaded(json)
                    } else {
                        onLoaded(null)
                    }
                }
                .addOnFailureListener {
                    onLoaded(null)
                }
        } catch (_: Exception) {
            onLoaded(null)
        }
    }
}
