package com.example

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.ui.components.AppUpdateView
import com.example.ui.components.FlikataBottomNav
import com.example.ui.screens.auth.ForgotPasswordScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.RegisterScreen
import com.example.ui.screens.chats.ChatDetailScreen
import com.example.ui.screens.chats.ChatsScreen
import com.example.ui.screens.chats.IncomingCallOverlay
import com.example.ui.screens.chats.ActiveAudioCallOverlay
import com.example.ui.screens.createpost.CreatePostScreen
import com.example.ui.screens.friends.FriendsScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.menu.MenuScreen
import com.example.ui.screens.notifications.NotificationScreen
import com.example.ui.screens.profile.EditProfileScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.reels.ReelsScreen
import com.example.ui.screens.search.SearchScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.screens.story.CreateStoryScreen
import com.example.ui.screens.story.StoryViewerScreen
import com.example.ui.screens.viewer.FullImageViewerScreen
import com.example.ui.screens.admin.AdminPanelScreen
import com.example.ui.screens.leaderboard.LeaderboardScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.marketplace.MarketplaceScreen
import com.example.ui.screens.verified.VerifiedBadgeScreen
import com.example.ui.screens.history.HistoryScreen
import com.example.ui.screens.saved.SavedScreen
import com.example.ui.screens.pages.CreatePageScreen
import com.example.ui.screens.pages.PageProfileScreen
import com.example.ui.screens.pages.PagesScreen
import com.example.ui.screens.wallet.DepositScreen
import com.example.ui.screens.wallet.WalletScreen
import com.example.ui.screens.wallet.WithdrawScreen
import com.example.ui.theme.FlikataBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleDeepLink(intent)
        try {
            enableEdgeToEdge()
        } catch (e: Throwable) {
            android.util.Log.w("MainActivity", "enableEdgeToEdge not fully supported on this OS: ${e.message}")
        }

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()

            val isSplashScreen = currentScreen == Screen.SPLASH
            val isTransparentStatusBar = currentScreen == Screen.PROFILE || currentScreen == Screen.REELS || currentScreen == Screen.STORY_VIEWER || currentScreen == Screen.CREATE_STORY

            // Update status bar & nav bar icon colors according to theme safely
            try {
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                if (isSplashScreen) {
                    controller.isAppearanceLightStatusBars = false // Crisp white icons on black status bar
                    window.statusBarColor = AndroidColor.BLACK // Black status bar on Splash Screen
                } else {
                    controller.isAppearanceLightStatusBars = if (isTransparentStatusBar) false else !isDarkMode
                    window.statusBarColor = if (isTransparentStatusBar) {
                        AndroidColor.TRANSPARENT
                    } else if (isDarkMode) {
                        AndroidColor.parseColor("#18191A")
                    } else {
                        AndroidColor.WHITE
                    }
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    controller.isAppearanceLightNavigationBars = if (isSplashScreen) false else !isDarkMode
                    window.navigationBarColor = if (isSplashScreen) AndroidColor.BLACK else if (isDarkMode) AndroidColor.parseColor("#18191A") else AndroidColor.WHITE
                } else {
                    window.navigationBarColor = AndroidColor.BLACK
                }
            } catch (e: Throwable) {
                android.util.Log.w("MainActivity", "Window decor adjustment error: ${e.message}")
            }

            MyApplicationTheme(darkTheme = isDarkMode) {
                MainAppContent(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkForAppUpdates()
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: android.content.Intent?) {
        val data = intent?.data ?: return
        val path = data.path ?: ""
        val host = data.host ?: ""

        val idFromQuery = data.getQueryParameter("id")
            ?: data.getQueryParameter("post_id")
            ?: data.getQueryParameter("reel_id")
            ?: data.getQueryParameter("user_id")

        val pathSegments = data.pathSegments ?: emptyList()
        val idFromPath = if (pathSegments.size >= 2) pathSegments[1] else if (pathSegments.size == 1 && (host == "post" || host == "reel" || host == "profile" || host == "user")) pathSegments[0] else null
        val targetId = idFromQuery ?: idFromPath ?: ""

        if (host == "reel" || path.startsWith("/reel") || data.getQueryParameter("reel_id") != null) {
            if (targetId.isNotBlank()) {
                viewModel.openReel(targetId)
            } else {
                viewModel.navigateTo(Screen.REELS)
            }
        } else if (host == "post" || path.startsWith("/post") || data.getQueryParameter("post_id") != null || path.contains("post.php")) {
            if (targetId.isNotBlank()) {
                viewModel.openPost(targetId)
            } else {
                viewModel.navigateTo(Screen.HOME)
            }
        } else if (host == "profile" || host == "user" || path.startsWith("/profile") || path.startsWith("/user") || path.contains("profile.php")) {
            if (targetId.isNotBlank()) {
                viewModel.openUserProfile(targetId)
            } else {
                viewModel.navigateTo(Screen.PROFILE)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val fullScreenImageUrl by viewModel.fullScreenImageUrl.collectAsState()
    val activeStoryIndex by viewModel.activeStoryIndex.collectAsState()
    val stories by viewModel.stories.collectAsState()

    // Periodically sync admin feature settings and toggles
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.refreshFeatureSettings()
            kotlinx.coroutines.delay(10000)
        }
    }

    // Handle back button behavior globally via stack
    if (currentScreen != Screen.HOME && currentScreen != Screen.LOGIN && currentScreen != Screen.SPLASH) {
        BackHandler {
            viewModel.navigateBack()
        }
    }

    val showBottomBar = (currentScreen == Screen.HOME ||
            currentScreen == Screen.REELS ||
            currentScreen == Screen.CHATS ||
            currentScreen == Screen.PROFILE) && currentScreen != Screen.SEARCH && currentScreen != Screen.CREATE_STORY && currentScreen != Screen.NOTIFICATIONS && currentScreen != Screen.FRIENDS

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
    ) {
        // Screen Content filling full height
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (currentScreen) {
                Screen.SPLASH -> SplashScreen(
                    viewModel = viewModel
                )

                Screen.LOGIN -> LoginScreen(
                    viewModel = viewModel,
                    onNavigateToRegister = { viewModel.navigateTo(Screen.REGISTER) },
                    onNavigateToForgotPassword = { viewModel.navigateTo(Screen.FORGOT_PASSWORD) }
                )

                Screen.FORGOT_PASSWORD -> ForgotPasswordScreen(
                    viewModel = viewModel,
                    onNavigateBack = { viewModel.navigateBack() }
                )

                Screen.REGISTER -> RegisterScreen(
                    viewModel = viewModel,
                    onNavigateToLogin = { viewModel.navigateTo(Screen.LOGIN) }
                )

                Screen.HOME -> HomeScreen(
                    viewModel = viewModel,
                    onCreatePostClick = { viewModel.navigateTo(Screen.CREATE_POST) },
                    onMenuClick = { viewModel.navigateTo(Screen.MENU) },
                    onCreateStoryClick = { viewModel.navigateTo(Screen.CREATE_STORY) }
                )

                Screen.REELS -> ReelsScreen(
                    viewModel = viewModel
                )

                Screen.CREATE_POST -> CreatePostScreen(
                    viewModel = viewModel,
                    onClose = { viewModel.navigateBack() }
                )

                Screen.CREATE_STORY -> CreateStoryScreen(
                    viewModel = viewModel,
                    onClose = { viewModel.navigateBack() }
                )

                Screen.CHATS -> ChatsScreen(
                    viewModel = viewModel,
                    onMenuClick = { viewModel.navigateTo(Screen.MENU) }
                )

                Screen.CHAT_DETAIL -> ChatDetailScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )

                Screen.PROFILE -> {
                    val activePage by viewModel.activePage.collectAsState()
                    if (activePage != null) {
                        PageProfileScreen(
                            page = activePage!!,
                            viewModel = viewModel,
                            onBack = { viewModel.navigateTo(Screen.HOME) }
                        )
                    } else {
                        ProfileScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateBack() },
                            onSearchClick = { viewModel.navigateTo(Screen.SEARCH) },
                            onCreatePostClick = { viewModel.navigateTo(Screen.CREATE_POST) },
                            onCreateStoryClick = { viewModel.navigateTo(Screen.CREATE_STORY) }
                        )
                    }
                }

                Screen.EDIT_PROFILE -> EditProfileScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )

                Screen.SEARCH -> SearchScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )

                Screen.NOTIFICATIONS -> NotificationScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )

                Screen.MENU -> MenuScreen(
                    viewModel = viewModel,
                    onProfileClick = { viewModel.navigateTo(Screen.PROFILE) }
                )

                Screen.FRIENDS -> FriendsScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )

                Screen.STORY_VIEWER -> StoryViewerScreen(
                    stories = stories,
                    initialIndex = activeStoryIndex,
                    onClose = { viewModel.navigateBack() }
                )

                Screen.FULL_IMAGE_VIEWER -> FullImageViewerScreen(
                    imageUrl = fullScreenImageUrl,
                    onClose = { viewModel.navigateBack() }
                )

                Screen.VERIFIED_BADGE -> VerifiedBadgeScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )

                Screen.MARKETPLACE -> MarketplaceScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )

                Screen.WALLET -> WalletScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )

                Screen.DEPOSIT -> DepositScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )

                Screen.WITHDRAW -> WithdrawScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )

                Screen.ADMIN_PANEL -> AdminPanelScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )

                Screen.DASHBOARD -> DashboardScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )

                Screen.HISTORY -> HistoryScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )

                Screen.SAVED -> SavedScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )

                Screen.PAGES -> PagesScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )

                Screen.CREATE_PAGE -> CreatePageScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )

                Screen.PAGE_PROFILE -> {
                    val pageProfile by viewModel.selectedPageProfile.collectAsState()
                    val fallbackPage = pageProfile ?: com.example.data.model.FlikataPage(
                        id = "default_page",
                        name = "My Page",
                        category = "General"
                    )
                    PageProfileScreen(
                        page = fallbackPage,
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() }
                    )
                }

                Screen.LEADERBOARD -> LeaderboardScreen(
                    viewModel = viewModel
                )
            }
        }

        // Floating Bottom Navigation Bar
        if (showBottomBar) {
            val currentUser by viewModel.currentUser.collectAsState()
            val activePage by viewModel.activePage.collectAsState()
            val chats by viewModel.chats.collectAsState()
            val unreadCount = remember(chats) { chats.sumOf { it.unreadCount } }
            FlikataBottomNav(
                currentScreen = currentScreen,
                onNavigate = { viewModel.navigateTo(it) },
                onCreatePostClick = { viewModel.navigateTo(Screen.CREATE_POST) },
                userAvatarUrl = activePage?.logoUrl ?: currentUser?.profilePic,
                unreadChatsCount = unreadCount,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        // Real-time Incoming Audio Call Fullscreen Overlay
        val incomingCall by viewModel.incomingCallState.collectAsState()
        if (incomingCall != null) {
            IncomingCallOverlay(
                call = incomingCall!!,
                onAccept = { viewModel.acceptIncomingCall() },
                onDecline = { viewModel.declineIncomingCall() }
            )
        }

        // Active 2-Way Audio Call Fullscreen Overlay
        val activeCall by viewModel.activeCallState.collectAsState()
        if (activeCall != null) {
            ActiveAudioCallOverlay(
                call = activeCall!!,
                audioCallManager = viewModel.audioCallManager,
                onEndCall = { viewModel.endActiveCall() }
            )
        }

        // Global In-App Software Update Overlay
        val isUpdateAvailable by viewModel.isUpdateAvailable.collectAsState()
        if (isUpdateAvailable) {
            AppUpdateView(
                viewModel = viewModel,
                onDismissOptional = {
                    viewModel.isUpdateAvailable.value = false
                }
            )
        }

        // Global Feature Notice Dialog in 100% Pure Bengali
        val featureNotice by viewModel.featureNoticeDialog.collectAsState()
        if (featureNotice != null) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { viewModel.featureNoticeDialog.value = null },
                title = {
                    androidx.compose.material3.Text(
                        text = "বিজ্ঞপ্তি",
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color(0xFF0F172A)
                    )
                },
                text = {
                    androidx.compose.material3.Text(
                        text = featureNotice!!,
                        fontSize = 15.sp,
                        color = androidx.compose.ui.graphics.Color(0xFF334155),
                        lineHeight = 22.sp
                    )
                },
                confirmButton = {
                    androidx.compose.material3.TextButton(
                        onClick = { viewModel.featureNoticeDialog.value = null }
                    ) {
                        androidx.compose.material3.Text(
                            text = "ঠিক আছে",
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color(0xFF0070F3)
                        )
                    }
                },
                containerColor = androidx.compose.ui.graphics.Color.White,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
            )
        }
    }
}
