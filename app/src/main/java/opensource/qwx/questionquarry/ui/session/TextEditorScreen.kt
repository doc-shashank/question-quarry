package opensource.qwx.questionquarry.ui.session

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.richeditor.model.RichTextState
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
    onBack: () -> Unit
) {
    val pair = viewModel.draftPairs.getOrNull(pairIndex) ?: return
    val allBlocks = if (isQuestion) pair.questionBlocks else pair.answerBlocks
    val block = allBlocks.getOrNull(blockIndex) as? CanvasBlock.Text ?: return
    
    val richTextState = rememberRichTextState()
    val clipboardManager = LocalClipboardManager.current
    val density = LocalDensity.current

    val customTextToolbar = remember {
        CustomTextToolbar(
            onCopyRequested = {
                val selection = richTextState.selection
                if (!selection.collapsed) {
                    val selectedText = richTextState.annotatedString.subSequence(selection.start, selection.end)
                    clipboardManager.setText(selectedText)
                }
            },
            onCutRequested = {
                val selection = richTextState.selection
                if (!selection.collapsed) {
                    val selectedText = richTextState.annotatedString.subSequence(selection.start, selection.end)
                    clipboardManager.setText(selectedText)
                    richTextState.replaceSelectedText("") 
                }
            },
            onPasteRequested = {
                val text = clipboardManager.getText()?.text
                if (text != null) {
                    richTextState.replaceSelectedText(text)
                }
            },
            onBoldRequested = { richTextState.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold)) },
            onItalicRequested = { richTextState.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic)) },
            onUnderlineRequested = { richTextState.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline)) }
        )
    }
    
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
        viewModel.updateTextBlock(pairIndex, block.id, isQuestion, richTextState.toMarkdown())
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
                    TextButton(
                        onClick = {
                            onBack()
                        }
                    ) {
                        Text("Discard")
                    }
                    TextButton(
                        onClick = {
                            showExitDialog = false
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    if (showDeleteDialog) {
        // ... (existing code)
    }

    CompositionLocalProvider(LocalTextToolbar provides customTextToolbar) {
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
                                text = if (isQuestion) "Question Section" else "Answer Section",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    },
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
                            Icon(
                                Icons.Rounded.Check,
                                null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Toolbar
                    Surface(
                        tonalElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            item {
                                ToolbarButton(
                                    icon = Icons.Rounded.FormatBold,
                                    onClick = { richTextState.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold)) },
                                    contentDescription = "Bold"
                                )
                            }
                            item {
                                ToolbarButton(
                                    icon = Icons.Rounded.FormatItalic,
                                    onClick = { richTextState.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic)) },
                                    contentDescription = "Italic"
                                )
                            }
                            item {
                                ToolbarButton(
                                    icon = Icons.Rounded.FormatUnderlined,
                                    onClick = { richTextState.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline)) },
                                    contentDescription = "Underline"
                                )
                            }
                            item {
                                ToolbarButton(
                                    icon = Icons.Rounded.FormatSize,
                                    onClick = { richTextState.toggleSpanStyle(SpanStyle(fontSize = 20.sp)) },
                                    contentDescription = "Font Size"
                                )
                            }
                            item {
                                ToolbarButton(
                                    icon = Icons.Rounded.FontDownload,
                                    onClick = { richTextState.toggleSpanStyle(SpanStyle(fontFamily = FontFamily.SansSerif)) },
                                    contentDescription = "Font Type"
                                )
                            }
                            item {
                                ToolbarButton(
                                    icon = Icons.Rounded.FormatColorText,
                                    onClick = { richTextState.toggleSpanStyle(SpanStyle(color = Color.Red)) },
                                    contentDescription = "Foreground Color"
                                )
                            }
                            item {
                                ToolbarButton(
                                    icon = Icons.Rounded.FormatColorFill,
                                    onClick = { richTextState.toggleSpanStyle(SpanStyle(background = Color.Yellow)) },
                                    contentDescription = "Highlight Color"
                                )
                            }
                            item {
                                VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 4.dp))
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
                                        contentDescription = "Delete Block",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
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

                // Custom Floating Toolbar
                if (customTextToolbar.status == TextToolbarStatus.Shown) {
                    val rect = customTextToolbar.lastRect
                    val popupOffset = with(density) {
                        IntOffset(
                            x = rect.center.x.toInt(),
                            y = (rect.bottom + 8.dp.toPx()).toInt()
                        )
                    }

                    Popup(
                        alignment = Alignment.TopCenter,
                        offset = popupOffset,
                        onDismissRequest = { customTextToolbar.hide() },
                        properties = PopupProperties(focusable = true)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            tonalElevation = 8.dp,
                            shadowElevation = 8.dp,
                            modifier = Modifier.widthIn(max = 300.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ToolbarActionItem(Icons.Rounded.ContentCut, "Cut") {
                                    customTextToolbar.onCutRequested()
                                    customTextToolbar.hide()
                                }
                                ToolbarActionItem(Icons.Rounded.ContentCopy, "Copy") {
                                    customTextToolbar.onCopyRequested()
                                    customTextToolbar.hide()
                                }
                                ToolbarActionItem(Icons.Rounded.ContentPaste, "Paste") {
                                    customTextToolbar.onPasteRequested()
                                    customTextToolbar.hide()
                                }
                                VerticalDivider(modifier = Modifier.height(24.dp))
                                ToolbarActionItem(Icons.Rounded.FormatBold, "Bold") {
                                    customTextToolbar.onBoldRequested()
                                }
                                ToolbarActionItem(Icons.Rounded.FormatItalic, "Italic") {
                                    customTextToolbar.onItalicRequested()
                                }
                                ToolbarActionItem(Icons.Rounded.FormatUnderlined, "Underline") {
                                    customTextToolbar.onUnderlineRequested()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToolbarActionItem(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
        Icon(icon, contentDescription, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun ToolbarButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String
) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier.size(40.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Icon(icon, contentDescription, modifier = Modifier.size(20.dp))
    }
}

