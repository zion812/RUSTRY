// generated/phase3/app/src/main/java/com/rio/rustry/domain/usecase/ExportTreeUseCase.kt

package com.rio.rustry.domain.usecase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.rio.rustry.analytics.AnalyticsService
import com.rio.rustry.breeding.FamilyTree
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class ExportTreeUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analyticsService: AnalyticsService
) {
    
    suspend fun exportAsPng(familyTree: FamilyTree) {
        val bitmap = generateTreeBitmap(familyTree)
        val file = File(context.getExternalFilesDir(null), "family_tree_${System.currentTimeMillis()}.png")
        
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        
        analyticsService.logTreeExported(
            fowlId = familyTree.nodes.firstOrNull()?.id ?: "",
            exportFormat = "png"
        )
        
        // Share file
        shareFile(file)
    }
    
    suspend fun exportAsPdf(familyTree: FamilyTree) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        
        val canvas = page.canvas
        drawTreeOnCanvas(canvas, familyTree)
        
        pdfDocument.finishPage(page)
        
        val file = File(context.getExternalFilesDir(null), "family_tree_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        
        analyticsService.logTreeExported(
            fowlId = familyTree.nodes.firstOrNull()?.id ?: "",
            exportFormat = "pdf"
        )
        
        // Share file
        shareFile(file)
    }
    
    private fun generateTreeBitmap(familyTree: FamilyTree): Bitmap {
        val bitmap = Bitmap.createBitmap(1200, 800, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawTreeOnCanvas(canvas, familyTree)
        return bitmap
    }
    
    private fun drawTreeOnCanvas(canvas: Canvas, familyTree: FamilyTree) {
        val paint = Paint().apply {
            isAntiAlias = true
            textSize = 24f
        }
        
        // Draw tree structure
        familyTree.nodes.forEach { node ->
            val x = 100f + (node.position * 200f)
            val y = 100f + (node.generation * 150f)
            
            // Draw node circle
            paint.color = android.graphics.Color.BLUE
            canvas.drawCircle(x, y, 30f, paint)
            
            // Draw node text
            paint.color = android.graphics.Color.BLACK
            canvas.drawText(node.name, x - 50f, y + 50f, paint)
        }
        
        // Draw connections
        familyTree.connections.forEach { connection ->
            val fromNode = familyTree.nodes.find { it.id == connection.fromId }
            val toNode = familyTree.nodes.find { it.id == connection.toId }
            
            if (fromNode != null && toNode != null) {
                val fromX = 100f + (fromNode.position * 200f)
                val fromY = 100f + (fromNode.generation * 150f)
                val toX = 100f + (toNode.position * 200f)
                val toY = 100f + (toNode.generation * 150f)
                
                paint.color = android.graphics.Color.GRAY
                paint.strokeWidth = 3f
                canvas.drawLine(fromX, fromY, toX, toY, paint)
            }
        }
    }
    
    private fun shareFile(file: File) {
        // Implementation for sharing file using Android's share intent
        // This would typically use FileProvider and Intent.ACTION_SEND
    }
}