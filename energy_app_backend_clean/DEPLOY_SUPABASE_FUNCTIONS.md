# 🚀 Deploy Supabase Edge Functions (Replace Render)

This guide will help you deploy M-Pesa Edge Functions to Supabase, eliminating the need for Render.

## Prerequisites

1. **Install Supabase CLI**
   ```powershell
   # Windows (using npm)
   npm install -g supabase
   
   # Or using Scoop
   scoop bucket add supabase https://github.com/supabase/scoop-bucket.git
   scoop install supabase
   ```

2. **Login to Supabase**
   ```powershell
   supabase login
   ```

## Step 1: Link to Your Project

```powershell
cd d:\Energy_App_Combined_Solution\energy_app_backend_clean

# Link to your Supabase project (get project ID from dashboard)
supabase link --project-ref pxcdaivlvltmdifxietb
```

## Step 2: Set Environment Variables

Go to your Supabase Dashboard → Settings → Edge Functions → Add these secrets:

| Secret Name | Value | Description |
|-------------|-------|-------------|
| `MPESA_CONSUMER_KEY` | Your key | From Safaricom Developer Portal |
| `MPESA_CONSUMER_SECRET` | Your secret | From Safaricom Developer Portal |
| `MPESA_SHORTCODE` | Your shortcode | e.g., 174379 for sandbox |
| `MPESA_PASSKEY` | Your passkey | From Safaricom |
| `MPESA_ENVIRONMENT` | production | or "sandbox" for testing |
| `MPESA_CALLBACK_URL` | See below | Your callback URL |

**Callback URL** (use after deploying):
```
https://pxcdaivlvltmdifxietb.supabase.co/functions/v1/callback
```

## Step 3: Deploy Functions

```powershell
cd d:\Energy_App_Combined_Solution\energy_app_backend_clean

# Deploy all functions
supabase functions deploy stkpush
supabase functions deploy callback
supabase functions deploy check-status
```

## Step 4: Update Safaricom Callback URL

In the Safaricom Developer Portal, update your callback URL to:
```
https://pxcdaivlvltmdifxietb.supabase.co/functions/v1/callback
```

## Step 5: Update Mobile App

Update `MpesaConfig.kt` to use Supabase Edge Functions:

```kotlin
// OLD (Render):
const val RENDER_BASE_URL = "https://online-link.onrender.com/"

// NEW (Supabase Edge Functions):
const val SUPABASE_FUNCTIONS_URL = "https://pxcdaivlvltmdifxietb.supabase.co/functions/v1/"
const val STK_PUSH_ENDPOINT = "${SUPABASE_FUNCTIONS_URL}stkpush"
const val CHECK_STATUS_ENDPOINT = "${SUPABASE_FUNCTIONS_URL}check-status"
```

## Function URLs

After deployment, your functions will be available at:

| Function | URL |
|----------|-----|
| STK Push | `https://pxcdaivlvltmdifxietb.supabase.co/functions/v1/stkpush` |
| Callback | `https://pxcdaivlvltmdifxietb.supabase.co/functions/v1/callback` |
| Check Status | `https://pxcdaivlvltmdifxietb.supabase.co/functions/v1/check-status` |

## Testing

Test the STK Push function:
```powershell
curl -X POST https://pxcdaivlvltmdifxietb.supabase.co/functions/v1/stkpush `
  -H "Content-Type: application/json" `
  -d '{"phone":"0720316175","amount":1,"station_id":1}'
```

## Benefits of Supabase Edge Functions

✅ **FREE** - No cold starts like Render free tier
✅ **Fast** - Edge deployment globally (~50ms response time)
✅ **Same database** - Direct access to Supabase DB
✅ **No CORS issues** - Same origin as your database
✅ **Real-time ready** - Can trigger Realtime updates directly

## Troubleshooting

### Check function logs:
```powershell
supabase functions logs stkpush
```

### Redeploy after changes:
```powershell
supabase functions deploy stkpush --no-verify-jwt
```
