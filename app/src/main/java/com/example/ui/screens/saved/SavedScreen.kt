package com.example.ui.screens.saved

import android.widget.Toast
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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Videocam
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.SavedItemEntity
import com.example.ui.components.FlikataAvatar
import com.example.ui.components.SoftUITheme
import com.example.ui.components.VerifiedBadge
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SavedScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val activeTab by viewModel.savedTab.collectAsState()
    val context = LocalContext.current

    val savedImages by viewModel.savedImages.collectAsState(initial = emptyList())
    val savedVideos by viewModel.savedVideos.collectAsState(initial = emptyList())
    val savedProducts by viewModel.savedProducts.collectAsState(initial = emptyList())

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

    val currentItems = when (activeTab) {
        "VIDEO" -> savedVideos
        "PRODUCT" -> savedProducts
        else -> savedImages
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
                            .testTag("saved_back_button"),
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
                        text = "Saved",
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

                // Saved Icon Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDarkMode) Color(0xFF1E2128) else Color(0xFFEBF5FF))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Saved",
                            tint = Color(0xFF0075FF),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${savedImages.size + savedVideos.size + savedProducts.size} Items",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0075FF)
                        )
                    }
                }
            }
        }

        // 3 Tabs: IMAGE, VIDEO, PRODUCT
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf(
                Triple("IMAGE", "Image", Icons.Default.Image),
                Triple("VIDEO", "Video", Icons.Default.Videocam),
                Triple("PRODUCT", "Product", Icons.Default.Storefront)
            )

            tabs.forEach { (tabKey, label, icon) ->
                val isSelected = activeTab == tabKey
                val count = when (tabKey) {
                    "VIDEO" -> savedVideos.size
                    "PRODUCT" -> savedProducts.size
                    else -> savedImages.size
                }

                val tabBrush = if (isSelected) {
                    Brush.horizontalGradient(listOf(Color(0xFF0075FF), Color(0xFF0052D4)))
                } else {
                    Brush.verticalGradient(
                        if (isDarkMode) listOf(Color(0xFF242730), Color(0xFF1E2128))
                        else listOf(Color.White, Color(0xFFF3F7FD))
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .shadow(if (isSelected) 3.dp else 1.dp, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .background(tabBrush)
                        .border(
                            1.dp,
                            if (isSelected) Color.Transparent else if (isDarkMode) Color(0x22FFFFFF) else Color(0x33A0C0E8),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { viewModel.savedTab.value = tabKey }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) Color.White else SoftUITheme.TextDark,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else SoftUITheme.TextDark,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        if (count > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "($count)",
                                color = if (isSelected) Color.White.copy(alpha = 0.85f) else Color(0xFF0075FF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Saved Content List
        if (currentItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "Empty Saved",
                        tint = SoftUITheme.TextSecondary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No Saved $activeTab Items",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftUITheme.TextDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (activeTab == "PRODUCT") {
                            "Save marketplace products by tapping the bookmark icon to view them later."
                        } else {
                            "Save posts and videos by tapping the 3-dots menu on any post in your feed."
                        },
                        fontSize = 13.sp,
                        color = SoftUITheme.TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(currentItems, key = { it.id }) { item ->
                    SavedItemCard(
                        item = item,
                        cardBg = cardBg,
                        cardBorderBrush = cardBorderBrush,
                        isDarkMode = isDarkMode,
                        onUnsave = {
                            viewModel.unsaveItem(item.id)
                            Toast.makeText(context, "Removed from saved", Toast.LENGTH_SHORT).show()
                        },
                        onClick = {
                            if (item.itemType == "PRODUCT") {
                                viewModel.navigateTo(Screen.MARKETPLACE)
                            } else if (item.itemType == "VIDEO") {
                                viewModel.openReel(item.originalId)
                            } else {
                                viewModel.openPost(item.originalId)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedItemCard(
    item: SavedItemEntity,
    cardBg: Color,
    cardBorderBrush: Brush,
    isDarkMode: Boolean,
    onUnsave: () -> Unit,
    onClick: () -> Unit
) {
    val dateText = remember(item.savedAt) {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        sdf.format(Date(item.savedAt))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDarkMode) 2.dp else 1.5.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = SoftUITheme.ShadowDark
            )
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, cardBorderBrush, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Media thumbnail
            if (!item.mediaUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDarkMode) Color(0xFF1E2128) else Color(0xFFF1F5F9))
                ) {
                    AsyncImage(
                        model = item.mediaUrl,
                        contentDescription = "Saved Media",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (item.itemType == "VIDEO") {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0x33000000)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xCC000000)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDarkMode) Color(0xFF1E2128) else Color(0xFFEBF5FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (item.itemType == "PRODUCT") Icons.Default.Storefront else Icons.Default.Bookmark,
                        contentDescription = "Item",
                        tint = Color(0xFF0075FF),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Information
            Column(modifier = Modifier.weight(1f)) {
                // Category or author
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!item.authorOrSellerAvatar.isNullOrBlank()) {
                        FlikataAvatar(
                            name = item.authorOrSellerName,
                            avatarUrl = item.authorOrSellerAvatar,
                            size = 18.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = item.authorOrSellerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        color = SoftUITheme.TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        VerifiedBadge(size = 12.dp)
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftUITheme.TextDark,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.subtitleOrPrice,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0075FF)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "· Saved $dateText",
                        fontSize = 10.5.sp,
                        color = SoftUITheme.TextSecondary
                    )
                }
            }

            // Unsave button
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (isDarkMode) Color(0xFF1E2128) else Color(0xFFEBF5FF))
                    .clickable { onUnsave() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkRemove,
                    contentDescription = "Unsave",
                    tint = Color(0xFF0075FF),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
