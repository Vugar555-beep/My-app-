package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Star
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
import com.example.data.model.ExerciseType
import com.example.data.model.Lesson
import com.example.data.model.UserProgress
import com.example.ui.LessonFeedback
import com.example.ui.components.*
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CaucasusEmerald
import com.example.ui.theme.GeorgianWinePrimary
import com.example.util.SpeechState

@Composable
fun LessonScreen(
    lesson: Lesson,
    currentExerciseIndex: Int,
    selectedOptions: List<String>,
    selectedBubbles: List<String>,
    firstSelectedPairItem: String?,
    matchedPairs: Map<String, String>,
    speechState: SpeechState,
    feedback: LessonFeedback?,
    isCompleted: Boolean,
    mistakesCount: Int,
    userProgress: UserProgress,
    onSelectOption: (String) -> Unit,
    onAddBubble: (String) -> Unit,
    onRemoveBubble: (Int) -> Unit,
    onPairItemClick: (String) -> Unit,
    onStartSpeechListening: (String) -> Unit,
    onStopSpeechListening: () -> Unit,
    onCheckAnswer: () -> Unit,
    onNextExercise: () -> Unit,
    onCloseLesson: () -> Unit,
    onPlayAudio: (String, Boolean) -> Unit,
    onRefillHearts: () -> Boolean,
    modifier: Modifier = Modifier
) {
    var showQuitDialog by remember { mutableStateOf(false) }

    BackHandler {
        if (!isCompleted) {
            showQuitDialog = true
        } else {
            onCloseLesson()
        }
    }

    if (showQuitDialog) {
        AlertDialog(
            onDismissRequest = { showQuitDialog = false },
            title = { Text("Выйти из урока?") },
            text = { Text("Прогресс этого урока будет утерян. Сосо будет ждать вашего возвращения!") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showQuitDialog = false
                        onCloseLesson()
                    }
                ) {
                    Text("Выйти", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuitDialog = false }) {
                    Text("Продолжить")
                }
            }
        )
    }

    if (isCompleted) {
        LessonCompletedCelebration(
            lesson = lesson,
            mistakesCount = mistakesCount,
            onFinish = onCloseLesson
        )
        return
    }

    if (userProgress.hearts <= 0) {
        OutOfHeartsDialog(
            coins = userProgress.coins,
            onRefill = onRefillHearts,
            onClose = onCloseLesson
        )
        return
    }

    val currentExercise = lesson.exercises.getOrNull(currentExerciseIndex)

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = { showQuitDialog = true },
                        modifier = Modifier.testTag("lesson_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть урок",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    val progressRatio = (currentExerciseIndex.toFloat() / lesson.exercises.size.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progressRatio },
                        modifier = Modifier
                            .weight(1f)
                            .height(12.dp)
                            .padding(horizontal = 12.dp)
                            .clip(CircleShape),
                        color = if (lesson.isExamOrCheckpoint) AmberGold else CaucasusEmerald,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${userProgress.hearts}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (feedback == null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    val canCheck = when (currentExercise?.type) {
                        ExerciseType.WORD_BUBBLES -> selectedBubbles.isNotEmpty()
                        ExerciseType.MATCH_PAIRS -> matchedPairs.size == (currentExercise.pairs.size)
                        ExerciseType.SPEAK_PRACTICE -> speechState is SpeechState.Result
                        else -> selectedOptions.isNotEmpty()
                    }

                    Button(
                        onClick = onCheckAnswer,
                        enabled = canCheck,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (lesson.isExamOrCheckpoint) AmberGold else CaucasusEmerald,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("lesson_check_button")
                    ) {
                        Text(
                            text = "ПРОВЕРИТЬ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (canCheck) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                BottomFeedbackSheet(
                    feedback = feedback,
                    onContinue = onNextExercise
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (currentExercise != null) {
                when (currentExercise.type) {
                    ExerciseType.WORD_BUBBLES -> {
                        WordBubblesExerciseView(
                            exercise = currentExercise,
                            selectedBubbles = selectedBubbles,
                            onAddBubble = onAddBubble,
                            onRemoveBubble = onRemoveBubble,
                            onPlayAudio = onPlayAudio
                        )
                    }
                    ExerciseType.SPEAK_PRACTICE -> {
                        SpeakPracticeExerciseView(
                            exercise = currentExercise,
                            speechState = speechState,
                            onStartListening = onStartSpeechListening,
                            onStopListening = onStopSpeechListening,
                            onPlayAudio = onPlayAudio
                        )
                    }
                    ExerciseType.READING_COMPREHENSION -> {
                        ReadingComprehensionExerciseView(
                            exercise = currentExercise,
                            selectedOption = selectedOptions.firstOrNull(),
                            onSelectOption = onSelectOption
                        )
                    }
                    ExerciseType.MATCH_PAIRS -> {
                        MatchPairsExerciseView(
                            exercise = currentExercise,
                            firstSelected = firstSelectedPairItem,
                            matchedPairs = matchedPairs,
                            onItemClick = onPairItemClick,
                            onPlayAudio = onPlayAudio
                        )
                    }
                    ExerciseType.AUDIO_LISTEN -> {
                        AudioListenExerciseView(
                            exercise = currentExercise,
                            selectedOption = selectedOptions.firstOrNull(),
                            onSelectOption = onSelectOption,
                            onPlayAudio = onPlayAudio
                        )
                    }
                    else -> {
                        MultipleChoiceExerciseView(
                            exercise = currentExercise,
                            selectedOption = selectedOptions.firstOrNull(),
                            onSelectOption = onSelectOption,
                            onPlayAudio = onPlayAudio
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LessonCompletedCelebration(
    lesson: Lesson,
    mistakesCount: Int,
    onFinish: () -> Unit
) {
    val totalQuestions = lesson.exercises.size
    val correctCount = (totalQuestions - mistakesCount).coerceAtLeast(0)
    val accuracy = if (totalQuestions > 0) ((correctCount.toFloat() / totalQuestions) * 100).toInt() else 100
    val xpEarned = if (mistakesCount == 0) lesson.xpReward + 5 else lesson.xpReward

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(AmberGold),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (lesson.isExamOrCheckpoint) "Экзамен успешно сдан!" else "Урок завершен!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "ყოჩაღ! Сосо гордится твоими успехами!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = AmberGold.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "ОПЫТ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AmberGold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "+$xpEarned XP",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AmberGold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CaucasusEmerald.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, CaucasusEmerald.copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "ТОЧНОСТЬ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CaucasusEmerald
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$accuracy%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = CaucasusEmerald
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = GeorgianWinePrimary.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, GeorgianWinePrimary.copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "МОНЕТЫ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = GeorgianWinePrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "+${lesson.coinReward}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = GeorgianWinePrimary
                        )
                    }
                }
            }
        }

        Button(
            onClick = onFinish,
            colors = ButtonDefaults.buttonColors(containerColor = CaucasusEmerald),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("celebration_finish_button")
        ) {
            Text(
                text = "ПРОДОЛЖИТЬ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun OutOfHeartsDialog(
    coins: Int,
    onRefill: () -> Boolean,
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Text(
                text = "Закончились сердца!",
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEF4444)
            )
        },
        text = {
            Column {
                Text("У вас не осталось попыток на этот урок. Вы можете восстановить все сердца за 20 монет или потренироваться в разделе «Ошибки»!")
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Баланс: $coins монет",
                    fontWeight = FontWeight.SemiBold,
                    color = AmberGold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onRefill() },
                enabled = coins >= 20,
                colors = ButtonDefaults.buttonColors(containerColor = AmberGold)
            ) {
                Text("Пополнить (20 монет)", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text("Вернуться на карту")
            }
        }
    )
}
