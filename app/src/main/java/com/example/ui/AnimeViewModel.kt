package com.example.ui

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.database.AnimeDatabase
import com.example.data.model.AnimeDetails
import com.example.data.model.AnimeEpisode
import com.example.data.model.AnimeSearchResult
import com.example.data.model.FavoriteAnimeEntity
import com.example.data.model.ProviderConfigEntity
import com.example.data.model.VideoServer
import com.example.data.model.WatchHistoryEntity
import com.example.provider.ProviderManager
import com.example.repository.AnimeRepository
import com.example.worker.AnimeNotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AnimeViewModel(application: Application) : AndroidViewModel(application) {
    private val database: AnimeDatabase = Room.databaseBuilder(
        application,
        AnimeDatabase::class.java,
        "anime_tracker_db"
    ).build()

    private val repository = AnimeRepository(
        context = application,
        dao = database.animeDao(),
        providerManager = ProviderManager()
    )

    // --- State variables ---
    var searchQuery by mutableStateOf("")
    var isSearching by mutableStateOf(false)
    private val _searchResults = MutableStateFlow<List<AnimeSearchResult>>(emptyList())
    val searchResults: StateFlow<List<AnimeSearchResult>> = _searchResults.asStateFlow()

    // Details State
    var selectedAnime by mutableStateOf<AnimeDetails?>(null)
    var isLoadingDetails by mutableStateOf(false)
    var episodesList by mutableStateOf<List<AnimeEpisode>>(emptyList())

    // Watch Player State
    var activeEpisode by mutableStateOf<AnimeEpisode?>(null)
    var availableServers by mutableStateOf<List<VideoServer>>(emptyList())
    var currentServer by mutableStateOf<VideoServer?>(null)
    var videoPlaybackPercent by mutableStateOf(0f)
    var videoPlaybackTimeMs by mutableStateOf(0L)

    // Global Settings Saved locally in Compose state/Room configuration
    var isDarkMode by mutableStateOf(true) // Default Dark Mode for Anime Lovers
    var syncIntervalHours by mutableStateOf(3) // Default 3 hours sync

    // Favorites & Watch History from Room Database
    val favorites: StateFlow<List<FavoriteAnimeEntity>> = repository.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchHistory: StateFlow<List<WatchHistoryEntity>> = repository.watchHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val providerConfigs: StateFlow<List<ProviderConfigEntity>> = repository.providerConfigs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Create Notification Channel
        AnimeNotificationManager.createNotificationChannel(application)

        // Prep provider configs if empty
        viewModelScope.launch {
            repository.providerConfigs.collect { configs ->
                if (configs.isEmpty()) {
                    val defaultConfigs = listOf(
                        ProviderConfigEntity("jikan_mal", "MyAnimeList (Jikan)", true, 0),
                        ProviderConfigEntity("anime_fox", "AnimeFox (سريع)", true, 1)
                    )
                    repository.saveProviderConfigs(defaultConfigs)
                }
            }
        }

        // Trigger Periodic Update Check Simulation
        AnimeNotificationManager.startPeriodicSyncSimulation(application, repository, syncIntervalHours)
    }

    // --- Actions ---
    fun search() {
        if (searchQuery.isBlank()) return
        isSearching = true
        viewModelScope.launch {
            try {
                val results = repository.searchAll(searchQuery)
                _searchResults.value = results
            } catch (e: Exception) {
                Log.e("AnimeViewModel", "Search failure: ${e.message}")
            } finally {
                isSearching = false
            }
        }
    }

    fun loadAnimeDetails(animeId: String, providerId: String) {
        isLoadingDetails = true
        selectedAnime = null
        episodesList = emptyList()
        activeEpisode = null
        availableServers = emptyList()
        currentServer = null

        viewModelScope.launch {
            try {
                val details = repository.getAnimeDetails(animeId, providerId)
                selectedAnime = details
                if (details != null) {
                    val episodes = repository.getEpisodes(animeId, providerId)
                    episodesList = episodes
                }
            } catch (e: Exception) {
                Log.e("AnimeViewModel", "Failed loading details: ${e.message}")
            } finally {
                isLoadingDetails = false
            }
        }
    }

    fun selectEpisode(episode: AnimeEpisode) {
        activeEpisode = episode
        viewModelScope.launch {
            try {
                val servers = repository.getWatchLinks(episode.id, episode.providerId)
                availableServers = servers
                
                // Read last used server if saved, or default to first
                // For simulation, we'll pick the first server
                currentServer = servers.firstOrNull()
                
                // Reset playback state
                videoPlaybackPercent = 0f
                videoPlaybackTimeMs = 0L
            } catch (e: Exception) {
                Log.e("AnimeViewModel", "Failed getting watch links: ${e.message}")
            }
        }
    }

    fun setServer(server: VideoServer) {
        currentServer = server
    }

    fun updatePlaybackProgress(percent: Float, timeMs: Long) {
        videoPlaybackPercent = percent
        videoPlaybackTimeMs = timeMs
        
        val currentAnime = selectedAnime ?: return
        val currentEp = activeEpisode ?: return

        // Save progress to database
        viewModelScope.launch {
            val history = WatchHistoryEntity(
                animeId = currentAnime.id,
                animeTitle = currentAnime.title,
                imageUrl = currentAnime.imageUrl,
                episodeId = currentEp.id,
                episodeNumber = currentEp.number,
                timestamp = System.currentTimeMillis(),
                positionMs = timeMs,
                durationMs = 1200000L, // 20 minutes representation
                percentWatched = percent
            )
            repository.saveWatchHistory(history)
        }
    }

    fun toggleFavorite(animeId: String) {
        val details = selectedAnime ?: return
        viewModelScope.launch {
            val isFav = favorites.value.any { it.animeId == animeId }
            if (isFav) {
                repository.removeFavorite(animeId)
            } else {
                val favorite = FavoriteAnimeEntity(
                    animeId = details.id,
                    title = details.title,
                    imageUrl = details.imageUrl,
                    lastEpisodeCount = details.episodesCount,
                    hasNewEpisode = false,
                    lastWatchedEpisodeNumber = 0,
                    lastWatchedTimestamp = System.currentTimeMillis(),
                    providerId = details.providerId
                )
                repository.addFavorite(favorite)
            }
        }
    }

    fun toggleProvider(providerId: String) {
        val config = providerConfigs.value.find { it.providerId == providerId } ?: return
        viewModelScope.launch {
            repository.updateProviderConfig(config.copy(isEnabled = !config.isEnabled))
        }
    }

    fun updateProviderOrder(providerId: String, newPriority: Int) {
        val config = providerConfigs.value.find { it.providerId == providerId } ?: return
        viewModelScope.launch {
            repository.updateProviderConfig(config.copy(priority = newPriority))
        }
    }

    fun updateSyncInterval(hours: Int) {
        syncIntervalHours = hours
        // Re-trigger manager checks with new interval
        AnimeNotificationManager.startPeriodicSyncSimulation(getApplication(), repository, hours)
    }

    fun clearHistory(animeId: String) {
        viewModelScope.launch {
            repository.removeWatchHistory(animeId)
        }
    }
}
