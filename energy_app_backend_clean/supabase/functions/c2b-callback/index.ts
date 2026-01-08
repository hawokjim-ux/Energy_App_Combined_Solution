// Supabase Edge Function: M-Pesa C2B Callback
// Receives payment confirmations when customers pay to your Till
// Deploy with: supabase functions deploy c2b-callback

import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

serve(async (req) => {
    try {
        // Get C2B callback data from Safaricom
        const callbackData = await req.json();

        console.log("📥 C2B Callback Received:", JSON.stringify(callbackData));

        // Extract C2B transaction data
        // Safaricom C2B format:
        // {
        //   TransactionType: "Pay Bill" | "Buy Goods",
        //   TransID: "SFJ7XXXXXX",
        //   TransTime: "20260108123456",
        //   TransAmount: "1000.00",
        //   BusinessShortCode: "123456",
        //   BillRefNumber: "AccountRef",
        //   InvoiceNumber: "",
        //   OrgAccountBalance: "5000.00",
        //   ThirdPartyTransID: "",
        //   MSISDN: "254712345678",
        //   FirstName: "John",
        //   MiddleName: "",
        //   LastName: "Doe"
        // }

        const transactionId = callbackData.TransID;
        const amount = parseFloat(callbackData.TransAmount);
        const phone = callbackData.MSISDN;
        const accountRef = callbackData.BillRefNumber || "";
        const firstName = callbackData.FirstName || "";
        const lastName = callbackData.LastName || "";
        const customerName = `${firstName} ${lastName}`.trim();
        const transTime = callbackData.TransTime;

        if (!transactionId || !amount) {
            throw new Error("Missing required fields: TransID or TransAmount");
        }

        // Parse transaction time (format: YYYYMMDDHHmmss)
        let transactionTime = new Date();
        if (transTime && transTime.length >= 14) {
            const year = transTime.substring(0, 4);
            const month = transTime.substring(4, 6);
            const day = transTime.substring(6, 8);
            const hour = transTime.substring(8, 10);
            const min = transTime.substring(10, 12);
            const sec = transTime.substring(12, 14);
            transactionTime = new Date(`${year}-${month}-${day}T${hour}:${min}:${sec}+03:00`);
        }

        // Initialize Supabase client
        const supabaseUrl = Deno.env.get("SUPABASE_URL")!;
        const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;
        const supabase = createClient(supabaseUrl, supabaseServiceKey);

        // Try to determine station from account reference or shortcode
        let stationId = null;
        if (accountRef) {
            // Try to extract station ID from account reference (e.g., "STN1", "PUMP3", etc.)
            const stationMatch = accountRef.match(/STN(\d+)|S(\d+)/i);
            if (stationMatch) {
                stationId = parseInt(stationMatch[1] || stationMatch[2]);
            }
        }

        // Insert C2B transaction record
        const { data: transaction, error: insertError } = await supabase
            .from("c2b_transactions")
            .insert({
                mpesa_receipt: transactionId,
                phone: phone,
                amount: amount,
                transaction_time: transactionTime.toISOString(),
                account_reference: accountRef,
                customer_name: customerName,
                is_linked: false,
                station_id: stationId,
            })
            .select()
            .single();

        if (insertError) {
            // Check if it's a duplicate (already received)
            if (insertError.code === "23505") {
                console.log(`⚠️ Duplicate transaction ignored: ${transactionId}`);
            } else {
                console.error("❌ Failed to insert C2B transaction:", insertError);
                throw insertError;
            }
        } else {
            console.log(`✅ C2B Transaction saved: ${transactionId}, Amount: ${amount}, Phone: ${phone}`);
        }

        // Respond to Safaricom with success
        return new Response(
            JSON.stringify({
                ResultCode: 0,
                ResultDesc: "Accepted",
            }),
            {
                headers: { "Content-Type": "application/json" },
                status: 200,
            }
        );
    } catch (error) {
        console.error("❌ C2B Callback error:", error);

        // Still return success to Safaricom to prevent retries
        return new Response(
            JSON.stringify({
                ResultCode: 0,
                ResultDesc: "Accepted",
            }),
            {
                headers: { "Content-Type": "application/json" },
                status: 200,
            }
        );
    }
});
