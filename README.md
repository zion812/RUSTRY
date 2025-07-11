# 🐓 Rooster Platform - Phase 1 MVP

A mobile app connecting rural poultry farmers with urban buyers, focusing on traceability and market access.

## 📱 Features Implemented

### ✅ Phase 1 MVP Features

#### **User Management**
- User registration with email/password
- User types: General (Buyer) vs Farmer
- Profile management with contact information
- Firebase Authentication integration

#### **Authentication Screens**
- Login screen with email/password
- Registration screen with user type selection
- Form validation and error handling
- Secure password input with show/hide toggle

#### **Marketplace**
- Browse fowl listings
- Filter by breed, location, and traceability status
- Traceability badges (✅ traceable / ⚠️ non-traceable)
- Empty state handling and error management

#### **Profile Management**
- View user profile information
- Display farmer statistics (non-traceable listing count)
- Sign out functionality
- User type-specific features

#### **Data Models**
- User model with traceability limits
- Fowl model with breed, lineage, and traceability data
- Message model for future messaging features
- Repository pattern for data access

## 🏗️ Architecture

### **Tech Stack**
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Backend**: Firebase (Auth, Firestore, Storage)
- **Architecture**: MVVM with Repository pattern
- **Navigation**: Navigation Compose
- **Image Loading**: Coil

### **Project Structure**
```
app/src/main/java/com/rio/rustry/
├── data/
│   ├── model/          # Data classes (User, Fowl, Message)
│   └── repository/     # Data access layer
├── navigation/         # Navigation setup
├── presentation/
│   ├── screen/        # UI screens
│   │   ├── auth/      # Login/Register screens
│   │   ├── marketplace/ # Marketplace and components
│   │   └── profile/   # Profile screen
│   └── viewmodel/     # ViewModels for state management
└── ui/theme/          # Material 3 theming
```

## 🚀 Getting Started

### **Prerequisites**
- Android Studio (latest version)
- Android SDK (API 23+)
- Firebase project with google-services.json

### **Setup**
1. Clone the repository
2. Open in Android Studio
3. Ensure Firebase configuration is in place
4. Build and run the app

### **Firebase Setup Required**
- Authentication (Email/Password)
- Firestore Database
- Storage (for image uploads)

## 📋 PRD Compliance

### **Implemented Requirements**
- ✅ FR1: User registration with phone/email and user type selection
- ✅ FR2: Profile editing capability (UI ready)
- ✅ FR6: Browse listings with filters
- ✅ FR7: Listings display with traceability badges
- ✅ FR11: Basic messaging structure (models ready)
- ✅ NFR3: Secure user data storage via Firebase Auth

### **Partially Implemented**
- 🔄 FR3-FR5: Fowl management (data models ready, UI pending)
- 🔄 FR8: Detailed fowl view (navigation ready)
- 🔄 FR9-FR10: Non-traceable limits (logic implemented, enforcement pending)

### **Next Sprint Features**
- Add Fowl screen with image upload
- Fowl detail view with lineage information
- 1:1 messaging implementation
- Non-traceable listing limit enforcement
- Offline caching capabilities

## 🎯 Key Features

### **Traceability Focus**
- Clear visual distinction between traceable and non-traceable fowls
- 5-fowl limit for non-traceable listings per farmer
- Parent ID tracking for lineage verification

### **User Experience**
- Simple, rural-friendly interface
- Maximum 3 taps to list a fowl (design goal)
- Offline-first approach (foundation laid)
- Material 3 design system

### **Security & Performance**
- Firebase Authentication for secure user management
- Repository pattern for clean data access
- Compose for modern, performant UI
- Error handling and loading states

## 🔧 Development Notes

### **Current State**
- Core authentication and navigation working
- Marketplace browsing functional
- Profile management complete
- Ready for fowl listing implementation

### **Known Issues**
- Some deprecated icon warnings (non-breaking)
- Fowl listing/adding screens need implementation
- Messaging system needs UI implementation

### **Performance Considerations**
- Lazy loading for fowl lists
- Image caching with Coil
- Efficient Firestore queries with indexing

## 📱 Screenshots
*Screenshots will be added once the app is running on device*

## 🤝 Contributing
This is a Phase 1 MVP implementation. Future phases will include:
- AI chatbot integration
- Live video streaming
- Advanced analytics
- Payment integration
- Bloodline visualization

---

**Built with ❤️ for rural poultry farmers and urban buyers**