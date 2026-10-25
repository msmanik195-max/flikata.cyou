package com.example.ui.screens.chats

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.audio.AudioCallManager
import com.example.data.model.IncomingCallData
import com.example.ui.components.FlikataAvatar
import com.example.ui.components.VerifiedBadge
import com.example.ui.viewmodel.ActiveCallState
import java.util.Locale

/**
 * Incoming Call Fullscreen Overlay
 */
@Composable
fun IncomingCallOverlay(
    call: IncomingCallData,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    val context = LocalContext.current
    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onAccept()
        } else {
            Toast.makeText(context, "Microphone permission required to receive call", Toast.LENGTH_SHORT).show()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xF00A1628),
                        Color(0xF00D2040),
                        Color(0xF0070E1A)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Text(
                    text = "Incoming Audio Call",
                    fontSize = 15.sp,
                    color = Color(0xFF64B5F6),
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(30.dp))

                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(Color(0x330075FF))
                    )
                    Box(
                        modifier = Modifier
                            .size(136.dp)
                            .clip(CircleShape)
                            .background(Color(0x44FFFFFF))
                    )
                    Box(
                        modifier = Modifier
                            .size(116.dp)
                            .shadow(12.dp, CircleShape, ambientColor = Color(0x660075FF))
                            .clip(CircleShape)
                            .border(3.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        FlikataAvatar(
                            name = call.callerName ?: "User",
                            avatarUrl = call.callerAvatar,
                            size = 116.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = call.callerName ?: "Flikata User",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (call.callerVerified == true) {
                        Spacer(modifier = Modifier.width(7.dp))
                        VerifiedBadge(size = 25.dp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x2AFFFFFF))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Ringing...",
                        fontSize = 13.5.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Bottom Actions (Decline vs Accept)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 36.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Decline (Red Button)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .shadow(8.dp, CircleShape, ambientColor = Color(0x66FF3B30))
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFFFF453A), Color(0xFFD32F2F))
                                )
                            )
                            .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                            .clickable { onDecline() }
                            .testTag("decline_call_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "Decline",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Decline",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                // Accept (Green Button)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .shadow(8.dp, CircleShape, ambientColor = Color(0x6634C759))
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF34C759), Color(0xFF28A745))
                                )
                            )
                            .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                            .clickable {
                                val hasMic = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasMic) {
                                    onAccept()
                                } else {
                                    micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                            .testTag("accept_call_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Accept",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Accept",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Active Audio Call Overlay with live waveform and controls
 */
@Composable
fun ActiveAudioCallOverlay(
    call: ActiveCallState,
    audioCallManager: AudioCallManager,
    onEndCall: () -> Unit
) {
    val isMuted by audioCallManager.isMuted.collectAsState()
    val isSpeakerOn by audioCallManager.isSpeakerOn.collectAsState()
    val micAmp by audioCallManager.micAmplitude.collectAsState()

    val mins = call.durationSeconds / 60
    val secs = call.durationSeconds % 60
    val callDurationString = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0B1B3D),
                        Color(0xFF102855),
                        Color(0xFF0A1628)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .scale(if (call.status == "connected") 1.0f + micAmp * 0.4f else pulseScale)
                            .clip(CircleShape)
                            .background(Color(0x220075FF))
                    )
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFFFFF))
                    )
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .shadow(10.dp, CircleShape, ambientColor = Color(0x660075FF))
                            .clip(CircleShape)
                            .border(3.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        FlikataAvatar(
                            name = call.partnerName,
                            avatarUrl = call.partnerAvatar,
                            size = 110.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = call.partnerName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (call.isVerified) {
                        Spacer(modifier = Modifier.width(7.dp))
                        VerifiedBadge(size = 25.dp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x33FFFFFF))
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (call.status == "connected") "In Call • $callDurationString" else "Calling...",
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Dynamic live sound waveform visualizer
                if (call.status == "connected") {
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val heights = listOf(14.dp, 22.dp, 32.dp, 20.dp, 12.dp)
                        heights.forEachIndexed { index, baseHeight ->
                            val dynamicHeight = (baseHeight.value * (0.4f + micAmp * 1.5f)).coerceIn(6f, 40f).dp
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(dynamicHeight)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (isMuted) Color.Gray else Color(0xFF0075FF))
                            )
                        }
                    }
                }
            }

            // Bottom Section: Call Controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 36.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute Mic Button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .shadow(4.dp, CircleShape)
                                .clip(CircleShape)
                                .background(if (isMuted) Color.White else Color(0x33FFFFFF))
                                .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                            .clickable { audioCallManager.toggleMute() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Mute",
                                tint = if (isMuted) Color(0xFF1E3A5F) else Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isMuted) "Unmute" else "Mute",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }

                    // End Call (Large Red Button)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .shadow(12.dp, CircleShape, ambientColor = Color(0x88FF3B30))
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFFFF453A), Color(0xFFD32F2F))
                                    )
                                )
                                .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                .clickable { onEndCall() }
                                .testTag("end_audio_call_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = "End Call",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "End Call",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    // Speaker Button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .shadow(4.dp, CircleShape)
                                .clip(CircleShape)
                                .background(if (isSpeakerOn) Color.White else Color(0x33FFFFFF))
                                .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                                .clickable { audioCallManager.toggleSpeaker() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                                contentDescription = "Speaker",
                                tint = if (isSpeakerOn) Color(0xFF1E3A5F) else Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isSpeakerOn) "Speaker" else "Earpiece",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
