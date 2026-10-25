<?php
/**
 * Create Marketplace Product Listing Endpoint
 * Flikata API
 */

require_once __DIR__ . '/../config/db.php';
require_once __DIR__ . '/../r2_helper.php';
$pdo = getDB();

$input = json_decode(file_get_contents('php://input'), true) ?: $_POST;

$rawUserId = trim((string)($input['user_id'] ?? ($_GET['user_id'] ?? '')));
$cleanUserId = preg_replace('/[^0-9]/', '', $rawUserId);
$userId = (int)$cleanUserId;

if ($userId <= 0) {
    $authUser = getAuthUser($pdo);
    if ($authUser && !empty($authUser['id'])) {
        $userId = (int)$authUser['id'];
    }
}

// Verify that user exists in users table to prevent FK errors
$uCheck = $pdo->prepare("SELECT id FROM users WHERE id = ?");
$uCheck->execute([$userId]);
if (!$uCheck->fetch()) {
    $firstU = (int)($pdo->query("SELECT id FROM users ORDER BY id ASC LIMIT 1")->fetchColumn() ?: 0);
    if ($firstU > 0) {
        $userId = $firstU;
    } else {
        // Create initial user row if users table is empty
        $pdo->exec("INSERT INTO users (id, full_name, phone, created_at) VALUES (1, 'Flikata User', '01700000000', NOW())");
        $userId = 1;
    }
}

$title = trim((string)($input['title'] ?? ''));
$price = (float)($input['price'] ?? 0.0);
$category = trim((string)($input['category'] ?? 'General'));
$condition = trim((string)($input['condition'] ?? 'New'));
$location = trim((string)($input['location'] ?? 'Dhaka, Bangladesh'));
$description = trim((string)($input['description'] ?? ''));
$imageUrl = trim((string)($input['image_url'] ?? ''));

// Handle multipart file upload if image was uploaded as file (save to Cloudflare R2 in products/ folder)
if (isset($_FILES['image']) && $_FILES['image']['error'] === UPLOAD_ERR_OK) {
    $tmpPath = $_FILES['image']['tmp_name'];
    $fileData = file_get_contents($tmpPath);
    $fileSize = strlen($fileData);
    $ext = strtolower(pathinfo($_FILES['image']['name'], PATHINFO_EXTENSION)) ?: 'jpg';
    $mimeType = mime_content_type($tmpPath) ?: 'image/jpeg';
    
    // Upload to Cloudflare R2 under products/
    $bucket = CloudflareR2::getAvailableBucket($pdo, $fileSize);
    if ($bucket) {
        $uniqueKey = 'products/flikata_' . date('Ymd_His') . '_' . bin2hex(random_bytes(6)) . '.' . $ext;
        $uploadResult = CloudflareR2::uploadFile($bucket, $fileData, $uniqueKey, $mimeType);
        if ($uploadResult['success']) {
            CloudflareR2::incrementBucketUsage($pdo, $bucket['id'], $fileSize);
            $imageUrl = $uploadResult['file_url'];
        }
    }
    
    if (empty($imageUrl)) {
        $uploadsDir = __DIR__ . '/../uploads/products';
        if (!is_dir($uploadsDir)) {
            mkdir($uploadsDir, 0755, true);
        }
        $filename = 'product_' . time() . '_' . rand(1000, 9999) . '.' . $ext;
        $targetPath = $uploadsDir . '/' . $filename;
        if (move_uploaded_file($_FILES['image']['tmp_name'], $targetPath)) {
            $scheme = (isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] === 'on') ? 'https' : 'http';
            $host = $_SERVER['HTTP_HOST'];
            $imageUrl = "$scheme://$host/backend/uploads/products/$filename";
        }
    }
}

if (empty($title)) {
    jsonResponse(false, null, 'Product title is required.');
}

if ($price < 0) {
    $price = 0.0;
}

try {
    $stmt = $pdo->prepare("
        INSERT INTO marketplace_products 
        (user_id, title, price, currency, category, condition_state, location, description, image_url, created_at)
        VALUES (?, ?, ?, 'BDT', ?, ?, ?, ?, ?, NOW())
    ");
    $stmt->execute([
        $userId,
        $title,
        $price,
        $category,
        $condition,
        $location,
        $description,
        $imageUrl
    ]);

    $newId = $pdo->lastInsertId();

    // Fetch user details for complete response
    $uStmt = $pdo->prepare("SELECT full_name, profile_pic, is_verified FROM users WHERE id = ?");
    $uStmt->execute([$userId]);
    $seller = $uStmt->fetch();

    $product = [
        'id' => (string)$newId,
        'userId' => (string)$userId,
        'title' => $title,
        'price' => $price,
        'formattedPrice' => "৳ " . number_format($price, 0),
        'currency' => 'BDT',
        'category' => $category,
        'condition' => $condition,
        'location' => $location,
        'description' => $description,
        'imageUrl' => $imageUrl,
        'isSold' => false,
        'viewsCount' => 0,
        'createdAt' => date('Y-m-d H:i:s'),
        'sellerName' => $seller['full_name'] ?? 'Seller',
        'sellerAvatar' => $seller['profile_pic'] ?? '',
        'sellerVerified' => (bool)($seller['is_verified'] ?? false)
    ];

    jsonResponse(true, $product, 'Product listed successfully on Marketplace.');
} catch (Exception $e) {
    jsonResponse(false, null, 'Failed to list product: ' . $e->getMessage());
}
