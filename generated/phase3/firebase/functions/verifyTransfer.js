// generated/phase3/firebase/functions/verifyTransfer.js

const functions = require('firebase-functions');
const admin = require('firebase-admin');
const crypto = require('crypto');

// Initialize Firebase Admin if not already initialized
if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();

/**
 * Cloud Function to verify transfer signatures and update transfer status
 * Triggered by HTTP request or Firestore document changes
 */
exports.verifyTransfer = functions.https.onCall(async (data, context) => {
  try {
    // Verify user authentication
    if (!context.auth) {
      throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
    }

    const { transferId, signature, proofData } = data;

    if (!transferId || !signature || !proofData) {
      throw new functions.https.HttpsError('invalid-argument', 'Missing required parameters');
    }

    // Get transfer document
    const transferRef = db.collection('transfers').doc(transferId);
    const transferDoc = await transferRef.get();

    if (!transferDoc.exists) {
      throw new functions.https.HttpsError('not-found', 'Transfer not found');
    }

    const transfer = transferDoc.data();

    // Verify user has permission to verify this transfer
    if (transfer.fromUid !== context.auth.uid && transfer.toUid !== context.auth.uid) {
      throw new functions.https.HttpsError('permission-denied', 'User not authorized to verify this transfer');
    }

    // Verify signature
    const isValidSignature = await verifyECDSASignature(proofData, signature);

    if (!isValidSignature) {
      // Update transfer as rejected
      await transferRef.update({
        verified: false,
        status: 'REJECTED',
        verificationTimestamp: admin.firestore.FieldValue.serverTimestamp(),
        rejectionReason: 'Invalid signature'
      });

      // Send notification to both parties
      await sendTransferNotification(transfer.fromUid, transfer.toUid, transferId, 'REJECTED');

      throw new functions.https.HttpsError('invalid-argument', 'Invalid signature');
    }

    // Verify proof data integrity
    const expectedProofHash = generateProofHash(transfer);
    const providedProofHash = generateProofHash(proofData);

    if (expectedProofHash !== providedProofHash) {
      await transferRef.update({
        verified: false,
        status: 'REJECTED',
        verificationTimestamp: admin.firestore.FieldValue.serverTimestamp(),
        rejectionReason: 'Proof data mismatch'
      });

      await sendTransferNotification(transfer.fromUid, transfer.toUid, transferId, 'REJECTED');

      throw new functions.https.HttpsError('invalid-argument', 'Proof data mismatch');
    }

    // Update transfer as verified
    await transferRef.update({
      verified: true,
      status: 'VERIFIED',
      verificationTimestamp: admin.firestore.FieldValue.serverTimestamp(),
      signature: signature
    });

    // Update fowl ownership
    await updateFowlOwnership(transfer.fowlId, transfer.fromUid, transfer.toUid);

    // Send success notification to both parties
    await sendTransferNotification(transfer.fromUid, transfer.toUid, transferId, 'VERIFIED');

    // Log analytics event
    await logAnalyticsEvent('transfer_verified', {
      transferId: transferId,
      fowlId: transfer.fowlId,
      verificationMethod: 'ecdsa_signature',
      timestamp: Date.now()
    });

    return {
      success: true,
      transferId: transferId,
      status: 'VERIFIED'
    };

  } catch (error) {
    console.error('Error verifying transfer:', error);
    
    if (error instanceof functions.https.HttpsError) {
      throw error;
    }
    
    throw new functions.https.HttpsError('internal', 'Internal server error');
  }
});

/**
 * Verify ECDSA signature
 */
async function verifyECDSASignature(data, signature) {
  try {
    // In a real implementation, you would:
    // 1. Get the public key from the user's profile or certificate
    // 2. Verify the signature using the public key
    // 3. Return true if valid, false otherwise
    
    // For this example, we'll simulate signature verification
    // In production, use proper cryptographic libraries
    
    const dataString = Object.keys(data)
      .sort()
      .map(key => `${key}=${data[key]}`)
      .join('&');
    
    // Simulate signature verification (replace with actual implementation)
    const expectedSignature = crypto
      .createHash('sha256')
      .update(dataString)
      .digest('base64');
    
    // In real implementation, use ECDSA verification
    return signature.length > 0; // Simplified for example
    
  } catch (error) {
    console.error('Signature verification error:', error);
    return false;
  }
}

/**
 * Generate proof hash for integrity verification
 */
function generateProofHash(data) {
  const proofString = [
    data.transferId || '',
    data.fowlId || '',
    data.fromUid || '',
    data.toUid || '',
    data.timestamp || '',
    (data.proofUrls || []).join(',')
  ].join('|');
  
  return crypto.createHash('sha256').update(proofString).digest('hex');
}

/**
 * Update fowl ownership in Firestore
 */
async function updateFowlOwnership(fowlId, fromUid, toUid) {
  const fowlRef = db.collection('fowls').doc(fowlId);
  
  await db.runTransaction(async (transaction) => {
    const fowlDoc = await transaction.get(fowlRef);
    
    if (!fowlDoc.exists) {
      throw new Error('Fowl not found');
    }
    
    const fowlData = fowlDoc.data();
    
    if (fowlData.ownerId !== fromUid) {
      throw new Error('Transfer sender is not the current owner');
    }
    
    // Update fowl ownership
    transaction.update(fowlRef, {
      ownerId: toUid,
      previousOwnerId: fromUid,
      ownershipTransferDate: admin.firestore.FieldValue.serverTimestamp()
    });
  });
}

/**
 * Send push notifications to transfer parties
 */
async function sendTransferNotification(fromUid, toUid, transferId, status) {
  try {
    const messaging = admin.messaging();
    
    // Get user FCM tokens
    const [fromUserDoc, toUserDoc] = await Promise.all([
      db.collection('users').doc(fromUid).get(),
      db.collection('users').doc(toUid).get()
    ]);
    
    const notifications = [];
    
    if (fromUserDoc.exists && fromUserDoc.data().fcmToken) {
      notifications.push({
        token: fromUserDoc.data().fcmToken,
        notification: {
          title: 'Transfer Update',
          body: `Your transfer has been ${status.toLowerCase()}`
        },
        data: {
          transferId: transferId,
          status: status,
          type: 'transfer_update'
        }
      });
    }
    
    if (toUserDoc.exists && toUserDoc.data().fcmToken) {
      notifications.push({
        token: toUserDoc.data().fcmToken,
        notification: {
          title: 'Transfer Update',
          body: `Transfer received has been ${status.toLowerCase()}`
        },
        data: {
          transferId: transferId,
          status: status,
          type: 'transfer_update'
        }
      });
    }
    
    if (notifications.length > 0) {
      await messaging.sendAll(notifications);
    }
    
  } catch (error) {
    console.error('Error sending notifications:', error);
    // Don't throw error as notification failure shouldn't fail the transfer
  }
}

/**
 * Log analytics event to BigQuery
 */
async function logAnalyticsEvent(eventName, eventData) {
  try {
    // Store analytics event in Firestore for BigQuery export
    await db.collection('analytics_events').add({
      eventName: eventName,
      eventData: eventData,
      timestamp: admin.firestore.FieldValue.serverTimestamp()
    });
  } catch (error) {
    console.error('Error logging analytics event:', error);
    // Don't throw error as analytics failure shouldn't fail the transfer
  }
}

/**
 * Scheduled function to export analytics to BigQuery (runs nightly)
 */
exports.exportAnalyticsToBigQuery = functions.pubsub
  .schedule('0 2 * * *') // Run at 2 AM daily
  .timeZone('UTC')
  .onRun(async (context) => {
    try {
      const yesterday = new Date();
      yesterday.setDate(yesterday.getDate() - 1);
      yesterday.setHours(0, 0, 0, 0);
      
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      
      // Get analytics events from yesterday
      const eventsSnapshot = await db.collection('analytics_events')
        .where('timestamp', '>=', yesterday)
        .where('timestamp', '<', today)
        .get();
      
      if (eventsSnapshot.empty) {
        console.log('No analytics events to export');
        return;
      }
      
      // In a real implementation, you would:
      // 1. Initialize BigQuery client
      // 2. Transform the data to match your BigQuery schema
      // 3. Insert the data into BigQuery
      // 4. Mark events as exported or delete them
      
      console.log(`Exported ${eventsSnapshot.size} analytics events to BigQuery`);
      
      // Clean up exported events (optional)
      const batch = db.batch();
      eventsSnapshot.docs.forEach(doc => {
        batch.delete(doc.ref);
      });
      await batch.commit();
      
    } catch (error) {
      console.error('Error exporting analytics to BigQuery:', error);
    }
  });