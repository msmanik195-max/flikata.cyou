package com.example.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.delay

/**
 * 3D Gradient Splash Screen
 * Features:
 * - Rich 3D gradient background with ambient depth lighting
 * - Floating 3D elevated logo card with glassmorphism border and breathing scale
 * - Much smaller, elegant "Feel free with Flikata" typewriter tagline (12.5sp)
 * - 3D pulsing dots and polished bottom branding
 */
@Composable
fun SplashScreen(
    viewModel: MainViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isUpdateAvailable by viewModel.isUpdateAvailable.collectAsState()
    val isUpdateMandatory by viewModel.isUpdateMandatory.collectAsState()

    val fullText = "Feel free with Flikata"
    var visibleTextLength by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.checkForAppUpdates()
        for (i in 1..fullText.length) {
            visibleTextLength = i
            delay(65) // smooth typewriter typing speed
        }
        delay(1000) // Hold briefly after typing

        // If update check is currently active over network, wait briefly for it to complete
        var checkWaitCount = 0
        while (viewModel.isCheckingUpdate.value && checkWaitCount < 10) {
            delay(200)
            checkWaitCount++
        }

        // If a mandatory update is required, halt navigation completely and lock splash screen
        if (viewModel.isUpdateAvailable.value && viewModel.isUpdateMandatory.value) {
            return@LaunchedEffect
        }

        if (currentUser != null) {
            viewModel.navigateTo(Screen.HOME)
        } else {
            viewModel.navigateTo(Screen.LOGIN)
        }
    }

    // 3D Breathing scale animation for Logo Card
    val infiniteTransition = rememberInfiniteTransition(label = "3d_float")
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )

    val ambientGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambient_glow"
    )

    // 3 Bouncing dots animation
    val dot1Offset = remember { Animatable(0f) }
    val dot2Offset = remember { Animatable(0f) }
    val dot3Offset = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        val animationSpec = infiniteRepeatable<Float>(
            animation = tween(durationMillis = 380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
        dot1Offset.animateTo(-10f, animationSpec)
    }
    LaunchedEffect(Unit) {
        delay(110)
        val animationSpec = infiniteRepeatable<Float>(
            animation = tween(durationMillis = 380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
        dot2Offset.animateTo(-10f, animationSpec)
    }
    LaunchedEffect(Unit) {
        delay(220)
        val animationSpec = infiniteRepeatable<Float>(
            animation = tween(durationMillis = 380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
        dot3Offset.animateTo(-10f, animationSpec)
    }

    // Modern 3D multi-stop deep gradient background
    val splashGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF060B18), // Deep Midnight Top
            Color(0xFF09183B), // Royal Indigo
            Color(0xFF0D2866), // Vibrant Deep Sapphire
            Color(0xFF081534), // Rich Navy
            Color(0xFF040813)  // Deep Midnight Bottom
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(splashGradient)
    ) {
        // Status bar background area: Solid Black as requested
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsTopHeight(WindowInsets.statusBars)
                .background(Color.Black)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
        // Decorative 3D Ambient Glowing Orbs in the background
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(y = (-40).dp)
                .scale(logoScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF0075FF).copy(alpha = ambientGlowAlpha * 0.45f),
                            Color(0xFF00C853).copy(alpha = ambientGlowAlpha * 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Center Section: 3D Elevated Logo Card + Small Elegant Typewriter Tagline
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 3D Glassmorphic Elevated Squircle Card containing the new App Logo
            Box(
                modifier = Modifier
                    .scale(logoScale)
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = Color(0xFF0075FF),
                        spotColor = Color(0xFF00E5FF)
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFFFFFFFF).copy(alpha = 0.95f),
                                Color(0xFFF0F6FF).copy(alpha = 0.90f)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                Color.White,
                                Color(0x66FFFFFF),
                                Color(0x660075FF)
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "Flikata Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(92.dp)
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            // App Brand Name with subtle 3D glowing gradient effect
            Text(
                text = "FLIKATA",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline "Feel free with Flikata" - significantly reduced size (12.5sp)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF14203A).copy(alpha = 0.65f))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            ) {
                Text(
                    text = fullText.take(visibleTextLength),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp,
                    color = Color(0xFFE2E8F0)
                )
            }
        }

        // Bottom Section: 3D Glowing Bouncing Dots + "from Flikata"
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 44.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .offset(y = dot1Offset.value.dp)
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E5FF))
                        .shadow(6.dp, CircleShape, spotColor = Color(0xFF00E5FF))
                )
                Box(
                    modifier = Modifier
                        .offset(y = dot2Offset.value.dp)
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0075FF))
                        .shadow(6.dp, CircleShape, spotColor = Color(0xFF0075FF))
                )
                Box(
                    modifier = Modifier
                        .offset(y = dot3Offset.value.dp)
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF60A5FA))
                        .shadow(6.dp, CircleShape, spotColor = Color(0xFF60A5FA))
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "from Flikata",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                color = Color(0xFF94A3B8)
            )
        }

        // Full-screen 3D Glowing Update Modal Overlay when update is detected
        if (isUpdateAvailable) {
            com.example.ui.components.AppUpdateView(
                viewModel = viewModel,
                onDismissOptional = {
                    if (currentUser != null) {
                        viewModel.navigateTo(Screen.HOME)
                    } else {
                        viewModel.navigateTo(Screen.LOGIN)
                    }
                }
            )
        }
    }
}
}

