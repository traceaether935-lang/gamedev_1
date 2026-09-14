package com.example.a2049.auth

import android.app.Activity
import com.google.android.gms.games.PlayGames
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
        } catch (e: Exception) {
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
        } catch (e: Exception) {
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
        } catch (e: Exception) {
            checkAuthentication()
        }
    }
}
