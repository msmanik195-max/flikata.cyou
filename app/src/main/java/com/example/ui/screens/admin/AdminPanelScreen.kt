package com.example.ui.screens.admin

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.DepositMethod
import com.example.data.model.DepositRequestItem
import com.example.data.model.VerificationPackage
import com.example.data.model.VerificationRequestItem
import com.example.ui.components.SoftUITheme
import com.example.ui.components.VerifiedBadge
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.res.painterResource
import com.example.R
import java.util.Locale

enum class AdminSection {
    FEATURE_CONTROLS,
    PACKAGES,
    VERIFICATION_REQUESTS,
    DEPOSIT_METHODS,
    DEPOSIT_REQUESTS,
    PAGE_SETTINGS,
    LEADERBOARD_SETTINGS
}

@Composable
fun AdminPanelScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isDark = SoftUITheme.isDark

    var currentSection by remember { mutableStateOf(AdminSection.PACKAGES) }
    val maxPagesPerProfile by viewModel.maxPagesPerProfile.collectAsState()
    var pageLimitInput by remember(maxPagesPerProfile) { mutableStateOf(maxPagesPerProfile.toString()) }

    val leaderboardLimit by viewModel.leaderboardLimit.collectAsState()
    var leaderboardLimitInput by remember(leaderboardLimit) { mutableStateOf(leaderboardLimit.toString()) }

    // Section 1: Packages & Requirements
    var packages by remember { mutableStateOf<List<VerificationPackage>>(emptyList()) }
    var idCardRequired by remember { mutableStateOf(false) }
    var selfieRequired by remember { mutableStateOf(false) }
    var graceDays by remember { mutableStateOf("3") }
    var isLoadingPackages by remember { mutableStateOf(false) }

    // Add / Edit Package Dialog
    var showPackageDialog by remember { mutableStateOf(false) }
    var editingPackage by remember { mutableStateOf<VerificationPackage?>(null) }
    var pkgTitle by remember { mutableStateOf("") }
    var pkgDesc by remember { mutableStateOf("") }
    var pkgPrice by remember { mutableStateOf("") }
    var pkgDurationType by remember { mutableStateOf("month") } // day, month, year, lifetime
    var pkgDurationValue by remember { mutableStateOf("1") }

    // Section 2: Verification Requests
    var verReqTab by remember { mutableStateOf("pending") } // pending, approved, rejected
    var verRequests by remember { mutableStateOf<List<VerificationRequestItem>>(emptyList()) }
    var isLoadingVerReqs by remember { mutableStateOf(false) }
    var showRejectVerDialog by remember { mutableStateOf(false) }
    var rejectingVerReqId by remember { mutableIntStateOf(0) }
    var rejectReasonText by remember { mutableStateOf("") }

    // Section 3: Deposit Methods
    var depositMethods by remember { mutableStateOf<List<DepositMethod>>(emptyList()) }
    var isLoadingMethods by remember { mutableStateOf(false) }
    var showMethodDialog by remember { mutableStateOf(false) }
    var editingMethod by remember { mutableStateOf<DepositMethod?>(null) }
    var methodName by remember { mutableStateOf("") }
    var methodNumber by remember { mutableStateOf("") }
    var methodImage by remember { mutableStateOf("") }
    var methodDesc by remember { mutableStateOf("") }
    var methodIsActive by remember { mutableStateOf(true) }

    // Section 4: Deposit Requests
    var depReqTab by remember { mutableStateOf("pending") } // pending, approved, rejected
    var depRequests by remember { mutableStateOf<List<DepositRequestItem>>(emptyList()) }
    var isLoadingDepReqs by remember { mutableStateOf(false) }
    var showRejectDepDialog by remember { mutableStateOf(false) }
    var rejectingDepReqId by remember { mutableIntStateOf(0) }
    var rejectDepReasonText by remember { mutableStateOf("") }

    // Fetch helpers
    fun loadPackagesAndSettings() {
        isLoadingPackages = true
        scope.launch {
            val res = viewModel.repository.getAdminPackages()
            if (res.isSuccess) {
                val data = res.getOrNull()
                packages = data?.packages ?: emptyList()
                idCardRequired = data?.settings?.idCardRequired ?: false
                selfieRequired = data?.settings?.selfieRequired ?: false
                graceDays = (data?.settings?.gracePeriodDays ?: 3).toString()
            }
            isLoadingPackages = false
        }
    }

    fun loadVerRequests(status: String) {
        isLoadingVerReqs = true
        scope.launch {
            val res = viewModel.repository.getAdminVerificationRequests(status)
            if (res.isSuccess) {
                verRequests = res.getOrNull()?.requests ?: emptyList()
            }
            isLoadingVerReqs = false
        }
    }

    fun loadDepositMethods() {
        isLoadingMethods = true
        scope.launch {
            val res = viewModel.repository.getAdminDepositMethods()
            if (res.isSuccess) {
                depositMethods = res.getOrNull() ?: emptyList()
            }
            isLoadingMethods = false
        }
    }

    fun loadDepositRequests(status: String) {
        isLoadingDepReqs = true
        scope.launch {
            val res = viewModel.repository.getAdminDepositRequests(status)
            if (res.isSuccess) {
                depRequests = res.getOrNull()?.requests ?: emptyList()
            }
            isLoadingDepReqs = false
        }
    }

    LaunchedEffect(currentSection) {
        when (currentSection) {
            AdminSection.FEATURE_CONTROLS -> viewModel.refreshFeatureSettings()
            AdminSection.PACKAGES -> loadPackagesAndSettings()
            AdminSection.VERIFICATION_REQUESTS -> loadVerRequests(verReqTab)
            AdminSection.DEPOSIT_METHODS -> loadDepositMethods()
            AdminSection.DEPOSIT_REQUESTS -> loadDepositRequests(depReqTab)
            AdminSection.PAGE_SETTINGS -> viewModel.loadPageSettings()
            AdminSection.LEADERBOARD_SETTINGS -> viewModel.loadLeaderboardSettings()
        }
    }

    val cardBg = if (isDark) Color(0xFF1E222B) else Color.White
    val cardBorder = if (isDark) Color(0xFF2D323E) else Color(0xFFE4E9F2)
    val featureSettings by viewModel.featureSettings.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF11141A) else Color(0xFFF5F7FB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDark) Color(0xFF1A1D24) else Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("admin_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = SoftUITheme.TextDark
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Admin Panel",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftUITheme.TextDark
                    )
                }
            }

            // Section Switcher Row (Scrollable tabs with clear badges)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDark) Color(0xFF161920) else Color(0xFFEBF0F8))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdminSectionButton(
                    title = "ফিচার ও ফিড কন্ট্রোল",
                    isSelected = currentSection == AdminSection.FEATURE_CONTROLS
                ) { currentSection = AdminSection.FEATURE_CONTROLS }

                AdminSectionButton(
                    title = "Badge Pkg",
                    isSelected = currentSection == AdminSection.PACKAGES
                ) { currentSection = AdminSection.PACKAGES }

                AdminSectionButton(
                    title = "Badge Req",
                    isSelected = currentSection == AdminSection.VERIFICATION_REQUESTS
                ) { currentSection = AdminSection.VERIFICATION_REQUESTS }

                AdminSectionButton(
                    title = "Deposit Mth",
                    isSelected = currentSection == AdminSection.DEPOSIT_METHODS
                ) { currentSection = AdminSection.DEPOSIT_METHODS }

                AdminSectionButton(
                    title = "Deposit Req",
                    isSelected = currentSection == AdminSection.DEPOSIT_REQUESTS
                ) { currentSection = AdminSection.DEPOSIT_REQUESTS }

                AdminSectionButton(
                    title = "Pages Limit",
                    isSelected = currentSection == AdminSection.PAGE_SETTINGS
                ) { currentSection = AdminSection.PAGE_SETTINGS }

                AdminSectionButton(
                    title = "Leaderboard",
                    isSelected = currentSection == AdminSection.LEADERBOARD_SETTINGS
                ) { currentSection = AdminSection.LEADERBOARD_SETTINGS }
            }

            // Body Content based on active section
            when (currentSection) {
                // ==========================================
                // 0. APP FEATURE & FEED CONTROLS
                // ==========================================
                AdminSection.FEATURE_CONTROLS -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Header Banner Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isDark) Color(0xFF1E2128) else Color(0xFFEEF2FF))
                                .border(1.dp, if (isDark) Color(0xFF282C37) else Color(0xFFC7D2FE), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4F46E5)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Save,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "ফিচার ও ফিড কন্ট্রোল (Feature & Feed Controls)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = SoftUITheme.TextDark
                                    )
                                    Text(
                                        text = "যেকোনো টগল অন/অফ করলে সাথে সাথে কার্যকর হয়",
                                        fontSize = 12.sp,
                                        color = SoftUITheme.TextSecondary
                                    )
                                }
                            }
                        }

                        // 1. REELS VIDEO PAGE TOGGLE
                        AdminFeatureToggleCard(
                            title = "রিলস (Reels) ভিডিও পেজ",
                            subtitle = if (featureSettings.allowReels) 
                                "চালু আছে: রিলস পেজে সকল ভিডিও রিলস দেখা যাবে" 
                                else "বন্ধ আছে: রিলস পেজে কোনো ভিডিও দেখাবে না এবং নোটিশ দেখাবে 'রিলস সুবিধা সাময়িকভাবে বন্ধ আছে'",
                            isChecked = featureSettings.allowReels,
                            accentColor = Color(0xFF9333EA),
                            isDark = isDark,
                            onToggle = { isChecked ->
                                viewModel.updateFeatureSetting("allow_reels", isChecked)
                                Toast.makeText(context, if (isChecked) "রিলস ভিডিও পেজ চালু করা হয়েছে" else "রিলস ভিডিও পেজ বন্ধ করা হয়েছে", Toast.LENGTH_SHORT).show()
                            }
                        )

                        // 2. HOME FEED VIDEO POSTS TOGGLE
                        AdminFeatureToggleCard(
                            title = "হোম ফিডে ভিডিও পোস্ট",
                            subtitle = if (featureSettings.feedShowVideoPosts) 
                                "চালু আছে: হোম ফিডে ভিডিও সংবলিত পোস্টগুলো দেখা যাবে" 
                                else "বন্ধ আছে: শুধুমাত্র হোম ফিড থেকে ভিডিও লুকানো থাকবে। রিলস পেজে ভিডিও স্বাভাবিক থাকবে (মুছবে না)",
                            isChecked = featureSettings.feedShowVideoPosts,
                            accentColor = Color(0xFFEF4444),
                            isDark = isDark,
                            onToggle = { isChecked ->
                                viewModel.updateFeatureSetting("feed_show_video_posts", isChecked)
                                Toast.makeText(context, if (isChecked) "হোম ফিডে ভিডিও পোস্ট চালু করা হয়েছে" else "হোম ফিডে ভিডিও পোস্ট বন্ধ করা হয়েছে (রিলসে থাকবে)", Toast.LENGTH_SHORT).show()
                            }
                        )

                        // 3. HOME FEED IMAGE POSTS TOGGLE
                        AdminFeatureToggleCard(
                            title = "হোম ফিডে ইমেজ পোস্ট",
                            subtitle = if (featureSettings.feedShowImagePosts) "হোম ফিডে ছবিযুক্ত পোস্ট দেখা যাবে" else "হোম ফিডে ছবিযুক্ত পোস্ট লুকানো থাকবে",
                            isChecked = featureSettings.feedShowImagePosts,
                            accentColor = Color(0xFF0EA5E9),
                            isDark = isDark,
                            onToggle = { isChecked ->
                                viewModel.updateFeatureSetting("feed_show_image_posts", isChecked)
                                Toast.makeText(context, "ইমেজ পোস্ট সেটিংস আপডেট হয়েছে", Toast.LENGTH_SHORT).show()
                            }
                        )

                        // 4. HOME FEED TEXT POSTS TOGGLE
                        AdminFeatureToggleCard(
                            title = "হোম ফিডে টেক্সট পোস্ট",
                            subtitle = if (featureSettings.feedShowTextPosts) "হোম ফিডে শুধুমাত্র লেখার পোস্ট দেখা যাবে" else "হোম ফিডে লেখার পোস্ট লুকানো থাকবে",
                            isChecked = featureSettings.feedShowTextPosts,
                            accentColor = Color(0xFF3B82F6),
                            isDark = isDark,
                            onToggle = { isChecked ->
                                viewModel.updateFeatureSetting("feed_show_text_posts", isChecked)
                                Toast.makeText(context, "টেক্সট পোস্ট সেটিংস আপডেট হয়েছে", Toast.LENGTH_SHORT).show()
                            }
                        )

                        // 5. ALLOW PAGE CREATION
                        AdminFeatureToggleCard(
                            title = "পেজ তৈরি করার অনুমতি",
                            subtitle = if (featureSettings.allowPageCreation) "ইউজাররা নতুন পেজ খুলতে পারবেন" else "পেজ তৈরি বন্ধ: ক্লিক করলে পপ-আপ মেসেজ আসবে",
                            isChecked = featureSettings.allowPageCreation,
                            accentColor = Color(0xFF10B981),
                            isDark = isDark,
                            onToggle = { isChecked ->
                                viewModel.updateFeatureSetting("allow_page_creation", isChecked)
                                Toast.makeText(context, "পেজ তৈরি সেটিংস আপডেট হয়েছে", Toast.LENGTH_SHORT).show()
                            }
                        )

                        // 6. ALLOW REGISTRATION
                        AdminFeatureToggleCard(
                            title = "নতুন রেজিস্ট্রেশন সুবিধা",
                            subtitle = if (featureSettings.allowRegistration) "নতুন ইউজার সাইন আপ করতে পারবেন" else "রেজিস্ট্রেশন বন্ধ: নোটিশ দেখাবে 'রেজিস্ট্রেশন সাময়িকভাবে বন্ধ আছে'",
                            isChecked = featureSettings.allowRegistration,
                            accentColor = Color(0xFFF59E0B),
                            isDark = isDark,
                            onToggle = { isChecked ->
                                viewModel.updateFeatureSetting("allow_registration", isChecked)
                                Toast.makeText(context, "রেজিস্ট্রেশন সেটিংস আপডেট হয়েছে", Toast.LENGTH_SHORT).show()
                            }
                        )

                        // 7. ALLOW LOGIN
                        AdminFeatureToggleCard(
                            title = "ইউজার লগইন সুবিধা",
                            subtitle = if (featureSettings.allowLogin) "ইউজাররা লগইন করতে পারবেন" else "লগইন বন্ধ: নোটিশ দেখাবে 'লগইন সাময়িকভাবে বন্ধ আছে'",
                            isChecked = featureSettings.allowLogin,
                            accentColor = Color(0xFF6366F1),
                            isDark = isDark,
                            onToggle = { isChecked ->
                                viewModel.updateFeatureSetting("allow_login", isChecked)
                                Toast.makeText(context, "লগইন সেটিংস আপডেট হয়েছে", Toast.LENGTH_SHORT).show()
                            }
                        )

                        // 8. ALLOW CHAT
                        AdminFeatureToggleCard(
                            title = "চ্যাট অপশন সুবিধা",
                            subtitle = if (featureSettings.allowChat) "ইউজাররা চ্যাটে মেসেজ পাঠাতে পারবেন" else "চ্যাট বন্ধ: নোটিশ দেখাবে 'চ্যাট অপশন সাময়িকভাবে বন্ধ আছে'",
                            isChecked = featureSettings.allowChat,
                            accentColor = Color(0xFF06B6D4),
                            isDark = isDark,
                            onToggle = { isChecked ->
                                viewModel.updateFeatureSetting("allow_chat", isChecked)
                                Toast.makeText(context, "চ্যাট সুবিধা সেটিংস আপডেট হয়েছে", Toast.LENGTH_SHORT).show()
                            }
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // ==========================================
                // 1. VERIFICATION PACKAGES & REQUIREMENTS
                // ==========================================
                AdminSection.PACKAGES -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // Requirements & Grace Period Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .background(cardBg)
                                .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Verification Requirements Configuration",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = SoftUITheme.TextDark
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                // ID Card toggle
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Require National ID / Passport",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.5.sp,
                                            color = SoftUITheme.TextDark
                                        )
                                        Text(
                                            text = "Front & back page upload required from user",
                                            fontSize = 11.5.sp,
                                            color = SoftUITheme.TextSecondary
                                        )
                                    }
                                    Switch(
                                        checked = idCardRequired,
                                        onCheckedChange = { idCardRequired = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF0866FF))
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Selfie toggle
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Require Live Face Selfie",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.5.sp,
                                            color = SoftUITheme.TextDark
                                        )
                                        Text(
                                            text = "Clear selfie upload required from user",
                                            fontSize = 11.5.sp,
                                            color = SoftUITheme.TextSecondary
                                        )
                                    }
                                    Switch(
                                        checked = selfieRequired,
                                        onCheckedChange = { selfieRequired = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF0866FF))
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Grace Period Days
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Grace Period (Extra Days)",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.5.sp,
                                            color = SoftUITheme.TextDark
                                        )
                                        Text(
                                            text = "Days badge stays on profile after expiration",
                                            fontSize = 11.5.sp,
                                            color = SoftUITheme.TextSecondary
                                        )
                                    }
                                    OutlinedTextField(
                                        value = graceDays,
                                        onValueChange = { graceDays = it },
                                        modifier = Modifier.width(90.dp),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        val gVal = graceDays.toIntOrNull() ?: 3
                                        scope.launch {
                                            val res = viewModel.repository.adminSaveVerificationSettings(
                                                idCardRequired = idCardRequired,
                                                selfieRequired = selfieRequired,
                                                gracePeriodDays = gVal
                                            )
                                            if (res.isSuccess) {
                                                Toast.makeText(context, "Settings saved!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, res.exceptionOrNull()?.message ?: "Save failed", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0866FF)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Save Configuration", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Packages Header & Add Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Verification Packages (${packages.size})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftUITheme.TextDark
                            )

                            Button(
                                onClick = {
                                    editingPackage = null
                                    pkgTitle = ""
                                    pkgDesc = ""
                                    pkgPrice = "200"
                                    pkgDurationType = "month"
                                    pkgDurationValue = "1"
                                    showPackageDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Package", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (packages.isEmpty() && !isLoadingPackages) {
                            Text(
                                text = "No verification packages added yet. Click Add Package to create one.",
                                fontSize = 13.sp,
                                color = SoftUITheme.TextSecondary,
                                modifier = Modifier.padding(vertical = 24.dp)
                            )
                        }

                        packages.forEach { pkg ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .shadow(2.dp, RoundedCornerShape(14.dp))
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(cardBg)
                                    .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = pkg.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = SoftUITheme.TextDark
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            VerifiedBadge(size = 18.dp)
                                        }
                                        if (!pkg.description.isNullOrEmpty()) {
                                            Text(
                                                text = pkg.description,
                                                fontSize = 12.sp,
                                                color = SoftUITheme.TextSecondary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "৳ ${String.format(Locale.getDefault(), "%.2f", pkg.price)} • Duration: ${pkg.durationText}",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF0866FF)
                                        )
                                    }

                                    Row {
                                        IconButton(onClick = {
                                            editingPackage = pkg
                                            pkgTitle = pkg.title
                                            pkgDesc = pkg.description ?: ""
                                            pkgPrice = pkg.price.toString()
                                            pkgDurationType = pkg.durationType
                                            pkgDurationValue = pkg.durationValue.toString()
                                            showPackageDialog = true
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF0866FF))
                                        }
                                        IconButton(onClick = {
                                            scope.launch {
                                                val res = viewModel.repository.adminDeletePackage(pkg.id)
                                                if (res.isSuccess) {
                                                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                                                    loadPackagesAndSettings()
                                                }
                                            }
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE53935))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 2. VERIFICATION REQUESTS
                // ==========================================
                AdminSection.VERIFICATION_REQUESTS -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        TabRow(
                            selectedTabIndex = when (verReqTab) {
                                "pending" -> 0
                                "approved" -> 1
                                else -> 2
                            },
                            containerColor = cardBg
                        ) {
                            Tab(
                                selected = verReqTab == "pending",
                                onClick = {
                                    verReqTab = "pending"
                                    loadVerRequests("pending")
                                },
                                text = { Text("Pending", fontWeight = FontWeight.Bold) }
                            )
                            Tab(
                                selected = verReqTab == "approved",
                                onClick = {
                                    verReqTab = "approved"
                                    loadVerRequests("approved")
                                },
                                text = { Text("Approved", fontWeight = FontWeight.Bold) }
                            )
                            Tab(
                                selected = verReqTab == "rejected",
                                onClick = {
                                    verReqTab = "rejected"
                                    loadVerRequests("rejected")
                                },
                                text = { Text("Rejected", fontWeight = FontWeight.Bold) }
                            )
                        }

                        if (isLoadingVerReqs) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        } else if (verRequests.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "No $verReqTab verification requests found.",
                                    fontSize = 14.sp,
                                    color = SoftUITheme.TextSecondary
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(verRequests, key = { it.id }) { req ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .shadow(2.dp, RoundedCornerShape(14.dp))
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(cardBg)
                                            .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
                                            .padding(14.dp)
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        text = req.userName ?: "User #${req.userId}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp,
                                                        color = SoftUITheme.TextDark
                                                    )
                                                    Text(
                                                        text = "Pkg: ${req.packageTitle ?: "Verified Badge"} • ৳ ${req.amount ?: 0.0}",
                                                        fontSize = 12.sp,
                                                        color = Color(0xFF0866FF),
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                                Text(
                                                    text = req.status.uppercase(Locale.getDefault()),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = when (req.status) {
                                                        "approved" -> Color(0xFF00C853)
                                                        "rejected" -> Color(0xFFE53935)
                                                        else -> Color(0xFFF57F17)
                                                    }
                                                )
                                            }

                                            if (!req.rejectionReason.isNullOrEmpty()) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Reason: ${req.rejectionReason}",
                                                    fontSize = 11.5.sp,
                                                    color = Color(0xFFE53935)
                                                )
                                            }

                                            // ID front, back, selfie preview if present
                                            if (!req.idFrontUrl.isNullOrEmpty() || !req.idBackUrl.isNullOrEmpty() || !req.selfieUrl.isNullOrEmpty()) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    req.idFrontUrl?.let { url ->
                                                        AsyncImage(
                                                            model = url,
                                                            contentDescription = "ID Front",
                                                            modifier = Modifier
                                                                .size(60.dp)
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(Color.LightGray)
                                                        )
                                                    }
                                                    req.idBackUrl?.let { url ->
                                                        AsyncImage(
                                                            model = url,
                                                            contentDescription = "ID Back",
                                                            modifier = Modifier
                                                                .size(60.dp)
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(Color.LightGray)
                                                        )
                                                    }
                                                    req.selfieUrl?.let { url ->
                                                        AsyncImage(
                                                            model = url,
                                                            contentDescription = "Selfie",
                                                            modifier = Modifier
                                                                .size(60.dp)
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(Color.LightGray)
                                                        )
                                                    }
                                                }
                                            }

                                            if (req.status == "pending") {
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.End
                                                ) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            rejectingVerReqId = req.id
                                                            rejectReasonText = ""
                                                            showRejectVerDialog = true
                                                        },
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935)),
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Text("Reject")
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Button(
                                                        onClick = {
                                                            scope.launch {
                                                                val res = viewModel.repository.adminApproveVerification(req.id)
                                                                if (res.isSuccess) {
                                                                    Toast.makeText(context, "Approved!", Toast.LENGTH_SHORT).show()
                                                                    loadVerRequests(verReqTab)
                                                                } else {
                                                                    Toast.makeText(context, res.exceptionOrNull()?.message ?: "Failed", Toast.LENGTH_SHORT).show()
                                                                }
                                                            }
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Text("Approve")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 3. DEPOSIT METHODS MANAGEMENT
                // ==========================================
                AdminSection.DEPOSIT_METHODS -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Deposit Payment Methods (${depositMethods.size})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftUITheme.TextDark
                            )

                            Button(
                                onClick = {
                                    editingMethod = null
                                    methodName = ""
                                    methodNumber = ""
                                    methodImage = ""
                                    methodDesc = "Send money and submit transaction ID."
                                    methodIsActive = true
                                    showMethodDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Method", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (depositMethods.isEmpty() && !isLoadingMethods) {
                            Text(
                                text = "No deposit payment methods added. Click Add Method to create bKash, Nagad, Rocket, Bank etc.",
                                fontSize = 13.sp,
                                color = SoftUITheme.TextSecondary,
                                modifier = Modifier.padding(vertical = 24.dp)
                            )
                        }

                        depositMethods.forEach { m ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .shadow(2.dp, RoundedCornerShape(14.dp))
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(cardBg)
                                    .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        if (!m.imageUrl.isNullOrEmpty()) {
                                            AsyncImage(
                                                model = m.imageUrl,
                                                contentDescription = m.name,
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFEEEEEE))
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                        }
                                        Column {
                                            Text(
                                                text = m.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = SoftUITheme.TextDark
                                            )
                                            Text(
                                                text = "Number: ${m.number}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF0866FF)
                                            )
                                            if (!m.description.isNullOrEmpty()) {
                                                Text(
                                                    text = m.description,
                                                    fontSize = 11.5.sp,
                                                    color = SoftUITheme.TextSecondary
                                                )
                                            }
                                        }
                                    }

                                    Row {
                                        IconButton(onClick = {
                                            editingMethod = m
                                            methodName = m.name
                                            methodNumber = m.number
                                            methodImage = m.imageUrl ?: ""
                                            methodDesc = m.description ?: ""
                                            methodIsActive = m.isActive == 1
                                            showMethodDialog = true
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF0866FF))
                                        }
                                        IconButton(onClick = {
                                            scope.launch {
                                                val res = viewModel.repository.adminDeleteDepositMethod(m.id)
                                                if (res.isSuccess) {
                                                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                                                    loadDepositMethods()
                                                }
                                            }
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE53935))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 4. DEPOSIT REQUESTS MANAGEMENT
                // ==========================================
                AdminSection.DEPOSIT_REQUESTS -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        TabRow(
                            selectedTabIndex = when (depReqTab) {
                                "pending" -> 0
                                "approved" -> 1
                                else -> 2
                            },
                            containerColor = cardBg
                        ) {
                            Tab(
                                selected = depReqTab == "pending",
                                onClick = {
                                    depReqTab = "pending"
                                    loadDepositRequests("pending")
                                },
                                text = { Text("Pending", fontWeight = FontWeight.Bold) }
                            )
                            Tab(
                                selected = depReqTab == "approved",
                                onClick = {
                                    depReqTab = "approved"
                                    loadDepositRequests("approved")
                                },
                                text = { Text("Approved", fontWeight = FontWeight.Bold) }
                            )
                            Tab(
                                selected = depReqTab == "rejected",
                                onClick = {
                                    depReqTab = "rejected"
                                    loadDepositRequests("rejected")
                                },
                                text = { Text("Rejected", fontWeight = FontWeight.Bold) }
                            )
                        }

                        if (isLoadingDepReqs) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        } else if (depRequests.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "No $depReqTab deposit requests found.",
                                    fontSize = 14.sp,
                                    color = SoftUITheme.TextSecondary
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(depRequests, key = { it.id }) { req ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .shadow(2.dp, RoundedCornerShape(14.dp))
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(cardBg)
                                            .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
                                            .padding(14.dp)
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        text = req.userName ?: "User #${req.userId}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp,
                                                        color = SoftUITheme.TextDark
                                                    )
                                                    Text(
                                                        text = "Amount: ৳ ${String.format(Locale.getDefault(), "%.2f", req.amount)} via ${req.methodName}",
                                                        fontSize = 13.sp,
                                                        color = Color(0xFF00C853),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "TrxID: ${req.transactionId}",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = SoftUITheme.TextDark
                                                    )
                                                }
                                                Text(
                                                    text = req.status.uppercase(Locale.getDefault()),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = when (req.status) {
                                                        "approved" -> Color(0xFF00C853)
                                                        "rejected" -> Color(0xFFE53935)
                                                        else -> Color(0xFFF57F17)
                                                    }
                                                )
                                            }

                                            if (!req.rejectionReason.isNullOrEmpty()) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Reason: ${req.rejectionReason}",
                                                    fontSize = 11.5.sp,
                                                    color = Color(0xFFE53935)
                                                )
                                            }

                                            if (req.status == "pending") {
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.End
                                                ) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            rejectingDepReqId = req.id
                                                            rejectDepReasonText = ""
                                                            showRejectDepDialog = true
                                                        },
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935)),
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Text("Reject")
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Button(
                                                        onClick = {
                                                            scope.launch {
                                                                val res = viewModel.repository.adminApproveDeposit(req.id)
                                                                if (res.isSuccess) {
                                                                    Toast.makeText(context, "Deposit Approved & Credited!", Toast.LENGTH_SHORT).show()
                                                                    loadDepositRequests(depReqTab)
                                                                } else {
                                                                    Toast.makeText(context, res.exceptionOrNull()?.message ?: "Failed", Toast.LENGTH_SHORT).show()
                                                                }
                                                            }
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Text("Approve")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 5. PAGE SETTINGS (Max Pages per Profile)
                // ==========================================
                AdminSection.PAGE_SETTINGS -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isDark) Color(0xFF1E2128) else Color.White)
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE3F2FD)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Flag,
                                            contentDescription = null,
                                            tint = Color(0xFF0075FF),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Page Creation Limit Settings",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp,
                                            color = SoftUITheme.TextDark
                                        )
                                        Text(
                                            text = "Control maximum pages each user profile can create",
                                            fontSize = 12.sp,
                                            color = SoftUITheme.TextSecondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Text(
                                    text = "Maximum Pages Per Profile",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftUITheme.TextDark
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = pageLimitInput,
                                    onValueChange = { if (it.all { ch -> ch.isDigit() }) pageLimitInput = it },
                                    label = { Text("Max Pages (e.g. 5)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Quick Presets:",
                                    fontSize = 12.sp,
                                    color = SoftUITheme.TextSecondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(2, 3, 5, 10, 20).forEach { preset ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (pageLimitInput == preset.toString()) Color(0xFF0075FF) else if (isDark) Color(0xFF282C37) else Color(0xFFF1F5F9))
                                                .clickable { pageLimitInput = preset.toString() }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "$preset Pages",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (pageLimitInput == preset.toString()) Color.White else SoftUITheme.TextDark
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Button(
                                    onClick = {
                                        val newLimit = pageLimitInput.toIntOrNull()
                                        if (newLimit != null && newLimit > 0) {
                                            viewModel.updateMaxPagesPerProfile(newLimit)
                                            Toast.makeText(context, "Page limit successfully saved: $newLimit pages per profile!", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "Please enter a valid number greater than 0", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0075FF))
                                ) {
                                    Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Save Page Limit Setting", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }
                        }

                        // Info card explaining enforcement
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(1.dp, RoundedCornerShape(14.dp))
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isDark) Color(0xFF1B2230) else Color(0xFFE8F1FC))
                                .padding(14.dp)
                        ) {
                            Column {
                                Text(
                                    text = "How this setting works:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = Color(0xFF0052D4)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "• Users see this quota limit when opening Create Page screen.\n• Once a user reaches this count, further page creation is prevented in the application and rejected by backend.\n• You can increase or decrease this limit at any time.",
                                    fontSize = 12.5.sp,
                                    lineHeight = 18.sp,
                                    color = SoftUITheme.TextSecondary
                                )
                            }
                        }
                    }
                }

                // ==========================================
                // 6. LEADERBOARD SETTINGS (লিডার বোর্ড সেটিংস)
                // ==========================================
                AdminSection.LEADERBOARD_SETTINGS -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isDark) Color(0xFF1E2128) else Color.White)
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFF3E0)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.EmojiEvents,
                                            contentDescription = null,
                                            tint = Color(0xFFFF9800),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Leaderboard Settings (লিডার বোর্ড সেটিংস)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp,
                                            color = SoftUITheme.TextDark
                                        )
                                        Text(
                                            text = "Configure maximum top engaged users to display on Leaderboard",
                                            fontSize = 12.sp,
                                            color = SoftUITheme.TextSecondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Text(
                                    text = "Total Users to Display (মোট লিডার বোর্ড টপ ইউজার)",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftUITheme.TextDark
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = leaderboardLimitInput,
                                    onValueChange = { if (it.all { ch -> ch.isDigit() }) leaderboardLimitInput = it },
                                    label = { Text("Display Limit (Default: 20)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Quick Presets:",
                                    fontSize = 12.sp,
                                    color = SoftUITheme.TextSecondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.horizontalScroll(rememberScrollState())
                                ) {
                                    listOf(10, 20, 30, 50, 100).forEach { preset ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (leaderboardLimitInput == preset.toString()) Color(0xFFFF9800)
                                                    else if (isDark) Color(0xFF282C37)
                                                    else Color(0xFFF1F5F9)
                                                )
                                                .clickable { leaderboardLimitInput = preset.toString() }
                                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = "$preset Users",
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (leaderboardLimitInput == preset.toString()) Color.White else SoftUITheme.TextDark
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Button(
                                    onClick = {
                                        val newLimit = leaderboardLimitInput.toIntOrNull()
                                        if (newLimit != null && newLimit > 0) {
                                            viewModel.updateLeaderboardLimit(newLimit)
                                            Toast.makeText(
                                                context,
                                                "Leaderboard limit updated to $newLimit users! (সাথে সাথে পরিবর্তন হবে)",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            Toast.makeText(context, "Please enter a valid count greater than 0", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                                ) {
                                    Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Save Leaderboard Settings", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                                }
                            }
                        }

                        // Info card explaining real-time leaderboard ranking
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(1.dp, RoundedCornerShape(14.dp))
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isDark) Color(0xFF25211B) else Color(0xFFFFF8E1))
                                .padding(14.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Leaderboard Algorithm & Rules:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = Color(0xFFE65100)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "• Rank 1: Gold 👑, Rank 2: Silver 🥈, Rank 3: Bronze/Diamond 🥉.\n• Users ranked by real Engagement: Total Likes, Total Comments, and Total Followers.\n• Default setting is 20 users. Any change here takes effect immediately on the user Leaderboard screen.",
                                    fontSize = 12.5.sp,
                                    lineHeight = 18.sp,
                                    color = SoftUITheme.TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // DIALOG: ADD / EDIT PACKAGE
    // ==========================================
    if (showPackageDialog) {
        var durationMenuExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showPackageDialog = false },
            title = {
                Text(
                    text = if (editingPackage == null) "Add Verification Package" else "Edit Verification Package",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = pkgTitle,
                        onValueChange = { pkgTitle = it },
                        label = { Text("Package Title *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = pkgDesc,
                        onValueChange = { pkgDesc = it },
                        label = { Text("Description (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = pkgPrice,
                        onValueChange = { pkgPrice = it },
                        label = { Text("Price (BDT / ৳) *") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Duration Type Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = when (pkgDurationType) {
                                "day" -> "Day"
                                "month" -> "Month"
                                "year" -> "Year"
                                "lifetime" -> "Lifetime"
                                else -> pkgDurationType.replaceFirstChar { it.uppercase() }
                            },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Duration Type *") },
                            trailingIcon = {
                                IconButton(onClick = { durationMenuExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { durationMenuExpanded = true }
                        )

                        DropdownMenu(
                            expanded = durationMenuExpanded,
                            onDismissRequest = { durationMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Day") },
                                onClick = {
                                    pkgDurationType = "day"
                                    durationMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Month") },
                                onClick = {
                                    pkgDurationType = "month"
                                    durationMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Year") },
                                onClick = {
                                    pkgDurationType = "year"
                                    durationMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Lifetime") },
                                onClick = {
                                    pkgDurationType = "lifetime"
                                    durationMenuExpanded = false
                                }
                            )
                        }
                    }

                    if (pkgDurationType != "lifetime") {
                        OutlinedTextField(
                            value = pkgDurationValue,
                            onValueChange = { pkgDurationValue = it },
                            label = { Text("Duration Count (e.g. 1, 3, 6, 12)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pPrice = pkgPrice.toDoubleOrNull() ?: 0.0
                        val pVal = if (pkgDurationType == "lifetime") 0 else (pkgDurationValue.toIntOrNull() ?: 1)
                        if (pkgTitle.isBlank()) {
                            Toast.makeText(context, "Title is required", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        scope.launch {
                            val res = if (editingPackage == null) {
                                viewModel.repository.adminCreatePackage(
                                    title = pkgTitle.trim(),
                                    description = pkgDesc.trim().ifEmpty { null },
                                    price = pPrice,
                                    durationType = pkgDurationType,
                                    durationValue = pVal
                                )
                            } else {
                                viewModel.repository.adminEditPackage(
                                    id = editingPackage!!.id,
                                    title = pkgTitle.trim(),
                                    description = pkgDesc.trim().ifEmpty { null },
                                    price = pPrice,
                                    durationType = pkgDurationType,
                                    durationValue = pVal
                                )
                            }
                            if (res.isSuccess) {
                                Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show()
                                showPackageDialog = false
                                loadPackagesAndSettings()
                            } else {
                                Toast.makeText(context, res.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0866FF))
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPackageDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ==========================================
    // DIALOG: REJECT VERIFICATION
    // ==========================================
    if (showRejectVerDialog) {
        AlertDialog(
            onDismissRequest = { showRejectVerDialog = false },
            title = { Text("Reject Verification Request", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Optionally provide rejection reason (fee will be refunded to user wallet):", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectReasonText,
                        onValueChange = { rejectReasonText = it },
                        placeholder = { Text("e.g. Blurry ID card or photo mismatch") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val res = viewModel.repository.adminRejectVerification(
                                requestId = rejectingVerReqId,
                                reason = rejectReasonText.trim().ifEmpty { null }
                            )
                            if (res.isSuccess) {
                                Toast.makeText(context, "Rejected and refunded", Toast.LENGTH_SHORT).show()
                                showRejectVerDialog = false
                                loadVerRequests(verReqTab)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("Reject & Refund")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectVerDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ==========================================
    // DIALOG: ADD / EDIT DEPOSIT METHOD
    // ==========================================
    if (showMethodDialog) {
        AlertDialog(
            onDismissRequest = { showMethodDialog = false },
            title = {
                Text(
                    text = if (editingMethod == null) "Add Deposit Method" else "Edit Deposit Method",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = methodName,
                        onValueChange = { methodName = it },
                        label = { Text("Method Name (e.g. bKash, Nagad) *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = methodNumber,
                        onValueChange = { methodNumber = it },
                        label = { Text("Account / Phone Number *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = methodImage,
                        onValueChange = { methodImage = it },
                        label = { Text("Method Logo Image URL (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = methodDesc,
                        onValueChange = { methodDesc = it },
                        label = { Text("Instructions / Description") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF5F7FA))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Active Method", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Visible to users for deposit", fontSize = 12.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = methodIsActive,
                            onCheckedChange = { methodIsActive = it }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (methodName.isBlank() || methodNumber.isBlank()) {
                            Toast.makeText(context, "Name and number required", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        scope.launch {
                            val activeVal = if (methodIsActive) 1 else 0
                            val res = if (editingMethod == null) {
                                viewModel.repository.adminCreateDepositMethod(
                                    name = methodName.trim(),
                                    number = methodNumber.trim(),
                                    imageUrl = methodImage.trim().ifEmpty { null },
                                    description = methodDesc.trim().ifEmpty { null },
                                    isActive = activeVal
                                )
                            } else {
                                viewModel.repository.adminEditDepositMethod(
                                    id = editingMethod!!.id,
                                    name = methodName.trim(),
                                    number = methodNumber.trim(),
                                    imageUrl = methodImage.trim().ifEmpty { null },
                                    description = methodDesc.trim().ifEmpty { null },
                                    isActive = activeVal
                                )
                            }
                            if (res.isSuccess) {
                                Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show()
                                showMethodDialog = false
                                loadDepositMethods()
                            } else {
                                Toast.makeText(context, res.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0866FF))
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMethodDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ==========================================
    // DIALOG: REJECT DEPOSIT
    // ==========================================
    if (showRejectDepDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDepDialog = false },
            title = { Text("Reject Deposit Request", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Specify rejection reason (optional):", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectDepReasonText,
                        onValueChange = { rejectDepReasonText = it },
                        placeholder = { Text("e.g. Transaction ID not found in statement") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val res = viewModel.repository.adminRejectDeposit(
                                requestId = rejectingDepReqId,
                                reason = rejectDepReasonText.trim().ifEmpty { null }
                            )
                            if (res.isSuccess) {
                                Toast.makeText(context, "Deposit Rejected", Toast.LENGTH_SHORT).show()
                                showRejectDepDialog = false
                                loadDepositRequests(depReqTab)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("Reject")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDepDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AdminSectionButton(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Color(0xFF0866FF) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 11.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else SoftUITheme.TextSecondary
        )
    }
}

@Composable
private fun AdminFeatureToggleCard(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    accentColor: Color,
    isDark: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(if (isDark) Color(0xFF1E2128) else Color.White)
            .border(
                1.dp,
                if (isChecked) accentColor.copy(alpha = 0.4f) else (if (isDark) Color(0xFF282C37) else Color(0xFFE2E8F0)),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isChecked) accentColor else Color(0xFF94A3B8))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = SoftUITheme.TextDark
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = SoftUITheme.TextSecondary,
                    lineHeight = 17.sp
                )
            }
            Switch(
                checked = isChecked,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = accentColor,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)
                )
            )
        }
    }
}

