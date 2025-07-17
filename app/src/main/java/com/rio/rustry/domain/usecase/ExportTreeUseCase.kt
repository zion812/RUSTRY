// generated/phase3/app/src/main/java/com/rio/rustry/domain/usecase/ExportTreeUseCase.kt

package com.rio.rustry.domain.usecase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.rio.rustry.analytics.AnalyticsService
import com.rio.rustry.domain.model.FamilyTree
import com.rio.rustry.domain.model.FamilyTreeNode
import com.rio.rustry.domain.model.FamilyRelationship
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
            fowlId = familyTree.nodes.firstOrNull()?.birdId ?: "",
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
            fowlId = familyTree.nodes.firstOrNull()?.birdId ?: "",
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
        
        // Calculate positions
        val positionMap = mutableMapOf<String, Pair<Float, Float>>()
        familyTree.nodes.groupBy { it.generation }.forEach { (gen, nodesInGen) ->
            nodesInGen.forEachIndexed { index, node ->
                val x = 100f + (index.toFloat() * 200f)
                val y = 100f + (gen * 150f)
                positionMap[node.birdId] = x to y
            }
        }
        
        // Draw tree structure
        familyTree.nodes.forEach { node ->
            val (x, y) = positionMap[node.birdId] ?: (0f to 0f)
            
            // Draw node circle
            paint.color = android.graphics.Color.BLUE
            canvas.drawCircle(x, y, 30f, paint)
            
            // Draw node text
            paint.color = android.graphics.Color.BLACK
            canvas.drawText(node.name, x - 50f, y + 50f, paint)
        }
        
        // Draw connections
        familyTree.relationships.forEach { connection ->
            val fromPos = positionMap[connection.parentId]
            val toPos = positionMap[connection.childId]
            
            if (fromPos != null && toPos != null) {
                paint.color = android.graphics.Color.GRAY
                paint.strokeWidth = 3f
                canvas.drawLine(fromPos.first, fromPos.second, toPos.first, toPos.second, paint)
            }
        }
    }
    
    @Suppress("UNUSED_PARAMETER")
    private fun shareFile(file: File) {
        // Implementation for sharing file using Android's share intent
        // This would typically use FileProvider and Intent.ACTION_SEND
    }
}