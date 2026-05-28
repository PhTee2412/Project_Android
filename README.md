
This project is a comprehensive Android application designed for modern library management. It offers a seamless experience for both users and administrators. Built with **Kotlin** and **Jetpack Compose**, it follows modern Android development practices.


## Table of Contents
- [Technologies](#technologies)
- [Features](#features)
- [Project Structure](#project-structure)
- [Key Libraries & Tools](#key-libraries--tools)
- [Setup & Build](#setup--build)
- [Configuration](#configuration)
- [Testing](#testing)

## Technologies
- **Language**: Kotlin (100%)
- **UI Framework**: Jetpack Compose (Modern declarative UI)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Build System**: Gradle (Kotlin DSL)
- **Design System**: Material Design 3 (Material3)

## Features

### User Features
- **Authentication**: Secure login via Email/Password, Google Sign-In, and Facebook Login.
- **Home/Catalog**: Browse a categorized list of books with search functionality.
- **Book Details**: View detailed information about books, including descriptions and availability.
- **Borrowing Flow**: Digital borrow requests with QR code integration.
- **Borrowed Cards**: Track current and historical borrow records with detailed status updates.
- **Profile Management**: Manage user settings and personal information.
- **Real-time Chat**: Support or community chat features.

### Admin Features
- **Admin Dashboard**: Overview of library statistics.
- **Inventory Management**: Create, edit, and delete book records.
- **User Management**: Monitor and manage library members.
- **Borrow Requests**: Approve or reject pending borrow/return requests.
- **Fine Management**: Track and manage overdue fines.

## Project Structure
```text
app/src/main/java/com/example/smartlibrary/
├── data/           # Local data management & Session handling
├── network/        # API interfaces, Retrofit client, DTO models
├── ui/
│   ├── components/ # Reusable UI widgets (Headers, Cards, etc.)
│   ├── screens/    # Full-screen Composable views
│   └── viewmodel/  # Logic for UI state management
├── util/           # Helper classes and formatters (Date, Validation)
└── ...
```

## Key Libraries & Tools
- **UI**: `androidx.compose.ui`, `material3`, `material-icons-extended`
- **Networking**: `Retrofit 2` & `OkHttp 3` for REST API communication.
- **Image Loading**: `Coil` for efficient image fetching and caching.
- **Navigation**: `Jetpack Navigation Compose`
- **Utilities**:
    - `QR Code Kotlin`: For generating QR codes for borrow cards.
    - `CameraX`: For scanning book/card barcodes.
    - `ML Kit`: Barcode scanning intelligence.
- **Authentication**: `Credential Manager`, `Google Play Services Auth`, `Facebook SDK`.
- **Lifecycle**: `ViewModel`, `Runtime-KTX`

## Setup & Build
1. **Requirements**:
   - Android Studio (Ladybug or newer recommended).
   - JDK 11 or higher.
2. **Steps**:
   - Clone the repository.
   - Open the project in Android Studio and wait for Gradle sync.
   - Connect an Android device or start an emulator (API 24+).
   - Click **Run** or use the terminal:
     ```powershell
     .\gradlew.bat assembleDebug
     ```

## Configuration
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36
- **Networking**: Update the `BASE_URL` in `com.example.smartlibrary.network.RetrofitClient` to point to your backend server.

## Testing
- **Unit Tests**: Located in `app/src/test/`.
- **Instrumented Tests**: Located in `app/src/androidTest/`.
- Run all tests via terminal:
  ```powershell
  .\gradlew.bat test
  .\gradlew.bat connectedAndroidTest
  ```
