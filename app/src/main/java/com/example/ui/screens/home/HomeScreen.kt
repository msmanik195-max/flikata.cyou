package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.ui.components.SoftUITheme
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Story
import com.example.ui.components.FlikataAvatar
import com.example.ui.components.FlikataHeader
import com.example.ui.components.PostCard
import com.example.ui.components.PostUploadPipeCard
import com.example.ui.components.ShimmerHomeFeed
import com.example.ui.theme.FlikataPrimary
import com.example.ui.theme.FlikataSoftBlue
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onCreatePostClick: () -> Unit,
    onMenuClick: () -> Unit,
    onCreateStoryClick: () -> Unit = {}
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val activePage by viewModel.activePage.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val stories by viewModel.stories.collectAsState()
    val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsState()
    val uploadState by viewModel.postUploadState.collectAsState()
    val highlightedPostId by viewModel.highlightedPostId.collectAsState()
    val listState = viewModel.homeListState

    LaunchedEffect(highlightedPostId) {
        val target = highlightedPostId
        if (!target.isNullOrBlank() && posts.isNotEmpty()) {
            val clean = target.removePrefix("post_")
            val index = posts.indexOfFirst { it.id == target || it.id == clean || it.id == "post_$clean" }
            if (index != -1) {
                // Scroll to target shared post
                val scrollIndex = if (uploadState.isUploading) index + 3 else index + 2
                listState.animateScrollToItem(scrollIndex)
                viewModel.clearHighlightedPost()
            } else {
                viewModel.clearHighlightedPost()
            }
        }
    }

    val userAvatarUrl = currentUser?.profilePic ?: ""

    val bg = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    listOf(
                        SoftUITheme.BackgroundGradientTop,
                        SoftUITheme.BackgroundGradientBottom
                    )
                )
            )
    ) {
        // Top Header
        FlikataHeader(
            unreadNotificationsCount = unreadNotificationsCount,
            onSearchClick = { viewModel.navigateTo(Screen.SEARCH) },
            onNotificationsClick = { viewModel.navigateTo(Screen.NOTIFICATIONS) },
            onMenuClick = onMenuClick
        )

        val isDark = SoftUITheme.isDark
        val cardBg = SoftUITheme.SurfaceCard
        val composerPillBg = if (isDark) Color(0xFF1E2128) else Color(0xFFF3F7FD)
        val cardBorderBrush = if (isDark) {
            androidx.compose.ui.graphics.Brush.verticalGradient(
                listOf(Color(0x33FFFFFF), Color(0x11FFFFFF))
            )
        } else {
            androidx.compose.ui.graphics.Brush.verticalGradient(
                listOf(Color(0xEEFFFFFF), Color(0x33A0C0E8))
            )
        }

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshFeed() },
            modifier = Modifier.fillMaxSize()
        ) {
            if (isRefreshing && posts.isEmpty()) {
                ShimmerHomeFeed()
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 70.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // "What's on your mind?" 3D Neumorphic Composer Card
                    item(key = "home_composer_header") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .shadow(
                                    elevation = 2.dp,
                                    shape = RoundedCornerShape(18.dp),
                                    ambientColor = SoftUITheme.ShadowDark,
                                    spotColor = SoftUITheme.ShadowDark
                                )
                                .clip(RoundedCornerShape(18.dp))
                                .background(cardBg)
                                .border(
                                    width = 1.dp,
                                    brush = cardBorderBrush,
                                    shape = RoundedCornerShape(18.dp)
                                )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                val composerAvatar = activePage?.logoUrl ?: currentUser?.profilePic
                                val composerName = activePage?.name ?: (currentUser?.fullName ?: "User")

                                // User Profile Avatar with Initials Logo Fallback
                                Box(modifier = Modifier.clickable {
                                    if (activePage != null) {
                                        viewModel.openPageProfile(activePage!!)
                                    } else {
                                        viewModel.openUserProfile(currentUser?.id ?: "")
                                    }
                                }) {
                                    FlikataAvatar(
                                        name = composerName,
                                        avatarUrl = composerAvatar,
                                        size = 40.dp
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // 3D Recessed Pill input box
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(21.dp))
                                        .background(composerPillBg)
                                        .border(
                                            1.dp,
                                            if (isDark) androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0x22FFFFFF), Color(0x08FFFFFF)))
                                            else androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0x66A0C0E8), Color(0xEEFFFFFF))),
                                            RoundedCornerShape(21.dp)
                                        )
                                        .clickable { onCreatePostClick() }
                                        .padding(horizontal = 14.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = if (activePage != null) "Write as ${activePage!!.name}..." else "What's on your mind?",
                                        color = SoftUITheme.TextSecondary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // Green Photo Gallery Icon in 3D circle
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .shadow(2.dp, CircleShape)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0xFF1E2D22) else Color(0xFFE8F7EC))
                                        .border(1.dp, Color(0x3345BD62), CircleShape)
                                        .clickable { onCreatePostClick() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = "Gallery",
                                        tint = Color(0xFF45BD62),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 3D Neumorphic Stories Section
                    item(key = "home_stories_row_header") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 2.dp)
                                .shadow(
                                    elevation = 2.dp,
                                    shape = RoundedCornerShape(18.dp),
                                    ambientColor = SoftUITheme.ShadowDark,
                                    spotColor = SoftUITheme.ShadowDark
                                )
                                .clip(RoundedCornerShape(18.dp))
                                .background(cardBg)
                                .border(
                                    width = 1.dp,
                                    brush = cardBorderBrush,
                                    shape = RoundedCornerShape(18.dp)
                                )
                        ) {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 1. Always exactly one Create Story Card at the start
                                item(key = "create_story_card_item") {
                                    CreateStoryCard(
                                        userAvatar = userAvatarUrl,
                                        onClick = onCreateStoryClick
                                    )
                                }

                                // 2. Real user and friend stories
                                val actualStories = stories.filter { it.id != "story_create" }
                                itemsIndexed(actualStories, key = { _, s -> s.id }) { index, story ->
                                    StoryItemCard(
                                        story = story,
                                        onStoryClick = { viewModel.openStoryViewer(index) }
                                    )
                                }
                            }
                        }
                    }

                    // Post Upload Pipe (animates 0% to 100% until post is complete)
                    if (uploadState.isUploading) {
                        item(key = "home_upload_pipe_active") {
                            PostUploadPipeCard(
                                uploadState = uploadState,
                                userAvatar = userAvatarUrl
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }

                    // Post Feed List
                    itemsIndexed(posts, key = { _, post -> "post_${post.id}" }) { _, post ->
                        LaunchedEffect(post.id) {
                            viewModel.recordPostViewHistory(post)
                        }
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

@Composable
private fun CreateStoryCard(
    userAvatar: String,
    onClick: () -> Unit
) {
    val isDark = SoftUITheme.isDark
    val cardBg = if (isDark) Color(0xFF1E2128) else Color(0xFFF9FAFC)
    val cardBorder = if (isDark) Color(0x33FFFFFF) else Color(0x1F000000)

    // Sleek Card without drop shadow (Clean flat card)
    Box(
        modifier = Modifier
            .width(108.dp)
            .height(170.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("create_story_card")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top: User Profile Image (68% height)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.68f)
                    .background(if (isDark) Color(0xFF16181E) else Color(0xFFE8F1FC)),
                contentAlignment = Alignment.Center
            ) {
                if (userAvatar.isNotBlank()) {
                    AsyncImage(
                        model = userAvatar,
                        contentDescription = "Create Story Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    FlikataAvatar(
                        name = "Me",
                        avatarUrl = null,
                        size = 48.dp
                    )
                }

                // Smooth gradient overlay to seamlessly blend the photo into the card base
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, cardBg.copy(alpha = 0.7f))
                            )
                        )
                )
            }

            // Bottom: Clean card base for "Create story" text (32% height)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.32f)
                    .background(cardBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Create story",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFFF0F2F5) else Color(0xFF1E293B),
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }

        // Circular '+' Button resting cleanly on the seam between photo and base
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
                .size(34.dp)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0075FF), Color(0xFF0052D4))
                    )
                )
                .border(2.5.dp, cardBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Story",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun StoryItemCard(
    story: Story,
    onStoryClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(105.dp)
            .height(165.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.DarkGray)
            .clickable { onStoryClick() }
    ) {
        if (!story.textContent.isNullOrBlank()) {
            val bgBrush = when (story.backgroundColor) {
                "purple" -> Brush.verticalGradient(listOf(Color(0xFF8E24AA), Color(0xFF4A148C)))
                "orange" -> Brush.verticalGradient(listOf(Color(0xFFFF6D00), Color(0xFFD50000)))
                "teal" -> Brush.verticalGradient(listOf(Color(0xFF00BFA5), Color(0xFF004D40)))
                "black" -> Brush.verticalGradient(listOf(Color(0xFF212121), Color(0xFF000000)))
                "crimson" -> Brush.verticalGradient(listOf(Color(0xFFFF1744), Color(0xFF880E4F)))
                "gradient_blue" -> Brush.verticalGradient(listOf(Color(0xFF00B0FF), Color(0xFF0044FF)))
                else -> Brush.verticalGradient(listOf(Color(0xFF1877F2), Color(0xFF0D47A1)))
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgBrush)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = story.textContent,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            AsyncImage(
                model = story.storyImage,
                contentDescription = story.authorName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        AsyncImage(
            model = story.authorAvatar,
            contentDescription = story.authorName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .padding(8.dp)
                .size(34.dp)
                .align(Alignment.TopStart)
                .clip(CircleShape)
                .border(2.dp, FlikataPrimary, CircleShape)
        )

        Text(
            text = story.authorName,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        )
    }
}
