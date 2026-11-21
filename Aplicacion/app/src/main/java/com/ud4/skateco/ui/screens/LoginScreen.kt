package com.ud4.skateco.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ud4.skateco.ui.theme.*

/**
 * VISTA-01: Autenticación y Acceso (Login / Gateway)
 * Plataforma: Android Jetpack Compose (Material Theme 3)
 */
@Composable
fun LoginScreen(
    onSignInClick: (String, String) -> Unit = { _, _ -> },
    onContinueAsGuestClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {}
) {
    var email by rememberSaveable { mutableStateOf("skater@email.com") }
    var password by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var keepSignedIn by rememberSaveable { mutableStateOf(true) }

    Scaffold(
        containerColor = AsphaltDark,
        contentColor = TextWhite
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Branding / Isotipo SCO
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Isotipo Geométrico SCO
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ConcreteSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚡",
                        fontSize = 32.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "SCO",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextWhite,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "THE CITY IS YOUR PARK",
                    style = MaterialTheme.typography.labelSmall,
                    color = UrethaneLime,
                    letterSpacing = 3.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Formulario de Credenciales
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ConcreteSurface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Campo Email
                    Text(
                        text = "EMAIL",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email Icon",
                                tint = TextMuted
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ConcreteSurfaceVariant,
                            unfocusedContainerColor = ConcreteSurfaceVariant,
                            focusedBorderColor = UrethaneLime,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo Password
                    Text(
                        text = "PASSWORD",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Lock Icon",
                                tint = TextMuted
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Password Visibility",
                                    tint = TextMuted
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ConcreteSurfaceVariant,
                            unfocusedContainerColor = ConcreteSurfaceVariant,
                            focusedBorderColor = UrethaneLime,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Opciones auxiliares: Recordar sesión y Olvidé contraseña
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = keepSignedIn,
                                onCheckedChange = { keepSignedIn = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = UrethaneLime,
                                    checkmarkColor = LimeOnContainer,
                                    uncheckedColor = TextMuted
                                )
                            )
                            Text(
                                text = "Keep me signed in",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }

                        TextButton(
                            onClick = onForgotPasswordClick,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Forgot password?",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Botón Principal Sign In
                    Button(
                        onClick = { onSignInClick(email, password) },
                        enabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = UrethaneLime,
                            contentColor = LimeOnContainer
                        )
                    ) {
                        Text(
                            text = "SIGN IN  ➔",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Separador OR y Acceso de Invitado
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = SurfaceBorder
                    )
                    Text(
                        text = "OR",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = SurfaceBorder
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onContinueAsGuestClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Continue as Guest (Explore Map)",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextWhite,
                        textDecoration = TextDecoration.Underline,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer con Versión
            Text(
                text = "SCO / BUILD 2.4.1",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun LoginScreenPreview() {
    SCOTheme {
        LoginScreen()
    }
}
