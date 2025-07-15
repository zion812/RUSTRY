// generated/phase2/app/src/main/java/com/rio/rustry/health/EnhancedAIHealthService.kt

package com.rio.rustry.health

import android.content.Context
import com.rio.rustry.data.model.AgeGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnhancedAIHealthService @Inject constructor(
    private val context: Context
) {

    private var interpreter: Interpreter? = null

    init {
        loadModel()
    }

    private fun loadModel() {
        try {
            val modelBuffer = loadModelFile("fowl_health_model.tflite")
            interpreter = Interpreter(modelBuffer)
        } catch (e: Exception) {
            // Fallback to rule-based recommendations if model loading fails
            interpreter = null
        }
    }

    private fun loadModelFile(filename: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(filename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun getHealthRecommendation(
        ageGroup: AgeGroup,
        weight: Float,
        symptoms: List<String>
    ): Flow<String> = flow {
        try {
            val recommendation = if (interpreter != null) {
                getMLRecommendation(ageGroup, weight, symptoms)
            } else {
                getRuleBasedRecommendation(ageGroup, weight, symptoms)
            }
            emit(recommendation)
        } catch (e: Exception) {
            emit("Unable to generate health recommendation at this time. Please consult a veterinarian.")
        }
    }

    private fun getMLRecommendation(
        ageGroup: AgeGroup,
        weight: Float,
        symptoms: List<String>
    ): String {
        val input = prepareInput(ageGroup, weight, symptoms)
        val output = Array(1) { FloatArray(1) }
        
        interpreter?.run(input, output)
        
        val confidence = output[0][0]
        
        return when {
            confidence > 0.8 -> "High priority: Immediate veterinary attention recommended. Symptoms indicate potential serious condition."
            confidence > 0.6 -> "Medium priority: Schedule veterinary check-up within 24-48 hours."
            confidence > 0.4 -> "Low priority: Monitor symptoms and consider veterinary consultation if they persist."
            else -> "Symptoms appear normal for age group. Continue regular care routine."
        }
    }

    private fun getRuleBasedRecommendation(
        ageGroup: AgeGroup,
        weight: Float,
        symptoms: List<String>
    ): String {
        val criticalSymptoms = listOf("difficulty breathing", "severe lethargy", "blood in stool", "seizures")
        val moderateSymptoms = listOf("loss of appetite", "diarrhea", "coughing", "limping")
        
        return when {
            symptoms.any { it.lowercase() in criticalSymptoms } -> {
                "URGENT: Critical symptoms detected. Seek immediate veterinary care."
            }
            symptoms.any { it.lowercase() in moderateSymptoms } -> {
                "Moderate concern: Schedule veterinary check-up within 24-48 hours. Monitor closely."
            }
            weight < getMinWeightForAge(ageGroup) -> {
                "Weight concern: Fowl appears underweight for age group. Consider nutritional assessment."
            }
            weight > getMaxWeightForAge(ageGroup) -> {
                "Weight concern: Fowl may be overweight. Consider dietary adjustment and exercise."
            }
            else -> {
                "Health status appears normal. Continue regular care and monitoring routine."
            }
        }
    }

    private fun prepareInput(ageGroup: AgeGroup, weight: Float, symptoms: List<String>): Array<FloatArray> {
        // Convert inputs to normalized float array for ML model
        val ageValue = when (ageGroup) {
            AgeGroup.CHICK -> 0.1f
            AgeGroup.JUVENILE -> 0.3f
            AgeGroup.YOUNG_ADULT -> 0.6f
            AgeGroup.ADULT -> 0.8f
            AgeGroup.SENIOR -> 1.0f
        }
        
        val normalizedWeight = weight / 10.0f // Normalize weight
        
        // Simple symptom encoding (in real implementation, use proper feature engineering)
        val symptomScore = symptoms.size / 10.0f
        
        return arrayOf(floatArrayOf(ageValue, normalizedWeight, symptomScore))
    }

    private fun getMinWeightForAge(ageGroup: AgeGroup): Float {
        return when (ageGroup) {
            AgeGroup.CHICK -> 0.1f
            AgeGroup.JUVENILE -> 0.5f
            AgeGroup.YOUNG_ADULT -> 1.0f
            AgeGroup.ADULT -> 1.5f
            AgeGroup.SENIOR -> 1.2f
        }
    }

    private fun getMaxWeightForAge(ageGroup: AgeGroup): Float {
        return when (ageGroup) {
            AgeGroup.CHICK -> 0.3f
            AgeGroup.JUVENILE -> 1.0f
            AgeGroup.YOUNG_ADULT -> 2.5f
            AgeGroup.ADULT -> 4.0f
            AgeGroup.SENIOR -> 3.5f
        }
    }

    fun getVaccinationReminder(ageGroup: AgeGroup): String {
        return when (ageGroup) {
            AgeGroup.CHICK -> "Due: First vaccination series (1-2 weeks old)"
            AgeGroup.JUVENILE -> "Due: Second vaccination booster (4-6 weeks old)"
            AgeGroup.YOUNG_ADULT -> "Due: Annual vaccination update"
            AgeGroup.ADULT -> "Due: Annual health check and vaccination"
            AgeGroup.SENIOR -> "Due: Bi-annual senior health assessment"
        }
    }
}