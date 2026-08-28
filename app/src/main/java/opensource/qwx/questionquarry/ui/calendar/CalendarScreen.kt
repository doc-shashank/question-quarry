package opensource.qwx.questionquarry.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: opensource.qwx.questionquarry.ui.session.SessionViewModel,
    onNavigateToDailyList: (Long) -> Unit,
    onNavigateToTest: (List<Long>) -> Unit,
    onBack: () -> Unit,
) {
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDateForSheet by remember { mutableStateOf<Calendar?>(null) }
    var showTestDialog by remember { mutableStateOf(value = false) }
    var selectedDateForTest by remember { mutableStateOf<Calendar?>(null) }
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )
    
    val monthName = remember(currentMonth) {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentMonth.time)
    }

    val days = remember(currentMonth) {
        viewModel.getDaysOfMonth(currentMonth)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Calendar Browser",
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
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Month Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val newMonth = currentMonth.clone() as Calendar
                    newMonth.add(Calendar.MONTH, -1)
                    currentMonth = newMonth
                }) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Previous Month")
                }
                Text(
                    text = monthName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = {
                    val newMonth = currentMonth.clone() as Calendar
                    newMonth.add(Calendar.MONTH, 1)
                    currentMonth = newMonth
                }) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Next Month")
                }
            }

            Spacer(Modifier.height(16.dp))

            // Days of week header
            Row(modifier = Modifier.fillMaxWidth()) {
                val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Calendar Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(days) { date ->
                    if (date == null) {
                        Box(modifier = Modifier.aspectRatio(1f))
                    } else {
                        val isToday = isToday(date)
                        val isSelected = selectedDateForSheet?.let { isSameDay(it, date) } ?: false
                        
                        val dateMillis = date.timeInMillis

                        Box(
                            modifier = Modifier
                                .aspectRatio(0.7f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        isToday -> MaterialTheme.colorScheme.primaryContainer
                                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    }
                                )
                                .clickable {
                                    selectedDateForSheet = date.clone() as Calendar
                                    showSheet = true
                                }
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = date.get(Calendar.DAY_OF_MONTH).toString(),
                                        modifier = Modifier.align(Alignment.TopEnd),
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.onPrimary
                                            isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                            else -> MaterialTheme.colorScheme.onSurface
                                        },
                                        fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }

                                val sessionCount = viewModel.getSessionCountOnDate(dateMillis)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isSelected) Color.Black.copy(alpha = 0.1f) 
                                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                                        )
                                        .padding(vertical = 4.dp, horizontal = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (sessionCount > 0) sessionCount.toString() else "-",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 11.sp
                                        ),
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.onPrimary
                                            isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                            else -> MaterialTheme.colorScheme.primary
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { 
                showSheet = false
                selectedDateForSheet = null
            },
            sheetState = sheetState
        ) {
            selectedDateForSheet?.let { date ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val formattedDate = remember(date) {
                        try {
                            SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(date.time)
                        } catch (_: Exception) {
                            "Selected Date"
                        }
                    }
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                val time = date.timeInMillis
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        showSheet = false
                                        selectedDateForSheet = null
                                    }
                                }
                                onNavigateToDailyList(time)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.Visibility, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("View")
                        }
                        OutlinedButton(
                            onClick = {
                                selectedDateForTest = (date.clone() as Calendar)
                                showTestDialog = true
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        showSheet = false
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.Quiz, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Test")
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    val count = viewModel.getSessionCountOnDate(date.timeInMillis)
                    Text(
                        text = "YOU HAVE DONE $count SESSIONS",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }

    if (showTestDialog && (selectedDateForTest != null)) {
        val dateMillis = selectedDateForTest!!.timeInMillis
        val cal = Calendar.getInstance().apply {
            timeInMillis = dateMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val normalizedDate = cal.timeInMillis
        
        val allSessionsMap by viewModel.allSessionsByDate.collectAsState()
        val sessions = allSessionsMap[normalizedDate] ?: emptyList()
        
        val testSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        
        ModalBottomSheet(
            onDismissRequest = { 
                showTestDialog = false
                selectedDateForTest = null
            },
            sheetState = testSheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 48.dp)
            ) {
                Text(
                    "Select Sessions for Test",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )

                if (sessions.isEmpty()) {
                    Text(
                        "No sessions found for this day.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    val formattedDate = remember(selectedDateForTest) {
                        SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(selectedDateForTest!!.time)
                    }
                    
                    Surface(
                        onClick = {
                            onNavigateToTest(sessions.map { it.id })
                            showTestDialog = false
                            selectedDateForTest = null
                        },
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ListItem(
                            headlineContent = { 
                                Text(
                                    "All Sessions from $formattedDate",
                                    fontWeight = FontWeight.ExtraBold
                                ) 
                            },
                            supportingContent = { Text("${sessions.size} sessions") },
                            leadingContent = { 
                                Icon(
                                    Icons.Rounded.Quiz, 
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp)
                                ) 
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = Color.Transparent
                            )
                        )
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Text(
                        "INDIVIDUAL SESSIONS",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sessions.forEach { session ->
                            OutlinedCard(
                                onClick = {
                                    onNavigateToTest(listOf(session.id))
                                    showTestDialog = false
                                    selectedDateForTest = null
                                },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ListItem(
                                    headlineContent = { Text(session.title, fontWeight = FontWeight.Bold) },
                                    supportingContent = { Text("Session ID: ${session.id}") },
                                    leadingContent = { 
                                        Icon(
                                            Icons.Rounded.Quiz, 
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary
                                        ) 
                                    },
                                    colors = ListItemDefaults.colors(
                                        containerColor = Color.Transparent
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

// REMOVE getDaysOfMonth function from here as it's now in ViewModel

fun isToday(calendar: Calendar): Boolean {
    val today = Calendar.getInstance()
    return isSameDay(today, calendar)
}

fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)) &&
            (cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR))
}
