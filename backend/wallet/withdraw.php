<?php
/**
 * Submit Withdraw Request
 * Flikata API
 */
require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$auth = getAuthUser($pdo);
$input = json_decode(file_get_contents('php://input'), true) ?: $_POST;

$userId = $auth ? (int)$auth['id'] : (!empty($input['user_id']) ? (int)$input['user_id'] : 0);
$methodId = !empty($input['method_id']) ? (int)$input['method_id'] : 0;
$methodName = !empty($input['method_name']) ? trim($input['method_name']) : '';
$amount = !empty($input['amount']) ? (float)$input['amount'] : 0.00;
$accountName = !empty($input['account_name']) ? trim($input['account_name']) : '';
$accountNumber = !empty($input['account_number']) ? trim($input['account_number']) : '';

if (!$userId || $amount <= 0 || empty($accountName) || empty($accountNumber)) {
    jsonResponse(false, null, 'User ID, valid withdrawal amount, account name, and account number are required.');
}

// 1. Fetch user & check wallet balance
$uStmt = $pdo->prepare("SELECT id, full_name, wallet_balance FROM users WHERE id = ?");
$uStmt->execute([$userId]);
$user = $uStmt->fetch();

if (!$user) {
    jsonResponse(false, null, 'User not found.');
}

$balance = (float)($user['wallet_balance'] ?? 0.00);
if ($amount > $balance) {
    jsonResponse(false, null, 'Insufficient wallet balance. Your available balance is ৳ ' . number_format($balance, 2));
}

// 2. Fetch method name if missing & check min/max limits
if ($methodId) {
    $mStmt = $pdo->prepare("SELECT name, min_amount, max_amount FROM withdraw_methods WHERE id = ?");
    $mStmt->execute([$methodId]);
    $methodRow = $mStmt->fetch();
    if ($methodRow) {
        if (empty($methodName)) $methodName = $methodRow['name'];
        $min = (float)($methodRow['min_amount'] ?? 50.00);
        $max = (float)($methodRow['max_amount'] ?? 25000.00);
        if ($amount < $min) {
            jsonResponse(false, null, "Minimum withdrawal amount for {$methodName} is ৳ " . number_format($min, 2));
        }
        if ($max > 0 && $amount > $max) {
            jsonResponse(false, null, "Maximum withdrawal amount for {$methodName} is ৳ " . number_format($max, 2));
        }
    }
}
if (empty($methodName)) {
    $methodName = 'Online Transfer';
}

$pdo->beginTransaction();
try {
    // 3. Deduct balance from user
    $deductStmt = $pdo->prepare("UPDATE users SET wallet_balance = wallet_balance - ? WHERE id = ? AND wallet_balance >= ?");
    $deductStmt->execute([$amount, $userId, $amount]);
    if ($deductStmt->rowCount() === 0) {
        $pdo->rollBack();
        jsonResponse(false, null, 'Failed to deduct balance. Insufficient funds.');
    }

    // 4. Insert into withdraw_requests
    $wStmt = $pdo->prepare("INSERT INTO withdraw_requests (user_id, method_id, method_name, amount, account_name, account_number, status) VALUES (?, ?, ?, ?, ?, ?, 'pending')");
    $wStmt->execute([$userId, $methodId, $methodName, $amount, $accountName, $accountNumber]);
    $reqId = (int)$pdo->lastInsertId();

    // 5. Insert transaction log
    $tStmt = $pdo->prepare("INSERT INTO wallet_transactions (user_id, type, amount, description, status) VALUES (?, 'withdraw', ?, ?, 'pending')");
    $tDesc = "Withdrawal Request: {$methodName} ({$accountNumber})";
    $tStmt->execute([$userId, $amount, $tDesc]);

    $pdo->commit();

    jsonResponse(true, [
        'withdraw_id'    => $reqId,
        'amount'         => $amount,
        'method_name'    => $methodName,
        'account_name'   => $accountName,
        'account_number' => $accountNumber,
        'status'         => 'pending',
        'created_at'     => date('Y-m-d H:i:s')
    ], 'Withdrawal request submitted successfully! Your funds will be sent to your account once verified by admin.');
} catch (Exception $e) {
    if ($pdo->inTransaction()) $pdo->rollBack();
    jsonResponse(false, null, 'Withdrawal submission failed: ' . $e->getMessage());
}
