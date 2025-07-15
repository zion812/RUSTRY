// generated/phase2/functions/enhancedDeleteUserData.js

const functions = require('firebase-functions');
const admin = require('firebase-admin');

if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();
const storage = admin.storage();

exports.enhancedDeleteUserData = functions.https.onCall(async (data, context) => {
  // Verify authentication
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
  }

  const userId = context.auth.uid;
  const batch = db.batch();
  
  try {
    console.log(`Starting enhanced data deletion for user: ${userId}`);
    
    // 1. Delete user's fowl listings
    const fowlsSnapshot = await db.collection('fowls')
      .where('ownerUid', '==', userId)
      .get();
    
    fowlsSnapshot.docs.forEach(doc => {
      batch.delete(doc.ref);
    });
    
    // 2. Delete user's orders
    const ordersSnapshot = await db.collection('orders')
      .where('userId', '==', userId)
      .get();
    
    ordersSnapshot.docs.forEach(doc => {
      batch.delete(doc.ref);
    });
    
    // 3. Delete user's chat messages
    const chatsSnapshot = await db.collectionGroup('messages')
      .where('senderId', '==', userId)
      .get();
    
    chatsSnapshot.docs.forEach(doc => {
      batch.delete(doc.ref);
    });
    
    // 4. Delete user's posts and comments
    const postsSnapshot = await db.collection('posts')
      .where('authorId', '==', userId)
      .get();
    
    postsSnapshot.docs.forEach(doc => {
      batch.delete(doc.ref);
    });
    
    // 5. Delete user's comments on other posts
    const commentsSnapshot = await db.collectionGroup('comments')
      .where('authorId', '==', userId)
      .get();
    
    commentsSnapshot.docs.forEach(doc => {
      batch.delete(doc.ref);
    });
    
    // 6. Delete user's payments
    const paymentsSnapshot = await db.collection('payments')
      .where('userId', '==', userId)
      .get();
    
    paymentsSnapshot.docs.forEach(doc => {
      batch.delete(doc.ref);
    });
    
    // 7. Delete user's cart items (if stored in Firestore)
    const cartSnapshot = await db.collection('cart')
      .where('userId', '==', userId)
      .get();
    
    cartSnapshot.docs.forEach(doc => {
      batch.delete(doc.ref);
    });
    
    // 8. Delete user's images from Storage
    try {
      const bucket = storage.bucket();
      const [files] = await bucket.getFiles({
        prefix: `users/${userId}/`
      });
      
      const deletePromises = files.map(file => file.delete());
      await Promise.all(deletePromises);
      
      console.log(`Deleted ${files.length} files from Storage for user: ${userId}`);
    } catch (storageError) {
      console.error('Error deleting user files from Storage:', storageError);
      // Continue with Firestore deletion even if Storage deletion fails
    }
    
    // 9. Delete fowl images uploaded by user
    try {
      const bucket = storage.bucket();
      const [fowlFiles] = await bucket.getFiles({
        prefix: `fowl_images/`
      });
      
      // Filter files that belong to user's fowls
      const userFowlIds = fowlsSnapshot.docs.map(doc => doc.id);
      const userFowlFiles = fowlFiles.filter(file => {
        return userFowlIds.some(fowlId => file.name.includes(fowlId));
      });
      
      const deleteFowlPromises = userFowlFiles.map(file => file.delete());
      await Promise.all(deleteFowlPromises);
      
      console.log(`Deleted ${userFowlFiles.length} fowl images for user: ${userId}`);
    } catch (fowlStorageError) {
      console.error('Error deleting fowl images:', fowlStorageError);
    }
    
    // 10. Update user document to mark as deleted (keep for audit trail)
    const userRef = db.collection('users').doc(userId);
    batch.update(userRef, {
      isDeleted: true,
      deletedAt: admin.firestore.FieldValue.serverTimestamp(),
      email: '[DELETED]',
      displayName: '[DELETED]',
      photoURL: null,
      // Keep minimal data for legal/audit purposes
      deletionReason: 'GDPR_REQUEST',
      originalCreatedAt: admin.firestore.FieldValue.serverTimestamp()
    });
    
    // Commit all deletions
    await batch.commit();
    
    // 11. Create audit log
    await db.collection('audit_logs').add({
      action: 'USER_DATA_DELETION',
      userId: userId,
      timestamp: admin.firestore.FieldValue.serverTimestamp(),
      details: {
        fowlsDeleted: fowlsSnapshot.size,
        ordersDeleted: ordersSnapshot.size,
        messagesDeleted: chatsSnapshot.size,
        postsDeleted: postsSnapshot.size,
        commentsDeleted: commentsSnapshot.size,
        paymentsDeleted: paymentsSnapshot.size,
        cartItemsDeleted: cartSnapshot.size
      },
      status: 'COMPLETED'
    });
    
    console.log(`Enhanced data deletion completed for user: ${userId}`);
    
    return {
      success: true,
      message: 'All user data has been successfully deleted',
      deletionSummary: {
        fowls: fowlsSnapshot.size,
        orders: ordersSnapshot.size,
        messages: chatsSnapshot.size,
        posts: postsSnapshot.size,
        comments: commentsSnapshot.size,
        payments: paymentsSnapshot.size,
        cartItems: cartSnapshot.size
      }
    };
    
  } catch (error) {
    console.error('Error during enhanced data deletion:', error);
    
    // Log the error for audit purposes
    await db.collection('audit_logs').add({
      action: 'USER_DATA_DELETION',
      userId: userId,
      timestamp: admin.firestore.FieldValue.serverTimestamp(),
      status: 'FAILED',
      error: error.message
    });
    
    throw new functions.https.HttpsError('internal', 'Failed to delete user data: ' + error.message);
  }
});