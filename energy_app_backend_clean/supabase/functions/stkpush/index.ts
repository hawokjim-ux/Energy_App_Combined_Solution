// Supabase Edge Function: M-Pesa STK Push - ULTRA FAST VERSION
// Deploy with: supabase functions deploy stkpush

import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

// Global token cache (persists during edge function warm instances)
let cachedToken: { token: string; expires: number } | null = null;

serve(async (req) => {
  // Handle CORS preflight
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  const startTime = Date.now();

  try {
    // Get M-Pesa credentials from environment
    const consumerKey = Deno.env.get("MPESA_CONSUMER_KEY")!;
    const consumerSecret = Deno.env.get("MPESA_CONSUMER_SECRET")!;
    const shortcode = Deno.env.get("MPESA_SHORTCODE")!;
    const passkey = Deno.env.get("MPESA_PASSKEY")!;
    const callbackUrl = Deno.env.get("MPESA_CALLBACK_URL")!;
    const environment = Deno.env.get("MPESA_ENVIRONMENT") || "production";

    // M-Pesa API URLs
    const baseUrl = environment === "production"
      ? "https://api.safaricom.co.ke"
      : "https://sandbox.safaricom.co.ke";

    // Get request body
    const {
      amount,
      phone,
      account,
      description,
      user_id,
      attendant_id,
      pump_id,
      pump_shift_id,
      shift_id,
      station_id,
      fuel_type_id,
      liters_sold,
      price_per_liter,
      fcm_token
    } = await req.json();

    // Validate inputs
    if (!amount || !phone) {
      throw new Error("Amount and phone are required");
    }

    // Format phone number
    const formattedPhone = formatPhoneNumber(phone);
    if (!formattedPhone.match(/^254(7\d{8}|1[01]\d{7})$/)) {
      throw new Error("Invalid phone number. Supported formats: 07XXXXXXXX, 0110XXXXXX, 0119XXXXXX, 0100XXXXXX, or 254XXXXXXXXX");
    }

    console.log(`⚡ [ULTRA FAST] STK Push - Phone: ${formattedPhone}, Amount: ${amount}`);

    // ⚡ STEP 1: Get cached or new access token
    const access_token = await getAccessToken(baseUrl, consumerKey, consumerSecret);
    const tokenTime = Date.now() - startTime;
    console.log(`🔑 Token obtained in ${tokenTime}ms`);

    // ⚡ STEP 2: Generate timestamp and password
    const timestamp = new Date().toISOString().replace(/[-:TZ.]/g, "").slice(0, 14);
    const password = btoa(`${shortcode}${passkey}${timestamp}`);
    const tillNumber = Deno.env.get("MPESA_TILL_NUMBER") || shortcode;

    // ⚡ STEP 3: Send STK Push (the ONLY blocking call)
    const stkPushData = {
      BusinessShortCode: shortcode,
      Password: password,
      Timestamp: timestamp,
      TransactionType: "CustomerBuyGoodsOnline",
      Amount: Math.floor(amount),
      PartyA: formattedPhone,
      PartyB: tillNumber,
      PhoneNumber: formattedPhone,
      CallBackURL: callbackUrl,
      AccountReference: account || "EnergyApp",
      TransactionDesc: description || "Fuel Payment",
    };

    const stkResponse = await fetch(`${baseUrl}/mpesa/stkpush/v1/processrequest`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${access_token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(stkPushData),
    });

    const stkResult = await stkResponse.json();
    const stkTime = Date.now() - startTime;
    console.log(`📱 STK Push sent in ${stkTime}ms`);

    if (stkResult.ResponseCode !== "0") {
      throw new Error(stkResult.errorMessage || stkResult.ResponseDescription || "STK Push failed");
    }

    // ⚡ RETURN IMMEDIATELY - Don't wait for DB writes!
    const processingTime = Date.now() - startTime;
    console.log(`✅ [ULTRA FAST] STK Push response in ${processingTime}ms`);

    // Generate sale ID for response
    const saleIdNo = `RCP-${String(Date.now()).slice(-5)}`;

    // 🔥 FIRE AND FORGET: Save to database in background (non-blocking)
    const supabaseUrl = Deno.env.get("SUPABASE_URL")!;
    const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;

    // Use EdgeRuntime.waitUntil to run DB operations after response
    const dbPromise = saveToDatabase(
      supabaseUrl,
      supabaseServiceKey,
      {
        saleIdNo,
        checkoutRequestId: stkResult.CheckoutRequestID,
        merchantRequestId: stkResult.MerchantRequestID,
        formattedPhone,
        amount,
        pump_shift_id,
        pump_id,
        attendant_id: attendant_id || user_id,
        station_id,
        fuel_type_id,
        liters_sold,
        price_per_liter,
        fcm_token,
      }
    );

    // Don't await - let it run in background
    dbPromise.catch((err) => console.error("Background DB error:", err));

    // Return success response IMMEDIATELY
    return new Response(
      JSON.stringify({
        success: true,
        message: "STK Push sent! Check your phone.",
        sale_id: saleIdNo,
        checkout_request_id: stkResult.CheckoutRequestID,
        merchant_request_id: stkResult.MerchantRequestID,
        processing_time_ms: processingTime,
      }),
      {
        headers: { ...corsHeaders, "Content-Type": "application/json" },
        status: 200,
      }
    );
  } catch (error) {
    const processingTime = Date.now() - startTime;
    console.error(`❌ [EDGE] STK Push error after ${processingTime}ms:`, error);

    return new Response(
      JSON.stringify({
        success: false,
        message: (error as Error).message || "An error occurred",
        processing_time_ms: processingTime,
      }),
      {
        headers: { ...corsHeaders, "Content-Type": "application/json" },
        status: 400,
      }
    );
  }
});

/**
 * Get cached access token or fetch new one
 */
async function getAccessToken(baseUrl: string, consumerKey: string, consumerSecret: string): Promise<string> {
  const now = Date.now();

  // Check if we have a valid cached token (with 60 second buffer)
  if (cachedToken && cachedToken.expires > now + 60000) {
    console.log("🔑 Using cached token");
    return cachedToken.token;
  }

  // Fetch new token
  console.log("🔄 Fetching new access token...");
  const credentials = btoa(`${consumerKey}:${consumerSecret}`);
  const tokenResponse = await fetch(`${baseUrl}/oauth/v1/generate?grant_type=client_credentials`, {
    headers: { Authorization: `Basic ${credentials}` },
  });

  if (!tokenResponse.ok) {
    throw new Error("Failed to get M-Pesa access token");
  }

  const data = await tokenResponse.json();

  // Cache token (M-Pesa tokens expire in 3600 seconds = 1 hour)
  cachedToken = {
    token: data.access_token,
    expires: now + (data.expires_in || 3600) * 1000,
  };

  return data.access_token;
}

/**
 * Save transaction to database (runs in background)
 */
async function saveToDatabase(
  supabaseUrl: string,
  supabaseServiceKey: string,
  data: {
    saleIdNo: string;
    checkoutRequestId: string;
    merchantRequestId: string;
    formattedPhone: string;
    amount: number;
    pump_shift_id?: number;
    pump_id?: number;
    attendant_id?: string;
    station_id?: number;
    fuel_type_id?: number;
    liters_sold?: number;
    price_per_liter?: number;
    fcm_token?: string;
  }
) {
  const supabase = createClient(supabaseUrl, supabaseServiceKey);

  // Create sale record
  const { data: sale, error: saleError } = await supabase
    .from("sales")
    .insert({
      sale_id_no: data.saleIdNo,
      pump_shift_id: data.pump_shift_id || 1,
      pump_id: data.pump_id || 1,
      attendant_id: data.attendant_id,
      amount: data.amount,
      total_amount: data.amount,
      customer_mobile_no: data.formattedPhone,
      transaction_status: "PENDING",
      checkout_request_id: data.checkoutRequestId,
      station_id: data.station_id || 1,
      fuel_type_id: data.fuel_type_id,
      liters_sold: data.liters_sold || 0,
      price_per_liter: data.price_per_liter || 0,
      payment_method: "mpesa",
    })
    .select()
    .single();

  if (saleError) {
    console.error("Sale creation error:", saleError);
  }

  // Create M-Pesa transaction record
  const { error: mpesaError } = await supabase
    .from("mpesa_transactions")
    .insert({
      checkout_request_id: data.checkoutRequestId,
      merchant_request_id: data.merchantRequestId,
      phone: data.formattedPhone,
      amount: data.amount,
      account_ref: sale?.sale_id || data.saleIdNo,
      station_id: data.station_id || 1,
      status: "pending",
      fcm_token: data.fcm_token,
    });

  if (mpesaError) {
    console.error("M-Pesa transaction creation error:", mpesaError);
  }

  console.log("✅ Background DB save completed");
}

/**
 * Format phone number to M-Pesa format (254XXXXXXXXX)
 */
function formatPhoneNumber(phone: string): string {
  const cleaned = phone.replace(/[^0-9]/g, "");

  if (cleaned.startsWith("254")) {
    return cleaned;
  } else if (cleaned.startsWith("0")) {
    return "254" + cleaned.slice(1);
  } else if (cleaned.startsWith("7") || cleaned.startsWith("1")) {
    return "254" + cleaned;
  }

  return cleaned;
}