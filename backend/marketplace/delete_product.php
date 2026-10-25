<?php
/**
 * Delete Marketplace Product Listing Endpoint
 * Flikata API
 */

require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$input = json_decode(file_get_contents('php://input'), true) ?: $_POST;

$productId = (int)($input['product_id'] ?? ($_GET['product_id'] ?? 0));
$rawUserId = trim((string)($input['user_id'] ?? ($_GET['user_id'] ?? '')));
$cleanUserId = preg_replace('/[^0-9]/', '', $rawUserId);
$userId = (int)$cleanUserId;

if (!$productId) {
    jsonResponse(false, null, 'Product ID is required.');
}

try {
    $stmt = $pdo->prepare("SELECT user_id FROM marketplace_products WHERE id = ?");
    $stmt->execute([$productId]);
    $prod = $stmt->fetch();

    if (!$prod) {
        jsonResponse(false, null, 'Product not found.');
    }

    if ($userId > 0 && (int)$prod['user_id'] !== $userId) {
        // Check if admin
        $uStmt = $pdo->prepare("SELECT role FROM admin_users WHERE id = ?");
        $uStmt->execute([$userId]);
        $isAdmin = $uStmt->fetch();
        if (!$isAdmin) {
            jsonResponse(false, null, 'You are not authorized to delete this listing.');
        }
    }

    $del = $pdo->prepare("DELETE FROM marketplace_products WHERE id = ?");
    $del->execute([$productId]);

    jsonResponse(true, ['id' => (string)$productId], 'Listing deleted successfully.');
} catch (Exception $e) {
    jsonResponse(false, null, 'Failed to delete product: ' . $e->getMessage());
}
