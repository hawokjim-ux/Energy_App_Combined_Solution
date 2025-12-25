<?php
/**
 * get_transactions.php
 * Fetches M-Pesa transaction history from Supabase
 * For AlphaPlus POS App
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

// Supabase Configuration
$SUPABASE_URL = "https://pxcdaivlvltmdifxietb.supabase.co";
$SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InB4Y2RhaXZsdmx0bWRpZnhpZXRiIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU3NDI3NDIsImV4cCI6MjA4MTMxODc0Mn0.s6nv24s6M83gAcW_nSKCBfcXcqJ_7owwqdObPDT7Ky0";

// Get query parameters
$from_date = isset($_GET['from_date']) ? $_GET['from_date'] : null;
$to_date = isset($_GET['to_date']) ? $_GET['to_date'] : null;
$search = isset($_GET['search']) ? $_GET['search'] : null;
$limit = isset($_GET['limit']) ? (int)$_GET['limit'] : 100;

try {
    // Build Supabase REST API URL
    $endpoint = $SUPABASE_URL . "/rest/v1/mpesa_transactions";
    $params = [];
    
    // Select all fields
    $params[] = "select=*";
    
    // Date filters
    if ($from_date) {
        $params[] = "created_at=gte." . $from_date . "T00:00:00";
    }
    if ($to_date) {
        $params[] = "created_at=lte." . $to_date . "T23:59:59";
    }
    
    // Only show completed transactions (optional - remove if you want all)
    // $params[] = "status=eq.completed";
    
    // Order by most recent
    $params[] = "order=created_at.desc";
    
    // Limit results
    $params[] = "limit=" . $limit;
    
    $url = $endpoint . "?" . implode("&", $params);
    
    // Make request to Supabase
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        "apikey: " . $SUPABASE_ANON_KEY,
        "Authorization: Bearer " . $SUPABASE_ANON_KEY,
        "Content-Type: application/json"
    ]);
    
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);
    
    if ($httpCode !== 200) {
        throw new Exception("Supabase error: HTTP " . $httpCode);
    }
    
    $transactions = json_decode($response, true);
    
    if ($transactions === null) {
        $transactions = [];
    }
    
    // Apply search filter
    if ($search && !empty($transactions)) {
        $searchLower = strtolower($search);
        $transactions = array_filter($transactions, function($t) use ($searchLower) {
            return (isset($t['phone']) && strpos(strtolower($t['phone']), $searchLower) !== false) ||
                   (isset($t['mpesa_receipt_number']) && strpos(strtolower($t['mpesa_receipt_number']), $searchLower) !== false) ||
                   (isset($t['mpesa_receipt']) && strpos(strtolower($t['mpesa_receipt']), $searchLower) !== false) ||
                   (isset($t['account_ref']) && strpos(strtolower($t['account_ref']), $searchLower) !== false) ||
                   (isset($t['checkout_request_id']) && strpos(strtolower($t['checkout_request_id']), $searchLower) !== false);
        });
        $transactions = array_values($transactions);
    }
    
    // Transform to expected format for Android app
    $formattedTransactions = [];
    $totalAmount = 0;
    $completedCount = 0;
    
    foreach ($transactions as $t) {
        $amount = isset($t['amount']) ? floatval($t['amount']) : 0;
        $totalAmount += $amount;
        
        $status = isset($t['status']) ? $t['status'] : 'pending';
        if ($status === 'completed') {
            $completedCount++;
        }
        
        // Get receipt number from either field
        $receipt = '';
        if (!empty($t['mpesa_receipt_number'])) {
            $receipt = $t['mpesa_receipt_number'];
        } elseif (!empty($t['mpesa_receipt'])) {
            $receipt = $t['mpesa_receipt'];
        }
        
        $formattedTransactions[] = [
            "paymentId" => isset($t['id']) ? $t['id'] : "",
            "invoiceNo" => isset($t['account_ref']) ? $t['account_ref'] : "",
            "invoiceDate" => isset($t['created_at']) ? substr($t['created_at'], 0, 10) : "",
            "customerName" => isset($t['phone']) ? $t['phone'] : "Customer",
            "phoneNumber" => isset($t['phone']) ? $t['phone'] : "",
            "amount" => $amount,
            "mpesaReceiptNumber" => $receipt,
            "paymentDate" => isset($t['completed_at']) ? $t['completed_at'] : (isset($t['created_at']) ? $t['created_at'] : ""),
            "paymentMode" => "M-Pesa",
            "transactionStatus" => ucfirst($status),
            "salesmanName" => "",
            "grandTotal" => $amount,
            "checkoutRequestId" => isset($t['checkout_request_id']) ? $t['checkout_request_id'] : "",
            "resultDesc" => isset($t['result_desc']) ? $t['result_desc'] : ""
        ];
    }
    
    // Build response
    echo json_encode([
        "success" => true,
        "message" => "Transactions fetched successfully",
        "data" => $formattedTransactions,
        "summary" => [
            "totalTransactions" => count($formattedTransactions),
            "completedTransactions" => $completedCount,
            "totalAmount" => $totalAmount,
            "dateRange" => [
                "from" => $from_date ?? date('Y-m-d', strtotime('-30 days')),
                "to" => $to_date ?? date('Y-m-d')
            ]
        ]
    ]);
    
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "message" => $e->getMessage(),
        "data" => [],
        "summary" => null
    ]);
}
?>
