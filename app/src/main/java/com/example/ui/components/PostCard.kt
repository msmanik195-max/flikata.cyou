package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.example.R
import com.example.data.model.Post
import com.example.data.model.User
import com.example.ui.theme.FlikataDivider
import com.example.ui.theme.FlikataError
import com.example.ui.theme.FlikataPrimary
import com.example.ui.theme.FlikataSurface
import com.example.ui.theme.FlikataTextPrimary
import com.example.ui.theme.FlikataTextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCard(
    post: Post,
    currentUser: User? = null,
    onLikeClick: (String) -> Unit = {},
    onReactionSelect: (String, String?) -> Unit = { _, _ -> },
    onImageClick: (String) -> Unit = {},
    onDeletePost: (String) -> Unit = {},
    onEditPost: (String, String, String) -> Unit = { _, _, _ -> },
    onAddComment: (String, String) -> Unit = { _, _ -> },
    onUserClick: (String, String) -> Unit = { _, _ -> },
    onSavePost: (Post) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showReactionPicker by remember { mutableStateOf(false) }
    var activeFloatingEmoji by remember { mutableStateOf<String?>(null) }

    var showReactionsSheet by remember { mutableStateOf(false) }
    var showCommentsSheet by remember { mutableStateOf(false) }
    var showOptionsSheet by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }

    val reactionsSheetState = rememberModalBottomSheetState()
    val commentsSheetState = rememberModalBottomSheetState()
    val optionsSheetState = rememberModalBottomSheetState()
    val shareSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val isOwnPost = currentUser != null && (currentUser.id == post.userId || currentUser.fullName == post.authorName)

    val cardSurface = androidx.compose.material3.MaterialTheme.colorScheme.surface
    val textPrimary = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
    val textSecondary = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant

    val isDark = SoftUITheme.isDark
    val actualCardBg = if (isDark) Color(0xFF242730) else Color.White
    val cardBorderBrush = if (isDark) {
        Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x11FFFFFF)))
    } else {
        Brush.verticalGradient(listOf(Color(0xEEFFFFFF), Color(0x33A0C0E8)))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .shadow(
                elevation = if (isDark) 3.dp else 2.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = SoftUITheme.ShadowDark,
                spotColor = SoftUITheme.ShadowDark
            )
            .clip(RoundedCornerShape(18.dp))
            .background(actualCardBg)
            .border(
                width = 1.dp,
                brush = cardBorderBrush,
                shape = RoundedCornerShape(18.dp)
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onUserClick(post.userId, post.authorName) }
                ) {
                    FlikataAvatar(
                        name = post.authorName,
                        avatarUrl = post.authorAvatar,
                        size = 42.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = post.authorName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = textPrimary
                            )
                            if (post.isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                VerifiedBadge(size = 22.dp)
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = post.timestamp,
                                fontSize = 12.sp,
                                color = textSecondary
                            )
                            Text(
                                text = " • ",
                                fontSize = 12.sp,
                                color = textSecondary
                            )
                            val privacyIcon = when {
                                post.privacy.equals("Only Me", ignoreCase = true) || post.privacy.equals("Private", ignoreCase = true) -> Icons.Default.Lock
                                post.privacy.equals("Friends", ignoreCase = true) -> Icons.Default.People
                                else -> Icons.Default.Public
                            }
                            Icon(
                                imageVector = privacyIcon,
                                contentDescription = post.privacy,
                                tint = textSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                // 3-Dots Options Button
                IconButton(onClick = { showOptionsSheet = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "Options",
                        tint = textSecondary
                    )
                }
            }

            // Text Body / Background Styled Post
            if (post.backgroundStyle != null) {
                val bgBrush = PostBackgroundHelper.getBrush(post.backgroundStyle)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(bgBrush),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.content,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            } else if (post.content.isNotEmpty()) {
                Text(
                    text = post.content,
                    fontSize = 15.sp,
                    color = textPrimary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            // Attached Video Player if post is a video
            if (post.isVideo && !post.videoUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color.Black)
                ) {
                    FlikataVideoPlayer(
                        videoUri = post.videoUrl,
                        modifier = Modifier.fillMaxSize(),
                        autoPlay = false,
                        repeat = true,
                        showControls = true,
                        showRedScrubber = true,
                        scrubberBottomPadding = 0.dp
                    )
                }
            } else if (!post.imageUrl.isNullOrBlank()) {
                // Attached Image with clean, robust AsyncImage
                Spacer(modifier = Modifier.height(4.dp))
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = "Post Image",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onImageClick(post.imageUrl) }
                )
            }

            // Reaction Counts Summary Row - only show if there are likes, comments, or shares
            if (post.likesCount > 0 || post.commentsCount > 0 || post.sharesCount > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (post.likesCount > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showReactionsSheet = true }
                        ) {
                            val reactionEmoji = post.userReaction ?: "Like"
                            GraphicReactionIcon(reactionEmoji, size = 18.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${post.likesCount}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textSecondary
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (post.commentsCount > 0) {
                            Text(
                                text = "${post.commentsCount} ${if (post.commentsCount == 1) "comment" else "comments"}",
                                fontSize = 13.sp,
                                color = textSecondary,
                                modifier = Modifier.clickable { showCommentsSheet = true }
                            )
                        }
                        if (post.sharesCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${post.sharesCount} shares",
                                fontSize = 13.sp,
                                color = textSecondary
                            )
                        }
                    }
                }
            }

            Divider(color = androidx.compose.material3.MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), thickness = 0.5.dp)

            // Action Buttons Row with Reaction Picker Floating Popup
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (showReactionPicker) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(50f)
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = { showReactionPicker = false })
                            }
                    )

                    FacebookReactionPicker(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = 12.dp, y = (-54).dp)
                            .zIndex(100f),
                        onReactionSelected = { reactionName ->
                            onReactionSelect(post.id, reactionName)
                            activeFloatingEmoji = reactionName
                            showReactionPicker = false
                        }
                    )
                }

                if (activeFloatingEmoji != null) {
                    FloatingEmojiAnimation(
                        reactionType = activeFloatingEmoji!!,
                        onAnimationEnd = { activeFloatingEmoji = null },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = 32.dp, y = (-20).dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Box(
                        modifier = Modifier
                            .pointerInput(post.id, post.userReaction) {
                                detectTapGestures(
                                    onTap = {
                                        if (post.userReaction != null) {
                                            onReactionSelect(post.id, null)
                                        } else {
                                            onReactionSelect(post.id, "Like")
                                            activeFloatingEmoji = "Like"
                                        }
                                    },
                                    onLongPress = {
                                        showReactionPicker = !showReactionPicker
                                    }
                                )
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("like_post_${post.id}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (post.userReaction != null) {
                                GraphicReactionIcon(
                                    type = post.userReaction,
                                    size = 20.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = post.userReaction,
                                    color = when (post.userReaction) {
                                        "Love", "❤️" -> Color(0xFFFA383E)
                                        "Angry", "😡" -> Color(0xFFE44D3A)
                                        "Like", "👍" -> FlikataPrimary
                                        else -> Color(0xFFF7B125)
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_custom_like),
                                    contentDescription = "Like",
                                    tint = textSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Like",
                                    color = textSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .clickable { showCommentsSheet = true }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("comment_post_${post.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_custom_comment),
                            contentDescription = "Comment",
                            tint = textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Comment",
                            color = textSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clickable { showShareSheet = true }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("share_post_${post.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_custom_share),
                            contentDescription = "Share",
                            tint = textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Share",
                            color = textSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

    // Modal Sheet for Post Options (3-Dots)
    if (showOptionsSheet) {
        PostOptionsBottomSheet(
            isOwnPost = isOwnPost,
            sheetState = optionsSheetState,
            onDismiss = {
                scope.launch { optionsSheetState.hide() }.invokeOnCompletion { showOptionsSheet = false }
            },
            onSaveClick = {
                scope.launch { optionsSheetState.hide() }.invokeOnCompletion {
                    showOptionsSheet = false
                    onSavePost(post)
                    Toast.makeText(context, "Post saved to your collection!", Toast.LENGTH_SHORT).show()
                }
            },
            onDeleteClick = {
                scope.launch { optionsSheetState.hide() }.invokeOnCompletion {
                    showOptionsSheet = false
                    showDeleteConfirmDialog = true
                }
            },
            onEditClick = {
                scope.launch { optionsSheetState.hide() }.invokeOnCompletion {
                    showOptionsSheet = false
                    showEditDialog = true
                }
            },
            onCopyLinkClick = {
                scope.launch { optionsSheetState.hide() }.invokeOnCompletion {
                    showOptionsSheet = false
                    val isVideo = !post.videoUrl.isNullOrBlank() || post.isVideo
                    val shareUrl = com.example.data.remote.SecureConfig.getPostShareUrl(post.id, isVideo)
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Flikata Link", shareUrl)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
                }
            },
            onShareClick = {
                scope.launch { optionsSheetState.hide() }.invokeOnCompletion {
                    showOptionsSheet = false
                    showShareSheet = true
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Delete Post?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently delete this post? This cannot be undone.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeletePost(post.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FlikataError),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // Modern Share Bottom Sheet
    if (showShareSheet) {
        ShareBottomSheet(
            post = post,
            sheetState = shareSheetState,
            onDismiss = {
                scope.launch { shareSheetState.hide() }.invokeOnCompletion { showShareSheet = false }
            }
        )
    }

    if (showEditDialog) {
        EditPostDialog(
            post = post,
            onDismiss = { showEditDialog = false },
            onSave = { newContent, newPrivacy ->
                showEditDialog = false
                onEditPost(post.id, newContent, newPrivacy)
            }
        )
    }

    if (showReactionsSheet) {
        ReactionsBottomSheet(
            post = post,
            sheetState = reactionsSheetState,
            onDismiss = {
                scope.launch { reactionsSheetState.hide() }.invokeOnCompletion { showReactionsSheet = false }
            }
        )
    }

    if (showCommentsSheet) {
        CommentsBottomSheet(
            post = post,
            currentUser = currentUser,
            sheetState = commentsSheetState,
            onAddComment = onAddComment,
            onDismiss = {
                scope.launch { commentsSheetState.hide() }.invokeOnCompletion { showCommentsSheet = false }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostOptionsBottomSheet(
    isOwnPost: Boolean,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSaveClick: () -> Unit = {},
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit = {},
    onCopyLinkClick: () -> Unit = {},
    onShareClick: () -> Unit = {}
) {
    val sheetSurface = androidx.compose.material3.MaterialTheme.colorScheme.surface
    val sheetOnSurface = androidx.compose.material3.MaterialTheme.colorScheme.onSurface

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetSurface,
        contentColor = sheetOnSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(sheetSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Save Option (Universal for all posts - Saves to Saved Page)
            PostOptionRow(
                icon = Icons.Default.Bookmark,
                iconTint = Color(0xFF0075FF),
                title = "Save post",
                onClick = onSaveClick
            )

            if (isOwnPost) {
                // Own Post Options
                PostOptionRow(
                    icon = Icons.Default.Edit,
                    iconTint = FlikataPrimary,
                    title = "Edit Post",
                    onClick = onEditClick
                )

                PostOptionRow(
                    icon = Icons.Default.Delete,
                    iconTint = FlikataError,
                    title = "Delete Post",
                    onClick = onDeleteClick
                )

                PostOptionRow(
                    icon = Icons.Default.ContentCopy,
                    iconTint = FlikataTextSecondary,
                    title = "Copy Link",
                    onClick = onCopyLinkClick
                )

                PostOptionRow(
                    icon = Icons.Default.Share,
                    iconTint = FlikataTextSecondary,
                    title = "Share to...",
                    onClick = onShareClick
                )
            } else {
                // Someone Else's Post Options
                PostOptionRow(
                    icon = Icons.Default.ReportProblem,
                    iconTint = Color(0xFFFF9800),
                    title = "Report Post",
                    onClick = onDismiss
                )

                PostOptionRow(
                    icon = Icons.Default.ContentCopy,
                    iconTint = FlikataTextSecondary,
                    title = "Copy Link",
                    onClick = onCopyLinkClick
                )

                PostOptionRow(
                    icon = Icons.Default.Share,
                    iconTint = FlikataTextSecondary,
                    title = "Share to...",
                    onClick = onShareClick
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PostOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun EditPostDialog(
    post: Post,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var contentText by remember { mutableStateOf(post.content) }
    var selectedPrivacy by remember { mutableStateOf(post.privacy.ifBlank { "Public" }) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with title and close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Post",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Privacy selector chips
                Text(
                    text = "Who can see your post?",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Public
                    FilterChip(
                        selected = selectedPrivacy.equals("Public", ignoreCase = true),
                        onClick = { selectedPrivacy = "Public" },
                        label = { Text("Public", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FlikataPrimary,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )

                    // Only Me (Private)
                    FilterChip(
                        selected = selectedPrivacy.equals("Only Me", ignoreCase = true) || selectedPrivacy.equals("Private", ignoreCase = true),
                        onClick = { selectedPrivacy = "Only Me" },
                        label = { Text("Only Me", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FlikataPrimary,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )

                    // Friends
                    FilterChip(
                        selected = selectedPrivacy.equals("Friends", ignoreCase = true),
                        onClick = { selectedPrivacy = "Friends" },
                        label = { Text("Friends", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FlikataPrimary,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Text field for post content
                OutlinedTextField(
                    value = contentText,
                    onValueChange = { contentText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    placeholder = { Text("What's on your mind?", fontSize = 14.sp) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FlikataPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Buttons: Cancel & Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Button(
                        onClick = {
                            onSave(contentText, selectedPrivacy)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FlikataPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
