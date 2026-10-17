package com.droidtree.app.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Treemap : Screen("treemap")
    object FileBrowser : Screen("browser?path={path}") {
        fun createRoute(path: String) = "browser?path=${java.net.URLEncoder.encode(path, "UTF-8")}"
    }
}
