package com.xevrae.listentogether

import com.google.protobuf.ByteString
import com.xevrae.listentogether.proto.Listentogether
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString.Companion.toByteString
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class ListenTogetherService(
    private val serverUrl: String = SERVER_URL,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
) {
    companion object {
        const val SERVER_URL = "wss://dare-server-go.onrender.com/ws"
    }

    private val _connectionState = MutableStateFlow<LTConnectionState>(LTConnectionState.Disconnected)
    val connectionState: StateFlow<LTConnectionState> = _connectionState.asStateFlow()
    private val _roomState = MutableStateFlow<LTRoomState?>(null)
    val roomState: StateFlow<LTRoomState?> = _roomState.asStateFlow()
    private val _events = MutableSharedFlow<LTEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<LTEvent> = _events.asSharedFlow()

    var myUserId: String = ""; private set
    var mySessionToken: String = ""; private set
    var isHost: Boolean = false; private set

    private val http = OkHttpClient.Builder().readTimeout(0, TimeUnit.MILLISECONDS).build()
    private var ws: WebSocket? = null

    fun connect() {
        val s = _connectionState.value
        if (s == LTConnectionState.Connecting || s == LTConnectionState.Connected) return
        _connectionState.value = LTConnectionState.Connecting
        ws = http.newWebSocket(Request.Builder().url(serverUrl).build(), Listener())
    }

    fun disconnect() {
        ws?.close(1000, "bye"); ws = null
        _connectionState.value = LTConnectionState.Disconnected
        clearLocal()
    }

    private fun clearLocal() { _roomState.value = null; myUserId = ""; mySessionToken = ""; isHost = false }

    fun createRoom(username: String) = send("create_room", Listentogether.CreateRoomPayload.newBuilder().setUsername(username).build())
    fun joinRoom(roomCode: String, username: String) = send("join_room", Listentogether.JoinRoomPayload.newBuilder().setRoomCode(roomCode).setUsername(username).build())
    fun leaveRoom() { sendEmpty("leave_room"); clearLocal() }
    fun approveJoin(userId: String) = send("approve_join", Listentogether.ApproveJoinPayload.newBuilder().setUserId(userId).build())
    fun rejectJoin(userId: String, reason: String = "") = send("reject_join", Listentogether.RejectJoinPayload.newBuilder().setUserId(userId).setReason(reason).build())
    fun kickUser(userId: String, reason: String = "") = send("kick_user", Listentogether.KickUserPayload.newBuilder().setUserId(userId).setReason(reason).build())
    fun transferHost(newHostId: String) = send("transfer_host", Listentogether.TransferHostPayload.newBuilder().setNewHostId(newHostId).build())
    fun play(position: Long) = sendPB { setAction(LTAction.PLAY).setPosition(position).setServerTime(System.currentTimeMillis()) }
    fun pause(position: Long) = sendPB { setAction(LTAction.PAUSE).setPosition(position) }
    fun seek(position: Long) = sendPB { setAction(LTAction.SEEK).setPosition(position) }
    fun changeTrack(t: LTTrackInfo) = sendPB { setAction(LTAction.CHANGE_TRACK).setTrackInfo(t.toProto()) }
    fun skipNext() = sendPB { setAction(LTAction.SKIP_NEXT) }
    fun skipPrev() = sendPB { setAction(LTAction.SKIP_PREV) }
    fun addToQueue(t: LTTrackInfo, insertNext: Boolean = false) = sendPB { setAction(LTAction.QUEUE_ADD).setTrackInfo(t.toProto()).setInsertNext(insertNext) }
    fun removeFromQueue(trackId: String) = sendPB { setAction(LTAction.QUEUE_REMOVE).setTrackId(trackId) }
    fun clearQueue() = sendPB { setAction(LTAction.QUEUE_CLEAR) }
    fun syncQueue(queue: List<LTTrackInfo>) = sendPB { setAction(LTAction.SYNC_QUEUE).addAllQueue(queue.map { it.toProto() }) }
    fun setVolume(volume: Float) = sendPB { setAction(LTAction.SET_VOLUME).setVolume(volume) }
    fun bufferReady(trackId: String) = send("buffer_ready", Listentogether.BufferReadyPayload.newBuilder().setTrackId(trackId).build())
    fun requestSync() = sendEmpty("request_sync")
    fun ping() = sendEmpty("ping")
    fun reconnect(token: String) = send("reconnect", Listentogether.ReconnectPayload.newBuilder().setSessionToken(token).build())
    fun suggestTrack(t: LTTrackInfo) = send("suggest_track", Listentogether.SuggestTrackPayload.newBuilder().setTrackInfo(t.toProto()).build())
    fun approveSuggestion(id: String) = send("approve_suggestion", Listentogether.ApproveSuggestionPayload.newBuilder().setSuggestionId(id).build())
    fun rejectSuggestion(id: String, reason: String = "") = send("reject_suggestion", Listentogether.RejectSuggestionPayload.newBuilder().setSuggestionId(id).setReason(reason).build())

    private inline fun sendPB(block: Listentogether.PlaybackActionPayload.Builder.() -> Listentogether.PlaybackActionPayload.Builder) =
        send("playback_action", Listentogether.PlaybackActionPayload.newBuilder().block().build())

    private fun send(type: String, msg: com.google.protobuf.MessageLite) {
        var bytes = msg.toByteArray(); var compressed = false
        if (bytes.size > 100) { val gz = gzip(bytes); if (gz.size < bytes.size) { bytes = gz; compressed = true } }
        val env = Listentogether.Envelope.newBuilder().setType(type).setPayload(ByteString.copyFrom(bytes)).setCompressed(compressed).build()
        ws?.send(env.toByteArray().toByteString())
    }

    private fun sendEmpty(type: String) { ws?.send(Listentogether.Envelope.newBuilder().setType(type).build().toByteArray().toByteString()) }

    private fun emit(ev: LTEvent) { scope.launch { _events.emit(ev) } }

    private fun updateRoom(fn: (LTRoomState) -> LTRoomState) { _roomState.value = _roomState.value?.let(fn) }

    private inner class Listener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            _connectionState.value = LTConnectionState.Connected; emit(LTEvent.Connected)
        }
        override fun onMessage(webSocket: WebSocket, bytes: okio.ByteString) = handleMsg(bytes.toByteArray())
        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) { webSocket.close(code, reason) }
        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            _connectionState.value = LTConnectionState.Disconnected; emit(LTEvent.Disconnected)
        }
        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            _connectionState.value = LTConnectionState.Failed(t.message ?: "failed"); emit(LTEvent.Disconnected)
        }
    }

    private fun handleMsg(data: ByteArray) {
        try {
            val env = Listentogether.Envelope.parseFrom(data)
            val p = if (env.compressed) ungzip(env.payload.toByteArray()) else env.payload.toByteArray()
            val ev: LTEvent? = when (env.type) {
                "room_created" -> Listentogether.RoomCreatedPayload.parseFrom(p).let {
                    myUserId = it.userId; mySessionToken = it.sessionToken; isHost = true
                    LTEvent.RoomCreated(it.roomCode, it.userId, it.sessionToken)
                }
                "join_request" -> Listentogether.JoinRequestPayload.parseFrom(p).let { LTEvent.JoinRequest(it.userId, it.username) }
                "join_approved" -> Listentogether.JoinApprovedPayload.parseFrom(p).let {
                    myUserId = it.userId; mySessionToken = it.sessionToken; isHost = false
                    val state = it.state.toModel(); _roomState.value = state
                    LTEvent.JoinApproved(it.roomCode, it.userId, it.sessionToken, state)
                }
                "join_rejected" -> LTEvent.JoinRejected(Listentogether.JoinRejectedPayload.parseFrom(p).reason)
                "user_joined" -> Listentogether.UserJoinedPayload.parseFrom(p).let {
                    updateRoom { s -> s.copy(users = s.users + LTUserInfo(it.userId, it.username, false, true)) }
                    LTEvent.UserJoined(it.userId, it.username)
                }
                "user_left" -> Listentogether.UserLeftPayload.parseFrom(p).let {
                    updateRoom { s -> s.copy(users = s.users.filterNot { u -> u.userId == it.userId }) }
                    LTEvent.UserLeft(it.userId, it.username)
                }
                "user_disconnected" -> Listentogether.UserDisconnectedPayload.parseFrom(p).let {
                    updateRoom { s -> s.copy(users = s.users.map { u -> if (u.userId == it.userId) u.copy(isConnected = false) else u }) }
                    LTEvent.UserDisconnected(it.userId, it.username)
                }
                "user_reconnected" -> Listentogether.UserReconnectedPayload.parseFrom(p).let {
                    updateRoom { s -> s.copy(users = s.users.map { u -> if (u.userId == it.userId) u.copy(isConnected = true) else u }) }
                    LTEvent.UserReconnected(it.userId, it.username)
                }
                "sync_playback" -> Listentogether.PlaybackActionPayload.parseFrom(p).let { pb ->
                    applyPlayback(pb)
                    LTEvent.SyncPlayback(pb.action, pb.trackId, pb.position,
                        if (pb.hasTrackInfo()) pb.trackInfo.toModel() else null,
                        pb.insertNext, pb.queueList.map { it.toModel() }, pb.volume, pb.serverTime)
                }
                "sync_state" -> Listentogether.SyncStatePayload.parseFrom(p).let {
                    updateRoom { s -> s.copy(currentTrack = if (it.hasCurrentTrack()) it.currentTrack.toModel() else null,
                        isPlaying = it.isPlaying, position = it.position, lastUpdate = it.lastUpdate, volume = it.volume) }
                    LTEvent.SyncState(if (it.hasCurrentTrack()) it.currentTrack.toModel() else null, it.isPlaying, it.position, it.lastUpdate, it.volume)
                }
                "buffer_wait" -> Listentogether.BufferWaitPayload.parseFrom(p).let { LTEvent.BufferWait(it.trackId, it.waitingForList) }
                "buffer_complete" -> LTEvent.BufferComplete(Listentogether.BufferCompletePayload.parseFrom(p).trackId)
                "host_changed" -> Listentogether.HostChangedPayload.parseFrom(p).let {
                    updateRoom { s -> s.copy(hostId = it.newHostId, users = s.users.map { u -> u.copy(isHost = u.userId == it.newHostId) }) }
                    if (it.newHostId == myUserId) isHost = true
                    LTEvent.HostChanged(it.newHostId, it.newHostName)
                }
                "kicked" -> { clearLocal(); LTEvent.Kicked(Listentogether.KickedPayload.parseFrom(p).reason) }
                "reconnected" -> Listentogether.ReconnectedPayload.parseFrom(p).let {
                    myUserId = it.userId; isHost = it.isHost
                    val state = it.state.toModel(); _roomState.value = state
                    LTEvent.Reconnected(it.roomCode, it.userId, state, it.isHost)
                }
                "suggestion_received" -> Listentogether.SuggestionReceivedPayload.parseFrom(p).let {
                    LTEvent.SuggestionReceived(it.suggestionId, it.fromUserId, it.fromUsername, it.trackInfo.toModel())
                }
                "suggestion_approved" -> Listentogether.SuggestionApprovedPayload.parseFrom(p).let {
                    LTEvent.SuggestionApproved(it.suggestionId, if (it.hasTrackInfo()) it.trackInfo.toModel() else null)
                }
                "suggestion_rejected" -> Listentogether.SuggestionRejectedPayload.parseFrom(p).let { LTEvent.SuggestionRejected(it.suggestionId, it.reason) }
                "error" -> Listentogether.ErrorPayload.parseFrom(p).let { LTEvent.Error(it.code, it.message) }
                else -> null
            }
            ev?.let { emit(it) }
        } catch (_: Exception) {}
    }

    private fun applyPlayback(pb: Listentogether.PlaybackActionPayload) {
        val now = System.currentTimeMillis()
        updateRoom { s -> when (pb.action) {
            LTAction.PLAY -> s.copy(isPlaying = true, position = pb.position, lastUpdate = now)
            LTAction.PAUSE -> s.copy(isPlaying = false, position = pb.position, lastUpdate = now)
            LTAction.SEEK -> s.copy(position = pb.position, lastUpdate = now)
            LTAction.CHANGE_TRACK -> s.copy(currentTrack = if (pb.hasTrackInfo()) pb.trackInfo.toModel() else s.currentTrack, position = 0L, isPlaying = false, lastUpdate = now)
            LTAction.QUEUE_ADD -> if (pb.hasTrackInfo()) { val t = pb.trackInfo.toModel(); s.copy(queue = if (pb.insertNext) listOf(t) + s.queue else s.queue + t) } else s
            LTAction.QUEUE_REMOVE -> s.copy(queue = s.queue.filterNot { it.id == pb.trackId })
            LTAction.QUEUE_CLEAR -> s.copy(queue = emptyList())
            LTAction.SYNC_QUEUE -> s.copy(queue = pb.queueList.map { it.toModel() })
            LTAction.SET_VOLUME -> s.copy(volume = pb.volume)
            else -> s
        }}
    }

    private fun gzip(data: ByteArray): ByteArray { val b = ByteArrayOutputStream(); GZIPOutputStream(b).use { it.write(data) }; return b.toByteArray() }
    private fun ungzip(data: ByteArray): ByteArray = GZIPInputStream(data.inputStream()).use { it.readBytes() }
}

internal fun Listentogether.TrackInfo.toModel() = LTTrackInfo(id, title, artist, album, duration, thumbnail, suggestedBy)
internal fun LTTrackInfo.toProto(): Listentogether.TrackInfo = Listentogether.TrackInfo.newBuilder().setId(id).setTitle(title).setArtist(artist).setAlbum(album).setDuration(duration).setThumbnail(thumbnail).setSuggestedBy(suggestedBy).build()
internal fun Listentogether.UserInfo.toModel() = LTUserInfo(userId, username, isHost, isConnected)
internal fun Listentogether.RoomState.toModel() = LTRoomState(roomCode, hostId, usersList.map { it.toModel() }, if (hasCurrentTrack()) currentTrack.toModel() else null, isPlaying, position, lastUpdate, volume, queueList.map { it.toModel() })
