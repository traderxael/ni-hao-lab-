package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.TtsManager
import com.example.data.StoreRepository
import com.example.ui.components.AppHeader
import com.example.ui.components.AppIcons
import com.example.ui.components.ConfettiOverlay
import com.example.ui.components.ToastNotification
import com.example.ui.screens.*
import com.example.ui.theme.NiHaoLabTheme
import com.example.ui.theme.SelloRed

data class NavItem(
    val route: String,
    val title: String,
    val icon: @Composable () -> Unit
)

class MainActivity : ComponentActivity() {

    private lateinit var ttsManager: TtsManager
    private lateinit var store: StoreRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ttsManager = TtsManager(this)
        store = StoreRepository.getInstance(this)

        setContent {
            val state by store.state.collectAsState()
            val toastMessage by store.toastEvents.collectAsState()
            val confettiTrigger by store.confettiTrigger.collectAsState()

            ttsManager.isMuted = !state.sonido

            NiHaoLabTheme(darkTheme = state.isDarkTheme) {
                var currentRoute by remember { mutableStateOf("home") }

                val navItems = listOf(
                    NavItem("home", "Inicio") { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    NavItem("ruta", "Ruta") { Icon(AppIcons.route, contentDescription = "Ruta") },
                    NavItem("quiz", "Quiz") { Icon(Icons.Default.CheckCircle, contentDescription = "Quiz") },
                    NavItem("cuaderno", "Cuaderno") { Icon(AppIcons.book, contentDescription = "Cuaderno") },
                    NavItem("progreso", "Progreso") { Icon(AppIcons.barChart, contentDescription = "Progreso") }
                )

                val isPrimaryTab = navItems.any { it.route == currentRoute }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("main_scaffold"),
                    topBar = {
                        if (isPrimaryTab) {
                            AppHeader(
                                state = state,
                                onToggleSound = { store.toggleSonido() },
                                onToggleTheme = { store.toggleTema() },
                                onOpenTienda = { currentRoute = "tienda" },
                                onOpenStreak = {
                                    currentRoute = "home"
                                    store.triggerStreakCelebrationManual()
                                }
                            )
                        }
                    },
                    bottomBar = {
                        if (isPrimaryTab) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.testTag("bottom_navigation_bar")
                            ) {
                                navItems.forEach { item ->
                                    NavigationBarItem(
                                        selected = currentRoute == item.route,
                                        onClick = { currentRoute = item.route },
                                        icon = item.icon,
                                        label = { Text(item.title, fontSize = 11.sp) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = SelloRed,
                                            selectedTextColor = SelloRed,
                                            indicatorColor = SelloRed.copy(alpha = 0.12f)
                                        ),
                                        modifier = Modifier.testTag("nav_item_${item.route}")
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentRoute) {
                            "home" -> HomeScreen(
                                store = store,
                                state = state,
                                ttsManager = ttsManager,
                                onNavigate = { currentRoute = it }
                            )
                            "ruta" -> RutaScreen(
                                store = store,
                                ttsManager = ttsManager,
                                onBack = { currentRoute = "home" }
                            )
                            "quiz" -> QuizScreen(
                                store = store,
                                ttsManager = ttsManager,
                                onBack = { currentRoute = "home" }
                            )
                            "memorama" -> MemoramaScreen(
                                store = store,
                                ttsManager = ttsManager,
                                onBack = { currentRoute = "home" }
                            )
                            "escucha" -> EscuchaScreen(
                                store = store,
                                ttsManager = ttsManager,
                                onBack = { currentRoute = "home" }
                            )
                            "pinyin" -> PinyinScreen(
                                store = store,
                                ttsManager = ttsManager,
                                onBack = { currentRoute = "home" }
                            )
                            "tonos" -> TonosScreen(
                                store = store,
                                ttsManager = ttsManager,
                                onBack = { currentRoute = "home" }
                            )
                            "pronuncia" -> PronunciaScreen(
                                store = store,
                                ttsManager = ttsManager,
                                onBack = { currentRoute = "home" }
                            )
                            "escribe" -> EscribeScreen(
                                store = store,
                                ttsManager = ttsManager,
                                onBack = { currentRoute = "home" }
                            )
                            "flash" -> FlashcardScreen(
                                store = store,
                                ttsManager = ttsManager,
                                onBack = { currentRoute = "home" }
                            )
                            "cultura" -> CulturaScreen(
                                store = store,
                                ttsManager = ttsManager,
                                onBack = { currentRoute = "home" }
                            )
                            "cuaderno" -> CuadernoScreen(
                                store = store,
                                ttsManager = ttsManager,
                                onBack = { currentRoute = "home" }
                            )
                            "progreso" -> ProgresoScreen(
                                store = store,
                                state = state,
                                onBack = { currentRoute = "home" }
                            )
                            "tienda" -> TiendaScreen(
                                store = store,
                                state = state,
                                onBack = { currentRoute = "home" }
                            )
                            else -> HomeScreen(
                                store = store,
                                state = state,
                                ttsManager = ttsManager,
                                onNavigate = { currentRoute = it }
                            )
                        }

                        // App-wide Confetti overlay
                        ConfettiOverlay(trigger = confettiTrigger)

                        // Toast notifications overlay
                        ToastNotification(
                            message = toastMessage,
                            onDismiss = { store.toastEvents.value = null }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.destroy()
    }
}
