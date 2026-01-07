// Supabase Edge Function: Check Transaction Status
// Deploy to: supabase/functions/check-status/index.ts
// This function checks the database first, then queries M-Pesa directly as fallback

import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const url = new URL(req.url);
    const checkoutRequestId = url.searchParams.get("checkout_request_id");

    if (!checkoutRequestId) {
      throw new Error("checkout_request_id is required");
    }

    const supabaseUrl = Deno.env.get("SUPABASE_URL")!;
    const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;
    const supabase = createClient(supabaseUrl, supabaseServiceKey);

    // First, check our database
    const { data: transaction, error } = await supabase
      .from("mpesa_transactions")
      .select("*")
      .eq("checkout_request_id", checkoutRequestId)
      .single();

    // If found and already completed/cancelled/failed, return immediately
    if (transaction && transaction.status !== "pending") {
      let resultCode = null;
      switch (transaction.status) {
        case "completed":
          resultCode = 0;
          break;
        case "cancelled":
          resultCode = 1032;
          break;
        case "failed":
          resultCode = 1;
          break;
      }

      return new Response(
        JSON.stringify({
          success: true,
          resultCode: resultCode,
          resultDesc: transaction.result_desc || "Transaction " + transaction.status,
          checkoutRequestID: transaction.checkout_request_id,
          amount: transaction.amount,
          mpesaReceiptNumber: transaction.mpesa_receipt,
        }),
        { headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    // If pending or not found, query M-Pesa directly (STK Query)
    console.log("⏳ Status pending, querying M-Pesa directly...");

    const consumerKey = Deno.env.get("MPESA_CONSUMER_KEY");
    const consumerSecret = Deno.env.get("MPESA_CONSUMER_SECRET");
    const shortcode = Deno.env.get("MPESA_SHORTCODE");
    const passkey = Deno.env.get("MPESA_PASSKEY");
    const environment = Deno.env.get("MPESA_ENVIRONMENT") || "production";

    if (!consumerKey || !consumerSecret || !shortcode || !passkey) {
      // Return pending if we can't query M-Pesa
      return new Response(
        JSON.stringify({
          success: true,
          resultCode: null,
          resultDesc: "Transaction pending",
        }),
        { headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    const baseUrl = environment === "production"
      ? "https://api.safaricom.co.ke"
      : "https://sandbox.safaricom.co.ke";

    // Get access token
    const credentials = btoa(`${consumerKey}:${consumerSecret}`);
    const tokenResponse = await fetch(`${baseUrl}/oauth/v1/generate?grant_type=client_credentials`, {
      headers: { Authorization: `Basic ${credentials}` },
    });

    if (!tokenResponse.ok) {
      console.error("Failed to get access token for STK Query");
      return new Response(
        JSON.stringify({
          success: true,
          resultCode: null,
          resultDesc: "Transaction pending",
        }),
        { headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    const { access_token } = await tokenResponse.json();

    // Generate timestamp and password
    const timestamp = new Date().toISOString().replace(/[-:TZ.]/g, "").slice(0, 14);
    const password = btoa(`${shortcode}${passkey}${timestamp}`);

    // Query STK Push status
    const queryResponse = await fetch(`${baseUrl}/mpesa/stkpushquery/v1/query`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${access_token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        BusinessShortCode: shortcode,
        Password: password,
        Timestamp: timestamp,
        CheckoutRequestID: checkoutRequestId,
      }),
    });

    const queryResult = await queryResponse.json();
    console.log("M-Pesa STK Query result:", JSON.stringify(queryResult));

    // Parse the result
    let resultCode = null;
    let resultDesc = "Transaction pending";
    let mpesaReceiptNumber = null;

    if (queryResult.ResultCode !== undefined) {
      resultCode = parseInt(queryResult.ResultCode);
      resultDesc = queryResult.ResultDesc || "";

      // Update our database with the result
      let status = "pending";
      if (resultCode === 0) {
        status = "completed";
        // Try to extract receipt from result description
        const receiptMatch = resultDesc.match(/([A-Z0-9]{10})/);
        if (receiptMatch) {
          mpesaReceiptNumber = receiptMatch[1];
        }
      } else if (resultCode === 1032) {
        status = "cancelled";
      } else if (resultCode !== null) {
        status = "failed";
      }

      if (status !== "pending" && transaction) {
        // Update transaction in database
        await supabase
          .from("mpesa_transactions")
          .update({
            status,
            result_desc: resultDesc,
            mpesa_receipt: mpesaReceiptNumber,
            completed_at: resultCode === 0 ? new Date().toISOString() : null,
            updated_at: new Date().toISOString(),
          })
          .eq("checkout_request_id", checkoutRequestId);

        // Also update sales table
        await supabase
          .from("sales")
          .update({
            transaction_status: resultCode === 0 ? "M-PESA" : "FAILED",
            mpesa_receipt_number: mpesaReceiptNumber,
            updated_at: new Date().toISOString(),
          })
          .eq("checkout_request_id", checkoutRequestId);

        console.log(`✅ Updated database: status=${status}, receipt=${mpesaReceiptNumber}`);
      }
    }

    return new Response(
      JSON.stringify({
        success: true,
        resultCode: resultCode,
        resultDesc: resultDesc,
        checkoutRequestID: checkoutRequestId,
        amount: transaction?.amount,
        mpesaReceiptNumber: mpesaReceiptNumber,
      }),
      { headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  } catch (error) {
    console.error("Check status error:", error);
    return new Response(
      JSON.stringify({
        success: false,
        message: (error as Error).message
      }),
      { headers: { ...corsHeaders, "Content-Type": "application/json" }, status: 400 }
    );
  }
});