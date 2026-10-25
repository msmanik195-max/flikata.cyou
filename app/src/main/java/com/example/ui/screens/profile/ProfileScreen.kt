package com.example.ui.screens.profile

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Call
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import android.content.ClipboardManager
import android.content.ClipData
import android.widget.Toast
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.data.model.ReelModel
import com.example.ui.components.VerifiedBadge
import com.example.ui.viewmodel.Screen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.components.FlikataAvatar
import com.example.ui.components.PostCard
import com.example.ui.components.ProfileSettingsBottomSheet
import com.example.ui.components.SoftUITheme
import com.example.ui.theme.FlikataBackground
import com.example.ui.theme.FlikataPrimary
import com.example.ui.theme.FlikataSoftBlue
import com.example.ui.theme.FlikataSurface
import com.example.ui.theme.FlikataTextPrimary
import com.example.ui.theme.FlikataTextSecondary
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onCreatePostClick: () -> Unit = {},
    onCreateStoryClick: () -> Unit = {}
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allPosts by viewModel.posts.collectAsState()
    val allReelsList by viewModel.reels.collectAsState()
    val selectedUserProfileId by viewModel.selectedUserProfileId.collectAsState()
    val selectedUserProfileName by viewModel.selectedUserProfileName.collectAsState()
    val context = LocalContext.current

    val editOccupation by viewModel.editOccupation.collectAsState()
    val editEducation by viewModel.editEducation.collectAsState()
    val editGender by viewModel.editGender.collectAsState()
    val editDob by viewModel.editDob.collectAsState()
    val editEmail by viewModel.editEmail.collectAsState()
    val editPhone by viewModel.editPhone.collectAsState()
    val editLocation by viewModel.editLocation.collectAsState()
    val editWebsite by viewModel.editWebsite.collectAsState()

    val showPhoneInAbout by viewModel.showPhoneInAbout.collectAsState()
    val showEmailInAbout by viewModel.showEmailInAbout.collectAsState()
    val showBirthdayInAbout by viewModel.showBirthdayInAbout.collectAsState()
    val showGenderInAbout by viewModel.showGenderInAbout.collectAsState()
    val showLocationInAbout by viewModel.showLocationInAbout.collectAsState()
    val showOccupationInAbout by viewModel.showOccupationInAbout.collectAsState()
    val showEducationInAbout by viewModel.showEducationInAbout.collectAsState()
    val showWebsiteInAbout by viewModel.showWebsiteInAbout.collectAsState()

    val isOwnProfile = selectedUserProfileId == null || selectedUserProfileId == currentUser?.id

    BackHandler {
        if (!isOwnProfile) {
            viewModel.clearSelectedUserProfile()
        }
        onBack()
    }

    val tabs = listOf("All", "Reels", "Photos", "About", "More ▾")
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var dragAccumulator by remember { mutableFloatStateOf(0f) }

    val swipeModifier = Modifier.pointerInput(selectedTabIndex) {
        detectHorizontalDragGestures(
            onDragStart = { dragAccumulator = 0f },
            onHorizontalDrag = { change, dragAmount ->
                change.consume()
                dragAccumulator += dragAmount
            },
            onDragEnd = {
                val threshold = 50f
                if (dragAccumulator < -threshold) {
                    // Swiped Left -> Next tab
                    if (selectedTabIndex < tabs.size - 1) {
                        selectedTabIndex++
                    }
                } else if (dragAccumulator > threshold) {
                    // Swiped Right -> Previous tab
                    if (selectedTabIndex > 0) {
                        selectedTabIndex--
                    }
                }
                dragAccumulator = 0f
            },
            onDragCancel = {
                dragAccumulator = 0f
            }
        )
    }

    var showSettingsSheet by remember { mutableStateOf(false) }
    val settingsSheetState = rememberModalBottomSheetState()

    val userPosts = remember(allPosts, isOwnProfile, selectedUserProfileId, selectedUserProfileName, currentUser) {
        if (isOwnProfile) {
            allPosts.filter { (it.userId == currentUser?.id || it.authorName == currentUser?.fullName) && it.pageId == null && !it.userId.startsWith("page_") }
        } else {
            allPosts.filter { (it.userId == selectedUserProfileId || (selectedUserProfileName != null && it.authorName.equals(selectedUserProfileName, ignoreCase = true))) && it.pageId == null && !it.userId.startsWith("page_") }
        }
    }

    val externalProfile by viewModel.selectedUserProfileData.collectAsState()

    val displayUserName = if (isOwnProfile) (currentUser?.fullName ?: "User") else (externalProfile?.fullName ?: selectedUserProfileName ?: userPosts.firstOrNull()?.authorName ?: "Flikata User")
    val displayUserAvatar = if (isOwnProfile) (currentUser?.profilePic ?: "") else (externalProfile?.profilePic ?: userPosts.firstOrNull()?.authorAvatar ?: "")
    val displayUserCover = if (isOwnProfile) (currentUser?.coverPic ?: "") else (externalProfile?.coverPic ?: "")
    val displayIsVerified = if (isOwnProfile) (currentUser?.isVerified ?: false) else (externalProfile?.isVerifiedBool ?: userPosts.firstOrNull()?.isVerified ?: false)
    val displayIsOnline = if (isOwnProfile) true else (externalProfile?.isOnlineBool ?: false)
    val displayBio = if (isOwnProfile) (currentUser?.bio ?: "Drop a thought...") else (externalProfile?.bio ?: "Hey there! I am using Flikata.")
    val displayOccupation = if (isOwnProfile) "Digital Creator" else (externalProfile?.occupation ?: "Digital Creator")
    val displayLocation = if (isOwnProfile) "Dhaka, Bangladesh" else (externalProfile?.location ?: "Dhaka, Bangladesh")
    val displayJoinedDate = if (isOwnProfile) (currentUser?.joinedDate ?: "Joined September 2026") else (externalProfile?.joinedDate ?: "Joined October 2026")
    val isFollowingSelectedUser by viewModel.isFollowingSelectedUser.collectAsState()
    val selectedUserFollowersCount by viewModel.selectedUserFollowersCount.collectAsState()
    val selectedUserFollowingCount by viewModel.selectedUserFollowingCount.collectAsState()

    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val targetUid = selectedUserProfileId?.removePrefix("user_")?.toIntOrNull() ?: 1
            if (!displayIsOnline) {
                Toast.makeText(context, "Offline", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.startAudioCall(
                    partnerId = targetUid,
                    partnerName = displayUserName,
                    partnerAvatar = displayUserAvatar,
                    isVerified = displayIsVerified
                )
            }
        } else {
            Toast.makeText(context, "Microphone permission is required to make audio calls", Toast.LENGTH_SHORT).show()
        }
    }

    val userReels = remember(userPosts, allReelsList, isOwnProfile, selectedUserProfileId, selectedUserProfileName, currentUser) {
        val videoPosts = userPosts.filter { !it.videoUrl.isNullOrBlank() || it.isVideo }
        val matchingReels = allReelsList.filter { r ->
            if (isOwnProfile) {
                r.userId == currentUser?.id || r.authorName.equals(currentUser?.fullName, ignoreCase = true)
            } else {
                r.userId == selectedUserProfileId || (selectedUserProfileName != null && r.authorName.equals(selectedUserProfileName, ignoreCase = true))
            }
        }
        val fromPosts = videoPosts.map { p ->
            val matchedReel = allReelsList.find { r ->
                (r.mediaUrl.isNotBlank() && r.mediaUrl == p.videoUrl) || r.id == "post_${p.id}"
            }
            val realViews = matchedReel?.viewsCount?.ifBlank { null } ?: "${p.viewsCount}"
            ReelModel(
                id = "post_${p.id}",
                userId = p.userId,
                authorName = p.authorName,
                authorAvatar = p.authorAvatar,
                isVerified = p.isVerified,
                mediaUrl = p.videoUrl ?: "",
                caption = p.content.ifBlank { "Video Reel" },
                audioTitle = "Original Audio - ${p.authorName}",
                likesCount = p.likesCount,
                commentsCount = p.commentsCount,
                sharesCount = p.sharesCount,
                viewsCount = realViews,
                isLiked = p.isLiked,
                userReaction = p.userReaction
            )
        }
        (fromPosts + matchingReels).distinctBy { it.mediaUrl.ifBlank { it.id } }
    }

    val userPhotos = userPosts.mapNotNull { it.imageUrl }

    val isDark = SoftUITheme.isDark
    val screenBg = MaterialTheme.colorScheme.background
    val screenSurface = MaterialTheme.colorScheme.surface
    val screenVariant = MaterialTheme.colorScheme.surfaceVariant
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    val cardBg = if (isDark) Color(0xFF242730) else Color.White
    val secondaryBtnBg = if (isDark) Color(0xFF2D3039) else Color(0xFFF3F7FD)
    val secondaryBtnBorder = if (isDark) Color(0x33FFFFFF) else Color(0x33A0C0E8)
    val secondaryBtnText = if (isDark) Color(0xFF3897F0) else Color(0xFF0075FF)

    val isRefreshingProfile by viewModel.isRefreshingProfile.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshingProfile,
            onRefresh = { viewModel.refreshProfile() },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
            // Top Cover Banner + Overlapping Curved Profile Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(screenBg)
                ) {
                    // 1. Blue Gradient Taller Cover Banner (Height 260.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF0D47A1),
                                        Color(0xFF1877F2),
                                        Color(0xFF42A5F5)
                                    )
                                )
                            )
                    ) {
                        val coverModel: Any = if (isOwnProfile) {
                            if (!currentUser?.coverPic.isNullOrEmpty() && !currentUser?.coverPic!!.contains("unsplash")) {
                                currentUser?.coverPic!!
                            } else {
                                R.drawable.default_banner
                            }
                        } else {
                            if (displayUserCover.isNotBlank() && !displayUserCover.contains("unsplash")) {
                                displayUserCover
                            } else {
                                R.drawable.default_banner
                            }
                        }

                        AsyncImage(
                            model = coverModel,
                            contentDescription = "Cover Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Top Bar Action Buttons with status bars padding for clear status bar visibility
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CoverCircleButton(
                                icon = Icons.AutoMirrored.Filled.ArrowBack,
                                onClick = {
                                    if (!isOwnProfile) {
                                        viewModel.clearSelectedUserProfile()
                                    }
                                    onBack()
                                }
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (isOwnProfile) {
                                    CoverCircleButton(icon = Icons.Default.Edit, onClick = { viewModel.prepareEditProfile() })
                                }
                                CoverCircleButton(icon = Icons.Default.Search, onClick = onSearchClick)
                                CoverCircleButton(icon = Icons.Default.MoreHoriz, onClick = { showSettingsSheet = true })
                            }
                        }

                        // Floating "Drop a thought..." Speech Bubble on Banner
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .offset(y = (-15).dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    color = screenSurface,
                                    shape = RoundedCornerShape(20.dp),
                                    shadowElevation = 4.dp
                                ) {
                                    Text(
                                        text = if (isOwnProfile) (currentUser?.bio?.ifBlank { "Drop a thought..." } ?: "Drop a thought...") else "Welcome to my profile!",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = textPrimary,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(GenericShape { size, _ ->
                                            moveTo(0f, 0f)
                                            lineTo(size.width, 0f)
                                            lineTo(size.width / 2, size.height)
                                            close()
                                        })
                                        .background(screenSurface)
                                )
                            }
                        }
                    }

                    // 2. 3D Neumorphic Curved Container Card Overlapping the Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 210.dp)
                            .shadow(
                                elevation = if (isDark) 5.dp else 8.dp,
                                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                                ambientColor = com.example.ui.components.SoftUITheme.ShadowDark,
                                spotColor = com.example.ui.components.SoftUITheme.ShadowDark
                            )
                            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                            .background(cardBg)
                            .border(
                                width = 1.dp,
                                brush = if (isDark)
                                    Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x11FFFFFF)))
                                else
                                    Brush.verticalGradient(listOf(Color(0xEEFFFFFF), Color(0x33A0C0E8))),
                                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(top = 56.dp, bottom = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Name & Verification Badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = displayUserName,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                                if (displayIsVerified) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    VerifiedBadge(size = 26.dp)
                                }
                                if (displayIsOnline && !isOwnProfile) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(11.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF45BD62))
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Followers, Following, Posts line
                            val followersCount = if (isOwnProfile) (currentUser?.followersCount ?: 0) else selectedUserFollowersCount
                            val followingCount = if (isOwnProfile) (currentUser?.followingCount ?: 0) else selectedUserFollowingCount
                            val actualPostsCount = if (isOwnProfile) userPosts.size else (externalProfile?.postsCount ?: userPosts.size)
                            Text(
                                text = "$followersCount followers • $followingCount following • $actualPostsCount posts",
                                fontSize = 13.sp,
                                color = textSecondary
                            )

                            if (displayBio.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = displayBio,
                                    fontSize = 13.5.sp,
                                    color = textPrimary,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // 3D Side-by-side Action Buttons
                            if (isOwnProfile) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // 3D Primary Button: Dashboard
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(46.dp)
                                            .shadow(
                                                elevation = 6.dp,
                                                shape = RoundedCornerShape(14.dp),
                                                ambientColor = Color(0x660075FF),
                                                spotColor = Color(0x880052D4)
                                            )
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(Color(0xFF0075FF), Color(0xFF0052D4))
                                                )
                                            )
                                            .border(1.dp, Color(0x88FFFFFF), RoundedCornerShape(14.dp))
                                            .clickable {
                                                viewModel.navigateTo(Screen.DASHBOARD)
                                            }
                                            .testTag("dashboard_btn"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.BarChart,
                                                contentDescription = "Dashboard",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Dashboard",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    // 3D Secondary Button: Add to Story
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(46.dp)
                                            .shadow(
                                                elevation = if (isDark) 2.dp else 4.dp,
                                                shape = RoundedCornerShape(14.dp),
                                                ambientColor = com.example.ui.components.SoftUITheme.ShadowDark
                                            )
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(secondaryBtnBg)
                                            .border(1.dp, secondaryBtnBorder, RoundedCornerShape(14.dp))
                                            .clickable { onCreateStoryClick() }
                                            .testTag("add_to_story_btn"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Add Story",
                                                tint = secondaryBtnText,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Add to Story",
                                                fontWeight = FontWeight.Bold,
                                                color = secondaryBtnText,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // 3D Follow Button
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(46.dp)
                                            .shadow(
                                                elevation = if (isFollowingSelectedUser) (if (isDark) 2.dp else 3.dp) else 6.dp,
                                                shape = RoundedCornerShape(14.dp),
                                                ambientColor = if (isFollowingSelectedUser) com.example.ui.components.SoftUITheme.ShadowDark else Color(0x660075FF)
                                            )
                                            .clip(RoundedCornerShape(14.dp))
                                            .then(
                                                if (isFollowingSelectedUser) Modifier.background(secondaryBtnBg)
                                                else Modifier.background(Brush.verticalGradient(listOf(Color(0xFF0075FF), Color(0xFF0052D4))))
                                            )
                                            .border(
                                                1.dp,
                                                if (isFollowingSelectedUser) secondaryBtnBorder else Color(0x88FFFFFF),
                                                RoundedCornerShape(14.dp)
                                            )
                                            .clickable {
                                                val targetUid = selectedUserProfileId?.removePrefix("user_")?.toIntOrNull()
                                                    ?: externalProfile?.id
                                                    ?: 0
                                                viewModel.toggleFollowUser(targetUid)
                                            }
                                            .testTag("follow_user_btn"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (isFollowingSelectedUser) Icons.Default.Check else Icons.Default.PersonAdd,
                                                contentDescription = null,
                                                tint = if (isFollowingSelectedUser) secondaryBtnText else Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isFollowingSelectedUser) "Following" else "Follow",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = if (isFollowingSelectedUser) secondaryBtnText else Color.White
                                            )
                                        }
                                    }

                                    // 3D Message Button
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(46.dp)
                                            .shadow(
                                                elevation = if (isDark) 2.dp else 4.dp,
                                                shape = RoundedCornerShape(14.dp),
                                                ambientColor = com.example.ui.components.SoftUITheme.ShadowDark
                                            )
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(secondaryBtnBg)
                                            .border(1.dp, secondaryBtnBorder, RoundedCornerShape(14.dp))
                                            .clickable {
                                                val targetUid = selectedUserProfileId ?: userPosts.firstOrNull()?.userId
                                                viewModel.openChatWithUser(
                                                    name = displayUserName,
                                                    avatar = displayUserAvatar,
                                                    isVerified = displayIsVerified,
                                                    userId = targetUid
                                                )
                                            }
                                            .testTag("message_user_btn"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.ChatBubbleOutline,
                                                contentDescription = "Message",
                                                tint = secondaryBtnText,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Message",
                                                fontWeight = FontWeight.Bold,
                                                color = secondaryBtnText,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Filter Tabs ("All", "Reels", "Photos", "About", "More ▾")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(swipeModifier),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                tabs.forEachIndexed { index, tab ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clickable { selectedTabIndex = index }
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = tab,
                                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selectedTabIndex == index) FlikataPrimary else textSecondary,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        if (selectedTabIndex == index) {
                                            Box(
                                                modifier = Modifier
                                                    .width(36.dp)
                                                    .height(3.dp)
                                                    .background(FlikataPrimary, CircleShape)
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.height(3.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. Overlapping Circular Profile Picture Badge with initials logo fallback & Camera Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = 150.dp)
                    ) {
                        FlikataAvatar(
                            name = displayUserName,
                            avatarUrl = displayUserAvatar.ifBlank { null },
                            size = 110.dp,
                            borderWidth = 4.dp,
                            borderColor = screenSurface,
                            modifier = Modifier.clickable {
                                if (isOwnProfile) {
                                    viewModel.prepareEditProfile()
                                } else if (displayUserAvatar.isNotBlank()) {
                                    viewModel.openImageViewer(displayUserAvatar)
                                }
                            }
                        )

                        // Camera overlay badge on avatar (only for own profile)
                        if (isOwnProfile) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = (-4).dp, y = (-4).dp)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(FlikataPrimary)
                                    .border(2.dp, screenSurface, CircleShape)
                                    .clickable { viewModel.prepareEditProfile() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Change Profile Picture",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Active green status dot on avatar for other online user
                        if (displayIsOnline && !isOwnProfile) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = (-4).dp, y = (-4).dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF45BD62))
                                    .border(3.5.dp, screenSurface, CircleShape)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Tab Content Rendering with Hand Swipe Gesture & Animated Transition
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(swipeModifier)
                ) {
                    AnimatedContent(
                        targetState = selectedTabIndex,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> -width } + fadeOut()
                                )
                            } else {
                                (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> width } + fadeOut()
                                )
                            }
                        },
                        label = "ProfileTabContent"
                    ) { targetIndex ->
                        when (tabs.getOrElse(targetIndex) { "All" }) {
                            "All" -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (userPosts.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 32.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "No posts yet",
                                                color = textSecondary,
                                                fontSize = 14.sp
                                            )
                                        }
                                    } else {
                                        userPosts.forEach { post ->
                                            PostCard(
                                                post = post,
                                                currentUser = currentUser,
                                                onLikeClick = { viewModel.toggleLike(it) },
                                                onReactionSelect = { postId, emoji -> viewModel.setReaction(postId, emoji) },
                                                onImageClick = { imageUrl -> viewModel.openImageViewer(imageUrl) },
                                                onDeletePost = { postId -> viewModel.deletePost(postId) },
                                                onEditPost = { postId, content, privacy -> viewModel.editPost(postId, content, privacy) },
                                                onSavePost = { viewModel.toggleSavePost(it) }
                                            )
                                        }
                                    }
                                }
                            }

                            "Reels" -> {
                                Surface(color = screenSurface, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = "Uploaded Reels",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = textPrimary,
                                            modifier = Modifier.padding(bottom = 10.dp)
                                        )

                                        if (userReels.isEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 32.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "No reels or videos uploaded yet",
                                                    color = textSecondary,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        } else {
                                            val chunkedReels = userReels.chunked(3)
                                            chunkedReels.forEach { rowItems ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                rowItems.forEach { reel ->
                                                    val context = LocalContext.current
                                                    val videoRequest = remember(reel.mediaUrl) {
                                                        if (reel.mediaUrl.isNotBlank() && (reel.mediaUrl.endsWith(".mp4", ignoreCase = true) || reel.mediaUrl.contains("/video", ignoreCase = true))) {
                                                            ImageRequest.Builder(context)
                                                                .data(reel.mediaUrl)
                                                                .decoderFactory { result, options, _ -> VideoFrameDecoder(result.source, options) }
                                                                .videoFrameMillis(1000)
                                                                .crossfade(true)
                                                                .build()
                                                        } else {
                                                            reel.mediaUrl.ifBlank { reel.authorAvatar }
                                                        }
                                                    }

                                                    val rawViews = reel.viewsCount.toIntOrNull() ?: 0
                                                    val formattedViews = if (rawViews >= 1000) {
                                                        String.format(java.util.Locale.US, "%.1fK", rawViews / 1000.0)
                                                    } else {
                                                        "$rawViews"
                                                    }

                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .aspectRatio(0.5625f) // 9:16 vertical ratio for taller reels
                                                            .clip(RoundedCornerShape(10.dp))
                                                            .background(Color(0xFF1E2128))
                                                            .clickable { viewModel.openReel(reel.id) }
                                                    ) {
                                                        AsyncImage(
                                                            model = videoRequest,
                                                            contentDescription = "Reel Thumbnail",
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier.fillMaxSize()
                                                        )

                                                        // Subtle top gradient & play badge
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .height(40.dp)
                                                                .background(
                                                                    Brush.verticalGradient(
                                                                        listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)
                                                                    )
                                                                )
                                                        )

                                                        Icon(
                                                            imageVector = Icons.Default.PlayArrow,
                                                            contentDescription = "Play",
                                                            tint = Color.White,
                                                            modifier = Modifier
                                                                .align(Alignment.TopEnd)
                                                                .padding(6.dp)
                                                                .size(18.dp)
                                                        )

                                                        // Bottom views bar with verified real view count
                                                        Row(
                                                            modifier = Modifier
                                                                .align(Alignment.BottomStart)
                                                                .fillMaxWidth()
                                                                .background(
                                                                    Brush.verticalGradient(
                                                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                                                    )
                                                                )
                                                                .padding(horizontal = 6.dp, vertical = 6.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.PlayArrow,
                                                                contentDescription = "Views",
                                                                tint = Color.White,
                                                                modifier = Modifier.size(13.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(3.dp))
                                                            Text(
                                                                text = "$formattedViews views",
                                                                color = Color.White,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }

                                                repeat(3 - rowItems.size) {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                            "Photos" -> {
                                Surface(color = screenSurface, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = "Uploaded Photos",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = textPrimary,
                                            modifier = Modifier.padding(bottom = 10.dp)
                                        )

                                        if (userPhotos.isEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 32.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "No photos uploaded yet",
                                                    color = textSecondary,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        } else {
                                            val chunkedPhotos = userPhotos.chunked(3)
                                            chunkedPhotos.forEach { photoRow ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    photoRow.forEach { photoUrl ->
                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .aspectRatio(1f)
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                                .clickable { viewModel.openImageViewer(photoUrl) }
                                                        ) {
                                                            AsyncImage(
                                                                model = photoUrl,
                                                                contentDescription = "Photo",
                                                                contentScale = ContentScale.Crop,
                                                                modifier = Modifier.fillMaxSize()
                                                            )
                                                        }
                                                    }
                                                    repeat(3 - photoRow.size) {
                                                        Spacer(modifier = Modifier.weight(1f))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            "About" -> {
                                val finalOccupation = if (isOwnProfile) editOccupation.ifBlank { displayOccupation } else (externalProfile?.occupation ?: "Digital Creator")
                                val finalEducation = if (isOwnProfile) editEducation.ifBlank { "Studied at University" } else "Studied at University"
                                val finalLocation = if (isOwnProfile) editLocation.ifBlank { displayLocation } else (externalProfile?.location ?: "Dhaka, Bangladesh")
                                val finalPhone = if (isOwnProfile) editPhone.ifBlank { currentUser?.emailOrPhone?.takeIf { it.contains("+") || it.any { c -> c.isDigit() } } ?: "" } else (externalProfile?.phone ?: "")
                                val finalEmail = if (isOwnProfile) editEmail.ifBlank { currentUser?.emailOrPhone?.takeIf { it.contains("@") } ?: "" } else (externalProfile?.email ?: "")
                                val finalDob = if (isOwnProfile) editDob.ifBlank { currentUser?.dob ?: "" } else (externalProfile?.dob ?: "")
                                val finalGender = if (isOwnProfile) editGender.ifBlank { currentUser?.gender ?: "" } else (externalProfile?.gender ?: "")
                                val finalWebsite = if (isOwnProfile) editWebsite.ifBlank { "https://flikata.cyou" } else "https://flikata.cyou"

                                val externalAbout = externalProfile?.aboutSettings
                                val showOccupation = (if (isOwnProfile) showOccupationInAbout else (externalAbout?.get("show_occupation") ?: true)) && finalOccupation.isNotBlank()
                                val showEducation = (if (isOwnProfile) showEducationInAbout else (externalAbout?.get("show_education") ?: true)) && finalEducation.isNotBlank()
                                val showLocation = (if (isOwnProfile) showLocationInAbout else (externalAbout?.get("show_location") ?: true)) && finalLocation.isNotBlank()
                                val showPhone = (if (isOwnProfile) showPhoneInAbout else (externalAbout?.get("show_phone") ?: false)) && finalPhone.isNotBlank()
                                val showEmail = (if (isOwnProfile) showEmailInAbout else (externalAbout?.get("show_email") ?: false)) && finalEmail.isNotBlank()
                                val showDob = (if (isOwnProfile) showBirthdayInAbout else (externalAbout?.get("show_dob") ?: false)) && finalDob.isNotBlank()
                                val showGender = (if (isOwnProfile) showGenderInAbout else (externalAbout?.get("show_gender") ?: true)) && finalGender.isNotBlank()
                                val showWebsite = (if (isOwnProfile) showWebsiteInAbout else (externalAbout?.get("show_website") ?: true)) && finalWebsite.isNotBlank()

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Section Header Card
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "About & Intro",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 17.sp,
                                                color = textPrimary
                                            )
                                            Text(
                                                text = if (isOwnProfile) "Configured via Edit Profile toggles" else "Public profile details",
                                                fontSize = 12.sp,
                                                color = textSecondary
                                            )
                                        }

                                        if (isOwnProfile) {
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = FlikataPrimary.copy(alpha = 0.12f),
                                                modifier = Modifier.clickable { viewModel.prepareEditProfile() }
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = FlikataPrimary, modifier = Modifier.size(15.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(text = "Edit", color = FlikataPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                }
                                            }
                                        }
                                    }

                                    // Render each card if toggled ON and non-empty
                                    if (showOccupation) {
                                        AboutDetailCard(
                                            icon = Icons.Default.Work,
                                            label = "Work / Occupation",
                                            value = finalOccupation
                                        )
                                    }

                                    if (showEducation) {
                                        AboutDetailCard(
                                            icon = Icons.Default.School,
                                            label = "Education",
                                            value = finalEducation
                                        )
                                    }

                                    if (showLocation) {
                                        AboutDetailCard(
                                            icon = Icons.Default.LocationOn,
                                            label = "Lives in / Location",
                                            value = finalLocation
                                        )
                                    }

                                    if (showPhone) {
                                        AboutDetailCard(
                                            icon = Icons.Default.Call,
                                            label = "Phone Number",
                                            value = finalPhone,
                                            actionText = "Copy",
                                            onActionClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("Phone", finalPhone))
                                                Toast.makeText(context, "Phone number copied!", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }

                                    if (showEmail) {
                                        AboutDetailCard(
                                            icon = Icons.Default.Email,
                                            label = "Email Address",
                                            value = finalEmail,
                                            actionText = "Copy",
                                            onActionClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("Email", finalEmail))
                                                Toast.makeText(context, "Email address copied!", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }

                                    if (showDob) {
                                        AboutDetailCard(
                                            icon = Icons.Default.Cake,
                                            label = "Date of Birth",
                                            value = finalDob
                                        )
                                    }

                                    if (showGender) {
                                        AboutDetailCard(
                                            icon = Icons.Default.Person,
                                            label = "Gender",
                                            value = finalGender
                                        )
                                    }

                                    if (showWebsite) {
                                        AboutDetailCard(
                                            icon = Icons.Default.Language,
                                            label = "Website / Link",
                                            value = finalWebsite,
                                            actionText = "Visit",
                                            onActionClick = {
                                                try {
                                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(finalWebsite))
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    clipboard.setPrimaryClip(ClipData.newPlainText("Website", finalWebsite))
                                                    Toast.makeText(context, "Website link copied!", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        )
                                    }

                                    // Joined Date is always displayed
                                    AboutDetailCard(
                                        icon = Icons.Default.CalendarMonth,
                                        label = "Member Since",
                                        value = displayJoinedDate
                                    )

                                    val anyCustomShown = showOccupation || showEducation || showLocation || showPhone || showEmail || showDob || showGender || showWebsite
                                    if (!anyCustomShown && isOwnProfile) {
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = "No custom details enabled in About section.",
                                                    fontSize = 13.sp,
                                                    color = textSecondary
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Tap Edit Profile and toggle options ON to show them here.",
                                                    fontSize = 12.sp,
                                                    color = FlikataPrimary,
                                                    fontWeight = FontWeight.SemiBold,
                                                    modifier = Modifier.clickable { viewModel.prepareEditProfile() }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            else -> {
                                val realMonthlyViews = remember(userPosts, userReels) {
                                    val postViews = userPosts.sumOf { it.viewsCount }
                                    val reelViews = userReels.sumOf { it.viewsCount.toIntOrNull() ?: 0 }
                                    postViews + reelViews
                                }
                                val formattedViews = if (realMonthlyViews >= 1000) {
                                    String.format(java.util.Locale.US, "%.1fK", realMonthlyViews / 1000.0)
                                } else {
                                    "$realMonthlyViews"
                                }

                                val realMonthlyLikes = remember(userPosts, userReels) {
                                    userPosts.sumOf { it.likesCount } + userReels.sumOf { it.likesCount }
                                }
                                val formattedLikes = if (realMonthlyLikes >= 1000) {
                                    String.format(java.util.Locale.US, "%.1fK", realMonthlyLikes / 1000.0)
                                } else {
                                    "$realMonthlyLikes"
                                }

                                val realEngagement = remember(userPosts, userReels, realMonthlyViews, realMonthlyLikes) {
                                    val totalInteractions = realMonthlyLikes + userPosts.sumOf { it.commentsCount } + userReels.sumOf { it.commentsCount }
                                    if (realMonthlyViews > 0) {
                                        val rate = (totalInteractions.toDouble() / realMonthlyViews.toDouble()) * 100.0
                                        String.format(java.util.Locale.US, "%.1f%%", rate)
                                    } else if (totalInteractions > 0) {
                                        "$totalInteractions"
                                    } else {
                                        "0%"
                                    }
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ProfileMetricCard(
                                        icon = Icons.Default.Visibility,
                                        iconColor = Color(0xFF0075FF),
                                        title = "Monthly Visitors",
                                        value = formattedViews
                                    )

                                    ProfileMetricCard(
                                        icon = Icons.Default.Favorite,
                                        iconColor = Color(0xFFE91E63),
                                        title = "Monthly Likes",
                                        value = formattedLikes
                                    )

                                    ProfileMetricCard(
                                        icon = Icons.Default.TrendingUp,
                                        iconColor = Color(0xFF00C853),
                                        title = "Monthly Engagement",
                                        value = realEngagement
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

    // Modal Bottom Sheet for Profile 3-Dots Settings matching Screenshot 3
    if (showSettingsSheet) {
        ProfileSettingsBottomSheet(
            sheetState = settingsSheetState,
            isOwnProfile = isOwnProfile,
            onDismiss = { showSettingsSheet = false },
            onOptionClick = { option ->
                if (option.equals("Dashboard", ignoreCase = true)) {
                    viewModel.navigateTo(Screen.DASHBOARD)
                } else if (option == "Copy Link" || option.contains("Link", ignoreCase = true)) {
                    val targetUserId = if (isOwnProfile) (currentUser?.id ?: "") else (selectedUserProfileId ?: "")
                    val profileUrl = com.example.data.remote.SecureConfig.getProfileUrl(targetUserId)
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Flikata Profile Link", profileUrl)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Profile link copied to clipboard!", Toast.LENGTH_SHORT).show()
                } else if (option == "Verified Badge" || option == "Verified") {
                    viewModel.navigateTo(Screen.VERIFIED_BADGE)
                } else if (option == "Share Profile") {
                    val targetUserId = if (isOwnProfile) (currentUser?.id ?: "") else (selectedUserProfileId ?: "")
                    val profileUrl = com.example.data.remote.SecureConfig.getProfileUrl(targetUserId)
                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_TEXT, "Check out this profile on Flikata:\n$profileUrl")
                    }
                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Profile"))
                } else if (option.equals("Pages", ignoreCase = true)) {
                    Toast.makeText(context, "Pages coming soon!", Toast.LENGTH_SHORT).show()
                } else if (option.equals("Groups", ignoreCase = true)) {
                    Toast.makeText(context, "Groups coming soon!", Toast.LENGTH_SHORT).show()
                } else if (option.equals("Settings", ignoreCase = true)) {
                    Toast.makeText(context, "Settings coming soon!", Toast.LENGTH_SHORT).show()
                }
            },
            onLogoutClick = { viewModel.logout() }
        )
    }
}

@Composable
private fun AboutDetailCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(FlikataPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = FlikataPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (actionText != null && onActionClick != null) {
                TextButton(onClick = onActionClick) {
                    Text(
                        text = actionText,
                        fontSize = 12.5.sp,
                        color = FlikataPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileMetricCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    value: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun AboutDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = FlikataPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CoverCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}
