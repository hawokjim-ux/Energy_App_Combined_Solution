<?php
header('Content-Type: application/json');
$input = file_get_contents('php://input');
$mpesaResponse = json_decode($input, true);
$transactionStatus = ($mpesaResponse['Body']['stkCallback']['ResultCode'] ?? 1) === 0 ? 'SUCCESS' : 'FAILED';
$supabaseUrl = 'https://acqfnlizrkpfmogyxhtu.supabase.co/rest/v1';
$supabaseKey = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFjcWZubGl6cmtwZm1vZ3l4aHR1Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU0NjAxNTcsImV4cCI6MjA4MTAzNjE1N30.jOP8Hesw8ybi4ooRVgf8JiYyKsDtHTzDFuCfHS3PH6Y';
$updateData = ['transaction_status' => $transactionStatus];
$curl = curl_init($supabaseUrl . '/sales_records');
curl_setopt_array($curl, [CURLOPT_RETURNTRANSFER => true, CURLOPT_CUSTOMREQUEST => 'PATCH', CURLOPT_POSTFIELDS => json_encode($updateData), CURLOPT_HTTPHEADER => ['Content-Type: application/json', 'apikey: ' . $supabaseKey, 'Authorization: Bearer ' . $supabaseKey]]);
curl_exec($curl);
curl_close($curl);
http_response_code(200);
echo json_encode(['ResultCode' => 0]);
?>
