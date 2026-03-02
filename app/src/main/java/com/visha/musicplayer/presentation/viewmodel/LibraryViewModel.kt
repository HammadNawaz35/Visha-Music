package com.visha.musicplayer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.visha.musicplayer.domain.model.*
import com.visha.musicplayer.domain.repository.AudioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortOrder { DATE_ADDED, TITLE_ASC, TITLE_DESC, ARTIST, DURATION }

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: AudioRepository
) : ViewModel() {

    // ── Loading state ──────────────────────────────────────────────────────
    private val _loadState = MutableStateFlow<LoadState<List<Track>>>(LoadState.Loading)
    val loadState: StateFlow<LoadState<List<Track>>> = _loadState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _allTracks = MutableStateFlow<List<Track>>(emptyList())

    // ── Sort & filter ──────────────────────────────────────────────────────
    private val _sortOrder = MutableStateFlow(SortOrder.DATE_ADDED)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    val sortedTracks: StateFlow<List<Track>> = combine(_allTracks, _sortOrder) { tracks, order ->
        when (order) {
            SortOrder.DATE_ADDED  -> tracks
            SortOrder.TITLE_ASC   -> tracks.sortedBy { it.title.lowercase() }
            SortOrder.TITLE_DESC  -> tracks.sortedByDescending { it.title.lowercase() }
            SortOrder.ARTIST      -> tracks.sortedBy { it.artist.lowercase() }
            SortOrder.DURATION    -> tracks.sortedByDescending { it.duration }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Library search ─────────────────────────────────────────────────────
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val isSearchExpanded = MutableStateFlow(false)

    val searchResults: StateFlow<List<Track>> = combine(_allTracks, _searchQuery) { tracks, q ->
        if (q.isBlank()) emptyList()
        else tracks.filter {
            it.title.contains(q, ignoreCase = true) ||
            it.artist.contains(q, ignoreCase = true) ||
            it.album.contains(q, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Favorites ──────────────────────────────────────────────────────────
    val favoriteTracks: StateFlow<List<Track>> = repository.getFavoriteTracks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val favoriteIds: StateFlow<Set<Long>> = repository.getFavoriteIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // ── Playlists ──────────────────────────────────────────────────────────
    val playlists: StateFlow<List<Playlist>> = repository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Recently Played ────────────────────────────────────────────────────
    private val _recentIds = MutableStateFlow<List<Long>>(emptyList())
    val recentlyPlayedTracks: StateFlow<List<Track>> = combine(_allTracks, _recentIds) { tracks, ids ->
        val map = tracks.associateBy { it.id }
        ids.mapNotNull { map[it] }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Recent searches ────────────────────────────────────────────────────
    val recentSearches: StateFlow<List<String>> = repository.getRecentSearches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Playlist multi-select state ────────────────────────────────────────
    private val _selectedTrackIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedTrackIds: StateFlow<Set<Long>> = _selectedTrackIds.asStateFlow()

    init {
        loadTracks()
        viewModelScope.launch {
            repository.getRecentlyPlayedIds().collect { _recentIds.value = it }
        }
    }

    fun loadTracks() {
        viewModelScope.launch {
            repository.getTracksFlow().collect { state ->
                _loadState.value = state
                if (state is LoadState.Success) {
                    val favoriteSet = favoriteIds.value
                    _allTracks.value = state.data.map { it.copy(isFavorite = it.id in favoriteSet) }
                }
            }
        }
    }

    // Pull-to-refresh
    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _allTracks.value = repository.getAllTracks()
            _isRefreshing.value = false
        }
    }

    fun setSortOrder(order: SortOrder) { _sortOrder.value = order }
    fun setSearchQuery(q: String) { _searchQuery.value = q }
    fun toggleSearch() { isSearchExpanded.value = !isSearchExpanded.value; if (!isSearchExpanded.value) _searchQuery.value = "" }

    fun toggleFavorite(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            if (repository.isFavorite(id)) repository.removeFavorite(id) else repository.addFavorite(id)
        }
    }

    fun saveRecentSearch(q: String) { if (q.isNotBlank()) viewModelScope.launch { repository.addRecentSearch(q) } }
    fun removeRecentSearch(q: String) { viewModelScope.launch { repository.removeRecentSearch(q) } }
    fun clearRecentSearches() { viewModelScope.launch { repository.clearRecentSearches() } }

    // Playlist CRUD
    fun createPlaylist(name: String) { viewModelScope.launch { repository.createPlaylist(name) } }
    fun deletePlaylist(id: Long) { viewModelScope.launch { repository.deletePlaylist(id) } }

    // Multi-select for playlist track picker
    fun toggleTrackSelection(id: Long) {
        _selectedTrackIds.update { current ->
            if (id in current) current - id else current + id
        }
    }
    fun selectAllTracks() { _selectedTrackIds.value = _allTracks.value.map { it.id }.toSet() }
    fun clearSelection() { _selectedTrackIds.value = emptySet() }

    fun addSelectedTracksToPlaylist(playlistId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addTracksToPlaylist(playlistId, _selectedTrackIds.value.toList())
            clearSelection()
        }
    }
}
