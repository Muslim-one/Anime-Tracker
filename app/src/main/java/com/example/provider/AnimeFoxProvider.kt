package com.example.provider

import com.example.data.model.AnimeDetails
import com.example.data.model.AnimeEpisode
import com.example.data.model.AnimeSearchResult
import com.example.data.model.VideoServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AnimeFoxProvider : AnimeProvider {
    override val id: String = "anime_fox"
    override val displayName: String = "AnimeFox (سريع)"

    private val db = listOf(
        AnimeSearchResult("fox_1", "One Piece", "One Piece", "ون بيس", "Wan Pis", "https://cdn.myanimelist.net/images/anime/4/19644.jpg", "مستمر", 1110, id),
        AnimeSearchResult("fox_2", "Bleach", "Bleach", "بليتش", "Burichu", "https://cdn.myanimelist.net/images/anime/3/40451.jpg", "مكتمل", 366, id),
        AnimeSearchResult("fox_3", "Hunter x Hunter", "Hunter x Hunter (2011)", "هنتر x هنتر", "Hanta Hanta", "https://cdn.myanimelist.net/images/anime/1337/138259.jpg", "مكتمل", 148, id),
        AnimeSearchResult("fox_4", "Solo Leveling", "Solo Leveling", "سولو ليفيلينغ", "Ore dake Heiwa", "https://cdn.myanimelist.net/images/anime/1160/141018.jpg", "مستمر", 12, id),
        AnimeSearchResult("fox_5", "My Hero Academia", "My Hero Academia", "أكاديميتي للأبطال", "Boku no Hero Academia", "https://cdn.myanimelist.net/images/anime/10/78745.jpg", "مستمر", 138, id)
    )

    override suspend fun search(query: String): List<AnimeSearchResult> = withContext(Dispatchers.Default) {
        if (query.isBlank()) return@withContext db
        return@withContext db.filter {
            it.title.contains(query, true) ||
            it.titleEnglish.contains(query, true) ||
            it.titleArabic.contains(query, true) ||
            it.titleRomanized.contains(query, true)
        }
    }

    override suspend fun getDetails(animeId: String): AnimeDetails? = withContext(Dispatchers.Default) {
        val searchItem = db.find { it.id == animeId } ?: return@withContext null
        val description = when (animeId) {
            "fox_1" -> "قصة العصر الذهبي للقراصنة، حيث يسعى مونكي دي لوفي للعثور على الكنز الأسطوري المسمى ون بيس ليصبح ملك القراصنة الجديد."
            "fox_2" -> "إيتشيغو كوروساكي، طالب في المدرسة الثانوية لديه القدرة على رؤية الأشباح، يكتسب قوى حاصد الأرواح (شينيغامي) لحماية قريته وأصدقائه."
            "fox_3" -> "غون فريكس يطمح في أن يصبح صياداً محترفاً للعثور على والده المفقود، مخوضاً اختبارات شديدة الصعوبة وتحديات مرعبة برفقة أصدقائه."
            "fox_4" -> "في عالم يواجه بوابات الوحوش، يستيقظ الصياد الأضعف سونغ جين وو بقوة فريدة تتيح له الارتقاء بمستواه بمفرده دون حدود."
            "fox_5" -> "إيزوكو ميدوريا فتى يولد بلا قدرات خارقة في مجتمع يمتلك فيه الجميع قوة فائقة، لكن يحالفه الحظ بمقابلة البطل الأسطوري أول مايت."
            else -> "وصف تفصيلي مميز مقدم من سيرفر أنمي فوكس السريع."
        }
        return@withContext AnimeDetails(
            id = searchItem.id,
            title = searchItem.title,
            description = description,
            type = "TV",
            year = when (animeId) {
                "fox_1" -> 1999
                "fox_2" -> 2004
                "fox_3" -> 2011
                "fox_4" -> 2024
                "fox_5" -> 2016
                else -> 2024
            },
            status = searchItem.status,
            episodesCount = searchItem.episodesCount,
            lastEpisode = searchItem.episodesCount,
            imageUrl = searchItem.imageUrl,
            providerId = id
        )
    }

    override suspend fun getEpisodes(animeId: String): List<AnimeEpisode> = withContext(Dispatchers.Default) {
        val searchItem = db.find { it.id == animeId } ?: return@withContext emptyList()
        val list = mutableListOf<AnimeEpisode>()
        for (i in 1..searchItem.episodesCount) {
            list.add(
                AnimeEpisode(
                    id = "${animeId}_$i",
                    number = i,
                    releaseDate = "حلقة $i - أنمي فوكس",
                    animeId = animeId,
                    providerId = id
                )
            )
        }
        return@withContext list
    }

    override suspend fun getWatchLinks(episodeId: String): List<VideoServer> {
        return listOf(
            VideoServer("سيرفر فوكس فاست 1080p", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
            VideoServer("سيرفر ميجا أب 720p", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"),
            VideoServer("سيرفر مباشر جودة منخفضة", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4")
        )
    }
}
