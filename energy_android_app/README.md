# Energy App - Android (Kotlin + Jetpack Compose)

A native Android petrol station sales management system built with modern Android development practices.

## 🚀 Technology Stack

### Core Technologies
- **Kotlin** - Primary programming language
- **Jetpack Compose** - Modern declarative UI toolkit
- **Material Design 3** - Latest Material Design components
- **Android SDK** - Native Android development platform

### Key Libraries & Frameworks
- **Room Database** - Local SQLite database with type-safe queries
- **Coroutines** - Asynchronous programming (`kotlinx.coroutines`)
- **DataStore** - Modern preferences storage
- **Navigation Component** - Jetpack Compose navigation
- **Retrofit** - REST API client (for future backend integration)
- **Gson** - JSON serialization/deserialization
- **Material Icons Extended** - Comprehensive icon library

### Architecture
- **MVVM** (Model-View-ViewModel) pattern
- **Repository Pattern** for data layer abstraction
- **Single Activity Architecture** with Compose Navigation
- **Unidirectional Data Flow** with StateFlow

## ✨ Features

### User Management
- Two user roles: **Admin** and **Pump Attendant**
- Secure password hashing with SHA-256
- Session management with DataStore
- Role-based access control

### Pump & Shift Management (Admin Only)
- Open and close shifts for each pump
- Track opening and closing meter readings
- Prevent concurrent shifts on same pump
- Real-time shift status tracking

### Sales/POS Module (Attendant)
- Record fuel sales with automatic ID generation
- M-Pesa STK Push simulation
- Transaction status tracking (Success/Failed)
- Customer mobile number validation
- Receipt generation with M-Pesa codes

### M-Pesa Integration (Simulated)
- Realistic STK Push simulation
- Random outcomes with weighted probabilities:
  - **70%** Success
  - **10%** Insufficient Funds
  - **10%** Cancelled by Customer
  - **10%** Other Error
- Transaction logging and receipt generation

### Reporting (Admin)
- Comprehensive sales reports
- Filter by pump, attendant, shift, or mobile number
- Real-time total sales calculation
- Transaction status indicators
- Detailed transaction history

## 📱 Screenshots & UI

### Material Design 3 Features
- Dynamic color theming
- Elevated surfaces with proper shadows
- Rounded corners (12-16dp radius)
- Consistent spacing (8dp grid system)
- Modern typography scale
- Status-based color coding

### Screen Layouts
- **Login**: Centered card with logo and credentials
- **Admin Dashboard**: Grid of action cards
- **Attendant Dashboard**: Quick access to sales recording
- **Shift Management**: Pump selection with status indicators
- **Sales Screen**: Form with M-Pesa integration
- **Reports**: Filterable list with total calculations

## 🏗️ Project Structure

```
com.energyapp/
├── data/
│   ├── local/
│   │   ├── dao/           # Room DAOs for database access
│   │   ├── entity/        # Room entities (tables)
│   │   └── EnergyDatabase.kt
│   ├── remote/
│   │   ├── api/           # Retrofit API service
│   │   └── dto/           # Data transfer objects
│   ├── repository/        # Repository pattern implementation
│   └── model/             # Domain models
├── ui/
│   ├── theme/             # Material3 theme, colors, typography
│   ├── components/        # Reusable Compose components
│   ├── screens/           # Screen composables and ViewModels
│   │   ├── login/
│   │   ├── admin/
│   │   └── attendant/
│   └── navigation/        # Navigation graph
├── util/                  # Utilities and helpers
└── MainActivity.kt        # Single activity entry point
```

## 🔧 Setup Instructions

### Prerequisites
- **Android Studio** Hedgehog (2023.1.1) or later
- **Kotlin** 1.9.20 or later
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **JDK**: 17

### Installation Steps

1. **Open in Android Studio**
   ```bash
   File > Open > Select energy_android_app folder
   ```

2. **Sync Gradle**
   - Android Studio will automatically sync Gradle files
   - Wait for dependencies to download

3. **Build the Project**
   ```bash
   Build > Make Project (Ctrl+F9)
   ```

4. **Run on Emulator or Device**
   ```bash
   Run > Run 'app' (Shift+F10)
   ```

### Default Credentials

| Username    | Password  | Role            |
|-------------|-----------|-----------------|
| `admin`     | `admin123`| Admin           |
| `attendant1`| `pass123` | Pump Attendant  |

## 📊 Database Schema

### Tables
- **user_roles** - User role definitions (Admin, Pump Attendant)
- **users** - User accounts with hashed passwords
- **pumps** - Fuel pump information (P1, P2, P3)
- **shifts** - Shift types (Day Shift, Night Shift)
- **pump_shifts** - Shift opening/closing records
- **sales_records** - Sales transactions
- **mpesa_transactions** - M-Pesa transaction logs
- **settings** - App configuration

### Relationships
- User belongs to UserRole (Many-to-One)
- PumpShift belongs to Pump and Shift (Many-to-One)
- SalesRecord belongs to PumpShift, Pump, and User (Many-to-One)
- MpesaTransaction belongs to SalesRecord (One-to-One)

## 🔌 Backend Integration (Optional)

The app currently uses **Room** for local data storage. To integrate with the Flask backend:

1. **Update Retrofit Base URL**
   ```kotlin
   // In RetrofitClient.kt
   private const val BASE_URL = "http://YOUR_SERVER_IP:5000/"
   ```

2. **For Android Emulator accessing localhost**
   ```kotlin
   private const val BASE_URL = "http://10.0.2.2:5000/"
   ```

3. **For Physical Device**
   ```kotlin
   private const val BASE_URL = "http://192.168.1.XXX:5000/"
   ```

4. **Modify Repositories**
   - Replace Room DAO calls with Retrofit API calls
   - Handle network errors and offline scenarios
   - Implement sync mechanism for offline data

## 🧪 Testing

### Manual Testing Workflow

1. **Login as Admin**
   - Username: `admin`, Password: `admin123`
   - Verify navigation to Admin Dashboard

2. **Open a Shift**
   - Navigate to Shift Management
   - Select Pump P1, Day Shift
   - Enter opening meter reading: 1000.00
   - Click "Open Shift"

3. **Login as Attendant**
   - Logout and login with: `attendant1` / `pass123`
   - Navigate to Record Sale

4. **Record a Sale**
   - Amount: 500
   - Mobile: 0712345678
   - Click "Initiate M-Pesa Payment"
   - Observe random transaction outcome

5. **View Reports (Admin)**
   - Login as admin
   - Navigate to Reports
   - Apply filters and view sales

### Unit Testing (TODO)
- ViewModel unit tests with JUnit
- Repository tests with Room in-memory database
- UI tests with Compose Testing

## 🎨 Customization

### Change Theme Colors
Edit `ui/theme/Color.kt`:
```kotlin
val Primary = Color(0xFF1B5E20) // Your primary color
val Secondary = Color(0xFF0277BD) // Your secondary color
```

### Add New Screens
1. Create screen composable in `ui/screens/`
2. Create ViewModel for state management
3. Add route in `ui/navigation/Screen.kt`
4. Add composable in `ui/navigation/NavGraph.kt`

### Modify Database Schema
1. Update entities in `data/local/entity/`
2. Update DAOs in `data/local/dao/`
3. Increment database version in `EnergyDatabase.kt`
4. Add migration strategy

## 🚧 Future Enhancements

- [ ] Real M-Pesa Daraja API integration
- [ ] User management CRUD screens
- [ ] Biometric authentication
- [ ] Push notifications for transactions
- [ ] Offline sync with backend
- [ ] Export reports to PDF/Excel
- [ ] Multi-language support (i18n)
- [ ] Dark mode toggle
- [ ] Analytics dashboard with charts
- [ ] Backup and restore functionality

## 📝 Code Quality

### Best Practices Implemented
- ✅ MVVM architecture with clear separation of concerns
- ✅ Repository pattern for data abstraction
- ✅ Coroutines for asynchronous operations
- ✅ StateFlow for reactive UI updates
- ✅ Proper error handling with Result wrapper
- ✅ Input validation on all forms
- ✅ Password hashing for security
- ✅ Material Design 3 guidelines
- ✅ Single Activity Architecture
- ✅ Compose best practices (remember, derivedStateOf)

## 🐛 Known Issues

- User Management screen not yet implemented (placeholder)
- No data export functionality
- No offline sync mechanism
- M-Pesa simulation is random (not configurable)

## 📄 License

This project is created for educational and demonstration purposes.

## 👨‍💻 Development

### Build Variants
- **debug** - Development build with logging
- **release** - Production build with ProGuard

### Gradle Tasks
```bash
./gradlew clean          # Clean build
./gradlew assembleDebug  # Build debug APK
./gradlew assembleRelease # Build release APK
./gradlew test           # Run unit tests
```

## 📞 Support

For issues or questions:
1. Check the code comments and documentation
2. Review the architecture diagram in ARCHITECTURE.md
3. Examine the Flask backend API endpoints for integration

## 🎯 Key Differences from Flask Version

| Feature | Flask (Python) | Android (Kotlin) |
|---------|---------------|------------------|
| **Database** | SQLite/MySQL | Room (SQLite) |
| **UI** | HTML/CSS/JS | Jetpack Compose |
| **Language** | Python | Kotlin |
| **Architecture** | MVC | MVVM |
| **API** | REST endpoints | Local + Optional REST |
| **State Management** | Session/Cookies | DataStore + StateFlow |
| **Async** | Flask async | Coroutines |
| **Validation** | Server-side | Client-side + Server-side |

## 🌟 Highlights

- **100% Kotlin** - Modern, type-safe language
- **100% Jetpack Compose** - No XML layouts
- **Material Design 3** - Latest design system
- **Offline-First** - Works without internet
- **Type-Safe Navigation** - Compile-time route checking
- **Reactive UI** - StateFlow-based updates
- **Clean Architecture** - Testable and maintainable

---

**Built with ❤️ using Kotlin and Jetpack Compose**
