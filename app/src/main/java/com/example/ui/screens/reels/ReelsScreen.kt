package com.example.ui.screens.reels

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.geometry.Offset
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewModelScope
import androidx.media3.ui.AspectRatioFrameLayout
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.ReelCommentItem
import com.example.data.model.ReelModel
import com.example.ui.components.FacebookReactionPicker
import com.example.ui.components.FlikataAvatar
import com.example.ui.components.FlikataVideoPlayer
import com.example.ui.components.FloatingEmojiAnimation
import com.example.ui.components.GraphicReactionIcon
import com.example.ui.components.ShareBottomSheet
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.FlikataPrimary
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReelsScreen(
    viewModel: MainViewModel
) {
    val featureSettings by viewModel.featureSettings.collectAsState()
    val reelsList by viewModel.reels.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val targetReelId by viewModel.targetReelId.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var reelToDelete by remember { mutableStateOf<ReelModel?>(null) }
    val pagerState = rememberPagerState(pageCount = { reelsList.size })

    LaunchedEffect(featureSettings.allowReels) {
        if (featureSettings.allowReels) {
            viewModel.syncReelsFromServer()
        }
    }

    LaunchedEffect(targetReelId, reelsList) {
        if (!targetReelId.isNullOrBlank() && reelsList.isNotEmpty()) {
            val clean = targetReelId!!.removePrefix("post_")
            val index = reelsList.indexOfFirst { it.id == targetReelId || it.id == "post_$clean" || it.id == clean }
            if (index != -1) {
                pagerState.scrollToPage(index)
            }
        }
    }

    // Automatically record watched reel into local Video History storage
    LaunchedEffect(pagerState.currentPage, reelsList) {
        if (featureSettings.allowReels && reelsList.isNotEmpty() && pagerState.currentPage in reelsList.indices) {
            val currentReel = reelsList[pagerState.currentPage]
            viewModel.recordReelViewHistory(currentReel)
        }
    }

    val reelPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.uploadReelFromUri(uri, caption = "My new Reel 🎬")
        }
    }

    var selectedReelForComments by remember { mutableStateOf<ReelModel?>(null) }
    var selectedReelForShare by remember { mutableStateOf<ReelModel?>(null) }
    var isMutedGlobal by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (!featureSettings.allowReels) {
            // Reels Feature Disabled Notice
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                            .border(1.5.dp, Color(0xFFEF4444).copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_custom_reels),
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "রিলস সুবিধা সাময়িকভাবে বন্ধ আছে",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "অ্যাডমিন কর্তৃক রিলস ভিডিও ফিচার সাময়িকভাবে বন্ধ রাখা হয়েছে। পুনরায় চালু হলে ভিডিওগুলো দেখতে পাবেন।",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        } else if (reelsList.isEmpty()) {
            // Clean Empty State when no reels exist
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF121212)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_custom_reels),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Reels Yet",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Be the first to share an exciting video or photo reel with your friends.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            reelPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FlikataPrimary),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create Reel", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        } else {
            // Record Real View in Database when Reel is Viewed (Strictly unique, excludes self-views)
            LaunchedEffect(pagerState.currentPage, reelsList.size) {
                if (reelsList.isNotEmpty()) {
                    val currentReel = reelsList.getOrNull(pagerState.currentPage)
                    if (currentReel != null && currentReel.id.isNotBlank()) {
                        viewModel.recordReelView(currentReel.id) { verifiedCount ->
                            currentReel.viewsCount = verifiedCount.toString()
                        }
                    }
                }
            }

            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val reel = reelsList[page]
                val isOwnReel = currentUser != null && (reel.userId == currentUser?.id || reel.authorName.equals(currentUser?.fullName, ignoreCase = true))
                ReelPageItem(
                    reel = reel,
                    isPageSelected = pagerState.currentPage == page,
                    isOwnReel = isOwnReel,
                    isMuted = isMutedGlobal,
                    onDeleteClick = { reelToDelete = reel },
                    onLikeToggle = {
                        val willLike = !reel.isLiked
                        if (reel.isLiked) {
                            reel.isLiked = false
                            reel.userReaction = null
                            reel.likesCount = maxOf(0, reel.likesCount - 1)
                        } else {
                            reel.isLiked = true
                            reel.userReaction = "Like"
                            reel.likesCount += 1
                        }
                        // Persist to MySQL database!
                        viewModel.toggleReelLike(reel.id, if (willLike) (reel.userReaction ?: "Like") else "Like")
                    },
                    onReactionSelect = { reactionName ->
                        if (!reel.isLiked) {
                            reel.likesCount += 1
                        }
                        reel.isLiked = true
                        reel.userReaction = reactionName
                        // Persist reaction to MySQL database!
                        viewModel.setReelReaction(reel.id, reactionName)
                    },
                    onCommentClick = {
                        selectedReelForComments = reel
                    },
                    onShareClick = {
                        selectedReelForShare = reel
                    },
                    onFollowToggle = {
                        reel.isFollowing = !reel.isFollowing
                    },
                    onUserClick = { userId, authorName ->
                        viewModel.openUserProfile(userId, authorName)
                    }
                )
            }
        }

        // Top Reels Header Overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reels",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            // Audio Mute/Unmute Toggle Button in place of upload icon
            IconButton(
                onClick = {
                    isMutedGlobal = !isMutedGlobal
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.35f))
            ) {
                Icon(
                    imageVector = if (isMutedGlobal) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                    contentDescription = if (isMutedGlobal) "Unmute Video" else "Mute Video",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Comments Bottom Sheet - Persisted Real Database Comments
        if (selectedReelForComments != null) {
            val activeReel = selectedReelForComments!!
            ReelCommentsBottomSheet(
                reel = activeReel,
                currentUser = currentUser,
                viewModel = viewModel,
                onDismiss = { selectedReelForComments = null },
                onAddComment = { newText ->
                    // Save real comment to MySQL database
                    viewModel.addReelComment(activeReel.id, newText) { newComment ->
                        activeReel.comments.add(0, newComment)
                        activeReel.commentsCount += 1
                    }
                }
            )
        }

        // Share Bottom Sheet with WhatsApp, Facebook, Messenger, IMO, Copy Link
        if (selectedReelForShare != null) {
            val shareSheetState = rememberModalBottomSheetState()
            ShareBottomSheet(
                reel = selectedReelForShare!!,
                sheetState = shareSheetState,
                onDismiss = { selectedReelForShare = null }
            )
        }

        // Delete Confirmation Dialog
        if (reelToDelete != null) {
            AlertDialog(
                onDismissRequest = { reelToDelete = null },
                title = { Text("Delete Reel", fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to permanently delete this reel?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val id = reelToDelete!!.id
                            reelToDelete = null
                            viewModel.deleteReel(id)
                        }
                    ) {
                        Text("Delete", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { reelToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun ReelPageItem(
    reel: ReelModel,
    isPageSelected: Boolean = true,
    isOwnReel: Boolean = false,
    isMuted: Boolean = false,
    onDeleteClick: () -> Unit = {},
    onLikeToggle: () -> Unit,
    onReactionSelect: (String) -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onFollowToggle: () -> Unit,
    onUserClick: (String, String) -> Unit = { _, _ -> }
) {
    val coroutineScope = rememberCoroutineScope()
    var showReactionPicker by remember { mutableStateOf(false) }
    var activeFloatingReaction by remember { mutableStateOf<String?>(null) }
    var doubleTapOffset by remember { mutableStateOf<Offset?>(null) }

    // Big Heart Floating Animation for Double Tap
    val heartScale = remember { Animatable(0f) }
    val heartAlpha = remember { Animatable(0f) }

    val triggerDoubleTapLike = { offset: Offset ->
        doubleTapOffset = offset
        if (!reel.isLiked) {
            reel.isLiked = true
            reel.userReaction = "Love"
            reel.likesCount += 1
        }
        activeFloatingReaction = "Love"
        onReactionSelect("Love")
        coroutineScope.launch {
            heartScale.snapTo(0.2f)
            heartAlpha.snapTo(1f)
            heartScale.animateTo(
                targetValue = 1.35f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            heartScale.animateTo(1.0f, animationSpec = tween(120))
            delay(400)
            heartAlpha.animateTo(0f, animationSpec = tween(300))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Real Video Player for Reel (with autoPlay on scroll, single tap pause/play, double tap like, and prominent red seekbar)
        FlikataVideoPlayer(
            videoUri = reel.mediaUrl,
            modifier = Modifier.fillMaxSize(),
            autoPlay = isPageSelected,
            repeat = true,
            isMutedDefault = isMuted,
            showControls = false,
            showRedScrubber = true,
            scrubberBottomPadding = 62.dp,
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
            onDoubleTapWithOffset = { offset ->
                triggerDoubleTapLike(offset)
            }
        )

        // Gradient Shade at Bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.4f),
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // Big Animated Heart popping up on Double Tap at Tapped Location
        if (heartAlpha.value > 0f) {
            val density = androidx.compose.ui.platform.LocalDensity.current
            val screenWidth = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp
            val screenHeight = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp

            val xDp = with(density) {
                (doubleTapOffset?.x ?: (screenWidth.toPx() / 2f)).toDp() - 55.dp
            }
            val yDp = with(density) {
                (doubleTapOffset?.y ?: (screenHeight.toPx() / 2f)).toDp() - 55.dp
            }

            Box(
                modifier = Modifier
                    .offset(x = xDp, y = yDp)
                    .graphicsLayer {
                        scaleX = heartScale.value
                        scaleY = heartScale.value
                        alpha = heartAlpha.value
                    }
                    .zIndex(20f),
                contentAlignment = Alignment.Center
            ) {
                GraphicReactionIcon(type = "Love", size = 110.dp)
            }
        }

        // Small floating particles when reacting
        if (activeFloatingReaction != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 140.dp, end = 24.dp)
                    .zIndex(10f),
                contentAlignment = Alignment.BottomCenter
            ) {
                FloatingEmojiAnimation(
                    reactionType = activeFloatingReaction!!,
                    onAnimationEnd = { activeFloatingReaction = null }
                )
            }
        }

        // Mute / Unmute Floating Pill Indicator (flashes on tap)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp)
        ) {
            if (isMuted) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeOff,
                        contentDescription = "Muted",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Bottom Overlay Content & Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 14.dp, vertical = 76.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Left Column: Creator Profile, Follow, Caption & Audio
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                // Author row with Follow button
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onUserClick(reel.userId, reel.authorName) }
                    ) {
                        FlikataAvatar(
                            name = reel.authorName,
                            avatarUrl = reel.authorAvatar,
                            size = 40.dp,
                            borderWidth = 1.5.dp,
                            borderColor = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = reel.authorName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        if (reel.isVerified) {
                            Spacer(modifier = Modifier.width(5.dp))
                            VerifiedBadge(size = 18.dp)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    // Follow / Following pill button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (reel.isFollowing) Color.White.copy(alpha = 0.25f) else Color.Transparent)
                            .border(
                                width = 1.dp,
                                color = if (reel.isFollowing) Color.Transparent else Color.White,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { onFollowToggle() }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (reel.isFollowing) "Following" else "Follow",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Caption
                Text(
                    text = reel.caption,
                    color = Color.White,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Audio track ticker
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = reel.audioTitle,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right Action Column: Like, Comment, Share, Views Count
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. LIKE BUTTON (Post's Like Icon with Double Tap Reaction & Long Press Picker)
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .pointerInput(reel.id) {
                                detectTapGestures(
                                    onTap = {
                                        onLikeToggle()
                                        if (reel.isLiked) {
                                            activeFloatingReaction = reel.userReaction ?: "Like"
                                        }
                                    },
                                    onLongPress = {
                                        showReactionPicker = true
                                    }
                                )
                            }
                            .testTag("reel_like_btn_${reel.id}")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.35f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (reel.userReaction != null) {
                                GraphicReactionIcon(
                                    type = reel.userReaction!!,
                                    size = 30.dp
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_custom_like),
                                    contentDescription = "Like",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatCount(reel.likesCount),
                            color = when (reel.userReaction) {
                                "Love", "❤️" -> Color(0xFFFA383E)
                                "Angry", "😡" -> Color(0xFFE44D3A)
                                "Like", "👍" -> FlikataPrimary
                                null -> Color.White
                                else -> Color(0xFFF7B125)
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 2. COMMENT BUTTON (Matching Post's Comment Icon)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onCommentClick() }
                        .testTag("reel_comment_btn_${reel.id}")
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_custom_comment),
                            contentDescription = "Comment",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatCount(reel.commentsCount),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 3. SHARE BUTTON (Matching Post's Share Icon)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onShareClick() }
                        .testTag("reel_share_btn_${reel.id}")
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_custom_share),
                            contentDescription = "Share",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatCount(reel.sharesCount),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 4. EYE VIEWS COUNTER
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RemoveRedEye,
                            contentDescription = "Views",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = reel.viewsCount,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // DELETE REEL BUTTON (If owned by user)
                if (isOwnReel) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onDeleteClick() }
                            .testTag("reel_delete_btn_${reel.id}")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.35f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Reel",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Delete",
                            color = Color(0xFFFF5252),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Rotating Vinyl Disc at Bottom Right
                SpinningVinylDisc(authorAvatar = reel.authorAvatar)
            }
        }

        // Facebook Reaction Picker Floating Popup (Positioned gracefully to the left of Like button, outside action column)
        if (showReactionPicker) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) {
                        showReactionPicker = false
                    }
                    .zIndex(60f)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 280.dp, end = 68.dp)
                ) {
                    FacebookReactionPicker(
                        onReactionSelected = { reaction ->
                            onReactionSelect(reaction)
                            activeFloatingReaction = reaction
                            showReactionPicker = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SpinningVinylDisc(authorAvatar: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "disc_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Color(0xFF262626))
            .border(1.5.dp, Color(0xFF555555), CircleShape)
            .rotate(rotation),
        contentAlignment = Alignment.Center
    ) {
        FlikataAvatar(
            name = "M",
            avatarUrl = authorAvatar,
            size = 20.dp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReelCommentsBottomSheet(
    reel: ReelModel,
    currentUser: com.example.data.model.User?,
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onAddComment: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var commentInput by remember { mutableStateOf("") }
    val commentsList = remember(reel.id) {
        mutableStateListOf<ReelCommentItem>().apply { addAll(reel.comments) }
    }

    LaunchedEffect(reel.id) {
        viewModel.getReelComments(reel.id) { serverComments ->
            if (serverComments.isNotEmpty()) {
                commentsList.clear()
                commentsList.addAll(serverComments)
                reel.comments.clear()
                reel.comments.addAll(serverComments)
                reel.commentsCount = maxOf(reel.commentsCount, serverComments.size)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.72f)
        ) {
            // Header: "Comments" title + Close "X" button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Comments",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), thickness = 0.5.dp)

            // Comments List
            if (commentsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No comments yet. Be the first to comment!",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(commentsList, key = { it.id }) { comment ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            FlikataAvatar(
                                name = comment.authorName,
                                avatarUrl = comment.authorAvatar,
                                size = 38.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                // Bubble card
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Box {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = comment.authorName,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                if (comment.isVerified) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    VerifiedBadge(size = 16.dp)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = comment.text,
                                                fontSize = 14.sp,
                                                lineHeight = 20.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        if (!comment.reactionEmoji.isNullOrBlank()) {
                                            Text(
                                                text = comment.reactionEmoji,
                                                fontSize = 12.sp,
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .offset(x = 4.dp, y = 6.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.surface)
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }

                                // Meta: Time, Like, Reply
                                Row(
                                    modifier = Modifier.padding(start = 12.dp, top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = comment.timestamp,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Like",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (comment.isLiked) FlikataPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.clickable {
                                            comment.isLiked = !comment.isLiked
                                        }
                                    )
                                    Text(
                                        text = "Reply",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.clickable {
                                            commentInput = "@${comment.authorName} "
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), thickness = 0.5.dp)

            // Bottom Sticky Comment Input Bar (Matching Screenshot)
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .navigationBarsPadding()
                        .imePadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val sendCommentAction = {
                        if (commentInput.trim().isNotEmpty()) {
                            val text = commentInput.trim()
                            commentInput = ""
                            val authorName = currentUser?.fullName ?: "Me"
                            val authorAvatar = currentUser?.profilePic ?: ""
                            val isVerified = currentUser?.isVerified ?: false
                            val localComment = ReelCommentItem(
                                id = System.currentTimeMillis().toString(),
                                authorName = authorName,
                                authorAvatar = authorAvatar,
                                text = text,
                                timestamp = "Just now",
                                isVerified = isVerified
                            )
                            commentsList.add(0, localComment)
                            onAddComment(text)
                        }
                    }

                    // Light Gray Pill Input Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        androidx.compose.foundation.text.BasicTextField(
                            value = commentInput,
                            onValueChange = { commentInput = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reel_comment_input"),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Send,
                                keyboardType = KeyboardType.Text
                            ),
                            keyboardActions = KeyboardActions(
                                onSend = { sendCommentAction() }
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            ),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                if (commentInput.isEmpty()) {
                                    Text(
                                        text = "Write a comment...",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        fontSize = 14.sp
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Blue Circular Send Button
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(FlikataPrimary)
                            .clickable { sendCommentAction() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}
