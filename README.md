# 📱 FastConnect - Android Social & News App

A modern Android application built with **Kotlin**, **Jetpack Compose**, and **Firebase** for managing student societies, announcements, and news articles with real-time synchronization.

---
## 📋 Project Overview

**FastConnect** is a multi-assignment university project that evolved through three major phases:
 
### Assignment Phases:
- **Assignment #02**: Initial UI/UX with basic navigation
- **Assignment #03**: REST API integration + SQLite persistence
- **Assignment #04**: Firebase cloud migration with real-time sync.

 
---

## ✨ Key Features

### 🔐 Authentication & Authorization
- **Firebase Email/Password Authentication**
- Secure session persistence
- User profile management
- Role-based access

### 📰 News & Content Management
- **REST API Integration** via NewsAPI.org (Retrofit)
- Real-time news feed with pull-to-refresh
- Save articles to personal bookmarks
- Share functionality (WhatsApp, Email, etc.)

### 🔖 Bookmark Management
- Create & organize bookmarks in custom folders
- Full CRUD operations
- Advanced search (SQL LIKE queries)
- Sort bookmarks (Newest, A-Z, Z-A, Oldest)
- Swipe-to-delete with undo

### 🏛️ Society Management
- Browse registered societies
- Follow/unfollow societies
- View society announcements in real-time
- Receive push notifications (FCM)

### 📢 Announcements
- Real-time announcement synchronization
- Category-based filtering
- Announcement creation by society admins

### 💬 Additional Features
- Jetpack Compose UI components
- Push notifications via Firebase Cloud Messaging
- Offline-first design with Firebase Realtime Database
- Background threading via Kotlin Coroutines

---

## 🏗️ Project Structure

```
SMD_Project/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/fastconnect/
│   │   │   │   ├── activities/          # Activity screens
│   │   │   │   ├── fragments/           # Fragment screens
│   │   │   │   ├── adapters/            # RecyclerView adapters
│   │   │   │   ├── models/              # Data classes (NewsArticle, Bookmark, etc.)
│   │   │   │   ├── api/                 # Retrofit API client
│   │   │   │   ├── db/                  # Database helpers (Firebase)
│   │   │   │   ├── services/            # Background services (FCM)
│   │   │   │   └── utils/               # Utility functions
│   │   │   ├── res/
│   │   │   │   ├── layout/              # XML layouts
│   │   │   │   ├── menu/                # Navigation menus
│   │   │   │   ├── drawable/            # Images & vectors
│   │   │   │   └── values/              # Strings, colors, themes
│   │   │   └── AndroidManifest.xml
│   │   └── test/                         # Unit tests
│   ├── build.gradle.kts                 # App-level dependencies
│   └── google-services.json              # Firebase config
├── gradle/                               # Gradle wrapper
├── build.gradle.kts                     # Project-level build config
├── settings.gradle.kts                  # Project settings
├── FIREBASE_MIGRATION.md                # Migration guide
└── README.md                             # This file
```

---

## 🛠️ Tech Stack

### Core Technologies
- **Language**: Kotlin 1.9.0
- **Build System**: Gradle (Kotlin DSL)
- **Min SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 15 (API 35)

### Key Libraries

#### Backend & Cloud
- **Firebase**:
  - Authentication (Email/Password)
  - Realtime Database
  - Cloud Messaging (Push Notifications)
  - Analytics

#### API & Networking
- **Retrofit 2.9.0**: REST API client
- **Gson**: JSON serialization/deserialization
- **OkHttp**: HTTP client with logging interceptor

#### UI & Composition
- **Jetpack Compose**: Modern declarative UI
- **Material 3**: Material Design components
- **View Binding**: Type-safe view references

#### Concurrency
- **Kotlin Coroutines 1.7.3**: Asynchronous programming
- **Lifecycle Runtime KTX 2.9.0**: lifecycleScope integration

#### UI Components
- **SwipeRefreshLayout 1.1.0**: Pull-to-refresh functionality
- **ConstraintLayout**: Responsive layouts
- **RecyclerView**: Efficient list rendering

#### Testing
- **JUnit 4**: Unit testing
- **Espresso**: UI testing

---

## 📊 Database Architecture

### Firebase Realtime Database Structure

```
├── users/
│   └── {uid}/
│       ├── name, email, role
│       ├── folders/
│       │   └── {folderId}/ → {name, createdAt}
│       ├── bookmarks/
│       │   └── {bookmarkId}/ → {title, url, note, folderId, createdAt}
│       ├── followedSocieties/
│       │   └── {societyId}: true
│       └── savedEvents/
│           └── {eventId}: true
├── societies/
│   └── {pushId}/ → {id, name, description}
└── announcements/
    └── {pushId}/ → {id, title, description, category, type, date, societyId}
```

### Logical Relationships
1. `announcements.societyId` → references `societies/{id}`
2. `users/{uid}/bookmarks/{id}.folderId` → references `users/{uid}/folders/{id}`

---

## 🚀 Getting Started

### Prerequisites
- Android Studio (2023.1 or later)
- Kotlin 1.9.0+
- Java 11+
- Firebase project configured

### Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/tyab07/SMD_Project.git
   cd SMD_Project
   ```

2. **Configure Firebase**:
   - Download `google-services.json` from Firebase Console
   - Place it in the `app/` directory

3. **Build & Run**:
   ```bash
   ./gradlew build
   # Then run via Android Studio or adb
   ```

---

## 📱 App Navigation

The app features a 5-tab bottom navigation menu:

1. **Home** - Dashboard with quick links and featured content
2. **News** - REST API-powered news feed with save functionality
3. **Courses** - Society/course listings and announcements
4. **Bookmarks** - Personal bookmarks with search, sort, and filter
5. **Profile** - User profile and authentication controls

---

## 🔄 Evolution: Assignment Phases

### Phase 1: Assignment #02
- Basic UI/UX with Navigation Drawer
- Static mock data
- Fragment-based navigation

### Phase 2: Assignment #03
- ✅ **F1**: REST API integration (NewsAPI.org via Retrofit)
- ✅ **F2**: SQLite schema with 2 tables + Foreign Key constraints
- ✅ **F3**: Full CRUD operations on bookmarks
- ✅ **F4**: API and SQLite as separate functional modules
- ✅ **F5**: Dynamic SQL queries (LIKE search, ORDER BY sort, WHERE filter)

**Features Added**:
- NewsFragment with real-time API data
- BookmarksFragment with advanced filtering
- Save news articles to SQLite bookmarks
- Swipe-to-delete with undo
- Share functionality
- Kotlin Coroutines for background threading

### Phase 3: Assignment #04
- ✅ **F1**: Firebase Authentication (Email/Password with secure credential storage)
- ✅ **F2**: Firebase Realtime Database with 2+ logically related collections

**Migration Changes**:
- Removed local SQLite
- Migrated to Firebase for persistent cloud storage
- Real-time data synchronization across devices
- Firebase Cloud Messaging for push notifications
- Jetpack Compose for NotificationInboxActivity

---

## 🔐 Security Features

- **Firebase Auth**: Hashed password storage & session management
- **Database Rules**: Firebase Realtime Database rules enforce user-level data isolation
- **Network Security**: HTTPS/TLS for all network communications
- **Cleartext Traffic**: Disabled for production (enabled for development APIs only)

---

## 📡 API Integration

### NewsAPI.org
- **Endpoint**: `https://newsapi.org/v2/top-headlines`
- **Method**: GET with query parameters (country, search, etc.)
- **Authentication**: API key (configured in `NewsApiService.kt`)
- **Response Format**: JSON with articles array

### Firebase APIs
- **Authentication**: Email/Password signup & signin
- **Database**: Real-time read/write/delete operations
- **Messaging**: FCM tokens for push notifications

---

## 🧪 Testing

Run unit tests:
```bash
./gradlew test
```

Run instrumentation tests:
```bash
./gradlew connectedAndroidTest
```

---

## 📝 Documentation Files

- **[FIREBASE_MIGRATION.md](FIREBASE_MIGRATION.md)**: Detailed migration guide from SQLite to Firebase
- **[ASSIGNMENT#04-SMD.pdf](ASSIGNMENT#04-SMD.pdf)**: Assignment requirements & specifications
- **[update.txt](update.txt)**: Assignment #03 changelog with file-by-file modifications
- **[db.html](db.html)**: Database schema visualization
- **[logic_map.html](logic_map.html)**: Application logic flow diagram

---

## 🤝 Contributing

This is a university assignment project. To contribute:

1. Create a feature branch: `git checkout -b feature/your-feature`
2. Commit changes: `git commit -m 'Add your feature'`
3. Push to branch: `git push origin feature/your-feature`
4. Submit a pull request

---

## 📄 License

This project is part of a university assignment and is provided as-is.

---

## 📞 Contact & Support

- **Owner**: [tyab07](https://github.com/tyab07)
- **Repository**: [SMD_Project](https://github.com/tyab07/SMD_Project)
- **Firebase Project**: `smd-project-513f4`

---

## 🎯 Future Enhancements

- [ ] Offline-first with Firebase Realtime Sync improvements
- [ ] Advanced user analytics and personalization
- [ ] Dark mode support
- [ ] Multi-language support (i18n)
- [ ] Animated transitions and Lottie animations
- [ ] Voice-based search and filters
- [ ] Event calendar integration
- [ ] Social sharing to multiple platforms

---

**Last Updated**: May 11, 2026  
**Version**: 2.0 (Assignment #04 - Firebase Migration)
