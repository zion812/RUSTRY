package com.rio.rustry.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Enhanced Theme System for Rooster Platform
 * 
 * Provides comprehensive design system including:
 * - Extended color palette
 * - Enhanced typography scale
 * - Semantic color tokens
 * - Performance-optimized theme switching
 * - Accessibility improvements
 */

// Extended Color Palette
object RoosterColors {
    // Primary Brand Colors
    val Primary50 = Color(0xFFFFF3E0)
    val Primary100 = Color(0xFFFFE0B2)
    val Primary200 = Color(0xFFFFCC80)
    val Primary300 = Color(0xFFFFB74D)
    val Primary400 = Color(0xFFFFA726)
    val Primary500 = Color(0xFFFF9800) // Main brand color
    val Primary600 = Color(0xFFFB8C00)
    val Primary700 = Color(0xFFF57C00)
    val Primary800 = Color(0xFFEF6C00)
    val Primary900 = Color(0xFFE65100)
    
    // Secondary Colors (Green for health/success)
    val Secondary50 = Color(0xFFE8F5E8)
    val Secondary100 = Color(0xFFC8E6C9)
    val Secondary200 = Color(0xFFA5D6A7)
    val Secondary300 = Color(0xFF81C784)
    val Secondary400 = Color(0xFF66BB6A)
    val Secondary500 = Color(0xFF4CAF50) // Main secondary
    val Secondary600 = Color(0xFF43A047)
    val Secondary700 = Color(0xFF388E3C)
    val Secondary800 = Color(0xFF2E7D32)
    val Secondary900 = Color(0xFF1B5E20)
    
    // Tertiary Colors (Blue for information)
    val Tertiary50 = Color(0xFFE3F2FD)
    val Tertiary100 = Color(0xFFBBDEFB)
    val Tertiary200 = Color(0xFF90CAF9)
    val Tertiary300 = Color(0xFF64B5F6)
    val Tertiary400 = Color(0xFF42A5F5)
    val Tertiary500 = Color(0xFF2196F3) // Main tertiary
    val Tertiary600 = Color(0xFF1E88E5)
    val Tertiary700 = Color(0xFF1976D2)
    val Tertiary800 = Color(0xFF1565C0)
    val Tertiary900 = Color(0xFF0D47A1)
    
    // Semantic Colors
    val Success = Secondary500
    val Warning = Color(0xFFFF9800)
    val Error = Color(0xFFF44336)
    val Info = Tertiary500
    
    // Neutral Colors
    val Neutral50 = Color(0xFFFAFAFA)
    val Neutral100 = Color(0xFFF5F5F5)
    val Neutral200 = Color(0xFFEEEEEE)
    val Neutral300 = Color(0xFFE0E0E0)
    val Neutral400 = Color(0xFFBDBDBD)
    val Neutral500 = Color(0xFF9E9E9E)
    val Neutral600 = Color(0xFF757575)
    val Neutral700 = Color(0xFF616161)
    val Neutral800 = Color(0xFF424242)
    val Neutral900 = Color(0xFF212121)
}

// Enhanced Typography
val RoosterTypography = Typography(
    // Display styles
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    
    // Headline styles
    headlineLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    
    // Title styles
    titleLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    
    // Body styles
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    
    // Label styles
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// Enhanced Shapes
val RoosterShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
)

// Light Color Scheme
private val LightColorScheme = lightColorScheme(
    primary = RoosterColors.Primary500,
    onPrimary = Color.White,
    primaryContainer = RoosterColors.Primary100,
    onPrimaryContainer = RoosterColors.Primary900,
    
    secondary = RoosterColors.Secondary500,
    onSecondary = Color.White,
    secondaryContainer = RoosterColors.Secondary100,
    onSecondaryContainer = RoosterColors.Secondary900,
    
    tertiary = RoosterColors.Tertiary500,
    onTertiary = Color.White,
    tertiaryContainer = RoosterColors.Tertiary100,
    onTertiaryContainer = RoosterColors.Tertiary900,
    
    error = RoosterColors.Error,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF7F2FA),
    onSurfaceVariant = Color(0xFF49454F),
    
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0)
)

// Dark Color Scheme
private val DarkColorScheme = darkColorScheme(
    primary = RoosterColors.Primary400,
    onPrimary = RoosterColors.Primary900,
    primaryContainer = RoosterColors.Primary700,
    onPrimaryContainer = RoosterColors.Primary100,
    
    secondary = RoosterColors.Secondary400,
    onSecondary = RoosterColors.Secondary900,
    secondaryContainer = RoosterColors.Secondary800,
    onSecondaryContainer = RoosterColors.Secondary200,
    
    tertiary = RoosterColors.Tertiary400,
    onTertiary = RoosterColors.Tertiary900,
    tertiaryContainer = RoosterColors.Tertiary800,
    onTertiaryContainer = RoosterColors.Tertiary200,
    
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F)
)

// Theme Preferences
data class ThemePreferences(
    val isDarkMode: Boolean = false,
    val useDynamicColor: Boolean = true,
    val fontSize: FontScale = FontScale.Normal
)

enum class FontScale(val scale: Float) {
    Small(0.85f),
    Normal(1.0f),
    Large(1.15f),
    ExtraLarge(1.3f)
}

// Enhanced Theme Composable
@Composable
fun RoosterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    preferences: ThemePreferences = ThemePreferences(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S -> {
            val context = androidx.compose.ui.platform.LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = RoosterTypography,
        shapes = RoosterShapes,
        content = content
    )
}

// Theme utilities
object ThemeUtils {
    @Composable
    fun isDarkTheme(): Boolean = isSystemInDarkTheme()
    
    @Composable
    fun getSemanticColor(type: SemanticColorType): Color {
        return when (type) {
            SemanticColorType.Success -> RoosterColors.Success
            SemanticColorType.Warning -> RoosterColors.Warning
            SemanticColorType.Error -> RoosterColors.Error
            SemanticColorType.Info -> RoosterColors.Info
        }
    }
}

enum class SemanticColorType {
    Success, Warning, Error, Info
}