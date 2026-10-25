package com.example.ui.screens.leaderboard

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.LeaderboardUser
import com.example.ui.components.SoftUITheme
import com.example.ui.components.VerifiedBadge
import com.example.ui.viewmodel.MainViewModel

/**
 * Leaderboard Screen
 * - No top header, no bottom navigation bar, no on-screen back button
 * - Clean title "Leaderboard" at the top
 * - Hardware / gesture back button returns to previous screen
 * - Top 3 Podium in Gold, Silver, Diamond/Bronze styling
 * - Ranked list from #4 onwards with Likes, Comments, Followers counts
 * - Dynamically synced with Admin Panel leaderboard_limit setting (default: 20)
 */
@Composable
fun LeaderboardScreen(
    viewModel: MainViewModel
) {
    // Back navigation handled by hardware/gesture back button
    BackHandler {
        viewModel.navigateBack()
    }

    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val leaderboardUsers by viewModel.leaderboardUsers.collectAsState()
    val leaderboardLimit by viewModel.leaderboardLimit.collectAsState()
    val isLoading by viewModel.isLeaderboardLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadLeaderboardSettings()
        viewModel.loadLeaderboard()
    }

    LaunchedEffect(leaderboardLimit) {
        viewModel.loadLeaderboard()
    }

    val bgGradient = if (isDarkMode) {
        Brush.verticalGradient(
            listOf(
                Color(0xFF0F1218),
                Color(0xFF141923),
                Color(0xFF181F2C)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color(0xFFF7F9FD),
                Color(0xFFEFF4FB),
                Color(0xFFE9F0FA)
            )
        )
    }

    val textColorPrimary = if (isDarkMode) Color.White else Color(0xFF111418)
    val textColorSecondary = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val cardBg = if (isDarkMode) Color(0xFF1F2430) else Color.White
    val cardBorder = if (isDarkMode) Color(0x33FFFFFF) else Color(0x22CBD5E1)

    val top1 = leaderboardUsers.getOrNull(0)
    val top2 = leaderboardUsers.getOrNull(1)
    val top3 = leaderboardUsers.getOrNull(2)
    val restOfUsers = if (leaderboardUsers.size > 3) leaderboardUsers.drop(3) else emptyList()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("leaderboard_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 14.dp,
                vertical = 12.dp
            )
        ) {
            // Top Clean Title: "Leaderboard" (No header, no back button)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFFFD700), Color(0xFFFFA000))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Trophy",
                                tint = Color(0xFF5D4037),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Leaderboard",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textColorPrimary,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (leaderboardUsers.isNotEmpty()) "Top ${leaderboardUsers.size} Most Active & Engaged Community Members" else "Top $leaderboardLimit Most Active & Engaged Community Members",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColorSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Top 3 Podium (Gold, Silver, Bronze/Diamond)
            if (top1 != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // 2nd Place: Silver (Left)
                            if (top2 != null) {
                                PodiumItem(
                                    user = top2,
                                    place = 2,
                                    crownEmoji = "🥈",
                                    badgeText = "2nd",
                                    ringColor = Color(0xFFC0C0C0),
                                    gradient = Brush.verticalGradient(
                                        listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8))
                                    ),
                                    avatarSize = 68.dp,
                                    pedestalHeight = 90.dp,
                                    isDarkMode = isDarkMode,
                                    onClick = { viewModel.openUserProfile(top2.id) }
                                )
                            }

                            // 1st Place: Gold / Diamond (Center - Elevated Higher)
                            PodiumItem(
                                user = top1,
                                place = 1,
                                crownEmoji = "👑",
                                badgeText = "1st",
                                ringColor = Color(0xFFFFD700),
                                gradient = Brush.verticalGradient(
                                    listOf(Color(0xFFFFE082), Color(0xFFFFA000))
                                ),
                                avatarSize = 82.dp,
                                pedestalHeight = 120.dp,
                                isDarkMode = isDarkMode,
                                isCenterFirst = true,
                                onClick = { viewModel.openUserProfile(top1.id) }
                            )

                            // 3rd Place: Bronze / Amber (Right)
                            if (top3 != null) {
                                PodiumItem(
                                    user = top3,
                                    place = 3,
                                    crownEmoji = "🥉",
                                    badgeText = "3rd",
                                    ringColor = Color(0xFFCD7F32),
                                    gradient = Brush.verticalGradient(
                                        listOf(Color(0xFFFFCC80), Color(0xFFD87A24))
                                    ),
                                    avatarSize = 68.dp,
                                    pedestalHeight = 74.dp,
                                    isDarkMode = isDarkMode,
                                    onClick = { viewModel.openUserProfile(top3.id) }
                                )
                            }
                        }
                    }
                }
            }

            // Section Header for Ranks 4+
            if (restOfUsers.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Rankings (#4 - #$leaderboardLimit)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = textColorPrimary
                        )

                        Text(
                            text = "Likes • Comments • Followers",
                            fontSize = 11.5.sp,
                            color = textColorSecondary
                        )
                    }
                }

                // Ranked List (#4 to N)
                itemsIndexed(restOfUsers) { index, user ->
                    val rankNum = index + 4
                    LeaderboardUserRow(
                        user = user,
                        rank = rankNum,
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        textColorPrimary = textColorPrimary,
                        textColorSecondary = textColorSecondary,
                        isDarkMode = isDarkMode,
                        onClick = { viewModel.openUserProfile(user.id) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Loading Indicator
            if (isLoading && leaderboardUsers.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF0075FF),
                            strokeWidth = 3.dp
                        )
                    }
                }
            } else if (!isLoading && leaderboardUsers.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No users ranked on leaderboard yet.",
                            color = textColorSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Top 3 Podium Item (Gold, Silver, Bronze)
 */
@Composable
private fun PodiumItem(
    user: LeaderboardUser,
    place: Int,
    crownEmoji: String,
    badgeText: String,
    ringColor: Color,
    gradient: Brush,
    avatarSize: androidx.compose.ui.unit.Dp,
    pedestalHeight: androidx.compose.ui.unit.Dp,
    isDarkMode: Boolean,
    isCenterFirst: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(if (isCenterFirst) 118.dp else 100.dp)
            .clickable { onClick() }
    ) {
        // Crown icon on top
        Text(
            text = crownEmoji,
            fontSize = if (isCenterFirst) 28.sp else 22.sp
        )

        // Avatar with Ring & Rank Badge
        Box(
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .size(avatarSize)
                    .shadow(
                        elevation = if (isCenterFirst) 10.dp else 5.dp,
                        shape = CircleShape,
                        spotColor = ringColor
                    )
                    .clip(CircleShape)
                    .border(if (isCenterFirst) 3.5.dp else 2.5.dp, ringColor, CircleShape)
            ) {
                if (!user.avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = user.avatarUrl,
                        placeholder = painterResource(id = R.drawable.no_image),
                        error = painterResource(id = R.drawable.no_image),
                        contentDescription = user.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.no_image),
                        contentDescription = user.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Rank Badge Pill (1st, 2nd, 3rd)
            Box(
                modifier = Modifier
                    .offset(y = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(gradient)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A1A1A)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // User Name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = user.name.split(" ").firstOrNull() ?: user.name,
                fontWeight = FontWeight.Bold,
                fontSize = if (isCenterFirst) 14.5.sp else 13.sp,
                color = if (isDarkMode) Color.White else Color(0xFF111418),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (user.isVerified) {
                Spacer(modifier = Modifier.width(3.dp))
                VerifiedBadge(modifier = Modifier.size(13.dp))
            }
        }

        // Stats summary chips
        Text(
            text = "${formatCompactNumber(user.totalLikes)} likes",
            fontSize = 11.sp,
            color = Color(0xFFE91E63),
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "${formatCompactNumber(user.totalFollowers)} fans",
            fontSize = 10.5.sp,
            color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Pedestal base
        Box(
            modifier = Modifier
                .width(if (isCenterFirst) 96.dp else 84.dp)
                .height(pedestalHeight)
                .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                .background(
                    if (isDarkMode) {
                        if (isCenterFirst) Color(0xFF2A281E) else Color(0xFF1E222B)
                    } else {
                        if (isCenterFirst) Color(0xFFFFF8E1) else Color(0xFFF1F5F9)
                    }
                )
                .border(
                    width = 1.dp,
                    color = ringColor.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "#$place",
                    fontSize = if (isCenterFirst) 22.sp else 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ringColor
                )
                Text(
                    text = "Score ${formatCompactNumber(user.engagementScore)}",
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
                )
            }
        }
    }
}

/**
 * Ranked list row for users #4 to #N
 */
@Composable
private fun LeaderboardUserRow(
    user: LeaderboardUser,
    rank: Int,
    cardBg: Color,
    cardBorder: Color,
    textColorPrimary: Color,
    textColorSecondary: Color,
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Rank Number + Avatar + Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Rank number badge
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isDarkMode) Color(0xFF2C3240) else Color(0xFFF0F4F8)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#$rank",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColorPrimary
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box(modifier = Modifier.size(42.dp)) {
                    if (!user.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = user.avatarUrl,
                            placeholder = painterResource(id = R.drawable.no_image),
                            error = painterResource(id = R.drawable.no_image),
                            contentDescription = user.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.no_image),
                            contentDescription = user.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // User Name + Username/Category
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = textColorPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (user.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            VerifiedBadge(modifier = Modifier.size(13.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // 3 Metric Counters: Likes, Comments, Followers
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Likes
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Likes",
                                tint = Color(0xFFE91E63),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.5.dp))
                            Text(
                                text = formatCompactNumber(user.totalLikes),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textColorPrimary
                            )
                        }

                        // Comments
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ChatBubble,
                                contentDescription = "Comments",
                                tint = Color(0xFF0075FF),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.5.dp))
                            Text(
                                text = formatCompactNumber(user.totalComments),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textColorPrimary
                            )
                        }

                        // Followers
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = "Followers",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.5.dp))
                            Text(
                                text = formatCompactNumber(user.totalFollowers),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textColorPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Score Pill on the right
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDarkMode) Color(0xFF2A2E3D) else Color(0xFFEEF2FF))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = "Score",
                        tint = Color(0xFF4F46E5),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = formatCompactNumber(user.engagementScore),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4F46E5)
                    )
                }
            }
        }
    }
}

/**
 * Compact number formatting helper (e.g. 48.2K, 1.2M)
 */
private fun formatCompactNumber(num: Int): String {
    return when {
        num >= 1_000_000 -> String.format(java.util.Locale.US, "%.1fM", num / 1_000_000.0)
        num >= 1_000 -> String.format(java.util.Locale.US, "%.1fK", num / 1_000.0)
        else -> num.toString()
    }
}
