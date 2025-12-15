package com.energyapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.energyapp.ui.viewmodels.PumpAttendantsViewModel

data class PumpAttendant(
    val id: String,
    val fullName: String,
    val phoneNumber: String,
    val isActive: Boolean,
    val todaySales: String,
    val transactionCount: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PumpAttendantsScreen(
    onNavigateBack: () -> Unit,
    viewModel: PumpAttendantsViewModel = hiltViewModel()
) {
    // Sample data - replace with actual data from ViewModel
    val attendants = remember {
        listOf(
            PumpAttendant("1", "John Kamau", "+254712345678", true, "KES 15,000", 25),
            PumpAttendant("2", "Mary Wanjiku", "+254723456789", true, "KES 12,500", 18),
            PumpAttendant("3", "Peter Ochieng", "+254734567890", false, "KES 8,000", 12),
            PumpAttendant("4", "Grace Akinyi", "+254745678901", true, "KES 18,500", 30)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E293B),
                        Color(0xFF0F172A)
                    )
                )
            )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Pump Attendants",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* TODO: Add new attendant */ }) {
                            Icon(
                                imageVector = Icons.Rounded.PersonAdd,
                                contentDescription = "Add Attendant",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF1E293B).copy(alpha = 0.95f),
                        titleContentColor = Color.White,
                        actionIconContentColor = Color(0xFF06D6A6),
                        navigationIconContentColor = Color(0xFF06D6A6)
                    ),
                    modifier = Modifier.shadow(
                        elevation = 8.dp,
                        spotColor = Color(0xFF06D6A6).copy(alpha = 0.2f)
                    )
                )
            },
            containerColor = Color.Transparent,
            contentColor = Color.White,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { /* TODO: Add new attendant */ },
                    containerColor = Color(0xFF06D6A6),
                    contentColor = Color.White,
                    modifier = Modifier.shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(16.dp)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add Attendant",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Stats Summary Card
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn()
                ) {
                    AttendantsSummaryCard(
                        totalAttendants = attendants.size,
                        activeAttendants = attendants.count { it.isActive },
                        totalSales = "KES 54,000",
                        totalTransactions = attendants.sumOf { it.transactionCount }
                    )
                }

                // Attendants List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(attendants) { attendant ->
                        AttendantCard(
                            attendant = attendant,
                            onClick = { /* TODO: Navigate to attendant details */ }
                        )
                    }
                }
            }
        }

        // Decorative background elements
        PumpAttendantsDecorativeCircles()
    }
}

@Composable
fun AttendantsSummaryCard(
    totalAttendants: Int,
    activeAttendants: Int,
    totalSales: String,
    totalTransactions: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF06D6A6).copy(alpha = 0.15f),
                        Color(0xFF0EA5E9).copy(alpha = 0.1f)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                color = Color(0xFF06D6A6).copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryItem(
                label = "Total",
                value = totalAttendants.toString(),
                icon = Icons.Rounded.People,
                color = Color(0xFF06D6A6)
            )
            SummaryItem(
                label = "Active",
                value = activeAttendants.toString(),
                icon = Icons.Rounded.CheckCircle,
                color = Color(0xFF0EA5E9)
            )
            SummaryItem(
                label = "Sales",
                value = totalSales,
                icon = Icons.Rounded.TrendingUp,
                color = Color(0xFF06B6D4)
            )
            SummaryItem(
                label = "Trans.",
                value = totalTransactions.toString(),
                icon = Icons.Rounded.Receipt,
                color = Color(0xFF0EA5E9)
            )
        }
    }
}

@Composable
fun SummaryItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF94A3B8),
            fontSize = 10.sp
        )
    }
}

@Composable
fun AttendantCard(
    attendant: PumpAttendant,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E293B))
            .border(
                width = 1.5.dp,
                color = if (attendant.isActive) 
                    Color(0xFF06D6A6).copy(alpha = 0.3f) 
                else 
                    Color(0xFF94A3B8).copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = if (attendant.isActive) {
                                    listOf(Color(0xFF06D6A6), Color(0xFF0EA5E9))
                                } else {
                                    listOf(Color(0xFF64748B), Color(0xFF475569))
                                }
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Attendant Info
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = attendant.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        
                        // Status Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (attendant.isActive) 
                                        Color(0xFF06D6A6).copy(alpha = 0.2f)
                                    else 
                                        Color(0xFF64748B).copy(alpha = 0.2f)
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (attendant.isActive) "Active" else "Inactive",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (attendant.isActive) Color(0xFF06D6A6) else Color(0xFF94A3B8),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = attendant.phoneNumber,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        AttendantStat(
                            label = "Sales",
                            value = attendant.todaySales,
                            color = Color(0xFF06D6A6)
                        )
                        AttendantStat(
                            label = "Trans",
                            value = attendant.transactionCount.toString(),
                            color = Color(0xFF0EA5E9)
                        )
                    }
                }
            }

            // Action Icon
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "View Details",
                tint = Color(0xFF06D6A6),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun AttendantStat(
    label: String,
    value: String,
    color: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Text(
            text = "$label: $value",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFA1A5B7),
            fontSize = 10.sp
        )
    }
}

@Composable
fun PumpAttendantsDecorativeCircles() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(350.dp)
                .offset(x = (-120).dp, y = (-100).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF06D6A6).copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .size(350.dp)
                .offset(x = 180.dp, y = 450.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF0EA5E9).copy(alpha = 0.06f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}