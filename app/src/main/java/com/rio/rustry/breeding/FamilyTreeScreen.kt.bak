// generated/phase3/app/src/main/java/com/rio/rustry/breeding/FamilyTreeScreen.kt

package com.rio.rustry.breeding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyTreeScreen(
    fowlId: String,
    viewModel: BreedingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var selectedNode by remember { mutableStateOf<TreeNode?>(null) }

    LaunchedEffect(fowlId) {
        viewModel.loadFamilyTree(fowlId)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Bar
        TopAppBar(
            title = { Text("Family Tree") },
            actions = {
                IconButton(
                    onClick = { viewModel.exportTreeAsPng() }
                ) {
                    Icon(Icons.Default.Download, contentDescription = "Export PNG")
                }
                IconButton(
                    onClick = { viewModel.exportTreeAsPdf() }
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Export PDF")
                }
            }
        )

        when (uiState) {
            is BreedingUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is BreedingUiState.FamilyTreeLoaded -> {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Zoomable Tree Canvas
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            )
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(0.5f, 3f)
                                    offset += pan
                                }
                            }
                    ) {
                        drawFamilyTree(
                            tree = uiState.familyTree,
                            canvasSize = size,
                            onNodeClick = { node -> selectedNode = node }
                        )
                    }

                    // Reset zoom button
                    FloatingActionButton(
                        onClick = {
                            scale = 1f
                            offset = Offset.Zero
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    ) {
                        Text("Reset")
                    }
                }
            }
            is BreedingUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            else -> {
                // Handle other states
            }
        }
    }

    // Node Detail Sheet
    selectedNode?.let { node ->
        ModalBottomSheet(
            onDismissRequest = { selectedNode = null }
        ) {
            NodeDetailSheet(
                node = node,
                onDismiss = { selectedNode = null }
            )
        }
    }
}

@Composable
fun NodeDetailSheet(
    node: TreeNode,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = node.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Breed: ${node.breed}",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Text(
            text = "Generation: ${node.generation}",
            style = MaterialTheme.typography.bodyLarge
        )
        
        if (node.birthDate.isNotEmpty()) {
            Text(
                text = "Birth Date: ${node.birthDate}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Close")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

fun DrawScope.drawFamilyTree(
    tree: FamilyTree,
    canvasSize: androidx.compose.ui.geometry.Size,
    onNodeClick: (TreeNode) -> Unit
) {
    val centerX = canvasSize.width / 2
    val centerY = canvasSize.height / 2
    val levelHeight = 120.dp.toPx()
    val nodeRadius = 30.dp.toPx()
    
    // Draw connections first
    tree.connections.forEach { connection ->
        val fromNode = tree.nodes.find { it.id == connection.fromId }
        val toNode = tree.nodes.find { it.id == connection.toId }
        
        if (fromNode != null && toNode != null) {
            val fromPos = calculateNodePosition(fromNode, centerX, centerY, levelHeight)
            val toPos = calculateNodePosition(toNode, centerX, centerY, levelHeight)
            
            drawLine(
                color = Color.Gray,
                start = fromPos,
                end = toPos,
                strokeWidth = 2.dp.toPx()
            )
        }
    }
    
    // Draw nodes
    tree.nodes.forEach { node ->
        val position = calculateNodePosition(node, centerX, centerY, levelHeight)
        
        // Node circle
        drawCircle(
            color = when (node.gender) {
                "male" -> Color.Blue
                "female" -> Color.Magenta
                else -> Color.Gray
            },
            radius = nodeRadius,
            center = position
        )
        
        // Node border
        drawCircle(
            color = Color.Black,
            radius = nodeRadius,
            center = position,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
        )
    }
}

fun calculateNodePosition(
    node: TreeNode,
    centerX: Float,
    centerY: Float,
    levelHeight: Float
): Offset {
    val angle = (node.position * 2 * Math.PI / node.siblingCount).toFloat()
    val radius = node.generation * 100f
    
    return Offset(
        x = centerX + radius * cos(angle),
        y = centerY + radius * sin(angle)
    )
}

data class FamilyTree(
    val nodes: List<TreeNode>,
    val connections: List<TreeConnection>
)

data class TreeNode(
    val id: String,
    val name: String,
    val breed: String,
    val gender: String,
    val generation: Int,
    val position: Int,
    val siblingCount: Int,
    val birthDate: String = ""
)

data class TreeConnection(
    val fromId: String,
    val toId: String,
    val type: String // "parent", "offspring"
)