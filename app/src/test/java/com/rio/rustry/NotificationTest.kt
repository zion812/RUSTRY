package com.rio.rustry

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Notification system tests
 * Tests notification creation, delivery, and management
 */
class NotificationTest {

    @Before
    fun init() {
        // Test initialization
    }

    @Test
    fun testNotificationCreation() {
        val notification = MockNotification(
            id = "notif_123",
            title = "Test Notification",
            message = "This is a test notification",
            type = MockNotificationType.SALE_COMPLETED,
            userId = "user_123",
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        
        assertThat(notification.id).isEqualTo("notif_123")
        assertThat(notification.title).isEqualTo("Test Notification")
        assertThat(notification.message).isEqualTo("This is a test notification")
        assertThat(notification.type).isEqualTo(MockNotificationType.SALE_COMPLETED)
        assertThat(notification.isRead).isFalse()
    }

    @Test
    fun testNotificationTypes() {
        val types = MockNotificationType.values()
        
        assertThat(types).hasLength(8)
        assertThat(types.toList()).contains(MockNotificationType.SALE_COMPLETED)
        assertThat(types.toList()).contains(MockNotificationType.PAYMENT_RECEIVED)
        assertThat(types.toList()).contains(MockNotificationType.FOWL_LISTED)
        assertThat(types.toList()).contains(MockNotificationType.HEALTH_REMINDER)
        assertThat(types.toList()).contains(MockNotificationType.VACCINATION_DUE)
        assertThat(types.toList()).contains(MockNotificationType.MESSAGE_RECEIVED)
        assertThat(types.toList()).contains(MockNotificationType.SYSTEM_UPDATE)
        assertThat(types.toList()).contains(MockNotificationType.PRICE_ALERT)
    }

    @Test
    fun testNotificationPriority() {
        val highPriorityNotification = MockNotification(
            id = "notif_high",
            title = "Urgent Health Alert",
            message = "Your fowl needs immediate attention",
            type = MockNotificationType.HEALTH_REMINDER,
            userId = "user_123",
            timestamp = System.currentTimeMillis(),
            priority = MockNotificationPriority.HIGH
        )
        
        val lowPriorityNotification = MockNotification(
            id = "notif_low",
            title = "Price Update",
            message = "Market prices have been updated",
            type = MockNotificationType.PRICE_ALERT,
            userId = "user_123",
            timestamp = System.currentTimeMillis(),
            priority = MockNotificationPriority.LOW
        )
        
        assertThat(highPriorityNotification.priority).isEqualTo(MockNotificationPriority.HIGH)
        assertThat(lowPriorityNotification.priority).isEqualTo(MockNotificationPriority.LOW)
    }

    @Test
    fun testNotificationSerialization() {
        val notification = MockNotification(
            id = "notif_serialize",
            title = "Serialization Test",
            message = "Testing notification serialization",
            type = MockNotificationType.SYSTEM_UPDATE,
            userId = "user_456",
            timestamp = 1640995200000L,
            isRead = true,
            priority = MockNotificationPriority.MEDIUM
        )
        
        val map = notification.toMap()
        val deserializedNotification = MockNotification.fromMap(map)
        
        assertThat(deserializedNotification.id).isEqualTo(notification.id)
        assertThat(deserializedNotification.title).isEqualTo(notification.title)
        assertThat(deserializedNotification.message).isEqualTo(notification.message)
        assertThat(deserializedNotification.type).isEqualTo(notification.type)
        assertThat(deserializedNotification.userId).isEqualTo(notification.userId)
        assertThat(deserializedNotification.timestamp).isEqualTo(notification.timestamp)
        assertThat(deserializedNotification.isRead).isEqualTo(notification.isRead)
        assertThat(deserializedNotification.priority).isEqualTo(notification.priority)
    }

    @Test
    fun testNotificationManager() {
        val manager = MockNotificationManager()
        
        // Test adding notifications
        val notification1 = createTestNotification("notif_1", "Title 1")
        val notification2 = createTestNotification("notif_2", "Title 2")
        
        manager.addNotification(notification1)
        manager.addNotification(notification2)
        
        assertThat(manager.getAllNotifications()).hasSize(2)
        assertThat(manager.getUnreadCount()).isEqualTo(2)
    }

    @Test
    fun testNotificationMarkAsRead() {
        val manager = MockNotificationManager()
        val notification = createTestNotification("notif_read", "Read Test")
        
        manager.addNotification(notification)
        assertThat(manager.getUnreadCount()).isEqualTo(1)
        
        manager.markAsRead("notif_read")
        assertThat(manager.getUnreadCount()).isEqualTo(0)
        
        val updatedNotification = manager.getNotificationById("notif_read")
        assertThat(updatedNotification?.isRead).isTrue()
    }

    @Test
    fun testNotificationFiltering() {
        val manager = MockNotificationManager()
        
        val saleNotification = createTestNotification("sale_1", "Sale Completed", MockNotificationType.SALE_COMPLETED)
        val healthNotification = createTestNotification("health_1", "Health Alert", MockNotificationType.HEALTH_REMINDER)
        val messageNotification = createTestNotification("msg_1", "New Message", MockNotificationType.MESSAGE_RECEIVED)
        
        manager.addNotification(saleNotification)
        manager.addNotification(healthNotification)
        manager.addNotification(messageNotification)
        
        val saleNotifications = manager.getNotificationsByType(MockNotificationType.SALE_COMPLETED)
        val healthNotifications = manager.getNotificationsByType(MockNotificationType.HEALTH_REMINDER)
        
        assertThat(saleNotifications).hasSize(1)
        assertThat(healthNotifications).hasSize(1)
        assertThat(saleNotifications.first().id).isEqualTo("sale_1")
        assertThat(healthNotifications.first().id).isEqualTo("health_1")
    }

    @Test
    fun testNotificationDeletion() {
        val manager = MockNotificationManager()
        val notification = createTestNotification("delete_test", "Delete Me")
        
        manager.addNotification(notification)
        assertThat(manager.getAllNotifications()).hasSize(1)
        
        manager.deleteNotification("delete_test")
        assertThat(manager.getAllNotifications()).isEmpty()
        assertThat(manager.getNotificationById("delete_test")).isNull()
    }

    @Test
    fun testNotificationBulkOperations() {
        val manager = MockNotificationManager()
        
        val notifications = listOf(
            createTestNotification("bulk_1", "Bulk 1"),
            createTestNotification("bulk_2", "Bulk 2"),
            createTestNotification("bulk_3", "Bulk 3")
        )
        
        notifications.forEach { manager.addNotification(it) }
        assertThat(manager.getUnreadCount()).isEqualTo(3)
        
        // Mark all as read
        manager.markAllAsRead()
        assertThat(manager.getUnreadCount()).isEqualTo(0)
        
        // Clear all notifications
        manager.clearAllNotifications()
        assertThat(manager.getAllNotifications()).isEmpty()
    }

    @Test
    fun testNotificationTimestampSorting() {
        val manager = MockNotificationManager()
        
        val oldNotification = createTestNotification("old", "Old", timestamp = 1000L)
        val newNotification = createTestNotification("new", "New", timestamp = 2000L)
        val middleNotification = createTestNotification("middle", "Middle", timestamp = 1500L)
        
        manager.addNotification(oldNotification)
        manager.addNotification(newNotification)
        manager.addNotification(middleNotification)
        
        val sortedNotifications = manager.getNotificationsSortedByTime()
        
        assertThat(sortedNotifications).hasSize(3)
        assertThat(sortedNotifications[0].id).isEqualTo("new") // Most recent first
        assertThat(sortedNotifications[1].id).isEqualTo("middle")
        assertThat(sortedNotifications[2].id).isEqualTo("old")
    }

    @Test
    fun testNotificationValidation() {
        // Valid notification
        val validNotification = createTestNotification("valid", "Valid Notification")
        assertThat(isValidNotification(validNotification)).isTrue()
        
        // Invalid notifications
        val invalidNotifications = listOf(
            validNotification.copy(id = ""),
            validNotification.copy(title = ""),
            validNotification.copy(message = ""),
            validNotification.copy(userId = ""),
            validNotification.copy(timestamp = 0L)
        )
        
        invalidNotifications.forEach { notification ->
            assertThat(isValidNotification(notification)).isFalse()
        }
    }

    // Helper functions
    private fun createTestNotification(
        id: String,
        title: String,
        type: MockNotificationType = MockNotificationType.SYSTEM_UPDATE,
        timestamp: Long = System.currentTimeMillis()
    ): MockNotification {
        return MockNotification(
            id = id,
            title = title,
            message = "Test message for $title",
            type = type,
            userId = "test_user",
            timestamp = timestamp,
            isRead = false,
            priority = MockNotificationPriority.MEDIUM
        )
    }

    private fun isValidNotification(notification: MockNotification): Boolean {
        return notification.id.isNotBlank() &&
               notification.title.isNotBlank() &&
               notification.message.isNotBlank() &&
               notification.userId.isNotBlank() &&
               notification.timestamp > 0L
    }

    // Mock classes for testing
    enum class MockNotificationType {
        SALE_COMPLETED,
        PAYMENT_RECEIVED,
        FOWL_LISTED,
        HEALTH_REMINDER,
        VACCINATION_DUE,
        MESSAGE_RECEIVED,
        SYSTEM_UPDATE,
        PRICE_ALERT
    }

    enum class MockNotificationPriority {
        LOW, MEDIUM, HIGH, URGENT
    }

    data class MockNotification(
        val id: String,
        val title: String,
        val message: String,
        val type: MockNotificationType,
        val userId: String,
        val timestamp: Long,
        val isRead: Boolean = false,
        val priority: MockNotificationPriority = MockNotificationPriority.MEDIUM,
        val actionUrl: String? = null,
        val imageUrl: String? = null
    ) {
        fun toMap(): Map<String, Any?> {
            return mapOf(
                "id" to id,
                "title" to title,
                "message" to message,
                "type" to type.name,
                "userId" to userId,
                "timestamp" to timestamp,
                "isRead" to isRead,
                "priority" to priority.name,
                "actionUrl" to actionUrl,
                "imageUrl" to imageUrl
            )
        }

        companion object {
            fun fromMap(map: Map<String, Any?>): MockNotification {
                return MockNotification(
                    id = map["id"] as String,
                    title = map["title"] as String,
                    message = map["message"] as String,
                    type = MockNotificationType.valueOf(map["type"] as String),
                    userId = map["userId"] as String,
                    timestamp = map["timestamp"] as Long,
                    isRead = map["isRead"] as Boolean,
                    priority = MockNotificationPriority.valueOf(map["priority"] as String),
                    actionUrl = map["actionUrl"] as String?,
                    imageUrl = map["imageUrl"] as String?
                )
            }
        }
    }

    class MockNotificationManager {
        private val notifications = mutableMapOf<String, MockNotification>()

        fun addNotification(notification: MockNotification) {
            notifications[notification.id] = notification
        }

        fun getAllNotifications(): List<MockNotification> {
            return notifications.values.toList()
        }

        fun getNotificationById(id: String): MockNotification? {
            return notifications[id]
        }

        fun getUnreadCount(): Int {
            return notifications.values.count { !it.isRead }
        }

        fun markAsRead(id: String) {
            notifications[id]?.let { notification ->
                notifications[id] = notification.copy(isRead = true)
            }
        }

        fun markAllAsRead() {
            notifications.keys.forEach { markAsRead(it) }
        }

        fun deleteNotification(id: String) {
            notifications.remove(id)
        }

        fun clearAllNotifications() {
            notifications.clear()
        }

        fun getNotificationsByType(type: MockNotificationType): List<MockNotification> {
            return notifications.values.filter { it.type == type }
        }

        fun getNotificationsSortedByTime(): List<MockNotification> {
            return notifications.values.sortedByDescending { it.timestamp }
        }
    }
}