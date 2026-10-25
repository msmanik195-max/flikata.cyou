<?php
/**
 * Database Configuration & Global Helpers
 * Flikata Backend Engine
 */

header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit;
}

// Database Credentials
define('DB_HOST', 'localhost');
define('DB_NAME', 'flikata_bd');
define('DB_USER', 'flikata_bd');
define('DB_PASS', 'flikata_bd');
define('DB_CHARSET', 'utf8mb4');

function getDB() {
    static $pdo = null;
    if ($pdo === null) {
        $dsn = "mysql:host=" . DB_HOST . ";dbname=" . DB_NAME . ";charset=" . DB_CHARSET;
        $options = [
            PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
            PDO::ATTR_EMULATE_PREPARES   => false,
        ];
        try {
            $pdo = new PDO($dsn, DB_USER, DB_PASS, $options);
            try {
                require_once __DIR__ . '/schema_update.php';
                if (function_exists('ensureVerificationAndWalletSchema')) {
                    ensureVerificationAndWalletSchema($pdo);
                }
            } catch (Throwable $se) {
                error_log("Schema update notice: " . $se->getMessage());
            }
        } catch (Throwable $e) {
            if (headers_sent() || (isset($_SERVER['HTTP_ACCEPT']) && strpos($_SERVER['HTTP_ACCEPT'], 'text/html') !== false)) {
                echo "<div style='font-family:sans-serif;padding:20px;background:#fee2e2;border:1px solid #ef4444;border-radius:8px;max-width:600px;margin:40px auto;color:#991b1b;'>";
                echo "<h3>Database Connection / Configuration Notice</h3>";
                echo "<p><strong>Message:</strong> " . htmlspecialchars($e->getMessage()) . "</p>";
                echo "<p style='font-size:13px;color:#7f1d1d;'>Please check your <code>backend/config/db.php</code> settings (DB_HOST, DB_NAME, DB_USER, DB_PASS) and make sure your database is created and imported.</p>";
                echo "</div>";
            } else {
                header('Content-Type: application/json; charset=utf-8');
                echo json_encode([
                    'success' => false,
                    'message' => 'Database error: ' . $e->getMessage()
                ], JSON_UNESCAPED_UNICODE);
            }
            exit;
        }
    }
    return $pdo;
}

function jsonResponse($success, $data = null, $message = '') {
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode([
        'success' => (bool)$success,
        'message' => $message,
        'data'    => $data
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

function getAuthUser($pdo) {
    $headers = getallheaders();
    $token = null;
    if (isset($headers['Authorization'])) {
        $matches = [];
        if (preg_match('/Bearer\s(\S+)/', $headers['Authorization'], $matches)) {
            $token = $matches[1];
        } else {
            $token = trim($headers['Authorization']);
        }
    } elseif (isset($_REQUEST['auth_token'])) {
        $token = $_REQUEST['auth_token'];
    }

    if ($token) {
        $stmt = $pdo->prepare("SELECT * FROM users WHERE auth_token = ? LIMIT 1");
        $stmt->execute([$token]);
        $u = $stmt->fetch();
        if ($u) {
            return $u;
        }
    }

    // Direct fallback by sender_id, user_id, or X-Sender-Id
    $fallbackId = !empty($_REQUEST['sender_id']) ? (int)$_REQUEST['sender_id'] :
                  (!empty($_REQUEST['user_id']) ? (int)$_REQUEST['user_id'] :
                  (!empty($headers['X-Sender-Id']) ? (int)$headers['X-Sender-Id'] :
                  (!empty($headers['x-sender-id']) ? (int)$headers['x-sender-id'] : 0)));
    if ($fallbackId > 0) {
        $stmt = $pdo->prepare("SELECT * FROM users WHERE id = ? LIMIT 1");
        $stmt->execute([$fallbackId]);
        return $stmt->fetch() ?: null;
    }

    return null;
}

