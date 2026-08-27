package opensource.qwx.questionquarry.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import opensource.qwx.questionquarry.ui.theme.QuestionQuarryTheme
import opensource.qwx.questionquarry.util.StorageManager
import java.io.File
import java.util.*
import com.mikepenz.markdown.m3.Markdown
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CanvasEditor(
    blocks: List<CanvasBlock>,
    onBlocksChange: (List<CanvasBlock>) -> Unit,
    onEditText: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
    isReadOnly: Boolean = false,
    isScrollable: Boolean = true
) {
    val context = LocalContext.current
    val storageManager = remember { StorageManager(context) }
    val listState = rememberLazyListState()

    var focusedIndex by remember { mutableStateOf<Int?>(null) }
    val isImeVisible = WindowInsets.isImeVisible
    
    LaunchedEffect(isImeVisible) {
        if (isImeVisible && isScrollable) {
            focusedIndex?.let { index ->
                listState.animateScrollToItem(index)
            }
        }
    }

    var previousSize by remember { mutableIntStateOf(blocks.size) }
    LaunchedEffect(blocks.size) {
        if (blocks.size > previousSize && isScrollable) {
            listState.animateScrollToItem(blocks.size)
        }
        previousSize = blocks.size
    }

    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    Box(modifier = modifier.then(if (isScrollable) Modifier.fillMaxSize().imePadding() else Modifier.fillMaxWidth())) {
        if (blocks.isEmpty() && !isReadOnly) {
            EmptyCanvasPrompt(
                onAddText = { onBlocksChange(listOf(CanvasBlock.Text())) },
                onAddImage = { onBlocksChange(listOf(CanvasBlock.Image())) }
            )
        } else if (!isScrollable) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                blocks.forEachIndexed { index, block ->
                    val textCount = blocks.take(index).count { it is CanvasBlock.Text } + 1
                    val imageCount = blocks.take(index).count { it is CanvasBlock.Image } + 1
                    val label = when (block) {
                        is CanvasBlock.Text -> "Text $textCount"
                        is CanvasBlock.Image -> "Image $imageCount"
                    }

                    BlockItem(
                        block = block,
                        label = label,
                        onDeleteBlock = {
                            onBlocksChange(blocks.filter { it.id != block.id })
                        },
                        onEditText = { onEditText(index) },
                        onUpdate = { updatedBlock ->
                            onBlocksChange(blocks.map { if (it.id == block.id) updatedBlock else it })
                        },
                        storageManager = storageManager,
                        isReadOnly = isReadOnly
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (!isReadOnly) {
                            Modifier.pointerInput(blocks) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { offset ->
                                        listState.layoutInfo.visibleItemsInfo
                                            .firstOrNull { item ->
                                                offset.y.toInt() in item.offset..(item.offset + item.size) &&
                                                        item.index < blocks.size
                                            }
                                            ?.also {
                                                draggedItemIndex = it.index
                                            }
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffset += dragAmount.y

                                        draggedItemIndex?.let { currentIndex ->
                                            val currentItemInfo = listState.layoutInfo.visibleItemsInfo.find { it.index == currentIndex }
                                            if (currentItemInfo != null) {
                                                val currentItemCenter = currentItemInfo.offset + currentItemInfo.size / 2 + dragOffset
                                                
                                                val targetItem = listState.layoutInfo.visibleItemsInfo
                                                    .firstOrNull { item ->
                                                        item.index != currentIndex &&
                                                        item.index < blocks.size &&
                                                        currentItemCenter in item.offset.toFloat()..(item.offset + item.size).toFloat()
                                                    }

                                                targetItem?.let { target ->
                                                    val newBlocks = blocks.toMutableList()
                                                    val item = newBlocks.removeAt(currentIndex)
                                                    newBlocks.add(target.index, item)
                                                    onBlocksChange(newBlocks)
                                                    
                                                    dragOffset += currentItemInfo.offset - target.offset
                                                    draggedItemIndex = target.index
                                                }
                                            }
                                        }
                                    },
                                    onDragEnd = {
                                        draggedItemIndex = null
                                        dragOffset = 0f
                                    },
                                    onDragCancel = {
                                        draggedItemIndex = null
                                        dragOffset = 0f
                                    }
                                )
                            }
                        } else Modifier
                    ),
                contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = blocks,
                    key = { it.id }
                ) { block ->
                    val index = blocks.indexOfFirst { it.id == block.id }
                    val isDragging = index == draggedItemIndex
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                            .zIndex(if (isDragging) 1f else 0f)
                            .graphicsLayer {
                                translationY = if (isDragging) dragOffset else 0f
                                scaleX = if (isDragging) 1.02f else 1f
                                scaleY = if (isDragging) 1.02f else 1f
                            }
                            .then(
                                if (isDragging) {
                                    Modifier.border(
                                        width = 2.dp,
                                        color = Color.Green,
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                } else Modifier
                            )
                    ) {
                        val textCount = blocks.take(index).count { it is CanvasBlock.Text } + 1
                        val imageCount = blocks.take(index).count { it is CanvasBlock.Image } + 1
                        val label = when (block) {
                            is CanvasBlock.Text -> "Text $textCount"
                            is CanvasBlock.Image -> "Image $imageCount"
                        }

                        BlockItem(
                            block = block,
                            label = label,
                            onDeleteBlock = {
                                onBlocksChange(blocks.filter { it.id != block.id })
                            },
                            onEditText = { onEditText(index) },
                            onUpdate = { updatedBlock ->
                                onBlocksChange(blocks.map { if (it.id == block.id) updatedBlock else it })
                            },
                            storageManager = storageManager,
                            isReadOnly = isReadOnly
                        )
                    }
                }

                if (!isReadOnly && blocks.isNotEmpty()) {
                    item {
                        AddBlockRow(
                            onAddText = { onBlocksChange(blocks + CanvasBlock.Text()) },
                            onAddImage = { onBlocksChange(blocks + CanvasBlock.Image()) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyCanvasPrompt(
    onAddText: () -> Unit,
    onAddImage: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Rounded.DashboardCustomize,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Your canvas is empty",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Add your first element to start building.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onAddText,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Rounded.TextFields, null)
                Spacer(Modifier.width(8.dp))
                Text("Add Text")
            }
            FilledTonalButton(
                onClick = onAddImage,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Rounded.Image, null)
                Spacer(Modifier.width(8.dp))
                Text("Add Image")
            }
        }
    }
}

@Composable
fun AddBlockRow(
    onAddText: () -> Unit,
    onAddImage: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isExpanded) {
            FilledTonalButton(
                onClick = { isExpanded = true },
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Icon(Icons.Rounded.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Add Element", style = MaterialTheme.typography.labelLarge)
            }
        } else {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TextButton(
                        onClick = {
                            onAddText()
                            isExpanded = false
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                    ) {
                        Icon(Icons.Rounded.TextFields, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Text")
                    }

                    TextButton(
                        onClick = {
                            onAddImage()
                            isExpanded = false
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                    ) {
                        Icon(Icons.Rounded.Image, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Image")
                    }

                    VerticalDivider(
                        modifier = Modifier
                            .height(24.dp)
                            .width(1.dp)
                            .padding(horizontal = 4.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f)
                    )

                    IconButton(onClick = { isExpanded = false }) {
                        Icon(
                            Icons.Rounded.Close,
                            null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CanvasEditorPreview() {
    QuestionQuarryTheme {
        CanvasEditor(
            blocks = listOf(
                CanvasBlock.Text("Sample text content"),
                CanvasBlock.Image(null)
            ),
            onBlocksChange = {}
        )
    }
}
