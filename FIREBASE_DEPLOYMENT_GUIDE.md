# Firebase Deployment Guide

## Overview
This guide provides step-by-step instructions for deploying the Firebase-centric Rustry implementation to production.

## Prerequisites

### 1. Firebase CLI Installation
```bash
npm install -g firebase-tools
firebase login
```

### 2. Project Setup
```bash
# Initialize Firebase in project directory
firebase init

# Select the following services:
# ✓ Firestore
# ✓ Functions
# ✓ Hosting
# ✓ Storage
# ✓ Realtime Database
```

### 3. Environment Configuration
```bash
# Set Razorpay configuration for Cloud Functions
firebase functions:config:set razorpay.key_id="YOUR_RAZORPAY_KEY_ID"
firebase functions:config:set razorpay.key_secret="YOUR_RAZORPAY_KEY_SECRET"
```

## Deployment Steps

### Phase 1: Core Infrastructure Deployment

#### 1. Deploy Firestore Rules and Indexes
```bash
# Deploy Firestore security rules
firebase deploy --only firestore:rules

# Deploy Firestore indexes
firebase deploy --only firestore:indexes
```

#### 2. Deploy Realtime Database Rules
```bash
# Deploy Realtime Database rules
firebase deploy --only database
```

#### 3. Deploy Storage Rules
```bash
# Deploy Storage security rules
firebase deploy --only storage
```

#### 4. Deploy Cloud Functions
```bash
# Install dependencies
cd firebase/functions
npm install

# Deploy all functions
firebase deploy --only functions

# Or deploy specific functions
firebase deploy --only functions:processPayment
firebase deploy --only functions:verifyPayment
firebase deploy --only functions:enforceTraceability
```

#### 5. Deploy Hosting (Optional)
```bash
# Deploy privacy policy and terms
firebase deploy --only hosting
```

### Phase 2: Android App Configuration

#### 1. Update Build Configuration
Ensure the following in `app/build.gradle.kts`:
```kotlin
// Firebase dependencies are properly configured
implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
implementation("com.google.firebase:firebase-auth-ktx")
implementation("com.google.firebase:firebase-firestore-ktx")
implementation("com.google.firebase:firebase-database-ktx")
implementation("com.google.firebase:firebase-storage-ktx")
implementation("com.google.firebase:firebase-messaging-ktx")
implementation("com.google.firebase:firebase-functions-ktx")
```

#### 2. Configure Firebase in Application
The `RoosterApplication.kt` already includes Firebase initialization with offline persistence.

#### 3. Test Firebase Connection
```kotlin
// Use the test method in FirebaseService
val result = firebaseService.testFirebaseConnection()
```

## Security Configuration

### 1. Firestore Security Rules
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can only access their own data
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Fowls are readable by all, writable by owner
    match /fowls/{fowlId} {
      allow read: if true;
      allow write: if request.auth != null && 
        (resource == null || resource.data.ownerId == request.auth.uid);
    }
    
    // Payment logs are restricted
    match /payment_logs/{logId} {
      allow read, write: if false; // Only Cloud Functions can access
    }
  }
}
```

### 2. Realtime Database Rules
```json
{
  "rules": {
    "traceability": {
      "$fowlId": {
        ".read": true,
        ".write": "auth != null"
      }
    },
    "social_feed": {
      ".read": true,
      ".write": "auth != null"
    }
  }
}
```

### 3. Storage Rules
```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /fowls/{fowlId}/{allPaths=**} {
      allow read: if true;
      allow write: if request.auth != null;
    }
  }
}
```

## Testing and Validation

### 1. Offline Functionality Test
```bash
# Test offline persistence
adb shell settings put global airplane_mode_on 1
adb shell am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true

# Verify app functionality offline
# Re-enable network
adb shell settings put global airplane_mode_on 0
adb shell am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false
```

### 2. Real-time Sync Test
1. Open app on two devices
2. Add/modify fowl data on one device
3. Verify real-time updates on second device
4. Target: <1s update time

### 3. Payment Integration Test
```bash
# Test payment flow with Razorpay test credentials
# Use test card: 4111 1111 1111 1111
# CVV: 123, Expiry: Any future date
```

### 4. Cloud Functions Test
```bash
# Test functions locally
firebase emulators:start --only functions

# Test specific function
curl -X POST http://localhost:5001/YOUR_PROJECT/us-central1/processPayment \
  -H "Content-Type: application/json" \
  -d '{"amount": 1000, "fowlId": "test123", "buyerId": "buyer1", "sellerId": "seller1"}'
```

## Monitoring and Analytics

### 1. Firebase Console Monitoring
- Monitor function execution times
- Track Firestore read/write operations
- Monitor storage usage
- Review crash reports

### 2. Performance Monitoring
```kotlin
// Add custom traces in critical paths
val trace = FirebasePerformance.getInstance().newTrace("fowl_load_time")
trace.start()
// ... load fowls
trace.stop()
```

### 3. Analytics Events
```kotlin
// Track key user actions
firebaseAnalytics.logEvent("fowl_purchased") {
    param("fowl_id", fowlId)
    param("price", price)
    param("breed", breed)
}
```

## Production Checklist

### Pre-deployment
- [ ] Firebase project configured for production
- [ ] Razorpay production keys configured
- [ ] SSL certificate pinning updated with production certificates
- [ ] Security rules reviewed and tested
- [ ] Cloud Functions tested with production data
- [ ] Analytics and monitoring configured

### Post-deployment
- [ ] Verify offline functionality works
- [ ] Test real-time sync performance (<1s)
- [ ] Validate payment flow end-to-end
- [ ] Monitor function execution and errors
- [ ] Check Firestore quota usage
- [ ] Verify push notifications delivery

## Troubleshooting

### Common Issues

#### 1. Offline Persistence Not Working
```kotlin
// Ensure persistence is enabled before any Firestore operations
FirebaseFirestore.getInstance().enableNetwork()
```

#### 2. Real-time Updates Delayed
- Check network connectivity
- Verify Firestore indexes are deployed
- Monitor function execution times

#### 3. Payment Function Errors
- Verify Razorpay configuration
- Check function logs: `firebase functions:log`
- Validate request payload format

#### 4. SSL Pinning Failures
- Update certificate pins in NetworkSecurityManager
- Verify network_security_config.xml
- Test with development certificates first

### Debug Commands
```bash
# View function logs
firebase functions:log

# Check Firestore indexes
firebase firestore:indexes

# Test security rules
firebase emulators:start --only firestore
```

## Performance Optimization

### 1. Firestore Optimization
- Use compound indexes for complex queries
- Implement pagination for large datasets
- Cache frequently accessed data locally

### 2. Cloud Functions Optimization
- Use connection pooling for database connections
- Implement function warming to reduce cold starts
- Optimize function memory allocation

### 3. Real-time Database Optimization
- Structure data for efficient queries
- Use shallow queries when possible
- Implement proper indexing

## Scaling Considerations

### 1. Firestore Scaling
- Monitor read/write operations
- Implement proper data partitioning
- Use subcollections for large datasets

### 2. Cloud Functions Scaling
- Monitor concurrent executions
- Implement proper error handling
- Use appropriate timeout values

### 3. Storage Scaling
- Implement image compression
- Use CDN for global distribution
- Monitor bandwidth usage

## Support and Maintenance

### 1. Regular Maintenance Tasks
- Update SSL certificate pins quarterly
- Review and update security rules
- Monitor and optimize function performance
- Update Firebase SDK versions

### 2. Monitoring Alerts
- Set up alerts for function errors
- Monitor Firestore quota usage
- Track payment processing failures
- Monitor app crash rates

### 3. Backup and Recovery
- Regular Firestore exports
- Function code version control
- Configuration backup procedures
- Disaster recovery planning

## Conclusion

This deployment guide ensures a smooth transition from development to production for the Firebase-centric Rustry implementation. Follow the checklist carefully and monitor the system closely during the initial deployment phase.

For additional support, refer to:
- [Firebase Documentation](https://firebase.google.com/docs)
- [Razorpay Integration Guide](https://razorpay.com/docs)
- [Android Security Best Practices](https://developer.android.com/topic/security/best-practices)