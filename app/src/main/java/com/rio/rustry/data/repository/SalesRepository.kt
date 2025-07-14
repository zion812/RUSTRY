package com.rio.rustry.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.rio.rustry.data.model.BuyerSummary
import com.rio.rustry.data.model.SaleRecord
import com.rio.rustry.data.model.SalesSummary
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.*

class SalesRepository(
    private val firestore: FirebaseFirestore
) {
    private val salesCollection = firestore.collection("sales_records")
    
    fun getSalesRecords(farmId: String): Flow<List<SaleRecord>> = callbackFlow {
        val listener = salesCollection
            .whereEqualTo("farmId", farmId)
            .orderBy("saleDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val records = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(SaleRecord::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                trySend(records)
            }
        
        awaitClose { listener.remove() }
    }
    
    suspend fun addSaleRecord(record: SaleRecord) {
        val recordMap = mapOf(
            "farmId" to record.farmId,
            "fowlId" to record.fowlId,
            "fowlName" to record.fowlName,
            "buyerName" to record.buyerName,
            "buyerContact" to record.buyerContact,
            "buyerEmail" to record.buyerEmail,
            "salePrice" to record.salePrice,
            "originalPrice" to record.originalPrice,
            "discount" to record.discount,
            "saleDate" to record.saleDate,
            "paymentMethod" to record.paymentMethod,
            "paymentStatus" to record.paymentStatus,
            "deliveryMethod" to record.deliveryMethod,
            "deliveryAddress" to record.deliveryAddress,
            "deliveryDate" to record.deliveryDate,
            "notes" to record.notes,
            "invoiceNumber" to record.invoiceNumber,
            "taxAmount" to record.taxAmount,
            "commission" to record.commission,
            "netAmount" to record.netAmount,
            "isVerified" to record.isVerified,
            "verificationDocuments" to record.verificationDocuments,
            "createdAt" to record.createdAt,
            "updatedAt" to record.updatedAt
        )
        
        if (record.id.isNotEmpty()) {
            salesCollection.document(record.id).set(recordMap).await()
        } else {
            salesCollection.add(recordMap).await()
        }
    }
    
    suspend fun updateSaleRecord(record: SaleRecord) {
        val recordMap = mapOf(
            "farmId" to record.farmId,
            "fowlId" to record.fowlId,
            "fowlName" to record.fowlName,
            "buyerName" to record.buyerName,
            "buyerContact" to record.buyerContact,
            "buyerEmail" to record.buyerEmail,
            "salePrice" to record.salePrice,
            "originalPrice" to record.originalPrice,
            "discount" to record.discount,
            "saleDate" to record.saleDate,
            "paymentMethod" to record.paymentMethod,
            "paymentStatus" to record.paymentStatus,
            "deliveryMethod" to record.deliveryMethod,
            "deliveryAddress" to record.deliveryAddress,
            "deliveryDate" to record.deliveryDate,
            "notes" to record.notes,
            "invoiceNumber" to record.invoiceNumber,
            "taxAmount" to record.taxAmount,
            "commission" to record.commission,
            "netAmount" to record.netAmount,
            "isVerified" to record.isVerified,
            "verificationDocuments" to record.verificationDocuments,
            "updatedAt" to Date()
        )
        
        salesCollection.document(record.id).update(recordMap).await()
    }
    
    suspend fun deleteSaleRecord(recordId: String) {
        salesCollection.document(recordId).delete().await()
    }
    
    suspend fun getSaleRecord(recordId: String): SaleRecord? {
        return try {
            val doc = salesCollection.document(recordId).get().await()
            doc.toObject(SaleRecord::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun getSalesSummary(farmId: String): SalesSummary {
        return try {
            val snapshot = salesCollection
                .whereEqualTo("farmId", farmId)
                .get()
                .await()
            
            val records = snapshot.documents.mapNotNull { doc ->
                doc.toObject(SaleRecord::class.java)?.copy(id = doc.id)
            }
            
            if (records.isEmpty()) {
                return SalesSummary()
            }
            
            val totalSales = records.size
            val totalRevenue = records.sumOf { it.salePrice }
            val averageSalePrice = totalRevenue / totalSales
            
            // Calculate monthly revenue (current month)
            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentYear = calendar.get(Calendar.YEAR)
            
            val monthlyRevenue = records.filter { record ->
                calendar.time = record.saleDate
                calendar.get(Calendar.MONTH) == currentMonth && 
                calendar.get(Calendar.YEAR) == currentYear
            }.sumOf { it.salePrice }
            
            // Calculate yearly revenue
            val yearlyRevenue = records.filter { record ->
                calendar.time = record.saleDate
                calendar.get(Calendar.YEAR) == currentYear
            }.sumOf { it.salePrice }
            
            // Calculate top buyers
            val buyerGroups = records.groupBy { it.buyerName }
            val topBuyers = buyerGroups.map { (buyerName, buyerRecords) ->
                BuyerSummary(
                    buyerName = buyerName,
                    buyerContact = buyerRecords.firstOrNull()?.buyerContact ?: "",
                    totalPurchases = buyerRecords.size,
                    totalAmount = buyerRecords.sumOf { it.salePrice },
                    lastPurchaseDate = buyerRecords.maxByOrNull { it.saleDate }?.saleDate ?: Date(),
                    averageOrderValue = buyerRecords.sumOf { it.salePrice } / buyerRecords.size
                )
            }.sortedByDescending { it.totalAmount }.take(5)
            
            // Calculate sales by month (last 12 months)
            val salesByMonth = mutableMapOf<String, Double>()
            for (i in 0..11) {
                val monthCalendar = Calendar.getInstance().apply {
                    add(Calendar.MONTH, -i)
                }
                val monthKey = "${monthCalendar.get(Calendar.YEAR)}-${monthCalendar.get(Calendar.MONTH) + 1}"
                val monthRevenue = records.filter { record ->
                    calendar.time = record.saleDate
                    calendar.get(Calendar.YEAR) == monthCalendar.get(Calendar.YEAR) &&
                    calendar.get(Calendar.MONTH) == monthCalendar.get(Calendar.MONTH)
                }.sumOf { it.salePrice }
                salesByMonth[monthKey] = monthRevenue
            }
            
            // Calculate sales by payment method
            val salesByPaymentMethod = records.groupBy { it.paymentMethod }
                .mapValues { it.value.size }
            
            // Calculate pending and overdue payments
            val pendingPayments = records.filter { it.paymentStatus == "Pending" }
                .sumOf { it.salePrice }
            val overduePayments = records.filter { it.paymentStatus == "Overdue" }
                .sumOf { it.salePrice }
            
            SalesSummary(
                totalSales = totalSales,
                totalRevenue = totalRevenue,
                averageSalePrice = averageSalePrice,
                monthlyRevenue = monthlyRevenue,
                yearlyRevenue = yearlyRevenue,
                topBuyers = topBuyers,
                salesByMonth = salesByMonth,
                salesByPaymentMethod = salesByPaymentMethod,
                pendingPayments = pendingPayments,
                overduePayments = overduePayments
            )
        } catch (e: Exception) {
            SalesSummary()
        }
    }
    
    suspend fun getSalesByDateRange(farmId: String, startDate: Date, endDate: Date): List<SaleRecord> {
        return try {
            val snapshot = salesCollection
                .whereEqualTo("farmId", farmId)
                .whereGreaterThanOrEqualTo("saleDate", startDate)
                .whereLessThanOrEqualTo("saleDate", endDate)
                .orderBy("saleDate", Query.Direction.DESCENDING)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(SaleRecord::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getSalesByBuyer(farmId: String, buyerName: String): List<SaleRecord> {
        return try {
            val snapshot = salesCollection
                .whereEqualTo("farmId", farmId)
                .whereEqualTo("buyerName", buyerName)
                .orderBy("saleDate", Query.Direction.DESCENDING)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(SaleRecord::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}