package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Post
import com.example.data.model.ReelModel
import com.example.ui.theme.FlikataPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareBottomSheet(
    post: Post? = null,
    reel: ReelModel? = null,
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    val isVideo = reel != null || (post != null && (!post.videoUrl.isNullOrBlank() || post.isVideo))
    val targetId = reel?.id ?: post?.id ?: ""
    val authorName = reel?.authorName ?: post?.authorName ?: "Flikata User"
    val authorAvatar = reel?.authorAvatar ?: post?.authorAvatar ?: ""
    val previewText = reel?.caption ?: post?.content ?: ""
    val previewMedia = reel?.mediaUrl ?: post?.videoUrl ?: post?.imageUrl

    // Live share URL that opens in App if installed or shows preview in browser
    val shareUrl = com.example.data.remote.SecureConfig.getPostShareUrl(targetId, isVideo || reel != null)

    val shareText = if (previewText.isNotBlank()) {
        "$authorName on Flikata: \"$previewText\"\n$shareUrl"
    } else {
        "Check out this ${if (isVideo) "video" else "post"} by $authorName on Flikata:\n$shareUrl"
    }

    fun copyLinkToClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Flikata Link", shareUrl)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
        onDismiss()
    }

    fun shareToApp(packageName: String?, appName: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "$authorName on Flikata")
            putExtra(Intent.EXTRA_TEXT, shareText)
            if (!packageName.isNullOrBlank()) {
                setPackage(packageName)
            }
        }
        try {
            context.startActivity(intent)
            onDismiss()
        } catch (e: Exception) {
            // If target app is not installed, open system chooser
            val chooser = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }, "Share via $appName")
            try {
                context.startActivity(chooser)
                onDismiss()
            } catch (err: Exception) {
                copyLinkToClipboard()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = surfaceColor,
        contentColor = onSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(textSecondary.copy(alpha = 0.35f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isVideo) "Share Video" else "Share Post",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = onSurface
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), thickness = 0.5.dp)

            Spacer(modifier = Modifier.height(12.dp))

            // Post preview snippet card
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FlikataAvatar(
                        name = authorName,
                        avatarUrl = authorAvatar,
                        size = 38.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = authorName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = onSurface
                        )
                        if (previewText.isNotBlank()) {
                            Text(
                                text = previewText,
                                fontSize = 12.sp,
                                color = textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Text(
                                text = if (isVideo) "Shared Video" else "Shared Post",
                                fontSize = 12.sp,
                                color = textSecondary
                            )
                        }
                    }
                    if (!previewMedia.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        AsyncImage(
                            model = previewMedia,
                            contentDescription = "Thumbnail",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Send via",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = textSecondary,
                modifier = Modifier.padding(start = 18.dp, end = 18.dp, bottom = 12.dp)
            )

            // Horizontal arranged row: WhatsApp, Facebook, Messenger, IMO, Copy Link, More
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. WhatsApp
                item {
                    ShareAppIconItem(
                        name = "WhatsApp",
                        badgeBackground = Brush.verticalGradient(listOf(Color(0xFF25D366), Color(0xFF128C7E))),
                        iconContent = {
                            Text("WA", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        },
                        onClick = { shareToApp("com.whatsapp", "WhatsApp") }
                    )
                }

                // 2. Facebook
                item {
                    ShareAppIconItem(
                        name = "Facebook",
                        badgeBackground = Brush.verticalGradient(listOf(Color(0xFF1877F2), Color(0xFF0D47A1))),
                        iconContent = {
                            Text("f", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
                        },
                        onClick = { shareToApp("com.facebook.katana", "Facebook") }
                    )
                }

                // 3. Messenger
                item {
                    ShareAppIconItem(
                        name = "Messenger",
                        badgeBackground = Brush.linearGradient(listOf(Color(0xFF00C6FF), Color(0xFF0078FF), Color(0xFFA033FF))),
                        iconContent = {
                            Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        },
                        onClick = { shareToApp("com.facebook.orca", "Messenger") }
                    )
                }

                // 4. IMO
                item {
                    ShareAppIconItem(
                        name = "IMO",
                        badgeBackground = Brush.verticalGradient(listOf(Color(0xFF00A3E0), Color(0xFF006699))),
                        iconContent = {
                            Text("imo", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        },
                        onClick = { shareToApp("com.imo.android.imoim", "IMO") }
                    )
                }

                // 5. Copy Link
                item {
                    ShareAppIconItem(
                        name = "Copy Link",
                        badgeBackground = Brush.verticalGradient(listOf(Color(0xFF45BD62), Color(0xFF2ECC71))),
                        iconContent = {
                            Icon(Icons.Default.Link, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        },
                        onClick = { copyLinkToClipboard() }
                    )
                }

                // 6. More Options
                item {
                    ShareAppIconItem(
                        name = "More",
                        badgeBackground = Brush.verticalGradient(listOf(Color(0xFF607D8B), Color(0xFF455A64))),
                        iconContent = {
                            Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        },
                        onClick = {
                            val chooser = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }, "Share via")
                            context.startActivity(chooser)
                            onDismiss()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), thickness = 0.5.dp)

            // Direct Quick Action Row for Copy Link
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { copyLinkToClipboard() }
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .testTag("share_sheet_copy_link_btn"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(FlikataPrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Link",
                        tint = FlikataPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Copy link to clipboard",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = onSurface
                    )
                    Text(
                        text = shareUrl,
                        fontSize = 11.sp,
                        color = textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ShareAppIconItem(
    name: String,
    badgeBackground: Brush,
    iconContent: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .testTag("share_app_${name.lowercase()}")
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(badgeBackground)
                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            iconContent()
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}
