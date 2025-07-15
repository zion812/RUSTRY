// generated/phase3/firebase/functions/deleteUserData.js

const functions = require('firebase-functions');
const admin = require('firebase-admin');

// Initialize Firebase Admin if not already initialized
if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();

/**
 * Enhanced GDPR-compliant user data deletion function
 * Extends Phase 1 implementation to include Phase 3 data
 */
exports.deleteUserData = functions.https.onCall(async (data, context) => {
  try {
    // Verify user authentication
    if (!context.auth) {
      throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
    }

    const userId = context.auth.uid;
    const { confirmDeletion } = data;

    if (!confirmDeletion) {
      throw new functions.https.HttpsError('invalid-argument', 'User must confirm deletion');
    }

    console.log(`Starting GDPR deletion for user: ${userId}`);

    // Collections to delete from (Phase 1, 2, and 3)
    const collectionsToDelete = [
      'users',
      'fowls',
      'orders',
      'payments',
      'posts',
      'chats',
      'transfers',           // Phase 3
      'vaccination_events',  // Phase 3
      'breeding_events',     // Phase 3
      'analytics_events'     // Phase 3
    ];

    const deletionResults = {};

    // Delete user data from each collection
    for (const collectionName of collectionsToDelete) {
      try {
        const deletedCount = await deleteUserDataFromCollection(collectionName, userId);
        deletionResults[collectionName] = deletedCount;
        console.log(`Deleted ${deletedCount} documents from ${collectionName}`);
      } catch (error) {
        console.error(`Error deleting from ${collectionName}:`, error);
        deletionResults[collectionName] = { error: error.message };
      }
    }

    // Delete user's storage files
    try {
      const deletedFiles = await deleteUserStorageFiles(userId);
      deletionResults.storage = deletedFiles;
      console.log(`Deleted ${deletedFiles} storage files`);
    } catch (error) {
      console.error('Error deleting storage files:', error);
      deletionResults.storage = { error: error.message };
    }

    // Delete user authentication record
    try {
      await admin.auth().deleteUser(userId);
      deletionResults.auth = 'deleted';
      console.log('Deleted user authentication record');
    } catch (error) {
      console.error('Error deleting auth record:', error);
      deletionResults.auth = { error: error.message };
    }

    // Log deletion event for compliance
    await logDeletionEvent(userId, deletionResults);

    return {
      success: true,
      userId: userId,
      deletionResults: deletionResults,
      timestamp: new Date().toISOString()
    };

  } catch (error) {
    console.error('Error in deleteUserData:', error);
    
    if (error instanceof functions.https.HttpsError) {
      throw error;
    }
    
    throw new functions.https.HttpsError('internal', 'Internal server error');
  }
});

/**
 * Delete user data from a specific collection
 */
async function deleteUserDataFromCollection(collectionName, userId) {
  let query;
  
  // Different collections have different user ID field names
  switch (collectionName) {
    case 'users':
      query = db.collection(collectionName).where(admin.firestore.FieldPath.documentId(), '==', userId);
      break;
    case 'fowls':
    case 'posts':
      query = db.collection(collectionName).where('ownerId', '==', userId);
      break;
    case 'orders':
    case 'payments':
      query = db.collection(collectionName).where('userId', '==', userId);
      break;
    case 'chats':
      query = db.collection(collectionName).where('participants', 'array-contains', userId);
      break;
    case 'transfers':
      // Delete transfers where user is sender or receiver
      const senderQuery = db.collection(collectionName).where('fromUid', '==', userId);
      const receiverQuery = db.collection(collectionName).where('toUid', '==', userId);
      
      const [senderSnapshot, receiverSnapshot] = await Promise.all([
        senderQuery.get(),
        receiverQuery.get()
      ]);
      
      const batch = db.batch();
      let count = 0;
      
      senderSnapshot.docs.forEach(doc => {
        batch.delete(doc.ref);
        count++;
      });
      
      receiverSnapshot.docs.forEach(doc => {
        batch.delete(doc.ref);
        count++;
      });
      
      if (count > 0) {
        await batch.commit();
      }
      
      return count;
    case 'vaccination_events':
    case 'breeding_events':
      // These are linked to fowls owned by the user
      const userFowlsSnapshot = await db.collection('fowls').where('ownerId', '==', userId).get();
      const fowlIds = userFowlsSnapshot.docs.map(doc => doc.id);
      
      if (fowlIds.length === 0) {
        return 0;
      }
      
      // Delete events for user's fowls in batches
      let totalDeleted = 0;
      for (let i = 0; i < fowlIds.length; i += 10) { // Firestore 'in' query limit is 10
        const batchFowlIds = fowlIds.slice(i, i + 10);
        const eventsSnapshot = await db.collection(collectionName)
          .where('fowlId', 'in', batchFowlIds)
          .get();
        
        if (!eventsSnapshot.empty) {
          const deleteBatch = db.batch();
          eventsSnapshot.docs.forEach(doc => {
            deleteBatch.delete(doc.ref);
          });
          await deleteBatch.commit();
          totalDeleted += eventsSnapshot.size;
        }
      }
      
      return totalDeleted;
    case 'analytics_events':
      // Delete analytics events that contain user ID
      query = db.collection(collectionName).where('eventData.userId', '==', userId);
      break;
    default:
      query = db.collection(collectionName).where('userId', '==', userId);
  }

  if (!query) {
    return 0;
  }

  const snapshot = await query.get();
  
  if (snapshot.empty) {
    return 0;
  }

  // Delete in batches
  const batch = db.batch();
  snapshot.docs.forEach(doc => {
    batch.delete(doc.ref);
  });
  
  await batch.commit();
  return snapshot.size;
}

/**
 * Delete user's files from Firebase Storage
 */
async function deleteUserStorageFiles(userId) {
  try {
    const bucket = admin.storage().bucket();
    
    // List all files in user's directory
    const [files] = await bucket.getFiles({
      prefix: `users/${userId}/`
    });

    if (files.length === 0) {
      return 0;
    }

    // Delete files in batches
    const deletePromises = files.map(file => file.delete());
    await Promise.all(deletePromises);

    return files.length;
  } catch (error) {
    console.error('Error deleting storage files:', error);
    throw error;
  }
}

/**
 * Log deletion event for compliance audit trail
 */
async function logDeletionEvent(userId, deletionResults) {
  try {
    await db.collection('gdpr_deletions').add({
      userId: userId,
      deletionResults: deletionResults,
      timestamp: admin.firestore.FieldValue.serverTimestamp(),
      requestedBy: 'user',
      compliance: 'GDPR Article 17 - Right to Erasure'
    });
  } catch (error) {
    console.error('Error logging deletion event:', error);
    // Don't throw error as logging failure shouldn't fail the deletion
  }
}

/**
 * Generate user data export for GDPR compliance
 */
exports.exportUserData = functions.https.onCall(async (data, context) => {
  try {
    if (!context.auth) {
      throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
    }

    const userId = context.auth.uid;
    console.log(`Starting data export for user: ${userId}`);

    const exportData = {};

    // Export user profile
    const userDoc = await db.collection('users').doc(userId).get();
    if (userDoc.exists) {
      exportData.profile = userDoc.data();
    }

    // Export fowls
    const fowlsSnapshot = await db.collection('fowls').where('ownerId', '==', userId).get();
    exportData.fowls = fowlsSnapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));

    // Export orders
    const ordersSnapshot = await db.collection('orders').where('userId', '==', userId).get();
    exportData.orders = ordersSnapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));

    // Export transfers
    const transfersSnapshot = await db.collection('transfers')
      .where('fromUid', '==', userId)
      .get();
    const receivedTransfersSnapshot = await db.collection('transfers')
      .where('toUid', '==', userId)
      .get();
    
    exportData.transfers = {
      sent: transfersSnapshot.docs.map(doc => ({ id: doc.id, ...doc.data() })),
      received: receivedTransfersSnapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }))
    };

    // Export vaccination events for user's fowls
    if (exportData.fowls.length > 0) {
      const fowlIds = exportData.fowls.map(fowl => fowl.id);
      const vaccinationEvents = [];
      
      for (let i = 0; i < fowlIds.length; i += 10) {
        const batchFowlIds = fowlIds.slice(i, i + 10);
        const eventsSnapshot = await db.collection('vaccination_events')
          .where('fowlId', 'in', batchFowlIds)
          .get();
        
        eventsSnapshot.docs.forEach(doc => {
          vaccinationEvents.push({ id: doc.id, ...doc.data() });
        });
      }
      
      exportData.vaccinationEvents = vaccinationEvents;
    }

    // Create export file in Cloud Storage
    const bucket = admin.storage().bucket();
    const fileName = `exports/user_${userId}_${Date.now()}.json`;
    const file = bucket.file(fileName);

    await file.save(JSON.stringify(exportData, null, 2), {
      metadata: {
        contentType: 'application/json'
      }
    });

    // Generate signed URL for download (valid for 1 hour)
    const [signedUrl] = await file.getSignedUrl({
      action: 'read',
      expires: Date.now() + 60 * 60 * 1000 // 1 hour
    });

    // Log export event
    await db.collection('gdpr_exports').add({
      userId: userId,
      fileName: fileName,
      timestamp: admin.firestore.FieldValue.serverTimestamp(),
      recordCount: {
        fowls: exportData.fowls?.length || 0,
        orders: exportData.orders?.length || 0,
        transfers: (exportData.transfers?.sent?.length || 0) + (exportData.transfers?.received?.length || 0),
        vaccinationEvents: exportData.vaccinationEvents?.length || 0
      }
    });

    return {
      success: true,
      downloadUrl: signedUrl,
      fileName: fileName,
      expiresAt: new Date(Date.now() + 60 * 60 * 1000).toISOString()
    };

  } catch (error) {
    console.error('Error exporting user data:', error);
    throw new functions.https.HttpsError('internal', 'Failed to export user data');
  }
});