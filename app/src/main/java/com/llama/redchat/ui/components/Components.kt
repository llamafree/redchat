package com.llama.redchat.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import com.llama.redchat.domain.model.*

@Composable
fun RssiIndicator(rssiDbm: Int, modifier: Modifier = Modifier) {
    val (color, labelEs, labelEn) = when {
        rssiDbm >= -55 -> Triple(SignalGreen, "Excelente", "Excellent")
        rssiDbm >= -70 -> Triple(SignalYellow, "Buena", "Good")
        rssiDbm >= -85 -> Triple(SignalOrange, "Regular", "Fair")
        else -> Triple(SignalRed, "Débil", "Weak")
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = "$rssiDbm dBm",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun TransportBadge(transport: TransportType, modifier: Modifier = Modifier) {
    val (color, icon, text) = when (transport) {
        TransportType.BLE_MESH -> Triple(MeshBlue, Icons.Default.BluetoothSearching, "BLE Mesh")
        TransportType.TOR -> Triple(TorOnionPurple, Icons.Default.Security, "Tor Onion")
        TransportType.INTERNET -> Triple(SignalGreen, Icons.Default.Public, "Internet")
    }

    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = text,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun EncryptionHeaderBadge() {
    Surface(
        color = SignalGreen.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "E2EE",
                tint = SignalGreen,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.e2ee_verified),
                fontSize = 11.sp,
                color = SignalGreen,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isSelf: Boolean,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bubbleColor = if (isSelf) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isSelf) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isSelf) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isSelf) 16.dp else 4.dp,
                bottomEnd = if (isSelf) 4.dp else 16.dp
            ),
            modifier = Modifier
                .widthIn(max = 300.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = { onLongClick() })
                }
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!isSelf && message.isChannel) {
                    Text(
                        text = message.senderNickname,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = CrimsonAccent
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                if (message.attachmentName != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (message.type) {
                                    MessageType.IMAGE -> Icons.Default.Image
                                    MessageType.AUDIO -> Icons.Default.Mic
                                    MessageType.LOCATION -> Icons.Default.LocationOn
                                    MessageType.CONTACT -> Icons.Default.Person
                                    else -> Icons.Default.InsertDriveFile
                                },
                                contentDescription = "Adjunto",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = message.attachmentName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Text(
                    text = message.content,
                    color = textColor,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TransportBadge(transport = message.transport)

                    Text(
                        text = formatTime(message.timestamp),
                        fontSize = 10.sp,
                        color = textColor.copy(alpha = 0.7f)
                    )

                    if (isSelf) {
                        val (statusIcon, statusTint) = when (message.status) {
                            MessageStatus.SENDING -> Pair(Icons.Default.Schedule, textColor.copy(alpha = 0.5f))
                            MessageStatus.SENT -> Pair(Icons.Default.Check, textColor.copy(alpha = 0.7f))
                            MessageStatus.DELIVERED -> Pair(Icons.Default.DoneAll, textColor.copy(alpha = 0.7f))
                            MessageStatus.READ -> Pair(Icons.Default.DoneAll, SignalGreen)
                        }
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = "Estado",
                            tint = statusTint,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SlashCommandOverlay(
    query: String,
    onSelectCommand: (String) -> Unit
) {
    val commands = listOf(
        Pair("/j #canal", stringResource(R.string.cmd_join)),
        Pair("/m @usuario", stringResource(R.string.cmd_msg)),
        Pair("/w", stringResource(R.string.cmd_who)),
        Pair("/channels", stringResource(R.string.cmd_channels)),
        Pair("/block @user", stringResource(R.string.cmd_block)),
        Pair("/unblock @user", stringResource(R.string.cmd_unblock)),
        Pair("/clear", stringResource(R.string.cmd_clear)),
        Pair("/pass clave", stringResource(R.string.cmd_pass)),
        Pair("/transfer @user", stringResource(R.string.cmd_transfer)),
        Pair("/save", stringResource(R.string.cmd_save))
    ).filter { it.first.startsWith(query, ignoreCase = true) || query == "/" }

    if (commands.isNotEmpty()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = "Comandos Slash Disponibles",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                commands.take(5).forEach { (cmd, desc) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectCommand(cmd.split(" ")[0] + " ") }
                            .padding(vertical = 6.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = cmd,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(110.dp)
                        )
                        Text(
                            text = desc,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentPickerSheet(
    onDismiss: () -> Unit,
    onSelectAttachment: (MessageType, String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.action_attachments),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val options = listOf(
                Triple(Icons.Default.Image, stringResource(R.string.attach_image), MessageType.IMAGE to "foto_redchat_encrypt.jpg"),
                Triple(Icons.Default.InsertDriveFile, stringResource(R.string.attach_document), MessageType.FILE to "documento_privado.pdf"),
                Triple(Icons.Default.Mic, stringResource(R.string.attach_audio), MessageType.AUDIO to "nota_voz_x25519.aac"),
                Triple(Icons.Default.LocationOn, stringResource(R.string.attach_location), MessageType.LOCATION to "Ubicación GPS Cifrada (-33.45, -70.66)"),
                Triple(Icons.Default.Person, stringResource(R.string.attach_contact), MessageType.CONTACT to "Contacto: RedPeer Alpha")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                options.take(3).forEach { (icon, label, pair) ->
                    AttachmentItem(icon = icon, label = label) {
                        onSelectAttachment(pair.first, pair.second)
                        onDismiss()
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                options.drop(3).forEach { (icon, label, pair) ->
                    AttachmentItem(icon = icon, label = label) {
                        onSelectAttachment(pair.first, pair.second)
                        onDismiss()
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AttachmentItem(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.AttachFile,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = CircleShape,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatTime(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}
