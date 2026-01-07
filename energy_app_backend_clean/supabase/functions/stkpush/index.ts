// Supabase Edge Function: M-Pesa STK Push - FIXED VERSION
// Deploy with: supabase functions deploy stkpush

import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

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
    const tillNumber = Deno.env.get("MPESA_TILL_NUMBER") || shortcode;

    // M-Pesa API URLs
    const baseUrl = environment === "production"
      ? "https://api.safaricom.co.ke"
      : "https://sandbox.safaricom.co.ke";

    // Get request body - accept all fields from Android app
    const {
      amount,
      phone,
      account,
      description,
      user_id,
      pump_id,
      shift_id,
      station_id,
      fuel_type,
      quantity,  // Liters from Android app
    } = await req.json();

    // Validate inputs
    if (!amount || !phone) {
      throw new Error("Amount and phone are required");
    }

    // Format phone number
    const formattedPhone = formatPhoneNumber(phone);
    if (!formattedPhone.match(/^254(7\d{8}|1[01]\d{7})$/)) {
      throw new Error("Invalid phone number format");
    }

    console.log(`⚡ STK Push - Phone: ${formattedPhone}, Amount: ${amount}, Liters: ${quantity || 0}`);

    // Step 1: Get M-Pesa access token
    const credentials = btoa(`${consumerKey}:${consumerSecret}`);
    const tokenResponse = await fetch(`${baseUrl}/oauth/v1/generate?grant_type=client_credentials`, {
      headers: { Authorization: `Basic ${credentials}` },
    });

    if (!tokenResponse.ok) {
      throw new Error("Failed to get M-Pesa access token");
    }

    const { access_token } = await tokenResponse.json();
    const tokenTime = Date.now() - startTime;
    console.log(`🔑 Token obtained in ${tokenTime}ms`);

    // Step 2: Generate timestamp and password
    const timestamp = new Date().toISOString().replace(/[-:TZ.]/g, "").slice(0, 14);
    const password = btoa(`${shortcode}${passkey}${timestamp}`);

    // Step 3: Send STK Push
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

    // Step 4: Initialize Supabase and create records
    const supabaseUrl = Deno.env.get("SUPABASE_URL")!;
    const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;
    const supabase = createClient(supabaseUrl, supabaseServiceKey);

    // Get next receipt number (sequential)
    const { count } = await supabase
      .from("sales")
      .select("*", { count: "exact", head: true });

    const nextNum = (count || 0) + 1;
    const saleIdNo = `RCP-${String(nextNum).padStart(5, '0')}`;

    // Calculate price per liter
    const liters = quantity || (amount > 0 ? amount / 180.50 : 0);  // Default fuel price
    const pricePerLiter = liters > 0 ? amount / liters : 180.50;

    // Step 5: Create sale record with CORRECT fields
    const { data: sale, error: saleError } = await supabase
      .from("sales")
      .insert({
        sale_id_no: saleIdNo,
        pump_shift_id: parseInt(shift_id) || 1,
        pump_id: parseInt(pump_id) || 1,
        attendant_id: user_id,
        amount: amount,
        total_amount: amount,
        customer_mobile_no: formattedPhone,
        transaction_status: "PENDING",  // Will be updated by callback
        checkout_request_id: stkResult.CheckoutRequestID,
        station_id: parseInt(station_id) || 1,
        fuel_type_id: fuel_type ? parseInt(fuel_type) : null,
        liters_sold: liters,
        price_per_liter: pricePerLiter,
        payment_method: "mpesa",
      })
      .select()
      .single();

    if (saleError) {
      console.error("Sale creation error:", saleError);
    } else {
      console.log(`✅ Sale created: ${saleIdNo}, Liters: ${liters}`);
    }

    // Step 6: Create M-Pesa transaction record
    const { error: mpesaError } = await supabase
      .from("mpesa_transactions")
      .insert({
        checkout_request_id: stkResult.CheckoutRequestID,
        merchant_request_id: stkResult.MerchantRequestID,
        phone: formattedPhone,
        amount: amount,
        account_ref: sale?.sale_id || saleIdNo,
        station_id: parseInt(station_id) || 1,
        status: "pending",
      });

    if (mpesaError) {
      console.error("M-Pesa transaction creation error:", mpesaError);
    }

    const processingTime = Date.now() - startTime;
    console.log(`✅ STK Push complete in ${processingTime}ms`);

    // Return success response
    return new Response(
      JSON.stringify({
        success: true,
        message: "STK Push sent! Check your phone.",
        sale_id: sale?.sale_id || saleIdNo,
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
    console.error(`❌ STK Push error after ${processingTime}ms:`, error);

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