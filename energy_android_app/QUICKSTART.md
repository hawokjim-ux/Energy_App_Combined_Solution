# Quick Start Guide - Energy App Android

## 🚀 Get Started in 5 Minutes

### Step 1: Open in Android Studio
1. Launch **Android Studio**
2. Click **File** → **Open**
3. Navigate to and select the `energy_android_app` folder
4. Click **OK**

### Step 2: Wait for Gradle Sync
- Android Studio will automatically sync Gradle
- This may take 2-5 minutes on first run
- Wait for "Gradle sync finished" message

### Step 3: Run the App
1. Connect an Android device or start an emulator
2. Click the **Run** button (green triangle) or press **Shift+F10**
3. Select your device/emulator
4. Wait for the app to install and launch

### Step 4: Login and Test

#### Test as Admin
```
Username: admin
Password: admin123
```

**What you can do:**
- ✅ Open and close shifts
- ✅ View sales reports
- ✅ Filter reports by pump/attendant
- ✅ Manage users (coming soon)

#### Test as Attendant
```
Username: attendant1
Password: pass123
```

**What you can do:**
- ✅ Record fuel sales
- ✅ Initiate M-Pesa payments
- ✅ View transaction results

## 📱 Testing Workflow

### 1. Open a Shift (Admin)
1. Login as **admin**
2. Tap **Shift Management**
3. Select **Pump P1**
4. Select **Day Shift**
5. Enter opening meter reading: `1000.00`
6. Tap **Open Shift**
7. ✅ Success message appears

### 2. Record a Sale (Attendant)
1. Logout and login as **attendant1**
2. Tap **Record Sale**
3. Select **Pump P1** (should be available now)
4. Enter amount: `500`
5. Enter mobile: `0712345678`
6. Tap **Initiate M-Pesa Payment**
7. Wait 2 seconds for simulation
8. ✅ See random outcome (Success/Failed)

### 3. View Reports (Admin)
1. Logout and login as **admin**
2. Tap **Reports**
3. Tap filter icon (top right)
4. Select filters (pump, attendant, etc.)
5. ✅ See filtered sales records

## 🎯 Key Features to Test

### M-Pesa Simulation
- Try recording multiple sales
- Observe different outcomes:
  - ✅ **Success** (70% chance) - Shows receipt number
  - ❌ **Insufficient Funds** (10% chance)
  - ❌ **Cancelled** (10% chance)
  - ❌ **Other Error** (10% chance)

### Shift Management
- Try opening a shift that's already open → Error
- Open shifts on different pumps → Works
- Close a shift (coming soon)

### Reports Filtering
- Filter by pump → See only that pump's sales
- Filter by attendant → See only that attendant's sales
- Search by mobile → Find specific customer
- Clear filters → See all sales

## 🔧 Troubleshooting

### Gradle Sync Failed
```bash
File > Invalidate Caches > Invalidate and Restart
```

### App Doesn't Run
1. Check minimum SDK: Android 7.0 (API 24)
2. Update Android Studio to latest version
3. Sync Gradle again

### Database Issues
```bash
Settings > Apps > Energy App > Storage > Clear Data
```
This will reset the database with default users.

### Emulator Slow
- Use **x86** or **x86_64** system image
- Enable **Hardware Acceleration** (HAXM/KVM)
- Allocate more RAM to emulator (2GB+)

## 📚 Next Steps

1. **Explore the Code**
   - Check `MainActivity.kt` for app entry point
   - Review `NavGraph.kt` for navigation
   - Examine ViewModels for business logic

2. **Customize**
   - Change colors in `ui/theme/Color.kt`
   - Modify strings in `res/values/strings.xml`
   - Add new screens following existing patterns

3. **Backend Integration**
   - Update `RetrofitClient.kt` with your server URL
   - Modify repositories to use API instead of Room
   - Handle offline scenarios

## 💡 Tips

- **Use Logcat** to see debug logs
- **Layout Inspector** to debug UI
- **Database Inspector** to view Room data
- **Compose Preview** for quick UI iteration

## 📞 Need Help?

1. Check `README.md` for detailed documentation
2. Review `ARCHITECTURE.md` for design decisions
3. Examine code comments for implementation details

## 🎉 You're Ready!

Start exploring the app and building amazing features! 🚀
