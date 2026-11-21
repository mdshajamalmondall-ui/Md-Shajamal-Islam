package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.AppLanguage
import com.example.data.local.ThemeMode
import com.example.data.local.bi
import com.example.ui.screens.FilesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ToolsHubScreen
import com.example.ui.theme.NursingAiTheme
import com.example.ui.viewmodel.NursingViewModel

object NursingRoutes {
    const val HOME = "home"
    const val TOOLS = "tools"
    const val FILES = "files"
    const val PROFILE = "profile"
}

private data class NavTab(
    val route: String,
    val labelEn: String,
    val labelBn: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureAppCheck(this, intent)
        enableEdgeToEdge()
        setContent {
            val viewModel: NursingViewModel = viewModel()
            val prefs by viewModel.preferences.collectAsStateWithLifecycle()
            val isDark = when (prefs.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            NursingAiTheme(darkTheme = isDark) {
                NursingAiAppShell(viewModel = viewModel, language = prefs.language)
            }
        }
    }
}

@Composable
fun NursingAiAppShell(
    viewModel: NursingViewModel,
    language: AppLanguage
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val tabs = listOf(
        NavTab(NursingRoutes.HOME, "Search", "সার্চ", Icons.Filled.Search, Icons.Outlined.Search),
        NavTab(NursingRoutes.TOOLS, "Tools", "টুলস", Icons.Filled.Build, Icons.Outlined.Build),
        NavTab(NursingRoutes.FILES, "Files", "ফাইলস", Icons.Filled.Folder, Icons.Outlined.Folder),
        NavTab(NursingRoutes.PROFILE, "Profile", "প্রোফাইল", Icons.Filled.Person, Icons.Outlined.Person)
    )

    fun navigateTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (!useRail) {
                    NavigationBar {
                        tabs.forEach { tab ->
                            val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = bi(language, tab.labelEn, tab.labelBn)
                                    )
                                },
                                label = {
                                    Text(
                                        text = bi(language, tab.labelEn, tab.labelBn),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier.testTag("nav_${tab.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail {
                        tabs.forEach { tab ->
                            val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = bi(language, tab.labelEn, tab.labelBn)
                                    )
                                },
                                label = { Text(bi(language, tab.labelEn, tab.labelBn)) },
                                modifier = Modifier.testTag("rail_${tab.route}")
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = NursingRoutes.HOME,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(NursingRoutes.HOME) {
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigateToTools = { navigateTab(NursingRoutes.TOOLS) },
                            onNavigateToFiles = { navigateTab(NursingRoutes.FILES) }
                        )
                    }
                    composable(NursingRoutes.TOOLS) {
                        ToolsHubScreen(
                            viewModel = viewModel,
                            onAskInMainChat = { prompt ->
                                viewModel.startNewChat()
                                viewModel.sendChatQuery(prompt)
                                navigateTab(NursingRoutes.HOME)
                            }
                        )
                    }
                    composable(NursingRoutes.FILES) {
                        FilesScreen(viewModel = viewModel)
                    }
                    composable(NursingRoutes.PROFILE) {
                        ProfileScreen(
                            viewModel = viewModel,
                            onAskQuestionInSearch = { prompt ->
                                viewModel.startNewChat()
                                viewModel.sendChatQuery(prompt)
                                navigateTab(NursingRoutes.HOME)
                            }
                        )
                    }
                }
            }
        }
    }
}
