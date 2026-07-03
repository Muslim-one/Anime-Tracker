package com.example.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.FavoriteAnimeEntity
import com.example.repository.AnimeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

object AnimeNotificationManager {
    private const val CHANNEL_ID = "anime_updates_channel"
    private const val NOTIFICATION_ID_BASE = 1000

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "تحديثات الأنمي"
            val descriptionText = "إشعارات نزول حلقات جديدة للأنمي المفضل لديك"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showEpisodeNotification(
        context: Context,
        animeId: String,
        animeTitle: String,
        episodeNumber: Int,
        providerId: String
    ) {
        // Build pending intent to launch MainActivity and open details
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("OPEN_ANIME_ID", animeId)
            putExtra("OPEN_PROVIDER_ID", providerId)
            putExtra("PLAY_EPISODE_NUM", episodeNumber)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            animeId.hashCode() + episodeNumber,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Using standard systems vector icon or simple application drawable
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle("حلقة جديدة نزلت! 🎉")
            .setContentText("الحلقة $episodeNumber من أنمي $animeTitle متوفرة الآن للمشاهدة.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.ic_menu_view,
                "مشاهدة الآن",
                pendingIntent
            )

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(NOTIFICATION_ID_BASE + animeId.hashCode(), builder.build())
            } else {
                Log.w("AnimeNotification", "Cannot post notification: Permission Denied")
            }
        }
    }

    // A background check simulation coroutine that periodically fetches updates for favorites
    fun startPeriodicSyncSimulation(context: Context, repository: AnimeRepository, intervalHours: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                // Check intervals: 1, 3, 6, 12, 24 hours.
                val delayMs = intervalHours * 60 * 60 * 1000L
                Log.d("AnimeSync", "Periodic update check running, interval: $intervalHours hours.")
                
                try {
                    // Fetch favorites
                    val favList = repository.favorites.firstOrNull() ?: emptyList()
                    for (favorite in favList) {
                        // Check if provider has a newer episode count
                        val details = repository.getAnimeDetails(favorite.animeId, favorite.providerId)
                        if (details != null && details.episodesCount > favorite.lastEpisodeCount) {
                            // Update favorite
                            val updatedFav = favorite.copy(
                                lastEpisodeCount = details.episodesCount,
                                hasNewEpisode = true
                            )
                            repository.addFavorite(updatedFav)

                            // Show Notification
                            showEpisodeNotification(
                                context = context,
                                animeId = favorite.animeId,
                                animeTitle = favorite.title,
                                episodeNumber = details.episodesCount,
                                providerId = favorite.providerId
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AnimeSync", "Error in periodic sync simulation: ${e.message}")
                }

                // Simulate/delay. In actual production, WorkManager is used. We can use delay for the active session.
                // In background it remains, but during active app usage, delay works beautifully.
                // Since this is standard simulation, let's keep it robust.
                delay(delayMs)
            }
        }
    }
}
