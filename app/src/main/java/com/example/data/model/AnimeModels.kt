package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class AnimeSearchResult(
    val id: String,
    val title: String,
    val titleEnglish: String = "",
    val titleArabic: String = "",
    val titleRomanized: String = "",
    val imageUrl: String,
    val status: String,
    val episodesCount: Int,
    val providerId: String
)

data class AnimeDetails(
    val id: String,
    val title: String,
    val description: String,
    val type: String,
    val year: Int,
    val status: String,
    val episodesCount: Int,
    val lastEpisode: Int,
    val imageUrl: String,
    val providerId: String
)

data class AnimeEpisode(
    val id: String,
    val number: Int,
    val releaseDate: String,
    val animeId: String,
    val providerId: String
)

data class VideoServer(
    val name: String,
    val videoUrl: String
)

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val animeId: String,
    val animeTitle: String,
    val imageUrl: String,
    val episodeId: String,
    val episodeNumber: Int,
    val timestamp: Long,
    val positionMs: Long,
    val durationMs: Long,
    val percentWatched: Float
)

@Entity(tableName = "favorites")
data class FavoriteAnimeEntity(
    @PrimaryKey val animeId: String,
    val title: String,
    val imageUrl: String,
    val lastEpisodeCount: Int,
    val hasNewEpisode: Boolean,
    val lastWatchedEpisodeNumber: Int,
    val lastWatchedTimestamp: Long,
    val providerId: String
)

@Entity(tableName = "provider_config")
data class ProviderConfigEntity(
    @PrimaryKey val providerId: String,
    val displayName: String,
    val isEnabled: Boolean,
    val priority: Int
)
