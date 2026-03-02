package com.visha.musicplayer.data.mediastore

import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.visha.musicplayer.domain.model.LoadState
import com.visha.musicplayer.domain.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** Result of a delete attempt – caller must handle pending intent if needed */
sealed class DeleteResult {
    object Success : DeleteResult()
    /** On Android 10+ Scoped Storage, user confirmation is required */
    data class NeedsConfirmation(val intentSender: IntentSender) : DeleteResult()
    data class Error(val message: String) : DeleteResult()
}

@Singleton
class MediaStoreHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val collection: Uri get() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

    // ── Loading-state flow ────────────────────────────────────────────────
    fun getAllTracksFlow(): Flow<LoadState<List<Track>>> = flow {
        emit(LoadState.Loading)
        try {
            val tracks = scanTracks()
            emit(LoadState.Success(tracks))
        } catch (e: Exception) {
            emit(LoadState.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun getAllTracks(): List<Track> = withContext(Dispatchers.IO) { scanTracks() }

    private fun scanTracks(): List<Track> {
        val tracks = mutableListOf<Track>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} > 10000"
        context.contentResolver.query(collection, projection, selection, null,
            "${MediaStore.Audio.Media.DATE_ADDED} DESC")?.use { cursor ->
            val idCol      = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol   = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol  = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol   = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durCol     = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dateCol    = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val contentUri = ContentUris.withAppendedId(collection, id)
                val albumArtUri = Uri.parse("content://media/external/audio/albumart/${cursor.getLong(albumIdCol)}")
                tracks += Track(
                    id = id,
                    title = cursor.getString(titleCol) ?: "Unknown",
                    artist = cursor.getString(artistCol) ?: "<unknown>",
                    album = cursor.getString(albumCol) ?: "Unknown Album",
                    albumArtUri = albumArtUri,
                    uri = contentUri,
                    duration = cursor.getLong(durCol),
                    dateAdded = cursor.getLong(dateCol)
                )
            }
        }
        return tracks
    }

    // ── Safe deletion for all API levels ─────────────────────────────────
    suspend fun deleteTrack(trackId: Long): DeleteResult = withContext(Dispatchers.IO) {
        val uri = ContentUris.withAppendedId(collection, trackId)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // API 30+ — must use createDeleteRequest and confirm with user
                val pendingIntent = MediaStore.createDeleteRequest(
                    context.contentResolver, listOf(uri)
                )
                DeleteResult.NeedsConfirmation(pendingIntent.intentSender)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // API 29 — RecoverableSecurityException
                try {
                    context.contentResolver.delete(uri, null, null)
                    DeleteResult.Success
                } catch (e: android.app.RecoverableSecurityException) {
                    DeleteResult.NeedsConfirmation(e.userAction.actionIntent.intentSender)
                }
            } else {
                // API 28 and below — direct delete
                val rows = context.contentResolver.delete(uri, null, null)
                if (rows > 0) DeleteResult.Success else DeleteResult.Error("Track not found")
            }
        } catch (e: Exception) {
            DeleteResult.Error(e.message ?: "Delete failed")
        }
    }
}
