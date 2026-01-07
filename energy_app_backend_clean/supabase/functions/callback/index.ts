// Supabase Edge Function: M-Pesa Callback
// Deploy to: supabase/functions/callback/index.ts

import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

serve(async (req) => {
  try {
    // Get callback data from Safaricom
    const callbackData = await req.json();

    console.log("M-Pesa Callback Received:", JSON.stringify(callbackData));

    // Extract callback data
    const stkCallback = callbackData?.Body?.stkCallback;

    if (!stkCallback) {
      throw new Error("Invalid callback data");
    }

    const checkoutRequestID = stkCallback.CheckoutRequestID;
    const merchantRequestID = stkCallback.MerchantRequestID;
    const resultCode = stkCallback.ResultCode;
    const resultDesc = stkCallback.ResultDesc;

    if (!checkoutRequestID) {
      throw new Error("Missing CheckoutRequestID");
    }

    // Extract callback metadata (for successful transactions)
    let mpesaReceipt = null;
    let transactionDate = null;
    let phoneNumber = null;
    let amount = null;

    if (resultCode === 0) {
      const items = stkCallback.CallbackMetadata?.Item || [];

      for (const item of items) {
        switch (item.Name) {
          case "MpesaReceiptNumber":
            mpesaReceipt = item.Value;
            break;
          case "TransactionDate":
            transactionDate = item.Value;
            break;
          case "PhoneNumber":
            phoneNumber = item.Value;
            break;
          case "Amount":
            amount = item.Value;
            break;
        }
      }
    }

    // Determine status
    let status = "pending";
    switch (resultCode) {
      case 0:
        status = "completed";
        break;
      case 1032:
        status = "cancelled";
        break;
      default:
        status = "failed";
        break;
    }

    // Initialize Supabase client with service role key
    const supabaseUrl = Deno.env.get("SUPABASE_URL")!;
    const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;
    const supabase = createClient(supabaseUrl, supabaseServiceKey);

    // Update mpesa_transactions table
    const { data: transaction, error: updateError } = await supabase
      .from("mpesa_transactions")
      .update({
        status,
        mpesa_receipt: mpesaReceipt,
        result_desc: resultDesc,
        completed_at: resultCode === 0 ? new Date().toISOString() : null,
        updated_at: new Date().toISOString(),
      })
      .eq("checkout_request_id", checkoutRequestID)
      .select("account_ref, checkout_request_id")
      .single();

    if (updateError) {
      console.error("Failed to update mpesa_transactions:", updateError);
    } else {
      console.log(`✅ Updated mpesa_transactions: status=${status}`);
    }

    // Update sales table - use correct field names from schema
    // transaction_status and mpesa_receipt_number, match by checkout_request_id
    const transactionStatus = resultCode === 0 ? "M-PESA" : (resultCode === 1032 ? "CANCELLED" : "FAILED");

    const { error: saleError } = await supabase
      .from("sales")
      .update({
        transaction_status: transactionStatus,
        mpesa_receipt_number: mpesaReceipt,
        updated_at: new Date().toISOString(),
      })
      .eq("checkout_request_id", checkoutRequestID);

    if (saleError) {
      console.error("Failed to update sales:", saleError);
    } else {
      console.log(`✅ Updated sales: transaction_status=${transactionStatus}, receipt=${mpesaReceipt}`);
    }

    console.log(`✅ Callback processed successfully for: ${checkoutRequestID}`);
    console.log(`Status: ${status}, Receipt: ${mpesaReceipt}`);

    // Respond to Safaricom
    return new Response(
      JSON.stringify({
        ResultCode: 0,
        ResultDesc: "Success",
      }),
      {
        headers: { "Content-Type": "application/json" },
        status: 200,
      }
    );
  } catch (error) {
    console.error("❌ Callback processing error:", error);

    return new Response(
      JSON.stringify({
        ResultCode: 1,
        ResultDesc: (error as Error).message || "Callback processing failed",
      }),
      {
        headers: { "Content-Type": "application/json" },
        status: 200,
      }
    );
  }
});