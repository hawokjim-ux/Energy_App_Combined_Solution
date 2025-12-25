package com.energyapp.ui.screens.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.energyapp.ui.components.ErrorDialog
import com.energyapp.ui.components.LoadingDialog
import com.energyapp.ui.theme.*

/**
 * Login Screen - Super Modern 2024 Design
 * Light theme with vibrant gradients for outdoor visibility
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showPassword by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess && uiState.user != null) {
            val roleName = uiState.user?.roleName ?: "Unknown"
            onLoginSuccess(roleName)
        }
    }

    // Beautiful Light Background with subtle gradient
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        LightBackground,
                        Color(0xFFEEF2FF), // Light purple tint
                        LightBackground
                    )
                )
            )
    ) {
        // Decorative gradient circles
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-100).dp, y = (-50).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GradientPurple.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = 100.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GradientCyan.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomStart)
                .offset(x = 50.dp, y = 50.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GradientPink.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 24.dp,
                        shape = RoundedCornerShape(28.dp),
                        spotColor = GradientPurple.copy(alpha = 0.15f)
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(CardBackground)
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                GradientPurple.copy(alpha = 0.2f),
                                GradientCyan.copy(alpha = 0.2f),
                                GradientPink.copy(alpha = 0.2f)
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Stunning Gradient Logo
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .shadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(24.dp),
                            spotColor = GradientPurple.copy(alpha = 0.4f)
                        )
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    GradientPurple,
                                    GradientCyan,
                                    GradientPink
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚡",
                        fontSize = 52.sp
                    )
                }

                // Title with Gradient Effect
                Text(
                    text = "Energy Station",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = OnSurface,
                    fontSize = 28.sp
                )

                // Subtitle
                Text(
                    text = "Sales Management System",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    fontSize = 14.sp
                )

                // Modern Version Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = GradientPurple.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "v3.0.0.1",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = GradientPurple,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Username Field with Gradient Accent
                ModernLoginTextField(
                    value = uiState.username,
                    onValueChange = viewModel::onUsernameChange,
                    label = "Username",
                    icon = Icons.Rounded.Person,
                    enabled = !uiState.isLoading,
                    placeholder = "Enter username",
                    accentColor = GradientPurple
                )

                // Password Field with Gradient Accent
                ModernLoginTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = "Password",
                    icon = Icons.Rounded.Lock,
                    isPassword = true,
                    showPassword = showPassword,
                    onShowPasswordToggle = { showPassword = !showPassword },
                    enabled = !uiState.isLoading,
                    placeholder = "Enter password",
                    accentColor = GradientCyan
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Stunning Gradient Login Button
                Button(
                    onClick = viewModel::login,
                    enabled = !uiState.isLoading && uiState.username.isNotEmpty() && uiState.password.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = GradientPurple.copy(alpha = 0.4f)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = if (!uiState.isLoading && uiState.username.isNotEmpty() && uiState.password.isNotEmpty()) {
                                    Brush.horizontalGradient(
                                        colors = listOf(GradientPurple, GradientCyan, GradientPink)
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        colors = listOf(Color.Gray.copy(alpha = 0.4f), Color.Gray.copy(alpha = 0.3f))
                                    )
                                },
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Login",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Demo Credentials Card
                DemoCredentialsLoginCard()

                // Footer
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Text(
                        text = "© 2024 Energy App v3.0.0.1",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Full rights: Jimhawkins Korir",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Powered by Hawkinsoft Solutions",
                        style = MaterialTheme.typography.labelSmall,
                        color = GradientPurple,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }

    if (uiState.isLoading) {
        LoadingDialog(message = "Authenticating...")
    }

    if (uiState.error != null) {
        ErrorDialog(
            message = uiState.error!!,
            onDismiss = viewModel::clearError
        )
    }
}

@Composable
fun ModernLoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false,
    showPassword: Boolean = false,
    onShowPasswordToggle: (() -> Unit)? = null,
    enabled: Boolean = true,
    placeholder: String = "",
    accentColor: Color = GradientPurple
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = OnSurface,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(14.dp),
                    spotColor = accentColor.copy(alpha = 0.1f)
                )
                .clip(RoundedCornerShape(14.dp))
                .background(LightSurfaceVariant)
                .border(
                    width = 1.5.dp,
                    color = accentColor.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Gradient Icon Circle
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(accentColor, accentColor.copy(alpha = 0.7f))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = enabled,
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.Transparent),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = OnSurface,
                        fontSize = 15.sp
                    ),
                    placeholder = {
                        Text(
                            text = placeholder,
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    },
                    visualTransformation = if (isPassword && !showPassword) {
                        PasswordVisualTransformation()
                    } else {
                        VisualTransformation.None
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        cursorColor = accentColor
                    )
                )

                if (isPassword && onShowPasswordToggle != null) {
                    IconButton(
                        onClick = onShowPasswordToggle,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DemoCredentialsLoginCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(14.dp),
                spotColor = NeonGreen.copy(alpha = 0.1f)
            )
            .clip(RoundedCornerShape(14.dp))
            .background(SuccessLight)
            .border(
                width = 1.dp,
                color = Success.copy(alpha = 0.3f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            brush = Brush.linearGradient(
                                listOf(IconGradientGreen1, IconGradientGreen2)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💡", fontSize = 12.sp)
                }
                Text(
                    text = "Demo Credentials",
                    style = MaterialTheme.typography.labelMedium,
                    color = Success,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Text(
                text = "Admin: admin / admin123",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurface.copy(alpha = 0.8f),
                fontSize = 11.sp
            )
            Text(
                text = "User: attendant1 / pass123",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurface.copy(alpha = 0.8f),
                fontSize = 11.sp
            )
        }
    }
}