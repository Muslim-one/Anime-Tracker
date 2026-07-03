package com.example.provider

import com.example.data.model.AnimeSearchResult
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.Locale

class ProviderManager {
    val providers: List<AnimeProvider> = listOf(
        MALJikanProvider(),
        AnimeFoxProvider()
    )

    fun getProvider(providerId: String): AnimeProvider? {
        return providers.find { it.id == providerId }
    }

    suspend fun searchAll(
        query: String,
        enabledProviders: Map<String, Boolean>,
        priorityList: List<String>
    ): List<AnimeSearchResult> = coroutineScope {
        // Filter out disabled providers
        val activeProviders = providers.filter { enabledProviders[it.id] ?: true }
        
        // Sort active providers by user preferences
        val sortedProviders = activeProviders.sortedBy { provider ->
            val index = priorityList.indexOf(provider.id)
            if (index == -1) 99 else index
        }

        // Query active providers in parallel
        val deferred = sortedProviders.map { provider ->
            async {
                try {
                    provider.search(query)
                } catch (e: Exception) {
                    emptyList<AnimeSearchResult>()
                }
            }
        }

        val allResults = deferred.awaitAll().flatten()

        // Remove duplicate items (matching same title or normalized name, keeping the one from higher priority provider)
        val uniqueResults = mutableListOf<AnimeSearchResult>()
        for (result in allResults) {
            val isDuplicate = uniqueResults.any { existing ->
                normalize(existing.title) == normalize(result.title) ||
                (existing.titleArabic.isNotEmpty() && result.titleArabic.isNotEmpty() && normalize(existing.titleArabic) == normalize(result.titleArabic))
            }
            if (!isDuplicate) {
                uniqueResults.add(result)
            }
        }

        // Sort results by matching quality (exact matches or starts-with matches come first)
        val queryLower = query.lowercase(Locale.getDefault()).trim()
        val sortedResults = uniqueResults.sortedWith { o1, o2 ->
            val match1 = getMatchScore(o1, queryLower)
            val match2 = getMatchScore(o2, queryLower)
            match2.compareTo(match1) // Descending score
        }

        return@coroutineScope sortedResults
    }

    private fun getMatchScore(anime: AnimeSearchResult, query: String): Int {
        if (query.isEmpty()) return 0
        var score = 0
        val t1 = anime.title.lowercase(Locale.getDefault())
        val t2 = anime.titleEnglish.lowercase(Locale.getDefault())
        val t3 = anime.titleArabic.lowercase(Locale.getDefault())
        val t4 = anime.titleRomanized.lowercase(Locale.getDefault())

        if (t1 == query || t2 == query || t3 == query || t4 == query) {
            score += 100 // Perfect match
        } else if (t1.startsWith(query) || t2.startsWith(query) || t3.startsWith(query) || t4.startsWith(query)) {
            score += 50 // Prefix match
        } else if (t1.contains(query) || t2.contains(query) || t3.contains(query) || t4.contains(query)) {
            score += 20 // Contains match
        }
        return score
    }

    private fun normalize(str: String): String {
        return str.lowercase(Locale.getDefault())
            .replace(" ", "")
            .replace("-", "")
            .replace(":", "")
            .replace("_", "")
            .trim()
    }
}
