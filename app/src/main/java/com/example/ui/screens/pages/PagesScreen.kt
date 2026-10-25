package com.example.ui.screens.pages

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.FlikataPage
import com.example.ui.components.FlikataAvatar
import com.example.ui.components.SoftUITheme
import com.example.ui.components.VerifiedBadge
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

/**
 * Facebook-style Pages Hub Screen
 * Lists all pages managed by the user, active profile switcher,
 * and button to create new pages.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagesScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val pages by viewModel.pages.collectAsState()
    val activePage by viewModel.activePage.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val featureSettings by viewModel.featureSettings.collectAsState()

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

    // Default community pages for discover section
    val discoverPages = remember {
        listOf(
            FlikataPage(
                id = "disc_1",
                name = "Flikata Creators Club",
                category = "Community",
                bio = "Official community for top content creators, video producers and influencers.",
                followersCount = 4280,
                isVerified = true
            ),
            FlikataPage(
                id = "disc_2",
                name = "Dhaka Food & Travel",
                category = "Travel & Food",
                bio = "Discover the best street food, scenic restaurants and secret spots across Bangladesh.",
                followersCount = 1850,
                isVerified = false
            ),
            FlikataPage(
                id = "disc_3",
                name = "Tech Innovators BD",
                category = "Technology",
                bio = "Latest smartphone reviews, programming insights, AI updates and tech discussions.",
                followersCount = 3120,
                isVerified = true
            )
        )
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
                        text = "Pages",
                        style = TextStyle(
                            brush = Brush.horizontalGradient(listOf(Color(0xFF0075FF), Color(0xFF0052D4))),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }

                // "+ Create" Button
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFF0075FF), Color(0xFF0052D4))))
                        .clickable {
                            if (!featureSettings.allowPageCreation || featureSettings.defaultPageCreateLimit == 0) {
                                viewModel.featureNoticeDialog.value = "পেজ তৈরি করার সুবিধা সাময়িকভাবে বন্ধ আছে।"
                            } else {
                                viewModel.navigateTo(Screen.CREATE_PAGE)
                            }
                        }
                        .padding(horizontal = 14.dp)
                        .testTag("pages_create_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Create", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Create",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ==========================================
            // 1. ACTIVE IDENTITY SWITCHER BANNER
            // ==========================================
            item(key = "active_identity_banner") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp), ambientColor = SoftUITheme.ShadowDark)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDarkMode) Color(0xFF1E2128) else Color(0xFFF3F7FD))
                        .border(
                            1.dp,
                            if (isDarkMode) Color(0x22FFFFFF) else Color(0x330075FF),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (activePage != null) {
                                FlikataAvatar(name = activePage!!.name, avatarUrl = activePage!!.logoUrl, size = 42.dp)
                            } else {
                                FlikataAvatar(name = currentUser?.fullName ?: "User", avatarUrl = currentUser?.profilePic, size = 42.dp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (activePage != null) activePage!!.name else (currentUser?.fullName ?: "Personal Account"),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.5.sp,
                                        color = SoftUITheme.TextDark
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF00C853))
                                    )
                                }
                                Text(
                                    text = if (activePage != null) "Currently browsing as Page" else "Currently browsing as Personal Profile",
                                    fontSize = 11.5.sp,
                                    color = SoftUITheme.TextSecondary
                                )
                            }
                        }

                        // Switch profile action button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (activePage != null) Color(0xFFE8F5E9) else Color(0xFFE3F2FD))
                                .clickable {
                                    if (activePage != null) {
                                        viewModel.switchProfile(null)
                                    } else if (pages.isNotEmpty()) {
                                        viewModel.switchProfile(pages.first())
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = "Switch",
                                    tint = if (activePage != null) Color(0xFF2E7D32) else Color(0xFF0075FF),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (activePage != null) "Switch to Personal" else "Switch to Page",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (activePage != null) Color(0xFF2E7D32) else Color(0xFF0075FF)
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 2. PAGES YOU MANAGE
            // ==========================================
            item(key = "section_managed_pages_title") {
                Text(
                    text = "Pages you manage (${pages.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftUITheme.TextDark,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (pages.isEmpty()) {
                item(key = "empty_pages_card") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(18.dp), ambientColor = SoftUITheme.ShadowDark)
                            .clip(RoundedCornerShape(18.dp))
                            .background(cardBg)
                            .border(1.dp, cardBorderBrush, RoundedCornerShape(18.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE3F2FD)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Flag, contentDescription = "Page", tint = Color(0xFF0075FF), modifier = Modifier.size(28.dp))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "You haven't created any pages yet",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftUITheme.TextDark
                            )
                            Text(
                                text = "Create a page to connect with your audience, publish content, and build your brand.",
                                fontSize = 12.sp,
                                color = SoftUITheme.TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Brush.horizontalGradient(listOf(Color(0xFF0075FF), Color(0xFF0052D4))))
                                    .clickable {
                                        if (!featureSettings.allowPageCreation || featureSettings.defaultPageCreateLimit == 0) {
                                            viewModel.featureNoticeDialog.value = "পেজ তৈরি করার সুবিধা সাময়িকভাবে বন্ধ আছে।"
                                        } else {
                                            viewModel.navigateTo(Screen.CREATE_PAGE)
                                        }
                                    }
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Create Your First Page",
                                    color = Color.White,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                items(pages, key = { it.id }) { page ->
                    val isCurrent = activePage?.id == page.id
                    ManagedPageCard(
                        page = page,
                        isCurrent = isCurrent,
                        cardBg = cardBg,
                        cardBorderBrush = cardBorderBrush,
                        onSwitch = { viewModel.switchProfile(if (isCurrent) null else page) },
                        onView = { viewModel.openPageProfile(page) }
                    )
                }
            }

            // ==========================================
            // 3. DISCOVER PAGES
            // ==========================================
            item(key = "section_discover_pages_title") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Explore, contentDescription = "Discover", tint = Color(0xFF0075FF), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Discover Popular Pages",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftUITheme.TextDark
                    )
                }
            }

            items(discoverPages, key = { it.id }) { page ->
                DiscoverPageCard(
                    page = page,
                    cardBg = cardBg,
                    cardBorderBrush = cardBorderBrush,
                    onFollow = { viewModel.followPage(page.id) },
                    onView = { viewModel.openPageProfile(page) }
                )
            }
        }
    }
}

@Composable
private fun ManagedPageCard(
    page: FlikataPage,
    isCurrent: Boolean,
    cardBg: Color,
    cardBorderBrush: Brush,
    onSwitch: () -> Unit,
    onView: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp), ambientColor = SoftUITheme.ShadowDark)
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, cardBorderBrush, RoundedCornerShape(16.dp))
            .clickable(onClick = onView)
            .padding(14.dp)
            .testTag("page_card_${page.id}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!page.logoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = page.logoUrl,
                            contentDescription = page.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        FlikataAvatar(name = page.name, avatarUrl = null, size = 46.dp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = page.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = SoftUITheme.TextDark
                            )
                            if (page.isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                VerifiedBadge(size = 15.dp)
                            }
                        }
                        Text(
                            text = "${page.category} · ${page.followersCount} followers",
                            fontSize = 12.sp,
                            color = SoftUITheme.TextSecondary
                        )
                    }
                }

                if (isCurrent) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE8F5E9))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "Active", color = Color(0xFF2E7D32), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Switch profile button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isCurrent) androidx.compose.ui.graphics.SolidColor(Color(0xFFE8F5E9))
                            else Brush.horizontalGradient(listOf(Color(0xFF0075FF), Color(0xFF0052D4)))
                        )
                        .clickable(onClick = onSwitch),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isCurrent) Icons.Default.Check else Icons.Default.SwapHoriz,
                            contentDescription = "Switch",
                            tint = if (isCurrent) Color(0xFF2E7D32) else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCurrent) "Browsing as Page" else "Switch into Page",
                            color = if (isCurrent) Color(0xFF2E7D32) else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // View Page button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF1F5F9))
                        .clickable(onClick = onView),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Visibility, contentDescription = "View", tint = SoftUITheme.TextDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "View Page", color = SoftUITheme.TextDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscoverPageCard(
    page: FlikataPage,
    cardBg: Color,
    cardBorderBrush: Brush,
    onFollow: () -> Unit,
    onView: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.5.dp, RoundedCornerShape(14.dp), ambientColor = SoftUITheme.ShadowDark)
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(1.dp, cardBorderBrush, RoundedCornerShape(14.dp))
            .clickable(onClick = onView)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                FlikataAvatar(name = page.name, avatarUrl = null, size = 42.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = page.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SoftUITheme.TextDark)
                        if (page.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            VerifiedBadge(size = 14.dp)
                        }
                    }
                    Text(text = "${page.category} · ${page.followersCount} followers", fontSize = 11.5.sp, color = SoftUITheme.TextSecondary)
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE3F2FD))
                    .clickable(onClick = onFollow)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(text = "Follow", color = Color(0xFF0075FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
