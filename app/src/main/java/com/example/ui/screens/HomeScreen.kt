package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.IslamicViewModel
import com.example.ui.Translations
import com.example.ui.components.PremiumBackground
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.LuxuryMihrabArch
import com.example.ui.components.BrandRichGold
import com.example.ui.components.BrandEmeraldGreen
import com.example.ui.components.BrandIvoryWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: IslamicViewModel) {
    val timings by viewModel.prayerTimings.collectAsState()
    val isLoading by viewModel.timingsLoading.collectAsState()
    val error by viewModel.timingsError.collectAsState()
    val city by viewModel.userCity.collectAsState()
    val nextPrayer by viewModel.nextPrayerName.collectAsState()
    val countdown by viewModel.nextPrayerCountdown.collectAsState()
    val lang by viewModel.appLanguage.collectAsState()

    // Automatic location fetch on first start
    LaunchedEffect(Unit) {
        if (timings == null) {
            viewModel.loadLocationAndFetchTimes()
        }
    }

    PremiumBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp) // extra pad for navigation bar
        ) {
            
            // ARCHWAY MIHRAB HERO
            LuxuryMihrabArch(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(310.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = Translations.get("home", lang).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = BrandRichGold,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.5.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (timings != null) {
                        Text(
                            text = "${timings?.hijriDay} ${timings?.hijriMonthEn} ${timings?.hijriYear} AH",
                            style = MaterialTheme.typography.titleLarge,
                            color = BrandIvoryWhite,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "(${timings?.hijriMonthAr})",
                            style = MaterialTheme.typography.bodyLarge,
                            color = BrandRichGold.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium,
                            fontSize = 17.sp
                        )
                    } else {
                        Text(
                            text = "بسم الله الرحمن الرحيم",
                            style = MaterialTheme.typography.titleLarge,
                            color = BrandRichGold,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = Translations.get("next_prayer", lang).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = BrandIvoryWhite.copy(alpha = 0.6f),
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = nextPrayer,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandRichGold,
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = countdown,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = BrandIvoryWhite,
                        fontStyle = FontStyle.Italic,
                        letterSpacing = 1.sp
                    )
                }
            }

            // LOCATION CHIP / REFRESH
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Location icon",
                            tint = BrandRichGold,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = Translations.get("location", lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = BrandIvoryWhite.copy(alpha = 0.5f)
                            )
                            Text(
                                text = city,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrandIvoryWhite
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.loadLocationAndFetchTimes() },
                        modifier = Modifier.testTag("location_refresh_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = BrandRichGold
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh prayer times",
                                tint = BrandRichGold
                            )
                        }
                    }
                }
            }

            // ERROR DISPLAY
            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // DAILY TIMINGS LIST
            if (timings != null) {
                val t = timings!!
                val itemsList = listOf(
                    Translations.get("fajr", lang) to t.fajr,
                    Translations.get("sunrise", lang) to t.sunrise,
                    Translations.get("dhuhr", lang) to t.dhuhr,
                    Translations.get("asr", lang) to t.asr,
                    Translations.get("maghrib", lang) to t.maghrib,
                    Translations.get("isha", lang) to t.isha,
                    Translations.get("midnight", lang) to t.midnight
                )

                itemsList.forEach { (name, timeStr) ->
                    val isNext = nextPrayer.contains(name, ignoreCase = true)
                    
                    // Luxury highlight border for soonest prayer
                    val customBorder = if (isNext) {
                        BorderStroke(
                            width = 1.5.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(BrandRichGold, BrandEmeraldGreen)
                            )
                        )
                    } else {
                        BorderStroke(
                            width = 1.dp,
                            color = BrandRichGold.copy(alpha = 0.15f)
                        )
                    }

                    val overlayColor = if (isNext) BrandEmeraldGreen.copy(alpha = 0.35f) else Color.Transparent

                    GlassmorphicCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        borderStroke = customBorder
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(overlayColor)
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isNext) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = "Active next prayer icon",
                                        tint = BrandRichGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                }
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isNext) BrandRichGold else BrandIvoryWhite
                                )
                            }

                            Text(
                                text = timeStr,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrandIvoryWhite
                            )
                        }
                    }
                }
            } else if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandRichGold)
                }
            }
        }
    }
}
