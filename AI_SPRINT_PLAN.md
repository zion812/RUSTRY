# 🐓 RUSTRY – **AI-Assisted Sprint Plan & Gap-Fill Guide**

_(Everything you need to hand to Windsurf & Qodo Gen today)_

---

## ✅ 1. **Snapshot of what is already done**

| Module | Status | Files / Packages |
|--------|--------|------------------|
| Auth (Phone OTP) | ✅ 100% | `presentation/screen/auth/`, `di/FirebaseModule.kt` |
| CameraX + Gallery | ✅ 100% | `presentation/screen/camera/`, `presentation/components/PhotoGallery.kt` |
| Push (FCM) | ✅ 100% | `presentation/screen/notifications/`, `presentation/viewmodel/NotificationViewModel.kt` |
| Mock Payment | ✅ 100% | `presentation/screen/payment/`, `domain/payment/PaymentGateway.kt` |
| Navigation & Theme | ✅ 100% | `navigation/RoosterNavigation.kt`, `ui/theme/` |
| Local DB (Room) | ✅ 100% | `data/local/entities/`, `di/DatabaseModule.kt` |
| Basic Fowl Management | ✅ 80% | `presentation/screen/fowl/AddFowlScreen.kt`, `data/model/Fowl.kt` |
| Marketplace UI | ✅ 70% | `presentation/screen/MarketplaceScreen.kt` |
| User Profiles | ✅ 100% | `presentation/screen/profile/`, `data/model/User.kt` |

---

## 🎯 2. **Remaining Gaps (what AI must build next)**

| Gap-ID | Epic | Must-Have for MVP | Tech/Notes |
|--------|------|-------------------|------------|
| **G1** | **Fowl Traceability System** | Weekly health updates, 52-week breeder tracking | Room entity + Firestore sync |
| **G2** | **Coins Wallet System** | Micro-payment wallet (₹5 = 1 coin) + transaction ledger | Firestore sub-collection + Room cache |
| **G3** | **Verified Transfer Flow** | Atomic swap of bird ownership with OTP proof | Cloud Function + in-app modal |
| **G4** | **Enhanced Marketplace** | Advanced filtering, bidding, instant buy | Compose LazyGrid + Firestore queries |
| **G5** | **Hub Inventory System** | 30-min delivery buffer stock per city | Geo-hash based Firestore docs |
| **G6** | **Offline Sync Engine** | Retry queue when network returns | WorkManager + Room sync |
| **G7** | **Multi-Language Support** | Telugu, Tamil, Kannada, Hindi strings | `res/values-{lang}/strings.xml` |
| **G8** | **Health Tracking** | Vaccination records, weight tracking | Room entities + UI screens |

---

## 🧩 3. **AI-Agent Task Cards**

_(Copy-paste into Windsurf / Qodo Gen)_

### 🔹 Windsurf – UX & Screens

```
Task: Generate Jetpack Compose screens for Gap G1 "Fowl Traceability System"

- Screen 1: WeeklyUpdateScreen.kt – weight slider (0-10kg), photo proof (CameraX integration), vaccine checklist
- Screen 2: FowlLineageScreen.kt – family tree visualization with parent/child relationships
- Screen 3: HealthTimelineScreen.kt – chronological health records with photos
- Use Material3 design, Telugu strings placeholder, dark mode support
- Include preview annotations and accessibility features
- Return complete Composable code with state management
```

### 🔹 Qodo Gen – Backend & Data Layer

```
Task: Generate Kotlin code for Gap G2 "Coins Wallet System"

- Room entities: WalletTransactionEntity(id, userId, coinsDelta, reason, timestamp, type)
- Repository: WalletRepository with suspend fun addCoins(), spendCoins(), getBalance()
- Use cases: AddCoinsUseCase, SpendCoinsUseCase, GetWalletBalanceUseCase
- Firestore path: /users/{uid}/wallet/transactions/{transactionId}
- Include concurrent transaction handling and race condition prevention
- Unit tests: WalletRepositoryTest – test concurrent spend scenarios
```

### 🔹 Qodo Gen – Cloud Function _(Node.js/TypeScript)_

```
Task: Implement Firebase Cloud Function for Gap G3 "Verified Transfer Flow"

- Function: onFowlTransfer(fowlId, giverUid, receiverUid, otpCode, transferPrice)
- Transaction steps: verify OTP → validate ownership → update fowl owner → log transfer → handle coins
- Return: {success: true, newOwnerId, transactionId} or error details
- Include Firestore transaction for atomicity
- Generate Jest test suite with Firestore emulator
- Add rate limiting and fraud detection
```

### 🔹 Windsurf – Enhanced Marketplace

```
Task: Create advanced marketplace screens for Gap G4

- Screen 1: EnhancedMarketplaceScreen.kt – filter chips (breed, price, location, traceable)
- Screen 2: BiddingScreen.kt – real-time bidding interface with countdown timer
- Screen 3: FowlDetailScreen.kt – detailed view with image gallery, lineage, health records
- Include search functionality, sorting options, and pagination
- Use LazyVerticalGrid for performance with large datasets
- Add pull-to-refresh and loading states
```

---

## 🗂️ 4. **Folder Blueprint for New Code**

```
C:/RUSTRY/app/src/main/java/com/rio/rustry/

├── features/traceability/
│   ├── data/
│   │   ├── local/HealthRecordEntity.kt
│   │   ├── remote/HealthRecordDto.kt
│   │   └── repository/HealthTrackingRepository.kt
│   ├── domain/
│   │   ├── model/HealthRecord.kt
│   │   ├── usecase/AddHealthRecordUseCase.kt
│   │   └── usecase/GetFowlLineageUseCase.kt
│   └── presentation/
│       ├── WeeklyUpdateScreen.kt
│       ├── FowlLineageScreen.kt
│       └── HealthTimelineScreen.kt

├── features/wallet/
│   ├── data/
│   │   ├── local/WalletTransactionEntity.kt
│   │   ├── remote/WalletDto.kt
│   │   └── repository/WalletRepository.kt
│   ├── domain/
│   │   ├── model/WalletTransaction.kt
│   │   ├── usecase/AddCoinsUseCase.kt
│   │   ├── usecase/SpendCoinsUseCase.kt
│   │   └── usecase/GetWalletBalanceUseCase.kt
│   └── presentation/
│       ├── WalletScreen.kt
│       ├── AddCoinsScreen.kt
│       └── TransactionHistoryScreen.kt

├── features/transfer/
│   ├── data/
│   │   ├── local/TransferEntity.kt
│   │   └── repository/TransferRepository.kt
│   ├── domain/
│   │   ├── model/FowlTransfer.kt
│   │   └── usecase/InitiateTransferUseCase.kt
│   └─�� presentation/
│       ├── TransferBottomSheet.kt
│       └── TransferConfirmationScreen.kt

├── features/marketplace/
│   ├── data/
│   │   ├── local/MarketListingEntity.kt
│   │   └── repository/MarketplaceRepository.kt
│   ├── domain/
│   │   ├── model/MarketListing.kt
│   │   ├── usecase/SearchFowlsUseCase.kt
│   │   └── usecase/PlaceBidUseCase.kt
│   └── presentation/
│       ├── EnhancedMarketplaceScreen.kt
│       ├── BiddingScreen.kt
│       └── SearchFiltersScreen.kt

├── features/inventory/
│   ├── data/
│   │   ├── local/HubInventoryEntity.kt
│   │   └── repository/InventoryRepository.kt
│   ├── domain/
│   │   ├── model/HubInventory.kt
│   │   └── usecase/GetNearbyInventoryUseCase.kt
│   └── presentation/
│       ├── HubInventoryScreen.kt
│       └── DeliveryTrackingScreen.kt

└── features/sync/
    ├── data/
    │   ├── local/SyncQueueEntity.kt
    │   └── repository/SyncRepository.kt
    ├── domain/
    │   ├── model/SyncItem.kt
    │   └── usecase/ProcessSyncQueueUseCase.kt
    └── worker/
        ├── SyncWorker.kt
        └── OfflineDataWorker.kt
```

---

## 🧪 5. **Test Matrix** _(hand to Qodo Gen)_

| Test Class | Scope | Assertions |
|------------|-------|------------|
| `HealthTrackingRepositoryTest` | Room + Firestore sync | insert health record offline → sync when online |
| `WalletRepositoryTest` | Concurrent transactions | simultaneous spend operations fail gracefully |
| `TransferWorkflowTest` | End-to-end flow | OTP mismatch → transfer rejected with proper error |
| `MarketplacePagingTest` | UI + Data | scroll to bottom → loads next page of fowls |
| `BiddingSystemTest` | Real-time updates | bid placed → all watchers receive update |
| `OfflineSyncTest` | WorkManager | network restored → pending changes uploaded |
| `LocalizationTest` | Multi-language | Telugu labels render without truncation |
| `GeohashInventoryTest` | Location-based | nearby hubs returned within 30km radius |

---

## 🛠️ 6. **Gradle Additions for New Modules**

```kotlin
// In Version Catalog (libs.versions.toml)
[versions]
geohash = "1.4.0"
work-runtime = "2.9.0"
paging = "3.2.1"
room-paging = "2.6.1"

[libraries]
geohash = "ch.hsr:geohash:1.4.0"
work-runtime = "androidx.work:work-runtime-ktx:2.9.0"
paging-runtime = "androidx.paging:paging-runtime-ktx:3.2.1"
paging-compose = "androidx.paging:paging-compose:3.2.1"
room-paging = "androidx.room:room-paging:2.6.1"
```

```kotlin
// In app/build.gradle.kts
dependencies {
    // Location and mapping
    implementation(libs.geohash)
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    
    // Background processing
    implementation(libs.work.runtime)
    implementation("androidx.hilt:hilt-work:1.1.0")
    
    // Pagination
    implementation(libs.paging.runtime)
    implementation(libs.paging.compose)
    implementation(libs.room.paging)
    
    // Real-time updates
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0")
    
    // Additional testing
    testImplementation("androidx.paging:paging-testing:3.2.1")
    testImplementation("androidx.work:work-testing:2.9.0")
}
```

---

## 🗓️ 7. **4-Week Burn-Down (AI Sprint)**

| Week | Windsurf Deliverable | Qodo Gen Deliverable | QA Gate |
|------|----------------------|----------------------|---------|
| **W1** | Health tracking UI + Weekly update screens | Room entities + repositories + unit tests | Add 10 health records offline |
| **W2** | Wallet UI + Transaction history screens | Wallet system + concurrent transaction handling | Process 50 coin transactions |
| **W3** | Enhanced marketplace + bidding interface | Cloud functions + transfer system | Complete 10 fowl transfers |
| **W4** | Multi-language + inventory screens | Offline sync + geohash inventory | Test in 3 languages + sync 100 items |

---

## 🎤 8. **One-Line Prompts Ready for AI**

### **Windsurf Prompts:**

```
"Create Jetpack Compose screen `WeeklyUpdateScreen.kt` with weight slider (0-10kg), photo capture button, vaccine checklist – Telugu strings, Material3 design, dark mode support."

"Generate `BiddingScreen.kt` with real-time countdown timer, bid input field, bid history list – use WebSocket for live updates, include bid validation."

"Build `WalletScreen.kt` showing coin balance, recent transactions, add coins button – include animated coin counter and transaction animations."

"Create `FowlLineageScreen.kt` with family tree visualization using Canvas API – show parent-child relationships with connecting lines."

"Design `TransferBottomSheet.kt` with fowl photo, OTP input field, transfer confirmation – include loading states and error handling."
```

### **Qodo Gen Prompts:**

```
"Generate Kotlin use case `class AddHealthRecordUseCase @Inject constructor(...)` that saves health data to Room and syncs to Firestore – include offline handling."

"Create `class WalletRepository @Inject constructor(...)` with suspend functions for addCoins(), spendCoins(), getBalance() – handle concurrent transactions safely."

"Build Firebase Cloud Function `onFowlTransfer` that verifies OTP, updates ownership atomically, handles coin transactions – include comprehensive error handling."

"Generate `class OfflineSyncWorker` using WorkManager that processes pending changes when network is restored – include retry logic and conflict resolution."

"Create `class MarketplaceRepository` with Paging3 integration for infinite scroll, search filters, and real-time bid updates using Firestore listeners."
```

---

## ✅ 9. **Checklist Before Production**

### **Core Features:**
- [ ] All G1-G8 gaps closed and tested
- [ ] Health tracking with photo proof working
- [ ] Wallet system with concurrent transaction safety
- [ ] Transfer system with OTP verification
- [ ] Enhanced marketplace with bidding
- [ ] Offline sync with conflict resolution
- [ ] Multi-language support (4 languages)
- [ ] Hub inventory with geolocation

### **Quality Assurance:**
- [ ] Telugu strings reviewed by native speaker
- [ ] All unit tests passing (target: 95% coverage)
- [ ] Integration tests for critical flows
- [ ] Performance tests for large datasets (1000+ fowls)
- [ ] Security audit for payment and transfer flows

### **Build & Deployment:**
- [ ] `proguard-rules.pro` keeps all model classes
- [ ] `bundleRelease` < 25 MB (Play Store limit)
- [ ] Firebase App Distribution group of 50 farmers invited
- [ ] Crashlytics and Analytics configured
- [ ] Performance monitoring enabled

### **Business Readiness:**
- [ ] Onboarding flow for new farmers
- [ ] Help documentation in local languages
- [ ] Customer support contact integration
- [ ] Terms of service and privacy policy
- [ ] Payment gateway integration (Razorpay/UPI)

---

## 🚀 10. **Expected Business Impact Post-Sprint**

### **Week 1 Targets:**
- **500+ Farmer Sign-ups** (enhanced onboarding)
- **2,000+ Fowl Listings** (improved add-fowl flow)
- **100+ Successful Transfers** (verified transfer system)
- **₹50,000+ Coin Transactions** (wallet system)

### **Technical KPIs:**
- **App Startup Time:** <2 seconds (optimized)
- **Offline Capability:** 100% core features work offline
- **Real-time Updates:** <500ms bid notification delivery
- **Search Performance:** <200ms for 10,000+ fowl database
- **Transfer Success Rate:** >98% with OTP verification

### **User Experience Improvements:**
- **Multi-language Support:** 80% of users in native language
- **Health Tracking:** 90% farmers using weekly updates
- **Bidding System:** 40% increase in fowl prices
- **Offline Usage:** 60% of rural users benefit from offline mode

---

## 📱 11. **Cloud Function Specifications**

### **Firebase Functions to Implement:**

```typescript
// functions/src/index.ts

export const onFowlTransfer = functions.https.onCall(async (data, context) => {
  // Input: { fowlId, giverUid, receiverUid, otpCode, transferPrice }
  // 1. Verify OTP from SMS service
  // 2. Validate fowl ownership
  // 3. Check receiver's wallet balance
  // 4. Atomic transaction: transfer ownership + coins
  // 5. Send notifications to both parties
  // 6. Log transfer for audit trail
});

export const onBidPlaced = functions.firestore
  .document('fowls/{fowlId}/bids/{bidId}')
  .onCreate(async (snap, context) => {
    // 1. Notify fowl owner of new bid
    // 2. Notify previous highest bidder they've been outbid
    // 3. Update fowl document with current highest bid
    // 4. Schedule auto-close if auction time reached
  });

export const processWalletTransaction = functions.https.onCall(async (data, context) => {
  // Input: { userId, amount, type, reason }
  // 1. Validate user authentication
  // 2. Check transaction limits
  // 3. Process payment gateway integration
  // 4. Update wallet balance atomically
  // 5. Send transaction confirmation
});
```

---

## 🎯 12. **Success Metrics Dashboard**

### **Real-time Monitoring:**
```
📊 Active Users: 1,000+ daily
🐔 Fowl Listings: 5,000+ active
💰 Coin Transactions: ₹1,00,000+ daily volume
🔄 Transfer Success Rate: 98%+
📱 App Performance: 99.5% crash-free
🌐 Multi-language Usage: 70% non-English
```

### **Business Intelligence:**
- **Farmer Retention:** 80% monthly active users
- **Average Transaction Value:** ₹500 per fowl
- **Health Tracking Adoption:** 85% farmers logging weekly
- **Bidding Participation:** 60% fowls receive multiple bids
- **Offline Usage:** 40% of interactions happen offline

---

Hand this entire section to your Windsurf & Qodo Gen agents as a single context block.

They now have **exact scopes, file paths, test targets, and prompts** to finish the MVP in 4 weeks and achieve the business targets outlined above.

**🎉 RUSTRY will be the first comprehensive poultry marketplace with advanced traceability, real-time bidding, and offline-first design for rural farmers! 🐔💰📱**