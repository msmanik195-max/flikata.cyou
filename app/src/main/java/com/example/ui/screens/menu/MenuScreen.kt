package com.example.ui.screens.menu

import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FlikataAvatar
import com.example.ui.components.SoftUITheme
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.FlikataError
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

/**
 * 3D Soft Neumorphic Menu Screen
 * Cleaned menu layout with Dark Mode card at the top,
 * followed by: Wallet, Marketplace, Verified Badge, History, Saved, Dashboard, Advertisement, Settings,
 * and bottom Connected Server & Logout controls.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    viewModel: MainViewModel,
    onProfileClick: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val activePage by viewModel.activePage.collectAsState()
    val pages by viewModel.pages.collectAsState()
    val context = LocalContext.current
    var isSettingsExpanded by remember { mutableStateOf(false) }
    var showProfileSwitchSheet by remember { mutableStateOf(false) }
    val profileSheetState = rememberModalBottomSheetState()

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
    val headerBgBrush = if (isDarkMode) {
        Brush.verticalGradient(listOf(Color(0xFF242730), Color(0xFF1E2128), Color(0xFF16181E)))
    } else {
        Brush.verticalGradient(listOf(Color.White, Color(0xFFF3F7FD), Color(0xFFE8F1FC)))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBgBrush)
    ) {
        // 3D Neumorphic Top Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (isDarkMode) 2.dp else 1.dp,
                    shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                    ambientColor = if (isDarkMode) Color(0x33000000) else Color(0x0A000000),
                    spotColor = if (isDarkMode) Color(0x44000000) else Color(0x0C000000)
                )
                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                .background(headerBgBrush)
                .border(
                    width = 1.dp,
                    brush = if (isDarkMode) Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x11FFFFFF))) else Brush.verticalGradient(listOf(Color(0xEEFFFFFF), Color(0x1A000000))),
                    shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Circular Back Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .shadow(3.dp, CircleShape, ambientColor = SoftUITheme.ShadowDark)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color.White, Color(0xFFE8F1FC))))
                            .border(1.dp, Color(0xEEFFFFFF), CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { viewModel.navigateTo(Screen.HOME) }
                            )
                            .testTag("menu_back_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1E3A5F),
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Menu",
                        style = TextStyle(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF0075FF), Color(0xFF0052D4))
                            ),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.2.sp
                        )
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Search Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .shadow(3.dp, CircleShape, ambientColor = SoftUITheme.ShadowDark)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color.White, Color(0xFFE8F1FC))))
                            .border(1.dp, Color(0xEEFFFFFF), CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { viewModel.navigateTo(Screen.SEARCH) }
                            )
                            .testTag("menu_search_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF1E3A5F),
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Settings Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .shadow(3.dp, CircleShape, ambientColor = SoftUITheme.ShadowDark)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color.White, Color(0xFFE8F1FC))))
                            .border(1.dp, Color(0xEEFFFFFF), CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    Toast.makeText(context, "Settings is coming soon!", Toast.LENGTH_SHORT).show()
                                }
                            )
                            .testTag("menu_top_settings_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = if (isDarkMode) Color.White else Color(0xFF111418),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 3D Neumorphic User Profile Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 5.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = SoftUITheme.ShadowDark,
                        spotColor = SoftUITheme.ShadowDark
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .background(cardBg)
                    .border(
                        width = 1.dp,
                        brush = cardBorderBrush,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            if (activePage != null) {
                                viewModel.openPageProfile(activePage!!)
                            } else {
                                onProfileClick()
                            }
                        }
                    )
                    .padding(14.dp)
                    .testTag("menu_profile_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .shadow(2.dp, CircleShape, ambientColor = SoftUITheme.ShadowDark)
                                .clip(CircleShape)
                                .background(if (isDarkMode) Color(0xFF1E2128) else Color.White)
                                .border(1.5.dp, if (isDarkMode) Color(0x33FFFFFF) else Color(0x99FFFFFF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (activePage != null) {
                                FlikataAvatar(
                                    name = activePage!!.name,
                                    avatarUrl = activePage!!.logoUrl,
                                    size = 50.dp
                                )
                            } else {
                                FlikataAvatar(
                                    name = currentUser?.fullName ?: "User",
                                    avatarUrl = currentUser?.profilePic,
                                    size = 50.dp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (activePage != null) activePage!!.name else (currentUser?.fullName ?: "User Profile"),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.5.sp,
                                    color = SoftUITheme.TextDark
                                )
                                val isVer = if (activePage != null) activePage!!.isVerified else (currentUser?.isVerified == true)
                                if (isVer) {
                                    Spacer(modifier = Modifier.width(5.dp))
                                    VerifiedBadge(size = 20.dp)
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (activePage != null) "Page Profile · Tap to view page" else "See your profile",
                                fontSize = 12.5.sp,
                                color = Color(0xFF0075FF),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Facebook-style Profile Switcher Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(2.dp, CircleShape)
                            .clip(CircleShape)
                            .background(if (isDarkMode) Color(0xFF2C303A) else Color(0xFFE8F1FC))
                            .clickable { showProfileSwitchSheet = true }
                            .testTag("menu_switch_profile_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Switch Profile",
                            tint = Color(0xFF0075FF),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3D Neumorphic Dark Mode Switch Card (Preserved per request)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(18.dp),
                        ambientColor = SoftUITheme.ShadowDark
                    )
                    .clip(RoundedCornerShape(18.dp))
                    .background(cardBg)
                    .border(
                        1.dp,
                        cardBorderBrush,
                        RoundedCornerShape(18.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("menu_dark_mode_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .shadow(2.dp, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFF3E0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LightMode,
                                contentDescription = "Sun",
                                tint = Color(0xFFF57C00),
                                modifier = Modifier.size(23.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Dark Mode",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.5.sp,
                                color = SoftUITheme.TextDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isDarkMode) "On (Dark Theme)" else "Off (Light Theme)",
                                fontSize = 12.sp,
                                color = SoftUITheme.TextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF0075FF)
                        ),
                        modifier = Modifier.testTag("dark_mode_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section Header
            Text(
                text = "Shortcuts",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = SoftUITheme.TextDark,
                modifier = Modifier.padding(bottom = 12.dp, start = 2.dp)
            )

            // 8 Clean Cards in 2-column grid
            // Order:
            // 1. Wallet (First!)
            // 2. Marketplace (Coming Soon)
            // 3. Verified Badge
            // 4. History (Coming Soon)
            // 5. Saved (Coming Soon)
            // 6. Dashboard (Coming Soon)
            // 7. Advertisement (Coming Soon)
            // 8. Settings (Coming Soon)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Row 1: Wallet & Marketplace
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MenuShortcutCard(
                        icon = Icons.Default.AccountBalanceWallet,
                        title = "Wallet",
                        subtitle = "Balance & transactions",
                        iconBg = Color(0xFFE8F5E9),
                        iconTint = Color(0xFF2E7D32),
                        badgeText = null,
                        testTag = "menu_card_wallet",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.WALLET) }
                    )
                    MenuShortcutCard(
                        icon = Icons.Default.Storefront,
                        title = "Marketplace",
                        subtitle = "Buy & sell items",
                        iconBg = Color(0xFFE0F2FE),
                        iconTint = Color(0xFF0284C7),
                        badgeText = null,
                        testTag = "menu_card_marketplace",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.MARKETPLACE) }
                    )
                }

                // Row 2: Verified Badge & History
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MenuShortcutCard(
                        icon = Icons.Default.Verified,
                        title = "Verified Badge",
                        subtitle = "Official blue badge & status",
                        iconBg = Color(0xFFE3F2FD),
                        iconTint = Color(0xFF0075FF),
                        badgeText = null,
                        testTag = "menu_card_verified_badge",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.VERIFIED_BADGE) }
                    )
                    MenuShortcutCard(
                        icon = Icons.Default.History,
                        title = "History",
                        subtitle = "Activity log & views",
                        iconBg = Color(0xFFEDE7F6),
                        iconTint = Color(0xFF673AB7),
                        badgeText = null,
                        testTag = "menu_card_history",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.HISTORY) }
                    )
                }

                // Row 3: Saved & Dashboard
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MenuShortcutCard(
                        icon = Icons.Default.Bookmark,
                        title = "Saved",
                        subtitle = "Saved posts & bookmarks",
                        iconBg = Color(0xFFE0F7FA),
                        iconTint = Color(0xFF00897B),
                        badgeText = null,
                        testTag = "menu_card_saved",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.SAVED) }
                    )
                    MenuShortcutCard(
                        icon = Icons.Default.Dashboard,
                        title = "Dashboard",
                        subtitle = "Analytics",
                        iconBg = Color(0xFFFFF3E0),
                        iconTint = Color(0xFFE65100),
                        badgeText = null,
                        testTag = "menu_card_dashboard",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.navigateTo(Screen.DASHBOARD)
                        }
                    )
                }

                // Row 4: Advertisement & Pages
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MenuShortcutCard(
                        icon = Icons.Default.Campaign,
                        title = "Advertisement",
                        subtitle = "Promote & grow audience",
                        iconBg = Color(0xFFFFEBEE),
                        iconTint = Color(0xFFC2185B),
                        badgeText = "Coming Soon",
                        testTag = "menu_card_advertisement",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            Toast.makeText(context, "Ad Manager is coming soon!", Toast.LENGTH_SHORT).show()
                        }
                    )
                    MenuShortcutCard(
                        icon = Icons.Default.Flag,
                        title = "Pages",
                        subtitle = "Create & manage pages",
                        iconBg = Color(0xFFFFF8E1),
                        iconTint = Color(0xFFF57F17),
                        badgeText = if (pages.isNotEmpty()) "${pages.size}" else null,
                        testTag = "menu_card_pages",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.navigateTo(Screen.PAGES)
                        }
                    )
                }

                // Row 5: Leaderboard
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MenuShortcutCard(
                        icon = Icons.Default.EmojiEvents,
                        title = "Leaderboard",
                        subtitle = "Top active community leaders",
                        iconBg = Color(0xFFFFF8E1),
                        iconTint = Color(0xFFF59E0B),
                        badgeText = "Top Rankings",
                        testTag = "menu_card_leaderboard",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            viewModel.navigateTo(Screen.LEADERBOARD)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Expandable Settings Card (Full-width like Log Out Button with Dropdown Chevron)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (isDarkMode) 2.dp else 2.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = SoftUITheme.ShadowDark
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isDarkMode) Color(0xFF242730) else Color.White)
                    .border(
                        width = 1.dp,
                        brush = if (isDarkMode) Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x11FFFFFF)))
                                else Brush.verticalGradient(listOf(Color(0xEEFFFFFF), Color(0x33A0C0E8))),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .testTag("menu_settings_bar")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { isSettingsExpanded = !isSettingsExpanded }
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isDarkMode) Color(0xFF333842) else Color(0xFFECEFF1)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = if (isDarkMode) Color.White else Color(0xFF111418),
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Settings & Privacy",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isDarkMode) Color.White else Color(0xFF111418)
                            )
                        }

                        // Dropdown Chevron
                        Icon(
                            imageVector = if (isSettingsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isSettingsExpanded) "Collapse" else "Expand",
                            tint = SoftUITheme.TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Dropdown Options
                    AnimatedVisibility(
                        visible = isSettingsExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                                .padding(bottom = 8.dp)
                        ) {
                            HorizontalDivider(
                                color = if (isDarkMode) Color(0x22FFFFFF) else Color(0x1F000000),
                                thickness = 0.8.dp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            val settingsOptions = listOf(
                                Triple("Settings", Icons.Default.Settings, "Account & app preferences"),
                                Triple("Privacy Policy", Icons.Default.Security, "Data & privacy terms"),
                                Triple("About", Icons.Default.Info, "Version & app details"),
                                Triple("Support Center", Icons.Default.HelpOutline, "Help, FAQ & contact")
                            )

                            settingsOptions.forEach { (title, icon, subtitle) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            Toast.makeText(context, "$title is coming soon!", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(horizontal = 8.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(if (isDarkMode) Color(0xFF2E323D) else Color(0xFFF1F3F5)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = title,
                                                tint = if (isDarkMode) Color.White else Color(0xFF111418),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = title,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp,
                                                color = if (isDarkMode) Color.White else Color(0xFF111418)
                                            )
                                            Text(
                                                text = subtitle,
                                                fontSize = 11.sp,
                                                color = SoftUITheme.TextSecondary
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isDarkMode) Color(0xFF2A2E39) else Color(0xFFF1F3F5))
                                            .padding(horizontal = 7.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "Coming Soon",
                                            color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B),
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3D Neumorphic Log Out Button (Preserved per request)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .shadow(
                        elevation = if (isDarkMode) 2.dp else 3.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = Color(0x44FF3B30)
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isDarkMode) Color(0xFF2C1E20) else Color(0xFFFFF0F1))
                    .border(
                        width = 1.2.dp,
                        brush = Brush.verticalGradient(
                            listOf(if (isDarkMode) Color(0x33FF3B30) else Color.White, Color(0x66FF3B30))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { viewModel.logout() }
                    )
                    .testTag("menu_logout_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Log Out",
                        tint = FlikataError,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Log Out",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = FlikataError
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }

    // Facebook-style Profile & Page Switcher Bottom Sheet
    if (showProfileSwitchSheet) {
        ModalBottomSheet(
            onDismissRequest = { showProfileSwitchSheet = false },
            sheetState = profileSheetState,
            containerColor = if (isDarkMode) Color(0xFF1E2128) else Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .padding(bottom = 28.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Switch Profile",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftUITheme.TextDark
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE3F2FD))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Facebook Style",
                            fontSize = 11.sp,
                            color = Color(0xFF0075FF),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Personal Account Option
                val isPersonalActive = activePage == null
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(if (isPersonalActive) 1.dp else 0.dp, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isPersonalActive) (if (isDarkMode) Color(0xFF282C37) else Color(0xFFF0F5FA)) else Color.Transparent)
                        .border(
                            width = 1.dp,
                            color = if (isPersonalActive) Color(0xFF0075FF).copy(alpha = 0.5f) else Color.Transparent,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            viewModel.switchProfile(null)
                            Toast.makeText(context, "Switched to ${currentUser?.fullName ?: "Personal Profile"}", Toast.LENGTH_SHORT).show()
                            showProfileSwitchSheet = false
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        FlikataAvatar(name = currentUser?.fullName ?: "User", avatarUrl = currentUser?.profilePic, size = 44.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentUser?.fullName ?: "Personal Profile",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = SoftUITheme.TextDark
                                )
                                if (currentUser?.isVerified == true) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    VerifiedBadge(size = 16.dp)
                                }
                            }
                            Text(
                                text = "Personal Profile",
                                fontSize = 12.sp,
                                color = SoftUITheme.TextSecondary
                            )
                        }
                    }
                    if (isPersonalActive) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00C853)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Active", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                if (pages.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Pages you manage (${pages.size})",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftUITheme.TextSecondary,
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                    )

                    pages.forEach { p ->
                        val isThisActive = activePage?.id == p.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(if (isThisActive) 1.dp else 0.dp, RoundedCornerShape(14.dp))
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isThisActive) (if (isDarkMode) Color(0xFF282C37) else Color(0xFFF0F5FA)) else Color.Transparent)
                                .border(
                                    width = 1.dp,
                                    color = if (isThisActive) Color(0xFF0075FF).copy(alpha = 0.5f) else Color.Transparent,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    viewModel.switchProfile(p)
                                    Toast.makeText(context, "Switched to ${p.name}", Toast.LENGTH_SHORT).show()
                                    showProfileSwitchSheet = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                FlikataAvatar(name = p.name, avatarUrl = p.logoUrl, size = 44.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = p.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = SoftUITheme.TextDark
                                        )
                                        if (p.isVerified) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            VerifiedBadge(size = 16.dp)
                                        }
                                    }
                                    Text(
                                        text = "${p.category} · ${p.followersCount} followers",
                                        fontSize = 12.sp,
                                        color = SoftUITheme.TextSecondary
                                    )
                                }
                            }
                            if (isThisActive) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00C853)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Active", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Create Page Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isDarkMode) Color(0xFF242730) else Color(0xFFE8F1FC))
                        .border(1.dp, if (isDarkMode) Color(0x33FFFFFF) else Color(0x330075FF), RoundedCornerShape(14.dp))
                        .clickable {
                            showProfileSwitchSheet = false
                            viewModel.navigateTo(Screen.CREATE_PAGE)
                        }
                        .padding(vertical = 12.dp, horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Brush.horizontalGradient(listOf(Color(0xFF0075FF), Color(0xFF0052D4)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Create", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Create New Page",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp,
                            color = Color(0xFF0075FF)
                        )
                        Text(
                            text = "Build an audience for your creator brand or business",
                            fontSize = 12.sp,
                            color = SoftUITheme.TextSecondary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Modern 3D Neumorphic Shortcut Card with optional "Coming Soon" badge
 */
@Composable
private fun MenuShortcutCard(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    iconBg: Color,
    iconTint: Color,
    badgeText: String? = null,
    testTag: String = "",
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val isDark = SoftUITheme.isDark
    val cardBg = if (isDark) Color(0xFF242730) else Color.White
    val cardBorderBrush = if (isDark) {
        Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x11FFFFFF)))
    } else {
        Brush.verticalGradient(listOf(Color(0xEEFFFFFF), Color(0x33A0C0E8)))
    }
    val badgeBg = if (isDark) Color(0xFF1E2128) else iconBg

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDark) 3.dp else 2.dp,
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
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(14.dp)
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // 3D Soft Icon Badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(2.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(badgeBg)
                        .border(1.dp, if (isDark) Color(0x22FFFFFF) else Color(0x44FFFFFF), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(23.dp)
                    )
                }

                // "Coming Soon" Pill Badge
                if (!badgeText.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isDark) Color(0xFF352B12) else Color(0xFFFFF3E0)
                            )
                            .border(
                                0.8.dp,
                                if (isDark) Color(0x88FFB300) else Color(0xAAFFB74D),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.5.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) Color(0xFFFFB300) else Color(0xFFE65100),
                            letterSpacing = 0.3.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = SoftUITheme.TextDark
            )

            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.5.sp,
                    color = SoftUITheme.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
