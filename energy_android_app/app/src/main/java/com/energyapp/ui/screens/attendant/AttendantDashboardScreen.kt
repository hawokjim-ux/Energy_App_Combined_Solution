package com.energyapp.ui.screens.attendant

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendantDashboardScreen(
    fullName: String,
    onNavigateToSales: () -> Unit,
    onLogout: () -> Unit
) {
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
                            "Attendant Dashboard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    },
                    actions = {
                        IconButton(onClick = onLogout) {
                            Icon(
                                imageVector = Icons.Rounded.ExitToApp,
                                contentDescription = "Logout",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF1E293B).copy(alpha = 0.95f),
                        titleContentColor = Color.White,
                        actionIconContentColor = Color(0xFF06D6A6)
                    ),
                    modifier = Modifier.shadow(
                        elevation = 8.dp,
                        spotColor = Color(0xFF06D6A6).copy(alpha = 0.2f)
                    )
                )
            },
            containerColor = Color.Transparent,
            contentColor = Color.White
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Welcome Card
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn()
                ) {
                    PremiumAttendantWelcomeCard(fullName = fullName)
                }

                // Quick Stats
                QuickStatsRow()

                // Main Dashboard Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        PremiumActionCard(
                            title = "Record Sale",
                            subtitle = "Process transaction",
                            icon = Icons.Rounded.PointOfSale,
                            gradientColors = listOf(
                                Color(0xFF06B6D4),
                                Color(0xFF0EA5E9)
                            ),
                            badge = "Tap",
                            onClick = onNavigateToSales
                        )
                    }

                    item {
                        PremiumActionCard(
                            title = "My Sales",
                            subtitle = "View transactions",
                            icon = Icons.Rounded.Receipt,
                            gradientColors = listOf(
                                Color(0xFF0EA5E9),
                                Color(0xFF06B6D4)
                            ),
                            badge = "View",
                            onClick = { /* TODO */ }
                        )
                    }

                    item {
                        PremiumActionCard(
                            title = "Current Shift",
                            subtitle = "Shift details",
                            icon = Icons.Rounded.Schedule,
                            gradientColors = listOf(
                                Color(0xFF06D6A6),
                                Color(0xFF0EA5E9)
                            ),
                            badge = "Active",
                            onClick = { /* TODO */ }
                        )
                    }

                    item {
                        PremiumActionCard(
                            title = "Profile",
                            subtitle = "Personal details",
                            icon = Icons.Rounded.Person,
                            gradientColors = listOf(
                                Color(0xFF0EA5E9),
                                Color(0xFF06D6A6)
                            ),
                            badge = "View",
                            onClick = { /* TODO */ }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Access Info Card
                QuickAccessInfoCard()
            }
        }

        // Decorative background elements
        AttendantDecorativeCircles()
    }
}

@Composable
fun PremiumAttendantWelcomeCard(fullName: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
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
                shape = RoundedCornerShape(24.dp)
            )
            .padding(24.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF06D6A6),
                                    Color(0xFF0EA5E9)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column {
                    Text(
                        text = "Welcome, Attendant",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFA1A5B7),
                        fontSize = 12.sp
                    )
                    Text(
                        text = fullName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun QuickStatsRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickStatItem(
            label = "Today Sales",
            value = "KES 2,500",
            icon = Icons.Rounded.TrendingUp,
            color = Color(0xFF06D6A6),
            modifier = Modifier.weight(1f)
        )
        QuickStatItem(
            label = "Transactions",
            value = "12",
            icon = Icons.Rounded.SwapHoriz,
            color = Color(0xFF0EA5E9),
            modifier = Modifier.weight(1f)
        )
        QuickStatItem(
            label = "Shift Status",
            value = "Active",
            icon = Icons.Rounded.RadioButtonChecked,
            color = Color(0xFF06B6D4),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun QuickStatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E293B))
            .border(
                width = 1.5.dp,
                color = color.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(18.dp)
            )

            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun PremiumActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradientColors: List<Color>,
    badge: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(colors = gradientColors)
            )
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = gradientColors[0].copy(alpha = 0.3f)
            )
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Icon(
                    imageVector = Icons.Rounded.ArrowForward,
                    contentDescription = "Navigate",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun QuickAccessInfoCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E293B))
            .border(
                width = 1.5.dp,
                color = Color(0xFF06D6A6).copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = null,
                    tint = Color(0xFF06D6A6),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Pump Status",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFF06D6A6),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Text(
                text = "All pumps operational. Your shift is active until 18:00. Ensure M-Pesa payments are confirmed before closing transactions.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFA1A5B7),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun AttendantDecorativeCircles() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(400.dp)
                .offset(x = -150.dp, y = -150.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF06D6A6).copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .size(400.dp)
                .offset(x = 200.dp, y = 500.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF0EA5E9).copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}