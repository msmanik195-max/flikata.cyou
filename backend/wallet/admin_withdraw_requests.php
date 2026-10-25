<?php
/**
 * Admin API: Withdraw Requests Management
 * Flikata Admin Panel
 */
require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$action = !empty($_REQUEST['action']) ? trim($_REQUEST['action']) : '';
$input = json_decode(file_get_contents('php://input'), true) ?: $_POST;

if ($action === 'approve') {
    $reqId = !empty($input['request_id']) ? (int)$input['request_id'] : 0;
    if (!$reqId) {
        jsonResponse(false, null, 'Request ID required.');
    }

    $rStmt = $pdo->prepare("SELECT * FROM withdraw_requests WHERE id = ?");
    $rStmt->execute([$reqId]);
    $req = $rStmt->fetch();

    if (!$req) {
        jsonResponse(false, null, 'Withdrawal request not found.');
    }

    if ($req['status'] === 'approved') {
        jsonResponse(false, null, 'This request has already been approved.');
    }

    $userId = (int)$req['user_id'];
    $amount = (float)$req['amount'];
    $methodName = $req['method_name'];
    $accountNumber = $req['account_number'];

    $pdo->beginTransaction();
    try {
        // 1. Mark request as approved
        $up = $pdo->prepare("UPDATE withdraw_requests SET status = 'approved', updated_at = NOW() WHERE id = ?");
        $up->execute([$reqId]);

        // 2. Update wallet_transactions to completed
        $tUp = $pdo->prepare("UPDATE wallet_transactions SET status = 'completed' WHERE user_id = ? AND type = 'withdraw' AND status = 'pending' ORDER BY id DESC LIMIT 1");
        $tUp->execute([$userId]);

        // 3. Send Notification to User
        try {
            $notifMsg = "Your withdrawal request of ৳ " . number_format($amount, 2) . " ({$methodName} - {$accountNumber}) has been approved and sent.";
            $notif = $pdo->prepare("INSERT INTO notifications (user_id, sender_id, type, message, created_at) VALUES (?, ?, 'withdraw_approved', ?, NOW())");
            $notif->execute([$userId, 1, $notifMsg]);
        } catch (Exception $e) {}

        $pdo->commit();
        jsonResponse(true, null, 'Withdrawal request approved successfully.');
    } catch (Exception $e) {
        if ($pdo->inTransaction()) $pdo->rollBack();
        jsonResponse(false, null, 'Approve failed: ' . $e->getMessage());
    }
}

if ($action === 'reject') {
    $reqId = !empty($input['request_id']) ? (int)$input['request_id'] : 0;
    $reason = !empty($input['reason']) ? trim($input['reason']) : '';

    if (!$reqId) {
        jsonResponse(false, null, 'Request ID required.');
    }

    $rStmt = $pdo->prepare("SELECT * FROM withdraw_requests WHERE id = ?");
    $rStmt->execute([$reqId]);
    $req = $rStmt->fetch();

    if (!$req) {
        jsonResponse(false, null, 'Withdrawal request not found.');
    }

    if ($req['status'] === 'rejected') {
        jsonResponse(false, null, 'This request has already been rejected.');
    }

    $userId = (int)$req['user_id'];
    $amount = (float)$req['amount'];

    $pdo->beginTransaction();
    try {
        // 1. Mark request as rejected with reason
        $up = $pdo->prepare("UPDATE withdraw_requests SET status = 'rejected', rejection_reason = ?, updated_at = NOW() WHERE id = ?");
        $up->execute([$reason, $reqId]);

        // 2. Refund amount to user wallet
        $refStmt = $pdo->prepare("UPDATE users SET wallet_balance = wallet_balance + ? WHERE id = ?");
        $refStmt->execute([$amount, $userId]);

        // 3. Insert refund transaction
        $tStmt = $pdo->prepare("INSERT INTO wallet_transactions (user_id, type, amount, description, status) VALUES (?, 'refund', ?, ?, 'completed')");
        $refDesc = "Withdrawal rejected refund: ৳ " . number_format($amount, 2) . ($reason ? " (Reason: {$reason})" : "");
        $tStmt->execute([$userId, $amount, $refDesc]);

        // 4. Send Notification
        try {
            $notifMsg = "Your withdrawal request of ৳ " . number_format($amount, 2) . " was rejected and refunded to your wallet." . ($reason ? " Reason: {$reason}" : "");
            $notif = $pdo->prepare("INSERT INTO notifications (user_id, sender_id, type, message, created_at) VALUES (?, ?, 'withdraw_rejected', ?, NOW())");
            $notif->execute([$userId, 1, $notifMsg]);
        } catch (Exception $e) {}

        $pdo->commit();
        jsonResponse(true, null, 'Withdrawal request rejected and funds refunded to user.');
    } catch (Exception $e) {
        if ($pdo->inTransaction()) $pdo->rollBack();
        jsonResponse(false, null, 'Reject failed: ' . $e->getMessage());
    }
}

// Default: Fetch requests with filter
$tab = !empty($_GET['tab']) ? trim($_GET['tab']) : 'pending';
$sql = "
    SELECT 
        wr.*,
        u.full_name AS user_name,
        u.profile_pic AS user_avatar,
        u.email AS user_email,
        u.phone AS user_phone,
        u.wallet_balance
    FROM withdraw_requests wr
    LEFT JOIN users u ON wr.user_id = u.id
";

if (in_array($tab, ['pending', 'approved', 'rejected'])) {
    $sql .= " WHERE wr.status = " . $pdo->quote($tab);
}

$sql .= " ORDER BY wr.id DESC LIMIT 100";

$stmt = $pdo->query($sql);
$requests = $stmt->fetchAll();

// Counts for tabs
$counts = [
    'pending'  => (int)$pdo->query("SELECT COUNT(*) FROM withdraw_requests WHERE status = 'pending'")->fetchColumn(),
    'approved' => (int)$pdo->query("SELECT COUNT(*) FROM withdraw_requests WHERE status = 'approved'")->fetchColumn(),
    'rejected' => (int)$pdo->query("SELECT COUNT(*) FROM withdraw_requests WHERE status = 'rejected'")->fetchColumn(),
];

jsonResponse(true, [
    'counts'   => $counts,
    'requests' => $requests
], 'Withdrawal requests loaded.');
