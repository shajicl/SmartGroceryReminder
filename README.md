# Smart Grocery Reminder (Android)

Smart Grocery Reminder is a native Android app that helps users create and manage grocery lists and receive reminders when they arrive near a store. The app uses Firebase for authentication and cloud data storage, and uses geofencing + notifications to support location-based reminders.

## Features
- User authentication (Sign Up / Login / Forgot Password) using Firebase Auth
- Grocery list creation and item management
- Cloud sync with Firebase Firestore (store lists and items per user/household)
- Store reminders using geofencing (notify when entering a saved store radius)
- Push notifications support using Firebase Cloud Messaging (FCM)
- Jetpack Compose UI with MVVM-style separation (ViewModels + repositories)

## Tech Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose, Navigation Compose
- **Architecture:** MVVM-style (ViewModels + Repository layer)
- **Backend/Cloud:** Firebase Auth, Firebase Firestore, Firebase Messaging (FCM)
- **Location:** Google Play Services Location (Geofencing)
- **Tools:** Android Studio, Gradle

## Project Structure (High-level)
- `auth/` → authentication screens + AuthViewModel
- `data/` → repository layer (Firestore interactions)
- `model/` → data models + ViewModels for lists/stores/households
- `grocery/` → grocery UI + geofencing receiver/manager
- `notifications/` → NotificationHelper
- `service/` → Firebase Messaging service (FCM)
- `lists/`, `home/`, `settings/`, `household/` → UI screens

## Setup Instructions
### 1) Clone and open
1. Clone the repo
2. Open in **Android Studio**
3. Let Gradle sync complete

### 2) Firebase Configuration (Required)
This project requires your own Firebase project.

1. Create a Firebase project
2. Add an Android app in Firebase Console
3. Download `google-services.json`
4. Place it here:
   - `app/google-services.json`

> Security note: `google-services.json` is not included in this repo. Use your own Firebase project.

### 3) Enable Firebase services
In Firebase console:
- Enable **Authentication** (Email/Password)
- Create a **Firestore database**

### 4) Run the app
- Select an emulator/device
- Click **Run**

## Permissions Used
- Location permissions for geofencing:
  - `ACCESS_FINE_LOCATION`
  - `ACCESS_COARSE_LOCATION`
  - `ACCESS_BACKGROUND_LOCATION` (for better geofencing reliability)
- Notifications:
  - `POST_NOTIFICATIONS`
- Internet:
  - `INTERNET`


## Future Improvements
- Add Firestore security rules and validation for household sharing
- Improve offline support with local caching (Room)
- Add unit tests for repository + ViewModel logic
- Add CI (GitHub Actions) for build verification
