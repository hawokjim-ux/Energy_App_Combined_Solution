// Supabase Edge Function: Check Transaction Status
// Deploy to: supabase/functions/check-status/index.ts

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

    const { data: transaction, error } = await supabase
      .from("mpesa_transactions")
      .select("*")
      .eq("checkout_request_id", checkoutRequestId)
      .single();

    if (error) {
      return new Response(
        JSON.stringify({
          success: true,
          ResultCode: null,
          ResultDesc: "Transaction pending",
        }),
        { headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    let resultCode = null;
    let resultDesc = transaction.result_desc || "Transaction pending";

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
        resultDesc: resultDesc,
        checkoutRequestID: transaction.checkout_request_id,
        amount: transaction.amount,
        mpesaReceiptNumber: transaction.mpesa_receipt,
      }),
      { headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  } catch (error) {
    return new Response(
      JSON.stringify({
        success: false,
        message: (error as Error).message
      }),
      { headers: { ...corsHeaders, "Content-Type": "application/json" }, status: 400 }
    );
  }
});