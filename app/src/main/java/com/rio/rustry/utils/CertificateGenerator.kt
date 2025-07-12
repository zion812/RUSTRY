package com.rio.rustry.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.rio.rustry.data.model.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CertificateGenerator @Inject constructor() {
    
    companion object {
        private const val PAGE_WIDTH = 595 // A4 width in points
        private const val PAGE_HEIGHT = 842 // A4 height in points
        private const val MARGIN = 50
        private const val QR_CODE_SIZE = 100
    }
    
    suspend fun generateOwnershipCertificate(
        certificate: DigitalCertificate,
        fowl: Fowl,
        healthRecords: List<HealthRecord>
    ): ByteArray {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        
        try {
            drawCertificateContent(canvas, certificate, fowl, healthRecords)
            pdfDocument.finishPage(page)
            
            val outputStream = ByteArrayOutputStream()
            pdfDocument.writeTo(outputStream)
            return outputStream.toByteArray()
        } finally {
            pdfDocument.close()
        }
    }
    
    private suspend fun drawCertificateContent(
        canvas: Canvas,
        certificate: DigitalCertificate,
        fowl: Fowl,
        healthRecords: List<HealthRecord>
    ) {
        val paint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.LEFT
        }
        
        var yPosition = MARGIN + 50f
        
        // Header
        drawHeader(canvas, paint, yPosition)
        yPosition += 80
        
        // Certificate Title
        paint.apply {
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            color = Color.BLACK
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "DIGITAL OWNERSHIP CERTIFICATE",
            PAGE_WIDTH / 2f,
            yPosition,
            paint
        )
        yPosition += 40
        
        // Certificate Number
        paint.apply {
            textSize = 14f
            typeface = Typeface.DEFAULT
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "Certificate No: ${certificate.certificateNumber}",
            PAGE_WIDTH / 2f,
            yPosition,
            paint
        )
        yPosition += 40
        
        // Reset text alignment
        paint.textAlign = Paint.Align.LEFT
        
        // Fowl Information Section
        yPosition = drawSection(canvas, paint, "FOWL INFORMATION", yPosition)
        yPosition = drawKeyValue(canvas, paint, "Breed:", fowl.breed, yPosition)
        yPosition = drawKeyValue(canvas, paint, "Date of Birth:", 
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(fowl.dateOfBirth), yPosition)
        yPosition = drawKeyValue(canvas, paint, "Traceability:", 
            if (fowl.isTraceable) "Traceable" else "Non-traceable", yPosition)
        yPosition = drawKeyValue(canvas, paint, "Registration ID:", fowl.id, yPosition)
        yPosition += 20
        
        // Current Owner Section
        yPosition = drawSection(canvas, paint, "CURRENT OWNER", yPosition)
        yPosition = drawKeyValue(canvas, paint, "Owner Name:", certificate.currentOwnerName, yPosition)
        yPosition = drawKeyValue(canvas, paint, "Owner ID:", certificate.currentOwnerId, yPosition)
        yPosition += 20
        
        // Previous Owner Section
        if (certificate.previousOwnerName.isNotEmpty()) {
            yPosition = drawSection(canvas, paint, "PREVIOUS OWNER", yPosition)
            yPosition = drawKeyValue(canvas, paint, "Previous Owner:", certificate.previousOwnerName, yPosition)
            yPosition = drawKeyValue(canvas, paint, "Transfer Date:", 
                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(certificate.transferDetails.transferDate)), yPosition)
            yPosition = drawKeyValue(canvas, paint, "Transfer Type:", certificate.transferDetails.transferType.displayName, yPosition)
            if (certificate.transferDetails.transferPrice > 0) {
                yPosition = drawKeyValue(canvas, paint, "Transfer Price:", 
                    "₹${certificate.transferDetails.transferPrice}", yPosition)
            }
            yPosition += 20
        }
        
        // Health Summary Section
        yPosition = drawSection(canvas, paint, "HEALTH SUMMARY", yPosition)
        yPosition = drawKeyValue(canvas, paint, "Health Score:", "${certificate.healthSummary.healthScore}/100", yPosition)
        yPosition = drawKeyValue(canvas, paint, "Vaccination Status:", certificate.healthSummary.vaccinationStatus, yPosition)
        yPosition = drawKeyValue(canvas, paint, "Health Records:", "${certificate.healthSummary.healthRecordsCount} records", yPosition)
        yPosition = drawKeyValue(canvas, paint, "Vet Certificates:", "${certificate.healthSummary.vetCertificates} certificates", yPosition)
        yPosition += 20
        
        // Lineage Information
        if (fowl.parentIds.isNotEmpty()) {
            yPosition = drawSection(canvas, paint, "LINEAGE INFORMATION", yPosition)
            yPosition = drawKeyValue(canvas, paint, "Parent IDs:", fowl.parentIds.joinToString(", "), yPosition)
            yPosition += 20
        }
        
        // Certificate Details Section
        yPosition = drawSection(canvas, paint, "CERTIFICATE DETAILS", yPosition)
        yPosition = drawKeyValue(canvas, paint, "Issue Date:", 
            SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(certificate.issueDate)), yPosition)
        yPosition = drawKeyValue(canvas, paint, "Issued By:", certificate.issuedBy, yPosition)
        yPosition = drawKeyValue(canvas, paint, "Certificate Version:", certificate.certificateVersion, yPosition)
        yPosition = drawKeyValue(canvas, paint, "Verification URL:", certificate.verificationUrl, yPosition)
        yPosition += 30
        
        // QR Code
        drawQRCode(canvas, certificate.qrCodeData, (PAGE_WIDTH - MARGIN - QR_CODE_SIZE).toFloat(), yPosition)
        
        // QR Code Label
        paint.apply {
            textSize = 10f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "Scan to verify",
            PAGE_WIDTH - MARGIN - QR_CODE_SIZE / 2f,
            yPosition + QR_CODE_SIZE + 15,
            paint
        )
        
        // Footer
        drawFooter(canvas, paint)
    }
    
    private fun drawHeader(canvas: Canvas, paint: Paint, yPosition: Float) {
        // Platform Logo/Name
        paint.apply {
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            color = Color.parseColor("#2196F3")
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("ROOSTER PLATFORM", PAGE_WIDTH / 2f, yPosition, paint)
        
        // Tagline
        paint.apply {
            textSize = 12f
            typeface = Typeface.DEFAULT
            color = Color.GRAY
        }
        canvas.drawText("Connecting Rural Farmers with Urban Buyers", PAGE_WIDTH / 2f, yPosition + 20, paint)
        
        // Header line
        paint.apply {
            color = Color.parseColor("#2196F3")
            strokeWidth = 2f
        }
        canvas.drawLine(
            MARGIN.toFloat(),
            yPosition + 35,
            (PAGE_WIDTH - MARGIN).toFloat(),
            yPosition + 35,
            paint
        )
    }
    
    private fun drawSection(canvas: Canvas, paint: Paint, title: String, yPosition: Float): Float {
        paint.apply {
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            color = Color.parseColor("#2196F3")
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText(title, MARGIN.toFloat(), yPosition, paint)
        
        // Section underline
        paint.apply {
            color = Color.parseColor("#2196F3")
            strokeWidth = 1f
        }
        canvas.drawLine(
            MARGIN.toFloat(),
            yPosition + 5,
            MARGIN + 200f,
            yPosition + 5,
            paint
        )
        
        return yPosition + 25
    }
    
    private fun drawKeyValue(canvas: Canvas, paint: Paint, key: String, value: String, yPosition: Float): Float {
        paint.apply {
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            color = Color.BLACK
        }
        canvas.drawText(key, (MARGIN + 20).toFloat(), yPosition, paint)
        
        paint.apply {
            typeface = Typeface.DEFAULT
            color = Color.DKGRAY
        }
        canvas.drawText(value, (MARGIN + 150).toFloat(), yPosition, paint)
        
        return yPosition + 18
    }
    
    private suspend fun drawQRCode(canvas: Canvas, data: String, x: Float, y: Float) {
        try {
            val qrCodeBitmap = generateQRCode(data, QR_CODE_SIZE)
            canvas.drawBitmap(qrCodeBitmap, x, y, null)
        } catch (e: Exception) {
            // If QR code generation fails, draw a placeholder
            val paint = Paint().apply {
                color = Color.LTGRAY
                style = Paint.Style.FILL
            }
            canvas.drawRect(x, y, x + QR_CODE_SIZE, y + QR_CODE_SIZE, paint)
            
            paint.apply {
                color = Color.BLACK
                textSize = 10f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("QR Code", x + QR_CODE_SIZE / 2, y + QR_CODE_SIZE / 2, paint)
        }
    }
    
    private fun drawFooter(canvas: Canvas, paint: Paint) {
        val footerY = PAGE_HEIGHT - MARGIN.toFloat()
        
        // Footer line
        paint.apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }
        canvas.drawLine(
            MARGIN.toFloat(),
            footerY - 30,
            (PAGE_WIDTH - MARGIN).toFloat(),
            footerY - 30,
            paint
        )
        
        // Footer text
        paint.apply {
            textSize = 10f
            typeface = Typeface.DEFAULT
            color = Color.GRAY
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "This is a digitally generated certificate. Verify authenticity at roosterplatform.com/verify",
            PAGE_WIDTH / 2f,
            footerY - 10,
            paint
        )
        
        // Generation timestamp
        canvas.drawText(
            "Generated on ${SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()).format(Date())}",
            PAGE_WIDTH / 2f,
            footerY + 5,
            paint
        )
    }
    
    suspend fun generateQRCode(data: String, size: Int = 200): Bitmap {
        return try {
            val qrCodeWriter = QRCodeWriter()
            val bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, size, size)
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
            
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            
            bitmap
        } catch (e: Exception) {
            // Return a simple placeholder bitmap if QR generation fails
            Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).apply {
                eraseColor(Color.LTGRAY)
            }
        }
    }
    
    suspend fun generateHealthCertificate(
        fowl: Fowl,
        healthRecords: List<HealthRecord>,
        healthSummary: HealthSummary
    ): ByteArray {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        
        try {
            drawHealthCertificateContent(canvas, fowl, healthRecords, healthSummary)
            pdfDocument.finishPage(page)
            
            val outputStream = ByteArrayOutputStream()
            pdfDocument.writeTo(outputStream)
            return outputStream.toByteArray()
        } finally {
            pdfDocument.close()
        }
    }
    
    private fun drawHealthCertificateContent(
        canvas: Canvas,
        fowl: Fowl,
        healthRecords: List<HealthRecord>,
        healthSummary: HealthSummary
    ) {
        val paint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.LEFT
        }
        
        var yPosition = MARGIN + 50f
        
        // Header
        drawHeader(canvas, paint, yPosition)
        yPosition += 80
        
        // Certificate Title
        paint.apply {
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            color = Color.BLACK
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "HEALTH CERTIFICATE",
            PAGE_WIDTH / 2f,
            yPosition,
            paint
        )
        yPosition += 60
        
        paint.textAlign = Paint.Align.LEFT
        
        // Fowl Information
        yPosition = drawSection(canvas, paint, "FOWL INFORMATION", yPosition)
        yPosition = drawKeyValue(canvas, paint, "Fowl ID:", fowl.id, yPosition)
        yPosition = drawKeyValue(canvas, paint, "Breed:", fowl.breed, yPosition)
        yPosition = drawKeyValue(canvas, paint, "Owner:", fowl.ownerName, yPosition)
        yPosition += 20
        
        // Health Summary
        yPosition = drawSection(canvas, paint, "HEALTH SUMMARY", yPosition)
        yPosition = drawKeyValue(canvas, paint, "Health Score:", "${healthSummary.healthScore}/100", yPosition)
        yPosition = drawKeyValue(canvas, paint, "Total Records:", "${healthSummary.totalRecords}", yPosition)
        yPosition = drawKeyValue(canvas, paint, "Vaccinations:", "${healthSummary.vaccinationCount}", yPosition)
        yPosition = drawKeyValue(canvas, paint, "Vet Certificates:", "${healthSummary.vetCertificates}", yPosition)
        
        if (healthSummary.lastVaccinationDate != null) {
            yPosition = drawKeyValue(canvas, paint, "Last Vaccination:", 
                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(healthSummary.lastVaccinationDate)), yPosition)
        }
        yPosition += 20
        
        // Recent Health Records
        if (healthRecords.isNotEmpty()) {
            yPosition = drawSection(canvas, paint, "RECENT HEALTH RECORDS", yPosition)
            
            healthRecords.take(10).forEach { record ->
                val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(record.date))
                yPosition = drawKeyValue(canvas, paint, dateStr, "${record.type.displayName}: ${record.title}", yPosition)
            }
        }
        
        // Footer
        drawFooter(canvas, paint)
    }
    
    fun generateCertificatePreview(certificate: DigitalCertificate): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Certificate Preview</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; }
                    .header { text-align: center; color: #2196F3; }
                    .section { margin: 20px 0; }
                    .section h3 { color: #2196F3; border-bottom: 1px solid #2196F3; }
                    .key-value { display: flex; margin: 5px 0; }
                    .key { font-weight: bold; width: 150px; }
                    .value { color: #666; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>ROOSTER PLATFORM</h1>
                    <h2>DIGITAL OWNERSHIP CERTIFICATE</h2>
                    <p>Certificate No: ${certificate.certificateNumber}</p>
                </div>
                
                <div class="section">
                    <h3>Fowl Information</h3>
                    <div class="key-value">
                        <span class="key">Breed:</span>
                        <span class="value">${certificate.fowlDetails.breed}</span>
                    </div>
                    <div class="key-value">
                        <span class="key">Date of Birth:</span>
                        <span class="value">${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(certificate.fowlDetails.dateOfBirth))}</span>
                    </div>
                    <div class="key-value">
                        <span class="key">Traceability:</span>
                        <span class="value">${if (certificate.fowlDetails.isTraceable) "Traceable" else "Non-traceable"}</span>
                    </div>
                </div>
                
                <div class="section">
                    <h3>Current Owner</h3>
                    <div class="key-value">
                        <span class="key">Name:</span>
                        <span class="value">${certificate.currentOwnerName}</span>
                    </div>
                </div>
                
                <div class="section">
                    <h3>Health Summary</h3>
                    <div class="key-value">
                        <span class="key">Health Score:</span>
                        <span class="value">${certificate.healthSummary.healthScore}/100</span>
                    </div>
                    <div class="key-value">
                        <span class="key">Vaccination Status:</span>
                        <span class="value">${certificate.healthSummary.vaccinationStatus}</span>
                    </div>
                </div>
                
                <div class="section">
                    <h3>Certificate Details</h3>
                    <div class="key-value">
                        <span class="key">Issue Date:</span>
                        <span class="value">${SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(certificate.issueDate))}</span>
                    </div>
                    <div class="key-value">
                        <span class="key">Verification URL:</span>
                        <span class="value">${certificate.verificationUrl}</span>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}