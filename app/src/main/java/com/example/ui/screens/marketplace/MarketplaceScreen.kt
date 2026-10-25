package com.example.ui.screens.marketplace

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MarketplaceProduct
import com.example.ui.components.FlikataAvatar
import com.example.ui.components.SoftUITheme
import com.example.ui.components.VerifiedBadge
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

data class MarketCategoryItem(
    val name: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val products by viewModel.marketplaceProducts.collectAsState()
    val selectedCategory by viewModel.selectedMarketplaceCategory.collectAsState()
    val searchQuery by viewModel.marketplaceSearchQuery.collectAsState()
    val isLoading by viewModel.isLoadingMarketplace.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showSellSheet by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<MarketplaceProduct?>(null) }
    var searchInput by remember { mutableStateOf(searchQuery) }

    val categories = remember {
        listOf(
            MarketCategoryItem("All", Icons.Default.Storefront),
            MarketCategoryItem("Mobile Phones", Icons.Default.PhoneAndroid),
            MarketCategoryItem("Electronics", Icons.Default.Smartphone),
            MarketCategoryItem("Vehicles", Icons.Default.DirectionsCar),
            MarketCategoryItem("Fashion", Icons.Default.Checkroom),
            MarketCategoryItem("Home & Living", Icons.Default.Apartment)
        )
    }

    LaunchedEffect(Unit) {
        viewModel.loadMarketplaceProducts()
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
        // ==========================================
        // 1. TOP HEADER (Back, Title, Sell Button)
        // ==========================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (isDarkMode) 2.dp else 1.dp,
                    shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                    ambientColor = SoftUITheme.ShadowDark
                )
                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                .background(headerBgBrush)
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Back Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(3.dp, CircleShape, ambientColor = SoftUITheme.ShadowDark)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color.White, Color(0xFFE8F1FC))))
                            .border(1.dp, Color(0xEEFFFFFF), CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onBack
                            )
                            .testTag("marketplace_back_button"),
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
                        text = "Marketplace",
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Refresh Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .shadow(2.dp, CircleShape, ambientColor = SoftUITheme.ShadowDark)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color.White, Color(0xFFE8F1FC))))
                            .border(1.dp, Color(0xEEFFFFFF), CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { viewModel.loadMarketplaceProducts() }
                            )
                            .testTag("marketplace_refresh_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF0075FF)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = Color(0xFF1E3A5F),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // "Sell" Button (+ Icon)
                    Box(
                        modifier = Modifier
                            .shadow(3.dp, RoundedCornerShape(20.dp), ambientColor = Color(0x440075FF))
                            .clip(RoundedCornerShape(20.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFF0075FF), Color(0xFF0052D4))))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { showSellSheet = true }
                            )
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                            .testTag("marketplace_sell_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Sell",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Sell",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2. SEARCH & LOCATION BAR
        // ==========================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(14.dp), ambientColor = SoftUITheme.ShadowDark)
                    .clip(RoundedCornerShape(14.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorderBrush, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 3.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = SoftUITheme.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = searchInput,
                        onValueChange = {
                            searchInput = it
                            if (it.isBlank()) {
                                viewModel.loadMarketplaceProducts(query = "")
                            }
                        },
                        placeholder = {
                            Text(
                                "Search Marketplace (e.g. iPhone, Bike, Laptop)...",
                                fontSize = 13.sp,
                                color = SoftUITheme.TextSecondary
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = SoftUITheme.TextDark,
                            unfocusedTextColor = SoftUITheme.TextDark,
                            cursorColor = Color(0xFF0075FF)
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                viewModel.loadMarketplaceProducts(query = searchInput.trim())
                            }
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    if (searchInput.isNotBlank()) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = SoftUITheme.TextSecondary,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable {
                                    searchInput = ""
                                    viewModel.loadMarketplaceProducts(query = "")
                                }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Location Indicator Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = Color(0xFF0075FF),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Dhaka, Bangladesh · Within 40 km",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SoftUITheme.TextSecondary
                )
            }
        }

        // ==========================================
        // 3. CATEGORY PILLS (Horizontal Scroll)
        // ==========================================
        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            items(categories) { cat ->
                val isSelected = selectedCategory.equals(cat.name, ignoreCase = true)
                val pillBg = if (isSelected) {
                    Brush.horizontalGradient(listOf(Color(0xFF0075FF), Color(0xFF0052D4)))
                } else {
                    Brush.verticalGradient(
                        if (isDarkMode) listOf(Color(0xFF242730), Color(0xFF1E2128))
                        else listOf(Color.White, Color(0xFFF3F7FD))
                    )
                }
                val textColor = if (isSelected) Color.White else SoftUITheme.TextDark

                Box(
                    modifier = Modifier
                        .shadow(if (isSelected) 3.dp else 1.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(pillBg)
                        .border(
                            1.dp,
                            if (isSelected) Color.Transparent else if (isDarkMode) Color(0x22FFFFFF) else Color(0x33A0C0E8),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                if (selectedCategory != cat.name) {
                                    viewModel.loadMarketplaceProducts(category = cat.name)
                                }
                            }
                        )
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = cat.icon,
                            contentDescription = cat.name,
                            tint = textColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = cat.name,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                            fontSize = 12.5.sp,
                            color = textColor
                        )
                    }
                }
            }
        }

        // ==========================================
        // 4. PRODUCT GRID (2-Column Feed)
        // ==========================================
        if (products.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = "Empty",
                        tint = SoftUITheme.TextSecondary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No listings found",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftUITheme.TextDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Be the first one to post a product in this category!",
                        fontSize = 13.sp,
                        color = SoftUITheme.TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .shadow(3.dp, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFF0075FF), Color(0xFF0052D4))))
                            .clickable { showSellSheet = true }
                            .padding(horizontal = 18.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "List a Product",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(products, key = { it.id }) { item ->
                    ProductGridCard(
                        product = item,
                        cardBg = cardBg,
                        cardBorderBrush = cardBorderBrush,
                        isDarkMode = isDarkMode,
                        onClick = { selectedProduct = item },
                        onSave = {
                            viewModel.toggleSaveProduct(item)
                            Toast.makeText(context, "Product saved to your collection!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    // ==========================================
    // 5. PRODUCT DETAILS BOTTOM SHEET
    // ==========================================
    if (selectedProduct != null) {
        val prod = selectedProduct!!
        val isSeller = currentUser?.id == prod.userId
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { selectedProduct = null },
            sheetState = sheetState,
            containerColor = cardBg,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .padding(bottom = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top bar of sheet
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Listing Details",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftUITheme.TextDark
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Bookmark/Save Product Button
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isDarkMode) Color(0xFF333842) else Color(0xFFE3F2FD))
                                .clickable {
                                    viewModel.toggleSaveProduct(prod)
                                    Toast.makeText(context, "Product saved to your collection!", Toast.LENGTH_SHORT).show()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "Save Product",
                                tint = Color(0xFF0075FF),
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isDarkMode) Color(0xFF333842) else Color(0xFFF0F4F8))
                                .clickable { selectedProduct = null },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = SoftUITheme.TextDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Product Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDarkMode) Color(0xFF1E2128) else Color(0xFFF1F5F9))
                ) {
                    if (!prod.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = prod.imageUrl,
                            contentDescription = prod.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = "No Image",
                                tint = SoftUITheme.TextSecondary,
                                modifier = Modifier.size(54.dp)
                            )
                        }
                    }

                    // Condition Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xCC000000))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = prod.condition,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Price & Title
                Text(
                    text = prod.formattedPrice,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0075FF)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = prod.title,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftUITheme.TextDark
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Location & Posted Date
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${prod.location} · Listed ${prod.createdAt}",
                        fontSize = 12.5.sp,
                        color = SoftUITheme.TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description Box
                Text(
                    text = "Description",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = SoftUITheme.TextDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDarkMode) Color(0xFF1E2128) else Color(0xFFF8FAFC))
                        .border(1.dp, if (isDarkMode) Color(0x22FFFFFF) else Color(0x33A0C0E8), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = prod.description.ifBlank { "No detailed description provided by seller." },
                        fontSize = 13.5.sp,
                        color = SoftUITheme.TextDark,
                        lineHeight = 19.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Seller Information Card
                Text(
                    text = "Seller Information",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = SoftUITheme.TextDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isDarkMode) Color(0xFF1E2128) else Color(0xFFF8FAFC))
                        .border(1.dp, if (isDarkMode) Color(0x22FFFFFF) else Color(0x33A0C0E8), RoundedCornerShape(14.dp))
                        .clickable {
                            viewModel.openUserProfile(prod.userId, prod.sellerName)
                            selectedProduct = null
                        }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FlikataAvatar(
                                avatarUrl = prod.sellerAvatar,
                                name = prod.sellerName,
                                size = 46.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = prod.sellerName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.5.sp,
                                        color = SoftUITheme.TextDark
                                    )
                                    if (prod.sellerVerified) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        VerifiedBadge(size = 14.dp)
                                    }
                                }
                                Text(
                                    text = "Member on Flikata",
                                    fontSize = 11.5.sp,
                                    color = SoftUITheme.TextSecondary
                                )
                            }
                        }

                        Text(
                            text = "View Profile ➔",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0075FF)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Primary Action Button: "Message Seller" with custom inquiry
                if (!isSeller) {
                    var inquiryMessage by remember(prod.id) {
                        mutableStateOf("Hi ${prod.sellerName}, is this item still available? [${prod.title} - ${prod.formattedPrice}]")
                    }
                    val quickQuestions = listOf(
                        "Is this still available?",
                        "Is the price negotiable?",
                        "What is the condition?",
                        "Where can we meet?"
                    )

                    Text(
                        text = "Send seller a message",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = SoftUITheme.TextDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        items(quickQuestions) { q ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isDarkMode) Color(0xFF1E2128) else Color(0xFFF1F5F9))
                                    .border(1.dp, if (isDarkMode) Color(0x33FFFFFF) else Color(0x33A0C0E8), RoundedCornerShape(16.dp))
                                    .clickable {
                                        inquiryMessage = "Hi ${prod.sellerName}, $q [${prod.title}]"
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = q,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0075FF)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = inquiryMessage,
                        onValueChange = { inquiryMessage = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("Write your inquiry to the seller...", fontSize = 13.sp) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .shadow(4.dp, RoundedCornerShape(14.dp), ambientColor = Color(0x440075FF))
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFF0075FF), Color(0xFF0052D4))))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    val finalMsg = inquiryMessage.trim().ifBlank {
                                        "Hi ${prod.sellerName}, is this item still available? [${prod.title} - ${prod.formattedPrice}]"
                                    }
                                    viewModel.messageSeller(prod, finalMsg)
                                    selectedProduct = null
                                    Toast.makeText(context, "Opening chat with ${prod.sellerName}...", Toast.LENGTH_SHORT).show()
                                }
                            )
                            .testTag("marketplace_message_seller_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = "Message",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Message Seller",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // Own product actions
                    Text(
                        text = "You are selling this item",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0075FF),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Delete Listing if own product
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFFF1F2))
                            .border(1.dp, Color(0xFFF43F5E), RoundedCornerShape(14.dp))
                            .clickable {
                                viewModel.deleteMarketplaceProduct(prod.id)
                                selectedProduct = null
                                Toast.makeText(context, "Listing deleted successfully", Toast.LENGTH_SHORT).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFFE11D48),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Delete Listing",
                                color = Color(0xFFE11D48),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // 6. SELL / CREATE PRODUCT LISTING SHEET
    // ==========================================
    if (showSellSheet) {
        val sellSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var titleInput by remember { mutableStateOf("") }
        var priceInput by remember { mutableStateOf("") }
        var categoryInput by remember { mutableStateOf("Mobile Phones") }
        var conditionInput by remember { mutableStateOf("Used - Like New") }
        var locationInput by remember { mutableStateOf("Dhanmondi, Dhaka") }
        var descriptionInput by remember { mutableStateOf("") }
        var imageUrlInput by remember { mutableStateOf("") }
        var isSubmitting by remember { mutableStateOf(false) }
        var isUploadingPhoto by remember { mutableStateOf(false) }

        val galleryPicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                isUploadingPhoto = true
                scope.launch {
                    try {
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val bytes = inputStream?.readBytes()
                        inputStream?.close()
                        if (bytes != null && bytes.isNotEmpty()) {
                            val uploadRes = viewModel.repository.uploadMediaFile(
                                bytes = bytes,
                                fileName = "prod_${System.currentTimeMillis()}.jpg",
                                mimeType = "image/jpeg",
                                folder = "products"
                            )
                            if (uploadRes.isSuccess && !uploadRes.getOrNull().isNullOrBlank()) {
                                imageUrlInput = uploadRes.getOrNull()!!
                                Toast.makeText(context, "Photo uploaded successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                imageUrlInput = uri.toString()
                            }
                        } else {
                            imageUrlInput = uri.toString()
                        }
                    } catch (e: Exception) {
                        imageUrlInput = uri.toString()
                    } finally {
                        isUploadingPhoto = false
                    }
                }
            }
        }

        val samplePhotos = listOf(
            "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=600&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1558981403-c5f9899a28bc?w=600&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1533867617858-e7b97e060509?w=600&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1617806118233-18e1de247200?w=600&auto=format&fit=crop&q=80"
        )

        ModalBottomSheet(
            onDismissRequest = { showSellSheet = false },
            sheetState = sellSheetState,
            containerColor = cardBg,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .padding(bottom = 28.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Create New Listing",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftUITheme.TextDark
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isDarkMode) Color(0xFF333842) else Color(0xFFF0F4F8))
                            .clickable { showSellSheet = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = SoftUITheme.TextDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Product Photo Section with Gallery Picker & Presets
                Text(
                    text = "Product Photo",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftUITheme.TextDark
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Gallery Picker Action Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDarkMode) Color(0xFF1E2128) else Color(0xFFEBF5FF))
                        .border(1.2.dp, Brush.horizontalGradient(listOf(Color(0xFF0075FF), Color(0xFF0052D4))), RoundedCornerShape(12.dp))
                        .clickable(enabled = !isUploadingPhoto) {
                            galleryPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isUploadingPhoto) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF0075FF), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Uploading photo...", fontSize = 13.sp, color = Color(0xFF0075FF), fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = "Gallery",
                                tint = Color(0xFF0075FF),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (imageUrlInput.isBlank()) "+ Choose Photo from Gallery" else "✓ Photo Selected (Tap to change)",
                                color = Color(0xFF0075FF),
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (imageUrlInput.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDarkMode) Color(0xFF1E2128) else Color(0xFFF1F5F9))
                    ) {
                        AsyncImage(
                            model = imageUrlInput,
                            contentDescription = "Selected Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xCC000000))
                                .clickable { imageUrlInput = "" },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Or pick a popular preset photo:",
                    fontSize = 11.5.sp,
                    color = SoftUITheme.TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(samplePhotos) { photoUrl ->
                        val isChosen = imageUrlInput == photoUrl
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    if (isChosen) 2.5.dp else 1.dp,
                                    if (isChosen) Color(0xFF0075FF) else Color(0x33A0C0E8),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { imageUrlInput = photoUrl }
                        ) {
                            AsyncImage(
                                model = photoUrl,
                                contentDescription = "Sample",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (isChosen) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0075FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title Input
                Text(
                    text = "Title",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftUITheme.TextDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    placeholder = { Text("What are you selling?", fontSize = 13.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Price Input
                Text(
                    text = "Price (৳ BDT)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftUITheme.TextDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = priceInput,
                    onValueChange = { priceInput = it.filter { char -> char.isDigit() } },
                    placeholder = { Text("e.g. 5000", fontSize = 13.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category Selector
                Text(
                    text = "Category",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftUITheme.TextDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                val catOptions = listOf("Mobile Phones", "Electronics", "Vehicles", "Fashion", "Home & Living")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(catOptions) { cat ->
                        val isChosen = categoryInput == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isChosen) Color(0xFF0075FF) else if (isDarkMode) Color(0xFF1E2128) else Color(0xFFF1F5F9))
                                .clickable { categoryInput = cat }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                color = if (isChosen) Color.White else SoftUITheme.TextDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Condition Selector
                Text(
                    text = "Condition",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftUITheme.TextDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                val condOptions = listOf("New", "Used - Like New", "Used - Good", "Used - Fair")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(condOptions) { cond ->
                        val isChosen = conditionInput == cond
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isChosen) Color(0xFF0075FF) else if (isDarkMode) Color(0xFF1E2128) else Color(0xFFF1F5F9))
                                .clickable { conditionInput = cond }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cond,
                                fontSize = 12.sp,
                                fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                color = if (isChosen) Color.White else SoftUITheme.TextDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Location Input
                Text(
                    text = "Location",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftUITheme.TextDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = locationInput,
                    onValueChange = { locationInput = it },
                    placeholder = { Text("e.g. Dhanmondi, Dhaka", fontSize = 13.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description Input
                Text(
                    text = "Description",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftUITheme.TextDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = descriptionInput,
                    onValueChange = { descriptionInput = it },
                    placeholder = { Text("Details about condition, box, warranty, etc.", fontSize = 13.sp) },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Submit Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .shadow(4.dp, RoundedCornerShape(14.dp), ambientColor = Color(0x440075FF))
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFF0075FF), Color(0xFF0052D4))))
                        .clickable(enabled = !isSubmitting) {
                            if (titleInput.isBlank()) {
                                Toast.makeText(context, "Please enter a title", Toast.LENGTH_SHORT).show()
                                return@clickable
                            }
                            if (imageUrlInput.isBlank()) {
                                Toast.makeText(context, "Please choose a product photo to list", Toast.LENGTH_SHORT).show()
                                return@clickable
                            }
                            val priceVal = priceInput.toDoubleOrNull() ?: 0.0
                            isSubmitting = true
                            scope.launch {
                                val res = viewModel.createMarketplaceProduct(
                                    title = titleInput.trim(),
                                    price = priceVal,
                                    category = categoryInput,
                                    condition = conditionInput,
                                    location = locationInput.trim(),
                                    description = descriptionInput.trim(),
                                    imageUrl = imageUrlInput.ifBlank { samplePhotos.first() }
                                )
                                isSubmitting = false
                                if (res.isSuccess) {
                                    Toast.makeText(context, "Product listed successfully on Marketplace!", Toast.LENGTH_LONG).show()
                                    showSellSheet = false
                                } else {
                                    Toast.makeText(context, "Failed to list product", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        .testTag("marketplace_publish_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = "Publish Listing",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.5.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * 2-Column Product Card in Modern Marketplace Style
 */
@Composable
private fun ProductGridCard(
    product: MarketplaceProduct,
    cardBg: Color,
    cardBorderBrush: Brush,
    isDarkMode: Boolean,
    onClick: () -> Unit,
    onSave: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDarkMode) 2.dp else 1.5.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = SoftUITheme.ShadowDark
            )
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(1.dp, cardBorderBrush, RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag("marketplace_card_${product.id}")
    ) {
        Column {
            // Square Product Image (1:1 aspect ratio)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(if (isDarkMode) Color(0xFF1E2128) else Color(0xFFF1F5F9))
            ) {
                if (!product.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = "Product",
                            tint = SoftUITheme.TextSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Condition Tag on bottom start of image
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xB3000000))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = product.condition,
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Bookmark/Save button on top-right of image
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xB3000000))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onSave
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "Save Product",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            // Info Column
            Column(modifier = Modifier.padding(10.dp)) {
                // Price (Bold & Big)
                Text(
                    text = product.formattedPrice,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = Color(0xFF0075FF),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Title (2 lines max)
                Text(
                    text = product.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = SoftUITheme.TextDark,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Location & Relative Date
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = SoftUITheme.TextSecondary,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = product.location,
                        fontSize = 10.5.sp,
                        color = SoftUITheme.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
