package com.example.ui.screens.chats

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FlikataAvatar
import com.example.ui.components.SoftUITheme
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.FlikataError
import com.example.ui.theme.FlikataPrimary
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt

data class QuotedMessage(
    val id: String,
    val senderName: String,
    val text: String
)

data class DetailMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val isMe: Boolean = true,
    val time: String = "Just now",
    val replyTo: QuotedMessage? = null,
    val isEdited: Boolean = false,
    val isCallNotice: Boolean = false
)

/**
 * 3D Neumorphic Chat Detail Screen with text-only messaging, verified profile avatars,
 * and high-fidelity audio calling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val chat by viewModel.selectedChat.collectAsState()
    val activeMarketplaceProduct by viewModel.activeMarketplaceInquiryProduct.collectAsState()

    val rawPartnerId = (chat?.senderId?.takeIf { it.isNotBlank() && it != "partner" } ?: chat?.id)
        ?.removePrefix("user_")?.removePrefix("partner_")?.trim() ?: "1"
    val partnerId = rawPartnerId
    val partnerName = chat?.senderName ?: "Chat Partner"
    val partnerAvatar = chat?.senderAvatar ?: ""
    val isVerified = chat?.isVerified ?: false

    val myName = currentUser?.fullName ?: "You"
    val myAvatar = currentUser?.profilePic ?: ""

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    // Replying and Editing state
    var replyingTo by remember { mutableStateOf<DetailMessage?>(null) }
    var editingMessage by remember { mutableStateOf<DetailMessage?>(null) }

    // Message action bottom sheet state
    var selectedMessageForAction by remember { mutableStateOf<DetailMessage?>(null) }
    val actionSheetState = rememberModalBottomSheetState()

    var isPartnerOnline by remember(partnerId) { mutableStateOf(chat?.isOnline ?: false) }

    val context = LocalContext.current
    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val pId = partnerId.removePrefix("user_").removePrefix("partner_").toIntOrNull() ?: 1
            if (!isPartnerOnline) {
                Toast.makeText(context, "Offline", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.startAudioCall(
                    partnerId = pId,
                    partnerName = partnerName,
                    partnerAvatar = partnerAvatar,
                    isVerified = isVerified
                )
            }
        } else {
            Toast.makeText(context, "Microphone permission is required to make audio calls", Toast.LENGTH_SHORT).show()
        }
    }

    // Audio call state
    val isDark = SoftUITheme.isDark

    // Live conversation messages sync from real database
    val messages = remember(partnerId) { mutableStateListOf<DetailMessage>() }

    LaunchedEffect(partnerId) {
        val initialMsg = chat?.message?.takeIf { it.isNotBlank() && it != "Active now" }
        if (messages.isEmpty() && initialMsg != null) {
            messages.add(
                DetailMessage(
                    id = "initial_${chat?.id}",
                    text = initialMsg,
                    isMe = false,
                    time = chat?.timestamp ?: "Just now"
                )
            )
        }
        val inqProd = viewModel.activeMarketplaceInquiryProduct.value
        if (inqProd != null && messages.isEmpty()) {
            messages.add(
                DetailMessage(
                    id = "inquiry_${inqProd.id}",
                    text = "Hi $partnerName, is this item still available? [${inqProd.title} - ${inqProd.formattedPrice}]",
                    isMe = true,
                    time = "Just now"
                )
            )
        }
        // Poll for new messages and partner online status every 3 seconds while in conversation
        while (true) {
            val profRes = viewModel.repository.fetchUserProfile(partnerId)
            if (profRes.isSuccess) {
                val prof = profRes.getOrNull()
                if (prof != null) {
                    isPartnerOnline = prof.isOnlineBool
                }
            }

            val res = viewModel.repository.fetchConversationMessages(partnerId)
            if (res.isSuccess) {
                val serverMsgs = res.getOrNull() ?: emptyList()
                if (serverMsgs.isNotEmpty()) {
                    val converted = serverMsgs.filter { it.text.isNotBlank() }.map { sMsg ->
                        DetailMessage(
                            id = sMsg.id,
                            text = sMsg.text,
                            isMe = sMsg.isMe,
                            time = sMsg.time,
                            isEdited = sMsg.isEdited,
                            isCallNotice = sMsg.isCallNotice
                        )
                    }
                    if (converted.isNotEmpty()) {
                        messages.clear()
                        messages.addAll(converted)
                    }
                }
            }
            delay(3000)
        }
    }

    Scaffold(
        topBar = {
            // 3D Neumorphic Top Bar
            val isDark = SoftUITheme.isDark
            val chatHeaderBg = if (isDark) {
                Brush.verticalGradient(listOf(Color(0xFF242730), Color(0xFF1E2128), Color(0xFF16181E)))
            } else {
                Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF4F8FD), Color(0xFFE9F2FC)))
            }
            val chatHeaderBorder = if (isDark) {
                Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x11FFFFFF)))
            } else {
                Brush.verticalGradient(listOf(Color(0xEEFFFFFF), Color(0x33A0C0E8)))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (isDark) 2.dp else 1.dp,
                        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                        ambientColor = if (isDark) Color(0x33000000) else Color(0x0A000000),
                        spotColor = if (isDark) Color(0x44000000) else Color(0x0C000000)
                    )
                    .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                    .background(chatHeaderBg)
                    .border(
                        width = 1.dp,
                        brush = chatHeaderBorder,
                        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Back button & Partner Profile
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // 3D Circular Back Button
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .shadow(
                                    elevation = 2.dp,
                                    shape = CircleShape,
                                    ambientColor = SoftUITheme.ShadowDark,
                                    spotColor = SoftUITheme.ShadowDark
                                )
                                .clip(CircleShape)
                                .background(
                                    if (isDark) Brush.linearGradient(listOf(Color(0xFF2C303B), Color(0xFF20232B)))
                                    else Brush.linearGradient(listOf(Color.White, Color(0xFFE8F1FC)))
                                )
                                .border(1.dp, if (isDark) Color(0x33FFFFFF) else Color(0xEEFFFFFF), CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onBack
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = if (isDark) Color(0xFFE4E6EB) else Color(0xFF1E3A5F),
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Partner Avatar with 3D Ring & Online Status Dot
                        Box {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .shadow(3.dp, CircleShape, ambientColor = SoftUITheme.ShadowDark)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.5.dp, Color(0x99FFFFFF), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                FlikataAvatar(
                                    name = partnerName,
                                    avatarUrl = partnerAvatar,
                                    size = 40.dp
                                )
                            }

                            // Green Online Badge (Only when active)
                            if (isPartnerOnline) {
                                Box(
                                    modifier = Modifier
                                        .size(13.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF45BD62))
                                        .border(2.dp, Color.White, CircleShape)
                                        .align(Alignment.BottomEnd)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Partner Name & Status
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = partnerName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.5.sp,
                                    color = SoftUITheme.TextDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (isVerified) {
                                    Spacer(modifier = Modifier.width(5.dp))
                                    VerifiedBadge(size = 22.dp)
                                }
                                if (isPartnerOnline) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(9.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF45BD62))
                                    )
                                }
                            }
                            Text(
                                text = if (isPartnerOnline) "Active now" else "Offline",
                                fontSize = 11.sp,
                                color = if (isPartnerOnline) Color(0xFF45BD62) else Color(0xFF8E8E93),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Right: 3D Glossy Audio Call Button (Audio calling verified)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(
                                elevation = 5.dp,
                                shape = CircleShape,
                                ambientColor = Color(0x660075FF),
                                spotColor = Color(0x880052D4)
                            )
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF0075FF),
                                        Color(0xFF0052D4)
                                    )
                                )
                            )
                            .border(
                                width = 1.2.dp,
                                brush = Brush.verticalGradient(
                                    listOf(Color(0xEEFFFFFF), Color(0x44FFFFFF))
                                ),
                                shape = CircleShape
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    val hasMic = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (hasMic) {
                                        val pId = partnerId.removePrefix("user_").removePrefix("partner_").toIntOrNull() ?: 1
                                        if (!isPartnerOnline) {
                                            Toast.makeText(context, "Offline", Toast.LENGTH_SHORT).show()
                                        } else {
                                            viewModel.startAudioCall(
                                                partnerId = pId,
                                                partnerName = partnerName,
                                                partnerAvatar = partnerAvatar,
                                                isVerified = isVerified
                                            )
                                        }
                                    } else {
                                        micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            )
                            .testTag("chat_audio_call_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        // Top specular gloss highlight arc
                        Canvas(
                            modifier = Modifier
                                .size(40.dp)
                                .align(Alignment.TopCenter)
                        ) {
                            drawArc(
                                brush = Brush.verticalGradient(
                                    listOf(Color(0x66FFFFFF), Color.Transparent)
                                ),
                                startAngle = 180f,
                                sweepAngle = 180f,
                                useCenter = true,
                                size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.5f)
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Audio Call",
                            tint = Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFFF3F7FD)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            SoftUITheme.BackgroundGradientTop,
                            SoftUITheme.BackgroundGradientBottom
                        )
                    )
                )
                .imePadding()
        ) {
            // Marketplace Product Inquiry Top Banner
            if (activeMarketplaceProduct != null) {
                val prod = activeMarketplaceProduct!!
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .shadow(2.dp, RoundedCornerShape(14.dp), ambientColor = SoftUITheme.ShadowDark)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isDark) Color(0xFF242730) else Color.White)
                        .border(
                            1.dp,
                            if (isDark) Color(0x33FFFFFF) else Color(0x33A0C0E8),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Product Thumbnail
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isDark) Color(0xFF1E2128) else Color(0xFFF1F5F9))
                        ) {
                            if (!prod.imageUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = prod.imageUrl,
                                    contentDescription = prod.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Marketplace Inquiry",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0075FF)
                            )
                            Text(
                                text = prod.title,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftUITheme.TextDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${prod.formattedPrice} · ${prod.location}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0075FF)
                            )
                        }

                        // Close button to dismiss banner
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF333842) else Color(0xFFF0F4F8))
                                .clickable { viewModel.activeMarketplaceInquiryProduct.value = null },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = SoftUITheme.TextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            // Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    SwipeToReplyContainer(
                        onReply = { replyingTo = msg }
                    ) {
                        ChatBubbleWithAvatars(
                            msg = msg,
                            partnerName = partnerName,
                            partnerAvatar = partnerAvatar,
                            myName = myName,
                            myAvatar = myAvatar,
                            onLongClick = { selectedMessageForAction = msg }
                        )
                    }
                }
            }

            // Replying To Banner
            AnimatedVisibility(
                visible = replyingTo != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                if (replyingTo != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .shadow(3.dp, RoundedCornerShape(14.dp))
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0x330075FF), RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
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
                                        .width(3.dp)
                                        .height(30.dp)
                                        .background(Color(0xFF0075FF), RoundedCornerShape(2.dp))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Replying to ${if (replyingTo!!.isMe) "yourself" else partnerName}",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0075FF)
                                    )
                                    Text(
                                        text = replyingTo!!.text,
                                        fontSize = 12.sp,
                                        color = SoftUITheme.TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            IconButton(
                                onClick = { replyingTo = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel Reply",
                                    tint = SoftUITheme.TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Editing Banner
            AnimatedVisibility(
                visible = editingMessage != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                if (editingMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .shadow(3.dp, RoundedCornerShape(14.dp))
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFFF9E6))
                            .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
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
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Editing message",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD97706)
                                    )
                                    Text(
                                        text = editingMessage!!.text,
                                        fontSize = 12.sp,
                                        color = Color(0xFF92400E),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    editingMessage = null
                                    inputText = ""
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel Edit",
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3D Neumorphic Text-Only Input Bar (NO Image/Video sending as strictly instructed)
            val inputBarBg = if (isDark) {
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF242730),
                        Color(0xFF1E2128),
                        Color(0xFF16181E)
                    )
                )
            } else {
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFF4F8FD),
                        Color(0xFFE8F1FC)
                    )
                )
            }
            val inputPillBg = if (isDark) Color(0xFF1E2128) else Color(0xFFF3F7FD)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (isDark) 4.dp else 4.dp,
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                        ambientColor = SoftUITheme.ShadowDark,
                        spotColor = SoftUITheme.ShadowDark
                    )
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(inputBarBg)
                    .border(
                        width = 1.dp,
                        brush = if (isDark) Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x11FFFFFF)))
                        else Brush.verticalGradient(listOf(Color(0xEEFFFFFF), Color(0x33A0C0E8))),
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 9.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Soft 3D Recessed Text Input Pill
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .shadow(
                                elevation = 1.dp,
                                shape = RoundedCornerShape(22.dp),
                                ambientColor = SoftUITheme.ShadowDark
                            )
                            .clip(RoundedCornerShape(22.dp))
                            .background(inputPillBg)
                            .border(
                                width = 1.dp,
                                brush = if (isDark) Brush.verticalGradient(listOf(Color(0x22FFFFFF), Color(0x08FFFFFF)))
                                else Brush.verticalGradient(listOf(Color(0x66A0C0E8), Color(0xEEFFFFFF))),
                                shape = RoundedCornerShape(22.dp)
                            )
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("chat_input_field"),
                            textStyle = TextStyle(
                                color = SoftUITheme.TextDark,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            cursorBrush = SolidColor(Color(0xFF0075FF)),
                            maxLines = 4,
                            decorationBox = { innerTextField ->
                                if (inputText.isEmpty()) {
                                    Text(
                                        text = if (editingMessage != null) "Edit message..." else "Type text message...",
                                        color = SoftUITheme.TextMuted,
                                        fontSize = 14.sp
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // 3D Send / Quick Thumbs Up Button
                    if (editingMessage != null) {
                        // Save edit button
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .shadow(4.dp, CircleShape, ambientColor = Color(0x6645BD62))
                                .clip(CircleShape)
                                .background(Color(0xFF45BD62))
                                .border(1.2.dp, Color(0x88FFFFFF), CircleShape)
                                .clickable {
                                    if (inputText.isNotBlank()) {
                                        val idx = messages.indexOfFirst { it.id == editingMessage!!.id }
                                        if (idx != -1) {
                                            messages[idx] = messages[idx].copy(
                                                text = inputText.trim(),
                                                isEdited = true
                                            )
                                        }
                                        editingMessage = null
                                        inputText = ""
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save Edit",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else if (inputText.isNotBlank()) {
                        // Send Text Button
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .shadow(
                                    elevation = 6.dp,
                                    shape = CircleShape,
                                    ambientColor = Color(0x660075FF),
                                    spotColor = Color(0xAA0052D4)
                                )
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF0075FF), Color(0xFF0052D4))
                                    )
                                )
                                .border(
                                    1.5.dp,
                                    Brush.verticalGradient(
                                        listOf(Color(0xEEFFFFFF), Color(0x33FFFFFF))
                                    ),
                                    CircleShape
                                )
                                .clickable {
                                    val textToSend = inputText.trim()
                                    val currentTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
                                    messages.add(
                                        DetailMessage(
                                            id = UUID.randomUUID().toString(),
                                            text = textToSend,
                                            isMe = true,
                                            time = currentTime,
                                            replyTo = replyingTo?.let {
                                                QuotedMessage(
                                                    id = it.id,
                                                    senderName = if (it.isMe) "You" else partnerName,
                                                    text = it.text
                                                )
                                            }
                                        )
                                    )
                                    inputText = ""
                                    replyingTo = null

                                    // Persist message & trigger server sync
                                    viewModel.sendMessage(
                                        partnerId = partnerId,
                                        partnerName = partnerName,
                                        partnerAvatar = partnerAvatar,
                                        messageText = textToSend
                                    )

                                    coroutineScope.launch {
                                        listState.animateScrollToItem(messages.size - 1)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send Message",
                                tint = Color.White,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    } else {
                        // Quick Like Thumbs Up Button
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .shadow(
                                    elevation = 4.dp,
                                    shape = CircleShape,
                                    ambientColor = SoftUITheme.ShadowDark
                                )
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color.White, Color(0xFFE8F1FC))
                                    )
                                )
                                .border(1.dp, Color(0xEEFFFFFF), CircleShape)
                                .clickable {
                                    val currentTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
                                    messages.add(
                                        DetailMessage(
                                            id = UUID.randomUUID().toString(),
                                            text = "👍",
                                            isMe = true,
                                            time = currentTime
                                        )
                                    )
                                    viewModel.sendMessage(
                                        partnerId = partnerId,
                                        partnerName = partnerName,
                                        partnerAvatar = partnerAvatar,
                                        messageText = "👍"
                                    )
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(messages.size - 1)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ThumbUp,
                                contentDescription = "Send Like",
                                tint = Color(0xFF0075FF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Message Action Bottom Sheet (Reply, Edit, Copy, Delete)
    if (selectedMessageForAction != null) {
        val targetMsg = selectedMessageForAction!!
        ModalBottomSheet(
            onDismissRequest = { selectedMessageForAction = null },
            sheetState = actionSheetState,
            containerColor = Color(0xFFF7FAFE),
            shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "Message Actions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftUITheme.TextDark,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Reply Action
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            replyingTo = targetMsg
                            selectedMessageForAction = null
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Reply,
                        contentDescription = "Reply",
                        tint = Color(0xFF0075FF)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(text = "Reply", fontSize = 15.sp, color = SoftUITheme.TextDark, fontWeight = FontWeight.Medium)
                }

                // Copy Action
                if (targetMsg.text.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                clipboardManager.setText(AnnotatedString(targetMsg.text))
                                selectedMessageForAction = null
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = SoftUITheme.IconTint
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(text = "Copy text", fontSize = 15.sp, color = SoftUITheme.TextDark, fontWeight = FontWeight.Medium)
                    }
                }

                // Edit Action (Only for own messages)
                if (targetMsg.isMe) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                editingMessage = targetMsg
                                inputText = targetMsg.text
                                selectedMessageForAction = null
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFF0075FF)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(text = "Edit message", fontSize = 15.sp, color = SoftUITheme.TextDark, fontWeight = FontWeight.Medium)
                    }
                }

                // Delete Action
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            messages.remove(targetMsg)
                            if (replyingTo?.id == targetMsg.id) replyingTo = null
                            if (editingMessage?.id == targetMsg.id) {
                                editingMessage = null
                                inputText = ""
                            }
                            selectedMessageForAction = null
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = FlikataError
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(text = "Delete message", fontSize = 15.sp, color = FlikataError, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

/**
 * Chat bubble with verified profile avatars on both sender and receiver sides
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun ChatBubbleWithAvatars(
    msg: DetailMessage,
    partnerName: String,
    partnerAvatar: String,
    myName: String,
    myAvatar: String,
    onLongClick: () -> Unit
) {
    if (msg.isCallNotice) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .shadow(2.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0x33A0C0E8), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = msg.text,
                    fontSize = 12.sp,
                    color = SoftUITheme.TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (msg.isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        // Partner Avatar on incoming message (Left side)
        if (!msg.isMe) {
            Box(
                modifier = Modifier
                    .padding(end = 6.dp, bottom = 2.dp)
                    .size(30.dp)
                    .shadow(2.dp, CircleShape, ambientColor = SoftUITheme.ShadowDark)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0x99FFFFFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                FlikataAvatar(
                    name = partnerName,
                    avatarUrl = partnerAvatar,
                    size = 28.dp
                )
            }
        }

        // 3D Neumorphic Message Bubble Container
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .shadow(
                    elevation = if (msg.isMe) 5.dp else 3.dp,
                    shape = RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (msg.isMe) 18.dp else 4.dp,
                        bottomEnd = if (msg.isMe) 4.dp else 18.dp
                    ),
                    ambientColor = if (msg.isMe) Color(0x440075FF) else SoftUITheme.ShadowDark,
                    spotColor = if (msg.isMe) Color(0x660052D4) else SoftUITheme.ShadowDark
                )
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (msg.isMe) 18.dp else 4.dp,
                        bottomEnd = if (msg.isMe) 4.dp else 18.dp
                    )
                )
                .background(
                    if (msg.isMe) {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF0075FF),
                                Color(0xFF0052D4)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(200f, 200f)
                        )
                    } else {
                        if (SoftUITheme.isDark) {
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF282C36),
                                    Color(0xFF20232A)
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(100f, 100f)
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFFFFFF),
                                    Color(0xFFEFF5FD)
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(100f, 100f)
                            )
                        }
                    }
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        if (msg.isMe) listOf(Color(0x88FFFFFF), Color(0x22FFFFFF))
                        else if (SoftUITheme.isDark) listOf(Color(0x22FFFFFF), Color(0x11FFFFFF))
                        else listOf(Color(0xEEFFFFFF), Color(0x44A0C0E8))
                    ),
                    shape = RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (msg.isMe) 18.dp else 4.dp,
                        bottomEnd = if (msg.isMe) 4.dp else 18.dp
                    )
                )
                .combinedClickable(
                    onClick = { /* normal tap */ },
                    onLongClick = onLongClick
                )
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
                // Quoted message inside bubble
                if (msg.replyTo != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (msg.isMe) Color.White.copy(alpha = 0.2f) else Color(0xFFE2EDFC))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(24.dp)
                                    .background(if (msg.isMe) Color.White else Color(0xFF0075FF), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = msg.replyTo.senderName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (msg.isMe) Color.White else Color(0xFF0075FF)
                                )
                                Text(
                                    text = msg.replyTo.text,
                                    fontSize = 11.sp,
                                    color = if (msg.isMe) Color.White.copy(alpha = 0.9f) else SoftUITheme.TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Text Content
                Text(
                    text = msg.text,
                    color = if (msg.isMe) Color.White else SoftUITheme.TextDark,
                    fontSize = 15.sp,
                    lineHeight = 20.sp
                )

                // Time & checkmark row
                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (msg.isEdited) {
                        Text(
                            text = "(edited) ",
                            fontSize = 10.sp,
                            color = if (msg.isMe) Color.White.copy(alpha = 0.75f) else SoftUITheme.TextMuted
                        )
                    }
                    Text(
                        text = msg.time,
                        fontSize = 10.5.sp,
                        color = if (msg.isMe) Color.White.copy(alpha = 0.8f) else SoftUITheme.TextMuted
                    )
                }
            }
        }

        // My Avatar on outgoing message (Right side)
        if (msg.isMe) {
            Box(
                modifier = Modifier
                    .padding(start = 6.dp, bottom = 2.dp)
                    .size(30.dp)
                    .shadow(2.dp, CircleShape, ambientColor = Color(0x440075FF))
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0x99FFFFFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                FlikataAvatar(
                    name = myName,
                    avatarUrl = myAvatar,
                    size = 28.dp
                )
            }
        }
    }
}

@Composable
private fun SwipeToReplyContainer(
    onReply: () -> Unit,
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var isTriggered by remember { mutableStateOf(false) }
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "swipe_to_reply"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { isTriggered = false },
                    onHorizontalDrag = { change, dragAmount ->
                        if (dragAmount > 0 || offsetX > 0) {
                            change.consume()
                            val next = (offsetX + dragAmount).coerceIn(0f, 150f)
                            offsetX = next
                            if (next >= 85f && !isTriggered) {
                                isTriggered = true
                            }
                        }
                    },
                    onDragEnd = {
                        if (isTriggered || offsetX >= 85f) {
                            onReply()
                        }
                        offsetX = 0f
                        isTriggered = false
                    },
                    onDragCancel = {
                        offsetX = 0f
                        isTriggered = false
                    }
                )
            }
    ) {
        if (animatedOffsetX > 8f) {
            val progress = (animatedOffsetX / 85f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 6.dp)
                    .size(34.dp)
                .graphicsLayer {
                    scaleX = 0.6f + progress * 0.4f
                    scaleY = 0.6f + progress * 0.4f
                    alpha = progress
                }
                .clip(CircleShape)
                .background(if (isTriggered || animatedOffsetX >= 85f) Color(0xFF0075FF) else Color(0xFFE2EDFC)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Reply,
                contentDescription = "Reply",
                tint = if (isTriggered || animatedOffsetX >= 85f) Color.White else Color(0xFF0075FF),
                modifier = Modifier.size(18.dp)
            )
        }
    }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
        ) {
            content()
        }
    }
}

/**
 * 3D Soft Neumorphic Audio Calling Screen with Pulsing Rings and Realistic Controls
 */
@Composable
private fun AudioCallScreenOverlay(
    partnerName: String,
    partnerAvatar: String,
    onEndCall: (String) -> Unit
) {
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(false) }
    var callSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            callSeconds++
        }
    }

    val callDurationString = remember(callSeconds) {
        val mins = callSeconds / 60
        val secs = callSeconds % 60
        String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
    }

    // Outer pulsating wave animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0B1B3D),
                        Color(0xFF102855),
                        Color(0xFF0A1628)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Calling Avatar & Status
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    // Outer pulse ring
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(Color(0x220075FF))
                    )
                    // Inner pulse ring
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFFFFF))
                    )
                    // 3D Avatar Container
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .shadow(10.dp, CircleShape, ambientColor = Color(0x660075FF))
                            .clip(CircleShape)
                            .border(3.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        FlikataAvatar(
                            name = partnerName,
                            avatarUrl = partnerAvatar,
                            size = 110.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = partnerName,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Calling Status Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x33FFFFFF))
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (callSeconds < 3) "Connecting audio call..." else "In Call • $callDurationString",
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Bottom Section: 3D Call Controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 36.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute Mic Button
                    CallActionButton(
                        icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        label = if (isMuted) "Unmute" else "Mute",
                        isActive = isMuted,
                        onClick = { isMuted = !isMuted }
                    )

                    // End Call (Large 3D Red Glossy Button)
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .shadow(
                                elevation = 12.dp,
                                shape = CircleShape,
                                ambientColor = Color(0x88FF3B30),
                                spotColor = Color(0xCCFF3B30)
                            )
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFFFF453A), Color(0xFFD32F2F))
                                )
                            )
                            .border(
                                2.dp,
                                Brush.verticalGradient(
                                    listOf(Color.White, Color(0x44FFFFFF))
                                ),
                                CircleShape
                            )
                            .clickable { onEndCall(callDurationString) }
                            .testTag("end_audio_call_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Speaker Button
                    CallActionButton(
                        icon = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        label = if (isSpeakerOn) "Speaker" else "Earpiece",
                        isActive = isSpeakerOn,
                        onClick = { isSpeakerOn = !isSpeakerOn }
                    )
                }
            }
        }
    }
}

@Composable
private fun CallActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = CircleShape,
                    ambientColor = Color(0x44000000)
                )
                .clip(CircleShape)
                .background(if (isActive) Color.White else Color(0x33FFFFFF))
                .border(1.2.dp, Color(0x66FFFFFF), CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color(0xFF0B1B3D) else Color.White,
                modifier = Modifier.size(23.dp)
            )
        }
        Spacer(modifier = Modifier.height(7.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.85f),
            fontWeight = FontWeight.Medium
        )
    }
}
