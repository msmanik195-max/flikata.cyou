package com.example.ui.screens.pages

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.SoftUITheme
import com.example.ui.theme.FlikataPrimary
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

/**
 * Facebook-style Page Creation Screen
 * Includes Top Page Banner & Logo, Page Name, Category, Description,
 * Contact Email, Phone Number, and Website Link (Optional).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePageScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentUser by viewModel.currentUser.collectAsState()
    val pages by viewModel.pages.collectAsState()
    val maxPagesPerProfile by viewModel.maxPagesPerProfile.collectAsState()
    val featureSettings by viewModel.featureSettings.collectAsState()

    androidx.compose.runtime.LaunchedEffect(featureSettings.allowPageCreation) {
        if (!featureSettings.allowPageCreation || featureSettings.defaultPageCreateLimit == 0) {
            viewModel.featureNoticeDialog.value = "পেজ তৈরি করার সুবিধা সাময়িকভাবে বন্ধ আছে।"
            onBack()
        }
    }

    val ownedPages = remember(pages, currentUser) {
        pages.filter { it.userId == currentUser?.id }
    }
    val isLimitReached = ownedPages.size >= maxPagesPerProfile

    var pageName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Digital Creator") }
    var pageDescription by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }

    var bannerUri by remember { mutableStateOf<Uri?>(null) }
    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val categories = listOf(
        "Digital Creator",
        "Business & Brand",
        "Entertainment",
        "Personal Blog",
        "News & Media",
        "E-commerce & Store",
        "Community & Education"
    )

    val bannerPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            bannerUri = uri
        }
    }

    val logoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            logoUri = uri
        }
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
        // Top Header
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
                        text = "Create a Page",
                        style = TextStyle(
                            brush = Brush.horizontalGradient(listOf(Color(0xFF0075FF), Color(0xFF0052D4))),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE3F2FD)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = "Pages",
                        tint = Color(0xFF0075FF),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Scrollable Body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 36.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Limit status card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isLimitReached) Color(0xFFFEE2E2) else if (isDarkMode) Color(0xFF1E2638) else Color(0xFFE8F1FC))
                    .border(1.dp, if (isLimitReached) Color(0xFFEF4444) else Color(0x330075FF), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isLimitReached) Icons.Default.Info else Icons.Default.Flag,
                        contentDescription = null,
                        tint = if (isLimitReached) Color(0xFFDC2626) else Color(0xFF0075FF),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isLimitReached) {
                            "Limit Reached: You own ${ownedPages.size} of $maxPagesPerProfile allowed pages."
                        } else {
                            "Profile Page Limit: ${ownedPages.size} of $maxPagesPerProfile pages created"
                        },
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isLimitReached) Color(0xFFDC2626) else if (isDarkMode) Color(0xFF90CAF9) else Color(0xFF0052D4)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ==========================================
            // 1. VISUAL HEADER: COVER BANNER + LOGO
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .height(210.dp)
            ) {
                // Banner (Cover Photo)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                if (isDarkMode) listOf(Color(0xFF2C3240), Color(0xFF1E222A))
                                else listOf(Color(0xFF0075FF), Color(0xFF00C6FF))
                            )
                        )
                        .clickable { bannerPicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                        .testTag("create_page_banner_picker")
                ) {
                    if (bannerUri != null) {
                        AsyncImage(
                            model = bannerUri,
                            contentDescription = "Cover Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Add Cover",
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Add Page Cover Banner",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Edit Camera Badge on Banner
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xCC000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Edit Cover",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Overlapping Circular Page Logo Avatar
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = 16.dp, y = 0.dp)
                        .size(86.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(if (isDarkMode) Color(0xFF1E2128) else Color.White)
                        .border(3.dp, if (isDarkMode) Color(0xFF242730) else Color.White, CircleShape)
                        .clickable { logoPicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                        .testTag("create_page_logo_picker"),
                    contentAlignment = Alignment.Center
                ) {
                    if (logoUri != null) {
                        AsyncImage(
                            model = logoUri,
                            contentDescription = "Page Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Add Logo",
                                tint = Color(0xFF0075FF),
                                modifier = Modifier.size(26.dp)
                            )
                            Text(
                                text = "Logo",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0075FF)
                            )
                        }
                    }

                    // Edit Indicator
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0075FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Edit Logo",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // 2. PAGE DETAILS FORM CARD
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(18.dp), ambientColor = SoftUITheme.ShadowDark)
                    .clip(RoundedCornerShape(18.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorderBrush, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Page Information",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftUITheme.TextDark
                    )
                    Text(
                        text = "A page is where people can learn more about your brand, creator profile, or community.",
                        fontSize = 12.sp,
                        color = SoftUITheme.TextSecondary,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                    )

                    // Page Name
                    OutlinedTextField(
                        value = pageName,
                        onValueChange = { pageName = it },
                        label = { Text("Page Name *") },
                        placeholder = { Text("e.g. Flikata Tech, Sumon Vlogs") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create_page_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FlikataPrimary,
                            unfocusedBorderColor = if (isDarkMode) Color(0x33FFFFFF) else Color(0x33A0C0E8)
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Category
                    Text(
                        text = "Select Category",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SoftUITheme.TextDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FlikataPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = if (isDarkMode) Color(0xFF1E2128) else Color(0xFFF1F5F9),
                                    labelColor = SoftUITheme.TextDark
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bio / Description
                    OutlinedTextField(
                        value = pageDescription,
                        onValueChange = { pageDescription = it },
                        label = { Text("Bio / Description") },
                        placeholder = { Text("Tell your audience what this page is about...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create_page_bio_input"),
                        singleLine = false,
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FlikataPrimary,
                            unfocusedBorderColor = if (isDarkMode) Color(0x33FFFFFF) else Color(0x33A0C0E8)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // 3. CONTACT & WEB INFO (OPTIONAL)
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(18.dp), ambientColor = SoftUITheme.ShadowDark)
                    .clip(RoundedCornerShape(18.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorderBrush, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Contact & Links",
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftUITheme.TextDark
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(Optional)",
                            fontSize = 12.sp,
                            color = SoftUITheme.TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Contact Email") },
                        placeholder = { Text("contact@yourpage.com") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Email, contentDescription = "Email", tint = Color(0xFF0075FF), modifier = Modifier.size(18.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FlikataPrimary,
                            unfocusedBorderColor = if (isDarkMode) Color(0x33FFFFFF) else Color(0x33A0C0E8)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Phone Number
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        placeholder = { Text("+880 1XXXXXXXXX") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = "Phone", tint = Color(0xFF0075FF), modifier = Modifier.size(18.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FlikataPrimary,
                            unfocusedBorderColor = if (isDarkMode) Color(0x33FFFFFF) else Color(0x33A0C0E8)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Website Link
                    OutlinedTextField(
                        value = website,
                        onValueChange = { website = it },
                        label = { Text("Website Link") },
                        placeholder = { Text("https://yourwebsite.com") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Language, contentDescription = "Website", tint = Color(0xFF0075FF), modifier = Modifier.size(18.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FlikataPrimary,
                            unfocusedBorderColor = if (isDarkMode) Color(0x33FFFFFF) else Color(0x33A0C0E8)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ==========================================
            // 4. CREATE PAGE SUBMIT BUTTON
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .height(52.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x440075FF))
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF0075FF), Color(0xFF0052D4))))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = !isSubmitting,
                        onClick = {
                            if (!featureSettings.allowPageCreation || featureSettings.defaultPageCreateLimit == 0) {
                                viewModel.featureNoticeDialog.value = "পেজ তৈরি করার সুবিধা সাময়িকভাবে বন্ধ আছে।"
                                return@clickable
                            }
                            if (isLimitReached) {
                                viewModel.featureNoticeDialog.value = "আপনার পেজ তৈরির সর্বোচ্চ সীমা (${maxPagesPerProfile} টি) শেষ হয়েছে।"
                                return@clickable
                            }
                            if (pageName.trim().isBlank()) {
                                Toast.makeText(context, "Please enter a valid page name", Toast.LENGTH_SHORT).show()
                                return@clickable
                            }

                            isSubmitting = true
                            scope.launch {
                                val lUri = logoUri
                                val bUri = bannerUri
                                val cachedLogo = if (lUri != null) viewModel.cacheMediaFromUri(lUri, false) else null
                                val cachedBanner = if (bUri != null) viewModel.cacheMediaFromUri(bUri, false) else null

                                val res = viewModel.createPage(
                                    name = pageName.trim(),
                                    category = selectedCategory,
                                    bio = pageDescription.trim(),
                                    logoUrl = cachedLogo,
                                    bannerUrl = cachedBanner,
                                    email = email.trim().ifBlank { null },
                                    phone = phone.trim().ifBlank { null },
                                    website = website.trim().ifBlank { null }
                                )

                                isSubmitting = false
                                if (res.isSuccess) {
                                    val createdPage = res.getOrNull()
                                    Toast.makeText(context, "Page created successfully! Switched to ${createdPage?.name}", Toast.LENGTH_LONG).show()
                                    viewModel.openPageProfile(createdPage ?: return@launch)
                                } else {
                                    val errMsg = res.exceptionOrNull()?.message ?: "পেজ তৈরি করার সুবিধা সাময়িকভাবে বন্ধ আছে।"
                                    viewModel.featureNoticeDialog.value = errMsg
                                }
                            }
                        }
                    )
                    .testTag("submit_create_page_button"),
                contentAlignment = Alignment.Center
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Flag, contentDescription = "Create", tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Create Page",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
