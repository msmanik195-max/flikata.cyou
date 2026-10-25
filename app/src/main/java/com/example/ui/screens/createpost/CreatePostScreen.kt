package com.example.ui.screens.createpost

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.FlikataAvatar
import com.example.ui.components.FlikataVideoPlayer
import com.example.ui.components.PostBackgroundHelper
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.FlikataPrimary
import com.example.ui.theme.FlikataSoftBlue
import com.example.ui.theme.FlikataSurface
import com.example.ui.theme.FlikataTextPrimary
import com.example.ui.theme.FlikataTextSecondary
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    viewModel: MainViewModel,
    onClose: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val activePage by viewModel.activePage.collectAsState()
    val postContent by viewModel.postContent.collectAsState()
    val privacy by viewModel.postPrivacy.collectAsState()
    val bgStyle by viewModel.postBackgroundStyle.collectAsState()
    val imageUrl by viewModel.postSelectedImageUrl.collectAsState()
    val videoUrl by viewModel.postSelectedVideoUrl.collectAsState()
    val isVideo by viewModel.postSelectedIsVideo.collectAsState()

    var showPrivacySheet by remember { mutableStateOf(false) }
    var showStylePicker by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Android Photo Picker Launcher for uploading photos only
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val cached = viewModel.cacheMediaFromUri(uri, isVideo = false)
            viewModel.postSelectedImageUrl.value = cached
            viewModel.postSelectedVideoUrl.value = null
            viewModel.postSelectedIsVideo.value = false
        }
    }

    // Android Video Picker Launcher for uploading videos only with duration validation
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val context = viewModel.getApplication<android.app.Application>()
            val maxSeconds = viewModel.reelMaxDurationSeconds.value
            val durationSeconds = try {
                val retriever = android.media.MediaMetadataRetriever()
                retriever.setDataSource(context, uri)
                val durStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
                val durMs = durStr?.toLongOrNull() ?: 0L
                retriever.release()
                (durMs / 1000).toInt()
            } catch (e: Exception) {
                0
            }

            if (maxSeconds > 0 && durationSeconds > maxSeconds) {
                android.widget.Toast.makeText(
                    context,
                    "Video duration ($durationSeconds s) exceeds maximum limit ($maxSeconds s).",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                return@rememberLauncherForActivityResult
            }

            val cached = viewModel.cacheMediaFromUri(uri, isVideo = true)
            viewModel.postSelectedVideoUrl.value = cached
            viewModel.postSelectedImageUrl.value = null
            viewModel.postSelectedIsVideo.value = true
        }
    }

    Scaffold(
        topBar = {
            Surface(color = FlikataSurface, tonalElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClose) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = FlikataTextPrimary)
                    }

                    Text(
                        text = "Create Post",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = FlikataTextPrimary
                    )

                    Button(
                        onClick = { viewModel.submitPost() },
                        enabled = postContent.isNotBlank() || imageUrl != null || videoUrl != null,
                        colors = ButtonDefaults.buttonColors(containerColor = FlikataPrimary),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("submit_post_btn")
                    ) {
                        Text(text = "Post", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // User/Page Header Row
            val authorName = if (activePage != null) activePage!!.name else (currentUser?.fullName ?: "User")
            val authorPic = if (activePage != null) activePage!!.logoUrl else currentUser?.profilePic
            val isVerified = if (activePage != null) activePage!!.isVerified else (currentUser?.isVerified == true)

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlikataAvatar(
                    name = authorName,
                    avatarUrl = authorPic,
                    size = 46.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = authorName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = FlikataTextPrimary
                        )
                        if (isVerified) {
                            Spacer(modifier = Modifier.width(5.dp))
                            VerifiedBadge(size = 18.dp)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Privacy Dropdown Pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { showPrivacySheet = true }
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (privacy) {
                                    "Friends" -> Icons.Default.People
                                    "Only Me" -> Icons.Default.Lock
                                    else -> Icons.Default.Public
                                },
                                contentDescription = "Privacy",
                                tint = FlikataPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = privacy,
                                color = FlikataPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                tint = FlikataPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (activePage != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFE8F5E9))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Posting as Page",
                                    color = Color(0xFF2E7D32),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Post Content Text Input with Background Styling support
            val textBackgroundBrush = if (bgStyle != null) PostBackgroundHelper.getBrush(bgStyle!!) else null

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (bgStyle != null) 180.dp else 120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        if (textBackgroundBrush != null) Modifier.background(textBackgroundBrush)
                        else Modifier
                    ),
                contentAlignment = if (bgStyle != null) Alignment.Center else Alignment.TopStart
            ) {
                TextField(
                    value = postContent,
                    onValueChange = { viewModel.postContent.value = it },
                    placeholder = {
                        Text(
                            text = "What's on your mind?",
                            fontSize = if (bgStyle != null) 20.sp else 18.sp,
                            fontWeight = if (bgStyle != null) FontWeight.Bold else FontWeight.Normal,
                            color = if (bgStyle != null) Color.White.copy(alpha = 0.7f) else FlikataTextSecondary,
                            textAlign = if (bgStyle != null) TextAlign.Center else TextAlign.Start
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = if (bgStyle != null) Color.White else FlikataTextPrimary,
                        unfocusedTextColor = if (bgStyle != null) Color.White else FlikataTextPrimary
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = if (bgStyle != null) 20.sp else 18.sp,
                        fontWeight = if (bgStyle != null) FontWeight.Bold else FontWeight.Normal,
                        textAlign = if (bgStyle != null) TextAlign.Center else TextAlign.Start
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("create_post_text_field")
                )
            }

            // Attached Selected Video Preview
            if (isVideo && videoUrl != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black)
                ) {
                    FlikataVideoPlayer(
                        videoUri = videoUrl!!,
                        modifier = Modifier.fillMaxSize(),
                        autoPlay = false,
                        repeat = true,
                        showControls = true
                    )

                    // Top Left Video Badge
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Video Preview",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Delete X Button on Video Preview
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.65f))
                            .clickable {
                                viewModel.postSelectedVideoUrl.value = null
                                viewModel.postSelectedIsVideo.value = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove Video",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else if (!isVideo && imageUrl != null) {
                // Attached Selected Image Preview
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Selected Post Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Delete X Button on Image Preview
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.65f))
                            .clickable { viewModel.postSelectedImageUrl.value = null },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove Image",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Formatting Chips Row ("Tt 24", Alignment, "🎨 Style")
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FormatChip(text = "Tt 24", onClick = { })
                FormatChip(icon = Icons.Default.FormatAlignLeft, onClick = { })
                FormatChip(
                    text = "Style",
                    icon = Icons.Default.Palette,
                    isSelected = showStylePicker,
                    onClick = { showStylePicker = !showStylePicker }
                )
            }

            // Expanded Background Style Selector
            if (showStylePicker) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Background Style",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = FlikataTextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Row 1: Single Colors
                        Text(
                            text = "Single Colors", 
                            fontSize = 12.sp, 
                            fontWeight = FontWeight.SemiBold,
                            color = FlikataTextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(vertical = 4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Reset to None (No background)
                            item {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(
                                            width = if (bgStyle == null) 2.dp else 1.dp,
                                            color = if (bgStyle == null) FlikataPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { viewModel.postBackgroundStyle.value = null },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close, 
                                        contentDescription = "None", 
                                        tint = if (bgStyle == null) FlikataPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // All 22 Single Solid Colors
                            items(PostBackgroundHelper.singleColors) { item ->
                                ColorCircle(
                                    color = item.color,
                                    isSelected = bgStyle == item.id,
                                    onClick = { viewModel.postBackgroundStyle.value = item.id }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Row 2: Gradient Colors
                        Text(
                            text = "Gradient Colors", 
                            fontSize = 12.sp, 
                            fontWeight = FontWeight.SemiBold,
                            color = FlikataTextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(vertical = 4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // All 18 Multi-color Gradients
                            items(PostBackgroundHelper.gradientColors) { item ->
                                GradientSquare(
                                    brush = item.brush,
                                    isSelected = bgStyle == item.id,
                                    onClick = { viewModel.postBackgroundStyle.value = item.id }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Grid Card matching screenshot ("Add Photos", "Add Video", "Reels", "Tag")
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ActionGridItem(
                        icon = Icons.Default.Image,
                        label = "Add Photos",
                        bgColor = Color(0xFF2E7D32).copy(alpha = 0.15f),
                        iconTint = Color(0xFF45BD62)
                    ) {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }

                    ActionGridItem(
                        icon = Icons.Default.Videocam,
                        label = "Add Video",
                        bgColor = Color(0xFFC62828).copy(alpha = 0.15f),
                        iconTint = Color(0xFFFA383E)
                    ) {
                        videoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    }

                    ActionGridItem(
                        icon = Icons.Default.VideoLibrary,
                        label = "Reels",
                        bgColor = Color(0xFF6A1B9A).copy(alpha = 0.15f),
                        iconTint = Color(0xFFAB47BC)
                    ) {
                        videoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    }

                    ActionGridItem(
                        icon = Icons.Default.Tag,
                        label = "Tag",
                        bgColor = Color(0xFF00838F).copy(alpha = 0.15f),
                        iconTint = Color(0xFF26C6DA)
                    )
                }
            }
        }
    }

    // Modal Bottom Sheet: "Who can see your post?" - Explicitly White Background in Light Mode
    if (showPrivacySheet) {
        ModalBottomSheet(
            onDismissRequest = { showPrivacySheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(20.dp)
            ) {
                Text(
                    text = "Who can see your post?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = FlikataTextPrimary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                PrivacyOption(
                    title = "Public",
                    subtitle = "Anyone on or off Flikata",
                    icon = Icons.Default.Public,
                    isSelected = privacy == "Public"
                ) {
                    viewModel.postPrivacy.value = "Public"
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showPrivacySheet = false }
                }

                PrivacyOption(
                    title = "Friends",
                    subtitle = "Your friends on Flikata",
                    icon = Icons.Default.People,
                    isSelected = privacy == "Friends"
                ) {
                    viewModel.postPrivacy.value = "Friends"
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showPrivacySheet = false }
                }

                PrivacyOption(
                    title = "Only Me",
                    subtitle = "Only you can see this post",
                    icon = Icons.Default.Lock,
                    isSelected = privacy == "Only Me"
                ) {
                    viewModel.postPrivacy.value = "Only Me"
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showPrivacySheet = false }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun FormatChip(
    text: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) FlikataPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = text ?: "",
                    tint = if (isSelected) FlikataPrimary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp)
                )
                if (text != null) Spacer(modifier = Modifier.width(4.dp))
            }
            if (text != null) {
                Text(
                    text = text,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) FlikataPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ColorCircle(
    color: Color, 
    isSelected: Boolean = false, 
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(color)
            .then(
                if (isSelected) Modifier.border(2.5.dp, Color.White, RoundedCornerShape(10.dp))
                else Modifier.border(1.dp, Color.Black.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun GradientSquare(
    brush: Brush, 
    isSelected: Boolean = false, 
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(brush)
            .then(
                if (isSelected) Modifier.border(2.5.dp, Color.White, RoundedCornerShape(10.dp))
                else Modifier
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun ActionGridItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    bgColor: Color,
    iconTint: Color,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = iconTint)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = iconTint
        )
    }
}

@Composable
private fun PrivacyOption(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = FlikataPrimary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = FlikataTextPrimary)
                Text(text = subtitle, fontSize = 12.sp, color = FlikataTextSecondary)
            }
        }

        RadioButton(
            selected = isSelected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(selectedColor = FlikataPrimary)
        )
    }
}
