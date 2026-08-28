package opensource.qwx.questionquarry.ui.session

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import opensource.qwx.questionquarry.data.local.entity.Preset
import opensource.qwx.questionquarry.data.local.entity.PresetType

private sealed class PresetsBrowserLevel {
    data object Subjects : PresetsBrowserLevel()
    data class Chapters(val subjectId: Long, val subjectName: String) : PresetsBrowserLevel()
    data class Topics(val subjectId: Long, val subjectName: String, val chapterId: Long, val chapterName: String) : PresetsBrowserLevel()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPresetsScreen(
    viewModel: SessionViewModel,
    onBack: () -> Unit,
) {
    var currentLevel by remember { mutableStateOf<PresetsBrowserLevel>(PresetsBrowserLevel.Subjects) }

    val handleBack = {
        when (currentLevel) {
            PresetsBrowserLevel.Subjects -> onBack()
            is PresetsBrowserLevel.Chapters -> currentLevel = PresetsBrowserLevel.Subjects
            is PresetsBrowserLevel.Topics -> currentLevel = PresetsBrowserLevel.Chapters((currentLevel as PresetsBrowserLevel.Topics).subjectId, (currentLevel as PresetsBrowserLevel.Topics).subjectName)
        }
    }

    BackHandler(onBack = handleBack)

    var showAddDialog by remember { mutableStateOf<PresetType?>(null) }
    var showRenameDialog by remember { mutableStateOf<Preset?>(null) }
    var newPresetName by remember { mutableStateOf("") }
    
    val subjects by viewModel.presetSubjects.collectAsStateWithLifecycle()
    
    val chapters by remember(currentLevel) {
        when (val level = currentLevel) {
            is PresetsBrowserLevel.Chapters -> viewModel.getChaptersForSubject(level.subjectId)
            is PresetsBrowserLevel.Topics -> viewModel.getChaptersForSubject(level.subjectId)
            else -> kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    val topics by remember(currentLevel) {
        if (currentLevel is PresetsBrowserLevel.Topics) {
            viewModel.getTopicsForChapter((currentLevel as PresetsBrowserLevel.Topics).chapterId)
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        when (val level = currentLevel) {
                            PresetsBrowserLevel.Subjects -> "Edit Subjects"
                            is PresetsBrowserLevel.Chapters -> "Chapters: ${level.subjectName}"
                            is PresetsBrowserLevel.Topics -> "Topics: ${level.chapterName}"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                navigationIcon = {
                    IconButton(onClick = handleBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    showAddDialog = when (currentLevel) {
                        PresetsBrowserLevel.Subjects -> PresetType.SUBJECT
                        is PresetsBrowserLevel.Chapters -> PresetType.CHAPTER
                        is PresetsBrowserLevel.Topics -> PresetType.TOPIC
                    }
                },
                icon = { Icon(Icons.Rounded.Add, null) },
                text = {
                    Text(
                        when (currentLevel) {
                            PresetsBrowserLevel.Subjects -> "New Subject"
                            is PresetsBrowserLevel.Chapters -> "New Chapter"
                            is PresetsBrowserLevel.Topics -> "New Topic"
                        }
                    )
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val level = currentLevel) {
                PresetsBrowserLevel.Subjects -> {
                    PresetsList(
                        items = subjects,
                        onItemClick = { currentLevel = PresetsBrowserLevel.Chapters(it.id, it.name) },
                        onRename = { showRenameDialog = it; newPresetName = it.name },
                        onDelete = { viewModel.deletePreset(it) },
                        levelName = "Subject"
                    )
                }
                is PresetsBrowserLevel.Chapters -> {
                    PresetsList(
                        items = chapters,
                        onItemClick = { currentLevel = PresetsBrowserLevel.Topics(level.subjectId, level.subjectName, it.id, it.name) },
                        onRename = { showRenameDialog = it; newPresetName = it.name },
                        onDelete = { viewModel.deletePreset(it) },
                        levelName = "Chapter"
                    )
                }
                is PresetsBrowserLevel.Topics -> {
                    PresetsList(
                        items = topics,
                        onItemClick = { },
                        onRename = { showRenameDialog = it; newPresetName = it.name },
                        onDelete = { viewModel.deletePreset(it) },
                        levelName = "Topic"
                    )
                }
            }
        }
    }
    
    // Add Dialog
    if (showAddDialog != null) {
        AlertDialog(
            onDismissRequest = { showAddDialog = null; newPresetName = "" },
            title = { Text("New ${showAddDialog?.name?.lowercase()?.replaceFirstChar { it.uppercase() }}") },
            text = {
                OutlinedTextField(
                    value = newPresetName,
                    onValueChange = { newPresetName = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val (type, parentId, subject, chapter) = when (val level = currentLevel) {
                            is PresetsBrowserLevel.Subjects -> {
                                Quadruple(PresetType.SUBJECT, null, null, null)
                            }
                            is PresetsBrowserLevel.Chapters -> {
                                Quadruple(PresetType.CHAPTER, level.subjectId, level.subjectName, null)
                            }
                            is PresetsBrowserLevel.Topics -> {
                                Quadruple(PresetType.TOPIC, level.chapterId, level.subjectName, level.chapterName)
                            }
                        }
                        viewModel.createPreset(newPresetName, type, parentId, subject, chapter)
                        showAddDialog = null
                        newPresetName = ""
                    },
                    enabled = newPresetName.isNotBlank()
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = null; newPresetName = "" }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Rename Dialog
    if (showRenameDialog != null) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = null; newPresetName = "" },
            title = { Text("Rename ${showRenameDialog?.type?.name?.lowercase()?.replaceFirstChar { it.uppercase() }}") },
            text = {
                OutlinedTextField(
                    value = newPresetName,
                    onValueChange = { newPresetName = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.renamePreset(showRenameDialog!!, newPresetName)
                        showRenameDialog = null
                        newPresetName = ""
                    },
                    enabled = newPresetName.isNotBlank()
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = null; newPresetName = "" }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PresetsList(
    items: List<Preset>,
    onItemClick: (Preset) -> Unit,
    onRename: (Preset) -> Unit,
    onDelete: (Preset) -> Unit,
    levelName: String
) {
    var expandedItemId by remember { mutableStateOf<Long?>(null) }
    val haptic = LocalHapticFeedback.current

    if (items.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "No ${levelName.lowercase()}s created yet",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items, key = { it.id }) { item ->
                val isExpanded = expandedItemId == item.id
                val height by animateDpAsState(if (isExpanded) 120.dp else 64.dp, label = "height")
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height)
                        .combinedClickable(
                            onClick = { 
                                if (isExpanded) expandedItemId = null 
                                else onItemClick(item) 
                            },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                expandedItemId = if (isExpanded) null else item.id
                            }
                        ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            if (!isExpanded) {
                                Icon(Icons.Rounded.MoreVert, null, tint = MaterialTheme.colorScheme.outline)
                            }
                        }
                        
                        if (isExpanded) {
                            Spacer(Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { onRename(item); expandedItemId = null }) {
                                    Icon(Icons.Rounded.Edit, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Rename")
                                }
                                VerticalDivider(modifier = Modifier.height(24.dp))
                                TextButton(
                                    onClick = { onDelete(item); expandedItemId = null },
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
        }
    }
}
