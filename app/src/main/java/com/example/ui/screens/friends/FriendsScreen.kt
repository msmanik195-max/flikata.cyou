package com.example.ui.screens.friends

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FlikataAvatar
import com.example.ui.components.SoftUITheme
import com.example.ui.components.VerifiedBadge
import com.example.ui.viewmodel.MainViewModel

data class FriendItem(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val mutualFriends: Int,
    val bio: String = "",
    val isVerified: Boolean = false,
    val timeAgo: String = "",
    var isConfirmed: Boolean = false,
    var isRequestSent: Boolean = false
)

enum class FriendsTab {
    SUGGESTIONS,
    REQUESTS,
    YOUR_FRIENDS
}

@Composable
fun FriendsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler {
        onBack()
    }

    val isDark = SoftUITheme.isDark
    val currentUser by viewModel.currentUser.collectAsState()
    val allPosts by viewModel.posts.collectAsState()

    var selectedTab by remember { mutableStateOf(FriendsTab.SUGGESTIONS) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Friend requests state list
    val friendRequests = remember {
        mutableStateListOf(
            FriendItem(
                id = "5",
                name = "Test User",
                avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400",
                mutualFriends = 8,
                bio = "Photography enthusiast & traveler",
                isVerified = true,
                timeAgo = "1d ago"
            ),
            FriendItem(
                id = "7",
                name = "C D",
                avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400",
                mutualFriends = 14,
                bio = "Content Creator • Flikata Reels",
                isVerified = false,
                timeAgo = "2d ago"
            )
        )
    }

    // Dynamic suggested friends from real community posts
    val suggestions = remember(allPosts, currentUser) {
        val list = mutableStateListOf<FriendItem>()
        // 1. Prepend any authors from the real feed
        val realAuthors = allPosts
            .filter { it.userId.isNotBlank() && it.userId != currentUser?.id && it.authorName != currentUser?.fullName }
            .distinctBy { it.userId }
            .take(6)
            .map { p ->
                FriendItem(
                    id = p.userId,
                    name = p.authorName,
                    avatarUrl = p.authorAvatar,
                    mutualFriends = (p.likesCount % 15) + 3,
                    bio = p.content.take(45),
                    isVerified = p.isVerified
                )
            }
        list.addAll(realAuthors)

        // 2. Default real community profiles fallback
        if (list.none { it.id == "8" }) {
            list.add(
                FriendItem(
                    id = "8",
                    name = "Test User One",
                    avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400",
                    mutualFriends = 19,
                    bio = "Designer & Tech lover • Dhaka",
                    isVerified = true
                )
            )
        }
        if (list.none { it.id == "9" }) {
            list.add(
                FriendItem(
                    id = "9",
                    name = "Test User Two",
                    avatarUrl = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=400",
                    mutualFriends = 11,
                    bio = "Full-stack developer & gamer",
                    isVerified = true
                )
            )
        }
        if (list.none { it.id == "4" }) {
            list.add(
                FriendItem(
                    id = "4",
                    name = "Akash Ahmed",
                    avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400",
                    mutualFriends = 6,
                    bio = "Digital Marketing Strategist",
                    isVerified = false
                )
            )
        }
        list
    }

    // Your Friends list
    val yourFriends = remember {
        mutableStateListOf(
            FriendItem(
                id = "1",
                name = "Maruf Hossain",
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400",
                mutualFriends = 25,
                bio = "Software Engineer • Flikata Dev",
                isVerified = true
            ),
            FriendItem(
                id = "8",
                name = "Test User One",
                avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400",
                mutualFriends = 32,
                bio = "Photographer & Creative Director",
                isVerified = true
            ),
            FriendItem(
                id = "9",
                name = "Test User Two",
                avatarUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=400",
                mutualFriends = 18,
                bio = "Product Designer • UI/UX",
                isVerified = false
            )
        )
    }

    val cardBorderBrush = if (isDark) {
        Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x11FFFFFF)))
    } else {
        Brush.verticalGradient(listOf(Color(0xEEFFFFFF), Color(0x33A0C0E8)))
    }

    val headerBg = if (isDark) {
        Brush.verticalGradient(listOf(Color(0xFF242730), Color(0xFF1E2128), Color(0xFF16181E)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF4F8FD), Color(0xFFE9F2FC)))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        SoftUITheme.BackgroundGradientTop,
                        SoftUITheme.BackgroundGradientBottom
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // 3D Neumorphic Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (isDark) 3.dp else 2.dp,
                    shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                    ambientColor = SoftUITheme.ShadowDark,
                    spotColor = SoftUITheme.ShadowDark
                )
                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                .background(headerBg)
                .border(
                    width = 1.dp,
                    brush = cardBorderBrush,
                    shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 3D Circular Back Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(2.dp, CircleShape, ambientColor = SoftUITheme.ShadowDark)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF1E2128) else Color.White)
                            .border(1.dp, if (isDark) Color(0x33FFFFFF) else Color(0xEEFFFFFF), CircleShape)
                            .clickable { onBack() }
                            .testTag("friends_back_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SoftUITheme.TextDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Friends",
                        style = TextStyle(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF0075FF), Color(0xFF0052D4))
                            ),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.2.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    // 3D Search Toggle Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(2.dp, CircleShape, ambientColor = SoftUITheme.ShadowDark)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF1E2128) else Color.White)
                            .border(1.dp, if (isDark) Color(0x33FFFFFF) else Color(0xEEFFFFFF), CircleShape)
                            .clickable { isSearchActive = !isSearchActive },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (isSearchActive) "Close Search" else "Search Friends",
                            tint = SoftUITheme.TextDark,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                // 3D Inset Search Bar (Toggled)
                AnimatedVisibility(visible = isSearchActive) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .shadow(2.dp, RoundedCornerShape(22.dp), ambientColor = SoftUITheme.ShadowDark)
                            .clip(RoundedCornerShape(22.dp))
                            .background(if (isDark) Color(0xFF1E2128) else Color.White)
                            .border(
                                1.dp,
                                if (isDark) Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x11FFFFFF)))
                                else Brush.verticalGradient(listOf(Color(0xEEFFFFFF), Color(0x44A0C0E8))),
                                RoundedCornerShape(22.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 9.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = SoftUITheme.IconTint,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
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
                                decorationBox = { inner ->
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search friends by name...",
                                            color = SoftUITheme.TextMuted,
                                            fontSize = 14.sp
                                        )
                                    }
                                    inner()
                                }
                            )
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(20.dp)
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
                }

                // 3D Neumorphic Tabs Row
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        NeumorphicTabPill(
                            title = "Suggestions",
                            isSelected = selectedTab == FriendsTab.SUGGESTIONS,
                            badgeCount = null,
                            onClick = { selectedTab = FriendsTab.SUGGESTIONS }
                        )
                    }

                    item {
                        val reqCount = friendRequests.count { !it.isConfirmed }
                        NeumorphicTabPill(
                            title = "Requests",
                            isSelected = selectedTab == FriendsTab.REQUESTS,
                            badgeCount = if (reqCount > 0) reqCount else null,
                            onClick = { selectedTab = FriendsTab.REQUESTS }
                        )
                    }

                    item {
                        NeumorphicTabPill(
                            title = "Your Friends",
                            isSelected = selectedTab == FriendsTab.YOUR_FRIENDS,
                            badgeCount = yourFriends.size,
                            onClick = { selectedTab = FriendsTab.YOUR_FRIENDS }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Content Area
        when (selectedTab) {
            FriendsTab.SUGGESTIONS -> {
                val filteredSuggestions = remember(suggestions, searchQuery) {
                    if (searchQuery.isBlank()) suggestions
                    else suggestions.filter { it.name.contains(searchQuery, ignoreCase = true) || it.bio.contains(searchQuery, ignoreCase = true) }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "People You May Know",
                                color = SoftUITheme.TextDark,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${filteredSuggestions.size} suggested",
                                color = SoftUITheme.TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (filteredSuggestions.isEmpty()) {
                        item {
                            NeumorphicEmptyState(text = "No suggestions found matching \"$searchQuery\"")
                        }
                    } else {
                        items(filteredSuggestions, key = { it.id }) { item ->
                            NeumorphicSuggestionFriendCard(
                                item = item,
                                onAddFriend = {
                                    item.isRequestSent = !item.isRequestSent
                                },
                                onMessageClick = {
                                    viewModel.openChatWithUser(
                                        name = item.name,
                                        avatar = item.avatarUrl,
                                        isVerified = item.isVerified,
                                        userId = item.id
                                    )
                                },
                                onRemove = {
                                    suggestions.remove(item)
                                },
                                onProfileClick = {
                                    viewModel.openUserProfile(item.id, item.name)
                                }
                            )
                        }
                    }
                }
            }

            FriendsTab.REQUESTS -> {
                val filteredRequests = remember(friendRequests, searchQuery) {
                    if (searchQuery.isBlank()) friendRequests
                    else friendRequests.filter { it.name.contains(searchQuery, ignoreCase = true) }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Friend Requests",
                                color = SoftUITheme.TextDark,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            val pending = friendRequests.count { !it.isConfirmed }
                            Text(
                                text = "$pending pending",
                                color = SoftUITheme.TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (filteredRequests.isEmpty()) {
                        item {
                            NeumorphicEmptyState(text = "No friend requests at this time")
                        }
                    } else {
                        items(filteredRequests, key = { it.id }) { item ->
                            NeumorphicFriendRequestCard(
                                item = item,
                                onConfirm = {
                                    item.isConfirmed = true
                                    // Add to your friends list
                                    if (yourFriends.none { it.id == item.id }) {
                                        yourFriends.add(0, item)
                                    }
                                },
                                onDelete = {
                                    friendRequests.remove(item)
                                },
                                onMessageClick = {
                                    viewModel.openChatWithUser(
                                        name = item.name,
                                        avatar = item.avatarUrl,
                                        isVerified = item.isVerified,
                                        userId = item.id
                                    )
                                },
                                onProfileClick = {
                                    viewModel.openUserProfile(item.id, item.name)
                                }
                            )
                        }
                    }
                }
            }

            FriendsTab.YOUR_FRIENDS -> {
                val filteredFriends = remember(yourFriends, searchQuery) {
                    if (searchQuery.isBlank()) yourFriends
                    else yourFriends.filter { it.name.contains(searchQuery, ignoreCase = true) || it.bio.contains(searchQuery, ignoreCase = true) }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "All Friends",
                                color = SoftUITheme.TextDark,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${yourFriends.size} friends",
                                color = SoftUITheme.TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (filteredFriends.isEmpty()) {
                        item {
                            NeumorphicEmptyState(text = "No friends found matching \"$searchQuery\"")
                        }
                    } else {
                        items(filteredFriends, key = { it.id }) { item ->
                            NeumorphicYourFriendCard(
                                item = item,
                                onMessageClick = {
                                    viewModel.openChatWithUser(
                                        name = item.name,
                                        avatar = item.avatarUrl,
                                        isVerified = item.isVerified,
                                        userId = item.id
                                    )
                                },
                                onUnfriend = {
                                    yourFriends.remove(item)
                                },
                                onProfileClick = {
                                    viewModel.openUserProfile(item.id, item.name)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3D Neumorphic Tab Pill Button
 */
@Composable
private fun NeumorphicTabPill(
    title: String,
    isSelected: Boolean,
    badgeCount: Int? = null,
    onClick: () -> Unit
) {
    val isDark = SoftUITheme.isDark
    val bg = if (isSelected) {
        Brush.horizontalGradient(listOf(Color(0xFF0075FF), Color(0xFF0052D4)))
    } else {
        Brush.verticalGradient(
            if (isDark) listOf(Color(0xFF242730), Color(0xFF1E2128))
            else listOf(Color(0xFFFFFFFF), Color(0xFFF3F7FD))
        )
    }

    val border = if (isSelected) {
        Brush.verticalGradient(listOf(Color(0x66FFFFFF), Color(0x22FFFFFF)))
    } else {
        if (isDark) Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x11FFFFFF)))
        else Brush.verticalGradient(listOf(Color(0xEEFFFFFF), Color(0x44A0C0E8)))
    }

    Box(
        modifier = Modifier
            .shadow(
                elevation = if (isSelected) 3.dp else 2.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = if (isSelected) Color(0x400075FF) else SoftUITheme.ShadowDark,
                spotColor = if (isSelected) Color(0x600075FF) else SoftUITheme.ShadowDark
            )
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                color = if (isSelected) Color.White else SoftUITheme.TextDark,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
            )
            if (badgeCount != null && badgeCount > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isSelected) Color(0x33FFFFFF) else Color(0xFF0075FF))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeCount.toString(),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * 3D Neumorphic Friend Request Card
 */
@Composable
private fun NeumorphicFriendRequestCard(
    item: FriendItem,
    onConfirm: () -> Unit,
    onDelete: () -> Unit,
    onMessageClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val isDark = SoftUITheme.isDark
    val cardBg = SoftUITheme.SurfaceCard
    val cardBorder = if (isDark) {
        Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x11FFFFFF)))
    } else {
        Brush.verticalGradient(listOf(Color(0xEEFFFFFF), Color(0x33A0C0E8)))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = SoftUITheme.ShadowDark,
                spotColor = SoftUITheme.ShadowDark
            )
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 3D Avatar Frame
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .shadow(2.dp, CircleShape, ambientColor = SoftUITheme.ShadowDark)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF1E2128) else Color.White)
                    .border(1.5.dp, if (isDark) Color(0x33FFFFFF) else Color(0x99FFFFFF), CircleShape)
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                FlikataAvatar(
                    name = item.name,
                    avatarUrl = item.avatarUrl,
                    size = 60.dp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .clickable { onProfileClick() }
                    ) {
                        Text(
                            text = item.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = SoftUITheme.TextDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (item.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            VerifiedBadge(size = 14.dp)
                        }
                    }

                    if (item.timeAgo.isNotBlank()) {
                        Text(
                            text = item.timeAgo,
                            fontSize = 11.5.sp,
                            color = SoftUITheme.TextMuted
                        )
                    }
                }

                if (item.mutualFriends > 0) {
                    Text(
                        text = "${item.mutualFriends} mutual friends",
                        fontSize = 12.sp,
                        color = SoftUITheme.TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (item.isConfirmed) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFE8F7EC))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF45BD62),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Request Accepted",
                                    color = Color(0xFF2E7D32),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // 3D Message Button
                        Box(
                            modifier = Modifier
                                .shadow(2.dp, CircleShape, ambientColor = SoftUITheme.ShadowDark)
                                .clip(CircleShape)
                                .background(Brush.horizontalGradient(listOf(Color(0xFF0075FF), Color(0xFF0052D4))))
                                .clickable { onMessageClick() }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = "Message",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 3D Glossy Confirm Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .shadow(2.dp, RoundedCornerShape(12.dp), ambientColor = Color(0x400075FF))
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.horizontalGradient(listOf(Color(0xFF0075FF), Color(0xFF0052D4))))
                                .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(12.dp))
                                .clickable { onConfirm() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Confirm", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        // 3D Recessed Delete Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .shadow(1.dp, RoundedCornerShape(12.dp), ambientColor = SoftUITheme.ShadowDark)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) Color(0xFF1E2128) else Color(0xFFF3F7FD))
                                .border(1.dp, if (isDark) Color(0x22FFFFFF) else Color(0x33A0C0E8), RoundedCornerShape(12.dp))
                                .clickable { onDelete() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Delete",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SoftUITheme.TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3D Neumorphic Suggestion Friend Card
 */
@Composable
private fun NeumorphicSuggestionFriendCard(
    item: FriendItem,
    onAddFriend: () -> Unit,
    onMessageClick: () -> Unit,
    onRemove: () -> Unit,
    onProfileClick: () -> Unit
) {
    val isDark = SoftUITheme.isDark
    val cardBg = SoftUITheme.SurfaceCard
    val cardBorder = if (isDark) {
        Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x11FFFFFF)))
    } else {
        Brush.verticalGradient(listOf(Color(0xEEFFFFFF), Color(0x33A0C0E8)))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = SoftUITheme.ShadowDark,
                spotColor = SoftUITheme.ShadowDark
            )
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 3D Avatar
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .shadow(2.dp, CircleShape, ambientColor = SoftUITheme.ShadowDark)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF1E2128) else Color.White)
                    .border(1.5.dp, if (isDark) Color(0x33FFFFFF) else Color(0x99FFFFFF), CircleShape)
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                FlikataAvatar(
                    name = item.name,
                    avatarUrl = item.avatarUrl,
                    size = 60.dp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onProfileClick() }
                ) {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = SoftUITheme.TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        VerifiedBadge(size = 14.dp)
                    }
                }

                if (item.bio.isNotBlank()) {
                    Text(
                        text = item.bio,
                        fontSize = 12.sp,
                        color = SoftUITheme.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (item.mutualFriends > 0) {
                    Text(
                        text = "${item.mutualFriends} mutual friends",
                        fontSize = 11.5.sp,
                        color = SoftUITheme.TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 3D Follow / Following Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .shadow(
                                elevation = 2.dp,
                                shape = RoundedCornerShape(12.dp),
                                ambientColor = if (item.isRequestSent) SoftUITheme.ShadowDark else Color(0x400075FF)
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (item.isRequestSent) {
                                    SolidColor(if (isDark) Color(0xFF1E2128) else Color(0xFFE8F1FC))
                                } else {
                                    Brush.horizontalGradient(listOf(Color(0xFF0075FF), Color(0xFF0052D4)))
                                }
                            )
                            .border(
                                1.dp,
                                if (item.isRequestSent) Color(0x330075FF) else Color(0x44FFFFFF),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onAddFriend() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (item.isRequestSent) Icons.Default.Check else Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = if (item.isRequestSent) Color(0xFF0075FF) else Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (item.isRequestSent) "Following" else "Follow",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.isRequestSent) Color(0xFF0075FF) else Color.White
                            )
                        }
                    }

                    // 3D Message Icon Button (Direct Chat with real userId!)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .shadow(2.dp, CircleShape, ambientColor = SoftUITheme.ShadowDark)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF1E2D3E) else Color(0xFFE8F1FC))
                            .border(1.dp, Color(0x330075FF), CircleShape)
                            .clickable { onMessageClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Message",
                            tint = Color(0xFF0075FF),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // 3D Remove Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .shadow(1.dp, CircleShape, ambientColor = SoftUITheme.ShadowDark)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF1E2128) else Color(0xFFF3F7FD))
                            .border(1.dp, if (isDark) Color(0x22FFFFFF) else Color(0x33A0C0E8), CircleShape)
                            .clickable { onRemove() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = SoftUITheme.TextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 3D Neumorphic Your Friend Card
 */
@Composable
private fun NeumorphicYourFriendCard(
    item: FriendItem,
    onMessageClick: () -> Unit,
    onUnfriend: () -> Unit,
    onProfileClick: () -> Unit
) {
    val isDark = SoftUITheme.isDark
    val cardBg = SoftUITheme.SurfaceCard
    val cardBorder = if (isDark) {
        Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x11FFFFFF)))
    } else {
        Brush.verticalGradient(listOf(Color(0xEEFFFFFF), Color(0x33A0C0E8)))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = SoftUITheme.ShadowDark,
                spotColor = SoftUITheme.ShadowDark
            )
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 3D Avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(2.dp, CircleShape, ambientColor = SoftUITheme.ShadowDark)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF1E2128) else Color.White)
                    .border(1.5.dp, if (isDark) Color(0x33FFFFFF) else Color(0x99FFFFFF), CircleShape)
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                FlikataAvatar(
                    name = item.name,
                    avatarUrl = item.avatarUrl,
                    size = 54.dp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onProfileClick() }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = SoftUITheme.TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        VerifiedBadge(size = 14.dp)
                    }
                }

                if (item.bio.isNotBlank()) {
                    Text(
                        text = item.bio,
                        fontSize = 12.sp,
                        color = SoftUITheme.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (item.mutualFriends > 0) {
                    Text(
                        text = "${item.mutualFriends} mutual friends",
                        fontSize = 11.5.sp,
                        color = SoftUITheme.TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 3D Direct Message Action Button (Sends messages to real user!)
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .shadow(3.dp, CircleShape, ambientColor = Color(0x400075FF))
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(Color(0xFF0075FF), Color(0xFF0052D4))))
                    .border(1.dp, Color(0x66FFFFFF), CircleShape)
                    .clickable { onMessageClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = "Message",
                    tint = Color.White,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

/**
 * 3D Neumorphic Empty State Container
 */
@Composable
private fun NeumorphicEmptyState(text: String) {
    val isDark = SoftUITheme.isDark
    val cardBg = SoftUITheme.SurfaceCard

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 8.dp)
            .shadow(2.dp, RoundedCornerShape(18.dp), ambientColor = SoftUITheme.ShadowDark)
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .border(
                1.dp,
                if (isDark) Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x11FFFFFF)))
                else Brush.verticalGradient(listOf(Color(0xEEFFFFFF), Color(0x33A0C0E8))),
                RoundedCornerShape(18.dp)
            )
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF1E2128) else Color(0xFFF3F7FD))
                    .border(1.dp, if (isDark) Color(0x22FFFFFF) else Color(0x33A0C0E8), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = SoftUITheme.IconTint,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = text,
                color = SoftUITheme.TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
