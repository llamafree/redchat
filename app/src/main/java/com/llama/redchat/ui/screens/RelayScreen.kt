package com.llama.redchat.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SignalGreen
import com.llama.redchat.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelayScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val relayState by viewModel.relayState.collectAsState()

    var enabled by remember(relayState.isEnabled) { mutableStateOf(relayState.isEnabled) }
    var onlyCharging by remember(relayState.onlyWhenCharging) { mutableStateOf(relayState.onlyWhenCharging) }
    var onlyWifi by remember(relayState.onlyWifi) { mutableStateOf(relayState.onlyWifi) }
    var screenOff by remember(relayState.screenOffOnly) { mutableStateOf(relayState.screenOffOnly) }
    var limitBattery by remember(relayState.limitBattery) { mutableStateOf(relayState.limitBattery) }
    var limitData by remember(relayState.limitData) { mutableStateOf(relayState.limitData) }

    fun update() {
        viewModel.updateRelaySettings(
            enabled = enabled,
            onlyCharging = onlyCharging,
            onlyWifi = onlyWifi,
            minBattery = 15,
            screenOff = screenOff,
            limitBattery = limitBattery,
            limitData = limitData
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Router, contentDescription = "Relay", tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "Relay Voluntario Mesh",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Transparency & Consent Banner
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Security, contentDescription = "Security", tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "Transparencia y Cifrado E2EE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "Al activar el nodo Relay, tu teléfono ayuda a transportar paquetes cifrados de extremo a extremo entre nodos de la red descentralizada REDChat. Por diseño criptográfico, el Relay NUNCA puede leer el contenido de los mensajes ni conocer claves privadas.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Main Switch
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Convertir mi teléfono en Relay",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = if (enabled) "Nodo activo y retransmitiendo paquetes" else "Nodo desactivado",
                                fontSize = 12.sp,
                                color = if (enabled) SignalGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = enabled,
                            onCheckedChange = {
                                enabled = it
                                update()
                            }
                        )
                    }
                }
            }

            // Statistics Section
            item {
                Text(
                    text = "Estadísticas en Tiempo Real",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatRow("Estado del Relay", if (enabled) "Activo (Multi-Hop)" else "Inactivo")
                        StatRow("Tiempo activo", "${relayState.activeTimeSeconds} seg")
                        StatRow("Paquetes retransmitidos", "${relayState.packetsRetransmittedCount}")
                        StatRow("Dispositivos ayudados", "${relayState.devicesHelpedCount}")
                        StatRow("Latencia promedio", "${relayState.averageLatencyMs} ms")
                        StatRow("Consumo de batería", "${relayState.batteryUsagePercent}%")
                        StatRow("Consumo de datos", "${relayState.dataUsageBytes} bytes")
                    }
                }
            }

            // Policies / Configuration Section
            item {
                Text(
                    text = "Políticas de Rendimiento y Batería",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PolicyRow("Solo cuando esté cargando", onlyCharging) {
                            onlyCharging = it
                            update()
                        }
                        PolicyRow("Solo con Wi-Fi", onlyWifi) {
                            onlyWifi = it
                            update()
                        }
                        PolicyRow("Solo con pantalla apagada", screenOff) {
                            screenOff = it
                            update()
                        }
                        PolicyRow("Limitar consumo de batería", limitBattery) {
                            limitBattery = it
                            update()
                        }
                        PolicyRow("Limitar uso de datos", limitData) {
                            limitData = it
                            update()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PolicyRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, fontSize = 14.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
