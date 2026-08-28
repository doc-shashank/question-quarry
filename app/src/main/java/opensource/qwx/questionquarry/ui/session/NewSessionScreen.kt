package opensource.qwx.questionquarry.ui.session

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import opensource.qwx.questionquarry.ui.components.CanvasEditor
import opensource.qwx.questionquarry.ui.theme.QuestionQuarryTheme

enum class SessionStep {
    DETAILS, QUESTION, ANSWER
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSessionScreen(
    title: String,
    viewModel: SessionViewModel,
    onBack: () -> Unit,
    onComplete: (String, List<SessionViewModel.QAPair>, String, String, String, String, Boolean) -> Unit,
    onNavigateToTextEditor: (Int, Int, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    sessionId: Long? = null,
) {
    LaunchedEffect(sessionId) {
        if (sessionId != null && (viewModel.editingSessionId != sessionId)) {
            viewModel.loadSessionForEditing(sessionId)
        }
    }

    var currentStep by rememberSaveable { mutableStateOf(SessionStep.DETAILS) }
    var currentPairIndex by rememberSaveable { mutableIntStateOf(0) }
    
    var showFinishDialog by remember { mutableStateOf(false) }
    var showSaveConfirmDialog by remember { mutableStateOf(false) }
    var pendingBackAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var expandedSwitcher by remember { mutableStateOf(false) }

    val pairs = viewModel.draftPairs
    
    // Capture initial state for change detection
    var originalPairs by remember { mutableStateOf<List<SessionViewModel.QAPair>?>(null) }
    var originalSubject by remember { mutableStateOf("") }
    var originalChNo by remember { mutableStateOf("") }
    var originalChName by remember { mutableStateOf("") }
    var originalTopic by remember { mutableStateOf("") }
    var originalTopicEnabled by remember { mutableStateOf(false) }
    
    LaunchedEffect(viewModel.editingSessionId) {
        if (sessionId == null || viewModel.editingSessionId == sessionId) {
            originalPairs = pairs.toList()
            originalSubject = viewModel.draftSubject
            originalChNo = viewModel.draftChapterNumber
            originalChName = viewModel.draftChapterName
            originalTopic = viewModel.draftTopic
            originalTopicEnabled = viewModel.isTopicEnabled
        }
    }

    fun handleBack() {
        val hasChanges = originalPairs != null && (
                pairs != originalPairs ||
                viewModel.draftSubject != originalSubject ||
                viewModel.draftChapterNumber != originalChNo ||
                viewModel.draftChapterName != originalChName ||
                viewModel.draftTopic != originalTopic ||
                viewModel.isTopicEnabled != originalTopicEnabled
        )

        if (viewModel.editingSessionId != null && hasChanges) {
            showSaveConfirmDialog = true
            pendingBackAction = onBack
        } else {
            onBack()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = when (currentStep) {
                                SessionStep.DETAILS -> "SESSION DETAILS"
                                SessionStep.QUESTION -> "QUESTION"
                                SessionStep.ANSWER -> "ANSWER"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            letterSpacing = 1.2.sp
                        )
                        
                        if (currentStep != SessionStep.DETAILS) {
                            Surface(
                                onClick = { expandedSwitcher = true },
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Pair ${currentPairIndex + 1}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Icon(
                                        if (expandedSwitcher) Icons.Rounded.ArrowDropUp else Icons.Rounded.ArrowDropDown,
                                        null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }

                        DropdownMenu(
                            expanded = expandedSwitcher,
                            onDismissRequest = { expandedSwitcher = false }
                        ) {
                            pairs.forEachIndexed { index, _ ->
                                DropdownMenuItem(
                                    text = { Text("Pair ${index + 1}") },
                                    onClick = {
                                        currentPairIndex = index
                                        currentStep = SessionStep.QUESTION
                                        expandedSwitcher = false
                                    },
                                    leadingIcon = {
                                        if (index == currentPairIndex) {
                                            Icon(Icons.Rounded.Check, null, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                )
                            }
                        }
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                navigationIcon = {
                    IconButton(
                        onClick = {
                            when (currentStep) {
                                SessionStep.DETAILS -> handleBack()
                                SessionStep.QUESTION -> {
                                    if (currentPairIndex > 0) {
                                        currentPairIndex--
                                        currentStep = SessionStep.ANSWER
                                    } else {
                                        currentStep = SessionStep.DETAILS
                                    }
                                }
                                SessionStep.ANSWER -> currentStep = SessionStep.QUESTION
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            when (currentStep) {
                                SessionStep.DETAILS -> currentStep = SessionStep.QUESTION
                                SessionStep.QUESTION -> currentStep = SessionStep.ANSWER
                                SessionStep.ANSWER -> showFinishDialog = true
                            }
                        },
                        enabled = when (currentStep) {
                            SessionStep.DETAILS -> true
                            SessionStep.QUESTION -> if (currentPairIndex < pairs.size) pairs[currentPairIndex].questionBlocks.isNotEmpty() else false
                            SessionStep.ANSWER -> if (currentPairIndex < pairs.size) pairs[currentPairIndex].answerBlocks.isNotEmpty() else false
                        }
                    ) {
                        Text(if (currentStep == SessionStep.ANSWER) "Finish" else "Next")
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, modifier = Modifier.size(18.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (showFinishDialog) {
            AlertDialog(
                onDismissRequest = { showFinishDialog = false },
                title = { Text("Finish Session?") },
                text = { 
                    Text("Would you like to add another pair, or finish the session now?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onComplete(
                                title, 
                                viewModel.draftPairs,
                                viewModel.draftSubject,
                                viewModel.draftChapterNumber,
                                viewModel.draftChapterName,
                                viewModel.draftTopic,
                                viewModel.isTopicEnabled
                            )
                            showFinishDialog = false
                        }
                    ) {
                        Text("Finish")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            val newPairs = viewModel.draftPairs + SessionViewModel.QAPair(emptyList(), emptyList())
                            viewModel.updateDraftPairs(newPairs)
                            currentPairIndex = newPairs.size - 1
                            currentStep = SessionStep.QUESTION
                            showFinishDialog = false
                        }
                    ) {
                        Text("Add Another Pair")
                    }
                }
            )
        }

        if (showSaveConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showSaveConfirmDialog = false },
                title = { Text("Exit without saving?") },
                text = { Text("You have unsaved changes. These will be lost if you leave now.") },
                confirmButton = {
                    TextButton(onClick = {
                        showSaveConfirmDialog = false
                        pendingBackAction?.invoke()
                    }) {
                        Text("Exit & Discard")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showSaveConfirmDialog = false
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                LinearProgressIndicator(
                    progress = { 
                        when (currentStep) {
                            SessionStep.DETAILS -> 0.33f
                            SessionStep.QUESTION -> 0.66f
                            SessionStep.ANSWER -> 1f
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                
                AnimatedContent(
                    targetState = currentStep,
                    label = "stepTransition",
                    modifier = Modifier.weight(1f),
                    transitionSpec = {
                        fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(250))
                    }
                ) { step ->
                    when (step) {
                        SessionStep.DETAILS -> {
                            SessionDetailsEditor(viewModel = viewModel)
                        }
                        SessionStep.QUESTION -> {
                            CanvasEditor(
                                blocks = pairs[currentPairIndex].questionBlocks,
                                onBlocksChange = { newBlocks ->
                                    val newPairs = pairs.toMutableList()
                                    newPairs[currentPairIndex] = newPairs[currentPairIndex].copy(questionBlocks = newBlocks)
                                    viewModel.updateDraftPairs(newPairs)
                                },
                                onEditText = { blockIndex ->
                                    onNavigateToTextEditor(currentPairIndex, blockIndex, true)
                                }
                            )
                        }
                        SessionStep.ANSWER -> {
                            CanvasEditor(
                                blocks = pairs[currentPairIndex].answerBlocks,
                                onBlocksChange = { newBlocks ->
                                    val newPairs = pairs.toMutableList()
                                    newPairs[currentPairIndex] = newPairs[currentPairIndex].copy(answerBlocks = newBlocks)
                                    viewModel.updateDraftPairs(newPairs)
                                },
                                onEditText = { blockIndex ->
                                    onNavigateToTextEditor(currentPairIndex, blockIndex, false)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SessionDetailsEditor(viewModel: SessionViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Session Context",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            "Provide additional context for this session. These details will be associated with all questions created here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        if (viewModel.editingSessionId != null && viewModel.draftSubject.isBlank()) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "This question is untagged.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        var isSubjectFocused by remember { mutableStateOf(false) }
        RecommendationTextField(
            value = viewModel.draftSubject,
            onValueChange = { viewModel.draftSubject = it },
            label = "Subject",
            placeholder = "e.g. Physics, History, Math",
            icon = Icons.Rounded.Book,
            recommendations = if (isSubjectFocused) viewModel.getRecommendations(viewModel.draftSubject, RecommendationType.SUBJECT) else emptyList(),
            modifier = Modifier.onFocusChanged { isSubjectFocused = it.isFocused }
        )

        val isSubjectFilled = viewModel.draftSubject.isNotBlank()

        if (isSubjectFocused && !isSubjectFilled) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Rounded.Info, 
                        null, 
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(24.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
                            .padding(4.dp)
                    )
                    Text(
                        "Fill in the Subject field to unlock Chapter and Topic tagging.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        var isChNoFocused by remember { mutableStateOf(false) }
        RecommendationTextField(
            value = viewModel.draftChapterNumber,
            onValueChange = { viewModel.draftChapterNumber = it },
            label = "Chapter Number",
            placeholder = "e.g. 1, 2a, etc.",
            icon = Icons.Rounded.Numbers,
            enabled = isSubjectFilled,
            recommendations = if (isChNoFocused) viewModel.getRecommendations(viewModel.draftChapterNumber, RecommendationType.CHAPTER_NUMBER) else emptyList(),
            modifier = Modifier.onFocusChanged { isChNoFocused = it.isFocused }
        )

        var isChNameFocused by remember { mutableStateOf(false) }
        RecommendationTextField(
            value = viewModel.draftChapterName,
            onValueChange = { viewModel.draftChapterName = it },
            label = "Chapter Name",
            placeholder = "e.g. Thermodynamics",
            icon = Icons.Rounded.Topic,
            enabled = isSubjectFilled,
            recommendations = if (isChNameFocused) viewModel.getRecommendations(viewModel.draftChapterName, RecommendationType.CHAPTER_NAME) else emptyList(),
            modifier = Modifier.onFocusChanged { isChNameFocused = it.isFocused }
        )

        // Preset Creation Notification
        if (isSubjectFilled) {
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Rounded.AutoAwesome, 
                        null, 
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier
                            .size(24.dp)
                            .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                            .padding(4.dp)
                    )
                    Text(
                        "Entering a new Subject/Chapter/Topic combination will automatically create a new preset for future use.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Tag, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Text("Enable Topic", style = MaterialTheme.typography.bodyLarge)
            }
            Switch(
                checked = viewModel.isTopicEnabled,
                onCheckedChange = { viewModel.isTopicEnabled = it }
            )
        }

        if (viewModel.isTopicEnabled) {
            var isTopicFocused by remember { mutableStateOf(false) }
            RecommendationTextField(
                value = viewModel.draftTopic,
                onValueChange = { viewModel.draftTopic = it },
                label = "Topic",
                placeholder = "e.g. Specific Heat Capacity",
                icon = Icons.Rounded.Tag,
                enabled = isSubjectFilled,
                recommendations = if (isTopicFocused) viewModel.getRecommendations(viewModel.draftTopic, RecommendationType.TOPIC) else emptyList(),
                modifier = Modifier.onFocusChanged { isTopicFocused = it.isFocused }
            )
        }
    }
}

@Composable
fun RecommendationTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    recommendations: List<String>,
    enabled: Boolean = true,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(icon, null) },
            singleLine = true,
            enabled = enabled
        )
        
        if (recommendations.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                recommendations.forEach { recommendation ->
                    SuggestionChip(
                        onClick = { onValueChange(recommendation) },
                        label = { 
                            Text(
                                recommendation,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            ) 
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewSessionScreenPreview() {
    QuestionQuarryTheme {
        // Mock UI
    }
}
