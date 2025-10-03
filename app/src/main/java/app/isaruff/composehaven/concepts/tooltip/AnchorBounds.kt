package app.isaruff.composehaven.concepts.tooltip

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntRect

/**Adjust for Window Insets to cut scrim accordingly*/
@Composable
internal fun IntRect.safeBounds(insetsPadding: PaddingValues): IntRect {
    val layoutDirection = LocalLayoutDirection.current
    val density = LocalDensity.current
    val topInsetPx = with(density) { insetsPadding.calculateTopPadding().roundToPx() }
    val leftInsetPx =
        with(density) { insetsPadding.calculateLeftPadding(layoutDirection).roundToPx() }

    return IntRect(
        top = top - topInsetPx,
        left = left - leftInsetPx,
        bottom = bottom - topInsetPx,
        right = right - leftInsetPx
    )
}
