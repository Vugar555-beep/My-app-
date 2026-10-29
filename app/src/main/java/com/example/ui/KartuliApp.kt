package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.KartuliTopBar
import com.example.ui.screens.*
import com.example.ui.theme.AmberGold
import com.example.ui.theme.GeorgianWinePrimary

enum class KartuliTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    LEARN("Обучение", Icons.Filled.School, Icons.Outlined.School),
    ALPHABET("Алфавит", Icons.Filled.Spellcheck, Icons.Outlined.Spellcheck),
    VOCABULARY("Словарь", Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
    DIALOGUES("Диалоги", Icons.Filled.Forum, Icons.Outlined.Forum),
    GRAMMAR("Грамматика", Icons.Filled.AutoStories, Icons.Outlined.AutoStories),
    MISTAKES("Ошибки", Icons.Filled.Build, Icons.Outlined.Build),
    PROFILE("Профиль", Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun KartuliApp(
    viewModel: KartuliViewModel,
    modifier: Modifier = Modifier
) {
    val userProgress by viewModel.userProgress.collectAsStateWithLifecycle()
    val activeLesson by viewModel.activeLesson.collectAsStateWithLifecycle()
    val currentExerciseIndex by viewModel.currentExerciseIndex.collectAsStateWithLifecycle()
    val selectedOptions by viewModel.selectedOptions.collectAsStateWithLifecycle()
    val selectedBubbles by viewModel.selectedBubbles.collectAsStateWithLifecycle()
    val firstSelectedPairItem by viewModel.firstSelectedPairItem.collectAsStateWithLifecycle()
    val matchedPairs by viewModel.matchedPairs.collectAsStateWithLifecycle()
    val speechState by viewModel.speechState.collectAsStateWithLifecycle()
    val feedback by viewModel.feedback.collectAsStateWithLifecycle()
    val isLessonCompleted by viewModel.isLessonCompleted.collectAsStateWithLifecycle()
    val lessonMistakesCount by viewModel.lessonMistakesCount.collectAsStateWithLifecycle()
    val vocabSearchQuery by viewModel.vocabSearchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val activeDialogue by viewModel.activeDialogue.collectAsStateWithLifecycle()
    val currentDialogueTurnIndex by viewModel.currentDialogueTurnIndex.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(KartuliTab.LEARN) }
    var showHeartsRefillDialog by remember { mutableStateOf(false) }
    var showStatisticsScreen by remember { mutableStateOf(false) }

    // Back handler
    BackHandler(enabled = activeLesson == null && activeDialogue == null && (showStatisticsScreen || currentTab != KartuliTab.LEARN)) {
        if (showStatisticsScreen) {
            showStatisticsScreen = false
        } else {
            currentTab = KartuliTab.LEARN
        }
    }

    // Refill Hearts Modal
    if (showHeartsRefillDialog) {
        AlertDialog(
            onDismissRequest = { showHeartsRefillDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = { Text("Сердца: ${userProgress.hearts}/${userProgress.maxHearts}") },
            text = {
                Column {
                    Text("Каждое сердце дает право на одну ошибку в уроке. Сердца восстанавливаются при работе над ошибками или за монеты!")
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Стоимость мгновенного восполнения: 20 монет (баланс: ${userProgress.coins})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (viewModel.refillHeartsWithCoins()) {
                            showHeartsRefillDialog = false
                        }
                    },
                    enabled = userProgress.coins >= 20 && userProgress.hearts < userProgress.maxHearts,
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold)
                ) {
                    Text("Купить за 20 монет", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showHeartsRefillDialog = false
                        currentTab = KartuliTab.MISTAKES
                    }
                ) {
                    Text("Тренировать ошибки бесплатно")
                }
            }
        )
    }

    // Full screen views
    if (activeLesson != null) {
        LessonScreen(
            lesson = activeLesson!!,
            currentExerciseIndex = currentExerciseIndex,
            selectedOptions = selectedOptions,
            selectedBubbles = selectedBubbles,
            firstSelectedPairItem = firstSelectedPairItem,
            matchedPairs = matchedPairs,
            speechState = speechState,
            feedback = feedback,
            isCompleted = isLessonCompleted,
            mistakesCount = lessonMistakesCount,
            userProgress = userProgress,
            onSelectOption = { viewModel.selectOption(it) },
            onAddBubble = { viewModel.addBubble(it) },
            onRemoveBubble = { viewModel.removeBubble(it) },
            onPairItemClick = { viewModel.onPairItemClicked(it, activeLesson!!.exercises[currentExerciseIndex]) },
            onStartSpeechListening = { viewModel.startSpeechPractice(it) },
            onStopSpeechListening = { viewModel.stopSpeechPractice() },
            onCheckAnswer = { viewModel.checkAnswer() },
            onNextExercise = { viewModel.nextExercise() },
            onCloseLesson = { viewModel.closeLesson() },
            onPlayAudio = { text, isSlow -> viewModel.playSpeech(text, isSlow) },
            onRefillHearts = { viewModel.refillHeartsWithCoins() }
        )
    } else if (activeDialogue != null) {
        ActiveDialogueSession(
            scenario = activeDialogue!!,
            currentTurnIndex = currentDialogueTurnIndex,
            onAnswerTurn = { viewModel.answerDialogue(it) },
            onPlayAudio = { viewModel.playSpeech(it) },
            onClose = { viewModel.closeDialogue() }
        )
    } else if (showStatisticsScreen) {
        Scaffold(
            topBar = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    IconButton(onClick = { showStatisticsScreen = false }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                    Text(
                        text = "Подробная статистика",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        ) { padding ->
            StatisticsScreen(
                stats = viewModel.getStatistics(),
                modifier = Modifier.padding(padding)
            )
        }
    } else {
        Scaffold(
            topBar = {
                KartuliTopBar(
                    userProgress = userProgress,
                    onHeartsClick = { showHeartsRefillDialog = true },
                    onStreakClick = { currentTab = KartuliTab.PROFILE }
                )
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    KartuliTab.values().forEach { tab ->
                        val isSelected = currentTab == tab
                        val mistakesCount = userProgress.activeMistakeExerciseIds.size
                        val showBadge = tab == KartuliTab.MISTAKES && mistakesCount > 0

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentTab = tab },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (showBadge) {
                                            Badge { Text("$mistakesCount") }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.title
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = GeorgianWinePrimary,
                                selectedTextColor = GeorgianWinePrimary,
                                indicatorColor = GeorgianWinePrimary.copy(alpha = 0.12f)
                            ),
                            modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    KartuliTab.LEARN -> {
                        LearningMapScreen(
                            modules = viewModel.allModules,
                            userProgress = userProgress,
                            onStartLesson = { lesson -> viewModel.startLesson(lesson) },
                            onStartDailySmartPractice = { viewModel.startDailySmartPractice() },
                            onOpenAlphabet = { currentTab = KartuliTab.ALPHABET },
                            onOpenDialogues = { currentTab = KartuliTab.DIALOGUES },
                            onOpenMistakes = { currentTab = KartuliTab.MISTAKES }
                        )
                    }
                    KartuliTab.ALPHABET -> {
                        AlphabetScreen(
                            letters = viewModel.allLetters,
                            onPlayAudio = { text, isSlow -> viewModel.playSpeech(text, isSlow) }
                        )
                    }
                    KartuliTab.VOCABULARY -> {
                        VocabularyScreen(
                            vocabulary = viewModel.allVocabulary,
                            bookmarkedIds = userProgress.bookmarkedWordIds,
                            searchQuery = vocabSearchQuery,
                            selectedCategory = selectedCategory,
                            onSearchChange = { viewModel.setVocabSearchQuery(it) },
                            onCategoryChange = { viewModel.setSelectedCategory(it) },
                            onToggleBookmark = { viewModel.toggleBookmark(it) },
                            onPlayAudio = { text, isSlow -> viewModel.playSpeech(text, isSlow) }
                        )
                    }
                    KartuliTab.DIALOGUES -> {
                        DialogueScreen(
                            scenarios = viewModel.allDialogues,
                            completedIds = userProgress.completedDialogueIds,
                            onSelectScenario = { viewModel.startDialogue(it) }
                        )
                    }
                    KartuliTab.GRAMMAR -> {
                        GrammarScreen(
                            rules = viewModel.allGrammarRules
                        )
                    }
                    KartuliTab.MISTAKES -> {
                        val allEx = viewModel.allModules.flatMap { it.lessons }.flatMap { it.exercises }
                        val activeMistakeExercises = allEx.filter { userProgress.activeMistakeExerciseIds.contains(it.id) }
                        MistakesReviewScreen(
                            mistakeExercises = activeMistakeExercises,
                            onStartMistakesPractice = { viewModel.startMistakesPractice() }
                        )
                    }
                    KartuliTab.PROFILE -> {
                        ProfileScreen(
                            userProgress = userProgress,
                            achievements = viewModel.allAchievements,
                            dailyQuests = viewModel.dailyQuests,
                            onSetDailyGoal = { viewModel.setDailyGoal(it) },
                            onOpenStatistics = { showStatisticsScreen = true },
                            onToggleSound = { viewModel.toggleSound() },
                            onToggleHaptics = { viewModel.toggleHaptics() },
                            onToggleTheme = { viewModel.toggleTheme(it) }
                        )
                    }
                }
            }
        }
    }
}
