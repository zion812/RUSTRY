# 🔥 RUSTRY – Firebase-Only Production Readiness Checklist

## ✅ COMPLETED ITEMS

| # | Checkpoint | Firebase Service | Status | Evidence |
|---|------------|------------------|--------|----------|
| 1 | **Release build config** | N/A | ✅ DONE | Version 1.0.0-rc1, versionCode 2 |
| 2 | **R8 keep rules** | N/A | ✅ DONE | Added `-keep class com.rio.rustry.data.models.** { *; }` |
| 3 | **Privacy policy created** | Hosting | ✅ DONE | `privacy-policy.html` with Telugu translation |
| 4 | **Network security config** | N/A | ✅ DONE | SSL pinning for Firebase domains |

## ⚠️ CURRENT BLOCKER

**Build Status**: FAILING due to Hilt/KAPT compilation errors
**Root Cause**: Complex dependency injection setup preventing release build

## 🎯 IMMEDIATE FIREBASE ACTIONS

### 1. Firestore Security Rules
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Deny all by default
    match /{document=**} {
      allow read, write: if false;
    }
    
    // Allow users to access their own data
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Allow read access to public listings
    match /listings/{listingId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == resource.data.farmerId;
    }
    
    // Allow fowl data access
    match /fowls/{fowlId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == resource.data.ownerId;
    }
  }
}
```

### 2. Storage Security Rules
```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    // Deny all by default
    match /{allPaths=**} {
      allow read, write: if false;
    }
    
    // Allow users to upload to their own folders
    match /users/{userId}/{allPaths=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Allow public read for fowl images
    match /fowls/{fowlId}/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }
  }
}
```

### 3. Remote Config Setup
```json
{
  "min_version_code": 3,
  "force_update_enabled": false,
  "maintenance_mode": false,
  "max_image_size_mb": 5,
  "max_fowls_per_user": 100
}
```

## 🚨 CRITICAL DECISION POINT

**Option A: Fix Build Issues (2-4 hours)**
- Debug Hilt/KAPT compilation errors
- Fix all ViewModel dependencies
- Restore full functionality

**Option B: Simplified Firebase Build (30 minutes)**
- Create minimal Firebase-only APK
- Remove complex features temporarily
- Focus on core Firebase integration

## 📋 FIREBASE CONSOLE TASKS

### Authentication
- [ ] Add privacy policy URL: `https://rustry.web.app/privacy`
- [ ] Configure phone auth settings
- [ ] Set up authorized domains

### Firestore
- [ ] Deploy security rules (above)
- [ ] Create indexes for queries
- [ ] Set up backup schedule

### Storage
- [ ] Deploy security rules (above)
- [ ] Configure CORS for web access
- [ ] Set up lifecycle policies

### App Check
- [ ] Enable Play Integrity API
- [ ] Configure enforcement levels
- [ ] Test with debug tokens

### Crashlytics
- [ ] Force test crash
- [ ] Verify symbol upload
- [ ] Set up alerts

### Performance
- [ ] Enable monitoring
- [ ] Set cold-start threshold <3s
- [ ] Configure custom traces

### App Distribution
- [ ] Create "rusty-closed" group
- [ ] Add 5 farmer testers
- [ ] Upload first build

### Remote Config
- [ ] Add version control parameters
- [ ] Set up A/B tests
- [ ] Configure rollout strategy

## 🎯 SIMPLIFIED FIREBASE BUILD APPROACH

If we proceed with Option B, create minimal build:

1. **Remove complex features temporarily**:
   - Disable Hilt dependency injection
   - Remove Room database (use Firebase only)
   - Simplify ViewModels
   - Remove complex navigation

2. **Keep core Firebase features**:
   - Authentication (Phone OTP)
   - Firestore (basic CRUD)
   - Storage (image upload)
   - Crashlytics
   - Analytics

3. **Build and deploy**:
   ```bash
   ./gradlew assembleRelease
   firebase appdistribution:distribute app/build/outputs/apk/release/app-release.apk \
     --app 1:123456789:android:abcd \
     --groups "rusty-closed"
   ```

## 📅 TIMELINE

**Option A (Full Fix)**:
- 2-4 hours debugging
- Risk: May not complete today

**Option B (Simplified)**:
- 30 minutes to working build
- Firebase deployment in 1 hour
- Can iterate and improve later

## 🎉 SUCCESS CRITERIA (Simplified)

- [ ] APK builds successfully
- [ ] Firebase Auth works (phone OTP)
- [ ] Basic Firestore read/write
- [ ] Image upload to Storage
- [ ] Crashlytics reports crashes
- [ ] App Distribution delivers to testers

## 🚀 RECOMMENDATION

**Proceed with Option B** for immediate Firebase deployment:
1. Get working build with core Firebase features
2. Deploy to App Distribution for testing
3. Iterate and add complexity in next sprint

This ensures we meet the Firebase-only production checklist today and can build upon it incrementally.

---

**Next Action**: Choose Option A or B and proceed accordingly
**ETA**: 30 minutes (Option B) or 2-4 hours (Option A)