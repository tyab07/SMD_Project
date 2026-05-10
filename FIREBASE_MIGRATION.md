# 🔥 Firebase Migration — FastConnect (Assignment #04)

## Overview

This document describes the migration of FastConnect's data layer from **local SQLite** (Assignment #03) to **Firebase Cloud Services** (Assignment #04). The migration addresses two core requirements:

- **F1 — Firebase Authentication**: Email/Password authentication system.
- **F2 — Firebase Realtime Database**: Real-time data synchronization across devices with 2+ logically related collections.

---

## What Changed & Why

### 1. Firebase Authentication (F1)

**Why**: Replacing local password storage with Firebase Auth provides secure, hashed credential management and seamless session persistence.

**What was changed**:

| File | What Changed |
|------|-------------|
| `SignUpActivity.kt` | `dbHelper.insertUser()` → `FirebaseAuth.createUserWithEmailAndPassword()` + saves profile to `/users/{uid}` |
| `SignInActivity.kt` | `dbHelper.checkUserLogin()` → `FirebaseAuth.signInWithEmailAndPassword()` |
| `MainActivity.kt` | `SharedPrefs.IS_LOGGED_IN` → `FirebaseAuth.currentUser` for session check |
| `ProfileFragment.kt` | Added `FirebaseAuth.signOut()` on logout |

---

### 2. Firebase Realtime Database (F2)

**Why**: Cloud-based persistence and real-time synchronization.

**Database Structure**:
```
Firebase Realtime Database
├── users/
│   └── {uid}/
│       ├── name, email, role
│       ├── folders/
│       │   └── {folderId}/ → name, createdAt
│       ├── bookmarks/
│       │   └── {bookmarkId}/ → title, url, note, folderId, createdAt
│       ├── followedSocieties/
│       │   └── {societyId}: true
│       └── savedEvents/
│           └── {eventId}: true
├── societies/
│   └── {pushId}/ → id, name, description
└── announcements/
    └── {pushId}/ → id, title, description, category, type, date, societyId
```

**Logical Relationships**:
1. `announcements.societyId` → references `societies/{id}`
2. `users/{uid}/bookmarks/{id}.folderId` → references `users/{uid}/folders/{id}`

**What was changed**:

| File | What Changed |
|------|-------------|
| `AddSocietyActivity.kt` | SQLite insert → `FirebaseHelper.addSociety()` |
| `AddAnnouncementActivity.kt` | SQLite insert → `FirebaseHelper.addAnnouncement()` |
| `SocietiesFragment.kt` | `dbHelper.getAllSocieties()` → `FirebaseHelper.observeSocieties()` (real-time sync) |
| `HomeFragment.kt` | Sample lists → `FirebaseHelper.observeAnnouncements()` (real-time sync) |
| `BookmarksFragment.kt` | All SQLite CRUD → `FirebaseHelper` methods |
| `NewsFragment.kt` | Saved news now writes to Firebase `/users/{uid}/bookmarks` via `FirebaseHelper` |

---

### 3. Build Configuration

- **Kotlin 1.9.0**: Used for maximum stability and compatibility.
- **Compose Compiler**: Using `composeOptions` with `kotlinCompilerExtensionVersion = "1.5.1"`.
- **Firebase BoM**: Version `32.2.0` used for dependency management.

---

### 4. Jetpack Compose & Push Notifications

- **Jetpack Compose**: `NotificationInboxActivity.kt` implemented as a 100% Compose screen.
- **Push Notifications**: Integrated via `FastConnectMessagingService.kt` for real-time alerts.

---

## Technical Cleanup
- **Deleted**: `db/FastConnectDbHelper.kt` (REMOVED to resolve compilation errors and finalize cloud migration).
- **Streamlined**: Removed Google Sign-In to keep the authentication flow simple and focused on Email/Password.
