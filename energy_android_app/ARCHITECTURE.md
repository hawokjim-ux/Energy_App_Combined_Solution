# Energy App - Android Architecture Design

## Technology Stack

### Core Technologies
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Design System**: Material Design 3 (Material3)
- **Build System**: Gradle with Kotlin DSL

### Key Libraries
- **Retrofit**: REST API client for network communication
- **Gson**: JSON serialization/deserialization
- **Coroutines**: Asynchronous programming (kotlinx.coroutines)
- **Navigation Component**: Screen navigation (Compose Navigation)
- **Room**: Local database (SQLite wrapper)
- **DataStore**: Preferences storage
- **Coil**: Image loading for Compose

### Architecture Pattern
- **MVVM** (Model-View-ViewModel)
- **Repository Pattern** for data layer abstraction
- **Single Activity Architecture** with Compose Navigation

## Project Structure

```
com.energyapp/
├── data/
│   ├── local/
│   │   ├── dao/
│   │   │   ├── UserDao.kt
│   │   │   ├── PumpDao.kt
│   │   │   ├── ShiftDao.kt
│   │   │   └── SalesDao.kt
│   │   ├── entity/
│   │   │   ├── UserEntity.kt
│   │   │   ├── PumpEntity.kt
│   │   │   ├── ShiftEntity.kt
│   │   │   ├── PumpShiftEntity.kt
│   │   │   ├── SalesEntity.kt
│   │   │   └── MpesaTransactionEntity.kt
│   │   └── EnergyDatabase.kt
│   ├── remote/
│   │   ├── api/
│   │   │   └── ApiService.kt (for future backend integration)
│   │   └── dto/
│   │       ├── LoginRequest.kt
│   │       ├── LoginResponse.kt
│   │       └── ApiResponse.kt
│   ├── repository/
│   │   ├── UserRepository.kt
│   │   ├── PumpRepository.kt
│   │   ├── ShiftRepository.kt
│   │   └── SalesRepository.kt
│   └── model/
│       ├── User.kt
│       ├── Pump.kt
│       ├── Shift.kt
│       ├── PumpShift.kt
│       ├── SalesRecord.kt
│       └── MpesaTransaction.kt
├── ui/
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   ├── components/
│   │   ├── EnergyButton.kt
│   │   ├── EnergyTextField.kt
│   │   ├── LoadingDialog.kt
│   │   └── ErrorDialog.kt
│   ├── screens/
│   │   ├── login/
│   │   │   ├── LoginScreen.kt
│   │   │   └── LoginViewModel.kt
│   │   ├── admin/
│   │   │   ├── AdminDashboardScreen.kt
│   │   │   ├── AdminDashboardViewModel.kt
│   │   │   ├── UserManagementScreen.kt
│   │   │   ├── UserManagementViewModel.kt
│   │   │   ├── ShiftManagementScreen.kt
│   │   │   ├── ShiftManagementViewModel.kt
│   │   │   ├── ReportsScreen.kt
│   │   │   └── ReportsViewModel.kt
│   │   └── attendant/
│   │       ├── AttendantDashboardScreen.kt
│   │       ├── AttendantDashboardViewModel.kt
│   │       ├── SalesScreen.kt
│   │       └── SalesViewModel.kt
│   └── navigation/
│       ├── NavGraph.kt
│       └── Screen.kt
├── util/
│   ├── Constants.kt
│   ├── DateUtils.kt
│   ├── PreferencesManager.kt
│   └── MpesaSimulator.kt
└── MainActivity.kt
```

## Data Flow

### Authentication Flow
1. User enters credentials in LoginScreen
2. LoginViewModel validates and calls UserRepository
3. UserRepository queries local Room database
4. On success, user session saved to DataStore
5. Navigate to appropriate dashboard based on role

### Sales Recording Flow
1. Attendant enters sale details in SalesScreen
2. SalesViewModel initiates M-Pesa STK Push simulation
3. MpesaSimulator generates transaction result
4. SalesRepository saves transaction to Room database
5. UI updates with transaction status

### Shift Management Flow
1. Admin opens/closes shift in ShiftManagementScreen
2. ShiftManagementViewModel calls ShiftRepository
3. ShiftRepository updates PumpShift entity in Room
4. UI reflects current shift status

### Reporting Flow
1. Admin selects filters in ReportsScreen
2. ReportsViewModel queries SalesRepository with filters
3. SalesRepository performs complex query with joins
4. Results displayed in LazyColumn with filtering

## Database Schema (Room)

### Tables
- **users**: User accounts and authentication
- **user_roles**: Admin and Pump Attendant roles
- **pumps**: Fuel pump information
- **shifts**: Shift types (Day/Night)
- **pump_shifts**: Shift opening/closing records
- **sales_records**: Sales transactions
- **mpesa_transactions**: M-Pesa transaction logs
- **settings**: App configuration

### Relationships
- User -> UserRole (Many-to-One)
- PumpShift -> Pump (Many-to-One)
- PumpShift -> Shift (Many-to-One)
- PumpShift -> User (Many-to-One for opening/closing attendant)
- SalesRecord -> PumpShift (Many-to-One)
- SalesRecord -> Pump (Many-to-One)
- SalesRecord -> User (Many-to-One)
- MpesaTransaction -> SalesRecord (One-to-One)

## Key Features Implementation

### 1. User Authentication
- Local authentication using Room database
- Password hashing with Android Security library
- Session management with DataStore Preferences
- Role-based access control

### 2. Pump & Shift Management
- Admin-only shift opening/closing
- Real-time shift status tracking
- Meter reading validation
- Concurrent shift prevention

### 3. Sales/POS Module
- Sale ID generation
- Amount validation
- M-Pesa STK Push simulation
- Transaction status tracking
- Receipt generation

### 4. M-Pesa Simulation
- Random outcome generation (Success, Insufficient Funds, Cancelled, Error)
- Realistic delay simulation
- Transaction logging
- Receipt number generation

### 5. Reporting
- Sales filtering by pump, attendant, shift, mobile number
- Date range filtering
- Total calculations
- Export functionality (future)

## UI/UX Design Principles

### Material Design 3
- Dynamic color theming
- Elevated surfaces
- Rounded corners
- Consistent spacing (8dp grid)

### Screen Layouts
- **Login**: Centered card with logo, fields, and button
- **Dashboard**: Grid of action cards with icons
- **Sales**: Form with amount input and M-Pesa button
- **Reports**: Filter bar + LazyColumn list
- **Shift Management**: Pump list with status indicators

### Navigation
- Single Activity with Compose Navigation
- Bottom navigation for main sections (Admin only)
- Top app bar with back navigation
- Drawer navigation for settings (future)

## State Management

### ViewModel State
- UI state as StateFlow
- Loading, Success, Error states
- Form validation state
- Filter state

### Repository State
- Flow-based data streams
- Cached data with Room
- Error handling with Result wrapper

## Security Considerations

1. **Password Storage**: Hashed passwords in Room database
2. **Session Management**: Secure DataStore for user session
3. **Input Validation**: Client-side validation for all inputs
4. **SQL Injection Prevention**: Room parameterized queries
5. **No Hardcoded Secrets**: Configuration in BuildConfig

## Future Enhancements

1. **Backend Integration**: Connect to Flask API via Retrofit
2. **Real M-Pesa Integration**: Daraja API implementation
3. **Offline Support**: Sync queue for offline transactions
4. **Biometric Authentication**: Fingerprint/Face unlock
5. **Push Notifications**: Transaction alerts
6. **Analytics**: Firebase Analytics integration
7. **Crash Reporting**: Firebase Crashlytics
8. **Multi-language Support**: Localization resources
