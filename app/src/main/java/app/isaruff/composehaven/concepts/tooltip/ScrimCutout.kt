package app.isaruff.composehaven.concepts.tooltip

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape

fun scrimCutoutShape(
    cutoutLeft: Float,
    cutoutTop: Float,
    cutoutRight: Float,
    cutoutBottom: Float,
    type: CutoutType
): Shape = GenericShape { size, _ ->
    val fullRect = Path().apply {
        addRect(Rect(0f, 0f, size.width, size.height))
    }

    val cutoutPath = Path().apply {
        when (type) {
            is CutoutType.RoundedRect -> {
                addRoundRect(
                    RoundRect(
                        rect = Rect(cutoutLeft, cutoutTop, cutoutRight, cutoutBottom),
                        cornerRadius = CornerRadius(type.cornerRadius, type.cornerRadius)
                    )
                )
            }
            is CutoutType.Circle -> {
                val width = cutoutRight - cutoutLeft
                val height = cutoutBottom - cutoutTop
                val diameter = maxOf(width, height)
                val radius = diameter / 2f
                val centerX = (cutoutLeft + cutoutRight) / 2f
                val centerY = (cutoutTop + cutoutBottom) / 2f

                addOval(
                    Rect(
                        centerX - radius,
                        centerY - radius,
                        centerX + radius,
                        centerY + radius
                    )
                )
            }
        }
    }

    op(fullRect, cutoutPath, PathOperation.Difference)
}

sealed interface CutoutType {
    data class RoundedRect(val cornerRadius: Float) : CutoutType
    data object Circle : CutoutType
}
