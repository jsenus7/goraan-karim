package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.IslamicViewModel
import com.example.ui.Translations
import com.example.ui.components.PremiumBackground
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.BrandRichGold
import com.example.ui.components.BrandEmeraldGreen
import com.example.ui.components.BrandIvoryWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(viewModel: IslamicViewModel) {
    val surahs by viewModel.filteredSurahs.collectAsState()
    val selectedSurah by viewModel.selectedSurah.collectAsState()
    val ayahs by viewModel.ayahs.collectAsState()
    val isAyahsLoading by viewModel.ayahsLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isPlayingAudio by viewModel.isPlayingAudio.collectAsState()
    val currentPlayingIndex by viewModel.currentPlayingIndex.collectAsState()
    val textSize by viewModel.textSize.collectAsState()
    val lang by viewModel.appLanguage.collectAsState()

    PremiumBackground(modifier = Modifier.fillMaxSize()) {
        if (selectedSurah == null) {
            // === SURAH SELECTION VIEW ===
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Header Panel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(BrandRichGold.copy(alpha = 0.15f))
                            .border(1.dp, BrandRichGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = "Quran Menu Icon",
                            tint = BrandRichGold,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = Translations.get("quran", lang),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandIvoryWhite,
                        letterSpacing = 0.5.sp
                    )
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchSurah(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("quran_search_input"),
                    placeholder = { 
                        Text(
                            text = Translations.get("search_surah", lang),
                            color = BrandIvoryWhite.copy(alpha = 0.4f)
                        ) 
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon", tint = BrandRichGold) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchSurah("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = BrandRichGold)
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = BrandIvoryWhite,
                        unfocusedTextColor = BrandIvoryWhite,
                        focusedBorderColor = BrandRichGold,
                        unfocusedBorderColor = BrandRichGold.copy(alpha = 0.3f),
                        focusedContainerColor = Color(0x73000000),
                        unfocusedContainerColor = Color(0x40000000)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable List of Surahs
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp), // space for bottom nav
                    contentPadding = PaddingValues(16.dp)
                ) {
                    itemsIndexed(surahs) { _, surah ->
                        GlassmorphicCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectSurah(surah) }
                                .testTag("surah_card_${surah.number}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Index circle badge
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(BrandEmeraldGreen.copy(alpha = 0.25f))
                                            .border(1.dp, BrandRichGold.copy(alpha = 0.4f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = surah.number.toString(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandRichGold
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column {
                                        Text(
                                            text = surah.englishName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandIvoryWhite
                                        )
                                        Text(
                                            text = "${surah.englishNameTranslation} • ${surah.numberOfAyahs} verses",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = BrandIvoryWhite.copy(alpha = 0.5f)
                                        )
                                    }
                                }

                                Text(
                                    text = surah.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandRichGold
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // === NOBLE QURAN READER VIEW ===
            val activeSurah = selectedSurah!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Reader Navigation Bar Header
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = activeSurah.englishName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrandIvoryWhite
                            )
                            Text(
                                text = "${activeSurah.englishNameTranslation} • ${activeSurah.numberOfAyahs} verses",
                                style = MaterialTheme.typography.bodySmall,
                                color = BrandRichGold.copy(alpha = 0.8f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                viewModel.selectSurah(activeSurah) // Toggle reset
                                viewModel.stopAudio()
                                viewModel.searchSurah("") // Clear query
                            },
                            modifier = Modifier.testTag("quran_reader_back_bar")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Return to index",
                                tint = BrandRichGold
                            )
                        }
                    },
                    actions = {
                        // Font scaling buttons
                        IconButton(onClick = { viewModel.changeTextSize(false) }) {
                            Icon(Icons.Default.Remove, contentDescription = "Zoom out font", tint = BrandRichGold)
                        }
                        IconButton(onClick = { viewModel.changeTextSize(true) }) {
                            Icon(Icons.Default.Add, contentDescription = "Zoom in font", tint = BrandRichGold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = BrandIvoryWhite
                    )
                )

                // Revelation contextual bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF101C15))
                        .border(width = 0.5.dp, color = BrandRichGold.copy(alpha = 0.15f))
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Revelation: ${activeSurah.revelationType.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = BrandRichGold,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Surah ${activeSurah.number}",
                        style = MaterialTheme.typography.labelSmall,
                        color = BrandIvoryWhite.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isAyahsLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BrandRichGold)
                    }
                } else {
                    val listState = rememberLazyListState()

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 80.dp), // navigation buffer
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        // Display Bismillah except Surah 1 & 9
                        if (activeSurah.number != 1 && activeSurah.number != 9) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = BrandRichGold,
                                        textAlign = TextAlign.Center,
                                        fontSize = 28.sp
                                    )
                                }
                            }
                        }

                        itemsIndexed(ayahs) { index, ayah ->
                            val isActive = currentPlayingIndex == index
                            
                            val customBorder = if (isActive) {
                                BorderStroke(1.dp, BrandRichGold)
                            } else {
                                BorderStroke(1.dp, BrandRichGold.copy(alpha = 0.12f))
                            }

                            val backgroundOverlay = if (isActive) {
                                BrandEmeraldGreen.copy(alpha = 0.25f)
                            } else {
                                Color.Transparent
                            }

                            GlassmorphicCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                borderStroke = customBorder
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(backgroundOverlay)
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Number badge
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(BrandRichGold.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = ayah.ayahNumber.toString(),
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = BrandRichGold,
                                                fontSize = 13.sp
                                            )
                                        }

                                        // Play audio action
                                        IconButton(
                                            onClick = { viewModel.toggleAudioPlay(index) },
                                            modifier = Modifier.testTag("play_verse_btn_$index")
                                        ) {
                                            Icon(
                                                imageVector = if (isActive && isPlayingAudio) Icons.Default.Pause else Icons.Default.VolumeUp,
                                                contentDescription = "Read ayah audio",
                                                tint = if (isActive) BrandRichGold else BrandIvoryWhite.copy(alpha = 0.5f)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Beautiful Arabic Text Script
                                    Text(
                                        text = ayah.textArabic,
                                        modifier = Modifier.fillMaxWidth(),
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            lineHeight = (textSize * 1.6f).sp,
                                            fontSize = textSize.sp
                                        ),
                                        fontWeight = FontWeight.Bold,
                                        color = BrandRichGold,
                                        textAlign = TextAlign.Right
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Local Language Translation
                                    Text(
                                        text = ayah.textTranslation,
                                        modifier = Modifier.fillMaxWidth(),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = BrandIvoryWhite.copy(alpha = 0.85f),
                                        lineHeight = 24.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
