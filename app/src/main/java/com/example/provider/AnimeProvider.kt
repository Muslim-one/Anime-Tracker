package com.example.provider

import com.example.data.model.AnimeDetails
import com.example.data.model.AnimeEpisode
import com.example.data.model.AnimeSearchResult
import com.example.data.model.VideoServer

interface AnimeProvider {
    val id: String
    val displayName: String

    suspend fun search(query: String): List<AnimeSearchResult>
    suspend fun getDetails(animeId: String): AnimeDetails?
    suspend fun getEpisodes(animeId: String): List<AnimeEpisode>
    suspend fun getWatchLinks(episodeId: String): List<VideoServer>
}
