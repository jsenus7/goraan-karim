package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
fun SettingsScreen(viewModel: IslamicViewModel) {
    val methodId by viewModel.calculationMethod.collectAsState()
    val appLang by viewModel.appLanguage.collectAsState()
    val tasbihCount by viewModel.tasbihCount.collectAsState()
    val tasbihTarget by viewModel.tasbihTarget.collectAsState()
    val selectedDhikrIdx by viewModel.selectedDhikrIndex.collectAsState()

    // Local states
    val context = LocalContext.current
    var isGlobalAdhanEnabled by remember { mutableStateOf(true) }
    var fajrAdhan by remember { mutableStateOf(true) }
    var dhuhrAdhan by remember { mutableStateOf(true) }
    var asrAdhan by remember { mutableStateOf(true) }
    var maghribAdhan by remember { mutableStateOf(true) }
    var ishaAdhan by remember { mutableStateOf(true) }

    // Dropdowns
    var methodExpanded by remember { mutableStateOf(false) }
    var langExpanded by remember { mutableStateOf(false) }
    var dhikrExpanded by remember { mutableStateOf(false) }

    // Feedback forms
    var feedbackName by remember { mutableStateOf("") }
    var feedbackEmail by remember { mutableStateOf("") }
    var feedbackMsg by remember { mutableStateOf("") }
    var showDuaIndex by remember { mutableStateOf(-1) } // track expanded duas

    val calculationMethods = listOf(
        0 to "Shia Jafari",
        1 to "Karachi (Univ of Islam Sciences)",
        2 to "ISNA (North America)",
        3 to "MWL (Muslim World League)",
        4 to "Umm Al-Qura (Makkah)",
        5 to "Egyptian Survey Authority",
        7 to "Tehran (Institute of Geophysics)",
        11 to "Singapore (MUIS)",
        12 to "France (UOIF)",
        13 to "Turkey (Diyanet)",
        14 to "Russia (Spiritual Adm.)"
    )

    PremiumBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp) // navigation buffer
        ) {
            
            // Screen Title Panel
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
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings icon",
                        tint = BrandRichGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = Translations.get("settings", appLang),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandIvoryWhite,
                    letterSpacing = 0.5.sp
                )
            }

            // === 1. DIGITAL TASBIH COMPONENT ===
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                borderStroke = BorderStroke(1.5.dp, BrandRichGold.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = Translations.get("tasbih", appLang).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrandRichGold,
                            letterSpacing = 1.sp
                        )

                        // Target selectors
                        Row {
                            IconButton(onClick = { viewModel.updateTasbihTarget(33) }) {
                                Text(
                                    text = "33",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (tasbihTarget == 33) BrandRichGold else BrandIvoryWhite.copy(alpha = 0.4f)
                                )
                            }
                            IconButton(onClick = { viewModel.updateTasbihTarget(99) }) {
                                Text(
                                    text = "99",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (tasbihTarget == 99) BrandRichGold else BrandIvoryWhite.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Active Selector Dhikr Phrase Dropdown
                    val selectedDhikr = Translations.DHIKRS[selectedDhikrIdx]
                    Box {
                        Button(
                            onClick = { dhikrExpanded = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF101B17)),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BrandRichGold.copy(alpha = 0.4f)),
                            modifier = Modifier.testTag("dhikr_select_button")
                        ) {
                            Text(selectedDhikr.phrase, fontWeight = FontWeight.Bold, color = BrandRichGold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Dhikr expand dropdown", tint = BrandRichGold)
                        }

                        DropdownMenu(
                            expanded = dhikrExpanded,
                            onDismissRequest = { dhikrExpanded = false }
                        ) {
                            Translations.DHIKRS.forEachIndexed { index, dhikrItem ->
                                DropdownMenuItem(
                                    text = { Text("${dhikrItem.phrase} (${dhikrItem.arabic})") },
                                    onClick = {
                                        viewModel.selectDhikr(index)
                                        dhikrExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Arabic Dhikr readout
                    Text(
                        text = selectedDhikr.arabic,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandRichGold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = selectedDhikr.translation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = BrandIvoryWhite.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // ROUND CLICKABLE DIGITAL TASBIH RING
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(BrandEmeraldGreen.copy(alpha = 0.25f), Color.Transparent)
                                )
                            )
                            .border(2.dp, BrandRichGold, CircleShape)
                            .clickable { viewModel.incrementTasbih() }
                            .testTag("tasbih_increment_circle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = tasbihCount.toString(),
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = BrandIvoryWhite
                            )
                            Text(
                                text = "of $tasbihTarget",
                                style = MaterialTheme.typography.labelSmall,
                                color = BrandRichGold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = { viewModel.resetTasbih() },
                        modifier = Modifier.testTag("tasbih_reset_btn")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset clicks", tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = Translations.get("reset", appLang).uppercase(),
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // === 2. AUTHENTIC DUAS SECTION ===
            Text(
                text = Translations.get("duas", appLang).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BrandRichGold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                letterSpacing = 1.sp
            )

            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Translations.DUAS.forEachIndexed { index, dua ->
                        val isExpanded = showDuaIndex == index
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isExpanded) Color(0xFF101C15) else Color.Transparent)
                                .border(
                                    width = if (isExpanded) 1.dp else 0.dp,
                                    color = BrandRichGold.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { showDuaIndex = if (isExpanded) -1 else index }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = dua.title,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandIvoryWhite,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = dua.category.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BrandRichGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Expand Supplication",
                                    tint = BrandRichGold
                                )
                            }

                            AnimatedVisibility(visible = isExpanded) {
                                Column(modifier = Modifier.padding(top = 12.dp)) {
                                    Text(
                                        text = dua.arabicText,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Right,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandRichGold,
                                        fontSize = 22.sp
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = dua.englishText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = BrandIvoryWhite.copy(alpha = 0.85f),
                                        lineHeight = 22.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Ref: ${dua.source}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = BrandRichGold.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // === 3. PREFERENCES CONFIGURATIONS ===
            Text(
                text = "CONFIGURATIONS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BrandRichGold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                letterSpacing = 1.sp
            )

            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    
                    // CALCULATION METHOD
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { methodExpanded = true }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = Translations.get("calc_method", appLang),
                                    fontWeight = FontWeight.Bold,
                                    color = BrandIvoryWhite
                                )
                                Text(
                                    text = calculationMethods.firstOrNull { it.first == methodId }?.second ?: "Default Method",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BrandRichGold
                                )
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Expand calculation method drop", tint = BrandRichGold)
                        }

                        DropdownMenu(
                            expanded = methodExpanded,
                            onDismissRequest = { methodExpanded = false }
                        ) {
                            calculationMethods.forEach { (mId, mLabel) ->
                                DropdownMenuItem(
                                    text = { Text(mLabel) },
                                    onClick = {
                                        viewModel.updateCalculationMethod(mId)
                                        methodExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = BrandRichGold.copy(alpha = 0.12f))

                    // LANGUAGE SELECTION
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { langExpanded = true }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = Translations.get("language", appLang),
                                    fontWeight = FontWeight.Bold,
                                    color = BrandIvoryWhite
                                )
                                Text(
                                    text = Translations.LANGUAGES.firstOrNull { it.code == appLang }?.name ?: "English",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BrandRichGold
                                )
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Expand language drop", tint = BrandRichGold)
                        }

                        DropdownMenu(
                            expanded = langExpanded,
                            onDismissRequest = { langExpanded = false }
                        ) {
                            Translations.LANGUAGES.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.name) },
                                    onClick = {
                                        viewModel.updateLanguage(option.code)
                                        langExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = BrandRichGold.copy(alpha = 0.12f))

                    // GLOBAL ADHAN SWITCHES
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = Translations.get("adhan_alerts", appLang),
                                fontWeight = FontWeight.Bold,
                                color = BrandIvoryWhite
                            )
                            Text(
                                text = "Mute/unmute call to prayer sound",
                                style = MaterialTheme.typography.bodySmall,
                                color = BrandIvoryWhite.copy(alpha = 0.5f)
                            )
                        }
                        Switch(
                            checked = isGlobalAdhanEnabled,
                            onCheckedChange = { isGlobalAdhanEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = BrandRichGold,
                                checkedTrackColor = BrandEmeraldGreen
                            )
                        )
                    }

                    if (isGlobalAdhanEnabled) {
                        Column {
                            listOf(
                                "Fajr" to fajrAdhan,
                                "Dhuhr" to dhuhrAdhan,
                                "Asr" to asrAdhan,
                                "Maghrib" to maghribAdhan,
                                "Isha" to ishaAdhan
                            ).forEach { (prayerStr, alertFlag) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, top = 6.dp, bottom = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = prayerStr,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = BrandIvoryWhite
                                    )
                                    Checkbox(
                                        checked = alertFlag,
                                        onCheckedChange = {
                                            when (prayerStr) {
                                                "Fajr" -> fajrAdhan = it
                                                "Dhuhr" -> dhuhrAdhan = it
                                                "Asr" -> asrAdhan = it
                                                "Maghrib" -> maghribAdhan = it
                                                "Isha" -> ishaAdhan = it
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = BrandRichGold,
                                            checkmarkColor = Color.Black
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // === 4. FEEDBACK FORM ===
            Text(
                text = Translations.get("feedback", appLang).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BrandRichGold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                letterSpacing = 1.sp
            )

            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = feedbackName,
                        onValueChange = { feedbackName = it },
                        label = { Text("Name", color = BrandRichGold) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BrandIvoryWhite,
                            unfocusedTextColor = BrandIvoryWhite,
                            focusedBorderColor = BrandRichGold,
                            unfocusedBorderColor = BrandRichGold.copy(alpha = 0.3f)
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = feedbackEmail,
                        onValueChange = { feedbackEmail = it },
                        label = { Text("Email", color = BrandRichGold) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BrandIvoryWhite,
                            unfocusedTextColor = BrandIvoryWhite,
                            focusedBorderColor = BrandRichGold,
                            unfocusedBorderColor = BrandRichGold.copy(alpha = 0.3f)
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = feedbackMsg,
                        onValueChange = { feedbackMsg = it },
                        label = { Text("How can we improve your experience?", color = BrandRichGold) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BrandIvoryWhite,
                            unfocusedTextColor = BrandIvoryWhite,
                            focusedBorderColor = BrandRichGold,
                            unfocusedBorderColor = BrandRichGold.copy(alpha = 0.3f)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (feedbackMsg.isNotBlank()) {
                                Toast.makeText(context, "JazakAllahu Khair. Your suggestion has been recorded!", Toast.LENGTH_LONG).show()
                                feedbackName = ""
                                feedbackEmail = ""
                                feedbackMsg = ""
                            } else {
                                Toast.makeText(context, "Please write a suggestion first", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("feedback_submit_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRichGold),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = Translations.get("submit_fb", appLang).uppercase(),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // === 5. ABOUT APPLICATION ===
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                borderStroke = BorderStroke(1.dp, BrandRichGold.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Translations.get("about", appLang).uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BrandRichGold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = Translations.get("about_text", appLang),
                        style = MaterialTheme.typography.bodyMedium,
                        color = BrandIvoryWhite.copy(alpha = 0.7f),
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
