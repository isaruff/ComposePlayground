package app.isaruff.composehaven.concepts.tooltip

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.toRect

fun scrimCutoutShape(
    bounds: IntRect,
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
                        rect = bounds.toRect(),
                        cornerRadius = CornerRadius(type.cornerRadius, type.cornerRadius)
                    )
                )
            }

            is CutoutType.Circle -> {
                val width = bounds.right - bounds.left
                val height = bounds.bottom - bounds.top
                val diameter = maxOf(width, height)
                val radius = diameter / 2f
                val centerX = (bounds.left + bounds.right) / 2f
                val centerY = (bounds.top + bounds.bottom) / 2f

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