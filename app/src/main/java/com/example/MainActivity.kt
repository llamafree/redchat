package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.*
import com.example.ui.theme.REDChatTheme
import com.llama.redchat.notification.NotificationHelper
import com.llama.redchat.service.RedMeshService
import com.llama.redchat.ui.screens.*
import com.llama.redchat.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Notification Channels
        NotificationHelper.initNotificationChannels(this)

        // Start Foreground BLE Mesh Service
        try {
            val serviceIntent = Intent(this, RedMeshService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } catch (e: Exception) {
            // Service startup fallback
        }

        val initialTargetId = intent?.getStringExtra(NotificationHelper.EXTRA_TARGET_ID)

        setContent {
            val settings by viewModel.settingsState.collectAsState()

            // Request Notification and BLE permissions on startup
            RequestRequiredPermissions()

            REDChatTheme(themeSelection = settings.theme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    REDChatApp(
                        viewModel = viewModel,
                        initialTargetId = initialTargetId
                    )
                }
            }
        }
    }
}

@Composable
fun RequestRequiredPermissions() {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun REDChatApp(
    viewModel: MainViewModel,
    initialTargetId: String? = null
) {
    val navController = rememberNavController()

    LaunchedEffect(initialTargetId) {
        if (!initialTargetId.isNullOrEmpty()) {
            navController.navigate("conversation/$initialTargetId")
        }
    }

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                viewModel = viewModel,
                onSplashFinished = {
                    navController.navigate("main") {
                        popUpTo("splash") { inclusive = true }
                    }
                    if (!initialTargetId.isNullOrEmpty()) {
                        navController.navigate("conversation/$initialTargetId")
                    }
                }
            )
        }

        composable("main") {
            MainScaffold(
                viewModel = viewModel,
                onOpenConversation = { targetId ->
                    navController.navigate("conversation/$targetId")
                }
            )
        }

        composable("conversation/{targetId}") { backStackEntry ->
            val targetId = backStackEntry.arguments?.getString("targetId") ?: "chan_general"
            ConversationScreen(
                targetId = targetId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onOpenInfo = {
                    navController.navigate("channel_info/$targetId")
                }
            )
        }

        composable("channel_info/{channelId}") { backStackEntry ->
            val channelId = backStackEntry.arguments?.getString("channelId") ?: "chan_general"
            ChannelInfoScreen(
                channelId = channelId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun MainScaffold(
    viewModel: MainViewModel,
    onOpenConversation: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar"),
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chats") },
                    label = { Text("Chats") },
                    modifier = Modifier.testTag("nav_chats_tab")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Bluetooth, contentDescription = "REDLink") },
                    label = { Text("REDLink") },
                    modifier = Modifier.testTag("nav_redlink_tab")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Forum, contentDescription = "Canales") },
                    label = { Text("Canales") },
                    modifier = Modifier.testTag("nav_channels_tab")
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Router, contentDescription = "Relay") },
                    label = { Text("Relay") },
                    modifier = Modifier.testTag("nav_relay_tab")
                )

                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Ajustes") },
                    label = { Text("Ajustes") },
                    modifier = Modifier.testTag("nav_settings_tab")
                )

                NavigationBarItem(
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5 },
                    icon = { Icon(Icons.Default.Terminal, contentDescription = "Diagnóstico") },
                    label = { Text("Logs") },
                    modifier = Modifier.testTag("nav_info_tab")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> ChatsScreen(viewModel = viewModel, onOpenConversation = onOpenConversation)
                1 -> RedLinkScreen(viewModel = viewModel, onOpenConversation = onOpenConversation)
                2 -> ChannelsScreen(viewModel = viewModel, onOpenChannel = onOpenConversation)
                3 -> RelayScreen(viewModel = viewModel)
                4 -> SettingsScreen(viewModel = viewModel)
                5 -> DiagnosticsScreen(viewModel = viewModel)
            }
        }
    }
}

