package com.skateco.sco.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skateco.sco.ui.theme.*

// Modelo Mock detallado del Spot según base_de_datos_SCO.sql
data class MockSpotDetail(
    val id: String,
    val name: String,
    val description: String,
    val groundState: String, // 'LISO', 'RUGOSO', 'AGRIETADO'
    val creatorUsername: String,
    val dateCreated: String,
    val obstacles: List<MockSpotObstacle>,
    val positiveVotes: Int,
    val totalValidations: Int
)

data class MockSpotObstacle(
    val typeName: String,
    val details: String
)

/**
 * VISTA-03: Detalle del Spot (Spot Detail View)
 * Proporciona ficha técnica, verificación comunitaria GPS y acciones de navegación.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotDetailScreen(
    spotId: String = "spot-1",
    onBackClick: () -> Unit = {},
    onNavigateClick: () -> Unit = {},
    onReportClick: () -> Unit = {},
    onValidateGpsClick: () -> Unit = {}
) {
    var isBookmarked by rememberSaveable { mutableStateOf(false) }
    var hasValidatedGps by rememberSaveable { mutableStateOf(false) }

    // Datos semilla mockeados acordes con base_de_datos_SCO.sql
    val spot = remember {
        MockSpotDetail(
            id = spotId,
            name = "Plaza de Colón",
            description = "Mítico spot de street en el centro de Madrid. Suelo de granito ultraliso con bordillos encerados, caída limpia y bancos de doble altura. Zona concurrida, mejor patinar a partir de las 20:00 h.",
            groundState = "LISO",
            creatorUsername = "alvaro_sk8",
            dateCreated = "12 Nov 2026",
            obstacles = listOf(
                MockSpotObstacle("Bordillo / Cajón", "Granito encerado, 40cm altura"),
                MockSpotObstacle("Escaleras", "5 escalones con caída despejada"),
                MockSpotObstacle("Manual Pad", "Doble altura con chapa metálica"),
                MockSpotObstacle("Plano Inclinado", "Transición suave de entrada")
            ),
            positiveVotes = 38,
            totalValidations = 42
        )
    }

    Scaffold(
        containerColor = AsphaltDark,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = ConcreteSurface.copy(alpha = 0.8f),
                            contentColor = TextWhite
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { isBookmarked = !isBookmarked },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = ConcreteSurface.copy(alpha = 0.8f),
                            contentColor = if (isBookmarked) UrethaneLime else TextWhite
                        )
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Guardar en Favoritos"
                        )
                    }
                    IconButton(
                        onClick = { /* Compartir */ },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = ConcreteSurface.copy(alpha = 0.8f),
                            contentColor = TextWhite
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartir"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Cabecera Visual del Spot con Gradiente Urbano
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(ConcreteSurfaceVariant, AsphaltDark)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Ilustración / Placeholder de alta fidelidad
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🛹", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "FOTO VERIFICADA POR LA COMUNIDAD",
                        style = MaterialTheme.typography.labelSmall,
                        color = UrethaneLime,
                        letterSpacing = 2.sp
                    )
                }

                // Badge de estado de suelo superpuesto en la esquina inferior izquierda
                val statusColor = when (spot.groundState) {
                    "LISO" -> StatusSmooth
                    "RUGOSO" -> StatusRough
                    else -> StatusCracked
                }

                Surface(
                    shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp),
                    color = statusColor,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "SUELO ${spot.groundState}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Black,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // 2. Información Principal y Metadatos
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = spot.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = TextWhite
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Añadido por @${spot.creatorUsername} • ${spot.dateCreated}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Descripción
                Text(
                    text = spot.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextWhite.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 3. Ficha Técnica de Obstáculos Físicos (Relación 1:N)
                Text(
                    text = "OBSTÁCULOS FÍSICOS DISPONIBLES",
                    style = MaterialTheme.typography.labelSmall,
                    color = UrethaneLime,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    spot.obstacles.forEach { obstacle ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = ConcreteSurface
                            ),
                            border = BorderStroke(1.dp, SurfaceBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = obstacle.typeName,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = obstacle.details,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 4. Panel de Validación Comunitaria GPS
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ConcreteSurfaceVariant
                    ),
                    border = BorderStroke(1.dp, UrethaneLime.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.GpsFixed,
                                    contentDescription = "Validación GPS",
                                    tint = UrethaneLime,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "VALIDACIÓN POR GPS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = UrethaneLime,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "${spot.positiveVotes}/${spot.totalValidations} votos positivos",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (hasValidatedGps) {
                                "¡Has validado que este spot sigue patinable hoy!"
                            } else {
                                "Valida in-situ si el spot sigue patinable y libre de obras o seguridad."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextWhite
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Botón Primario: Validar Spot (GPS)
                        Button(
                            onClick = {
                                hasValidatedGps = !hasValidatedGps
                                onValidateGpsClick()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hasValidatedGps) ConcreteSurface else UrethaneLime,
                                contentColor = if (hasValidatedGps) UrethaneLime else LimeOnContainer
                            ),
                            border = if (hasValidatedGps) BorderStroke(1.dp, UrethaneLime) else null
                        ) {
                            Icon(
                                imageVector = Icons.Default.ThumbUp,
                                contentDescription = "Validar",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (hasValidatedGps) "SPOT VALIDADO POR TI" else "VALIDAR SPOT (GPS)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 5. Botones Secundarios: "Cómo llegar" y "Reportar"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cómo llegar
                    OutlinedButton(
                        onClick = onNavigateClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextWhite
                        ),
                        border = BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Directions,
                            contentDescription = "Cómo llegar",
                            tint = UrethaneLime,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cómo llegar",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Reportar Problema
                    OutlinedButton(
                        onClick = onReportClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = DangerRed
                        ),
                        border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReportProblem,
                            contentDescription = "Reportar",
                            tint = DangerRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reportar",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun SpotDetailScreenPreview() {
    SCOTheme {
        SpotDetailScreen()
    }
}
