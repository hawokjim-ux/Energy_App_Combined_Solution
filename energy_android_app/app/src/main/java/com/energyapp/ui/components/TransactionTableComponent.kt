package com.energyapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.energyapp.ui.theme.*

data class TransactionItem(
    val saleId: Int,
    val saleIdNo: String,
    val userName: String,
    val userId: Int,
    val mpesaReceipt: String?,
    val amount: Double,
    val time: String,
    val date: String,
    val status: String,
    val pumpName: String
)

@Composable
fun TransactionTable(
    transactions: List<TransactionItem>,
    currentPage: Int,
    pageSize: Int,
    totalCount: Int,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalPages = if (totalCount == 0) 1 else (totalCount + pageSize - 1) / pageSize
    val startItem = currentPage * pageSize + 1
    val endItem = minOf((currentPage + 1) * pageSize, totalCount)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with search
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transaction History",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
                Text(
                    text = "Showing $startItem-$endItem of $totalCount",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search by receipt, user, or amount...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LightPrimary,
                    unfocusedBorderColor = CardBorder
                ),
                singleLine = true
            )

            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(LightSurfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TableHeader("User", Modifier.weight(1.2f))
                TableHeader("Receipt", Modifier.weight(1f))
                TableHeader("Amount", Modifier.weight(0.8f))
                TableHeader("Date/Time", Modifier.weight(1f))
                TableHeader("Status", Modifier.weight(0.6f))
            }

            // Table Body
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions found",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    transactions.forEach { transaction ->
                        TransactionRow(transaction)
                        Divider(color = CardBorder, thickness = 0.5.dp)
                    }
                }
            }

            // Pagination
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (currentPage > 0) onPageChange(currentPage - 1) },
                    enabled = currentPage > 0
                ) {
                    Icon(
                        Icons.Rounded.ChevronLeft,
                        contentDescription = "Previous",
                        tint = if (currentPage > 0) LightPrimary else TextSecondary
                    )
                }

                // Page numbers
                for (page in maxOf(0, currentPage - 2)..minOf(totalPages - 1, currentPage + 2)) {
                    val isCurrentPage = page == currentPage
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCurrentPage) LightPrimary else Color.Transparent)
                            .clickable { onPageChange(page) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (page + 1).toString(),
                            fontSize = 13.sp,
                            fontWeight = if (isCurrentPage) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrentPage) Color.White else TextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = { if (currentPage < totalPages - 1) onPageChange(currentPage + 1) },
                    enabled = currentPage < totalPages - 1
                ) {
                    Icon(
                        Icons.Rounded.ChevronRight,
                        contentDescription = "Next",
                        tint = if (currentPage < totalPages - 1) LightPrimary else TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun TableHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = TextSecondary,
        maxLines = 1
    )
}

@Composable
fun TransactionRow(transaction: TransactionItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User column
        Column(modifier = Modifier.weight(1.2f)) {
            Text(
                text = transaction.userName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = OnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "ID: ${transaction.userId}",
                fontSize = 10.sp,
                color = TextSecondary
            )
        }

        // Receipt column
        Text(
            text = transaction.mpesaReceipt ?: "-",
            modifier = Modifier.weight(1f),
            fontSize = 11.sp,
            color = if (transaction.mpesaReceipt != null) LightPrimary else TextSecondary,
            fontWeight = if (transaction.mpesaReceipt != null) FontWeight.Medium else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Amount column
        Text(
            text = "KES ${String.format("%,.0f", transaction.amount)}",
            modifier = Modifier.weight(0.8f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = OnSurface,
            maxLines = 1
        )

        // Date/Time column
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.date,
                fontSize = 11.sp,
                color = OnSurface,
                maxLines = 1
            )
            Text(
                text = transaction.time,
                fontSize = 10.sp,
                color = TextSecondary
            )
        }

        // Status column
        Surface(
            modifier = Modifier.weight(0.6f),
            shape = RoundedCornerShape(4.dp),
            color = when (transaction.status) {
                "SUCCESS" -> Success.copy(alpha = 0.1f)
                "PENDING" -> Warning.copy(alpha = 0.1f)
                else -> Error.copy(alpha = 0.1f)
            }
        ) {
            Text(
                text = when (transaction.status) {
                    "SUCCESS" -> "✓"
                    "PENDING" -> "⏳"
                    else -> "✗"
                },
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = when (transaction.status) {
                    "SUCCESS" -> Success
                    "PENDING" -> Warning
                    else -> Error
                }
            )
        }
    }
}
