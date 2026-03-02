package com.visha.musicplayer.presentation.viewmodel

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.*
import com.google.common.util.concurrent.MoreExecutors
import com.visha.musicplayer.data.mediastore.DeleteResult
import com.visha.musicplayer.data.service.MusicService
import com.visha.musicplayer.domain.model.*
import com.visha.musicplayer.domain.repository.AudioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: AudioRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    val favoriteIds: StateFlow<Set<Long>> = repository.getFavoriteIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private val _deleteEvent = MutableSharedFlow<android.content.IntentSender>(extraBufferCapacity = 1)
    val deleteEvent: SharedFlow<android.content.IntentSender> = _deleteEvent.asSharedFlow()

    private val _toast = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val toast: SharedFlow<String> = _toast.asSharedFlow()

    private var controller: MediaController? = null
    private var controllerFuture: com.google.common.util.concurrent.ListenableFuture<MediaController>? = null

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.update { it.copy(isPlaying = isPlaying) }
        }
        override fun onMediaItemTransition(item: MediaItem?, reason: Int) {
            val ctrl = controller ?: return
            val idx  = ctrl.currentMediaItemIndex
            val queue = _state.value.queue
            if (idx in queue.indices) {
                val track = queue[idx]
                _state.update { it.copy(currentTrack = track, currentIndex = idx) }
                viewModelScope.launch(Dispatchers.IO) { repository.addToRecentlyPlayed(track.id) }
            }
        }
        override fun onShuffleModeEnabledChanged(shuffle: Boolean) {
            _state.update { it.copy(shuffleEnabled = shuffle) }
        }
        override fun onRepeatModeChanged(repeat: Int) {
            _state.update { it.copy(repeatMode = when (repeat) {
                Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                else -> RepeatMode.OFF
            })}
        }
        override fun onPlaybackStateChanged(playbackState: Int) {
            controller?.duration?.coerceAtLeast(0)?.let { dur ->
                _state.update { it.copy(duration = dur) }
            }
        }
    }

    init {
        connect()
        startProgressTicker()
    }

    private fun connect() {
        val token = SessionToken(context, ComponentName(context, MusicService::class.java))
        controllerFuture = MediaController.Builder(context, token).buildAsync()
        controllerFuture?.addListener({
            try {
                controller = controllerFuture?.get()
                controller?.addListener(listener)
            } catch (_: Exception) {}
        }, MoreExecutors.directExecutor())
    }

    private fun startProgressTicker() {
        viewModelScope.launch {
            while (isActive) {
                val ctrl = controller
                if (ctrl?.isPlaying == true) {
                    _state.update { it.copy(
                        progress = ctrl.currentPosition.coerceAtLeast(0),
                        duration = ctrl.duration.coerceAtLeast(0)
                    )}
                }
                delay(500)
            }
        }
    }

    fun playQueue(tracks: List<Track>, startIndex: Int = 0) {
        val ctrl = controller ?: return
        val items = tracks.map { t ->
            MediaItem.Builder().setUri(t.uri).setMediaId(t.id.toString())
                .setMediaMetadata(MediaMetadata.Builder()
                    .setTitle(t.title).setArtist(t.artist)
                    .setAlbumTitle(t.album).setArtworkUri(t.albumArtUri).build())
                .build()
        }
        _state.update { it.copy(queue = tracks, currentTrack = tracks.getOrNull(startIndex), currentIndex = startIndex) }
        ctrl.setMediaItems(items, startIndex, 0L)
        ctrl.prepare()
        ctrl.play()
    }

    /** Seek ExoPlayer to a specific queue index and start playing it immediately */
    fun seekToQueueIndex(index: Int) {
        val ctrl  = controller ?: return
        val queue = _state.value.queue
        if (index !in queue.indices) return
        ctrl.seekTo(index, 0L)
        if (!ctrl.isPlaying) ctrl.play()
        // onMediaItemTransition listener updates currentTrack / currentIndex
    }

    fun playPause()    { controller?.run { if (isPlaying) pause() else play() } }
    fun skipNext()     { controller?.seekToNextMediaItem() }
    fun skipPrevious() {
        val ctrl = controller ?: return
        if (ctrl.currentPosition > 3000) ctrl.seekTo(0) else ctrl.seekToPreviousMediaItem()
    }
    fun seekTo(ms: Long) {
        controller?.seekTo(ms)
        _state.update { it.copy(progress = ms) }
    }
    fun seekForward10() { controller?.let { seekTo((it.currentPosition + 10_000).coerceAtMost(it.duration)) } }
    fun seekBack10()    { controller?.let { seekTo((it.currentPosition - 10_000).coerceAtLeast(0)) } }
    fun toggleShuffle() { controller?.let { it.shuffleModeEnabled = !it.shuffleModeEnabled } }
    fun toggleRepeat() {
        val ctrl = controller ?: return
        ctrl.repeatMode = when (ctrl.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
            Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
            else                   -> Player.REPEAT_MODE_OFF
        }
    }
    fun setSpeed(speed: Float) {
        val args = Bundle().apply { putFloat(MusicService.ARG_SPEED, speed) }
        controller?.sendCustomCommand(SessionCommand(MusicService.CMD_SPEED, Bundle.EMPTY), args)
        controller?.setPlaybackSpeed(speed)
        _state.update { it.copy(playbackSpeed = speed) }
    }
    fun setSleepTimer(mins: Int) {
        val args = Bundle().apply { putInt(MusicService.ARG_SLEEP_MINS, mins) }
        controller?.sendCustomCommand(SessionCommand(MusicService.CMD_SLEEP, Bundle.EMPTY), args)
        _state.update { it.copy(sleepTimerMinutes = if (mins > 0) mins else null) }
    }
    fun toggleFavorite(trackId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            if (repository.isFavorite(trackId)) repository.removeFavorite(trackId)
            else repository.addFavorite(trackId)
        }
    }

    /**
     * Delete current track from MediaStore.
     * - On API 30+: creates a system delete request → emits intentSender
     * - On API 29:  catches RecoverableSecurityException → emits intentSender
     * - Below API 29: deletes directly
     * Does NOT force navigation — the UI (dialog → onBack) handles that.
     */
    fun deleteCurrentTrack() {
        val track = _state.value.currentTrack ?: return
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = repository.deleteTrack(track.id)) {
                is DeleteResult.Success -> {
                    _toast.emit("\"${track.title}\" deleted")
                    removeFromQueue(track.id)
                }
                is DeleteResult.NeedsConfirmation -> {
                    _deleteEvent.emit(result.intentSender)
                }
                is DeleteResult.Error -> {
                    _toast.emit("Delete failed: ${result.message}")
                }
            }
        }
    }

    fun onDeleteConfirmed(trackId: Long) {
        viewModelScope.launch(Dispatchers.IO) { removeFromQueue(trackId) }
    }

    private fun removeFromQueue(trackId: Long) {
        val queue = _state.value.queue.toMutableList()
        val idx   = queue.indexOfFirst { it.id == trackId }
        if (idx < 0) return
        queue.removeAt(idx)
        val newIdx = when {
            queue.isEmpty()   -> 0
            idx >= queue.size -> queue.size - 1
            else              -> idx
        }
        _state.update { it.copy(
            queue        = queue,
            currentIndex = newIdx,
            currentTrack = queue.getOrNull(newIdx)
        )}
        controller?.removeMediaItem(idx)
        if (queue.isNotEmpty()) controller?.seekTo(newIdx, 0)
    }

    override fun onCleared() {
        controller?.removeListener(listener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        super.onCleared()
    }
}