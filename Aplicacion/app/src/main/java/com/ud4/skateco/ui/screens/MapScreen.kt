package com.skateco.sco.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
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

// Modelo Mock para los spots en el mapa
data class MockSpotMarker(
    val id: String,
    val name: String,
    val obstacleCode: String,
    val obstacleLabel: String,
    val groundState: String, // 'LISO', 'RUGOSO', 'AGRIETADO'
    val distanceMeters: Int,
    val rating: Float,
    val validationCount: Int,
    val posXFraction: Float, // posición relativa en el canvas oscuro (0..1)
    val posYFraction: Float
)

/**
 * VISTA-02: Mapa Principal y Explorador de Spots (Main Map & Explorer)
 * Integra catálogo de obstáculos de la base de datos SCO (tipos_obstaculo).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onSpotClick: (String) -> Unit = {},
    onCreateSpotClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedObstacle by rememberSaveable { mutableStateOf("ALL") }

    // Catálogo derivado de tipos_obstaculo en PostgreSQL
    val obstacleFilters = remember {
        listOf(
            "ALL" to "Todos",
            "LEDGE" to "Bordillo / Cajón",
            "STAIRS" to "Escaleras",
            "HANDRAIL" to "Barandilla",
            "MANUAL_PAD" to "Manual Pad",
            "BANK" to "Plano Inclinado",
            "GAP" to "Gap / Salto",
            "BOWL" to "Bowl / Transición"
        )
    }

    // Spots mockeados basados en base_de_datos_SCO.sql
    val mockSpots = remember {
        listOf(
            MockSpotMarker(
                id = "spot-1",
                name = "Plaza de Colón",
                obstacleCode = "LEDGE",
                obstacleLabel = "Bordillo / Cajón",
                groundState = "LISO",
                distanceMeters = 850,
                rating = 4.8f,
                validationCount = 42,
                posXFraction = 0.45f,
                posYFraction = 0.38f
            ),
            MockSpotMarker(
                id = "spot-2",
                name = "Skatepark Madrid Río",
                obstacleCode = "BOWL",
                obstacleLabel = "Bowl / Transición",
                groundState = "LISO",
                distanceMeters = 2400,
                rating = 4.9f,
                validationCount = 78,
                posXFraction = 0.72f,
                posYFraction = 0.60f
            ),
            MockSpotMarker(
                id = "spot-3",
                name = "Macba Ledges Centro",
                obstacleCode = "MANUAL_PAD",
                obstacleLabel = "Manual Pad",
                groundState = "LISO",
                distanceMeters = 1100,
                rating = 4.6f,
                validationCount = 29,
                posXFraction = 0.25f,
                posYFraction = 0.52f
            )
        )
    }

    var selectedSpot by remember { mutableStateOf(mockSpots.first()) }

    Scaffold(
        containerColor = AsphaltDark,
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 140.dp) // Deja espacio para SpotPreviewCard
            ) {
                // FAB Secundario: Mi Ubicación
                SmallFloatingActionButton(
                    onClick = { /* Centrar en GPS */ },
                    containerColor = ConcreteSurface,
                    contentColor = TextWhite,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Mi Ubicación"
                    )
                }

                // FAB Primario: Añadir Spot
                FloatingActionButton(
                    onClick = onCreateSpotClick,
                    containerColor = UrethaneLime,
                    contentColor = LimeOnContainer,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Añadir Spot",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Simulación visual del mapa oscuro (Dark Cartography Grid)
            DarkMapCanvas(
                spots = mockSpots,
                selectedSpotId = selectedSpot.id,
                onMarkerSelect = { spot -> selectedSpot = spot }
            )

            // 2. Cabecera flotante: Barra de Búsqueda y Filtros
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Barra de búsqueda Material 3
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    placeholder = {
                        Text(
                            text = "Buscar spot, plaza, bordillo...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = UrethaneLime
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpiar",
                                    tint = TextMuted
                                )
                            }
                        }
                    },
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

                Spacer(modifier = Modifier.height(10.dp))

                // Fila horizontal de FilterChips de Material 3
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    obstacleFilters.forEach { (code, label) ->
                        val isSelected = selectedObstacle == code
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedObstacle = code },
                            label = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
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
            }

            // 3. Tarjeta Inferior de Previsualización (SpotPreviewCard)
            SpotPreviewCard(
                spot = selectedSpot,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                onDetailClick = { onSpotClick(selectedSpot.id) }
            )
        }
    }
}

/**
 * Lienzo de mapa en tema asfalto oscuro con marcadores y clusters reactivos
 */
@Composable
private fun DarkMapCanvas(
    spots: List<MockSpotMarker>,
    selectedSpotId: String,
    onMarkerSelect: (MockSpotMarker) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AsphaltDark)
    ) {
        val width = maxWidth
        val height = maxHeight

        // Líneas urbanas de fondo simulando calles en cuadrícula
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(6) {
                HorizontalDivider(color = SurfaceBorder.copy(alpha = 0.3f), thickness = 1.dp)
            }
        }

        // Renderizado de Pines de Spots
        spots.forEach { spot ->
            val isSelected = spot.id == selectedSpotId

            Box(
                modifier = Modifier
                    .offset(
                        x = width * spot.posXFraction - 20.dp,
                        y = height * spot.posYFraction - 20.dp
                    )
                    .clickable { onMarkerSelect(spot) }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Pin estilizado
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 44.dp else 36.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) UrethaneLime else ConcreteSurface)
                            .border(
                                width = 2.dp,
                                color = if (isSelected) TextWhite else SurfaceBorder,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = spot.name,
                            tint = if (isSelected) LimeOnContainer else UrethaneLime,
                            modifier = Modifier.size(if (isSelected) 26.dp else 20.dp)
                        )
                    }

                    // Etiqueta flotante del spot
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AsphaltDark.copy(alpha = 0.85f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = spot.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextWhite,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Cluster Indicador de Spots cercanos (+3)
        Box(
            modifier = Modifier
                .offset(x = width * 0.82f, y = height * 0.32f)
                .size(38.dp)
                .clip(CircleShape)
                .background(ConcreteSurfaceVariant)
                .border(2.dp, UrethaneLime, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+3",
                style = MaterialTheme.typography.labelSmall,
                color = UrethaneLime,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Tarjeta inferior deslizable de previsualización del spot seleccionado
 */
@Composable
fun SpotPreviewCard(
    spot: MockSpotMarker,
    modifier: Modifier = Modifier,
    onDetailClick: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ConcreteSurface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Miniatura del Spot con badge de estado de suelo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ConcreteSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🛹", fontSize = 32.sp)

                // Badge de Suelo (LISO / RUGOSO / AGRIETADO)
                val statusColor = when (spot.groundState) {
                    "LISO" -> StatusSmooth
                    "RUGOSO" -> StatusRough
                    else -> StatusCracked
                }

                Surface(
                    shape = RoundedCornerShape(topStart = 8.dp, bottomEnd = 8.dp),
                    color = statusColor,
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Text(
                        text = spot.groundState,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Información técnica y botón de navegación
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = spot.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = spot.obstacleLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = UrethaneLime
                    )

                    Text(text = "•", color = TextMuted)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = "Distancia",
                            tint = TextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${spot.distanceMeters} m",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Puntuación y Validaciones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = StarGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${spot.rating} (${spot.validationCount} GPS)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextWhite
                        )
                    }

                    // Botón directo al detalle
                    Button(
                        onClick = onDetailClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = UrethaneLime,
                            contentColor = LimeOnContainer
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = "Ver Spot",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun MapScreenPreview() {
    SCOTheme {
        MapScreen()
    }
}
