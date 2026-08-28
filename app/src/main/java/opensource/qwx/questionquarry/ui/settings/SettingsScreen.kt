package opensource.qwx.questionquarry.ui.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import opensource.qwx.questionquarry.BuildConfig
import opensource.qwx.questionquarry.util.DatabaseUtils
import opensource.qwx.questionquarry.util.UpdateManager
import opensource.qwx.questionquarry.util.UpdateInfo
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ThemeViewModel,
    updateManager: UpdateManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
    val colorSchemeId by viewModel.colorSchemeId.collectAsStateWithLifecycle(initialValue = ColorSchemeId.DEFAULT)

    var showExportDialog by remember { mutableStateOf(value = false) }
    var showImportDialog by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    var isCheckingForUpdate by remember { mutableStateOf(false) }
    var updateInfoState by remember { mutableStateOf<UpdateInfo?>(null) }
    var showNoUpdateDialog by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let {
            val success = DatabaseUtils.exportDatabase(context, it)
            if (success) {
                Toast.makeText(context, "Database exported successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val success = DatabaseUtils.importDatabase(context, it)
            if (success) {
                Toast.makeText(context, "Database imported. Please restart the app.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Import failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Settings", 
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Appearance Section
            SettingsSection(title = "Appearance", icon = Icons.Rounded.Palette) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Theme Mode",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    ThemeSelector(
                        selectedMode = themeMode
                    ) { viewModel.setThemeMode(it) }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Color Scheme",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    ColorSchemeSelector(
                        selectedScheme = colorSchemeId,
                        onSchemeSelected = { viewModel.setColorScheme(it) }
                    )
                }
            }

            // Data Management Section
            SettingsSection(title = "Data Management", icon = Icons.Rounded.Backup) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Icon(Icons.Rounded.Backup, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Export Database")
                    }

                    OutlinedButton(
                        onClick = { showImportDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Icon(Icons.Rounded.Restore, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Import Database")
                    }
                }
            }

            // About Section
            SettingsSection(title = "About", icon = Icons.Rounded.Info) {
                AboutSection(
                    isChecking = isCheckingForUpdate,
                    onCheckForUpdate = {
                        scope.launch {
                            isCheckingForUpdate = true
                            updateManager.checkForUpdates(force = true)
                                .onSuccess { info ->
                                    if (info != null) {
                                        updateInfoState = info
                                    } else {
                                        showNoUpdateDialog = true
                                    }
                                }
                                .onFailure { 
                                    Toast.makeText(context, "Check failed", Toast.LENGTH_SHORT).show()
                                }
                            isCheckingForUpdate = false
                        }
                    }
                )
            }
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Database") },
            text = { Text("This will create a copy of your database file. You can save it to your device or cloud storage.") },
            confirmButton = {
                Button(onClick = {
                    showExportDialog = false
                    exportLauncher.launch("question-quarry-backup.db")
                }) {
                    Text("Export")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Database") },
            text = { Text("Warning: This will replace your current data with the selected database file. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showImportDialog = false
                        importLauncher.launch(arrayOf("application/octet-stream", "*/*"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (updateInfoState != null) {
        AlertDialog(
            onDismissRequest = { updateInfoState = null },
            title = { Text("Update Available") },
            text = { Text("A new version (${updateInfoState!!.version}) is available. Would you like to download it now?") },
            confirmButton = {
                Button(onClick = {
                    updateManager.downloadAndInstall(updateInfoState!!)
                    updateInfoState = null
                }) {
                    Text("Download")
                }
            },
            dismissButton = {
                TextButton(onClick = { updateInfoState = null }) {
                    Text("Later")
                }
            }
        )
    }

    if (showNoUpdateDialog) {
        AlertDialog(
            onDismissRequest = { showNoUpdateDialog = false },
            title = { Text("Up to Date") },
            text = { Text("You are already on the latest version of QuestionQuarry.") },
            confirmButton = {
                TextButton(onClick = { showNoUpdateDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun AboutSection(
    isChecking: Boolean,
    onCheckForUpdate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "QuestionQuarry",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Version ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Button(
                    onClick = onCheckForUpdate,
                    enabled = !isChecking,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    if (isChecking) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Check for Updates", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            Text(
                "An open-source tool for question management and spaced repetition.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        content()
    }
}

@Composable
fun ColorSchemeSelector(
    selectedScheme: ColorSchemeId,
    onSchemeSelected: (ColorSchemeId) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ColorSchemeOption(
            color = Color(0xFF1565C0), // Blue
            selected = selectedScheme == ColorSchemeId.DEFAULT,
            onClick = { onSchemeSelected(ColorSchemeId.DEFAULT) }
        )
        ColorSchemeOption(
            color = Color(0xFF2E7D32), // Green
            selected = selectedScheme == ColorSchemeId.GREEN,
            onClick = { onSchemeSelected(ColorSchemeId.GREEN) }
        )
        ColorSchemeOption(
            color = Color(0xFFC62828), // Red
            selected = selectedScheme == ColorSchemeId.RED,
            onClick = { onSchemeSelected(ColorSchemeId.RED) }
        )
        ColorSchemeOption(
            color = Color(0xFF6650a4), // Purple (aligned with Purple40)
            selected = selectedScheme == ColorSchemeId.PURPLE,
            onClick = { onSchemeSelected(ColorSchemeId.PURPLE) }
        )
    }
}

@Composable
fun ColorSchemeOption(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.5f))
            )
        }
    }
}

@Composable
fun ThemeSelector(
    selectedMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .selectableGroup()
                .padding(vertical = 8.dp)
        ) {
            ThemeOption(
                text = "System Default",
                selected = selectedMode == ThemeMode.SYSTEM,
                onClick = { onModeSelected(ThemeMode.SYSTEM) }
            )
            ThemeOption(
                text = "Light",
                selected = selectedMode == ThemeMode.LIGHT,
                onClick = { onModeSelected(ThemeMode.LIGHT) }
            )
            ThemeOption(
                text = "Dark",
                selected = selectedMode == ThemeMode.DARK,
                onClick = { onModeSelected(ThemeMode.DARK) }
            )
        }
    }
}

@Composable
fun ThemeOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null // null recommended for accessibility with selectable modifier
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
