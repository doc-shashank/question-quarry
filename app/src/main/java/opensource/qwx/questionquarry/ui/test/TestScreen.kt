package opensource.qwx.questionquarry.ui.test

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.draw.clip
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.compose.components.markdownComponents
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.findChildOfType
import org.intellij.markdown.ast.getTextInNode
import opensource.qwx.questionquarry.util.StorageManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestScreen(
    sessionIds: List<Long>,
    viewModel: TestViewModel,
    onBack: () -> Unit
) {
    LaunchedEffect(sessionIds) {
        viewModel.loadSessions(sessionIds)
    }

    val context = LocalContext.current
    val storageManager = remember { StorageManager(context) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (!viewModel.isTestComplete && viewModel.flashcards.isNotEmpty()) {
                        Text(
                            text = "Question ${viewModel.currentIndex + 1} / ${viewModel.flashcards.size}",
                            style = MaterialTheme.typography.titleMedium
                        )
                    } else {
                        Text("Test Complete")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!viewModel.isTestComplete) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Timer,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = viewModel.formatTime(),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (viewModel.isTestComplete) {
                TestCompleteSummary(
                    totalQuestions = viewModel.flashcards.size,
                    timeTaken = viewModel.formatTime(),
                    onFinish = onBack
                )
            } else if (viewModel.flashcards.isNotEmpty()) {
                val currentCard = viewModel.flashcards[viewModel.currentIndex]
                
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Flashcard(
                            question = currentCard.questionContent,
                            answer = currentCard.answerContent,
                            isFlipped = viewModel.isFlipped,
                            onFlip = { viewModel.flip() },
                            storageManager = storageManager
                        )
                    }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { viewModel.flip() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            shape = MaterialTheme.shapes.extraLarge
                        ) {
                            Text(
                                if (viewModel.isFlipped) "Show Question" else "Show Answer",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        
                        FilledTonalButton(
                            onClick = { viewModel.next() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            shape = MaterialTheme.shapes.extraLarge
                        ) {
                            Text(
                                if (viewModel.currentIndex == viewModel.flashcards.size - 1) "Finish Test" else "Next Question",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            } else {
                Text(
                    "No questions found in selected sessions.",
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun Flashcard(
    question: String,
    answer: String,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    storageManager: StorageManager
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "CardFlip"
    )

    Card(
        onClick = onFlip,
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .fillMaxHeight(0.6f)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (rotation <= 90f) 
                MaterialTheme.colorScheme.surface
            else 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (rotation <= 90f) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "QUESTION",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        ),
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Markdown(
                            content = question,
                            modifier = Modifier.fillMaxWidth(),
                            components = markdownComponents(
                                image = { model ->
                                    val link = model.node.findChildOfType(MarkdownElementTypes.LINK_DESTINATION)?.getTextInNode(model.content)?.toString()
                                    val contentDescription = model.node.findChildOfType(MarkdownElementTypes.LINK_TEXT)?.getTextInNode(model.content)?.toString()
                                    if (link != null) {
                                        val file = remember(link) { storageManager.getFile(link) }
                                        AsyncImage(
                                            model = file ?: link,
                                            contentDescription = contentDescription,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(16.dp)),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }
                            )
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationY = 180f },
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = rotation > 90f,
                        enter = fadeIn(animationSpec = tween(250))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "ANSWER",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 2.sp
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Markdown(
                                    content = answer,
                                    modifier = Modifier.fillMaxWidth(),
                                    components = markdownComponents(
                                        image = { model ->
                                            val link = model.node.findChildOfType(MarkdownElementTypes.LINK_DESTINATION)?.getTextInNode(model.content)?.toString()
                                            val contentDescription = model.node.findChildOfType(MarkdownElementTypes.LINK_TEXT)?.getTextInNode(model.content)?.toString()
                                            if (link != null) {
                                                val file = remember(link) { storageManager.getFile(link) }
                                                AsyncImage(
                                                    model = file ?: link,
                                                    contentDescription = contentDescription,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(16.dp)),
                                                    contentScale = ContentScale.Fit
                                                )
                                            }
                                        }
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TestCompleteSummary(
    totalQuestions: Int,
    timeTaken: String,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(bottom = 32.dp),
            shape = RoundedCornerShape(40.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(Modifier.height(24.dp))
                
                Text(
                    "Test Complete!",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    "You reviewed $totalQuestions questions in total.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(32.dp))
                
                Surface(
                    color = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Time: $timeTaken",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        
        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(64.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Text("Back to Home", style = MaterialTheme.typography.titleMedium)
        }
    }
}
