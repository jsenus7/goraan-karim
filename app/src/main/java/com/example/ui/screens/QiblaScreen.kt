package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import kotlin.math.abs

@Composable
fun QiblaScreen(viewModel: IslamicViewModel) {
    val compassHeading by viewModel.compassHeading.collectAsState()
    val qiblaBearing by viewModel.qiblaBearing.collectAsState()
    val city by viewModel.userCity.collectAsState()
    val lang by viewModel.appLanguage.collectAsState()

    // Manage compass sensor lifecycles dynamically on navigation
    DisposableEffect(Unit) {
        viewModel.startCompass()
        onDispose {
            viewModel.stopCompass()
        }
    }

    // Determine Kaaba alignment offset
    val bearingDiff = (compassHeading - qiblaBearing + 360) % 360
    val shortestDiff = if (bearingDiff > 180) 360 - bearingDiff else bearingDiff
    val isAligned = shortestDiff <= 4.0 // within 4 degrees tolerance

    // Color animations matching alignment
    val glowColor by animateColorAsState(
        targetValue = if (isAligned) BrandEmeraldGreen else BrandRichGold.copy(alpha = 0.35f)
    )

    // Smooth needle rotators
    val animatedCompassHeading by animateFloatAsState(targetValue = compassHeading.toFloat())
    val animatedQiblaBearing by animateFloatAsState(targetValue = qiblaBearing.toFloat())

    PremiumBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // Header
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
                        imageVector = Icons.Default.CompassCalibration,
                        contentDescription = "Qibla marker icon",
                        tint = BrandRichGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = Translations.get("qibla", lang),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandIvoryWhite,
                    letterSpacing = 0.5.sp
                )
            }

            // Current Location context chip
            GlassmorphicCard(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .widthIn(max = 320.dp),
                borderStroke = BorderStroke(1.dp, BrandRichGold.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Map pinpoint",
                        tint = BrandRichGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Calculated from: $city",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrandIvoryWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ANALOG COMPASS COMPOSABLE DIAL BOX
            Box(
                modifier = Modifier
                    .size(290.dp)
                    .clip(CircleShape)
                    .border(
                        width = if (isAligned) 4.dp else 1.5.dp,
                        color = glowColor,
                        shape = CircleShape
                    )
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                BrandEmeraldGreen.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    )
                    .testTag("qibla_compass_dial"),
                contentAlignment = Alignment.Center
            ) {
                // Background ticks ring (rotates by negative of heading)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationZ = -animatedCompassHeading
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Cardinal markers drawn visually
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("N", fontWeight = FontWeight.ExtraBold, color = BrandRichGold, fontSize = 20.sp)
                        Text("S", fontWeight = FontWeight.Bold, color = BrandIvoryWhite.copy(alpha = 0.4f), fontSize = 16.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("W", fontWeight = FontWeight.Bold, color = BrandIvoryWhite.copy(alpha = 0.4f), fontSize = 16.sp)
                        Text("E", fontWeight = FontWeight.Bold, color = BrandIvoryWhite.copy(alpha = 0.4f), fontSize = 16.sp)
                    }
                }

                // Relative Qibla needle pointer (Mecca is located at absolute degree qiblaBearing)
                // When we rotate true North by -compassHeading, Mecca resides at (qiblaBearing - compassHeading)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationZ = animatedQiblaBearing - animatedCompassHeading
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(18.dp))
                        // Prominent Golden Needle Arrow pointing forward
                        Icon(
                            imageVector = Icons.Default.KeyboardDoubleArrowUp,
                            contentDescription = "Mecca arrow direction pointer",
                            tint = BrandRichGold,
                            modifier = Modifier.size(56.dp)
                        )
                        // Decorative Mecca label
                        Text(
                            text = "KABAH",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = BrandRichGold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Inner static core center piece (styled like the Kaaba!)
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0D0D0D)) // Kaaba black fabric
                        .border(1.5.dp, BrandRichGold, RoundedCornerShape(8.dp)) // Kiswa golden band
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Coordinates degree readout inside Kaaba core
                    Text(
                        text = "${compassHeading.toInt()}°",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandRichGold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // ALIGNMENT FEEDBACK GLOWING CARD
            val highlightColor = if (isAligned) BrandEmeraldGreen.copy(alpha = 0.22f) else Color(0x331E1E1E)
            val highlightBorder = if (isAligned) BorderStroke(1.5.dp, BrandEmeraldGreen) else BorderStroke(1.dp, BrandRichGold.copy(alpha = 0.2f))

            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                borderStroke = highlightBorder
            ) {
                Column(
                    modifier = Modifier
                        .background(highlightColor)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isAligned) Translations.get("alignment_perfect", lang) else Translations.get("rotate_phone", lang),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isAligned) BrandEmeraldGreen else BrandIvoryWhite,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${Translations.get("degree_offset", lang)}: ${qiblaBearing.toInt()}° (Facing Mecca)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BrandIvoryWhite.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
