package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlphabetLetter
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CaucasusEmerald
import com.example.ui.theme.GeorgianWinePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlphabetScreen(
    letters: List<AlphabetLetter>,
    onPlayAudio: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedLetter by remember { mutableStateOf<AlphabetLetter?>(null) }
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Все (33)", "Гласные", "Гортанные к'/п'/т'", "Придыхательные")

    val filteredLetters = remember(selectedTab, letters) {
        when (selectedTab) {
            1 -> letters.filter { listOf("ა", "ე", "ი", "ო", "უ").contains(it.letter) }
            2 -> letters.filter { it.isEjectiveOrSpecial }
            3 -> letters.filter { listOf("თ", "ფ", "ქ", "ჩ", "ც").contains(it.letter) }
            else -> letters
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Top Header
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Text(
                text = "Алфавит Мхедрули (მხედრული)",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "33 буквы современного грузинского письма. Нажмите для звучания!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Tabs
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, tabName ->
                    SegmentedButton(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = tabs.size),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = GeorgianWinePrimary,
                            activeContentColor = Color.White
                        )
                    ) {
                        Text(
                            text = tabName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Letter Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredLetters) { item ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        1.5.dp,
                        if (item.isEjectiveOrSpecial) AmberGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .aspectRatio(0.9f)
                        .clickable {
                            selectedLetter = item
                            onPlayAudio(item.letter, false)
                        }
                        .testTag("alphabet_tile_${item.letter}")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(6.dp)
                    ) {
                        Text(
                            text = item.letter,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (item.isEjectiveOrSpecial) AmberGold else GeorgianWinePrimary
                        )
                        Text(
                            text = item.transcriptionRu,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }

    // Letter Detail Modal Dialog
    selectedLetter?.let { letter ->
        ModalBottomSheet(
            onDismissRequest = { selectedLetter = null },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large Letter Display
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(GeorgianWinePrimary.copy(alpha = 0.12f))
                        .border(3.dp, if (letter.isEjectiveOrSpecial) AmberGold else GeorgianWinePrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter.letter,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = GeorgianWinePrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Буква «${letter.name}» [${letter.transcriptionRu}]",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (letter.isEjectiveOrSpecial) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AmberGold.copy(alpha = 0.15f),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "Гортанный / Резкий звук",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AmberGold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sound description card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Произношение:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = letter.soundDescription,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Example word
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Пример слова:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${letter.exampleWordGe} [${letter.exampleWordTranscription}]",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = GeorgianWinePrimary
                            )
                            Text(
                                text = letter.exampleWordRu,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = { onPlayAudio(letter.exampleWordGe, false) },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CaucasusEmerald)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Озвучить слово",
                                tint = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { onPlayAudio(letter.letter, false) },
                        colors = ButtonDefaults.buttonColors(containerColor = GeorgianWinePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Озвучить")
                    }

                    OutlinedButton(
                        onClick = { onPlayAudio(letter.letter, true) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Speed, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Медленно")
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
