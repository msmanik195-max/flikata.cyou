package com.example.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.FlikataAvatar
import com.example.ui.components.ImageCropDialog
import com.example.ui.theme.FlikataBackground
import com.example.ui.theme.FlikataDivider
import com.example.ui.theme.FlikataPrimary
import com.example.ui.theme.FlikataSurface
import com.example.ui.theme.FlikataTextPrimary
import com.example.ui.theme.FlikataTextSecondary
import com.example.ui.viewmodel.MainViewModel

@Composable
fun EditProfileScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val editFullName by viewModel.editFullName.collectAsState()
    val editBio by viewModel.editBio.collectAsState()
    val editProfilePic by viewModel.editProfilePic.collectAsState()
    val editCoverPic by viewModel.editCoverPic.collectAsState()
    val editOccupation by viewModel.editOccupation.collectAsState()
    val editEducation by viewModel.editEducation.collectAsState()
    val editGender by viewModel.editGender.collectAsState()
    val editDob by viewModel.editDob.collectAsState()
    val editEmail by viewModel.editEmail.collectAsState()
    val editPhone by viewModel.editPhone.collectAsState()
    val editLocation by viewModel.editLocation.collectAsState()
    val editWebsite by viewModel.editWebsite.collectAsState()
    val editNid by viewModel.editNid.collectAsState()

    // About Section Display Toggles
    val showPhoneInAbout by viewModel.showPhoneInAbout.collectAsState()
    val showEmailInAbout by viewModel.showEmailInAbout.collectAsState()
    val showBirthdayInAbout by viewModel.showBirthdayInAbout.collectAsState()
    val showGenderInAbout by viewModel.showGenderInAbout.collectAsState()
    val showLocationInAbout by viewModel.showLocationInAbout.collectAsState()
    val showOccupationInAbout by viewModel.showOccupationInAbout.collectAsState()
    val showEducationInAbout by viewModel.showEducationInAbout.collectAsState()
    val showWebsiteInAbout by viewModel.showWebsiteInAbout.collectAsState()

    // Privacy Toggles matching Screenshot 1
    val hideUsername by viewModel.hideUsername.collectAsState()
    val hidePhone by viewModel.hidePhone.collectAsState()
    val hideEmail by viewModel.hideEmail.collectAsState()
    val hideBirthday by viewModel.hideBirthday.collectAsState()
    val hideAge by viewModel.hideAge.collectAsState()
    val hideGender by viewModel.hideGender.collectAsState()
    val hideLocation by viewModel.hideLocation.collectAsState()
    val isSavingProfile by viewModel.isSavingProfile.collectAsState()

    val scrollState = rememberScrollState()

    var pendingCropUri by remember { mutableStateOf<Uri?>(null) }
    var isCoverCrop by remember { mutableStateOf(false) }

    // Photo Pickers for Profile Picture and Cover Banner
    val profilePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingCropUri = uri
            isCoverCrop = false
        }
    }

    val coverPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingCropUri = uri
            isCoverCrop = true
        }
    }

    Scaffold(
        topBar = {
            Surface(color = FlikataSurface, tonalElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }

                    Text(
                        text = "Edit Profile",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = FlikataTextPrimary
                    )

                    TextButton(
                        onClick = { viewModel.saveProfileChanges() },
                        enabled = !isSavingProfile,
                        modifier = Modifier.testTag("save_profile_btn")
                    ) {
                        if (isSavingProfile) {
                            CircularProgressIndicator(
                                color = FlikataPrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text(
                                text = "Save",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = FlikataPrimary
                            )
                        }
                    }
                }
            }
        },
        containerColor = FlikataBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Profile Cover Banner Edit Box with Camera Button
            Surface(
                color = FlikataSurface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Cover Banner", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = FlikataTextPrimary)
                        Text(
                            text = "Edit",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = FlikataPrimary,
                            modifier = Modifier.clickable {
                                coverPhotoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF0D47A1), Color(0xFF1877F2), Color(0xFF42A5F5))
                                )
                            )
                            .clickable {
                                coverPhotoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (editCoverPic.isNotEmpty()) {
                            AsyncImage(
                                model = editCoverPic,
                                contentDescription = "Cover Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.55f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Change Cover",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Profile Picture with Camera Badge matching Screenshot 2
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box {
                    Box(
                        modifier = Modifier
                            .clickable {
                                profilePhotoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                    ) {
                        FlikataAvatar(
                            name = editFullName.ifBlank { "User" },
                            avatarUrl = editProfilePic.ifBlank { null },
                            size = 110.dp,
                            borderWidth = 2.dp,
                            borderColor = Color.LightGray
                        )
                    }

                    // Blue Camera Badge at bottom right of avatar
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(FlikataPrimary)
                            .border(2.dp, Color.White, CircleShape)
                            .clickable {
                                profilePhotoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Edit Avatar Photo",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 1. Section: Basic Info
            SectionHeader("Basic Info")

            Surface(
                color = FlikataSurface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = FlikataTextSecondary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Full Name", fontSize = 12.sp, color = FlikataTextSecondary)
                            OutlinedTextField(
                                value = editFullName,
                                onValueChange = { viewModel.editFullName.value = it },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("edit_fullname")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = FlikataSurface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = FlikataTextSecondary, modifier = Modifier.padding(top = 4.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Bio", fontSize = 12.sp, color = FlikataTextSecondary)
                            OutlinedTextField(
                                value = editBio,
                                onValueChange = { viewModel.editBio.value = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("edit_bio")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = FlikataSurface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Work, contentDescription = null, tint = FlikataTextSecondary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Occupation", fontSize = 12.sp, color = FlikataTextSecondary)
                            OutlinedTextField(
                                value = editOccupation,
                                onValueChange = { viewModel.editOccupation.value = it },
                                placeholder = { Text("Occupation") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Section: Gender
            SectionHeader("Gender")

            Surface(
                color = FlikataSurface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    listOf("Male", "Female", "Other").forEach { item ->
                        val isSelected = editGender == item
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                                .height(40.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) FlikataPrimary.copy(alpha = 0.15f) else Color.Transparent)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) FlikataPrimary else Color.LightGray,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { viewModel.editGender.value = item },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) FlikataPrimary else FlikataTextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Section: Date of Birth
            SectionHeader("Date of Birth")

            Surface(
                color = FlikataSurface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Cake, contentDescription = null, tint = FlikataTextSecondary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (editDob.isNotEmpty()) editDob else "Tap to select",
                            fontSize = 15.sp,
                            color = FlikataTextPrimary
                        )
                    }
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = FlikataTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Section: Contact Info
            SectionHeader("Contact Info")

            Surface(
                color = FlikataSurface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Email row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = FlikataTextSecondary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (editEmail.isNotEmpty()) editEmail else "marufma143g@gmail.com",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = FlikataTextPrimary
                                )
                                Text(
                                    text = "Verified ✓",
                                    fontSize = 12.sp,
                                    color = Color(0xFF42B72A),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "Change",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = FlikataPrimary,
                            modifier = Modifier.clickable { }
                        )
                    }

                    Divider(color = FlikataDivider, thickness = 0.8.dp, modifier = Modifier.padding(vertical = 12.dp))

                    // Phone row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = FlikataTextSecondary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (editPhone.isNotEmpty()) editPhone else "No phone added",
                                fontSize = 14.sp,
                                color = FlikataTextSecondary
                            )
                        }
                        Text(
                            text = "Add",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = FlikataPrimary,
                            modifier = Modifier.clickable { }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5. Location Card
            Surface(
                color = FlikataSurface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = FlikataTextSecondary)
                        Spacer(modifier = Modifier.width(12.dp))
                        OutlinedTextField(
                            value = editLocation,
                            onValueChange = { viewModel.editLocation.value = it },
                            placeholder = { Text("Location") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 6. Website Card
            Surface(
                color = FlikataSurface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = FlikataTextSecondary)
                        Spacer(modifier = Modifier.width(12.dp))
                        OutlinedTextField(
                            value = editWebsite,
                            onValueChange = { viewModel.editWebsite.value = it },
                            placeholder = { Text("Website") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 7. Identity (for verification)
            SectionHeader("Identity (for verification)")

            Surface(
                color = FlikataSurface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CreditCard, contentDescription = null, tint = FlikataTextSecondary)
                        Spacer(modifier = Modifier.width(12.dp))
                        OutlinedTextField(
                            value = editNid,
                            onValueChange = { viewModel.editNid.value = it },
                            placeholder = { Text("National ID (NID)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Blue NID Privacy Box
            Surface(
                color = MaterialTheme.colorScheme.tertiary,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = FlikataPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NID is kept private and only used for verification.",
                        fontSize = 13.sp,
                        color = FlikataPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 8. About Section Display Toggles (Per user request)
            SectionHeader("About Section Display Toggles")
            Text(
                text = "Turn ON the toggles below to show these details on your profile About cards. Other users will only see the details you turn ON and fill in.",
                fontSize = 12.5.sp,
                color = FlikataTextSecondary,
                lineHeight = 17.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Surface(
                color = FlikataSurface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                    PrivacyToggleRow(
                        label = "Show Phone Number",
                        icon = Icons.Default.Phone,
                        checked = showPhoneInAbout,
                        onCheckedChange = { viewModel.setAboutToggle("show_phone", it) }
                    )
                    PrivacyToggleRow(
                        label = "Show Email Address",
                        icon = Icons.Default.Email,
                        checked = showEmailInAbout,
                        onCheckedChange = { viewModel.setAboutToggle("show_email", it) }
                    )
                    PrivacyToggleRow(
                        label = "Show Birthday (DOB)",
                        icon = Icons.Default.Cake,
                        checked = showBirthdayInAbout,
                        onCheckedChange = { viewModel.setAboutToggle("show_dob", it) }
                    )
                    PrivacyToggleRow(
                        label = "Show Gender",
                        icon = Icons.Default.Person,
                        checked = showGenderInAbout,
                        onCheckedChange = { viewModel.setAboutToggle("show_gender", it) }
                    )
                    PrivacyToggleRow(
                        label = "Show Location / City",
                        icon = Icons.Default.LocationOn,
                        checked = showLocationInAbout,
                        onCheckedChange = { viewModel.setAboutToggle("show_location", it) }
                    )
                    PrivacyToggleRow(
                        label = "Show Work / Occupation",
                        icon = Icons.Default.Work,
                        checked = showOccupationInAbout,
                        onCheckedChange = { viewModel.setAboutToggle("show_occupation", it) }
                    )
                    PrivacyToggleRow(
                        label = "Show Education",
                        icon = Icons.Default.School,
                        checked = showEducationInAbout,
                        onCheckedChange = { viewModel.setAboutToggle("show_education", it) }
                    )
                    PrivacyToggleRow(
                        label = "Show Website",
                        icon = Icons.Default.Link,
                        checked = showWebsiteInAbout,
                        onCheckedChange = { viewModel.setAboutToggle("show_website", it) },
                        showDivider = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Image Crop Dialog when user selects photo/banner
        if (pendingCropUri != null) {
            ImageCropDialog(
                imageUri = pendingCropUri!!,
                isCoverCrop = isCoverCrop,
                onDismiss = { pendingCropUri = null },
                onCropDone = { croppedUri ->
                    val cached = viewModel.cacheMediaFromUri(croppedUri, isVideo = false)
                    if (isCoverCrop) {
                        viewModel.editCoverPic.value = cached
                    } else {
                        viewModel.editProfilePic.value = cached
                    }
                    pendingCropUri = null
                }
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = FlikataTextSecondary,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun PrivacyToggleRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    showDivider: Boolean = true
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = label, tint = FlikataPrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = label, fontSize = 15.sp, color = FlikataTextPrimary)
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = FlikataPrimary
                )
            )
        }
        if (showDivider) {
            Divider(color = FlikataDivider, thickness = 0.5.dp)
        }
    }
}
