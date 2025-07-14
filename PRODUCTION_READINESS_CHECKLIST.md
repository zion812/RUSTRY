# 🚀 RUSTRY Production Readiness Checklist

## ✅ COMPLETED ITEMS

### 1. 🔍 Critical Build & Release Issues

| # | Check Item | Status | Evidence |
|---|------------|--------|----------|
| 1 | **Release build configuration** | ✅ DONE | `versionCode = 2`, `versionName = "1.0.0-rc1"` |
| 2 | **R8 / ProGuard rules** | ✅ DONE | Added `-keep class com.rio.rustry.data.models.** { *; }` |
| 3 | **Firebase config per flavour** | ✅ DONE | `google-services.json` copied to `app/src/release/` |
| 4 | **Versioning** | ✅ DONE | Version bumped to 1.0.0-rc1 (versionCode 2) |
| 5 | **SSL pinning** | ✅ DONE | Added `network_security_config.xml` with Firebase certs |

### 2. 🔐 Security & Compliance

| # | Item | Status | Action |
|---|------|--------|--------|
| 2.1 | **Privacy policy** | ✅ DONE | Created `privacy-policy.html` with Telugu translation |
| 2.2 | **SSL pinning** | ✅ DONE | Network security config with Firebase certificate pins |
| 2.3 | **ProGuard obfuscation** | ✅ DONE | Comprehensive rules for all components |

### 3. 🚀 CI/CD Infrastructure

| Item | Status |
|------|--------|
| **GitHub Actions workflow** | ✅ DONE |
| **Release build automation** | ✅ DONE |
| **Artifact upload** | ✅ DONE |
| **Mapping file preservation** | ✅ DONE |

## ⏳ PENDING ITEMS (Manual Setup Required)

### 1. 🔑 Keystore & Signing

**CRITICAL: Must be done before release build**

```bash
# Generate release keystore
keytool -genkey -v -keystore release.keystore -alias rustry-key -keyalg RSA -keysize 2048 -validity 10000

# Convert to base64 for GitHub secrets
base64 -i release.keystore | pbcopy  # macOS
base64 -w 0 release.keystore         # Linux
```

**GitHub Secrets to Add:**
- `KEYSTORE_BASE64`: Base64 encoded keystore file
- `KEYSTORE_PASSWORD`: Keystore password
- `KEY_ALIAS`: Key alias (rustry-key)
- `KEY_PASSWORD`: Key password

### 2. 🔥 Firebase Console Setup

**Authentication:**
- [ ] Add privacy policy URL: `https://your-domain.com/privacy-policy.html`
- [ ] Configure phone auth settings
- [ ] Set up authorized domains

**Firestore Rules:**
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
  }
}
```

**Storage Rules:**
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
  }
}
```

### 3. 📱 Play Store Assets (Required)

**Screenshots needed:**
- [ ] 2× phone screenshots (1080x1920)
- [ ] 2× tablet screenshots (1536x2048)
- [ ] Both in Telugu & English

**Graphics needed:**
- [ ] 512×512 hi-res icon
- [ ] 1024×500 feature graphic with tagline "నేటి కోడి – నేటి డెలివరీ"

**Store listing:**
- [ ] Short description ≤80 chars: "Buy native chickens directly from farmers"
- [ ] Full description with Telugu translation
- [ ] Privacy policy URL

### 4. 🧪 Pre-Production QA Script

**30-Minute Test Flow (on Android 8+ device):**

1. Install release APK
2. Register new farmer (phone OTP)
3. Take 3 photos → upload vaccination proof
4. Add 1 fowl → marketplace listing
5. Switch to airplane mode → add 2nd fowl offline
6. Re-enable network → verify offline sync
7. Force-stop → relaunch → data still present
8. **Success criteria:** No crash, no ANR, images load <2s

## 🎯 IMMEDIATE NEXT STEPS

### Step 1: Generate Keystore (5 minutes)
```bash
cd C:/RUSTRY
keytool -genkey -v -keystore release.keystore -alias rustry-key -keyalg RSA -keysize 2048 -validity 10000
```

### Step 2: Test Release Build (10 minutes)
```bash
./gradlew assembleRelease
```

### Step 3: Firebase Rules Update (5 minutes)
- Copy rules above to Firebase Console
- Test with Firebase Emulator

### Step 4: GitHub Secrets Setup (5 minutes)
- Add keystore secrets to GitHub repository
- Test workflow with manual trigger

### Step 5: Privacy Policy Hosting (10 minutes)
```bash
# Deploy to Firebase Hosting
firebase init hosting
firebase deploy --only hosting
```

## 🚨 BLOCKERS TO RESOLVE

1. **Keystore Creation** - Required for signed builds
2. **Firebase Rules** - Required for security compliance
3. **Privacy Policy URL** - Required for Play Store submission
4. **QA Testing** - Required for production confidence

## 📅 TIMELINE

**Today (2-3 hours):**
- ✅ Build configuration fixes (DONE)
- ⏳ Keystore generation (15 min)
- ⏳ Firebase rules update (15 min)
- ⏳ Privacy policy hosting (15 min)
- ⏳ Release build test (30 min)

**Tomorrow:**
- Play Store assets creation
- Beta testing with 5 farmers
- Final QA validation

## 🎉 DEPLOYMENT READY CRITERIA

All items must be ✅ before proceeding:

- [ ] Release build succeeds with zero errors
- [ ] Keystore configured and GitHub secrets set
- [ ] Firebase rules deployed and tested
- [ ] Privacy policy live and accessible
- [ ] QA script passes on real device
- [ ] Crashlytics symbols uploaded correctly

**When all criteria met:** Tag `v1.0.0-rc1` → GitHub Actions → Play Store Internal Testing

---

*Last updated: December 2024*
*Next review: After keystore setup*