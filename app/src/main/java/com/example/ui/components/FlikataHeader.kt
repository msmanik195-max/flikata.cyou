package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * 3D Soft Neumorphic Header with subtle, refined elevation (no harsh black shadow line)
 * and seamless support for both Light and Dark modes.
 */
@Composable
fun FlikataHeader(
    unreadNotificationsCount: Int = 0,
    onSearchClick: () -> Unit = {},
    onFriendsClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    val isDark = SoftUITheme.isDark

    val headerBgBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF242730),
                Color(0xFF1E2128),
                Color(0xFF181A20)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFF9FBFE),
                Color(0xFFF2F6FC)
            )
        )
    }

    val headerBorderBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x33FFFFFF),
                Color(0x11FFFFFF)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xEEFFFFFF),
                Color(0x1A000000)
            )
        )
    }

    // Clean, subtle elevation to completely remove the harsh dark black shadow
    val shadowElevation = if (isDark) 2.dp else 1.dp
    val shadowAmbient = if (isDark) Color(0x33000000) else Color(0x08000000)
    val shadowSpot = if (isDark) Color(0x44000000) else Color(0x0C000000)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = shadowElevation,
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                ambientColor = shadowAmbient,
                spotColor = shadowSpot
            )
            .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            .background(headerBgBrush)
            .border(
                width = 1.dp,
                brush = headerBorderBrush,
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: 3D Embossed Logo Badge & Gradient Brand Title
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 3D Neumorphic Logo Badge
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(13.dp),
                            ambientColor = if (isDark) Color(0x33000000) else Color(0x10000000),
                            spotColor = if (isDark) Color(0x44000000) else Color(0x14000000)
                        )
                        .clip(RoundedCornerShape(13.dp))
                        .background(
                            if (isDark) {
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF2C303B), Color(0xFF20232B)),
                                    start = Offset(0f, 0f),
                                    end = Offset(80f, 80f)
                                )
                            } else {
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFE9F1FD)),
                                    start = Offset(0f, 0f),
                                    end = Offset(80f, 80f)
                                )
                            }
                        )
                        .border(
                            width = 1.2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    if (isDark) Color(0x33FFFFFF) else Color(0xEEFFFFFF),
                                    if (isDark) Color(0x11FFFFFF) else Color(0x44A0C0E8)
                                )
                            ),
                            shape = RoundedCornerShape(13.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "Flikata Logo",
                        modifier = Modifier.size(26.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Brand Title with 3D Glossy Text Look
                Box {
                    Text(
                        text = "Flikata",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        style = TextStyle(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF0075FF),
                                    if (isDark) Color(0xFF3897F0) else Color(0xFF0052D4)
                                )
                            )
                        )
                    )
                }
            }

            // Right: 3D Soft Clay Circular Action Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Search
                Header3DIconButton(
                    drawableRes = R.drawable.ic_custom_search,
                    contentDescription = "Search",
                    iconSize = 19.dp,
                    onClick = onSearchClick
                )

                // 2. Notification (with 3D badge if unread)
                Box {
                    Header3DIconButton(
                        drawableRes = R.drawable.ic_custom_notification,
                        contentDescription = "Notifications",
                        iconSize = 19.dp,
                        onClick = onNotificationsClick
                    )

                    if (unreadNotificationsCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-2).dp)
                                .size(18.dp)
                                .shadow(2.dp, CircleShape)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFFFF453A), Color(0xFFD70015))
                                    )
                                )
                                .border(1.5.dp, if (isDark) Color(0xFF242730) else Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (unreadNotificationsCount > 99) "99+" else unreadNotificationsCount.toString(),
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 4. Menu
                Header3DIconButton(
                    drawableRes = R.drawable.ic_custom_menu,
                    contentDescription = "Menu",
                    iconSize = 19.dp,
                    onClick = onMenuClick
                )
            }
        }
    }
}

/**
 * 3D Soft Neumorphic Circular Icon Button with dual lighting & top specular gloss
 */
@Composable
private fun Header3DIconButton(
    drawableRes: Int,
    contentDescription: String,
    iconSize: Dp = 19.dp,
    onClick: () -> Unit
) {
    val isDark = SoftUITheme.isDark

    Box(
        modifier = Modifier
            .size(38.dp)
            .shadow(
                elevation = 2.dp,
                shape = CircleShape,
                ambientColor = if (isDark) Color(0x33000000) else Color(0x10000000),
                spotColor = if (isDark) Color(0x44000000) else Color(0x14000000)
            )
            .clip(CircleShape)
            .background(
                if (isDark) {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF2C303B),
                            Color(0xFF20232B)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(80f, 80f)
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFE8F1FC)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(80f, 80f)
                    )
                }
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        if (isDark) Color(0x33FFFFFF) else Color(0xEEFFFFFF),
                        if (isDark) Color(0x11FFFFFF) else Color(0x33A0C0E8)
                    )
                ),
                shape = CircleShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Specular top highlight arc for 3D sphere look
        Canvas(
            modifier = Modifier
                .size(38.dp)
                .align(Alignment.TopCenter)
        ) {
            drawArc(
                brush = Brush.verticalGradient(
                    listOf(
                        if (isDark) Color(0x22FFFFFF) else Color(0x55FFFFFF),
                        Color(0x00FFFFFF)
                    )
                ),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.55f)
            )
        }

        Icon(
            painter = painterResource(id = drawableRes),
            contentDescription = contentDescription,
            tint = if (isDark) Color(0xFFE4E6EB) else Color(0xFF1E3A5F),
            modifier = Modifier.size(iconSize)
        )
    }
}
