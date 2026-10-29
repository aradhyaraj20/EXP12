# Experiment 12: Android Web Services HTTP Request Application

An Android application that accesses RESTful web services via HTTP requests (`GET` and `POST`) using Kotlin Coroutines and standard `HttpURLConnection`.

---

## 📌 Project Overview

This application demonstrates how to interact with remote REST APIs over the network in an Android app without external heavy networking libraries. It executes non-blocking asynchronous HTTP network operations on a background thread (`Dispatchers.IO`), parses JSON responses, extracts HTTP status codes and headers, and updates the user interface safely on the main thread (`Dispatchers.Main`).

---

## ✨ Features

- **HTTP GET Requests:** Retrieve data for a specific record ID (e.g. Post ID `1`, `2`, `3`...) from a remote web service.
- **HTTP POST Requests:** Send structured JSON payloads (`title`, `body`, `userId`) to a remote endpoint.
- **Dynamic ID Querying:** User-definable ID input field (`etId`) allowing queries for any specific endpoint ID.
- **JSON Response Parsing & Formatting:** Formats raw JSON strings into human-readable key-value pairs (`JSONObject` / `JSONArray`).
- **HTTP Status & Diagnostics:** Displays response status codes (e.g. `200 OK`, `201 Created`), request method, URL, and raw JSON payload.
- **Asynchronous Execution:** Non-blocking network calls executed using Kotlin Coroutines (`lifecycleScope.launch(Dispatchers.IO)`).
- **Responsive Material 3 Layout:** Features a `NestedScrollView` container that auto-scrolls down to show the response output, automatically dismissing the soft keyboard when buttons are tapped.
- **Cleartext & Security Configured:** Internet permission (`INTERNET`) and cleartext traffic support enabled in `AndroidManifest.xml`.

---

## 🛠️ Tech Stack & Requirements

- **Language:** Kotlin
- **Min SDK:** 24 (Android 7.0 Nougat)
- **Target / Compile SDK:** 37
- **Architecture / Toolkit:**
  - AndroidX AppCompat & Material Components
  - Kotlin Coroutines (`kotlinx.coroutines`)
  - Android Lifecycle (`androidx.lifecycle:lifecycle-runtime-ktx`)
  - Standard Java/Kotlin `java.net.HttpURLConnection` & `org.json`
  - ConstraintLayout & MaterialCardView

---

## 📁 Project Structure

```
EXP12/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/exp12/
│   │       │   └── MainActivity.kt          # Main Activity: Handles UI listeners, HTTP GET/POST, & Coroutines
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   │   └── activity_main.xml    # UI Layout: ID Input, URL Input, Buttons, Progress Bar, & Output Card
│   │       │   └── values/
│   │       │       └── strings.xml          # String resources
│   │       └── AndroidManifest.xml          # Permissions (INTERNET) & Application Configuration
│   └── build.gradle.kts                     # App-level Gradle build configuration
├── build.gradle.kts                         # Root-level Gradle build file
└── README.md                                # Project documentation
```

---

## 🚀 How to Run the Project

### Prerequisites
1. **Android Studio** (Ladybug 2024.2.1 or newer recommended)
2. **Java Development Kit (JDK):** Version 11 or later
3. **Android Device / Emulator:** Running API Level 24 or higher with active Internet connectivity

### Steps to Run
1. **Open Project:** Launch Android Studio and open the `EXP12` folder.
2. **Sync Gradle:** Wait for Android Studio to sync the project dependencies automatically (`Sync Project with Gradle Files`).
3. **Connect Device / Start Emulator:** Start an Android Virtual Device (AVD) or connect a physical device with USB Debugging enabled.
4. **Build & Run:**
   - Click the **Run 'app'** button (`Shift + F10`) in Android Studio.
   - Alternatively, execute the Gradle assemble command from the terminal:
     ```bash
     ./gradlew app:assembleDebug
     ```

---

## 🌐 Sample Web Service API

The app uses the free mock REST API **JSONPlaceholder**:
- **Base Endpoint:** `https://jsonplaceholder.typicode.com/posts/`
- **GET Request Example:** `https://jsonplaceholder.typicode.com/posts/1`
- **POST Request Example:** `https://jsonplaceholder.typicode.com/posts/`

---

## 📱 How to Use the App

1. **Querying Data (HTTP GET):**
   - Enter an ID in the **"Enter Post ID"** field (e.g. `1`, `2`, `5`).
   - Tap **Fetch Data (GET)**.
   - The keyboard dismisses, progress spinner activates, and the screen automatically scrolls to display:
     - `STATUS: 200 OK`
     - Parsed response fields (`• ID`, `• USERID`, `• TITLE`, `• BODY`)
     - Complete raw JSON payload.

2. **Sending Data (HTTP POST):**
   - Enter an ID or leave default `1`.
   - Tap **Send Data (POST)**.
   - The app posts a JSON payload to the web service and returns:
     - `STATUS: 201 Created`
     - Sent JSON payload details
     - Server response containing generated record ID (e.g. `101`).

---

## 📄 License & Usage

This project was created for educational purposes and Android development lab experiments.

## images
![Screenshot 2026-10-29 110416.png](images/Screenshot%202026-10-29%20110416.png)
![Screenshot 2026-10-29 110429.png](images/Screenshot%202026-10-29%20110429.png)
![Screenshot 2026-10-29 110510.png](images/Screenshot%202026-10-29%20110510.png)