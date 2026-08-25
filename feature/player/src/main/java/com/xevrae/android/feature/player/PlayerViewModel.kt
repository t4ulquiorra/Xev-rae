package com.xevrae.android.feature.player

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState
}

data class PlayerUiState(
    val isPlaying: Boolean = false,
    val title: String = "",
    val artist: String = "",
    val albumArtUrl: String? = null,
    val durationMs: Long = 0L,
    val currentPositionMs: Long = 0L,
)
