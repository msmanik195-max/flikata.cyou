package com.example.ui.screens.wallet

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DepositRequestItem
import com.example.data.model.WithdrawRequestItem
import com.example.data.model.WalletData
import com.example.data.model.WalletTransactionItem
import com.example.ui.components.SoftUITheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Modern Flikata Wallet Screen
 * Matches Screenshot 2:
 * - Top Bar: Back arrow, centered "Wallet" title, History icon
 * - Rich Royal Blue Available Balance Card with "Available Balance", "৳ BDT" pill, bold amount, divider, "Total In" & "Total Out"
 * - 3 Action Cards: Recharge (+), Withdraw (wallet), Transaction History (clock)
 * - Transactions Section: "Transactions" header, "See All", empty state with receipt icon
 * - Bottom Sheet Modal for Transaction History
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUser by viewModel.currentUser.collectAsState()
    val isDark = SoftUITheme.isDark

    var walletData by remember { mutableStateOf<WalletData?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Modal Bottom Sheet State for Transaction History
    var showHistorySheet by remember { mutableStateOf(false) }
    val historySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Rejection details modal state
    var selectedRejectionItem by remember { mutableStateOf<RequestDisplayItem?>(null) }

    fun loadWallet() {
        isLoading = true
        scope.launch {
            val res = viewModel.repository.getWallet()
            if (res.isSuccess) {
                walletData = res.getOrNull()
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadWallet()
    }

    val transactions = walletData?.transactions ?: emptyList()
    val depositRequests = walletData?.depositRequests ?: emptyList()
    val withdrawRequests = walletData?.withdrawRequests ?: emptyList()

    val allRequests = remember(depositRequests, withdrawRequests) {
        val dList = depositRequests.map {
            RequestDisplayItem(
                id = it.id,
                isDeposit = true,
                methodName = it.methodName,
                amount = it.amount,
                status = it.status.lowercase(),
                referenceInfo = "Trx: ${it.transactionId}",
                rejectionReason = it.rejectionReason,
                createdAt = it.createdAt
            )
        }
        val wList = withdrawRequests.map {
            RequestDisplayItem(
                id = it.id,
                isDeposit = false,
                methodName = it.methodName,
                amount = it.amount,
                status = it.status.lowercase(),
                referenceInfo = "${it.accountName} (${it.accountNumber})",
                rejectionReason = it.rejectionReason,
                createdAt = it.createdAt
            )
        }
        (dList + wList).sortedByDescending { it.id }
    }

    val totalIn = transactions.filter {
        it.type.equals("deposit", ignoreCase = true) || it.type.equals("refund", ignoreCase = true)
    }.sumOf { it.amount }

    val totalOut = transactions.filter {
        it.type.equals("withdraw", ignoreCase = true) || it.type.equals("verification", ignoreCase = true) || it.type.equals("expense", ignoreCase = true)
    }.sumOf { it.amount }

    // Colors matching Screenshot 2
    val screenBgColor = if (isDark) Color(0xFF121418) else Color(0xFFF8F9FA)
    val cardBg = if (isDark) Color(0xFF1E222B) else Color.White
    val cardBorder = if (isDark) Color(0x33FFFFFF) else Color(0xFFE5E7EB)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ==========================================
            // 1. TOP BAR
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("wallet_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = if (isDark) Color.White else Color(0xFF1E293B),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Wallet",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )

                IconButton(
                    onClick = { showHistorySheet = true },
                    modifier = Modifier.testTag("wallet_top_history_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "History",
                        tint = if (isDark) Color.White else Color(0xFF1E293B),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF1570EF))
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ==========================================
                    // 2. MAIN AVAILABLE BALANCE CARD (Screenshot 2 Match)
                    // ==========================================
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(8.dp, RoundedCornerShape(24.dp), ambientColor = Color(0x331570EF), spotColor = Color(0x551570EF))
                                .clip(RoundedCornerShape(24.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF1570EF), // Vibrant rich royal blue
                                            Color(0xFF0D54CF)
                                        )
                                    )
                                )
                                .padding(22.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Top Row: "Available Balance" + "৳ BDT" Pill
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Available Balance",
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontWeight = FontWeight.Medium
                                    )

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(Color.White.copy(alpha = 0.22f))
                                            .padding(horizontal = 12.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = "৳ BDT",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Large Balance
                                Text(
                                    text = "৳ ${String.format(Locale.getDefault(), "%.2f", walletData?.walletBalance ?: 0.0)}",
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                // Divider Line
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Color.White.copy(alpha = 0.22f))
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Bottom Row: Total In & Total Out
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    // Total In
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Total In",
                                            fontSize = 12.5.sp,
                                            color = Color.White.copy(alpha = 0.75f),
                                            fontWeight = FontWeight.Normal
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "৳ ${String.format(Locale.getDefault(), "%.2f", totalIn)}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    // Total Out
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Total Out",
                                            fontSize = 12.5.sp,
                                            color = Color.White.copy(alpha = 0.75f),
                                            fontWeight = FontWeight.Normal
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "৳ ${String.format(Locale.getDefault(), "%.2f", totalOut)}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ==========================================
                    // 3. THREE ACTION CARDS ROW (Screenshot 2 Match)
                    // ==========================================
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // 1. Recharge Card (Light blue circular + icon)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(125.dp)
                                    .shadow(2.dp, RoundedCornerShape(18.dp), ambientColor = Color(0x11000000))
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(cardBg)
                                    .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                                    .clickable { viewModel.navigateTo(Screen.DEPOSIT) }
                                    .padding(vertical = 14.dp, horizontal = 8.dp)
                                    .testTag("wallet_recharge_card"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE0F2FE)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Recharge",
                                            tint = Color(0xFF0284C7),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "Recharge",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color.White else Color(0xFF0F172A)
                                    )
                                }
                            }

                            // 2. Withdraw Card (Light green circular wallet icon)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(125.dp)
                                    .shadow(2.dp, RoundedCornerShape(18.dp), ambientColor = Color(0x11000000))
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(cardBg)
                                    .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                                    .clickable { viewModel.navigateTo(Screen.WITHDRAW) }
                                    .padding(vertical = 14.dp, horizontal = 8.dp)
                                    .testTag("wallet_withdraw_card"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFDCFCE7)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccountBalanceWallet,
                                            contentDescription = "Withdraw",
                                            tint = Color(0xFF16A34A),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "Withdraw",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color.White else Color(0xFF0F172A)
                                    )
                                }
                            }

                            // 3. Transaction History Card (Light amber circular clock icon)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(125.dp)
                                    .shadow(2.dp, RoundedCornerShape(18.dp), ambientColor = Color(0x11000000))
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(cardBg)
                                    .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                                    .clickable { showHistorySheet = true }
                                    .padding(vertical = 10.dp, horizontal = 6.dp)
                                    .testTag("wallet_history_card"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFEF3C7)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = "History",
                                            tint = Color(0xFFD97706),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Transaction\nHistory",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 15.sp,
                                        color = if (isDark) Color.White else Color(0xFF0F172A)
                                    )
                                }
                            }
                        }
                    }

                    // ==========================================
                    // 3.5. MY REQUESTS SECTION (Pending, Approved, Rejected)
                    // ==========================================
                    if (allRequests.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "My Requests",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Color(0xFF0F172A)
                                )

                                Text(
                                    text = "See All",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1570EF),
                                    modifier = Modifier
                                        .clickable { showHistorySheet = true }
                                        .padding(vertical = 4.dp, horizontal = 6.dp)
                                )
                            }
                        }

                        items(allRequests.take(4)) { req ->
                            RequestRowCard(
                                item = req,
                                isDark = isDark,
                                cardBg = cardBg,
                                cardBorder = cardBorder,
                                onShowReason = { selectedRejectionItem = it }
                            )
                        }
                    }

                    // ==========================================
                    // 4. TRANSACTIONS SECTION (Screenshot 2 Match)
                    // ==========================================
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Transactions",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF0F172A)
                            )

                            Text(
                                text = "See All",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1570EF),
                                modifier = Modifier
                                    .clickable { showHistorySheet = true }
                                    .padding(vertical = 4.dp, horizontal = 6.dp)
                            )
                        }
                    }

                    if (transactions.isEmpty()) {
                        // Empty State Card (Screenshot 2 Match)
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(2.dp, RoundedCornerShape(20.dp), ambientColor = Color(0x0A000000))
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(cardBg)
                                    .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
                                    .padding(vertical = 44.dp, horizontal = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.ReceiptLong,
                                        contentDescription = "No transactions",
                                        tint = Color(0xFFCBD5E1),
                                        modifier = Modifier.size(54.dp)
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Text(
                                        text = "No transactions yet",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    } else {
                        // Recent 5 Transactions preview
                        items(transactions.take(5)) { tx ->
                            TransactionRowItem(tx = tx, isDark = isDark, cardBg = cardBg, cardBorder = cardBorder)
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }

    // =========================================================================
    // 5. TRANSACTION HISTORY MODAL BOTTOM SHEET
    // =========================================================================
    if (showHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
            sheetState = historySheetState,
            containerColor = if (isDark) Color(0xFF1E222B) else Color.White,
            shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
        ) {
            var selectedFilterTab by remember { mutableIntStateOf(0) }

            val filterTabs = listOf(
                "All",
                "Pending Deposit",
                "Approved Deposit",
                "Rejected Deposit",
                "Pending Withdraw",
                "Approved Withdraw",
                "Rejected Withdraw"
            )

            val filteredItems: List<RequestDisplayItem> = when (selectedFilterTab) {
                1 -> allRequests.filter { it.isDeposit && it.status == "pending" }
                2 -> allRequests.filter { it.isDeposit && it.status == "approved" }
                3 -> allRequests.filter { it.isDeposit && it.status == "rejected" }
                4 -> allRequests.filter { !it.isDeposit && it.status == "pending" }
                5 -> allRequests.filter { !it.isDeposit && it.status == "approved" }
                6 -> allRequests.filter { !it.isDeposit && it.status == "rejected" }
                else -> allRequests
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Transaction History",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )
                        Text(
                            text = when (selectedFilterTab) {
                                0 -> "${allRequests.size + transactions.size} total records"
                                else -> "${filteredItems.size} record(s)"
                            },
                            fontSize = 12.5.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    IconButton(onClick = { showHistorySheet = false }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = if (isDark) Color.White else Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Filter Tabs: Exactly matching user requirement in English
                ScrollableTabRow(
                    selectedTabIndex = selectedFilterTab,
                    containerColor = if (isDark) Color(0xFF161920) else Color(0xFFF1F5F9),
                    edgePadding = 8.dp,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    filterTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedFilterTab == index,
                            onClick = { selectedFilterTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedFilterTab == index) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.5.sp,
                                    maxLines = 1
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedFilterTab == 0 && filteredItems.isEmpty() && transactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = Color(0xFFCBD5E1),
                                modifier = Modifier.size(50.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No records found",
                                fontSize = 14.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                } else if (selectedFilterTab != 0 && filteredItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = Color(0xFFCBD5E1),
                                modifier = Modifier.size(50.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No records for ${filterTabs[selectedFilterTab]}",
                                fontSize = 14.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (selectedFilterTab == 0) {
                            // Show all requests first
                            items(allRequests) { req ->
                                RequestRowCard(
                                    item = req,
                                    isDark = isDark,
                                    cardBg = if (isDark) Color(0xFF262B36) else Color(0xFFF8FAFC),
                                    cardBorder = cardBorder,
                                    onShowReason = { selectedRejectionItem = it }
                                )
                            }
                            // Then any other wallet transactions
                            items(transactions) { tx ->
                                TransactionRowItem(
                                    tx = tx,
                                    isDark = isDark,
                                    cardBg = if (isDark) Color(0xFF262B36) else Color(0xFFF8FAFC),
                                    cardBorder = cardBorder
                                )
                            }
                        } else {
                            items(filteredItems) { req ->
                                RequestRowCard(
                                    item = req,
                                    isDark = isDark,
                                    cardBg = if (isDark) Color(0xFF262B36) else Color(0xFFF8FAFC),
                                    cardBorder = cardBorder,
                                    onShowReason = { selectedRejectionItem = it }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // 6. REJECTION REASON DIALOG
    // =========================================================================
    if (selectedRejectionItem != null) {
        val rejItem = selectedRejectionItem!!
        AlertDialog(
            onDismissRequest = { selectedRejectionItem = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Rejection Reason",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Rejection Reason",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = SoftUITheme.TextDark
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (rejItem.isDeposit) {
                            "Your deposit request of ৳ ${String.format(Locale.getDefault(), "%.2f", rejItem.amount)} via ${rejItem.methodName} was rejected by admin."
                        } else {
                            "Your withdrawal request of ৳ ${String.format(Locale.getDefault(), "%.2f", rejItem.amount)} via ${rejItem.methodName} was rejected by admin."
                        },
                        fontSize = 13.5.sp,
                        color = SoftUITheme.TextDark,
                        lineHeight = 19.sp
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFEF2F2))
                            .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = "Admin Note / Reason:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB91C1C)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (!rejItem.rejectionReason.isNullOrBlank()) rejItem.rejectionReason else "No specific reason provided by the administrator.",
                                fontSize = 13.sp,
                                color = Color(0xFF7F1D1D),
                                lineHeight = 18.sp
                            )
                        }
                    }

                    if (!rejItem.isDeposit) {
                        Text(
                            text = "Refund Note: The withdrawal amount has been automatically refunded to your wallet balance.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF16A34A)
                        )
                    } else {
                        Text(
                            text = "Tip: Please double-check your payment number and transaction ID, then submit a new deposit request.",
                            fontSize = 12.sp,
                            color = SoftUITheme.TextSecondary
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedRejectionItem = null }) {
                    Text("Dismiss", fontWeight = FontWeight.Bold, color = Color(0xFF0866FF))
                }
            }
        )
    }
}

/**
 * Single Transaction Row Item
 */
@Composable
private fun TransactionRowItem(
    tx: WalletTransactionItem,
    isDark: Boolean,
    cardBg: Color,
    cardBorder: Color
) {
    val isIn = tx.type.equals("deposit", ignoreCase = true) || tx.type.equals("refund", ignoreCase = true)
    val isVerification = tx.type.equals("verification", ignoreCase = true)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
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
                // Circle icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (isIn) Color(0xFFDCFCE7)
                            else if (isVerification) Color(0xFFE0F2FE)
                            else Color(0xFFFEE2E2)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isIn) Icons.Default.ArrowDownward
                        else if (isVerification) Icons.Default.VerifiedUser
                        else Icons.Default.ArrowUpward,
                        contentDescription = tx.type,
                        tint = if (isIn) Color(0xFF16A34A)
                        else if (isVerification) Color(0xFF0284C7)
                        else Color(0xFFDC2626),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = tx.description.ifEmpty {
                            if (isIn) "Wallet Deposit" else "Payment Expense"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF0F172A),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tx.createdAt ?: "Recently",
                        fontSize = 11.5.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isIn) "+ " else "- "}৳ ${String.format(Locale.getDefault(), "%.2f", tx.amount)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isIn) Color(0xFF16A34A) else if (isDark) Color.White else Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (tx.status.equals("completed", ignoreCase = true)) Color(0xFFDCFCE7)
                            else if (tx.status.equals("pending", ignoreCase = true)) Color(0xFFFEF3C7)
                            else Color(0xFFF1F5F9)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = tx.status.replaceFirstChar { it.uppercase() },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (tx.status.equals("completed", ignoreCase = true)) Color(0xFF15803D)
                        else if (tx.status.equals("pending", ignoreCase = true)) Color(0xFFB45309)
                        else Color(0xFF475569)
                    )
                }
            }
        }
    }
}

/**
 * Unified model for rendering Deposit & Withdraw Requests
 */
data class RequestDisplayItem(
    val id: Int,
    val isDeposit: Boolean,
    val methodName: String,
    val amount: Double,
    val status: String, // "pending", "approved", "rejected"
    val referenceInfo: String,
    val rejectionReason: String?,
    val createdAt: String?
)

/**
 * Request Row Card: Shows Deposit/Withdraw, amount, method, status badge,
 * and on Rejected status: an icon to view the rejection reason.
 */
@Composable
private fun RequestRowCard(
    item: RequestDisplayItem,
    isDark: Boolean,
    cardBg: Color,
    cardBorder: Color,
    onShowReason: (RequestDisplayItem) -> Unit
) {
    val isPending = item.status.equals("pending", ignoreCase = true)
    val isApproved = item.status.equals("approved", ignoreCase = true)
    val isRejected = item.status.equals("rejected", ignoreCase = true)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
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
                // Circle icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (item.isDeposit) Color(0xFFDCFCE7)
                            else Color(0xFFFEF3C7)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (item.isDeposit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = if (item.isDeposit) "Deposit" else "Withdraw",
                        tint = if (item.isDeposit) Color(0xFF16A34A) else Color(0xFFD97706),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (item.isDeposit) "Deposit (${item.methodName})" else "Withdraw (${item.methodName})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF0F172A),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.referenceInfo,
                        fontSize = 11.5.sp,
                        color = Color(0xFF64748B),
                        maxLines = 1
                    )
                    Text(
                        text = item.createdAt ?: "Recently",
                        fontSize = 10.5.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (item.isDeposit) "+ " else "- "}৳ ${String.format(Locale.getDefault(), "%.2f", item.amount)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (item.isDeposit) Color(0xFF16A34A) else if (isDark) Color.White else Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                when {
                                    isApproved -> Color(0xFFDCFCE7)
                                    isPending -> Color(0xFFFEF3C7)
                                    isRejected -> Color(0xFFFEE2E2)
                                    else -> Color(0xFFF1F5F9)
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 2.5.dp)
                    ) {
                        Text(
                            text = when {
                                isApproved -> "Approved"
                                isPending -> "Pending"
                                isRejected -> "Rejected"
                                else -> item.status.replaceFirstChar { it.uppercase() }
                            },
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isApproved -> Color(0xFF15803D)
                                isPending -> Color(0xFFB45309)
                                isRejected -> Color(0xFFDC2626)
                                else -> Color(0xFF475569)
                            }
                        )
                    }

                    // Rejection info icon button: tap to view admin rejection reason
                    if (isRejected) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEE2E2))
                                .clickable { onShowReason(item) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "View Rejection Reason",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

