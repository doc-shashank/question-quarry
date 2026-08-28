package opensource.qwx.questionquarry.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import opensource.qwx.questionquarry.data.local.entity.Session
import opensource.qwx.questionquarry.ui.session.SessionViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: SessionViewModel,
    onNavigateToNewSession: (String, Long?) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSessionDetail: (Long) -> Unit,
    onNavigateToSubjects: () -> Unit,
    onNavigateToEditPresets: () -> Unit,
    modifier: Modifier = Modifier,
    filterDate: Long? = null,
) {
    val dueSessions by viewModel.getDueSessions().collectAsStateWithLifecycle(initialValue = emptyList())
    val recentSessions by viewModel.getRecentSessions().collectAsStateWithLifecycle(initialValue = emptyList())
    val untaggedSessions by viewModel.getUntaggedSessions().collectAsStateWithLifecycle(initialValue = emptyList())

    var selectedSessionIds by remember { mutableStateOf(setOf<Long>()) }
    var isSelectionMode by remember { mutableStateOf(false) }

    var showLibrarySheet by remember { mutableStateOf(false) }
    var sessionToDelete by remember { mutableStateOf<Long?>(null) }
    var expandedSessionId by remember { mutableStateOf<Long?>(null) }

    val haptic = LocalHapticFeedback.current
    val listState = rememberLazyListState()

    val filteredSessions by remember(filterDate) {
        if (filterDate != null) {
            val calendar = Calendar.getInstance().apply { timeInMillis = filterDate }
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val start = calendar.timeInMillis
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val end = calendar.timeInMillis
            viewModel.getSessionsByDateRange(start, end)
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    var showNamingDialog by remember { mutableStateOf(false) }
    var newSessionName by remember { mutableStateOf("") }

    if (sessionToDelete != null) {
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = { Text("Delete Session?") },
            text = { Text("Do you want to delete the content along with the session history, or just remove the session entry and keep the questions in your library?") },
            confirmButton = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            viewModel.deleteSessionWithOptions(sessionToDelete!!, deleteAll = true)
                            sessionToDelete = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete All")
                    }
                    FilledTonalButton(
                        onClick = {
                            viewModel.deleteSessionWithOptions(sessionToDelete!!, deleteAll = false)
                            sessionToDelete = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Dissociate and Delete")
                    }
                    
                    TextButton(
                        onClick = { sessionToDelete = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel")
                    }
                }
            },
            dismissButton = null
        )
    }

    if (showLibrarySheet) {
        ModalBottomSheet(
            onDismissRequest = { showLibrarySheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "CHOOSE AN ACTION",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = 8.dp),
                    letterSpacing = 1.sp
                )
                
                ListItem(
                    headlineContent = { Text("View Library", fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text("Browse organized content") },
                    leadingContent = { 
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.Search, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    },
                    modifier = Modifier.clickable { 
                        showLibrarySheet = false
                        onNavigateToSubjects() 
                    }
                )
                
                ListItem(
                    headlineContent = { Text("Edit Presets", fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text("Manage subjects and chapters") },
                    leadingContent = { 
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.Edit, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }
                    },
                    modifier = Modifier.clickable { 
                        showLibrarySheet = false
                        onNavigateToEditPresets() 
                    }
                )
            }
        }
    }

    if (showNamingDialog) {
        AlertDialog(
            onDismissRequest = { 
                showNamingDialog = false
                newSessionName = ""
            },
            title = {
                Text(
                    "New Session",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Give your session a name to get started.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = newSessionName,
                        onValueChange = { if (it.length <= 40) newSessionName = it },
                        label = { Text("Session Name") },
                        placeholder = { Text("e.g. Physics Quiz") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        supportingText = {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                                Text(
                                    "${newSessionName.length}/40",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        singleLine = true,
                        isError = false
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = newSessionName
                        showNamingDialog = false
                        newSessionName = ""
                        onNavigateToNewSession(name, null)
                    },
                    enabled = (newSessionName.isNotBlank()) && (newSessionName.length <= 40)
                ) {
                    Text("Start")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showNamingDialog = false
                        newSessionName = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    val title = if (filterDate != null) {
        val config = androidx.compose.ui.platform.LocalConfiguration.current
        remember(filterDate, config) {
            SimpleDateFormat("MMMM d", config.locales[0]).format(Date(filterDate))
                .let { "Sessions: $it" }
        }
    } else "QuestionQuarry"

    val displayDue = if (filterDate == null) dueSessions else emptyList()
    val displayToday = if (filterDate == null) recentSessions else filteredSessions

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                navigationIcon = {
                    if (isSelectionMode) {
                        IconButton(onClick = { 
                            isSelectionMode = false
                            selectedSessionIds = emptySet()
                        }) {
                            Icon(Icons.Rounded.Close, "Cancel Selection")
                        }
                    } else {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Rounded.Settings, contentDescription = "Settings")
                        }
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        IconButton(onClick = { 
                            viewModel.deleteSessions(selectedSessionIds.toList())
                            isSelectionMode = false
                            selectedSessionIds = emptySet()
                        }) {
                            Icon(Icons.Rounded.Delete, "Delete Selected", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showNamingDialog = true },
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("New Session") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            state = listState,
            userScrollEnabled = true, // Simplified: standard scrolling is usually fine in Compose
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionHeader(title = "Library")
            }
            
            item {
                LibraryCard(
                    icon = Icons.Rounded.Book,
                    onClick = onNavigateToSubjects,
                    onLongClick = { showLibrarySheet = true }
                )
            }

            if (displayDue.isNotEmpty()) {
                item {
                    SectionHeader(title = "Due Questions")
                }

                items(displayDue) { session ->
                    QuestionItem(
                        session = session,
                        onClick = { onNavigateToSessionDetail(session.id) }
                    )
                }
            }

            if (displayToday.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionHeader(title = "Recent Sessions")
                }

                items(displayToday, key = { it.id }) { session ->
                    val isSelected = selectedSessionIds.contains(session.id)
                    val isExpanded = expandedSessionId == session.id && !isSelectionMode

                    SessionItem(
                        session = session,
                        selected = isSelected,
                        isExpanded = isExpanded,
                        onLongClick = {
                            if (!isSelectionMode) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                expandedSessionId = if (isExpanded) null else session.id
                            } else {
                                selectedSessionIds = selectedSessionIds + session.id
                            }
                        },
                        onClick = { 
                            if (isSelectionMode) {
                                if (isSelected) {
                                    selectedSessionIds -= session.id
                                    if (selectedSessionIds.isEmpty()) isSelectionMode = false
                                } else {
                                    selectedSessionIds += session.id
                                }
                            } else {
                                if (isExpanded) expandedSessionId = null
                                else onNavigateToSessionDetail(session.id) 
                            }
                        },
                        isSelectionMode = isSelectionMode,
                        onDelete = {
                            sessionToDelete = session.id
                            expandedSessionId = null
                        }
                    )
                }
            }

            if (untaggedSessions.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionHeader(title = "Untagged Questions")
                }

                items(untaggedSessions, key = { it.id }) { session ->
                    val isSelected = selectedSessionIds.contains(session.id)
                    val isExpanded = expandedSessionId == session.id && !isSelectionMode

                    SessionItem(
                        session = session,
                        selected = isSelected,
                        isExpanded = isExpanded,
                        onLongClick = {
                            if (!isSelectionMode) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                expandedSessionId = if (isExpanded) null else session.id
                            } else {
                                selectedSessionIds = selectedSessionIds + session.id
                            }
                        },
                        onClick = { 
                            if (isSelectionMode) {
                                if (isSelected) {
                                    selectedSessionIds -= session.id
                                    if (selectedSessionIds.isEmpty()) isSelectionMode = false
                                } else {
                                    selectedSessionIds += session.id
                                }
                            } else {
                                if (isExpanded) expandedSessionId = null
                                else onNavigateToSessionDetail(session.id) 
                            }
                        },
                        isSelectionMode = isSelectionMode,
                        onDelete = {
                            sessionToDelete = session.id
                            expandedSessionId = null
                        }
                    )
                }
            }
            
            if (displayDue.isEmpty() && displayToday.isEmpty() && untaggedSessions.isEmpty()) {
                // No text here as requested
            }

            item {
                Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LibraryCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val title = "Your Subjects"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Browse by subject, chapter & topic",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun QuestionItem(session: Session, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.QuestionMark,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = session.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    val time = remember(session.id, session.completionTime, session.date) {
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(if (session.completionTime > 0) session.completionTime else session.date))
                    }
                    Text(
                        text = time,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "Due for review",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SessionItem(
    session: Session, 
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onDelete: () -> Unit = {},
    selected: Boolean = false,
    isSelectionMode: Boolean = false,
    isExpanded: Boolean = false
) {
    // Collapse if selection mode starts - handled by caller now
    
    val height by animateDpAsState(if (isExpanded) 120.dp else 88.dp, label = "height")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = session.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        val time = remember(session.id, session.completionTime, session.date) {
                            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(if (session.completionTime > 0) session.completionTime else session.date))
                        }
                        Text(
                            text = time,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "Completed today",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (selected) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            
            if (isExpanded) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { /* TODO: Rename logic if needed */ }) {
                        Icon(Icons.Rounded.Edit, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Rename")
                    }
                    VerticalDivider(modifier = Modifier.height(24.dp))
                    TextButton(
                        onClick = { onDelete() },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Rounded.Delete, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Delete")
                    }
                }
            }
        }
    }
}
