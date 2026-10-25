package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import kotlin.math.abs

@Composable
fun FlikataAvatar(
    name: String,
    avatarUrl: String?,
    size: Dp = 40.dp,
    borderWidth: Dp = 0.dp,
    borderColor: Color = Color.Transparent,
    fallbackDrawable: Int = R.drawable.no_logo,
    modifier: Modifier = Modifier
) {
    val cleanName = name.trim().ifEmpty { "User" }

    val baseModifier = modifier
        .size(size)
        .clip(CircleShape)
        .then(
            if (borderWidth > 0.dp) {
                Modifier.border(borderWidth, borderColor, CircleShape)
            } else {
                Modifier
            }
        )

    val resolvedUrl = androidx.compose.runtime.remember(avatarUrl) {
        val u = avatarUrl?.trim()
        if (u.isNullOrBlank()) null
        else if (u.startsWith("http://") || u.startsWith("https://") || u.startsWith("file://") || u.startsWith("content://") || u.startsWith("data:")) u
        else {
            val base = com.example.data.remote.ApiClient.baseUrl.trimEnd('/')
            val cleanPath = u.trimStart('/')
            "$base/$cleanPath"
        }
    }

    if (!resolvedUrl.isNullOrBlank()) {
        AsyncImage(
            model = resolvedUrl,
            placeholder = painterResource(id = fallbackDrawable),
            error = painterResource(id = fallbackDrawable),
            contentDescription = cleanName,
            contentScale = ContentScale.Crop,
            modifier = baseModifier
        )
    } else {
        Image(
            painter = painterResource(id = fallbackDrawable),
            contentDescription = cleanName,
            contentScale = ContentScale.Crop,
            modifier = baseModifier
        )
    }
}

