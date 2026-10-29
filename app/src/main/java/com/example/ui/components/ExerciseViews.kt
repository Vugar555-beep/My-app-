package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.Exercise
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CaucasusEmerald
import com.example.ui.theme.GeorgianError
import com.example.ui.theme.GeorgianWinePrimary
import com.example.util.SpeechState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordBubblesExerciseView(
    exercise: Exercise,
    selectedBubbles: List<String>,
    onAddBubble: (String) -> Unit,
    onRemoveBubble: (Int) -> Unit,
    onPlayAudio: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = exercise.prompt,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = exercise.ruText,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Собранное предложение (нажмите на слово, чтобы убрать):",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 72.dp)
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectedBubbles.isEmpty()) {
                    Text(
                        text = "Нажимайте на слова снизу...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp)
                    )
                } else {
                    selectedBubbles.forEachIndexed { index, word ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GeorgianWinePrimary,
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .clickable { onRemoveBubble(index) }
                                .testTag("selected_bubble_$index")
                        ) {
                            Text(
                                text = word,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Варианты слов:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            exercise.bubblePool.forEachIndexed { index, word ->
                val countInPool = exercise.bubblePool.count { it == word }
                val countSelected = selectedBubbles.count { it == word }
                val isAvailable = countSelected < countInPool

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        1.5.dp,
                        if (isAvailable) GeorgianWinePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    color = if (isAvailable) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shadowElevation = if (isAvailable) 2.dp else 0.dp,
                    modifier = Modifier
                        .clickable(enabled = isAvailable) {
                            onAddBubble(word)
                            onPlayAudio(word, false)
                        }
                        .testTag("pool_bubble_$index")
                ) {
                    Text(
                        text = word,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isAvailable) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SpeakPracticeExerciseView(
    exercise: Exercise,
    speechState: SpeechState,
    onStartListening: (String) -> Unit,
    onStopListening: () -> Unit,
    onPlayAudio: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            onStartListening(exercise.geText)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = exercise.prompt,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Target Phrase Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = exercise.geText,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = GeorgianWinePrimary,
                    textAlign = TextAlign.Center
                )
                if (exercise.transcriptionRu.isNotEmpty()) {
                    Text(
                        text = exercise.transcriptionRu,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                if (exercise.ruText.isNotEmpty()) {
                    Text(
                        text = "«${exercise.ruText}»",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilledTonalButton(
                        onClick = { onPlayAudio(exercise.geText, false) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Образец")
                    }

                    OutlinedButton(
                        onClick = { onPlayAudio(exercise.geText, true) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("0.6x")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Microphone Button & Waveform
        val isListening = speechState is SpeechState.Listening
        val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
        val pulseScale by if (isListening) {
            infiniteTransition.animateFloat(
                initialValue = 1.0f,
                targetValue = 1.18f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "mic_scale"
            )
        } else {
            rememberUpdatedState(1.0f)
        }

        Box(
            modifier = Modifier
                .size(96.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(if (isListening) GeorgianError else GeorgianWinePrimary)
                .clickable {
                    if (isListening) {
                        onStopListening()
                    } else {
                        if (hasPermission) {
                            onStartListening(exercise.geText)
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                }
                .testTag("microphone_record_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = "Запись голоса",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = when (speechState) {
                is SpeechState.Idle -> "Нажмите на микрофон и произнесите фразу"
                is SpeechState.Listening -> "Слушаем ваш голос... Говорите!"
                is SpeechState.Processing -> "Анализируем грузинское произношение..."
                is SpeechState.Result -> "Готово! Результат проверен."
                is SpeechState.Error -> speechState.message
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (speechState is SpeechState.Error) GeorgianError else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        // Pronunciation evaluation result feedback card
        if (speechState is SpeechState.Result) {
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (speechState.isPassed) CaucasusEmerald.copy(alpha = 0.12f) else AmberGold.copy(alpha = 0.15f),
                border = BorderStroke(1.5.dp, if (speechState.isPassed) CaucasusEmerald else AmberGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (speechState.isPassed) "✅ Точность: ${speechState.accuracyScore}%" else "⚠️ Точность: ${speechState.accuracyScore}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (speechState.isPassed) CaucasusEmerald else AmberGold
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (speechState.isPassed) CaucasusEmerald else AmberGold
                        ) {
                            Text(
                                text = if (speechState.isPassed) "ЗАЧТЕНО" else "ПОВТОРИТЕ",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Вы сказали: «${speechState.spokenText}»",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = speechState.feedbackComment,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun ReadingComprehensionExerciseView(
    exercise: Exercise,
    selectedOption: String?,
    onSelectOption: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showRussianHelp by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = exercise.prompt,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Passage Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = exercise.readingPassageGe,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 26.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (showRussianHelp && exercise.readingPassageRu.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = exercise.readingPassageRu,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = { showRussianHelp = !showRussianHelp },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = if (showRussianHelp) "Скрыть подсказку" else "Подсказка перевода",
                        style = MaterialTheme.typography.labelSmall,
                        color = GeorgianWinePrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Выберите правильный ответ:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        exercise.options.forEachIndexed { index, option ->
            val isSelected = selectedOption == option
            Surface(
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    if (isSelected) 2.dp else 1.dp,
                    if (isSelected) GeorgianWinePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                ),
                color = if (isSelected) GeorgianWinePrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onSelectOption(option) }
                    .testTag("reading_option_$index")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun MultipleChoiceExerciseView(
    exercise: Exercise,
    selectedOption: String?,
    onSelectOption: (String) -> Unit,
    onPlayAudio: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = exercise.prompt,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(14.dp))

        val promptText = if (exercise.geText.isNotEmpty()) exercise.geText else exercise.ruText
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(18.dp)
            ) {
                if (exercise.geText.isNotEmpty()) {
                    IconButton(
                        onClick = { onPlayAudio(exercise.geText, false) },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(GeorgianWinePrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Озвучить",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = promptText,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (exercise.transcriptionRu.isNotEmpty()) {
                        Text(
                            text = exercise.transcriptionRu,
                            style = MaterialTheme.typography.bodyMedium,
                            color = GeorgianWinePrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        exercise.options.forEachIndexed { index, option ->
            val isSelected = selectedOption == option
            Surface(
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    if (isSelected) 2.dp else 1.dp,
                    if (isSelected) GeorgianWinePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                ),
                color = if (isSelected) GeorgianWinePrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onSelectOption(option) }
                    .testTag("option_item_$index")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .border(
                                2.dp,
                                if (isSelected) GeorgianWinePrimary else MaterialTheme.colorScheme.outline,
                                CircleShape
                            )
                            .background(if (isSelected) GeorgianWinePrimary else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun MatchPairsExerciseView(
    exercise: Exercise,
    firstSelected: String?,
    matchedPairs: Map<String, String>,
    onItemClick: (String) -> Unit,
    onPlayAudio: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = exercise.prompt,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        val allItems = (exercise.pairs.keys + exercise.pairs.values).toList()

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(allItems) { item ->
                val isMatched = matchedPairs.containsKey(item) || matchedPairs.containsValue(item)
                val isSelected = firstSelected == item

                val bgCol = when {
                    isMatched -> CaucasusEmerald.copy(alpha = 0.15f)
                    isSelected -> AmberGold.copy(alpha = 0.2f)
                    else -> MaterialTheme.colorScheme.surface
                }
                val borderCol = when {
                    isMatched -> CaucasusEmerald
                    isSelected -> AmberGold
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(2.dp, borderCol),
                    color = bgCol,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clickable(enabled = !isMatched) {
                            onItemClick(item)
                            if (exercise.pairs.containsKey(item)) {
                                onPlayAudio(item, false)
                            }
                        }
                        .testTag("pair_tile_$item")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = item,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = when {
                                isMatched -> CaucasusEmerald
                                isSelected -> AmberGold
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AudioListenExerciseView(
    exercise: Exercise,
    selectedOption: String?,
    onSelectOption: (String) -> Unit,
    onPlayAudio: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = exercise.prompt,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = GeorgianWinePrimary,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .size(80.dp)
                    .clickable { onPlayAudio(exercise.audioText, false) }
                    .testTag("audio_normal_play")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Воспроизвести",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = AmberGold,
                shadowElevation = 3.dp,
                modifier = Modifier
                    .size(60.dp)
                    .clickable { onPlayAudio(exercise.audioText, true) }
                    .testTag("audio_slow_play")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Замедленно 0.6x",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        exercise.options.forEachIndexed { index, option ->
            val isSelected = selectedOption == option
            Surface(
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    if (isSelected) 2.dp else 1.dp,
                    if (isSelected) GeorgianWinePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                ),
                color = if (isSelected) GeorgianWinePrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onSelectOption(option) }
                    .testTag("audio_option_$index")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
