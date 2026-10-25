package com.example.ui.screens.wallet

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.layout.ContentScale
import java.text.SimpleDateFormat
import java.util.Date
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.DepositMethod
import com.example.ui.components.SoftUITheme
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Multi-step Deposit Screen
 * Step 1: Amount input
 * Step 2: Method selection, Copy Number & Copy Amount, TrxID input
 * Step 3: Thank You confirmation -> Redirect to Wallet
 * No header, No footer, No back button on screen — Mobile system back button handles back navigation.
 */
@Composable
fun DepositScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) } // 1: Amount, 2: Method & TrxID, 3: Thank you

    BackHandler {
        if (step > 1 && step < 3) {
            step -= 1
        } else {
            onBack()
        }
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isDark = SoftUITheme.isDark

    var amountInput by remember { mutableStateOf("500") }
    var selectedMethod by remember { mutableStateOf<DepositMethod?>(null) }
    var trxIdInput by remember { mutableStateOf("") }
    var methodsList by remember { mutableStateOf<List<DepositMethod>>(emptyList()) }
    var isLoadingMethods by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var submissionTimestamp by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val walletRes = viewModel.repository.getWallet()
        var loaded: List<DepositMethod>? = null
        if (walletRes.isSuccess) {
            val wm = walletRes.getOrNull()?.methods
            if (!wm.isNullOrEmpty()) {
                loaded = wm.filter { it.isActive == 1 }
            }
        }
        if (loaded.isNullOrEmpty()) {
            val res = viewModel.repository.getAdminDepositMethods()
            if (res.isSuccess) {
                loaded = (res.getOrNull() ?: emptyList()).filter { it.isActive == 1 }
            }
        }
        methodsList = loaded ?: emptyList()
        if (methodsList.isNotEmpty()) {
            selectedMethod = methodsList.firstOrNull()
        }
        isLoadingMethods = false
    }

    val screenBg = if (isDark) {
        Brush.verticalGradient(listOf(Color(0xFF0F1115), Color(0xFF161920), Color(0xFF0A0C10)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFF4F8FD), Color(0xFFE8F1FC), Color(0xFFDFEAF7)))
    }
    val cardBg = if (isDark) Color(0xFF1E222B) else Color.White
    val cardBorder = if (isDark) Color(0x33FFFFFF) else Color(0x66B0C8E8)

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        if (step == 3) {
            // ==========================================
            // SUCCESS / THANK YOU STATEMENT PAGE
            // ==========================================
            val activeMethodName = selectedMethod?.name ?: "Online Payment"
            val activeMethodNumber = selectedMethod?.number ?: ""
            val formattedAmt = String.format(Locale.getDefault(), "%.2f", amountInput.toDoubleOrNull() ?: 0.0)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .shadow(10.dp, CircleShape, ambientColor = Color(0x6645BD62))
                        .clip(CircleShape)
                        .background(Color(0xFF45BD62)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Thank You!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = SoftUITheme.TextDark
                )

                Text(
                    text = "Your deposit request has been received and is waiting for admin verification.",
                    fontSize = 13.5.sp,
                    color = SoftUITheme.TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Detailed Deposit Statement Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(22.dp), ambientColor = Color(0x22000000))
                        .clip(RoundedCornerShape(22.dp))
                        .background(cardBg)
                        .border(1.2.dp, cardBorder, RoundedCornerShape(22.dp))
                        .padding(22.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Deposit Statement",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftUITheme.TextDark
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xFFFEF3C7))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "PENDING APPROVAL",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD97706)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Large Amount
                        Text(
                            text = "Amount Deposited",
                            fontSize = 12.sp,
                            color = SoftUITheme.TextSecondary
                        )
                        Text(
                            text = "৳ $formattedAmt",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF16A34A)
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(cardBorder)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Statement Row Items
                        DepositStatementRow(label = "Payment Method", value = activeMethodName)
                        Spacer(modifier = Modifier.height(10.dp))
                        if (activeMethodNumber.isNotBlank()) {
                            DepositStatementRow(label = "Sent To Number", value = activeMethodNumber)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        DepositStatementRowWithCopy(
                            label = "Transaction ID",
                            value = trxIdInput,
                            onCopy = { copyToClipboard("Transaction ID", trxIdInput) }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        DepositStatementRow(label = "Date & Time", value = submissionTimestamp)
                        Spacer(modifier = Modifier.height(10.dp))
                        DepositStatementRow(label = "Status", value = "Pending Admin Verification")

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) Color(0xFF252B36) else Color(0xFFF1F5F9))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Notice: The deposited funds will be added directly to your wallet as soon as admin confirms the transaction.",
                                fontSize = 11.5.sp,
                                color = SoftUITheme.TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0866FF))
                        .clickable { onBack() }
                        .testTag("deposit_done_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Back to Wallet",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 18.dp)
            ) {
                // Header Title
                Text(
                    text = "Deposit Funds",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = SoftUITheme.TextDark
                )
                Text(
                    text = if (step == 1) "Step 1 of 2: Enter Amount" else "Step 2 of 2: Select Method & Confirm",
                    fontSize = 13.sp,
                    color = SoftUITheme.TextSecondary
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (step == 1) {
                    // Step 1: Amount Selection
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(22.dp))
                            .clip(RoundedCornerShape(22.dp))
                            .background(cardBg)
                            .border(1.2.dp, cardBorder, RoundedCornerShape(22.dp))
                            .padding(20.dp)
                    ) {
                        Column {
                            Text(
                                text = "Enter Deposit Amount (BDT)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftUITheme.TextDark
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = amountInput,
                                onValueChange = { amountInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                leadingIcon = {
                                    Text(
                                        text = "৳",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0866FF),
                                        modifier = Modifier.padding(start = 12.dp)
                                    )
                                },
                                placeholder = { Text("500") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Quick Amount Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("100", "300", "500", "1000").forEach { preset ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .shadow(1.dp, RoundedCornerShape(12.dp))
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (amountInput == preset) Color(0xFF0866FF) else if (isDark) Color(0xFF2C3240) else Color(0xFFF0F4FA))
                                            .clickable { amountInput = preset }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "৳ $preset",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (amountInput == preset) Color.White else SoftUITheme.TextDark
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Next Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .shadow(4.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (amountInput.toDoubleOrNull() != null && amountInput.toDouble() > 0) {
                                    Brush.linearGradient(listOf(Color(0xFF0866FF), Color(0xFF0052D4)))
                                } else {
                                    Brush.linearGradient(listOf(Color.Gray, Color.DarkGray))
                                }
                            )
                            .clickable(enabled = amountInput.toDoubleOrNull() != null && amountInput.toDouble() > 0) {
                                step = 2
                            }
                            .testTag("deposit_next_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Next Step",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else if (step == 2) {
                    // Step 2: Payment Method Selection & Copy Details
                    Text(
                        text = "Select Payment Method",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftUITheme.TextDark,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    if (methodsList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(18.dp))
                                .clip(RoundedCornerShape(18.dp))
                                .background(cardBg)
                                .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No payment methods configured by admin yet.",
                                fontSize = 13.sp,
                                color = SoftUITheme.TextSecondary
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            methodsList.forEach { m ->
                                val isSelected = selectedMethod?.id == m.id
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(if (isSelected) 4.dp else 2.dp, RoundedCornerShape(18.dp))
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(cardBg)
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) Color(0xFF0866FF) else cardBorder,
                                            shape = RoundedCornerShape(18.dp)
                                        )
                                        .clickable { selectedMethod = m }
                                        .padding(14.dp)
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
                                                    .size(22.dp)
                                                    .clip(CircleShape)
                                                    .border(
                                                        2.dp,
                                                        if (isSelected) Color(0xFF0866FF) else Color.Gray,
                                                        CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isSelected) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(10.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFF0866FF))
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            PaymentMethodLogo(method = m, modifier = Modifier.size(42.dp))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = m.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = SoftUITheme.TextDark
                                                )
                                                Text(
                                                    text = m.number,
                                                    fontSize = 12.5.sp,
                                                    color = SoftUITheme.TextSecondary
                                                )
                                            }
                                        }

                                        Icon(
                                            imageVector = Icons.Default.Payment,
                                            contentDescription = null,
                                            tint = if (isSelected) Color(0xFF0866FF) else SoftUITheme.TextSecondary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val activeMethod = selectedMethod
                    if (activeMethod != null) {
                        // Payment Details with Copy Icons
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(20.dp))
                                .clip(RoundedCornerShape(20.dp))
                                .background(cardBg)
                                .border(1.2.dp, cardBorder, RoundedCornerShape(20.dp))
                                .padding(18.dp)
                        ) {
                            Column {
                                // 1. Account Number + Copy Icon
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        PaymentMethodLogo(method = activeMethod, modifier = Modifier.size(46.dp))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "${activeMethod.name} Number",
                                                fontSize = 11.5.sp,
                                                color = SoftUITheme.TextSecondary
                                            )
                                            Text(
                                                text = activeMethod.number,
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = SoftUITheme.TextDark
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { copyToClipboard("Account Number", activeMethod.number) },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(if (isDark) Color(0xFF2C3240) else Color(0xFFF0F4FA))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy Number",
                                            tint = Color(0xFF0866FF),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // 2. Deposit Amount + Copy Icon
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Amount to Send",
                                            fontSize = 11.5.sp,
                                            color = SoftUITheme.TextSecondary
                                        )
                                        Text(
                                            text = "৳ $amountInput",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }

                                    IconButton(
                                        onClick = { copyToClipboard("Deposit Amount", amountInput) },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(if (isDark) Color(0xFF2C3240) else Color(0xFFF0F4FA))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy Amount",
                                            tint = Color(0xFF2E7D32),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                if (!activeMethod.description.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = activeMethod.description,
                                        fontSize = 12.sp,
                                        color = SoftUITheme.TextSecondary,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Transaction ID Input
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(18.dp))
                                .clip(RoundedCornerShape(18.dp))
                                .background(cardBg)
                                .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Enter Transaction ID (TrxID)",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftUITheme.TextDark
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = trxIdInput,
                                    onValueChange = { trxIdInput = it },
                                    placeholder = { Text("e.g. 9J4K2L8M1") },
                                    leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        // Verify & Submit Deposit Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .shadow(4.dp, RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (trxIdInput.isNotBlank() && !isSubmitting) {
                                        Brush.linearGradient(listOf(Color(0xFF0866FF), Color(0xFF0052D4)))
                                    } else {
                                        Brush.linearGradient(listOf(Color.Gray, Color.DarkGray))
                                    }
                                )
                                .clickable(enabled = trxIdInput.isNotBlank() && !isSubmitting) {
                                    val amt = amountInput.toDoubleOrNull() ?: 0.0
                                    if (amt <= 0) {
                                        Toast.makeText(context, "Please enter a valid amount.", Toast.LENGTH_SHORT).show()
                                        return@clickable
                                    }
                                    if (trxIdInput.isBlank()) {
                                        Toast.makeText(context, "Please enter Transaction ID.", Toast.LENGTH_SHORT).show()
                                        return@clickable
                                    }

                                    isSubmitting = true
                                    scope.launch {
                                        val res = viewModel.repository.submitDeposit(
                                            methodId = activeMethod.id,
                                            methodName = activeMethod.name,
                                            amount = amt,
                                            transactionId = trxIdInput
                                        )
                                        isSubmitting = false
                                        if (res.isSuccess) {
                                            submissionTimestamp = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
                                            step = 3 // Show Thank You Screen with Statement!
                                        } else {
                                            Toast.makeText(context, res.exceptionOrNull()?.message ?: "Deposit submission failed", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                                .testTag("verify_deposit_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Verify & Submit Deposit",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun PaymentMethodLogo(
    method: DepositMethod,
    modifier: Modifier = Modifier.size(42.dp)
) {
    val cleanUrl = resolveMethodImageUrl(method.imageUrl)
    val nameLower = method.name.lowercase(Locale.getDefault())

    val (badgeBg, badgeText, badgeTextColor) = when {
        nameLower.contains("bkash") -> Triple(Color(0xFFE2136E), "bKash", Color.White)
        nameLower.contains("nagad") -> Triple(Color(0xFFF7941D), "Nagad", Color.White)
        nameLower.contains("rocket") -> Triple(Color(0xFF8C3494), "Rocket", Color.White)
        nameLower.contains("upay") -> Triple(Color(0xFF002A54), "Upay", Color(0xFFFFD200))
        nameLower.contains("cellfin") -> Triple(Color(0xFF0B6330), "Cellfin", Color.White)
        nameLower.contains("bank") -> Triple(Color(0xFF1E88E5), "Bank", Color.White)
        else -> Triple(Color(0xFF0866FF), method.name.take(4).uppercase(), Color.White)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(0.8.dp, Color(0x22000000), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (!cleanUrl.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(cleanUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = method.name,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(3.dp),
                contentScale = ContentScale.Fit,
                loading = {
                    BrandBadgeBox(badgeBg, badgeText, badgeTextColor)
                },
                error = {
                    BrandBadgeBox(badgeBg, badgeText, badgeTextColor)
                }
            )
        } else {
            BrandBadgeBox(badgeBg, badgeText, badgeTextColor)
        }
    }
}

@Composable
private fun BrandBadgeBox(bg: Color, text: String, textColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

fun resolveMethodImageUrl(url: String?): String? {
    if (url.isNullOrBlank()) return null
    return com.example.data.remote.SecureConfig.normalizeUrl(url)
}

@Composable
fun DepositStatementRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = SoftUITheme.TextSecondary
        )
        Text(
            text = value,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = SoftUITheme.TextDark,
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun DepositStatementRowWithCopy(label: String, value: String, onCopy: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = SoftUITheme.TextSecondary
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onCopy() }
        ) {
            Text(
                text = value,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0866FF)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy",
                tint = Color(0xFF0866FF),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}


