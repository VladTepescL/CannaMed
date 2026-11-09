package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

val TherapeuticEmeraldDarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = EmeraldOnPrimary,
    primaryContainer = EmeraldPrimaryContainer,
    onPrimaryContainer = EmeraldOnPrimaryContainer,
    secondary = EmeraldSecondary,
    onSecondary = EmeraldOnSecondary,
    secondaryContainer = EmeraldSecondaryContainer,
    onSecondaryContainer = EmeraldOnSecondaryContainer,
    tertiary = EmeraldTertiary,
    onTertiary = EmeraldOnTertiary,
    tertiaryContainer = EmeraldTertiaryContainer,
    onTertiaryContainer = EmeraldOnTertiaryContainer,
    background = EmeraldBackground,
    onBackground = EmeraldOnBackground,
    surface = EmeraldSurface,
    onSurface = EmeraldOnSurface,
    surfaceVariant = EmeraldSurfaceVariant,
    onSurfaceVariant = EmeraldOnSurfaceVariant,
    outline = EmeraldOutline,
    outlineVariant = EmeraldOutlineVariant,
    surfaceContainerLowest = EmeraldSurfaceContainerLowest,
    surfaceContainerLow = EmeraldSurfaceContainerLow,
    surfaceContainer = EmeraldSurfaceContainer,
    surfaceContainerHigh = EmeraldSurfaceContainerHigh,
    surfaceContainerHighest = EmeraldSurfaceContainerHighest,
    error = EmeraldError,
    onError = EmeraldOnError,
    errorContainer = EmeraldErrorContainer,
    onErrorContainer = EmeraldOnErrorContainer
)

@Composable
fun CannaMedTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TherapeuticEmeraldDarkColorScheme,
        typography = Typography,
        content = content
    )
}
