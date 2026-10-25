package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R

/**
 * Authentic Facebook-style Blue Verification Rosette Badge (Scalloped Starburst Rosette)
 * Exactly replicates the Facebook / Meta 16-point scalloped starburst rosette with centered checkmark.
 */
@Composable
fun VerifiedBadge(
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    badgeColor: Color = Color(0xFF0866FF)
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_fb_verified),
            contentDescription = "Verified Account",
            tint = Color.Unspecified,
            modifier = Modifier.size(size)
        )
    }
}
