package com.lennit.cryptolyzer.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LennitDarkScheme = darkColorScheme(
    primary         = LennitCopper,
    onPrimary       = LennitBlack,
    primaryContainer= LennitSurface2,
    secondary       = LennitCopperLight,
    onSecondary     = LennitBlack,
    background      = LennitBlack,
    surface         = LennitSurface,
    surfaceVariant  = LennitSurface2,
    onSurface       = LennitWhite,
    onSurfaceVariant= LennitGrey,
    error           = LennitRed,
    outline         = LennitGreyDark,
)

@Composable
fun LennitTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LennitDarkScheme,
        typography  = Typography(),
        content     = content,
    )
}
