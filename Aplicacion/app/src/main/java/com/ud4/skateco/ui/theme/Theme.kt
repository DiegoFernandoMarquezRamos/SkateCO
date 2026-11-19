package com.skateco.sco.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// Configuración del esquema de color Dark para Material Theme 3
private val DarkColorScheme = darkColorScheme(
    primary = UrethaneLime,
    onPrimary = LimeOnContainer,
    primaryContainer = ConcreteSurfaceVariant,
    onPrimaryContainer = UrethaneLime,
    secondary = TextWhite,
    onSecondary = AsphaltDark,
    background = AsphaltDark,
    onBackground = TextWhite,
    surface = ConcreteSurface,
    onSurface = TextWhite,
    surfaceVariant = ConcreteSurfaceVariant,
    onSurfaceVariant = TextMuted,
    outline = SurfaceBorder,
    outlineVariant = SurfaceBorder,
    error = DangerRed,
    onError = TextWhite
)

@Composable
fun SCOTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Por diseño de la app de skate urbana SCO, se fuerza darkColorScheme por defecto
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = SCOTypography,
        content = content
    )
}
