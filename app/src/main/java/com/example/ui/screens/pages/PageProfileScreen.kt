package com.example.ui.screens.pages

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.FlikataPage
import com.example.ui.components.FlikataAvatar
import com.example.ui.components.PostCard
import com.example.ui.components.SoftUITheme
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.FlikataPrimary
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

/**
 * Facebook-style Page Profile Screen
 * Displays Cover Banner, Logo, Name, Verified Badge, Followers,
 * Switch into Page profile action, Create Post as Page, and Tab content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageProfileScreen(
    page: FlikataPage,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val activePage by viewModel.activePage.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val allPosts by viewModel.posts.collectAsState()
    val context = LocalContext.current

    val isSwitchedIntoThisPage = activePage?.id == page.id
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Posts", "About", "Photos", "Videos")

    // Photo & Video pickers for posting as this Page
    val pagePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.switchProfile(page)
            val cached = viewModel.cacheMediaFromUri(uri, false)
            viewModel.postSelectedImageUrl.value = cached
            viewModel.postSelectedIsVideo.value = false
            viewModel.navigateTo(com.example.ui.viewmodel.Screen.CREATE_POST)
        }
    }

    val pageVideoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.switchProfile(page)
            val cached = viewModel.cacheMediaFromUri(uri, true)
            viewModel.postSelectedVideoUrl.value = cached
            viewModel.postSelectedIsVideo.value = true
            viewModel.navigateTo(com.example.ui.viewmodel.Screen.CREATE_POST)
        }
    }

    // Filter posts for this page
    val pagePosts = remember(allPosts, page.id, page.name) {
        allPosts.filter { it.pageId == page.id || it.userId == "page_${page.id}" || (it.authorName == page.name && it.userId.startsWith("page_")) }
    }

    val screenBgBrush = if (isDarkMode) {
        Brush.verticalGradient(listOf(Color(0xFF121418), Color(0xFF181A20)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFF4F8FD), Color(0xFFE8F1FC)))
    }

    val cardBg = if (isDarkMode) Color(0xFF242730) else Color.White
    val cardBorderBrush = if (isDarkMode) {
        Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x11FFFFFF)))
    } else {
        Brush.verticalGradient(listOf(Color(0xEEFFFFFF), Color(0x33A0C0E8)))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBgBrush)
    ) {
        // Top App Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (isDarkMode) 2.dp else 1.dp,
                    shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                    ambientColor = SoftUITheme.ShadowDark
                )
                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                .background(if (isDarkMode) Color(0xFF1E2128) else Color.White)
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .shadow(2.dp, CircleShape, ambientColor = SoftUITheme.ShadowDark)
                            .clip(CircleShape)
                            .background(if (isDarkMode) Color(0xFF2C303A) else Color(0xFFF0F4F8))
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SoftUITheme.TextDark,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = page.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftUITheme.TextDark
                    )
                }

                // Switch profile button on top right
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSwitchedIntoThisPage) Color(0xFFE8F5E9) else Color(0xFFE3F2FD))
                        .clickable {
                            if (isSwitchedIntoThisPage) {
                                viewModel.switchProfile(null)
                            } else {
                                viewModel.switchProfile(page)
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Switch",
                            tint = if (isSwitchedIntoThisPage) Color(0xFF2E7D32) else Color(0xFF0075FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isSwitchedIntoThisPage) "Active" else "Switch",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSwitchedIntoThisPage) Color(0xFF2E7D32) else Color(0xFF0075FF)
                        )
                    }
                }
            }
        }

        // Scrollable Body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 70.dp)
        ) {
            // Visual Banner & Logo Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            ) {
                // Cover Banner Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(
                            Brush.linearGradient(
                                if (isDarkMode) listOf(Color(0xFF2C3240), Color(0xFF1E222A))
                                else listOf(Color(0xFF0075FF), Color(0xFF00C6FF))
                            )
                        )
                ) {
                    if (!page.bannerUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = page.bannerUrl,
                            contentDescription = "Cover",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Circular Page Logo Avatar
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = 18.dp, y = 0.dp)
                        .size(88.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(if (isDarkMode) Color(0xFF1E2128) else Color.White)
                        .border(3.dp, if (isDarkMode) Color(0xFF242730) else Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!page.logoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = page.logoUrl,
                            contentDescription = page.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        FlikataAvatar(name = page.name, avatarUrl = null, size = 84.dp)
                    }
                }
            }

            // Page Info Header Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = page.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SoftUITheme.TextDark
                    )
                    if (page.isVerified) {
                        Spacer(modifier = Modifier.width(6.dp))
                        VerifiedBadge(size = 20.dp)
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Category & Stats
                Text(
                    text = "${page.category} · ${page.followersCount} followers · ${page.likesCount} likes",
                    fontSize = 13.sp,
                    color = SoftUITheme.TextSecondary,
                    fontWeight = FontWeight.Medium
                )

                if (page.bio.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = page.bio,
                        fontSize = 13.5.sp,
                        color = SoftUITheme.TextDark,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ==========================================
                // FACEBOOK-STYLE PRIMARY ACTIONS BAR
                // ==========================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Switch into Page button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .shadow(2.dp, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSwitchedIntoThisPage) {
                                    Brush.horizontalGradient(listOf(Color(0xFF2E7D32), Color(0xFF388E3C)))
                                } else {
                                    Brush.horizontalGradient(listOf(Color(0xFF0075FF), Color(0xFF0052D4)))
                                }
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    if (isSwitchedIntoThisPage) {
                                        viewModel.switchProfile(null)
                                    } else {
                                        viewModel.switchProfile(page)
                                    }
                                }
                            )
                            .testTag("page_switch_profile_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isSwitchedIntoThisPage) Icons.Default.Check else Icons.Default.SwapHoriz,
                                contentDescription = "Switch",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSwitchedIntoThisPage) "Browsing as Page" else "Switch into Page",
                                color = Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Create Post button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .shadow(1.dp, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDarkMode) Color(0xFF2C3240) else Color(0xFFF0F4F8))
                            .border(1.dp, if (isDarkMode) Color(0x33FFFFFF) else Color(0x33A0C0E8), RoundedCornerShape(12.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    viewModel.switchProfile(page)
                                    viewModel.navigateTo(Screen.CREATE_POST)
                                }
                            )
                            .testTag("page_create_post_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PostAdd,
                                contentDescription = "Post",
                                tint = Color(0xFF0075FF),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Create Post",
                                color = SoftUITheme.TextDark,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Media Actions Row (Photo, Reel / Video, Switch Back)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDarkMode) Color(0xFF282C37) else Color(0xFFE8F1FC))
                            .clickable {
                                pagePhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = "Photo", tint = Color(0xFF0075FF), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Photo", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = SoftUITheme.TextDark)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDarkMode) Color(0xFF282C37) else Color(0xFFE8F1FC))
                            .clickable {
                                pageVideoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VideoLibrary, contentDescription = "Video", tint = Color(0xFFE91E63), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Video", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = SoftUITheme.TextDark)
                        }
                    }

                    if (isSwitchedIntoThisPage) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF00C853).copy(alpha = 0.15f))
                                .border(1.dp, Color(0xFF00C853), RoundedCornerShape(10.dp))
                                .clickable {
                                    viewModel.switchProfile(null)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = "Personal", tint = Color(0xFF00C853), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("To Profile", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00C853))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ==========================================
            // TABS (Posts, About, Photos)
            // ==========================================
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = cardBg,
                contentColor = FlikataPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = FlikataPrimary
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // Posts Tab
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Composer prompt card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(14.dp), ambientColor = SoftUITheme.ShadowDark)
                                .clip(RoundedCornerShape(14.dp))
                                .background(cardBg)
                                .border(1.dp, cardBorderBrush, RoundedCornerShape(14.dp))
                                .clickable {
                                    viewModel.switchProfile(page)
                                    viewModel.navigateTo(Screen.CREATE_POST)
                                }
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FlikataAvatar(name = page.name, avatarUrl = page.logoUrl, size = 38.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Write something as ${page.name}...",
                                    color = SoftUITheme.TextSecondary,
                                    fontSize = 13.5.sp
                                )
                            }
                        }

                        if (pagePosts.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 36.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.PostAdd,
                                        contentDescription = "No posts",
                                        tint = SoftUITheme.TextSecondary,
                                        modifier = Modifier.size(46.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "No posts yet by this Page",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftUITheme.TextDark
                                    )
                                    Text(
                                        text = "Posts created while switched into this page will appear here.",
                                        fontSize = 12.sp,
                                        color = SoftUITheme.TextSecondary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.padding(top = 4.dp, start = 20.dp, end = 20.dp)
                                    )
                                }
                            }
                        } else {
                            for (post in pagePosts) {
                                PostCard(
                                    post = post,
                                    currentUser = currentUser,
                                    onLikeClick = { viewModel.toggleLike(it) },
                                    onReactionSelect = { postId, emoji -> viewModel.setReaction(postId, emoji) },
                                    onImageClick = { imageUrl -> viewModel.openImageViewer(imageUrl) },
                                    onDeletePost = { postId -> viewModel.deletePost(postId) },
                                    onEditPost = { postId, content, privacy -> viewModel.editPost(postId, content, privacy) },
                                    onAddComment = { postId, text -> viewModel.addComment(postId, text) },
                                    onUserClick = { _, _ -> },
                                    onSavePost = { viewModel.toggleSavePost(it) }
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // About Tab
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(16.dp), ambientColor = SoftUITheme.ShadowDark)
                                .clip(RoundedCornerShape(16.dp))
                                .background(cardBg)
                                .border(1.dp, cardBorderBrush, RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "About This Page",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = SoftUITheme.TextDark
                                )

                                if (page.bio.isNotBlank()) {
                                    Text(
                                        text = page.bio,
                                        fontSize = 13.5.sp,
                                        color = SoftUITheme.TextDark,
                                        lineHeight = 19.sp
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Flag, contentDescription = "Category", tint = Color(0xFF0075FF), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(text = "Category: ${page.category}", fontSize = 13.5.sp, color = SoftUITheme.TextDark)
                                }

                                if (!page.website.isNullOrBlank()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(page.website))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Invalid website link", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    ) {
                                        Icon(imageVector = Icons.Default.Language, contentDescription = "Website", tint = Color(0xFF0075FF), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(text = page.website, fontSize = 13.5.sp, color = Color(0xFF0075FF), fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                if (!page.email.isNullOrBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Email, contentDescription = "Email", tint = Color(0xFF0075FF), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(text = page.email, fontSize = 13.5.sp, color = SoftUITheme.TextDark)
                                    }
                                }

                                if (!page.phone.isNullOrBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Phone, contentDescription = "Phone", tint = Color(0xFF0075FF), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(text = page.phone, fontSize = 13.5.sp, color = SoftUITheme.TextDark)
                                    }
                                }

                                Text(
                                    text = "Page created on Flikata · ${page.createdAt}",
                                    fontSize = 11.5.sp,
                                    color = SoftUITheme.TextSecondary
                                )
                            }
                        }
                    }
                }

                2 -> {
                    // Photos Tab
                    val pagePhotos = remember(pagePosts) { pagePosts.mapNotNull { it.imageUrl }.filter { it.isNotBlank() } }
                    if (pagePhotos.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No photos uploaded yet by ${page.name}",
                                color = SoftUITheme.TextSecondary,
                                fontSize = 13.5.sp
                            )
                        }
                    } else {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "${pagePhotos.size} Photos",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = SoftUITheme.TextDark
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                                columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth().height(300.dp)
                            ) {
                                items(pagePhotos.size) { idx ->
                                    Box(
                                        modifier = Modifier
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isDarkMode) Color(0xFF1E2128) else Color(0xFFF1F5F9))
                                    ) {
                                        AsyncImage(
                                            model = pagePhotos[idx],
                                            contentDescription = "Photo",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // Videos / Reels Tab
                    val pageVideos = remember(pagePosts) { pagePosts.filter { it.isVideo || !it.videoUrl.isNullOrBlank() } }
                    if (pageVideos.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No videos or reels posted yet by ${page.name}",
                                color = SoftUITheme.TextSecondary,
                                fontSize = 13.5.sp
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            pageVideos.forEach { videoPost ->
                                PostCard(
                                    post = videoPost,
                                    currentUser = currentUser,
                                    onLikeClick = { viewModel.toggleLike(it) },
                                    onReactionSelect = { postId, emoji -> viewModel.setReaction(postId, emoji) },
                                    onImageClick = { imageUrl -> viewModel.openImageViewer(imageUrl) },
                                    onDeletePost = { postId -> viewModel.deletePost(postId) },
                                    onEditPost = { postId, content, privacy -> viewModel.editPost(postId, content, privacy) },
                                    onAddComment = { postId, text -> viewModel.addComment(postId, text) },
                                    onUserClick = { _, _ -> },
                                    onSavePost = { viewModel.toggleSavePost(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
