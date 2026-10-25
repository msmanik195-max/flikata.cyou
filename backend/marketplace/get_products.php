<?php
/**
 * Get Marketplace Products Endpoint
 * Flikata API
 */

require_once __DIR__ . '/../config/db.php';
$pdo = getDB();

$category = trim((string)($_GET['category'] ?? ''));
$query = trim((string)($_GET['query'] ?? ''));
$location = trim((string)($_GET['location'] ?? ''));
$sellerId = (int)($_GET['user_id'] ?? 0);
$limit = max(1, min(100, (int)($_GET['limit'] ?? 50)));
$offset = max(0, (int)($_GET['offset'] ?? 0));

$sql = "
    SELECT mp.*, 
           COALESCE(u.full_name, 'Seller') AS seller_name, 
           COALESCE(u.profile_pic, '') AS seller_avatar, 
           COALESCE(u.is_verified, 0) AS seller_verified,
           COALESCE(u.phone, '') AS seller_phone,
           COALESCE(u.created_at, mp.created_at) AS seller_joined_at
    FROM marketplace_products mp
    LEFT JOIN users u ON mp.user_id = u.id
    WHERE 1=1
";
$params = [];

if ($category !== '' && strtolower($category) !== 'all') {
    $sql .= " AND (LOWER(mp.category) = LOWER(?) OR mp.category LIKE ?)";
    $params[] = $category;
    $params[] = "%$category%";
}

if ($query !== '') {
    $sql .= " AND (mp.title LIKE ? OR mp.description LIKE ? OR mp.location LIKE ?)";
    $params[] = "%$query%";
    $params[] = "%$query%";
    $params[] = "%$query%";
}

if ($location !== '' && strtolower($location) !== 'all') {
    $sql .= " AND mp.location LIKE ?";
    $params[] = "%$location%";
}

if ($sellerId > 0) {
    $sql .= " AND mp.user_id = ?";
    $params[] = $sellerId;
}

$sql .= " ORDER BY mp.created_at DESC LIMIT $limit OFFSET $offset";

try {
    $stmt = $pdo->prepare($sql);
    $stmt->execute($params);
    $rows = $stmt->fetchAll(PDO::FETCH_ASSOC);

    $products = [];
    foreach ($rows as $row) {
        $priceVal = (float)$row['price'];
        $formattedPrice = "৳ " . number_format($priceVal, 0);

        $products[] = [
            'id' => (string)$row['id'],
            'userId' => (string)$row['user_id'],
            'title' => $row['title'],
            'price' => $priceVal,
            'formattedPrice' => $formattedPrice,
            'currency' => $row['currency'] ?: 'BDT',
            'category' => $row['category'] ?: 'General',
            'condition' => $row['condition_state'] ?: 'New',
            'location' => $row['location'] ?: 'Dhaka, Bangladesh',
            'description' => $row['description'] ?: '',
            'imageUrl' => $row['image_url'] ?: '',
            'isSold' => (bool)$row['is_sold'],
            'viewsCount' => (int)$row['views_count'],
            'createdAt' => $row['created_at'],
            'sellerName' => $row['seller_name'] ?: 'Seller',
            'sellerAvatar' => $row['seller_avatar'] ?: '',
            'sellerVerified' => (bool)$row['seller_verified']
        ];
    }

    jsonResponse(true, $products, 'Products retrieved successfully.');
} catch (Exception $e) {
    jsonResponse(false, [], 'Failed to fetch products: ' . $e->getMessage());
}
