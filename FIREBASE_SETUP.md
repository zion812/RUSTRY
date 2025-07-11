# 🔥 Firebase Configuration Fix Guide

## ❌ **Current Issue**
You're getting `CONFIGURATION_NOT_FOUND` error when creating an account because Firebase Authentication is not properly configured.

## ✅ **Step-by-Step Fix**

### **1. Enable Email/Password Authentication**

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: **rustry-rio**
3. Click **Authentication** in the left sidebar
4. Click **Sign-in method** tab
5. Find **Email/Password** in the Native providers list
6. Click on **Email/Password**
7. Toggle **Enable** to ON
8. Click **Save**

### **2. Enable Firestore Database**

1. In Firebase Console, click **Firestore Database** in the left sidebar
2. Click **Create database**
3. Choose **Start in test mode** (for development)
4. Select a location (choose closest to your region)
5. Click **Done**

### **3. Enable Firebase Storage**

1. Click **Storage** in the left sidebar
2. Click **Get started**
3. Choose **Start in test mode** (for development)
4. Select the same location as Firestore
5. Click **Done**

### **4. Download Fresh Configuration File**

1. In Firebase Console, click the **Settings gear icon** → **Project settings**
2. Scroll down to **Your apps** section
3. Find your Android app
4. Click **Download google-services.json**
5. Replace the existing file in your project at: `app/google-services.json`

### **5. Verify Configuration**

After completing the above steps:

1. **Clean and rebuild** your project:
   ```bash
   ./gradlew clean
   ./gradlew build
   ```

2. **Run the app** and try creating an account again

## 🔍 **Debugging Tools**

I've added better error messages to help you debug. The app will now show:

- ✅ Clear error messages for configuration issues
- ✅ Specific instructions for each type of Firebase error
- ✅ Network connectivity issues
- ✅ Invalid email/password format errors

## 📱 **Test the Fix**

1. Open the app
2. Go to **Sign Up** screen
3. Fill in the form:
   - **Name**: Test User
   - **Email**: test@example.com
   - **Phone**: +1234567890
   - **Location**: Test City
   - **User Type**: Farmer or Buyer
   - **Password**: test123 (minimum 6 characters)
4. Tap **Create Account**

## ⚠️ **Common Issues & Solutions**

### **Issue**: Still getting CONFIGURATION_NOT_FOUND
**Solution**: 
- Make sure you downloaded the **latest** google-services.json
- Restart Android Studio
- Clean and rebuild the project

### **Issue**: Network error
**Solution**:
- Check internet connection
- Try using a different network
- Disable VPN if using one

### **Issue**: Email already in use
**Solution**:
- Use a different email address
- Or try signing in with the existing email

### **Issue**: Weak password error
**Solution**:
- Use at least 6 characters
- Include letters and numbers

## 🔧 **Firebase Console URLs**

- **Main Console**: https://console.firebase.google.com/
- **Your Project**: https://console.firebase.google.com/project/rustry-rio
- **Authentication**: https://console.firebase.google.com/project/rustry-rio/authentication
- **Firestore**: https://console.firebase.google.com/project/rustry-rio/firestore
- **Storage**: https://console.firebase.google.com/project/rustry-rio/storage

## 📋 **Checklist**

- [ ] Email/Password authentication enabled
- [ ] Firestore database created (test mode)
- [ ] Firebase Storage enabled (test mode)
- [ ] Fresh google-services.json downloaded and replaced
- [ ] Project cleaned and rebuilt
- [ ] App tested with account creation

## 🎯 **Expected Result**

After completing these steps, you should be able to:

1. ✅ Create new user accounts
2. ✅ Sign in with existing accounts
3. ✅ Navigate to the marketplace
4. ✅ View user profiles
5. ✅ Sign out successfully

## 🆘 **Still Having Issues?**

If you're still experiencing problems:

1. Check the Android Studio **Logcat** for detailed error messages
2. Verify your internet connection
3. Make sure you're using the correct Firebase project
4. Try creating a test account with a simple email like `test@test.com`

The app now provides much better error messages that will guide you to the specific issue if something is still not working correctly.

---

**Once Firebase is properly configured, the Rooster Platform will work perfectly! 🐓**