package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.AnimeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Beautiful Bento Grid Palette matching user theme specifications
val PrimaryAnime = Color(0xFF6750A4)
val SurfaceDark = Color(0xFF1C1B1F)

val BentoSkyBg = Color(0xFFE0F2FE)
val BentoSkyBorder = Color(0xFFBAE6FD)
val BentoSkyText = Color(0xFF0369A1)

val BentoLavenderBg = Color(0xFFF3E8FF)
val BentoLavenderBorder = Color(0xFFE9D5FF)
val BentoLavenderText = Color(0xFF7E22CE)

val BentoAmberBg = Color(0xFFFEF3C7)
val BentoAmberBorder = Color(0xFFFDE68A)
val BentoAmberText = Color(0xFFB45309)

val BentoRoseBg = Color(0xFFFFE4E6)
val BentoRoseBorder = Color(0xFFFECDD3)
val BentoRoseText = Color(0xFFBE123C)

@Composable
fun MainSearchScreen(
    viewModel: AnimeViewModel,
    onNavigateToDetails: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchResults by viewModel.searchResults.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Large Premium Search Box (Bento Grid Header Card)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFF6750A4), Color(0xFF9E82F0))))
                .padding(24.dp)
                .testTag("search_hero_card")
        ) {
            Column {
                Text(
                    text = "ابحث عن أنميك المفضل 🔍",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "نظام بحث فوري يبحث في جميع المواقع والمصادر المفعلة في نفس الوقت",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Search Bar Input
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = viewModel.searchQuery,
                        onValueChange = { viewModel.searchQuery = it },
                        placeholder = { Text("مثال: ون بيس أو One Piece", color = Color.Gray, fontSize = 14.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_text_input"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryAnime)
                            .clickable { viewModel.search() }
                            .testTag("search_submit_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (viewModel.isSearching) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "بحث", tint = Color.White)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Search Results list / grid
        if (viewModel.isSearching) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = PrimaryAnime)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("جاري البحث وتصفية المكررات...", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else if (searchResults.isEmpty()) {
            // Empty State illustration / recommendations (Bento Grid Recommendation)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Generated Hero Banner Illustration
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.anime_tracker_hero),
                    contentDescription = "متابع الأنمي",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )

                Text(
                    text = "أنميات مقترحة شائعة 🔥",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Bento Recommendation Grid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BentoRecommendationCard(
                        title = "ون بيس (One Piece)",
                        tag = "مستمر",
                        colorBg = BentoSkyBg,
                        colorText = BentoSkyText,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.searchQuery = "One Piece"
                            viewModel.search()
                        }
                    )
                    BentoRecommendationCard(
                        title = "هجوم العمالقة",
                        tag = "مكتمل",
                        colorBg = BentoRoseBg,
                        colorText = BentoRoseText,
                        modifier = Modifier.weight(1.2f),
                        onClick = {
                            viewModel.searchQuery = "Attack on Titan"
                            viewModel.search()
                        }
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BentoRecommendationCard(
                        title = "قاتل الشياطين",
                        tag = "مكتمل",
                        colorBg = BentoLavenderBg,
                        colorText = BentoLavenderText,
                        modifier = Modifier.weight(1.3f),
                        onClick = {
                            viewModel.searchQuery = "Demon Slayer"
                            viewModel.search()
                        }
                    )
                    BentoRecommendationCard(
                        title = "سولو ليفيلينغ",
                        tag = "حلقة جديدة",
                        colorBg = BentoAmberBg,
                        colorText = BentoAmberText,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.searchQuery = "Solo Leveling"
                            viewModel.search()
                        }
                    )
                }
            }
        } else {
            // Results list in asymmetric elegant layout (Bento Grid cards)
            Text(
                text = "نتائج البحث (${searchResults.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(searchResults) { anime ->
                    BentoAnimeResultCard(
                        anime = anime,
                        onClick = { onNavigateToDetails(anime.id, anime.providerId) }
                    )
                }
            }
        }
    }
}

@Composable
fun BentoRecommendationCard(
    title: String,
    tag: String,
    colorBg: Color,
    colorText: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(130.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(colorBg)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(text = tag, color = colorText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colorText,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun BentoAnimeResultCard(
    anime: AnimeSearchResult,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .testTag("anime_card_${anime.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Anime Image
            AsyncImage(
                model = anime.imageUrl,
                contentDescription = anime.title,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Text Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = anime.titleArabic.ifEmpty { anime.title },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = anime.titleEnglish,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (anime.status == "مستمر") BentoSkyBg else BentoLavenderBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = anime.status,
                            color = if (anime.status == "مستمر") BentoSkyText else BentoLavenderText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Episodes count
                    Text(
                        text = "📺 ${anime.episodesCount} حلقة",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            // Arrow Icon
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "التفاصيل",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

// --- Anime Details Screen ---
@Composable
fun AnimeDetailsScreen(
    viewModel: AnimeViewModel,
    animeId: String,
    providerId: String,
    onBack: () -> Unit,
    onWatchEpisode: (AnimeEpisode) -> Unit,
    modifier: Modifier = Modifier
) {
    val details = viewModel.selectedAnime
    val episodes = viewModel.episodesList
    val favorites by viewModel.favorites.collectAsState()
    val isFav = favorites.any { it.animeId == animeId }
    val context = LocalContext.current

    LaunchedEffect(animeId, providerId) {
        viewModel.loadAnimeDetails(animeId, providerId)
    }

    if (viewModel.isLoadingDetails) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryAnime)
        }
    } else if (details == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("عذراً، فشل تحميل التفاصيل 😢")
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onBack) {
                    Text("الرجوع للخلف")
                }
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Large Image Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                AsyncImage(
                    model = details.imageUrl,
                    contentDescription = details.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Dark Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                            )
                        )
                )

                // Top Actions Layer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .testTag("detail_back_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { 
                                viewModel.toggleFavorite(animeId)
                                val msg = if (isFav) "تمت الإزالة من المفضلة" else "تمت الإضافة إلى المفضلة ❤️"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .testTag("detail_fav_button")
                        ) {
                            Icon(
                                imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "المفضلة",
                                tint = if (isFav) Color.Red else Color.White
                            )
                        }
                    }
                }

                // Title and basic info over the hero image bottom
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(text = details.type, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = details.title,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Bento Grid Details Cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Info block row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Status Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(BentoSkyBg)
                            .border(1.dp, BentoSkyBorder, RoundedCornerShape(20.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                            Text("حالة العمل", fontSize = 11.sp, color = BentoSkyText.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                            Text(details.status, fontSize = 15.sp, color = BentoSkyText, fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    // Episodes Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(BentoLavenderBg)
                            .border(1.dp, BentoLavenderBorder, RoundedCornerShape(20.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                            Text("عدد الحلقات", fontSize = 11.sp, color = BentoLavenderText.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                            Text("${details.episodesCount} حلقة", fontSize = 15.sp, color = BentoLavenderText, fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    // Year Card
                    Box(
                        modifier = Modifier
                            .weight(0.8f)
                            .height(80.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(BentoAmberBg)
                            .border(1.dp, BentoAmberBorder, RoundedCornerShape(20.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                            Text("السنة", fontSize = 11.sp, color = BentoAmberText.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                            Text(details.year.toString(), fontSize = 15.sp, color = BentoAmberText, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                // Description Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "قصة الأنمي 📖",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = details.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            lineHeight = 20.sp
                        )
                    }
                }

                // Episodes Segment Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "قائمة الحلقات 📺",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${episodes.size} حلقة مضافة",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // Episodes Grid list
                if (episodes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("جاري تحميل قائمة الحلقات...", color = Color.Gray)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        episodes.forEach { episode ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                    .clickable { onWatchEpisode(episode) }
                                    .padding(16.dp)
                                    .testTag("episode_${episode.number}"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(PrimaryAnime.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = episode.number.toString(),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryAnime
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = "الحلقة رقم ${episode.number}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = episode.releaseDate,
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                Button(
                                    onClick = { onWatchEpisode(episode) },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAnime),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "مشاهدة", modifier = Modifier.size(14.dp))
                                        Text("مشاهدة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Watch/Video Player Screen ---
@Composable
fun WatchScreen(
    viewModel: AnimeViewModel,
    onBackToDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeEp = viewModel.activeEpisode
    val currentAnime = viewModel.selectedAnime
    val servers = viewModel.availableServers
    val selectedServer = viewModel.currentServer
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isPlaying by remember { mutableStateOf(true) }
    var streamLoading by remember { mutableStateOf(false) }

    // Simulation of continuous progress save
    LaunchedEffect(isPlaying, selectedServer) {
        if (isPlaying && selectedServer != null) {
            streamLoading = true
            delay(1200) // load stream
            streamLoading = false
            
            while (isPlaying) {
                delay(2000)
                var nextPercent = viewModel.videoPlaybackPercent + 0.05f
                if (nextPercent >= 1.0f) {
                    nextPercent = 1.0f
                    isPlaying = false
                }
                viewModel.updatePlaybackProgress(nextPercent, (nextPercent * 1200000L).toLong())
            }
        }
    }

    if (activeEp == null || currentAnime == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لا توجد حلقة محددة للمشاهدة!")
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBackToDetails) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "عودة للتفاصيل")
                }
                Text(
                    text = "${currentAnime.title} - حلقة ${activeEp.number}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }

            // SIMULATED PLAYER CONTAINER (High-Contrast Slate Bento Card)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.Black)
                    .testTag("video_player_box"),
                contentAlignment = Alignment.Center
            ) {
                if (streamLoading) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PrimaryAnime)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("جاري تشفير واستدعاء البث السريع...", color = Color.White, fontSize = 12.sp)
                    }
                } else {
                    // Video Content overlay representation
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Red)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("بث مباشر", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = selectedServer?.name ?: "تحميل السيرفر...",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }

                        // Center Play/Pause button
                        IconButton(
                            onClick = { isPlaying = !isPlaying },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .align(Alignment.CenterHorizontally)
                                .testTag("video_play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "تشغيل",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        // Custom Seeking bar
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val currentMin = (viewModel.videoPlaybackTimeMs / 1000) / 60
                                val currentSec = (viewModel.videoPlaybackTimeMs / 1000) % 60
                                Text(
                                    text = String.format("%02d:%02d", currentMin, currentSec),
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "20:00",
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { viewModel.videoPlaybackPercent },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = Color.Red,
                                trackColor = Color.White.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }

            // Servers Selector Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "اختر سيرفر المشاهدة 📡",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(servers) { server ->
                            val isSelected = selectedServer == server
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) PrimaryAnime else MaterialTheme.colorScheme.surface)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        viewModel.setServer(server)
                                        Toast.makeText(context, "تم تحويل البث إلى ${server.name}", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("server_item_${server.name}")
                            ) {
                                Text(
                                    text = server.name,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Description / Details summary below player
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(BentoSkyBg)
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "ملاحظات التشغيل 💡",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoSkyText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "يقوم التطبيق بحفظ آخر سيرفر مستخدم بالإضافة إلى نسبة مشاهدة الحلقة تلقائياً، للعودة والمتابعة من حيث توقفت بأي وقت.",
                        fontSize = 11.sp,
                        color = BentoSkyText.copy(alpha = 0.9f),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

// --- Favorites Screen ---
@Composable
fun FavoritesScreen(
    viewModel: AnimeViewModel,
    onNavigateToDetails: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val favorites by viewModel.favorites.collectAsState()
    val watchHistory by viewModel.watchHistory.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header section
        Text(
            text = "قائمتي المفضلة ❤️",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "الأنميات التي تتابعها وتتلقى إشعارات نزول حلقاتها فوراً",
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.FavoriteBorder, contentDescription = "مفضلة فارغة", modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("قائمة المفضلة فارغة حالياً!", fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text("أضف الأنميات المفضلة لديك من صفحة تفاصيل العمل.", fontSize = 11.sp, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(favorites) { fav ->
                    // Find matching watch progress in history if exists
                    val progress = watchHistory.find { it.animeId == fav.animeId }
                    
                    BentoFavoriteCard(
                        favorite = fav,
                        history = progress,
                        onClick = { onNavigateToDetails(fav.animeId, fav.providerId) }
                    )
                }
            }
        }
    }
}

@Composable
fun BentoFavoriteCard(
    favorite: FavoriteAnimeEntity,
    history: WatchHistoryEntity?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .testTag("favorite_card_${favorite.animeId}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image
            Box {
                AsyncImage(
                    model = favorite.imageUrl,
                    contentDescription = favorite.title,
                    modifier = Modifier
                        .size(85.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )

                // NEW Episode Notification Badge
                if (favorite.hasNewEpisode) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Red)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("جديد 🔥", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = favorite.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "آخر الحلقات المكتشفة: ${favorite.lastEpisodeCount}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                // Watch progress indicator
                if (history != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "حلقة ${history.episodeNumber}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryAnime
                            )
                            Text(
                                text = "مشاهدة ${(history.percentWatched * 100).toInt()}%",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        LinearProgressIndicator(
                            progress = { history.percentWatched },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = PrimaryAnime,
                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "لم يبدأ بعد ⏳",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "الانتقال",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

// --- Settings Screen ---
@Composable
fun SettingsScreen(
    viewModel: AnimeViewModel,
    modifier: Modifier = Modifier
) {
    val configs by viewModel.providerConfigs.collectAsState()
    val context = LocalContext.current
    var showIntervalDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Title Header
        Column {
            Text(
                text = "إعدادات التطبيق ⚙️",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "تخصيص مزودات البث والفحص الدوري للأنمي المفضل",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }

        // Providers Config Card (Bento Grid Style)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مزودات البث (Providers) 📡",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryAnime.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${configs.size} مزودات",
                            fontSize = 10.sp,
                            color = PrimaryAnime,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "قم بتفعيل أو تعطيل أي موقع، وترتيب أولوية البث والبحث عبر السحب والإفلات أدناه.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // List of providers configs
                configs.forEachIndexed { index, config ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Drag simulator icon
                            IconButton(
                                onClick = {
                                    // Simulated Reordering trigger
                                    val otherIndex = if (index == 0) 1 else 0
                                    viewModel.updateProviderOrder(config.providerId, otherIndex)
                                    Toast.makeText(context, "تم تغيير أولوية ومطابقة ${config.displayName}", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(imageVector = Icons.Default.DragHandle, contentDescription = "ترتيب", tint = Color.Gray)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(
                                    text = config.displayName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "الأولوية: #${config.priority + 1}",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Switch(
                            checked = config.isEnabled,
                            onCheckedChange = { viewModel.toggleProvider(config.providerId) },
                            colors = SwitchDefaults.colors(checkedThumbColor = PrimaryAnime),
                            modifier = Modifier.testTag("provider_switch_${config.providerId}")
                        )
                    }
                }
            }
        }

        // Background sync config card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(BentoSkyBg)
                .border(1.dp, BentoSkyBorder, RoundedCornerShape(24.dp))
                .clickable { showIntervalDialog = true }
                .padding(16.dp)
                .testTag("settings_sync_card")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "دورية فحص الحلقات الجديدة ⏰",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoSkyText
                    )
                    Text(
                        text = "الفحص الدوري مفعل للمفضلة فقط لتوفير البيانات والبطارية",
                        fontSize = 11.sp,
                        color = BentoSkyText.copy(alpha = 0.8f)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    val label = when (viewModel.syncIntervalHours) {
                        1 -> "كل ساعة"
                        3 -> "كل 3 ساعات"
                        6 -> "كل 6 ساعات"
                        12 -> "كل 12 ساعة"
                        else -> "يومياً"
                    }
                    Text(text = label, color = BentoSkyText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Dark mode card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "الوضع الليلي (Dark Mode) 🌙",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "توفير مريح للعين أثناء ساعات الليل الطويلة",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                Switch(
                    checked = viewModel.isDarkMode,
                    onCheckedChange = { viewModel.isDarkMode = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = PrimaryAnime),
                    modifier = Modifier.testTag("dark_mode_switch")
                )
            }
        }

        // App details metadata card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(BentoLavenderBg)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "نبذة عن نظام Providers 📂",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoLavenderText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "تم بناء هيكلية التطبيق ليدعم الفصل التام لكل مزود بث في ملف خاص. يتيح هذا التصميم للمطورين إضافة أي موقع بث في المستقبل فورياً وبسهولة بالغة دون تعديل أي واجهة أو منطق برمجي عام.",
                    fontSize = 11.sp,
                    color = BentoLavenderText.copy(alpha = 0.9f),
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Interval Selector Dialog
    if (showIntervalDialog) {
        AlertDialog(
            onDismissRequest = { showIntervalDialog = false },
            title = { Text("اختر دورية الفحص ⏰", fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Right) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val intervals = listOf(1 to "كل ساعة", 3 to "كل 3 ساعات", 6 to "كل 6 ساعات", 12 to "كل 12 ساعة", 24 to "يومياً")
                    intervals.forEach { (hours, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateSyncInterval(hours)
                                    showIntervalDialog = false
                                    Toast.makeText(context, "تم ضبط الفحص: $name", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = name, fontSize = 14.sp)
                            if (viewModel.syncIntervalHours == hours) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = "محدد", tint = PrimaryAnime)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showIntervalDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
