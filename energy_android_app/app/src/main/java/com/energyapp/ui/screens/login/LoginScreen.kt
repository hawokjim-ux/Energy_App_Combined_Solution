package com.energyapp.ui.screens.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showPassword by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess && uiState.user != null) {
            // Handle null roleName properly
            val roleName = uiState.user?.roleName ?: "Unknown"
            onLoginSuccess(roleName)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0E27),
                        Color(0xFF1A1F3A),
                        Color(0xFF0F1419)
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .shadow(
                        elevation = 24.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Color(0xFF4A90E2).copy(alpha = 0.3f)
                    )
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1E2742),
                                Color(0xFF2A3551),
                                Color(0xFF1E2742)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF4A90E2).copy(alpha = 0.3f),
                                Color(0xFF63B3ED).copy(alpha = 0.2f)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Logo
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF4A90E2),
                                    Color(0xFF357ABD)
                                )
                            )
                        )
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = Color(0xFF4A90E2).copy(alpha = 0.5f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚡",
                        style = MaterialTheme.typography.displayLarge,
                        fontSize = 48.sp
                    )
                }

                // Title
                Text(
                    text = "Energy Station",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color(0xFFE8F4F8),
                    fontSize = 28.sp
                )

                // Subtitle
                Text(
                    text = "Sales Management System",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFB8C5D0),
                    fontSize = 13.sp
                )

                // Version
                Text(
                    text = "v3.0.0.1",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF8996A3),
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Username Field
                ModernLoginTextField(
                    value = uiState.username,
                    onValueChange = viewModel::onUsernameChange,
                    label = "Username",
                    icon = Icons.Rounded.Person,
                    enabled = !uiState.isLoading,
                    placeholder = "Enter username"
                )

                // Password Field
                ModernLoginTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = "Password",
                    icon = Icons.Rounded.Lock,
                    isPassword = true,
                    showPassword = showPassword,
                    onShowPasswordToggle = { showPassword = !showPassword },
                    enabled = !uiState.isLoading,
                    placeholder = "Enter password"
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Login Button
                Button(
                    onClick = viewModel::login,
                    enabled = !uiState.isLoading && uiState.username.isNotEmpty() && uiState.password.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(14.dp),
                            ambientColor = Color(0xFF4A90E2).copy(alpha = 0.4f)
                        ),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4A90E2),
                        disabledContainerColor = Color(0xFF4A90E2).copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = "Login",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Demo Credentials
                DemoCredentialsLoginCard()

                // Footer
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = "© 2024 Energy App v3.0.0.1",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF8996A3),
                        fontSize = 10.sp
                    )
                    Text(
                        text = "Full rights: Jimhawkins Korir",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF8996A3),
                        fontSize = 10.sp
                    )
                    Text(
                        text = "Powered by Hawkinsoft Solutions",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF4A90E2),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
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
    placeholder: String = ""
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFFB8C5D0),
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F1419))
                .border(
                    width = 1.dp,
                    color = Color(0xFF4A90E2).copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color(0xFF4A90E2),
                    modifier = Modifier.size(20.dp)
                )

                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = enabled,
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.Transparent),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFFE8F4F8),
                        fontSize = 14.sp
                    ),
                    placeholder = {
                        Text(
                            text = placeholder,
                            color = Color(0xFF6B7280),
                            fontSize = 13.sp
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
                        cursorColor = Color(0xFF4A90E2)
                    )
                )

                if (isPassword && onShowPasswordToggle != null) {
                    IconButton(
                        onClick = onShowPasswordToggle,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = Color(0xFF4A90E2),
                            modifier = Modifier.size(18.dp)
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
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F1419))
            .border(
                width = 1.dp,
                color = Color(0xFF4A90E2).copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = "Demo Credentials",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF4A90E2),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
            Text(
                text = "Admin: admin / admin123",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFB8C5D0),
                fontSize = 10.sp
            )
            Text(
                text = "User: attendant1 / pass123",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFB8C5D0),
                fontSize = 10.sp
            )
        }
    }
}