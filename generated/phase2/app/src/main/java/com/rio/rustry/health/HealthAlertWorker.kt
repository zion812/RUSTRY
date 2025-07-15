// generated/phase2/app/src/main/java/com/rio/rustry/health/HealthAlertWorker.kt

package com.rio.rustry.health

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rio.rustry.R
import com.rio.rustry.data.model.Fowl
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

@HiltWorker
class HealthAlertWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val healthService: EnhancedAIHealthService
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            checkVaccinationSchedule()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun checkVaccinationSchedule() {
        val currentUser = auth.currentUser ?: return
        
        // Get user's fowls
        val snapshot = firestore.collection("fowls")
            .whereEqualTo("ownerUid", currentUser.uid)
            .whereEqualTo("isActive", true)
            .get()
            .await()
        
        val fowls = snapshot.documents.mapNotNull { doc ->
            doc.toObject(Fowl::class.java)?.copy(id = doc.id)
        }
        
        // Check each fowl for vaccination needs
        fowls.forEach { fowl ->
            val daysSinceLastVaccination = calculateDaysSinceLastVaccination(fowl)
            val vaccinationDue = isVaccinationDue(fowl.ageGroup, daysSinceLastVaccination)
            
            if (vaccinationDue) {
                val reminder = healthService.getVaccinationReminder(fowl.ageGroup)
                sendVaccinationNotification(fowl, reminder)
            }
        }
    }

    private fun calculateDaysSinceLastVaccination(fowl: Fowl): Int {
        // In a real implementation, this would check vaccination records
        // For now, use creation date as a proxy
        val daysSinceCreation = (System.currentTimeMillis() - fowl.createdAt) / (24 * 60 * 60 * 1000)
        return daysSinceCreation.toInt()
    }

    private fun isVaccinationDue(ageGroup: com.rio.rustry.data.model.AgeGroup, daysSinceLastVaccination: Int): Boolean {
        return when (ageGroup) {
            com.rio.rustry.data.model.AgeGroup.CHICK -> daysSinceLastVaccination >= 7 // Weekly for chicks
            com.rio.rustry.data.model.AgeGroup.JUVENILE -> daysSinceLastVaccination >= 30 // Monthly for juveniles
            com.rio.rustry.data.model.AgeGroup.YOUNG_ADULT -> daysSinceLastVaccination >= 90 // Quarterly
            com.rio.rustry.data.model.AgeGroup.ADULT -> daysSinceLastVaccination >= 365 // Annually
            com.rio.rustry.data.model.AgeGroup.SENIOR -> daysSinceLastVaccination >= 180 // Bi-annually for seniors
        }
    }

    private fun sendVaccinationNotification(fowl: Fowl, reminder: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Health Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Vaccination and health reminders for your fowls"
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Vaccination Reminder")
            .setContentText("${fowl.breed.displayName}: $reminder")
            .setStyle(NotificationCompat.BigTextStyle().bigText(reminder))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(fowl.id.hashCode(), notification)
    }

    companion object {
        private const val CHANNEL_ID = "health_alerts"
        private const val WORK_NAME = "health_alert_worker"

        fun schedulePeriodicWork(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<HealthAlertWorker>(
                1, TimeUnit.DAYS // Check daily
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
        }
    }
}