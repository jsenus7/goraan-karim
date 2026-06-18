package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

// Theme specific luxury colors
val BrandDeepBlack = Color(0xFF0D0D0D)
val BrandRichGold = Color(0xFFD4AF37)
val BrandEmeraldGreen = Color(0xFF0F7A5A)
val BrandIvoryWhite = Color(0xFFF8F4E3)

@Composable
fun PremiumBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        BrandEmeraldGreen.copy(alpha = 0.22f), // Emerald dome center aura
                        BrandDeepBlack                         // Kaaba black bounds
                    ),
                    center = Offset(540f, 200f),
                    radius = 1800f
                )
            )
    ) {
        // Subtle decorative Islamic geometric starlight constellation background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Star Coordinates to give hand-drawn architecture precision
            val stars = listOf(
                Offset(width * 0.15f, height * 0.12f),
                Offset(width * 0.85f, height * 0.18f),
                Offset(width * 0.50f, height * 0.05f),
                Offset(width * 0.30f, height * 0.22f),
                Offset(width * 0.70f, height * 0.28f),
                Offset(width * 0.08f, height * 0.45f),
                Offset(width * 0.92f, height * 0.52f),
                Offset(width * 0.25f, height * 0.75f),
                Offset(width * 0.78f, height * 0.82f)
            )

            stars.forEach { pos ->
                // Draw luxury 8-pointed golden star (Rub el Hizb element) dynamically
                val r1 = 3.dp.toPx()
                val r2 = 7.dp.toPx()
                val goldColor = BrandRichGold.copy(alpha = 0.25f)
                
                // Draw 8 points
                for (i in 0 until 8) {
                    val angle = (i * Math.PI / 4)
                    val nextAngle = ((i + 1) * Math.PI / 4)
                    
                    val p1 = Offset(
                        (pos.x + r2 * cos(angle)).toFloat(),
                        (pos.y + r2 * sin(angle)).toFloat()
                    )
                    val p2 = Offset(
                        (pos.x + r1 * cos(angle + Math.PI / 8)).toFloat(),
                        (pos.y + r1 * sin(angle + Math.PI / 8)).toFloat()
                    )
                    
                    drawLine(
                        color = goldColor,
                        start = p1,
                        end = p2,
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
        }
        content()
    }
}

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    borderStroke: BorderStroke? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardShape = RoundedCornerShape(20.dp)
    val actualBorder = borderStroke ?: BorderStroke(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = listOf(
                BrandRichGold.copy(alpha = 0.45f),
                BrandRichGold.copy(alpha = 0.06f)
            )
        )
    )

    Card(
        modifier = modifier
            .padding(vertical = 6.dp)
            .shadow(
                elevation = 6.dp,
                shape = cardShape,
                ambientColor = BrandRichGold.copy(alpha = 0.1f),
                spotColor = BrandRichGold.copy(alpha = 0.15f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xDF141715) // Glass charcoal translucent backdrop
        ),
        shape = cardShape,
        border = actualBorder
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null) Modifier.fillMaxWidth().background(Color.Transparent)
                    else Modifier
                ),
            content = content
        )
    }
}

@Composable
fun LuxuryMihrabArch(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    // Elegant combination of Islamic architecture arches (Pointed top arch geometry)
    val archShape = RoundedCornerShape(
        topStart = 100.dp,
        topEnd = 100.dp,
        bottomStart = 24.dp,
        bottomEnd = 24.dp
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(archShape)
            .border(
                border = BorderStroke(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            BrandRichGold,
                            BrandRichGold.copy(alpha = 0.15f)
                        )
                    )
                ),
                shape = archShape
            )
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF101B17), // Deep emerald onyx
                        BrandDeepBlack      // Pure black lower depth
                    )
                )
            )
    ) {
        // Double internal golden arch framing lines
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
                .clip(archShape)
                .border(
                    border = BorderStroke(
                        width = 0.75.dp,
                        color = BrandRichGold.copy(alpha = 0.25f)
                    ),
                    shape = archShape
                )
        ) {
            content()
        }
    }
}
