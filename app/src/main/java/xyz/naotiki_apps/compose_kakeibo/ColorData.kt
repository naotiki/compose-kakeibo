package xyz.naotiki_apps.compose_kakeibo

import androidx.annotation.ColorInt
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb

val colors: List<Color> = listOf(
    Color.Transparent,
    Color.LightGray,
    Color.Red,
    Color.Magenta,
    Color.Blue,
    Color.Cyan,
    Color.Green,
    Color.Yellow
)

data class ColorData(@ColorInt var colorInt: Int) {
    fun toColor(): Color {
        return Color(colorInt)
    }

    companion object {
        fun Color.toColorData() = ColorData(this.toArgb())
    }
}

/**
 * Generate content colors (e.g. Text color) from background colors.
 *   if the background color luminance is bright, return Black.
 *   if it is dark, return White.
 *   if it is transparent (alpha is less than 0.4), return [onSurface color] [androidx.compose.material.Colors.onSurface].
 *
 *   Calculated result is based on the formula for relative luminance defined in WCAG 2.0.
 *   @return Easy to see color,White or Black
 * */
@Composable
fun Color.generateContentColor(): Color {
    val brightness = luminance()
    return if (alpha <= 0.4)
        MaterialTheme.colors.onSurface
    else if (brightness < 0.5) {
        //background is dark
        Color.White
    } else {
        //light
        Color.Black
    }
}
