package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.IslamicViewModel
import com.example.ui.Translations
import com.example.ui.components.BrandRichGold
import com.example.ui.components.BrandIvoryWhite
import com.example.ui.components.BrandEmeraldGreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.QiblaScreen
import com.example.ui.screens.QuranScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: IslamicViewModel = viewModel()
                MainApplicationContainer(viewModel)
            }
        }
    }
}

@Composable
fun MainApplicationContainer(viewModel: IslamicViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val appLang by viewModel.appLanguage.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        
        // Render Active Screen Layout on Backdrop
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                0 -> HomeScreen(viewModel = viewModel)
                1 -> QuranScreen(viewModel = viewModel)
                2 -> QiblaScreen(viewModel = viewModel)
                3 -> SettingsScreen(viewModel = viewModel)
            }
        }

        // FLOATING LUXURY GLASSMORPHIC CAPSULE NAVIGATION BAR
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = BrandRichGold.copy(alpha = 0.25f),
                    spotColor = BrandRichGold.copy(alpha = 0.35f)
                )
                .border(
                    border = BorderStroke(
                        width = 1.2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                BrandRichGold.copy(alpha = 0.65f),
                                BrandRichGold.copy(alpha = 0.15f)
                            )
                        )
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .background(
                    color = Color(0xEC090B0A), // Extra dark translucent carbon background
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(vertical = 10.dp, horizontal = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(0.95f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tabs = listOf(
                    NavigationTabItem(
                        titleKey = "home",
                        iconActive = Icons.Default.Home,
                        iconInactive = Icons.Outlined.Home,
                        tag = "nav_home"
                    ),
                    NavigationTabItem(
                        titleKey = "quran",
                        iconActive = Icons.Default.Book,
                        iconInactive = Icons.Outlined.Book,
                        tag = "nav_quran"
                    ),
                    NavigationTabItem(
                        titleKey = "qibla",
                        iconActive = Icons.Default.Explore,
                        iconInactive = Icons.Outlined.Explore,
                        tag = "nav_qibla"
                    ),
                    NavigationTabItem(
                        titleKey = "settings",
                        iconActive = Icons.Default.Settings,
                        iconInactive = Icons.Outlined.Settings,
                        tag = "nav_settings"
                    )
                )

                tabs.forEachIndexed { index, tab ->
                    val isActive = selectedTab == index
                    
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .clickable { selectedTab = index }
                            .testTag(tab.tag)
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // High Contrast Active Pill indicator glowing emerald / gold
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isActive) BrandEmeraldGreen.copy(alpha = 0.25f) else Color.Transparent)
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isActive) tab.iconActive else tab.iconInactive,
                                contentDescription = Translations.get(tab.titleKey, appLang),
                                tint = if (isActive) BrandRichGold else BrandIvoryWhite.copy(alpha = 0.5f),
                                modifier = Modifier.size(23.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(2.dp))
                        
                        Text(
                            text = Translations.get(tab.titleKey, appLang),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                            color = if (isActive) BrandRichGold else BrandIvoryWhite.copy(alpha = 0.5f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

data class NavigationTabItem(
    val titleKey: String,
    val iconActive: androidx.compose.ui.graphics.vector.ImageVector,
    val iconInactive: androidx.compose.ui.graphics.vector.ImageVector,
    val tag: String
)

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
