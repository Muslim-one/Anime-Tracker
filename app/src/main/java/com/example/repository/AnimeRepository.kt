package com.example.repository

import android.content.Context
import android.util.Log
import com.example.data.database.AnimeDao
import com.example.data.model.AnimeDetails
import com.example.data.model.AnimeEpisode
import com.example.data.model.AnimeSearchResult
import com.example.data.model.FavoriteAnimeEntity
import com.example.data.model.ProviderConfigEntity
import com.example.data.model.VideoServer
import com.example.data.model.WatchHistoryEntity
import com.example.provider.ProviderManager
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await

class AnimeRepository(
    private val context: Context,
    private val dao: AnimeDao,
    val providerManager: ProviderManager
) {
    private var firestore: FirebaseFirestore? = null

    init {
        // Safe Firebase initialization check
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                firestore = FirebaseFirestore.getInstance()
                Log.d("AnimeRepository", "Firebase Firestore initialized successfully!")
            } else {
                Log.w("AnimeRepository", "Firebase is not initialized. Running in Offline Room-only mode.")
            }
        } catch (e: Exception) {
            Log.e("AnimeRepository", "Error checking Firebase: ${e.message}. Fallback to Room-only mode.")
        }
    }

    // --- Search & Providers ---
    suspend fun searchAll(query: String): List<AnimeSearchResult> {
        val configs = dao.getProviderConfigs().firstOrNull() ?: emptyList()
        val enabledMap = configs.associate { it.providerId to it.isEnabled }
        val priorityList = configs.sortedBy { it.priority }.map { it.providerId }
        return providerManager.searchAll(query, enabledMap, priorityList)
    }

    suspend fun getAnimeDetails(animeId: String, providerId: String): AnimeDetails? {
        val provider = providerManager.getProvider(providerId) ?: return null
        return provider.getDetails(animeId)
    }

    suspend fun getEpisodes(animeId: String, providerId: String): List<AnimeEpisode> {
        val provider = providerManager.getProvider(providerId) ?: return emptyList()
        return provider.getEpisodes(animeId)
    }

    suspend fun getWatchLinks(episodeId: String, providerId: String): List<VideoServer> {
        val provider = providerManager.getProvider(providerId) ?: return emptyList()
        return provider.getWatchLinks(episodeId)
    }

    // --- Favorites (Room + Firebase) ---
    val favorites: Flow<List<FavoriteAnimeEntity>> = dao.getFavorites()

    fun isFavorite(animeId: String): Flow<Boolean> = dao.isFavorite(animeId)

    suspend fun addFavorite(fav: FavoriteAnimeEntity) {
        dao.insertFavorite(fav)
        // Sync to Firebase in background if available
        firestore?.let { db ->
            try {
                db.collection("favorites")
                    .document(fav.animeId)
                    .set(fav)
                    .addOnSuccessListener { Log.d("AnimeRepository", "Favorite synced to Firestore!") }
                    .addOnFailureListener { e -> Log.e("AnimeRepository", "Failed to sync favorite: ${e.message}") }
            } catch (e: Exception) {
                Log.e("AnimeRepository", "Firestore write crash: ${e.message}")
            }
        }
    }

    suspend fun removeFavorite(animeId: String) {
        dao.deleteFavorite(animeId)
        firestore?.let { db ->
            try {
                db.collection("favorites")
                    .document(animeId)
                    .delete()
                    .addOnSuccessListener { Log.d("AnimeRepository", "Favorite deleted from Firestore!") }
            } catch (e: Exception) {
                Log.e("AnimeRepository", "Firestore delete crash: ${e.message}")
            }
        }
    }

    // --- Watch History (Room + Firebase) ---
    val watchHistory: Flow<List<WatchHistoryEntity>> = dao.getWatchHistory()

    fun getWatchHistoryById(animeId: String): Flow<WatchHistoryEntity?> = dao.getWatchHistoryById(animeId)

    suspend fun saveWatchHistory(history: WatchHistoryEntity) {
        dao.insertWatchHistory(history)
        firestore?.let { db ->
            try {
                db.collection("watch_history")
                    .document(history.animeId)
                    .set(history)
                    .addOnSuccessListener { Log.d("AnimeRepository", "Watch history synced to Firestore!") }
            } catch (e: Exception) {
                Log.e("AnimeRepository", "Firestore history write crash: ${e.message}")
            }
        }
    }

    suspend fun removeWatchHistory(animeId: String) {
        dao.deleteWatchHistory(animeId)
        firestore?.let { db ->
            try {
                db.collection("watch_history")
                    .document(animeId)
                    .delete()
            } catch (e: Exception) {
                Log.e("AnimeRepository", "Firestore history delete crash: ${e.message}")
            }
        }
    }

    // --- Provider Configurations ---
    val providerConfigs: Flow<List<ProviderConfigEntity>> = dao.getProviderConfigs()

    suspend fun saveProviderConfigs(configs: List<ProviderConfigEntity>) {
        dao.insertProviderConfigs(configs)
    }

    suspend fun updateProviderConfig(config: ProviderConfigEntity) {
        dao.updateProviderConfig(config)
    }

    // Sync Settings / Favorites from Firestore to Room (Pull logic)
    suspend fun syncFromFirestore() {
        val db = firestore ?: return
        try {
            // Pull Favorites
            db.collection("favorites")
                .get()
                .addOnSuccessListener { snapshot ->
                    val favList = snapshot.toObjects(FavoriteAnimeEntity::class.java)
                    for (fav in favList) {
                        // Insert into Room
                        // We run it on a separate scope or coroutine
                        // dao.insertFavorite(fav)
                    }
                }

            // Pull Watch History
            db.collection("watch_history")
                .get()
                .addOnSuccessListener { snapshot ->
                    val histList = snapshot.toObjects(WatchHistoryEntity::class.java)
                    for (hist in histList) {
                        // dao.insertWatchHistory(hist)
                    }
                }
        } catch (e: Exception) {
            Log.e("AnimeRepository", "Pull Firestore error: ${e.message}")
        }
    }
}
