package com.ud4.skateco

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.ud4.skateco.ui.screens.CreateSpotScreen
import com.ud4.skateco.ui.screens.LoginScreen
import com.ud4.skateco.ui.screens.MapScreen
import com.ud4.skateco.ui.screens.ProfileAdminScreen
import com.ud4.skateco.ui.screens.SpotDetailScreen
import com.ud4.skateco.ui.theme.ConcreteSurface
import com.ud4.skateco.ui.theme.SCOTheme
import com.ud4.skateco.ui.theme.TextMuted
import com.ud4.skateco.ui.theme.UrethaneLime
import kotlinx.serialization.Serializable

enum class UserRole { GUEST, USER, ADMIN }

// ── Rutas de navegación ──────────────────────────────────────
@Serializable object LoginRoute
@Serializable object MapRoute
@Serializable data class SpotDetailRoute(val spotId: String)
@Serializable object CreateSpotRoute
@Serializable object ProfileRoute

// ── Destinos de la barra inferior ────────────────────────────
data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: Any          // ruta type-safe (objeto @Serializable)
)

val bottomNavItems = listOf(
    BottomNavItem("Mapa", Icons.Default.Explore, MapRoute),
    BottomNavItem("Perfil", Icons.Default.Person, ProfileRoute)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SCOTheme {
                SkateCOApp()
            }
        }
    }
}

@Composable
fun SkateCOApp() {
    val navController = rememberNavController()
    var userRole by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(UserRole.GUEST) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Mostrar bottom bar solo en las pantallas principales (Mapa y Perfil)
    val showBottomBar = currentDestination?.let { dest ->
        dest.hasRoute(MapRoute::class) || dest.hasRoute(ProfileRoute::class)
    } ?: false

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = ConcreteSurface) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hasRoute(item.route::class) == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = UrethaneLime,
                                selectedTextColor = UrethaneLime,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted,
                                indicatorColor = ConcreteSurface
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = LoginRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1️⃣ Login → entra al Mapa tras iniciar sesión o como invitado
            composable<LoginRoute> {
                LoginScreen(
                    onSignInClick = { _, _ ->
                        // Authentication requires a verified backend session. A local UI login
                        // must never grant an application role.
                    },
                    onContinueAsGuestClick = {
                        userRole = UserRole.GUEST
                        navController.navigate(MapRoute) {
                            popUpTo(LoginRoute) { inclusive = true }
                        }
                    }
                )
            }

            // 2️⃣ Mapa principal
            composable<MapRoute> {
                MapScreen(
                    onSpotClick = { spotId ->
                        navController.navigate(SpotDetailRoute(spotId))
                    },
                    onCreateSpotClick = {
                        if (userRole == UserRole.USER || userRole == UserRole.ADMIN) {
                            navController.navigate(CreateSpotRoute)
                        }
                    },
                    onProfileClick = {
                        navController.navigate(ProfileRoute) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            // 3️⃣ Detalle de un Spot
            composable<SpotDetailRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<SpotDetailRoute>()
                SpotDetailScreen(
                    spotId = route.spotId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // 4️⃣ Crear nuevo Spot
            composable<CreateSpotRoute> {
                CreateSpotScreen(
                    onBackClick = { navController.popBackStack() },
                    onSaveSpotClick = { _, _, _, _ ->
                        navController.popBackStack()
                    }
                )
            }

            // 5️⃣ Perfil y Administración
            composable<ProfileRoute> {
                ProfileAdminScreen(
                    role = userRole,
                    onSignOutClick = {
                        navController.navigate(LoginRoute) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onSpotClick = { spotId ->
                        navController.navigate(SpotDetailRoute(spotId))
                    }
                )
            }
        }
    }
}
