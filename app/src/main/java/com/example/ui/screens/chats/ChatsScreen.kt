package com.example.ui.screens.chats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ChatMessage
import com.example.ui.components.FlikataAvatar
import com.example.ui.components.SoftUITheme
import com.example.ui.theme.FlikataError
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.FlikataPrimary
import com.example.ui.viewmodel.MainViewModel

data class ActiveUser(
    val id: String,
    val name: String,
    val avatar: String,
    val isVerified: Boolean = false
)

/**
 * 3D Soft Neumorphic Chats Screen with tactile cards, active stories row, and verified avatars
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    viewModel: MainViewModel,
    onMenuClick: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val chats by viewModel.chats.collectAsState()
    val allPosts by viewModel.posts.collectAsState()
    val isRefreshingChats by viewModel.isRefreshingChats.collectAsState()
    val featureSettings by viewModel.featureSettings.collectAsState()
    val activeUsers = remember(chats) {
        chats.filter { it.isOnline }.map {
            ActiveUser(
                id = it.senderId,
                name = it.senderName,
                avatar = it.senderAvatar,
                isVerified = it.isVerified
            )
        }.distinctBy { if (it.id.isNotBlank()) it.id else it.name }
    }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(currentUser) {
        val uid = currentUser?.id
        if (!uid.isNullOrBlank()) {
            while (true) {
                viewModel.repository.syncChatsFromServer(uid)
                kotlinx.coroutines.delay(2500)
            }
        }
    }

    val filteredChats = remember(chats, searchQuery) {
        if (searchQuery.isBlank()) {
            chats
        } else {
            chats.filter {
                it.senderName.contains(searchQuery, ignoreCase = true) ||
                        it.message.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SoftUITheme.BackgroundGradientTop,
                        SoftUITheme.BackgroundGradientBottom
                    )
                )
            )
            .statusBarsPadding()
    ) {
        // 3D Neumorphic Top Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Title in 3D royal blue gradient
                Text(
                    text = "Chats",
                    style = TextStyle(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF0075FF), Color(0xFF0052D4))
                        ),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.2.sp
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 3D Online Count Badge
                    Box(
                        modifier = Modifier
                            .shadow(2.dp, RoundedCornerShape(14.dp), ambientColor = SoftUITheme.ShadowDark)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0x33A0C0E8), RoundedCornerShape(14.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF45BD62))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${activeUsers.size.coerceAtLeast(1)} Active",
                                color = Color(0xFF0075FF),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // 3D Menu Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(3.dp, CircleShape, ambientColor = SoftUITheme.ShadowDark)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color.White, Color(0xFFE8F1FC))
                                )
                            )
                            .border(1.dp, Color(0xEEFFFFFF), CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onMenuClick
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_custom_menu),
                            contentDescription = "Menu",
                            tint = Color(0xFF1E3A5F),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }

        // 3D Neumorphic Search Bar
        val isDark = SoftUITheme.isDark
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .shadow(
                    elevation = if (isDark) 2.dp else 1.dp,
                    shape = RoundedCornerShape(22.dp),
                    ambientColor = SoftUITheme.ShadowDark
                )
                .clip(RoundedCornerShape(22.dp))
                .background(if (isDark) Color(0xFF1E2128) else Color.White)
                .border(
                    width = 1.dp,
                    brush = if (isDark) Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x11FFFFFF)))
                    else Brush.verticalGradient(listOf(Color(0xEEFFFFFF), Color(0x44A0C0E8))),
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = SoftUITheme.IconTint,
                    modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.width(9.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(
                        color = SoftUITheme.TextDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    cursorBrush = SolidColor(Color(0xFF0075FF)),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search conversations...",
                                color = SoftUITheme.TextMuted,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                )
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { searchQuery = "" },
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = SoftUITheme.IconTint,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Chat List & Active users in LazyColumn with Pull-to-Refresh
        PullToRefreshBox(
            isRefreshing = isRefreshingChats,
            onRefresh = { viewModel.refreshChats() },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
            if (!featureSettings.allowChat) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .shadow(2.dp, RoundedCornerShape(14.dp))
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFFF1F2))
                            .border(1.dp, Color(0xFFFECDD3), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "বিজ্ঞপ্তি: চ্যাট অপশন সাময়িকভাবে বন্ধ আছে",
                            color = Color(0xFFBE123C),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Active Users Row
            if (activeUsers.isNotEmpty() && searchQuery.isBlank()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                        Text(
                            text = "Active Now",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftUITheme.TextDark,
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(activeUsers, key = { it.id }) { user ->
                                ActiveUser3DItem(
                                    user = user,
                                    onClick = {
                                        if (!featureSettings.allowChat) {
                                            viewModel.featureNoticeDialog.value = "চ্যাট অপশন সাময়িকভাবে বন্ধ আছে"
                                            return@ActiveUser3DItem
                                        }
                                        viewModel.openChatWithUser(
                                            name = user.name,
                                            avatar = user.avatar,
                                            isVerified = user.isVerified,
                                            userId = user.id
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Section Header
            item {
                Text(
                    text = "Recent Conversations",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftUITheme.TextDark,
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp, start = 2.dp)
                )
            }

            // Chat Threads
            if (filteredChats.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp)
                            .shadow(3.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0x33A0C0E8), RoundedCornerShape(16.dp))
                            .padding(24.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (searchQuery.isBlank()) "No conversation history yet" else "No existing chat for \"$searchQuery\"",
                                color = SoftUITheme.TextSecondary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (searchQuery.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .shadow(3.dp, RoundedCornerShape(14.dp), ambientColor = Color(0x660075FF))
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Brush.verticalGradient(listOf(Color(0xFF0075FF), Color(0xFF0052D4))))
                                        .clickable {
                                            viewModel.openChatWithUser(
                                                name = searchQuery,
                                                avatar = "",
                                                userId = searchQuery.removePrefix("user_").trim().toIntOrNull()?.toString()
                                            )
                                        }
                                        .padding(horizontal = 16.dp, vertical = 9.dp)
                                ) {
                                    Text(
                                        text = "Message \"$searchQuery\"",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                items(filteredChats, key = { it.id }) { chat ->
                    Chat3DItemRow(
                        chat = chat,
                        onClick = {
                            if (!featureSettings.allowChat) {
                                viewModel.featureNoticeDialog.value = "চ্যাট অপশন সাময়িকভাবে বন্ধ আছে"
                            } else {
                                viewModel.openChatDetail(chat)
                            }
                        }
                    )
                }
            }
        }
    }
}
}

/**
 * 3D Neumorphic Active User Avatar
 */
@Composable
private fun ActiveUser3DItem(
    user: ActiveUser,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(62.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Box {
            // 3D Circular Avatar Container
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = CircleShape,
                        ambientColor = SoftUITheme.ShadowDark
                    )
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            listOf(Color.White, Color(0x660075FF))
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                FlikataAvatar(
                    name = user.name,
                    avatarUrl = user.avatar,
                    size = 50.dp
                )
            }

            // Green Online Dot
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF45BD62))
                    .border(2.dp, Color.White, CircleShape)
                    .align(Alignment.BottomEnd)
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = user.name.split(" ").firstOrNull() ?: user.name,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = SoftUITheme.TextDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (user.isVerified) {
                Spacer(modifier = Modifier.width(3.dp))
                VerifiedBadge(size = 14.dp)
            }
        }
    }
}

/**
 * 3D Soft Neumorphic Conversation Row Card
 */
@Composable
private fun Chat3DItemRow(
    chat: ChatMessage,
    onClick: () -> Unit
) {
    val isDark = SoftUITheme.isDark
    val rowBg = if (isDark) {
        Brush.linearGradient(
            colors = listOf(Color(0xFF242730), Color(0xFF1E2128)),
            start = Offset(0f, 0f),
            end = Offset(250f, 250f)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color(0xFFFFFFFF), Color(0xFFF3F7FD)),
            start = Offset(0f, 0f),
            end = Offset(250f, 250f)
        )
    }

    val rowBorder = if (isDark) {
        Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x11FFFFFF)))
    } else {
        Brush.verticalGradient(listOf(Color(0xEEFFFFFF), Color(0x33A0C0E8)))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDark) 2.dp else 2.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = SoftUITheme.ShadowDark,
                spotColor = SoftUITheme.ShadowDark
            )
            .clip(RoundedCornerShape(18.dp))
            .background(rowBg)
            .border(
                width = 1.dp,
                brush = rowBorder,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("chat_item_${chat.id}"),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Partner Avatar with 3D Ring
            Box {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .shadow(2.dp, CircleShape, ambientColor = SoftUITheme.ShadowDark)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF1E2128) else Color.White)
                        .border(1.5.dp, if (isDark) Color(0x33FFFFFF) else Color(0x99FFFFFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    FlikataAvatar(
                        name = chat.senderName,
                        avatarUrl = chat.senderAvatar,
                        size = 50.dp
                    )
                }

                // Active dot (only when online)
                if (chat.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(13.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF45BD62))
                            .border(2.dp, Color.White, CircleShape)
                            .align(Alignment.BottomEnd)
                    )
                }
            }

            Spacer(modifier = Modifier.width(13.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Name & Timestamp Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(
                            text = chat.senderName,
                            fontWeight = if (chat.unreadCount > 0) FontWeight.ExtraBold else FontWeight.Bold,
                            fontSize = 15.5.sp,
                            color = SoftUITheme.TextDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (chat.isVerified) {
                            Spacer(modifier = Modifier.width(5.dp))
                            VerifiedBadge(size = 20.dp)
                        }
                        if (chat.isOnline) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF45BD62))
                            )
                        }
                    }

                    Text(
                        text = chat.timestamp,
                        fontSize = 11.5.sp,
                        fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (chat.unreadCount > 0) Color(0xFF0075FF) else SoftUITheme.TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Last Message Preview & Unread Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (chat.isVoiceMessage) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Message",
                            tint = Color(0xFF0075FF),
                            modifier = Modifier
                                .size(15.dp)
                                .padding(end = 4.dp)
                        )
                    } else if (chat.isMissedCall) {
                        Icon(
                            imageVector = Icons.Default.PhoneCallback,
                            contentDescription = "Missed Call",
                            tint = FlikataError,
                            modifier = Modifier
                                .size(15.dp)
                                .padding(end = 4.dp)
                        )
                    }

                    Text(
                        text = chat.message,
                        fontSize = 13.5.sp,
                        color = if (chat.unreadCount > 0) SoftUITheme.TextDark else SoftUITheme.TextSecondary,
                        fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (chat.unreadCount > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(19.dp)
                                .shadow(2.dp, CircleShape, ambientColor = Color(0x660075FF))
                                .clip(CircleShape)
                                .background(Color(0xFF0075FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${chat.unreadCount}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
