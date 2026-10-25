package com.example.ui.screens.auth

import android.app.DatePickerDialog
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SoftNeumorphicBackground
import com.example.ui.components.SoftNeumorphicCard
import com.example.ui.components.SoftNeumorphicTextField
import com.example.ui.components.SoftPrimaryBlueButton
import com.example.ui.components.SoftUITheme
import com.example.ui.theme.FlikataError
import com.example.ui.theme.FlikataPrimary
import com.example.ui.viewmodel.MainViewModel
import java.util.Calendar

@Composable
fun RegisterScreen(
    viewModel: MainViewModel,
    onNavigateToLogin: () -> Unit
) {
    androidx.activity.compose.BackHandler { onNavigateToLogin() }
    val context = LocalContext.current
    val firstName by viewModel.regFirstName.collectAsState()
    val lastName by viewModel.regLastName.collectAsState()
    val accountType by viewModel.regAccountType.collectAsState() // "phone" or "email"
    val identifier by viewModel.regIdentifier.collectAsState()
    val gender by viewModel.regGender.collectAsState()
    val password by viewModel.regPassword.collectAsState()
    val confirmPassword by viewModel.regConfirmPassword.collectAsState()
    val dobString by viewModel.regDob.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val isLoading by viewModel.authLoading.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val scrollState = rememberScrollState()

    // DatePicker setup
    val calendar = Calendar.getInstance()
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val formattedMonth = String.format("%02d", month + 1)
                val formattedDay = String.format("%02d", dayOfMonth)
                viewModel.regDob.value = "$year-$formattedMonth-$formattedDay"
            },
            calendar.get(Calendar.YEAR) - 20,
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    SoftNeumorphicBackground(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Header Bar: "Create Account" on left, Server/Menu Icon on right matching Screenshot 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Create Account",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0B132A),
                    letterSpacing = (-0.5).sp
                )

                // Top right soft square action button: Dark / Light Mode Toggle
                SoftNeumorphicCard(
                    modifier = Modifier.size(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    elevation = 5.dp,
                    backgroundColor = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFF1F6FD),
                    onClick = { viewModel.toggleDarkMode() }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkMode) "Light Mode" else "Dark Mode",
                            tint = if (isDarkMode) Color(0xFFFFB800) else Color(0xFF0070F3),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 1. First Name & Last Name (Side by side pills)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // First Name
                SoftNeumorphicTextField(
                    value = firstName,
                    onValueChange = { viewModel.regFirstName.value = it },
                    placeholder = "First name",
                    leadingIcon = Icons.Default.PersonOutline,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("reg_first_name")
                )

                // Last Name
                SoftNeumorphicTextField(
                    value = lastName,
                    onValueChange = { viewModel.regLastName.value = it },
                    placeholder = "Last name",
                    leadingIcon = Icons.Default.PersonOutline,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("reg_last_name")
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Section: Contact Method
            Text(
                text = "Contact Method",
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0B132A)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Mobile Number Choice Pill
                val isPhoneSelected = accountType == "phone"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .shadow(
                            elevation = if (isPhoneSelected) 6.dp else 3.dp,
                            shape = RoundedCornerShape(26.dp),
                            ambientColor = if (isPhoneSelected) Color(0x660070F3) else SoftUITheme.ShadowDark,
                            spotColor = if (isPhoneSelected) Color(0x660070F3) else SoftUITheme.ShadowDark
                        )
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            if (isPhoneSelected) {
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFE3F0FF), Color(0xFFD3E7FF))
                                )
                            } else {
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFF1F6FD), Color(0xFFE8F1FC))
                                )
                            }
                        )
                        .border(
                            width = if (isPhoneSelected) 1.8.dp else 1.dp,
                            color = if (isPhoneSelected) Color(0xFF0075FF) else Color(0x66FFFFFF),
                            shape = RoundedCornerShape(26.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { viewModel.regAccountType.value = "phone" },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = if (isPhoneSelected) Color(0xFF0075FF) else Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mobile Number",
                            fontSize = 13.5.sp,
                            fontWeight = if (isPhoneSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isPhoneSelected) Color(0xFF0075FF) else Color(0xFF64748B)
                        )
                    }
                }

                // Email Address Choice Pill
                val isEmailSelected = accountType == "email"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .shadow(
                            elevation = if (isEmailSelected) 6.dp else 3.dp,
                            shape = RoundedCornerShape(26.dp),
                            ambientColor = if (isEmailSelected) Color(0x660070F3) else SoftUITheme.ShadowDark,
                            spotColor = if (isEmailSelected) Color(0x660070F3) else SoftUITheme.ShadowDark
                        )
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            if (isEmailSelected) {
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFE3F0FF), Color(0xFFD3E7FF))
                                )
                            } else {
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFF1F6FD), Color(0xFFE8F1FC))
                                )
                            }
                        )
                        .border(
                            width = if (isEmailSelected) 1.8.dp else 1.dp,
                            color = if (isEmailSelected) Color(0xFF0075FF) else Color(0x66FFFFFF),
                            shape = RoundedCornerShape(26.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { viewModel.regAccountType.value = "email" },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = if (isEmailSelected) Color(0xFF0075FF) else Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Email Address",
                            fontSize = 13.5.sp,
                            fontWeight = if (isEmailSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isEmailSelected) Color(0xFF0075FF) else Color(0xFF64748B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Dynamic Contact Input Pill Field
            SoftNeumorphicTextField(
                value = identifier,
                onValueChange = { viewModel.regIdentifier.value = it },
                placeholder = if (accountType == "phone") "Mobile number" else "Email address",
                leadingIcon = if (accountType == "phone") Icons.Default.Phone else Icons.Default.Email,
                modifier = Modifier.testTag("reg_identifier")
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Section: Gender
            Text(
                text = "Gender",
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0B132A)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("Male", "Female", "Other").forEach { item ->
                    val isSelected = gender == item
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .shadow(
                                elevation = if (isSelected) 6.dp else 3.dp,
                                shape = RoundedCornerShape(24.dp),
                                ambientColor = if (isSelected) Color(0x660070F3) else SoftUITheme.ShadowDark,
                                spotColor = if (isSelected) Color(0x660070F3) else SoftUITheme.ShadowDark
                            )
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                if (isSelected) {
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFE3F0FF), Color(0xFFD3E7FF))
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFF1F6FD), Color(0xFFE8F1FC))
                                    )
                                }
                            )
                            .border(
                                width = if (isSelected) 1.8.dp else 1.dp,
                                color = if (isSelected) Color(0xFF0075FF) else Color(0x66FFFFFF),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { viewModel.regGender.value = item },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF0075FF) else Color(0xFF64748B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Section: Date of Birth
            Text(
                text = "Date of Birth",
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0B132A)
            )

            Spacer(modifier = Modifier.height(10.dp))

            SoftNeumorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                elevation = 4.dp,
                backgroundColor = Color(0xFFF1F6FD),
                onClick = { datePickerDialog.show() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Calendar",
                            tint = Color(0xFF0075FF),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (dobString.isNotEmpty()) dobString else "2000-01-01",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1E293B)
                        )
                    }

                    Text(
                        text = "Change",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0070F3)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Password Field
            SoftNeumorphicTextField(
                value = password,
                onValueChange = { viewModel.regPassword.value = it },
                placeholder = "Password",
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                modifier = Modifier.testTag("reg_password")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 7. Confirm Password Field
            SoftNeumorphicTextField(
                value = confirmPassword,
                onValueChange = { viewModel.regConfirmPassword.value = it },
                placeholder = "Confirm Password",
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                modifier = Modifier.testTag("reg_confirm_password")
            )

            if (authError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = authError!!,
                    color = FlikataError,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 8. Primary Blue Button: "Sign Up"
            SoftPrimaryBlueButton(
                text = "Sign Up",
                onClick = { viewModel.performRegister() },
                isLoading = isLoading,
                modifier = Modifier.testTag("reg_submit_btn")
            )

            Spacer(modifier = Modifier.height(22.dp))

            // 9. Footer: "Already have an account? Sign In"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = "Sign In",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0070F3),
                    modifier = Modifier
                        .clickable { onNavigateToLogin() }
                        .testTag("reg_goto_login_btn")
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
        }

    }
}
