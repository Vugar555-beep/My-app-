package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Lesson
import com.example.data.model.Module
import com.example.data.model.UserProgress
import com.example.ui.components.SosoMascot
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CaucasusEmerald
import com.example.ui.theme.GeorgianWinePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningMapScreen(
    modules: List<Module>,
    userProgress: UserProgress,
    onStartLesson: (Lesson) -> Unit,
    onStartDailySmartPractice: () -> Unit,
    onOpenAlphabet: () -> Unit,
    onOpenDialogues: () -> Unit,
    onOpenMistakes: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedLessonForModal by remember { mutableStateOf<Lesson?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Mascot Greeting Header
        item {
            SosoMascot(
                speechGe = "გაუმარჯოს! მზად ხარ?",
                speechRu = "Здравствуй! Готов покорять грузинский язык?",
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Daily Goal and Quick Action Banner
        item {
            DailyProgressCard(
                todayXp = userProgress.todayXp,
                goalXp = userProgress.dailyGoalXp,
                streak = userProgress.streakDays,
                mistakesCount = userProgress.activeMistakeExerciseIds.size,
                onOpenMistakes = onOpenMistakes,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // Smart Practice & Dialogue Shortcuts Row
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = GeorgianWinePrimary,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onStartDailySmartPractice)
                        .testTag("smart_practice_banner")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = AmberGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Умная практика",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Персональный подбор",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onOpenDialogues)
                        .testTag("dialogues_banner")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forum,
                            contentDescription = null,
                            tint = GeorgianWinePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Диалоги",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "В кафе и такси",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Quick Alphabet Shortcut Banner
        item {
            AlphabetBanner(
                onOpenAlphabet = onOpenAlphabet,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // Modules & Winding Levels Tree
        itemsIndexed(modules) { moduleIndex, module ->
            ModuleSection(
                module = module,
                completedLessonIds = userProgress.completedLessonIds,
                isModuleUnlocked = userProgress.unlockedModuleIds.contains(module.id) || moduleIndex == 0,
                onSelectLesson = { lesson ->
                    selectedLessonForModal = lesson
                }
            )
        }
    }

    // Lesson Detail Bottom Sheet Dialog
    selectedLessonForModal?.let { lesson ->
        val isCompleted = userProgress.completedLessonIds.contains(lesson.id)
        ModalBottomSheet(
            onDismissRequest = { selectedLessonForModal = null },
            sheetState = rememberModalBottomSheetState(),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(if (isCompleted) AmberGold else GeorgianWinePrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.EmojiEvents else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = lesson.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AmberGold.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "+${lesson.xpReward} XP",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = AmberGold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CaucasusEmerald.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "+${lesson.coinReward} Лари",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = CaucasusEmerald,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val target = lesson
                        selectedLessonForModal = null
                        onStartLesson(target)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GeorgianWinePrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("start_lesson_modal_button")
                ) {
                    Text(
                        text = if (isCompleted) "ПОВТОРИТЬ УРОК" else "НАЧАТЬ УРОК",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun DailyProgressCard(
    todayXp: Int,
    goalXp: Int,
    streak: Int,
    mistakesCount: Int,
    onOpenMistakes: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = AmberGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ежедневная цель",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "$todayXp / $goalXp XP",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val progress = (todayXp.toFloat() / goalXp.toFloat()).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = AmberGold,
                trackColor = AmberGold.copy(alpha = 0.2f)
            )

            if (mistakesCount > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable(onClick = onOpenMistakes)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = GeorgianWinePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ошибок для отработки: $mistakesCount",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "Повторить",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = GeorgianWinePrimary
                    )
                }
            }
        }
    }
}

@Composable
fun AlphabetBanner(
    onOpenAlphabet: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenAlphabet)
            .testTag("alphabet_banner_card")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ა-ჰ",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Грузинский алфавит Мхедрули",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "33 буквы, произношение, гортанные звуки",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ModuleSection(
    module: Module,
    completedLessonIds: Set<String>,
    isModuleUnlocked: Boolean,
    onSelectLesson: (Lesson) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Module Banner Header
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = if (isModuleUnlocked) GeorgianWinePrimary else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${module.levelTag} • ${module.locationName}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isModuleUnlocked) AmberGold else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = module.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isModuleUnlocked) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = module.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isModuleUnlocked) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!isModuleUnlocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Заблокировано",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Lessons Winding Nodes
        module.lessons.forEachIndexed { index, lesson ->
            val isCompleted = completedLessonIds.contains(lesson.id)
            val isCurrent = isModuleUnlocked && !isCompleted &&
                    (index == 0 || completedLessonIds.contains(module.lessons[index - 1].id))
            val isLocked = !isModuleUnlocked || (!isCompleted && !isCurrent)

            // Dynamic horizontal zigzag offset for Duolingo-like winding trail
            val xOffset = when (index % 4) {
                0 -> 0.dp
                1 -> 45.dp
                2 -> 0.dp
                3 -> (-45).dp
                else -> 0.dp
            }

            Box(
                modifier = Modifier
                    .offset(x = xOffset)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                LessonCircleNode(
                    lesson = lesson,
                    isCompleted = isCompleted,
                    isCurrent = isCurrent,
                    isLocked = isLocked,
                    onClick = {
                        if (!isLocked) onSelectLesson(lesson)
                    }
                )
            }
        }
    }
}

@Composable
fun LessonCircleNode(
    lesson: Lesson,
    isCompleted: Boolean,
    isCurrent: Boolean,
    isLocked: Boolean,
    onClick: () -> Unit
) {
    val nodeSize = 74.dp
    val bgColor = when {
        isCompleted -> AmberGold
        isCurrent -> GeorgianWinePrimary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val iconVector: ImageVector = when {
        isCompleted -> Icons.Default.Check
        isLocked -> Icons.Default.Lock
        else -> Icons.Default.Star
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.testTag("lesson_node_${lesson.id}")
    ) {
        Surface(
            shape = CircleShape,
            color = bgColor,
            shadowElevation = if (isCurrent) 8.dp else 4.dp,
            border = if (isCurrent) BorderStroke(4.dp, AmberGold) else null,
            modifier = Modifier
                .size(nodeSize)
                .clickable(enabled = !isLocked, onClick = onClick)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = lesson.title,
                    tint = if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = lesson.title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            color = if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 120.dp)
        )
    }
}
