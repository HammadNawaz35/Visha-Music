package com.visha.musicplayer.data.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.*
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.visha.musicplayer.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MusicService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    lateinit var player: ExoPlayer
    private val handler = Handler(Looper.getMainLooper())
    private var sleepRunnable: Runnable? = null

    companion object {
        const val CMD_SHUFFLE     = "SHUFFLE"
        const val CMD_REPEAT      = "REPEAT"
        const val CMD_SPEED       = "SPEED"
        const val CMD_SLEEP       = "SLEEP"
        const val ARG_SPEED       = "speed"
        const val ARG_SLEEP_MINS  = "sleep_mins"
    }

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA).build(), true)
            .setHandleAudioBecomingNoisy(true)
            .build()

        val intent = PendingIntent.getActivity(this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(intent)
            .setCallback(Callback())
            .build()
    }

    override fun onGetSession(info: MediaSession.ControllerInfo) = mediaSession
    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!player.playWhenReady || player.mediaItemCount == 0) stopSelf()
    }

    override fun onDestroy() {
        cancelSleep()
        mediaSession?.run { player.release(); release() }
        super.onDestroy()
    }

    private fun cancelSleep() { sleepRunnable?.let { handler.removeCallbacks(it) }; sleepRunnable = null }

    inner class Callback : MediaSession.Callback {
        override fun onConnect(session: MediaSession, controller: MediaSession.ControllerInfo): MediaSession.ConnectionResult {
            val cmds = MediaSession.ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS.buildUpon()
                .add(SessionCommand(CMD_SHUFFLE, Bundle.EMPTY))
                .add(SessionCommand(CMD_REPEAT, Bundle.EMPTY))
                .add(SessionCommand(CMD_SPEED, Bundle.EMPTY))
                .add(SessionCommand(CMD_SLEEP, Bundle.EMPTY))
                .build()
            return MediaSession.ConnectionResult.accept(cmds, MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS)
        }

        override fun onCustomCommand(
            session: MediaSession, controller: MediaSession.ControllerInfo,
            cmd: SessionCommand, args: Bundle
        ): ListenableFuture<SessionResult> {
            when (cmd.customAction) {
                CMD_SHUFFLE -> player.shuffleModeEnabled = !player.shuffleModeEnabled
                CMD_REPEAT  -> player.repeatMode = when (player.repeatMode) {
                    Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
                    Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
                    else -> Player.REPEAT_MODE_OFF
                }
                CMD_SPEED   -> args.getFloat(ARG_SPEED, 1f).let { player.setPlaybackSpeed(it) }
                CMD_SLEEP   -> {
                    val mins = args.getInt(ARG_SLEEP_MINS, 0)
                    cancelSleep()
                    if (mins > 0) {
                        sleepRunnable = Runnable { player.pause() }
                            .also { handler.postDelayed(it, mins * 60_000L) }
                    }
                }
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }
}
