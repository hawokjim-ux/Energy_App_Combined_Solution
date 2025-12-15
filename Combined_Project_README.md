# Energy App - Combined Project Solution

This archive contains the complete source code for the Energy App, including the original Flask/Python backend and the newly developed native Android application.

## 📁 Project Contents

1.  **energy_android_app/**: The complete source code for the native Android application built with **Kotlin** and **Jetpack Compose**.
2.  **energy_app_backend_clean/**: The source code for the original **Flask/Python REST API** backend.
3.  **energy_app_db_setup.sql**: The original MySQL database schema script.

## 🚀 Getting Started

### Android Application (Kotlin/Compose)
- **Purpose**: A modern, offline-first mobile application.
- **Location**: `energy_android_app/`
- **Instructions**: See `energy_android_app/QUICKSTART.md` for setup.

### Flask Backend (Python)
- **Purpose**: The original REST API for server-side operations.
- **Location**: `energy_app_backend_clean/`
- **Instructions**:
    1.  `cd energy_app_backend_clean`
    2.  `python3 -m venv venv`
    3.  `source venv/bin/activate`
    4.  `pip install -r requirements.txt`
    5.  `python run.py` (Runs on http://127.0.0.1:5000)

## 💡 Note on Backend Files

The large `venv` folder (containing Python dependencies) has been excluded to keep the file size small. You must recreate the virtual environment and install dependencies using `requirements.txt` as described above.

## 🔑 Default Credentials

| Username | Password | Role |
| :--- | :--- | :--- |
| `admin` | `admin123` | Admin |
| `attendant1` | `pass123` | Pump Attendant |
