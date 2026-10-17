package com.droidtree.app.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.droidtree.app.core.model.FileNode
import com.droidtree.app.ui.browser.FileBrowserScreen
import com.droidtree.app.ui.dashboard.DashboardScreen
import com.droidtree.app.ui.treemap.TreemapScreen
import java.net.URLDecoder

@Composable
fun DroidTreeNavGraph() {
    val navController = rememberNavController()
    val activeScannedRoot = remember { mutableStateOf<FileNode?>(null) }

    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToTreemap = { root ->
                    activeScannedRoot.value = root
                    navController.navigate(Screen.Treemap.route)
                },
                onNavigateToFileBrowser = { path ->
                    navController.navigate(Screen.FileBrowser.createRoute(path))
                }
            )
        }

        composable(Screen.Treemap.route) {
            val root = activeScannedRoot.value
            if (root != null) {
                TreemapScreen(
                    rootNode = root,
                    onNavigateBack = { navController.popBackStack() }
                )
            } else {
                DashboardScreen(
                    onNavigateToTreemap = {},
                    onNavigateToFileBrowser = {}
                )
            }
        }

        composable(
            route = Screen.FileBrowser.route,
            arguments = listOf(
                navArgument("path") {
                    type = NavType.StringType
                    defaultValue = android.os.Environment.getExternalStorageDirectory().absolutePath
                }
            )
        ) { backStackEntry ->
            val encodedPath = backStackEntry.arguments?.getString("path") ?: ""
            val path = try {
                URLDecoder.decode(encodedPath, "UTF-8")
            } catch (e: Exception) {
                encodedPath
            }

            FileBrowserScreen(
                initialPath = path,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
