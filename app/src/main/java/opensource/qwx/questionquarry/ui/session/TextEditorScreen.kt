package opensource.qwx.questionquarry.ui.session

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults
import opensource.qwx.questionquarry.ui.components.CanvasBlock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextEditorScreen(
    pairIndex: Int,
    blockIndex: Int,
    isQuestion: Boolean,
    viewModel: SessionViewModel,
    onBack: () -> Unit,
) {
    val pair = viewModel.draftPairs.getOrNull(pairIndex) ?: return
    val allBlocks = if (isQuestion) (pair.questionBlocks) else (pair.answerBlocks)
    val block = (allBlocks.getOrNull(blockIndex) as? CanvasBlock.Text) ?: return
    
    val richTextState = rememberRichTextState()
    
    // Dynamic states based on current selection/cursor
    val isBold = richTextState.currentSpanStyle.fontWeight == FontWeight.Bold
    val isItalic = richTextState.currentSpanStyle.fontStyle == FontStyle.Italic
    val isUnderlined = richTextState.currentSpanStyle.textDecoration?.contains(TextDecoration.Underline) == true
    
    var showFontSizeMenu by remember { mutableStateOf(false) }
    var showColorMenu by remember { mutableStateOf(false) }
    var showHighlightMenu by remember { mutableStateOf(false) }
    
    var initialContent by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(block.id) {
        richTextState.setMarkdown(block.content)
        initialContent = richTextState.toMarkdown()
    }

    var showExitDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val currentMarkdown = richTextState.toMarkdown()
    val hasChanges = remember(currentMarkdown, initialContent) {
        initialContent != null && currentMarkdown != initialContent
    }

    val saveCurrentText = {
        // Fix for unusual line breaks: 
        // 1. Trim the markdown to remove trailing/leading whitespace.
        // 2. Normalize multiple internal newlines.
        val normalized = richTextState.toMarkdown()
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
        viewModel.updateTextBlock(pairIndex, block.id, isQuestion, normalized)
    }

    val handleBack = {
        if (hasChanges) {
            showExitDialog = true
        } else {
            onBack()
        }
    }

    BackHandler(onBack = handleBack)

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Save Changes?") },
            text = { Text("You have unsaved changes. Would you like to save them before leaving?") },
            confirmButton = {
                Button(
                    onClick = {
                        saveCurrentText()
                        onBack()
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { onBack() }) {
                        Text("Discard")
                    }
                    TextButton(onClick = { showExitDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Block?") },
            text = { Text("Are you sure you want to delete this text block?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTextBlock(pairIndex, block.id, isQuestion)
                        onBack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "EDIT TEXT BLOCK",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "Text Block ${blockIndex + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                navigationIcon = {
                    FilledTonalButton(
                        onClick = handleBack,
                        modifier = Modifier.padding(start = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            saveCurrentText()
                            onBack()
                        },
                        modifier = Modifier.padding(end = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Finish")
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Rounded.Check, null, modifier = Modifier.size(18.dp))
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        ToolbarButton(
                            icon = Icons.Rounded.FormatBold,
                            onClick = { richTextState.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold)) },
                            contentDescription = "Bold",
                            selected = isBold
                        )
                    }
                    item {
                        ToolbarButton(
                            icon = Icons.Rounded.FormatItalic,
                            onClick = { richTextState.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic)) },
                            contentDescription = "Italic",
                            selected = isItalic
                        )
                    }
                    item {
                        ToolbarButton(
                            icon = Icons.Rounded.FormatUnderlined,
                            onClick = { richTextState.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline)) },
                            contentDescription = "Underline",
                            selected = isUnderlined
                        )
                    }
                    item {
                        VerticalDivider(modifier = Modifier.height(24.dp))
                    }
                    item {
                        Box {
                            ToolbarButton(
                                icon = Icons.Rounded.FormatSize,
                                onClick = { showFontSizeMenu = true },
                                contentDescription = "Font Size"
                            )
                            DropdownMenu(
                                expanded = showFontSizeMenu,
                                onDismissRequest = { showFontSizeMenu = false }
                            ) {
                                listOf(12, 14, 16, 18, 20, 24, 28, 32).forEach { size ->
                                    DropdownMenuItem(
                                        text = { Text("$size sp") },
                                        onClick = {
                                            richTextState.toggleSpanStyle(SpanStyle(fontSize = size.sp))
                                            showFontSizeMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Box {
                            ToolbarButton(
                                icon = Icons.Rounded.FormatColorText,
                                onClick = { showColorMenu = true },
                                contentDescription = "Text Color",
                                selected = richTextState.currentSpanStyle.color != Color.Unspecified
                            )
                            DropdownMenu(
                                expanded = showColorMenu,
                                onDismissRequest = { showColorMenu = false }
                            ) {
                                val colors = listOf(
                                    "Default" to Color.Unspecified,
                                    "Red" to Color.Red,
                                    "Blue" to Color(0xFF2196F3),
                                    "Green" to Color(0xFF4CAF50),
                                    "Orange" to Color(0xFFFF9800)
                                )
                                colors.forEach { (name, color) ->
                                    DropdownMenuItem(
                                        text = { 
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(16.dp).background(color, CircleShape))
                                                Spacer(Modifier.width(8.dp))
                                                Text(name)
                                            }
                                        },
                                        onClick = {
                                            richTextState.toggleSpanStyle(SpanStyle(color = color))
                                            showColorMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Box {
                            ToolbarButton(
                                icon = Icons.Rounded.FormatColorFill,
                                onClick = { showHighlightMenu = true },
                                contentDescription = "Highlight",
                                selected = richTextState.currentSpanStyle.background != Color.Transparent && richTextState.currentSpanStyle.background != Color.Unspecified
                            )
                            DropdownMenu(
                                expanded = showHighlightMenu,
                                onDismissRequest = { showHighlightMenu = false }
                            ) {
                                val highlights = listOf(
                                    "None" to Color.Transparent,
                                    "Yellow" to Color.Yellow,
                                    "Cyan" to Color.Cyan,
                                    "Green" to Color(0xFFCCFF90),
                                    "Pink" to Color(0xFFFF80AB)
                                )
                                highlights.forEach { (name, color) ->
                                    DropdownMenuItem(
                                        text = { 
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(16.dp).background(color, if (color == Color.Transparent) CircleShape else RoundedCornerShape(2.dp)).border(1.dp, MaterialTheme.colorScheme.outline, if (color == Color.Transparent) CircleShape else RoundedCornerShape(2.dp)))
                                                Spacer(Modifier.width(8.dp))
                                                Text(name)
                                            }
                                        },
                                        onClick = {
                                            if (color == Color.Transparent) {
                                                // Remove background by setting it to unspecified or transparent
                                                richTextState.toggleSpanStyle(SpanStyle(background = Color.Transparent))
                                            } else {
                                                richTextState.toggleSpanStyle(SpanStyle(background = color))
                                            }
                                            showHighlightMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    item {
                        VerticalDivider(modifier = Modifier.height(24.dp))
                    }
                    item {
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f), CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val focusRequester = remember { FocusRequester() }
            
            RichTextEditor(
                state = richTextState,
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(focusRequester),
                placeholder = { Text("Write your markdown content...") },
                colors = RichTextEditorDefaults.richTextEditorColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                textStyle = MaterialTheme.typography.bodyLarge
            )
            
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        }
    }
}

@Composable
fun ToolbarButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String,
    selected: Boolean = false
) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier.size(40.dp),
        shape = RoundedCornerShape(10.dp),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Icon(icon, contentDescription, modifier = Modifier.size(20.dp))
    }
}
