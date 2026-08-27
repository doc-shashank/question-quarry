package opensource.qwx.questionquarry.ui.session

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import opensource.qwx.questionquarry.data.local.entity.Session

sealed class BrowserLevel {
    data object Subjects : BrowserLevel()
    data class Chapters(val subject: String) : BrowserLevel()
    data class Topics(val subject: String, val chapter: String?) : BrowserLevel()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsBrowserScreen(
    viewModel: SessionViewModel,
    onNavigateToSessionDetail: (Long) -> Unit,
    onBack: () -> Unit
) {
    var currentLevel by remember { mutableStateOf<BrowserLevel>(BrowserLevel.Subjects) }
    
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val chapters by viewModel.chapterNames.collectAsStateWithLifecycle()
    
    // For Topics level, we need to filter sessions
    val sessions by remember(currentLevel) {
        if (currentLevel is BrowserLevel.Topics) {
            val level = currentLevel as BrowserLevel.Topics
            viewModel.getSessionsBySubjectAndChapter(level.subject, level.chapter)
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (val level = currentLevel) {
                            BrowserLevel.Subjects -> "Subjects"
                            is BrowserLevel.Chapters -> level.subject
                            is BrowserLevel.Topics -> level.chapter ?: "No Chapter"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        when (currentLevel) {
                            BrowserLevel.Subjects -> onBack()
                            is BrowserLevel.Chapters -> currentLevel = BrowserLevel.Subjects
                            is BrowserLevel.Topics -> currentLevel = BrowserLevel.Chapters((currentLevel as BrowserLevel.Topics).subject)
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when (val level = currentLevel) {
                BrowserLevel.Subjects -> {
                    SubjectsList(subjects) { currentLevel = BrowserLevel.Chapters(it) }
                }
                is BrowserLevel.Chapters -> {
                    // In a real app we'd filter chapters by subject. 
                    // For simplicity, we'll show all and filter the list or just show what's available.
                    ChaptersList(chapters) { currentLevel = BrowserLevel.Topics(level.subject, it) }
                }
                is BrowserLevel.Topics -> {
                    TopicsAndSessionsList(sessions, onNavigateToSessionDetail)
                }
            }
        }
    }
}

@Composable
private fun SubjectsList(subjects: List<String>, onSubjectClick: (String) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(subjects) { subject ->
            BrowserItem(title = subject, icon = Icons.Rounded.Book) { onSubjectClick(subject) }
        }
    }
}

@Composable
private fun ChaptersList(chapters: List<String>, onChapterClick: (String?) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            BrowserItem(title = "No Chapter", icon = Icons.Rounded.Numbers) { onChapterClick(null) }
        }
        items(chapters) { chapter ->
            BrowserItem(title = "Chapter $chapter", icon = Icons.Rounded.Numbers) { onChapterClick(chapter) }
        }
    }
}

@Composable
private fun TopicsAndSessionsList(
    sessions: List<Session>,
    onSessionClick: (Long) -> Unit
) {
    val groupedByTopic = sessions.groupBy { if (it.isTopicEnabled) it.topic else null }
    
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        groupedByTopic.forEach { (topic, topicSessions) ->
            item {
                Text(
                    text = topic ?: "No Topic",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            items(topicSessions) { session ->
                SessionItem(session) { onSessionClick(session.id) }
            }
        }
    }
}

@Composable
private fun BrowserItem(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Text(text = title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun SessionItem(session: Session, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = session.title, style = MaterialTheme.typography.titleSmall)
        }
    }
}
