package opensource.qwx.questionquarry

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.room.Room
import opensource.qwx.questionquarry.data.local.QuestionQuarryDatabase
import opensource.qwx.questionquarry.navigation.Route
import opensource.qwx.questionquarry.ui.calendar.CalendarScreen
import opensource.qwx.questionquarry.ui.calendar.DailySessionListScreen
import opensource.qwx.questionquarry.ui.home.HomeScreen
import opensource.qwx.questionquarry.ui.session.NewSessionScreen
import opensource.qwx.questionquarry.ui.session.SessionDetailScreen
import opensource.qwx.questionquarry.ui.session.SessionViewModel
import opensource.qwx.questionquarry.ui.session.SubjectsBrowserScreen
import opensource.qwx.questionquarry.ui.session.TextEditorScreen
import opensource.qwx.questionquarry.ui.test.TestScreen
import opensource.qwx.questionquarry.ui.test.TestViewModel
import opensource.qwx.questionquarry.ui.settings.ColorSchemeId
import opensource.qwx.questionquarry.ui.settings.SettingsScreen
import opensource.qwx.questionquarry.ui.settings.ThemeMode
import opensource.qwx.questionquarry.ui.settings.ThemeViewModel
import opensource.qwx.questionquarry.ui.theme.QuestionQuarryTheme

class MainActivity : ComponentActivity() {
    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            QuestionQuarryDatabase::class.java,
            "question-quarry-db"
        ).fallbackToDestructiveMigration(true).build()
    }
    
    private val viewModel by lazy { SessionViewModel(db.blockDao()) }
    private val testViewModel by lazy { TestViewModel(db.blockDao()) }
    private val themeViewModel by lazy { ThemeViewModel(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
            val colorSchemeId by themeViewModel.colorSchemeId.collectAsStateWithLifecycle(initialValue = ColorSchemeId.DEFAULT)

            QuestionQuarryTheme(
                themeMode = themeMode,
                colorSchemeId = colorSchemeId
            ) {
                val backStack = rememberNavBackStack(Route.Home())
                val currentRoute = backStack.lastOrNull()

                Scaffold(
                    bottomBar = {
                        AnimatedVisibility(
                            visible = currentRoute is Route.Home || currentRoute is Route.Calendar || currentRoute is Route.Settings,
                            enter = slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy)
                            ) + fadeIn(animationSpec = tween(250)),
                            exit = slideOutVertically(
                                targetOffsetY = { it },
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy)
                            ) + fadeOut(animationSpec = tween(250))
                        ) {
                            NavigationBar(
                                modifier = Modifier.graphicsLayer {
                                    // Offload to GPU layer to prevent relayout jank during animation
                                    compositingStrategy = CompositingStrategy.Offscreen
                                }
                            ) {
                                NavigationBarItem(
                                    selected = currentRoute is Route.Home,
                                    onClick = { 
                                        if (currentRoute !is Route.Home) {
                                            backStack.removeIf { true }
                                            backStack.add(Route.Home())
                                        }
                                    },
                                    icon = { Icon(Icons.Rounded.Home, contentDescription = "Home") },
                                    label = { Text("Home") }
                                )
                                NavigationBarItem(
                                    selected = currentRoute is Route.Calendar,
                                    onClick = {
                                        if (currentRoute !is Route.Calendar) {
                                            backStack.add(Route.Calendar)
                                        }
                                    },
                                    icon = { Icon(Icons.Rounded.CalendarMonth, contentDescription = "Calendar") },
                                    label = { Text("Calendar") }
                                )
                                NavigationBarItem(
                                    selected = currentRoute is Route.Settings,
                                    onClick = {
                                        if (currentRoute !is Route.Settings) {
                                            backStack.add(Route.Settings)
                                        }
                                    },
                                    icon = { Icon(Icons.Rounded.Settings, contentDescription = "Settings") },
                                    label = { Text("Settings") }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavDisplay(
                            backStack = backStack,
                            entryDecorators = listOf(
                                rememberSaveableStateHolderNavEntryDecorator(),
                                rememberViewModelStoreNavEntryDecorator()
                            ),
                            onBack = { 
                                if (backStack.size > 1) {
                                    backStack.removeAt(backStack.size - 1)
                                }
                            },
                            entryProvider = entryProvider {
                                entry<Route.Home> { route ->
                                    HomeScreen(
                                        viewModel = viewModel,
                                        filterDate = route.filterDate,
                                        onNavigateToNewSession = { title, sessionId -> 
                                            viewModel.resetDraft()
                                            if (sessionId != null) {
                                                viewModel.loadSessionForEditing(sessionId)
                                            }
                                            backStack.add(Route.NewSession(title, sessionId)) 
                                        },
                                        onNavigateToSettings = {
                                            backStack.add(Route.Settings)
                                        },
                                        onNavigateToSessionDetail = { sessionId ->
                                            backStack.add(Route.SessionDetail(sessionId))
                                        },
                                        onNavigateToSubjects = {
                                            backStack.add(Route.SubjectsBrowser)
                                        }
                                    )
                                }
                                entry<Route.SubjectsBrowser> {
                                    SubjectsBrowserScreen(
                                        viewModel = viewModel,
                                        onNavigateToSessionDetail = { sessionId ->
                                            backStack.add(Route.SessionDetail(sessionId))
                                        },
                                        onBack = {
                                            if (backStack.size > 1) {
                                                backStack.removeAt(backStack.size - 1)
                                            }
                                        }
                                    )
                                }
                                entry<Route.Settings> {
                                    SettingsScreen(
                                        viewModel = themeViewModel,
                                        onBack = {
                                            if (backStack.size > 1) {
                                                backStack.removeAt(backStack.size - 1)
                                            }
                                        }
                                    )
                                }
                                entry<Route.Calendar> {
                                    CalendarScreen(
                                        viewModel = viewModel,
                                        onNavigateToDailyList = { date ->
                                            backStack.add(Route.DailySessionList(date))
                                        },
                                        onNavigateToTest = { sessionIds ->
                                            backStack.add(Route.Test(sessionIds))
                                        },
                                        onBack = {
                                            if (backStack.size > 1) {
                                                backStack.removeAt(backStack.size - 1)
                                            }
                                        }
                                    )
                                }
                                entry<Route.DailySessionList> { route ->
                                    DailySessionListScreen(
                                        date = route.date,
                                        viewModel = viewModel,
                                        onSessionClick = { sessionId ->
                                            backStack.add(Route.SessionDetail(sessionId))
                                        },
                                        onBack = {
                                            if (backStack.size > 1) {
                                                backStack.removeAt(backStack.size - 1)
                                            }
                                        }
                                    )
                                }
                                entry<Route.SessionDetail> { route ->
                                    SessionDetailScreen(
                                        sessionId = route.sessionId,
                                        viewModel = viewModel,
                                        onEditSession = { title ->
                                            viewModel.resetDraft()
                                            viewModel.loadSessionForEditing(route.sessionId)
                                            backStack.add(Route.NewSession(title, route.sessionId))
                                        },
                                        onBack = {
                                            if (backStack.size > 1) {
                                                backStack.removeAt(backStack.size - 1)
                                            }
                                        }
                                    )
                                }
                                entry<Route.NewSession> { route ->
                                    NewSessionScreen(
                                        title = route.title,
                                        sessionId = route.sessionId,
                                        viewModel = viewModel,
                                        onBack = { 
                                            if (backStack.size > 1) {
                                                backStack.removeAt(backStack.size - 1)
                                            }
                                        },
                                        onNavigateToTextEditor = { pIdx, bIdx, isQ ->
                                            backStack.add(Route.TextEditor(pIdx, bIdx, isQ))
                                        },
                                        onComplete = { title, pairs, subject, chNo, chName, topic, isTopicEnabled ->
                                            viewModel.saveSession(title, pairs, subject, chNo, chName, topic, isTopicEnabled) {
                                                if (backStack.size > 1) {
                                                    backStack.removeAt(backStack.size - 1)
                                                }
                                            }
                                        }
                                    )
                                }
                                entry<Route.TextEditor> { route ->
                                    TextEditorScreen(
                                        pairIndex = route.pairIndex,
                                        blockIndex = route.blockIndex,
                                        isQuestion = route.isQuestion,
                                        viewModel = viewModel,
                                        onBack = {
                                            if (backStack.size > 1) {
                                                backStack.removeAt(backStack.size - 1)
                                            }
                                        }
                                    )
                                }
                                entry<Route.Test> { route ->
                                    TestScreen(
                                        sessionIds = route.sessionIds,
                                        viewModel = testViewModel,
                                        onBack = {
                                            if (backStack.size > 1) {
                                                backStack.removeAt(backStack.size - 1)
                                            }
                                        }
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
