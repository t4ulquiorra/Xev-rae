package com.xevrae.android.feature.login

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState
}

data class LoginUiState(
    val isLoading: Boolean = false,
    val isSpotifyLoggedIn: Boolean = false,
    val isDiscordLoggedIn: Boolean = false,
    val error: String? = null,
)
