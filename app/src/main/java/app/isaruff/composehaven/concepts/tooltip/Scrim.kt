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

internal fun animatedScrimShape(
    progress: Float,
    anchorBounds: IntRect,
    cutoutType: CutoutType
): Shape {
    val centerX = anchorBounds.center.x.toFloat()
    val centerY = anchorBounds.center.y.toFloat()

    val halfWidth = anchorBounds.width / 2f
    val halfHeight = anchorBounds.height / 2f

    // Grow outward from center
    val currentHalfWidth = halfWidth * progress
    val currentHalfHeight = halfHeight * progress

    val left = centerX - currentHalfWidth
    val right = centerX + currentHalfWidth
    val top = centerY - currentHalfHeight
    val bottom = centerY + currentHalfHeight

    val animatedBounds = IntRect(
        left = left.toInt(),
        right = right.toInt(),
        top = top.toInt(),
        bottom = bottom.toInt()
    )

    return scrimCutoutShape(
        bounds = animatedBounds,
        type = cutoutType
    )
}


sealed interface CutoutType {
    data class RoundedRect(val cornerRadius: Float) : CutoutType
    data object Circle : CutoutType
}