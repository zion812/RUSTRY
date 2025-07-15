// generated/phase2/app/src/main/java/com/rio/rustry/orders/OrderHistoryScreen.kt

package com.rio.rustry.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.rio.rustry.data.model.Order
import com.rio.rustry.data.model.OrderStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    onOrderClick: (String) -> Unit,
    viewModel: OrderHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val swipeRefreshState = rememberSwipeRefreshState(uiState.isRefreshing)

    LaunchedEffect(Unit) {
        viewModel.loadOrders()
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header with tabs
        Text(
            text = "Order History",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        TabRow(
            selectedTabIndex = uiState.selectedTab.ordinal
        ) {
            OrderTab.values().forEach { tab ->
                Tab(
                    selected = uiState.selectedTab == tab,
                    onClick = { viewModel.selectTab(tab) },
                    text = { Text(tab.displayName) }
                )
            }
        }

        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = viewModel::refreshOrders
        ) {
            when {
                uiState.isLoading && uiState.orders.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.error,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = viewModel::loadOrders) {
                            Text("Retry")
                        }
                    }
                }
                uiState.filteredOrders.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No ${uiState.selectedTab.displayName.lowercase()} orders",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.filteredOrders) { order ->
                            OrderItem(
                                order = order,
                                onClick = { onOrderClick(order.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderItem(
    order: Order,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Order #${order.id.take(8)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatDate(order.createdAt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${order.items.size} item(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    OrderStatusChip(status = order.status)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$${order.total}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun OrderStatusChip(status: OrderStatus) {
    val (text, color) = when (status) {
        OrderStatus.PENDING -> "Pending" to MaterialTheme.colorScheme.outline
        OrderStatus.CONFIRMED -> "Confirmed" to MaterialTheme.colorScheme.primary
        OrderStatus.SHIPPED -> "Shipped" to MaterialTheme.colorScheme.tertiary
        OrderStatus.DELIVERED -> "Delivered" to MaterialTheme.colorScheme.primary
        OrderStatus.CANCELLED -> "Cancelled" to MaterialTheme.colorScheme.error
    }

    AssistChip(
        onClick = { },
        label = { Text(text) },
        colors = AssistChipDefaults.assistChipColors(
            labelColor = color
        )
    )
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
}

enum class OrderTab(val displayName: String) {
    ACTIVE("Active"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}