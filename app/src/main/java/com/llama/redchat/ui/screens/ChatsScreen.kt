package com.llama.redchat.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import com.llama.redchat.domain.model.*
import com.llama.redchat.ui.components.RssiIndicator
import com.llama.redchat.ui.components.TransportBadge
import com.llama.redchat.viewmodel.MainViewModel

@Composable
fun ChatsScreen(
    viewModel: MainViewModel,
    onOpenConversation: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val peers by viewModel.activePeers.collectAsState()
    val channels by viewModel.channelsState.collectAsState()
    val messagesMap by viewModel.messagesState.collectAsState()
    val settings by viewModel.settingsState.collectAsState()
    val torStatus by viewModel.torStatus.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    val filteredPeers = peers.filter {
        it.nickname.contains(searchQuery, ignoreCase = true)
    }

    val filteredChannels = channels.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Status Bar Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (settings.isBleMeshEnabled) {
                            Surface(
                                color = MeshBlue.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bluetooth,
                                        contentDescription = "Mesh",
                                        tint = MeshBlue,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.mesh_status_active),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MeshBlue
                                    )
                                }
                            }
                        }

                        if (settings.isTorEnabled && torStatus.isConnected) {
                            Surface(
                                color = TorOnionPurple.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = "Tor",
                                        tint = TorOnionPurple,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.tor_status_connected),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TorOnionPurple
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringResource(R.string.search_chats)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_chats_input"),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Direct Conversations Header
            item {
                Text(
                    text = "Mensajes Directos (${filteredPeers.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            if (filteredPeers.isEmpty()) {
                item {
                    EmptyChatsCard()
                }
            } else {
                items(filteredPeers, key = { it.peerId }) { peer ->
                    val lastMsg = messagesMap[peer.peerId]?.lastOrNull()
                    ChatItemRow(
                        title = peer.nickname,
                        subtitle = lastMsg?.content ?: "Sin mensajes aún",
                        timestamp = lastMsg?.timestamp,
                        isOnline = peer.isOnline,
                        rssiDbm = peer.rssiDbm,
                        transport = peer.transport,
                        isChannel = false,
                        unreadCount = if (lastMsg != null && lastMsg.senderPeerId != "my_self") 1 else 0,
                        onClick = { onOpenConversation(peer.peerId) }
                    )
                }
            }

            // Public Channels Section
            item {
                Text(
                    text = "Canales de la Red (${filteredChannels.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }

            items(filteredChannels, key = { it.channelId }) { channel ->
                val lastMsg = messagesMap[channel.channelId]?.lastOrNull()
                ChatItemRow(
                    title = channel.name,
                    subtitle = lastMsg?.let { "${it.senderNickname}: ${it.content}" } ?: channel.description,
                    timestamp = lastMsg?.timestamp ?: channel.createdAt,
                    isOnline = true,
                    rssiDbm = -60,
                    transport = TransportType.BLE_MESH,
                    isChannel = true,
                    isProtected = channel.isProtected,
                    unreadCount = 0,
                    onClick = { onOpenConversation(channel.channelId) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ChatItemRow(
    title: String,
    subtitle: String,
    timestamp: Long?,
    isOnline: Boolean,
    rssiDbm: Int,
    transport: TransportType,
    isChannel: Boolean,
    isProtected: Boolean = false,
    unreadCount: Int = 0,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("chat_item_${title.lowercase().replace(" ", "_")}"),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box {
                Surface(
                    color = if (isChannel) CrimsonPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isChannel) {
                            Icon(
                                imageVector = if (isProtected) Icons.Default.Lock else Icons.Default.Tag,
                                contentDescription = "Channel",
                                tint = CrimsonPrimary
                            )
                        } else {
                            Text(
                                text = title.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                if (isOnline && !isChannel) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(SignalGreen)
                            .align(Alignment.BottomEnd)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    timestamp?.let {
                        Text(
                            text = formatShortTime(it),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TransportBadge(transport = transport)
                    if (!isChannel) {
                        RssiIndicator(rssiDbm = rssiDbm)
                    }
                }
            }

            if (unreadCount > 0) {
                Surface(
                    color = CrimsonPrimary,
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(20.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = unreadCount.toString(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyChatsCard() {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.BluetoothSearching,
                contentDescription = "Buscando",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.no_chats_title),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.no_chats_subtitle),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun formatShortTime(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}
