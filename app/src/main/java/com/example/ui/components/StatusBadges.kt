package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.StringKey
import com.example.localization.appString
import com.example.model.PlaybackStatus
import com.example.ui.theme.LiveCoral
import com.example.ui.theme.NavyMuted
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PlayingGreen
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.StatusConnecting
import com.example.ui.theme.StatusError

@Composable
fun LiveStatusBadge(
    status: PlaybackStatus,
    isAutoRetrying: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (isAutoRetrying) {
        Row(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .background(StatusConnecting.copy(alpha = 0.18f))
                .padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(StatusConnecting)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = appString(StringKey.PLAYER_STATUS_RECONNECTING),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = StatusConnecting,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            )
        }
        return
    }

    when (status) {
        PlaybackStatus.PLAYING -> {
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseAlpha"
            )

            Row(
                modifier = modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(PlayingGreen.copy(alpha = 0.25f))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(PlayingGreen)
                        .alpha(alpha)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = appString(StringKey.PLAYER_STATUS_PLAYING),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = NavyPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                )
            }
        }
        PlaybackStatus.CONNECTING, PlaybackStatus.BUFFERING -> {
            Row(
                modifier = modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(StatusConnecting.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(StatusConnecting)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = appString(StringKey.PLAYER_STATUS_BUFFERING),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = StatusConnecting,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                )
            }
        }
        PlaybackStatus.ERROR -> {
            Row(
                modifier = modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(StatusError.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(StatusError)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = appString(StringKey.PLAYER_STATUS_ERROR),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = StatusError,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                )
            }
        }
        PlaybackStatus.PAUSED -> {
            Row(
                modifier = modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(NavyMuted.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(NavyMuted)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = appString(StringKey.PLAYER_STATUS_PAUSED),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = NavyMuted,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                )
            }
        }
        else -> {
            Row(
                modifier = modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(LiveCoral.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(LiveCoral)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = appString(StringKey.HOME_LIVE_NOW),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = LiveCoral,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

@Composable
fun QualityBadge(
    bitrate: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SkyBlueLight)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = bitrate,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = NavyMuted,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

