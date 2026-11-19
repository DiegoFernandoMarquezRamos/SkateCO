package com.skateco.sco.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Settings
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

// Modelos Mock para Perfil y Moderación
data class MockReportItem(
    val id: String,
    val spotName: String,
    val reportReason: String,
    val reporterUsername: String,
    val dateReported: String,
    val status: String // 'PENDIENTE'
)

data class MockUserSpot(
    val id: String,
    val name: String,
    val groundState: String,
    val obstacleLabel: String,
    val validVotes: Int
)

/**
 * VISTA-05: Perfil del Usuario y Panel de Moderación Admin (Profile & Admin)
 * Integra pestañas TabRow de Material 3 para Mis Spots, Guardados y Moderación de Reportes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileAdminScreen(
    onSignOutClick: () -> Unit = {},
    onSpotClick: (String) -> Unit = {}
) {
    var selectedTabIndex by rememberSaveable { mutableStateOf(2) } // Por defecto en Moderación Admin para mostrar la funcionalidad completa
    val tabs = listOf("Mis Spots (2)", "Guardados (5)", "Moderación Admin (2)")

    // Lista mockeada de reportes pendientes de moderación (ADMIN)
    val reportsList = remember {
        mutableStateListOf(
            MockReportItem(
                id = "rep-1",
                spotName = "Ledge Renfe Nuevos Ministerios",
                reportReason = "Bordillo destrozado por obras municipales. Intransitable con patín.",
                reporterUsername = "skater_vallecas",
                dateReported = "Hace 2 horas",
                status = "PENDIENTE"
            ),
            MockReportItem(
                id = "rep-2",
                spotName = "Plaza de los Cubos (Escaleras)",
                reportReason = "Instalación de valla perimetral y seguridad privada hostil.",
                reporterUsername = "diego_street",
                dateReported = "Hace 1 día",
                status = "PENDIENTE"
            )
        )
    }

    // Lista mockeada de spots del usuario
    val userSpots = remember {
        listOf(
            MockUserSpot("spot-1", "Plaza de Colón", "LISO", "Bordillo / Cajón", 38),
            MockUserSpot("spot-2", "Skatepark Madrid Río", "LISO", "Bowl / Transición", 78)
        )
    }

    Scaffold(
        containerColor = AsphaltDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mi Perfil SCO",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { /* Ajustes */ }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ajustes",
                            tint = TextMuted
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AsphaltDark
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Cabecera de Perfil: Avatar, Rol y Métricas
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ConcreteSurface),
                border = BorderStroke(1.dp, SurfaceBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar circular con borde lima
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(ConcreteSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🛹", fontSize = 28.sp)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Broxter FM",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                // Badge Rol ADMIN
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = UrethaneLime,
                                    contentColor = LimeOnContainer
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AdminPanelSettings,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "ADMIN",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "yodiegojr@gmail.com • Madrid Centro",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )

                            Text(
                                text = "Stance: Goofy • Street & Bowls",
                                style = MaterialTheme.typography.bodySmall,
                                color = UrethaneLime
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = SurfaceBorder)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Contadores de Gamificación Comunitaria
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ProfileStatItem(title = "SPOTS", value = "12")
                        ProfileStatItem(title = "VALIDACIONES", value = "48")
                        ProfileStatItem(title = "REPUTACIÓN", value = "1.2k")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Pestañas de Navegación TabRow de Material 3
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = AsphaltDark,
                contentColor = UrethaneLime,
                divider = { HorizontalDivider(color = SurfaceBorder) }
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTabIndex == index
                    Tab(
                        selected = isSelected,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) UrethaneLime else TextMuted
                            )
                        }
                    )
                }
            }

            // 3. Contenido de la Pestaña Seleccionada
            when (selectedTabIndex) {
                2 -> {
                    // Pestaña de Moderación ADMIN con Tarjetas de Reportes
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "COLA DE REPORTES ACTIVOS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = UrethaneLime,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "${reportsList.size} pendientes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                        }

                        if (reportsList.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = ConcreteSurface),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No hay reportes pendientes de moderación.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        } else {
                            items(reportsList, key = { it.id }) { report ->
                                AdminReportCard(
                                    report = report,
                                    onApprove = { reportsList.remove(report) },
                                    onArchive = { reportsList.remove(report) }
                                )
                            }
                        }
                    }
                }
                else -> {
                    // Pestaña de Mis Spots o Guardados
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(userSpots, key = { it.id }) { spot ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = ConcreteSurface),
                                border = BorderStroke(1.dp, SurfaceBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = spot.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = TextWhite,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${spot.obstacleLabel} • Suelo ${spot.groundState}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextMuted
                                        )
                                    }

                                    Button(
                                        onClick = { onSpotClick(spot.id) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ConcreteSurfaceVariant,
                                            contentColor = UrethaneLime
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Ver Ficha", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta de Reporte para el Administrador con botones Aprobar y Archivar
 */
@Composable
fun AdminReportCard(
    report: MockReportItem,
    onApprove: () -> Unit,
    onArchive: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ConcreteSurface),
        border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f))
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
                        imageVector = Icons.Default.ReportProblem,
                        contentDescription = "Reporte",
                        tint = DangerRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = report.spotName,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = report.dateReported,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "\"${report.reportReason}\"",
                style = MaterialTheme.typography.bodyMedium,
                color = TextWhite.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Reportado por @${report.reporterUsername}",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Botones de acción rápida: Aprobar (Mantener activo) vs Archivar (Retirar spot)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Botón Aprobar / Mantener Spot
                Button(
                    onClick = onApprove,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = UrethaneLime,
                        contentColor = LimeOnContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Aprobar",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Aprobar",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Botón Archivar / Retirar Spot
                OutlinedButton(
                    onClick = onArchive,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = DangerRed
                    ),
                    border = BorderStroke(1.dp, DangerRed)
                ) {
                    Icon(
                        imageVector = Icons.Default.Archive,
                        contentDescription = "Archivar",
                        tint = DangerRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Archivar",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileStatItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = UrethaneLime,
            fontWeight = FontWeight.Black
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun ProfileAdminScreenPreview() {
    SCOTheme {
        ProfileAdminScreen()
    }
}
