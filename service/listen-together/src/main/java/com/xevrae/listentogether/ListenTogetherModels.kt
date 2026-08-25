package com.xevrae.listentogether

data class LTTrackInfo(
    val id: String, val title: String, val artist: String,
    val album: String = "", val duration: Long = 0L,
    val thumbnail: String = "", val suggestedBy: String = "",
)
data class LTUserInfo(val userId: String, val username: String, val isHost: Boolean, val isConnected: Boolean)
data class LTRoomState(
    val roomCode: String, val hostId: String, val users: List<LTUserInfo>,
    val currentTrack: LTTrackInfo?, val isPlaying: Boolean,
    val position: Long, val lastUpdate: Long, val volume: Float, val queue: List<LTTrackInfo>,
) {
    fun livePosition(): Long = if (isPlaying) position + (System.currentTimeMillis() - lastUpdate) else position
}

sealed class LTConnectionState {
    object Disconnected : LTConnectionState()
    object Connecting   : LTConnectionState()
    object Connected    : LTConnectionState()
    data class Failed(val reason: String) : LTConnectionState()
}

sealed class LTEvent {
    object Connected : LTEvent()
    object Disconnected : LTEvent()
    data class RoomCreated(val roomCode: String, val userId: String, val sessionToken: String) : LTEvent()
    data class JoinRequest(val userId: String, val username: String) : LTEvent()
    data class JoinApproved(val roomCode: String, val userId: String, val sessionToken: String, val state: LTRoomState) : LTEvent()
    data class JoinRejected(val reason: String) : LTEvent()
    data class UserJoined(val userId: String, val username: String) : LTEvent()
    data class UserLeft(val userId: String, val username: String) : LTEvent()
    data class UserDisconnected(val userId: String, val username: String) : LTEvent()
    data class UserReconnected(val userId: String, val username: String) : LTEvent()
    data class SyncPlayback(
        val action: String, val trackId: String = "", val position: Long = 0L,
        val trackInfo: LTTrackInfo? = null, val insertNext: Boolean = false,
        val queue: List<LTTrackInfo> = emptyList(), val volume: Float = 1f, val serverTime: Long = 0L,
    ) : LTEvent()
    data class SyncState(val currentTrack: LTTrackInfo?, val isPlaying: Boolean, val position: Long, val lastUpdate: Long, val volume: Float) : LTEvent()
    data class BufferWait(val trackId: String, val waitingFor: List<String>) : LTEvent()
    data class BufferComplete(val trackId: String) : LTEvent()
    data class HostChanged(val newHostId: String, val newHostName: String) : LTEvent()
    data class Kicked(val reason: String) : LTEvent()
    data class Reconnected(val roomCode: String, val userId: String, val state: LTRoomState, val isHost: Boolean) : LTEvent()
    data class SuggestionReceived(val suggestionId: String, val fromUserId: String, val fromUsername: String, val trackInfo: LTTrackInfo) : LTEvent()
    data class SuggestionApproved(val suggestionId: String, val trackInfo: LTTrackInfo?) : LTEvent()
    data class SuggestionRejected(val suggestionId: String, val reason: String) : LTEvent()
    data class Error(val code: String, val message: String) : LTEvent()
}

object LTAction {
    const val PLAY = "play"; const val PAUSE = "pause"; const val SEEK = "seek"
    const val CHANGE_TRACK = "change_track"; const val SKIP_NEXT = "skip_next"; const val SKIP_PREV = "skip_prev"
    const val QUEUE_ADD = "queue_add"; const val QUEUE_REMOVE = "queue_remove"
    const val QUEUE_CLEAR = "queue_clear"; const val SYNC_QUEUE = "sync_queue"; const val SET_VOLUME = "set_volume"
}
