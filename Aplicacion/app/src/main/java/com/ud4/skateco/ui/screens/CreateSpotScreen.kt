package com.skateco.sco.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skateco.sco.ui.theme.*

/**
 * VISTA-04: Registrar / Añadir Nuevo Spot (Create Spot Form)
 * Integra subida multimedia Edge AI Ready, catálogo de obstáculos y estado del suelo.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateSpotScreen(
    onBackClick: () -> Unit = {},
    onSaveSpotClick: (name: String, description: String, groundState: String, obstacles: Set<String>) -> Unit = { _, _, _, _ -> }
) {
    var spotName by rememberSaveable { mutableStateOf("Plaza de Colón") }
    var spotDescription by rememberSaveable { mutableStateOf("Mítico spot con bordillos pulidos y suelo de granito en el centro.") }
    var selectedGroundState by rememberSaveable { mutableStateOf("LISO") }
    var hasPhotoSelected by rememberSaveable { mutableStateOf(true) }

    // Catálogo inmutable de tipos_obstaculo de PostgreSQL
    val availableObstacles = remember {
        listOf(
            "LEDGE" to "Bordillo / Cajón",
            "STAIRS" to "Escaleras",
            "HANDRAIL" to "Barandilla",
            "MANUAL_PAD" to "Manual Pad",
            "BANK" to "Plano Inclinado",
            "GAP" to "Gap / Salto",
            "BOWL" to "Bowl / Transición"
        )
    }

    val selectedObstacles = remember {
        mutableStateListOf("LEDGE", "MANUAL_PAD")
    }

    val groundStates = listOf(
        Triple("LISO", "Liso", StatusSmooth),
        Triple("RUGOSO", "Rugoso", StatusRough),
        Triple("AGRIETADO", "Agrietado", StatusCracked)
    )

    Scaffold(
        containerColor = AsphaltDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Registrar Nuevo Spot",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = TextWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AsphaltDark
                )
            )
        },
        bottomBar = {
            // Botón Inferior Fijo de Guardar Spot
            Surface(
                color = AsphaltDark,
                border = BorderStroke(1.dp, SurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            onSaveSpotClick(
                                spotName,
                                spotDescription,
                                selectedGroundState,
                                selectedObstacles.toSet()
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = UrethaneLime,
                            contentColor = LimeOnContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Guardar",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GUARDAR SPOT",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Tarjeta de Subida de Fotos con Badge "Edge AI Ready"
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { hasPhotoSelected = !hasPhotoSelected },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ConcreteSurface
                ),
                border = BorderStroke(1.dp, if (hasPhotoSelected) UrethaneLime else SurfaceBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Badge "Edge AI Ready" en la esquina superior derecha
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ConcreteSurfaceVariant,
                        border = BorderStroke(1.dp, UrethaneLime.copy(alpha = 0.6f)),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI",
                                tint = UrethaneLime,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Edge AI Ready",
                                style = MaterialTheme.typography.labelSmall,
                                color = UrethaneLime,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Contenido central de subida
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(ConcreteSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (hasPhotoSelected) Icons.Default.CheckCircle else Icons.Default.AddAPhoto,
                                contentDescription = "Añadir Foto",
                                tint = if (hasPhotoSelected) UrethaneLime else TextWhite,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (hasPhotoSelected) "1 Foto Seleccionada (spot_colon_01.webp)" else "Tocar para capturar o subir evidencia",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Edge AI clasificará automáticamente los bordillos y escalones",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Campo Nombre del Spot
            Text(
                text = "NOMBRE DEL SPOT *",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            OutlinedTextField(
                value = spotName,
                onValueChange = { spotName = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("Ej. Plaza de Colón, DIY Vallecas...", color = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ConcreteSurface,
                    unfocusedContainerColor = ConcreteSurface,
                    focusedBorderColor = UrethaneLime,
                    unfocusedBorderColor = SurfaceBorder,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Campo Descripción
            Text(
                text = "DESCRIPCIÓN Y ACCESO",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            OutlinedTextField(
                value = spotDescription,
                onValueChange = { spotDescription = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("Detalla el acceso, horarios recomendados, iluminación...", color = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ConcreteSurface,
                    unfocusedContainerColor = ConcreteSurface,
                    focusedBorderColor = UrethaneLime,
                    unfocusedBorderColor = SurfaceBorder,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Selector de Estado del Suelo (CHECK: 'LISO', 'RUGOSO', 'AGRIETADO')
            Text(
                text = "ESTADO DEL SUELO (SUPERFICIE)",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groundStates.forEach { (code, label, color) ->
                    val isSelected = selectedGroundState == code
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedGroundState = code },
                        color = if (isSelected) ConcreteSurfaceVariant else ConcreteSurface,
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) color else SurfaceBorder
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) TextWhite else TextMuted,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Selector Múltiple de Obstáculos Físicos
            Text(
                text = "OBSTÁCULOS EN EL SPOT (SELECCIÓN MÚLTIPLE)",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableObstacles.forEach { (code, label) ->
                    val isSelected = selectedObstacles.contains(code)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) {
                                selectedObstacles.remove(code)
                            } else {
                                selectedObstacles.add(code)
                            }
                        },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = LimeOnContainer,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        } else null,
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = ConcreteSurface,
                            labelColor = TextMuted,
                            selectedContainerColor = UrethaneLime,
                            selectedLabelColor = LimeOnContainer
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = SurfaceBorder,
                            selectedBorderColor = UrethaneLime
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 6. Tarjeta Informativa de Geolocalización Detectada
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ConcreteSurface),
                border = BorderStroke(1.dp, SurfaceBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "GPS",
                        tint = UrethaneLime,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Coordenadas fijadas automáticamente por GPS",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Lat: 40.4250, Long: -3.6900 (Madrid Centro)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun CreateSpotScreenPreview() {
    SCOTheme {
        CreateSpotScreen()
    }
}
