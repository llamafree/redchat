package com.llama.redchat.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CrimsonPrimary
import com.llama.redchat.viewmodel.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    viewModel: MainViewModel,
    onSplashFinished: () -> Unit
) {
    val bleText = stringResource(R.string.init_ble)
    val torText = stringResource(R.string.init_tor)
    val dbText = stringResource(R.string.init_db)
    val cryptoText = stringResource(R.string.init_crypto)
    val completeText = stringResource(R.string.init_complete)

    var stepText by remember { mutableStateOf(bleText) }
    var progress by remember { mutableFloatStateOf(0.1f) }

    LaunchedEffect(Unit) {
        stepText = bleText
        progress = 0.25f
        delay(300)

        stepText = torText
        progress = 0.50f
        delay(300)

        stepText = dbText
        progress = 0.75f
        delay(300)

        stepText = cryptoText
        progress = 0.95f
        delay(300)

        stepText = completeText
        progress = 1.0f
        delay(250)

        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                color = CrimsonPrimary.copy(alpha = 0.15f),
                shape = CircleShape,
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(R.drawable.redchat_icon_1785017789097),
                        contentDescription = "REDChat Logo",
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.splash_title),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = stringResource(R.string.splash_subtitle),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(6.dp)
                    .clip(CircleShape),
                color = CrimsonPrimary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(
                targetState = stepText,
                label = "stepTextAnimation"
            ) { text ->
                Text(
                    text = text,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = CrimsonPrimary
                )
            }
        }
    }
}
