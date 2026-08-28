package opensource.qwx.questionquarry.ui.components

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.mikepenz.markdown.m3.Markdown
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import opensource.qwx.questionquarry.util.StorageManager
import java.io.File
import java.util.*

private const val TAG = "CanvasBlock"

sealed class CanvasBlock {
    abstract val id: String
    
    data class Text(
        val content: String = "",
        override val id: String = UUID.randomUUID().toString(),
    ) : CanvasBlock()
    
    data class Image(
        val imagePath: String? = null,
        override val id: String = UUID.randomUUID().toString(),
    ) : CanvasBlock()
}

@Composable
fun BlockItem(
    block: CanvasBlock,
    label: String,
    onDeleteBlock: () -> Unit,
    onEditText: () -> Unit,
    onUpdate: (CanvasBlock) -> Unit,
    storageManager: StorageManager,
    isReadOnly: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isReadOnly) Color.Transparent else MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = if (isReadOnly) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(if (isReadOnly) 0.dp else 12.dp)) {
            if (!isReadOnly) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    IconButton(onClick = onDeleteBlock) {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = "Remove Block",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            when (block) {
                is CanvasBlock.Text -> {
                    val processedContent = remember(block.content) {
                        block.content.replace("\n", "  \n")
                    }
                    if (isReadOnly) {
                        Markdown(
                            content = processedContent,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        if (block.content.isBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Button(
                                    onClick = onEditText,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Rounded.Edit, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Edit Text")
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                            ) {
                                Markdown(
                                    content = processedContent,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                // Fade out overlay
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .align(Alignment.BottomCenter)
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    MaterialTheme.colorScheme.surfaceContainerLow
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    Button(
                                        onClick = onEditText,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    ) {
                                        Icon(Icons.Rounded.Edit, null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Edit Text")
                                    }
                                }
                            }
                        }
                    }
                }
                is CanvasBlock.Image -> {
                    ImageBlockContent(
                        imagePath = block.imagePath,
                        onImageSelected = { path: String? ->
                            onUpdate(block.copy(imagePath = path))
                        },
                        storageManager = storageManager,
                        isReadOnly = isReadOnly
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ImageBlockContent(
    imagePath: String?,
    onImageSelected: (String?) -> Unit,
    storageManager: StorageManager,
    isReadOnly: Boolean = false
) {
    val scope = rememberCoroutineScope()
    
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    var isChangingImage by rememberSaveable { mutableStateOf(value = false) }
    var shouldLaunchCamera by rememberSaveable { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            Log.d(TAG, "Gallery selected URI: $it")
            scope.launch(Dispatchers.IO) {
                val path = storageManager.saveImage(it)
                withContext(Dispatchers.Main) {
                    onImageSelected(path)
                    isChangingImage = false
                }
            }
        }
    }

    var tempUriString by rememberSaveable { mutableStateOf<String?>(null) }
    
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        Log.d(TAG, "Camera TakePicture result: $success, URI: $tempUriString")
        if (success) {
            tempUriString?.let { uriString ->
                val uri = uriString.toUri()
                scope.launch(Dispatchers.IO) {
                    try {
                        val path = storageManager.saveImage(uri)
                        withContext(Dispatchers.Main) {
                            Log.d(TAG, "Image saved to path: $path")
                            onImageSelected(path)
                            isChangingImage = false
                            tempUriString = null
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing camera image", e)
                    }
                }
            } ?: Log.e(TAG, "tempUriString is null after success")
        } else {
            Log.w(TAG, "Camera failed or was cancelled")
        }
    }
    
    fun launchCamera() {
        try {
            val uri = storageManager.createTempImageUri()
            if (uri != null) {
                tempUriString = uri.toString()
                Log.d(TAG, "Launching camera with URI: $uri")
                cameraLauncher.launch(uri)
            } else {
                Log.e(TAG, "Failed to create temp image URI")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception launching camera", e)
        }
    }

    LaunchedEffect(cameraPermissionState.status.isGranted) {
        if (cameraPermissionState.status.isGranted && shouldLaunchCamera) {
            launchCamera()
            shouldLaunchCamera = false
        }
    }

    if ((imagePath != null) && (!isChangingImage)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            val file = storageManager.getFile(imagePath)
            AsyncImage(
                model = file,
                contentDescription = "Selected Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(if (isReadOnly) 12.dp else 16.dp)),
                contentScale = ContentScale.FillWidth
            )
            
            if (!isReadOnly) {
                Surface(
                    onClick = { isChangingImage = true },
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Change",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    } else if (!isReadOnly) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 140.dp)
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isChangingImage) {
                Text(
                    "Replace current image",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            OutlinedButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(0.8f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Rounded.PhotoLibrary, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add from Gallery")
            }
            OutlinedButton(
                onClick = {
                    if (cameraPermissionState.status.isGranted) {
                        launchCamera()
                    } else {
                        shouldLaunchCamera = true
                        cameraPermissionState.launchPermissionRequest()
                    }
                },
                modifier = Modifier.fillMaxWidth(0.8f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Rounded.CameraAlt, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Capture from Camera")
            }
            
            if (isChangingImage) {
                TextButton(onClick = { isChangingImage = false }) {
                    Text("Cancel")
                }
            }
        }
    }
}
