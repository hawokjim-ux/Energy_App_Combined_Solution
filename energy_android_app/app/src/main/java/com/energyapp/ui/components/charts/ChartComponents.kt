package com.energyapp.ui.components.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.energyapp.ui.theme.*
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries

/**
 * Sales Line Chart - shows sales trend over time
 */
@Composable
fun SalesLineChart(
    salesData: List<Double>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    
    androidx.compose.runtime.LaunchedEffect(salesData) {
        if (salesData.isNotEmpty()) {
            modelProducer.runTransaction {
                lineSeries { series(salesData) }
            }
        }
    }

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
            Text(
                text = "Sales Trend",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurface
            )
            
            if (salesData.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data available",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            } else {
                CartesianChartHost(
                    chart = rememberCartesianChart(
                        rememberLineCartesianLayer(),
                        startAxis = rememberStartAxis(),
                        bottomAxis = rememberBottomAxis()
                    ),
                    modelProducer = modelProducer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }
    }
}

/**
 * Pump Comparison Bar Chart
 */
@Composable
fun PumpBarChart(
    pumpNames: List<String>,
    salesAmounts: List<Double>,
    modifier: Modifier = Modifier
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    
    androidx.compose.runtime.LaunchedEffect(salesAmounts) {
        if (salesAmounts.isNotEmpty()) {
            modelProducer.runTransaction {
                columnSeries { series(salesAmounts) }
            }
        }
    }

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
            Text(
                text = "Pump-wise Sales Comparison",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurface
            )
            
            if (salesAmounts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data available",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            } else {
                CartesianChartHost(
                    chart = rememberCartesianChart(
                        rememberColumnCartesianLayer(),
                        startAxis = rememberStartAxis(),
                        bottomAxis = rememberBottomAxis()
                    ),
                    modelProducer = modelProducer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
                
                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pumpNames.forEachIndexed { index, name ->
                        if (index < 4) { // Show max 4 items
                            LegendItem(
                                color = when (index) {
                                    0 -> ChartPrimary
                                    1 -> ChartSecondary
                                    2 -> ChartTertiary
                                    else -> ChartQuaternary
                                },
                                label = name
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = TextSecondary
        )
    }
}

/**
 * Simple Summary Stats Card
 */
@Composable
fun SummaryStatsCard(
    totalSales: Double,
    mpesaSales: Double,
    transactionCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryStatItem(
                label = "Total Sales",
                value = "KES ${String.format("%,.0f", totalSales)}",
                color = LightPrimary
            )
            SummaryStatItem(
                label = "M-Pesa",
                value = "KES ${String.format("%,.0f", mpesaSales)}",
                color = Success
            )
            SummaryStatItem(
                label = "Transactions",
                value = transactionCount.toString(),
                color = Secondary
            )
        }
    }
}

@Composable
fun SummaryStatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/**
 * User Sales Bar Chart - compares sales by user/attendant
 */
@Composable
fun UserSalesBarChart(
    userNames: List<String>,
    salesAmounts: List<Double>,
    modifier: Modifier = Modifier
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    
    androidx.compose.runtime.LaunchedEffect(salesAmounts) {
        if (salesAmounts.isNotEmpty()) {
            modelProducer.runTransaction {
                columnSeries { series(salesAmounts) }
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sales by Attendant",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
                Text(
                    text = "${userNames.size} users",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            
            if (salesAmounts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data available",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            } else {
                CartesianChartHost(
                    chart = rememberCartesianChart(
                        rememberColumnCartesianLayer(),
                        startAxis = rememberStartAxis(),
                        bottomAxis = rememberBottomAxis()
                    ),
                    modelProducer = modelProducer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
                
                // User Legend
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    userNames.forEachIndexed { index, name ->
                        if (index < 6) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(
                                                when (index % 4) {
                                                    0 -> ChartPrimary
                                                    1 -> ChartSecondary
                                                    2 -> ChartTertiary
                                                    else -> ChartQuaternary
                                                }
                                            )
                                    )
                                    Text(
                                        text = name,
                                        fontSize = 12.sp,
                                        color = OnSurface
                                    )
                                }
                                Text(
                                    text = "KES ${String.format("%,.0f", salesAmounts.getOrElse(index) { 0.0 })}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = LightPrimary
                                )
                            }
                        }
                    }
                    if (userNames.size > 6) {
                        Text(
                            text = "+${userNames.size - 6} more...",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Enhanced Stats Card - with gradient backgrounds and icons
 */
@Composable
fun EnhancedStatsCard(
    totalSales: Double,
    mpesaSales: Double,
    cashSales: Double,
    transactionCount: Int,
    successfulCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Main Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Total Sales Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LightPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "💰",
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Total Sales",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "KES ${String.format("%,.0f", totalSales)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            // Transactions Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Success),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📊",
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Transactions",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "$successfulCount / $transactionCount",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
        
        // Payment Methods Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // M-Pesa Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "M-Pesa",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "KES ${String.format("%,.0f", mpesaSales)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Success
                        )
                    }
                    Text(text = "📱", fontSize = 20.sp)
                }
            }
            
            // Cash Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Cash",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "KES ${String.format("%,.0f", cashSales)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Secondary
                        )
                    }
                    Text(text = "💵", fontSize = 20.sp)
                }
            }
        }
    }
}

/**
 * Sales Histogram Chart - shows sales distribution by amount ranges
 */
@Composable
fun SalesHistogramChart(
    salesData: List<Double>,
    modifier: Modifier = Modifier
) {
    // Calculate histogram buckets
    val buckets = remember(salesData) {
        if (salesData.isEmpty()) emptyList()
        else {
            val ranges = listOf(
                0.0 to 500.0,
                500.0 to 1000.0,
                1000.0 to 2000.0,
                2000.0 to 5000.0,
                5000.0 to Double.MAX_VALUE
            )
            val labels = listOf("0-500", "500-1K", "1K-2K", "2K-5K", "5K+")
            
            ranges.mapIndexed { index, (min, max) ->
                labels[index] to salesData.count { it >= min && it < max }
            }
        }
    }
    
    val modelProducer = remember { CartesianChartModelProducer() }
    
    androidx.compose.runtime.LaunchedEffect(buckets) {
        if (buckets.isNotEmpty()) {
            modelProducer.runTransaction {
                columnSeries { series(buckets.map { it.second.toDouble() }) }
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sales Distribution",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
                Text(
                    text = "${salesData.size} transactions",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            
            if (buckets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data available",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            } else {
                CartesianChartHost(
                    chart = rememberCartesianChart(
                        rememberColumnCartesianLayer(),
                        startAxis = rememberStartAxis(),
                        bottomAxis = rememberBottomAxis()
                    ),
                    modelProducer = modelProducer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
                
                // Bucket labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    buckets.forEach { (label, count) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = count.toString(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = LightPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
