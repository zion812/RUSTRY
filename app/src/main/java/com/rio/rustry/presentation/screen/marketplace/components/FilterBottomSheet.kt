package com.rio.rustry.presentation.screen.marketplace.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rio.rustry.data.model.FowlBreed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    onDismiss: () -> Unit,
    onApplyFilter: (breed: String?, location: String?, isTraceable: Boolean?) -> Unit
) {
    var selectedBreed by remember { mutableStateOf<String?>(null) }
    var selectedLocation by remember { mutableStateOf("") }
    var selectedTraceability by remember { mutableStateOf<Boolean?>(null) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Filter Fowls",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Breed filter
            Text(
                text = "Breed",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            var breedExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = breedExpanded,
                onExpandedChange = { breedExpanded = !breedExpanded }
            ) {
                OutlinedTextField(
                    value = selectedBreed ?: "All Breeds",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = breedExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = breedExpanded,
                    onDismissRequest = { breedExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All Breeds") },
                        onClick = {
                            selectedBreed = null
                            breedExpanded = false
                        }
                    )
                    
                    FowlBreed.values().forEach { breed ->
                        DropdownMenuItem(
                            text = { Text(breed.displayName) },
                            onClick = {
                                selectedBreed = breed.displayName
                                breedExpanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Location filter
            Text(
                text = "Location",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = selectedLocation,
                onValueChange = { selectedLocation = it },
                placeholder = { Text("Enter location") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Traceability filter
            Text(
                text = "Traceability",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedTraceability == null,
                            onClick = { selectedTraceability = null }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedTraceability == null,
                        onClick = { selectedTraceability = null }
                    )
                    Text(
                        text = "All",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedTraceability == true,
                            onClick = { selectedTraceability = true }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedTraceability == true,
                        onClick = { selectedTraceability = true }
                    )
                    Text(
                        text = "Traceable only",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedTraceability == false,
                            onClick = { selectedTraceability = false }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedTraceability == false,
                        onClick = { selectedTraceability = false }
                    )
                    Text(
                        text = "Non-traceable only",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        selectedBreed = null
                        selectedLocation = ""
                        selectedTraceability = null
                        onApplyFilter(null, null, null)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
                
                Button(
                    onClick = {
                        val location = if (selectedLocation.isBlank()) null else selectedLocation
                        onApplyFilter(selectedBreed, location, selectedTraceability)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Apply")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}