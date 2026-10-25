package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.MainViewModel

/**
 * 3D Glowing Colorful In-App Software Update Dialog / Screen
 * Features:
 * - Animated multi-layer neon gradient aura (Flikata Blue, Electric Violet, Emerald)
 * - 3D Neumorphic elevated Card with glassmorphism border
 * - Live in-app download progress (0% - 100%, MBs stream)
 * - Auto-triggers Android Package Installer on finish (No external browser)
 */
@Composable
fun AppUpdateView(
    viewModel: MainViewModel,
    onDismissOptional: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val updateInfo by viewModel.appUpdateInfo.collectAsState()
    val isDownloading by viewModel.isDownloadingUpdate.collectAsState()
    val progress by viewModel.updateDownloadProgress.collectAsState()
    val statusText by viewModel.updateDownloadStatus.collectAsState()
    val downloadedFile by viewModel.downloadedApkFile.collectAsState()
    val errorMessage by viewModel.updateErrorMessage.collectAsState()

    val currentVersionName = com.example.BuildConfig.VERSION_NAME
    val newVersionName = updateInfo?.latestVersionName?.ifBlank { "New" } ?: "New"
    val isForce = updateInfo?.isForce ?: true
    val notes = updateInfo?.releaseNotes?.ifBlank {
        "• Performance upgrades & smoother transitions\n• Enhanced live video & reels views counting\n• Persistent user profile settings & security fixes"
    } ?: "General improvements & bug fixes."

    // 3D Breathing Glow Animation
    val infiniteTransition = rememberInfiniteTransition(label = "update_glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradient_offset"
    )

    val cardBorderBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF00C6FF),
            Color(0xFF0072FF),
            Color(0xFF8E2DE2),
            Color(0xFFFF007F),
            Color(0xFF00C6FF)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xEE0A0E1A))
            .padding(horizontal = 20.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer 3D Ambient Glow Sphere
        Box(
            modifier = Modifier
                .size(340.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x330075FF),
                            Color(0x228E2DE2),
                            Color.Transparent
                        )
                    )
                )
        )

        // Main 3D Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = Color(0x660075FF),
                    spotColor = Color(0x998E2DE2)
                )
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF161B26),
                            Color(0xFF10141D),
                            Color(0xFF0C0F17)
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    brush = cardBorderBrush,
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top 3D Animated Rocket Emblem
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(pulseScale)
                        .shadow(12.dp, CircleShape, spotColor = Color(0xFF0075FF))
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF0075FF),
                                    Color(0xFF8E2DE2),
                                    Color(0xFFFF007F)
                                )
                            )
                        )
                        .border(2.dp, Color(0x88FFFFFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (downloadedFile != null) Icons.Default.CheckCircle else Icons.Default.RocketLaunch,
                        contentDescription = "Update Rocket",
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = if (downloadedFile != null) "Ready to Install! 🚀" else "New Update Available! ⚡",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isForce) "Mandatory update required to continue using Flikata" else "Upgrade now for the best experience",
                    fontSize = 12.5.sp,
                    color = if (isForce) Color(0xFFFF7B7B) else Color(0xFF94A3B8),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Version Comparison Pill Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E2536))
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Current Version", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text(text = "v$currentVersionName", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFCBD5E1))
                        }

                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = "Arrow",
                            tint = Color(0xFF00C6FF),
                            modifier = Modifier.size(20.dp)
                        )

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "New Version", fontSize = 11.sp, color = Color(0xFF00E5FF))
                            Text(text = "v$newVersionName", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF00E5FF))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Release Notes Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF181E2C))
                        .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "What's New in this Release:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = notes,
                            fontSize = 12.sp,
                            color = Color(0xFFE2E8F0),
                            lineHeight = 18.sp
                        )
                    }
                }

                // Download Progress / Status Section
                if (isDownloading || downloadedFile != null) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (downloadedFile != null) "Download Finished" else "Downloading Update...",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = "$progress%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { progress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF00E5FF),
                            trackColor = Color(0xFF263248),
                            strokeCap = StrokeCap.Round
                        )

                        if (statusText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = statusText,
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage ?: "",
                        fontSize = 11.5.sp,
                        color = Color(0xFFFF5252),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Primary 3D Action Button: Update Now / Installing / Install Now
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = Color(0x660075FF),
                            spotColor = Color(0xAA8E2DE2)
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (downloadedFile != null) {
                                Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
                            } else {
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF0075FF),
                                        Color(0xFF6366F1),
                                        Color(0xFF8B5CF6)
                                    )
                                )
                            }
                        )
                        .border(1.dp, Color(0x88FFFFFF), RoundedCornerShape(16.dp))
                        .clickable(enabled = !isDownloading) {
                            if (downloadedFile != null) {
                                viewModel.launchApkInstaller(context, downloadedFile)
                            } else {
                                viewModel.startInAppUpdateDownload(context)
                            }
                        }
                        .testTag("app_update_primary_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isDownloading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Downloading Update...",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else if (downloadedFile != null) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Install Update (ইনস্টল করুন)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Update Now (এখনই আপডেট করুন)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Optional Cancel/Later Button only if NOT force update
                if (!isForce && !isDownloading) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Remind Me Later (পরে আপডেট করব)",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier
                            .clickable { onDismissOptional?.invoke() }
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}
