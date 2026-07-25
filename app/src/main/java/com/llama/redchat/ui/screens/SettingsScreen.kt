package com.llama.redchat.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.SignalRed
import com.llama.redchat.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settingsState.collectAsState()
    val torStatus by viewModel.torStatus.collectAsState()
    val decoysCount by viewModel.decoysCount.collectAsState()

    var nicknameInput by remember(settings.nickname) { mutableStateOf(settings.nickname) }
    var showWipeConfirmDialog by remember { mutableStateOf(false) }

    var tapCount by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.redchat_icon_1785017789097),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .pointerInput(Unit) {
                                    detectTapGestures(onTap = {
                                        tapCount++
                                        if (tapCount >= 3) {
                                            tapCount = 0
                                            showWipeConfirmDialog = true
                                        }
                                    })
                                }
                        )
                        Text(
                            text = stringResource(R.string.settings_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Temporary Profile Card
            SettingsCard(title = stringResource(R.string.profile_section)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = stringResource(R.string.nickname_label),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = nicknameInput,
                            onValueChange = { nicknameInput = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("nickname_edit_input"),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                if (nicknameInput.isNotBlank()) {
                                    viewModel.updateNickname(nicknameInput)
                                    Toast.makeText(context, "Nickname actualizado", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.testTag("save_nickname_button")
                        ) {
                            Text("Guardar")
                        }
                    }

                    Text(
                        text = "🔒 No se solicita correo, teléfono ni contraseña. Tu identidad es un par de claves efímeras X25519.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Appearance & Localization
            SettingsCard(title = stringResource(R.string.app_appearance)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.theme_label),
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = settings.theme == "DARK",
                                onClick = { viewModel.updateTheme("DARK") },
                                label = { Text(stringResource(R.string.theme_dark)) }
                            )
                            FilterChip(
                                selected = settings.theme == "LIGHT",
                                onClick = { viewModel.updateTheme("LIGHT") },
                                label = { Text(stringResource(R.string.theme_light)) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.language_label),
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = settings.language == "ES",
                                onClick = { viewModel.updateLanguage("ES") },
                                label = { Text(stringResource(R.string.lang_es)) }
                            )
                            FilterChip(
                                selected = settings.language == "EN",
                                onClick = { viewModel.updateLanguage("EN") },
                                label = { Text(stringResource(R.string.lang_en)) }
                            )
                        }
                    }
                }
            }

            // Bluetooth Mesh
            SettingsCard(title = stringResource(R.string.ble_mesh_section)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.ble_mesh_enable),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = stringResource(R.string.ble_mesh_desc),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.isBleMeshEnabled,
                            onCheckedChange = { viewModel.toggleBleMesh(it) },
                            modifier = Modifier.testTag("ble_mesh_toggle")
                        )
                    }
                }
            }

            // Tor Network
            SettingsCard(title = stringResource(R.string.tor_section)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.tor_enable),
                            fontWeight = FontWeight.SemiBold
                        )
                        Switch(
                            checked = settings.isTorEnabled,
                            onCheckedChange = { viewModel.toggleTor(it) },
                            modifier = Modifier.testTag("tor_toggle")
                        )
                    }

                    if (settings.isTorEnabled) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = stringResource(R.string.tor_node_status, torStatus.currentNode, torStatus.latencyMs),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Circuitos cebolla activos: ${torStatus.activeCircuits}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Cover Traffic
            SettingsCard(title = stringResource(R.string.cover_traffic_section)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.cover_traffic_enable),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = stringResource(R.string.cover_traffic_desc),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.isCoverTrafficEnabled,
                            onCheckedChange = { viewModel.toggleCoverTraffic(it) },
                            modifier = Modifier.testTag("cover_traffic_toggle")
                        )
                    }

                    if (settings.isCoverTrafficEnabled) {
                        Text(
                            text = "Paquetes señuelo transmitidos: $decoysCount",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CrimsonPrimary
                        )
                    }
                }
            }

            // Emergency Wipe Section
            Surface(
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Wipe",
                            tint = SignalRed
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.emergency_wipe_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = SignalRed
                        )
                    }

                    Text(
                        text = stringResource(R.string.emergency_wipe_desc),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )

                    Text(
                        text = stringResource(R.string.triple_tap_hint),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Button(
                        onClick = { showWipeConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SignalRed,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("emergency_wipe_button")
                    ) {
                        Text(
                            text = stringResource(R.string.trigger_wipe_button),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Emergency Wipe Modal Confirmation
    if (showWipeConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showWipeConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DeleteForever, contentDescription = "Wipe", tint = SignalRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.wipe_confirm_title), color = SignalRed)
                }
            },
            text = {
                Text(stringResource(R.string.wipe_confirm_message))
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.emergencyWipe()
                        showWipeConfirmDialog = false
                        Toast.makeText(context, "Borrado de Emergencia Ejecutado", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SignalRed),
                    modifier = Modifier.testTag("confirm_emergency_wipe_button")
                ) {
                    Text(stringResource(R.string.confirm_wipe))
                }
            },
            dismissButton = {
                TextButton(onClick = { showWipeConfirmDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}
