package com.example.ui.screens.search

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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FlikataAvatar
import com.example.ui.components.PostCard
import com.example.ui.components.SoftUITheme
import com.example.ui.components.VerifiedBadge
import com.example.ui.viewmodel.MainViewModel

data class SearchUserResult(
    val id: String,
    val name: String,
    val avatar: String,
    val isVerified: Boolean,
    val subtitle: String
)

@Composable
fun SearchScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler {
        onBack()
    }

    val isDark = SoftUITheme.isDark
    val query by viewModel.searchQuery.collectAsState()
    val allPosts by viewModel.posts.collectAsState()
    val allReels by viewModel.reels.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var activeFilter by remember { mutableStateOf("All") } // "All", "People", "Posts", "Reels"

    var dynamicSearchedUser by remember { mutableStateOf<SearchUserResult?>(null) }

    LaunchedEffect(query) {
        val trimmed = query.trim().removePrefix("user_").removePrefix("partner_").trim()
        if (trimmed.isNotBlank() && trimmed.toIntOrNull() != null) {
            val res = viewModel.repository.fetchUserProfile(trimmed)
            if (res.isSuccess) {
                val p = res.getOrNull()
                if (p != null && p.idString != currentUser?.id) {
                    dynamicSearchedUser = SearchUserResult(
                        id = p.idString,
                        name = p.fullName ?: "User $trimmed",
                        avatar = p.profilePic ?: "",
                        isVerified = p.isVerifiedBool,
                        subtitle = p.bio ?: "Active on Flikata"
                    )
                } else {
                    dynamicSearchedUser = null
                }
            } else {
                dynamicSearchedUser = null
            }
        } else {
            dynamicSearchedUser = null
        }
    }

    // Extract unique users across posts, community, and dynamic search
    val matchedUsers = remember(query, allPosts, currentUser, dynamicSearchedUser) {
        if (query.isBlank()) emptyList()
        else {
            val userMap = mutableMapOf<String, SearchUserResult>()
            dynamicSearchedUser?.let { userMap[it.id] = it }
            for (post in allPosts) {
                if (post.authorName.contains(query, ignoreCase = true) || post.userId == query) {
                    val uid = post.userId.ifBlank { "user_${post.authorName.hashCode()}" }
                    if (!userMap.containsKey(uid) && uid != currentUser?.id) {
                        userMap[uid] = SearchUserResult(
                            id = uid,
                            name = post.authorName,
                            avatar = post.authorAvatar,
                            isVerified = post.isVerified,
                            subtitle = post.content.take(40).ifBlank { "Active on Flikata" }
                        )
                    }
                }
            }
            // Also include sample community profiles if matched
            val samples = listOf(
                SearchUserResult("8", "Test User One", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400", true, "Designer & Tech lover • Dhaka"),
                SearchUserResult("9", "Test User Two", "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=400", true, "Full-stack developer & gamer"),
                SearchUserResult("4", "Akash Ahmed", "https://pub-c98a2e409ad94dbb8aded428cf2952a1.r2.dev/images/flikata_20261004_090334_c57b9129d2b6.jpg", false, "Digital Creator • Flikata"),
                SearchUserResult("3", "Maruf Home", "https://pub-c98a2e409ad94dbb8aded428cf2952a1.r2.dev/images/flikata_20261003_055434_34e12090472c.jpg", false, "Content Creator")
            )
            for (s in samples) {
                if (s.name.contains(query, ignoreCase = true) && s.id != currentUser?.id) {
                    if (!userMap.containsKey(s.id)) {
                        userMap[s.id] = s
                    }
                }
            }
            userMap.values.toList()
        }
    }

    val matchedPosts = remember(query, allPosts) {
        if (query.isBlank()) emptyList()
        else {
            allPosts.filter { post ->
                post.content.contains(query, ignoreCase = true) ||
                        post.authorName.contains(query, ignoreCase = true)
            }
        }
    }

    val matchedReels = remember(query, allReels) {
        if (query.isBlank()) emptyList()
        else {
            allReels.filter { reel ->
                reel.caption.contains(query, ignoreCase = true) ||
                        reel.authorName.contains(query, ignoreCase = true)
            }
        }
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

    val searchFieldBg = if (isDark) Color(0xFF191B22) else Color(0xFFF3F7FD)

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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                // Top Row: Back button + 3D Recessed Search Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 3D Circular Back Button
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .shadow(2.dp, CircleShape, ambientColor = SoftUITheme.ShadowDark)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF1E2128) else Color.White)
                            .border(1.dp, if (isDark) Color(0x33FFFFFF) else Color(0xEEFFFFFF), CircleShape)
                            .clickable { onBack() }
                            .testTag("search_back_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SoftUITheme.TextDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // 3D Recessed Rounded Search Bar
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .shadow(
                                elevation = if (isDark) 1.dp else 2.dp,
                                shape = RoundedCornerShape(23.dp),
                                ambientColor = SoftUITheme.ShadowDark
                            )
                            .clip(RoundedCornerShape(23.dp))
                            .background(searchFieldBg)
                            .border(
                                width = 1.dp,
                                brush = if (isDark) Brush.verticalGradient(listOf(Color(0x22FFFFFF), Color(0x08FFFFFF)))
                                else Brush.verticalGradient(listOf(Color(0x66A0C0E8), Color(0xEEFFFFFF))),
                                shape = RoundedCornerShape(23.dp)
                            )
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF0075FF),
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            BasicTextField(
                                value = query,
                                onValueChange = { viewModel.searchQuery.value = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = SoftUITheme.TextDark,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(Color(0xFF0075FF)),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("search_input"),
                                decorationBox = { innerTextField ->
                                    if (query.isEmpty()) {
                                        Text(
                                            text = "Search Flikata posts, people, reels...",
                                            color = SoftUITheme.TextMuted,
                                            fontSize = 14.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )

                            if (query.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0x33FFFFFF) else Color(0x1F0075FF))
                                        .clickable { viewModel.searchQuery.value = "" },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = if (isDark) Color.White else Color(0xFF0075FF),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3D Filter Pills Row with glowing state
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf("All", "People", "Posts", "Reels")
                    filters.forEach { filter ->
                        val isSelected = activeFilter == filter
                        val count = when (filter) {
                            "People" -> matchedUsers.size
                            "Posts" -> matchedPosts.size
                            "Reels" -> matchedReels.size
                            else -> matchedUsers.size + matchedPosts.size + matchedReels.size
                        }

                        item {
                            Box(
                                modifier = Modifier
                                    .shadow(
                                        elevation = if (isSelected) 3.dp else 2.dp,
                                        shape = RoundedCornerShape(20.dp),
                                        ambientColor = if (isSelected) Color(0x400075FF) else SoftUITheme.ShadowDark
                                    )
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) {
                                            Brush.horizontalGradient(listOf(Color(0xFF0075FF), Color(0xFF0052D4)))
                                        } else {
                                            if (isDark) Brush.verticalGradient(listOf(Color(0xFF242730), Color(0xFF1E2128)))
                                            else Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF3F7FD)))
                                        }
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) Brush.verticalGradient(listOf(Color(0x66FFFFFF), Color(0x22FFFFFF)))
                                        else if (isDark) Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x11FFFFFF)))
                                        else Brush.verticalGradient(listOf(Color(0xEEFFFFFF), Color(0x44A0C0E8))),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clickable { activeFilter = filter }
                                    .padding(horizontal = 14.dp, vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = filter,
                                        color = if (isSelected) Color.White else SoftUITheme.TextDark,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                    )
                                    if (query.isNotBlank() && count > 0) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(if (isSelected) Color(0x33FFFFFF) else Color(0x1F0075FF))
                                                .padding(horizontal = 6.dp, vertical = 1.5.dp)
                                        ) {
                                            Text(
                                                text = count.toString(),
                                                color = if (isSelected) Color.White else Color(0xFF0075FF),
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Search Content Section
        if (query.isBlank()) {
            // 3D Empty/Welcome State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(22.dp), ambientColor = SoftUITheme.ShadowDark)
                        .clip(RoundedCornerShape(22.dp))
                        .background(SoftUITheme.SurfaceCard)
                        .border(1.dp, cardBorderBrush, RoundedCornerShape(22.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .shadow(3.dp, CircleShape, ambientColor = Color(0x330075FF))
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF0075FF), Color(0xFF00C6FF))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "Search Flikata 3D",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftUITheme.TextDark
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Search for your friends, community posts, reels, and hashtags with instant real-time results.",
                            fontSize = 13.5.sp,
                            color = SoftUITheme.TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 19.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Quick suggestion pills
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Akash", "Maruf", "Reels", "Photo").forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .shadow(1.dp, RoundedCornerShape(14.dp))
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isDark) Color(0xFF1E2128) else Color(0xFFE8F1FC))
                                        .border(1.dp, Color(0x330075FF), RoundedCornerShape(14.dp))
                                        .clickable { viewModel.searchQuery.value = tag }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = tag,
                                        color = Color(0xFF0075FF),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Result List
            val showPeople = (activeFilter == "All" || activeFilter == "People") && matchedUsers.isNotEmpty()
            val showPosts = (activeFilter == "All" || activeFilter == "Posts") && matchedPosts.isNotEmpty()
            val showReels = (activeFilter == "All" || activeFilter == "Reels") && matchedReels.isNotEmpty()

            val totalFound = (if (showPeople) matchedUsers.size else 0) +
                    (if (showPosts) matchedPosts.size else 0) +
                    (if (showReels) matchedReels.size else 0)

            if (totalFound == 0) {
                // No Results Card
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(20.dp), ambientColor = SoftUITheme.ShadowDark)
                            .clip(RoundedCornerShape(20.dp))
                            .background(SoftUITheme.SurfaceCard)
                            .border(1.dp, cardBorderBrush, RoundedCornerShape(20.dp))
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No results found",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftUITheme.TextDark
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "We couldn't find anything matching \"$query\". Try checking the spelling or use a different keyword.",
                                fontSize = 13.5.sp,
                                color = SoftUITheme.TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. People Section
                    if (showPeople) {
                        item(key = "section_people_header") {
                            Text(
                                text = "People (${matchedUsers.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftUITheme.TextDark,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }

                        items(matchedUsers, key = { "user_${it.id}" }) { user ->
                            SearchUser3DCard(
                                user = user,
                                onProfileClick = {
                                    viewModel.openUserProfile(user.id, user.name)
                                },
                                onMessageClick = {
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

                    // 2. Posts Section
                    if (showPosts) {
                        item(key = "section_posts_header") {
                            Text(
                                text = "Posts (${matchedPosts.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftUITheme.TextDark,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }

                        items(matchedPosts, key = { "post_${it.id}" }) { post ->
                            PostCard(
                                post = post,
                                currentUser = currentUser,
                                onLikeClick = { viewModel.toggleLike(it) },
                                onReactionSelect = { postId, emoji -> viewModel.setReaction(postId, emoji) },
                                onImageClick = { imageUrl -> viewModel.openImageViewer(imageUrl) },
                                onDeletePost = { postId -> viewModel.deletePost(postId) },
                                onEditPost = { postId, content, privacy -> viewModel.editPost(postId, content, privacy) },
                                onAddComment = { postId, text -> viewModel.addComment(postId, text) },
                                onUserClick = { userId, authorName -> viewModel.openUserProfile(userId, authorName) },
                                onSavePost = { viewModel.toggleSavePost(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3D Soft Neumorphic Card for Matched Users
 */
@Composable
private fun SearchUser3DCard(
    user: SearchUserResult,
    onProfileClick: () -> Unit,
    onMessageClick: () -> Unit
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
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 3D Circular Avatar Container
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .shadow(2.dp, CircleShape, ambientColor = SoftUITheme.ShadowDark)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF1E2128) else Color.White)
                    .border(1.5.dp, if (isDark) Color(0x33FFFFFF) else Color(0x99FFFFFF), CircleShape)
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                FlikataAvatar(
                    name = user.name,
                    avatarUrl = user.avatar,
                    size = 50.dp
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
                        text = user.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftUITheme.TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (user.isVerified) {
                        Spacer(modifier = Modifier.width(5.dp))
                        VerifiedBadge(size = 18.dp)
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = user.subtitle,
                    fontSize = 12.5.sp,
                    color = SoftUITheme.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 3D Message Button
            Box(
                modifier = Modifier
                    .shadow(2.dp, RoundedCornerShape(14.dp), ambientColor = Color(0x330075FF))
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF0075FF), Color(0xFF0052D4))
                        )
                    )
                    .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(14.dp))
                    .clickable { onMessageClick() }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
                    .testTag("search_message_user_${user.id}"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Message",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Message",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
