const functions = require('firebase-functions');
const admin = require('firebase-admin');
const Razorpay = require('razorpay');

admin.initializeApp();

// Initialize Razorpay with environment variables
const razorpay = new Razorpay({
  key_id: functions.config().razorpay.key_id,
  key_secret: functions.config().razorpay.key_secret,
});

/**
 * Cloud Function for Razorpay payment processing
 * Handles secure payment transactions for fowl purchases
 */
exports.processPayment = functions.https.onCall(async (data, context) => {
  // Verify authentication
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
  }

  const { amount, currency = 'INR', fowlId, buyerId, sellerId } = data;

  try {
    // Validate input
    if (!amount || !fowlId || !buyerId || !sellerId) {
      throw new functions.https.HttpsError('invalid-argument', 'Missing required payment data');
    }

    // Create Razorpay order
    const order = await razorpay.orders.create({
      amount: amount * 100, // Convert to paise
      currency: currency,
      receipt: `fowl_${fowlId}_${Date.now()}`,
      notes: {
        fowlId: fowlId,
        buyerId: buyerId,
        sellerId: sellerId,
        timestamp: Date.now()
      }
    });

    // Log payment initiation
    await admin.firestore().collection('payment_logs').add({
      orderId: order.id,
      fowlId: fowlId,
      buyerId: buyerId,
      sellerId: sellerId,
      amount: amount,
      currency: currency,
      status: 'initiated',
      timestamp: admin.firestore.FieldValue.serverTimestamp()
    });

    return {
      success: true,
      orderId: order.id,
      amount: order.amount,
      currency: order.currency,
      key: functions.config().razorpay.key_id
    };

  } catch (error) {
    console.error('Payment processing error:', error);
    throw new functions.https.HttpsError('internal', 'Payment processing failed');
  }
});

/**
 * Cloud Function for payment verification
 * Verifies Razorpay payment signature and updates transaction status
 */
exports.verifyPayment = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
  }

  const { razorpay_order_id, razorpay_payment_id, razorpay_signature, fowlId } = data;

  try {
    // Verify payment signature
    const crypto = require('crypto');
    const expectedSignature = crypto
      .createHmac('sha256', functions.config().razorpay.key_secret)
      .update(razorpay_order_id + '|' + razorpay_payment_id)
      .digest('hex');

    if (expectedSignature !== razorpay_signature) {
      throw new functions.https.HttpsError('invalid-argument', 'Invalid payment signature');
    }

    // Update payment log
    const paymentQuery = await admin.firestore()
      .collection('payment_logs')
      .where('orderId', '==', razorpay_order_id)
      .get();

    if (paymentQuery.empty) {
      throw new functions.https.HttpsError('not-found', 'Payment record not found');
    }

    const paymentDoc = paymentQuery.docs[0];
    await paymentDoc.ref.update({
      paymentId: razorpay_payment_id,
      signature: razorpay_signature,
      status: 'completed',
      verifiedAt: admin.firestore.FieldValue.serverTimestamp()
    });

    // Update fowl availability
    await admin.firestore().collection('fowls').doc(fowlId).update({
      isAvailable: false,
      soldAt: admin.firestore.FieldValue.serverTimestamp(),
      paymentId: razorpay_payment_id
    });

    // Create transfer record
    const paymentData = paymentDoc.data();
    await admin.firestore().collection('transfers').add({
      fowlId: fowlId,
      fromUserId: paymentData.sellerId,
      toUserId: paymentData.buyerId,
      amount: paymentData.amount,
      paymentId: razorpay_payment_id,
      status: 'completed',
      timestamp: admin.firestore.FieldValue.serverTimestamp()
    });

    return {
      success: true,
      message: 'Payment verified successfully'
    };

  } catch (error) {
    console.error('Payment verification error:', error);
    throw new functions.https.HttpsError('internal', 'Payment verification failed');
  }
});

/**
 * Cloud Function for traceability enforcement
 * Enforces business rules for fowl traceability and limits
 */
exports.enforceTraceability = functions.firestore
  .document('fowls/{fowlId}')
  .onCreate(async (snap, context) => {
    const fowl = snap.data();
    const fowlId = context.params.fowlId;

    try {
      // Check if fowl is non-traceable and enforce limits
      if (!fowl.isTraceable) {
        const nonTraceableQuery = await admin.firestore()
          .collection('fowls')
          .where('ownerId', '==', fowl.ownerId)
          .where('isTraceable', '==', false)
          .get();

        const nonTraceableCount = nonTraceableQuery.size;

        // Enforce limit of 5 non-traceable fowls per user
        if (nonTraceableCount > 5) {
          // Delete the fowl that exceeds the limit
          await snap.ref.delete();
          
          // Log the violation
          await admin.firestore().collection('traceability_violations').add({
            userId: fowl.ownerId,
            fowlId: fowlId,
            violation: 'non_traceable_limit_exceeded',
            count: nonTraceableCount,
            timestamp: admin.firestore.FieldValue.serverTimestamp()
          });

          throw new Error(`Non-traceable fowl limit exceeded. User ${fowl.ownerId} has ${nonTraceableCount} non-traceable fowls.`);
        }
      }

      // Validate parent IDs for traceable fowls
      if (fowl.isTraceable && fowl.parentIds && fowl.parentIds.length > 0) {
        const parentChecks = fowl.parentIds.map(async (parentId) => {
          const parentDoc = await admin.firestore().collection('fowls').doc(parentId).get();
          if (!parentDoc.exists) {
            throw new Error(`Parent fowl ${parentId} does not exist`);
          }
          return parentDoc.data();
        });

        await Promise.all(parentChecks);

        // Update traceability data in Realtime Database
        await admin.database().ref(`traceability/${fowlId}`).set({
          parentIds: fowl.parentIds,
          ownerId: fowl.ownerId,
          timestamp: Date.now(),
          verified: fowl.isVerified || false,
          generation: await calculateGeneration(fowl.parentIds)
        });
      }

      // Log successful creation
      console.log(`Fowl ${fowlId} created successfully with traceability checks passed`);

    } catch (error) {
      console.error(`Traceability enforcement failed for fowl ${fowlId}:`, error);
      
      // Log the error but don't fail the function completely
      await admin.firestore().collection('function_errors').add({
        function: 'enforceTraceability',
        fowlId: fowlId,
        error: error.message,
        timestamp: admin.firestore.FieldValue.serverTimestamp()
      });
    }
  });

/**
 * Helper function to calculate generation level for traceability
 */
async function calculateGeneration(parentIds) {
  if (!parentIds || parentIds.length === 0) {
    return 1; // First generation
  }

  try {
    const parentGenerations = await Promise.all(
      parentIds.map(async (parentId) => {
        const traceabilityData = await admin.database().ref(`traceability/${parentId}`).once('value');
        const data = traceabilityData.val();
        return data ? (data.generation || 1) : 1;
      })
    );

    return Math.max(...parentGenerations) + 1;
  } catch (error) {
    console.error('Error calculating generation:', error);
    return 1; // Default to first generation on error
  }
}

/**
 * Cloud Function for sending notifications
 * Handles FCM notifications for various app events
 */
exports.sendNotification = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
  }

  const { userId, title, body, data: notificationData, type } = data;

  try {
    // Get user's FCM token
    const userDoc = await admin.firestore().collection('users').doc(userId).get();
    if (!userDoc.exists) {
      throw new functions.https.HttpsError('not-found', 'User not found');
    }

    const userData = userDoc.data();
    const fcmToken = userData.fcmToken;

    if (!fcmToken) {
      throw new functions.https.HttpsError('failed-precondition', 'User has no FCM token');
    }

    // Send notification
    const message = {
      token: fcmToken,
      notification: {
        title: title,
        body: body
      },
      data: {
        type: type || 'general',
        ...notificationData
      },
      android: {
        priority: 'high',
        notification: {
          sound: 'default',
          channelId: 'rustry_notifications'
        }
      }
    };

    const response = await admin.messaging().send(message);

    // Log notification
    await admin.firestore().collection('notification_logs').add({
      userId: userId,
      title: title,
      body: body,
      type: type,
      messageId: response,
      timestamp: admin.firestore.FieldValue.serverTimestamp()
    });

    return {
      success: true,
      messageId: response
    };

  } catch (error) {
    console.error('Notification sending error:', error);
    throw new functions.https.HttpsError('internal', 'Failed to send notification');
  }
});

/**
 * Cloud Function for marketplace analytics
 * Generates real-time analytics for marketplace activity
 */
exports.updateMarketplaceAnalytics = functions.firestore
  .document('fowls/{fowlId}')
  .onWrite(async (change, context) => {
    const fowlId = context.params.fowlId;

    try {
      const before = change.before.exists ? change.before.data() : null;
      const after = change.after.exists ? change.after.data() : null;

      // Handle fowl creation
      if (!before && after) {
        await incrementAnalytics('fowls_created', after.breed, after.location);
        if (after.isForSale) {
          await incrementAnalytics('fowls_listed', after.breed, after.location);
        }
      }

      // Handle fowl updates
      if (before && after) {
        // Check if fowl was sold
        if (before.isAvailable && !after.isAvailable) {
          await incrementAnalytics('fowls_sold', after.breed, after.location);
          await updatePriceAnalytics(after.breed, after.price);
        }

        // Check if fowl was listed for sale
        if (!before.isForSale && after.isForSale) {
          await incrementAnalytics('fowls_listed', after.breed, after.location);
        }
      }

      // Handle fowl deletion
      if (before && !after) {
        await incrementAnalytics('fowls_deleted', before.breed, before.location);
      }

    } catch (error) {
      console.error('Analytics update error:', error);
    }
  });

/**
 * Helper function to increment analytics counters
 */
async function incrementAnalytics(metric, breed, location) {
  const analyticsRef = admin.firestore().collection('analytics').doc('marketplace');
  
  await analyticsRef.set({
    [`${metric}.total`]: admin.firestore.FieldValue.increment(1),
    [`${metric}.by_breed.${breed}`]: admin.firestore.FieldValue.increment(1),
    [`${metric}.by_location.${location}`]: admin.firestore.FieldValue.increment(1),
    lastUpdated: admin.firestore.FieldValue.serverTimestamp()
  }, { merge: true });
}

/**
 * Helper function to update price analytics
 */
async function updatePriceAnalytics(breed, price) {
  const priceRef = admin.firestore().collection('analytics').doc('prices');
  
  await priceRef.set({
    [`by_breed.${breed}.total_sales`]: admin.firestore.FieldValue.increment(1),
    [`by_breed.${breed}.total_value`]: admin.firestore.FieldValue.increment(price),
    [`by_breed.${breed}.last_sale_price`]: price,
    [`by_breed.${breed}.last_updated`]: admin.firestore.FieldValue.serverTimestamp()
  }, { merge: true });
}

/**
 * Scheduled function to clean up old data
 * Runs daily to maintain database performance
 */
exports.cleanupOldData = functions.pubsub.schedule('0 2 * * *')
  .timeZone('Asia/Kolkata')
  .onRun(async (context) => {
    const cutoffTime = Date.now() - (30 * 24 * 60 * 60 * 1000); // 30 days ago

    try {
      // Clean up old payment logs
      const oldPaymentLogs = await admin.firestore()
        .collection('payment_logs')
        .where('timestamp', '<', new Date(cutoffTime))
        .get();

      const paymentBatch = admin.firestore().batch();
      oldPaymentLogs.docs.forEach(doc => {
        paymentBatch.delete(doc.ref);
      });
      await paymentBatch.commit();

      // Clean up old notification logs
      const oldNotificationLogs = await admin.firestore()
        .collection('notification_logs')
        .where('timestamp', '<', new Date(cutoffTime))
        .get();

      const notificationBatch = admin.firestore().batch();
      oldNotificationLogs.docs.forEach(doc => {
        notificationBatch.delete(doc.ref);
      });
      await notificationBatch.commit();

      console.log(`Cleanup completed: ${oldPaymentLogs.size} payment logs, ${oldNotificationLogs.size} notification logs deleted`);

    } catch (error) {
      console.error('Cleanup error:', error);
    }
  });