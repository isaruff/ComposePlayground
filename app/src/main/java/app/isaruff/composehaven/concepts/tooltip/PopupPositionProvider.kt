package app.isaruff.composehaven.concepts.tooltip

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider

internal object TooltipPositionProvider : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val anchorCenterX = anchorBounds.left + anchorBounds.width / 2
        val anchorBottom = anchorBounds.bottom
        val anchorTop = anchorBounds.top
        val spacing = 16 // Gap between tooltip and anchor

        // X position (center with fallback)
        var x = anchorCenterX - popupContentSize.width / 2
        if (x < 0) x = 16 // Left margin
        if (x + popupContentSize.width > windowSize.width) {
            x = windowSize.width - popupContentSize.width - 16 // Right margin
        }

        // Y position (prefer below, but go above if it obscures or doesn't fit)
        val preferredY = anchorBottom + spacing
        val y = if (preferredY + popupContentSize.height > windowSize.height) {
            // Doesn't fit below, place above
            (anchorTop - popupContentSize.height - spacing).coerceAtLeast(16)
        } else {
            // Check if placing below would obscure the anchor
            val tooltipBottom = preferredY + popupContentSize.height
            val tooltipTop = preferredY
            val tooltipLeft = x
            val tooltipRight = x + popupContentSize.width

            // Check for overlap
            val overlapsHorizontally =
                tooltipRight > anchorBounds.left && tooltipLeft < anchorBounds.right
            val overlapsVertically =
                tooltipBottom > anchorBounds.top && tooltipTop < anchorBounds.bottom

            if (overlapsHorizontally && overlapsVertically) {
                // place above instead
                (anchorTop - popupContentSize.height - spacing).coerceAtLeast(16)
            } else {
                preferredY
            }
        }

        return IntOffset(x = x, y = y)
    }
}

internal object FullScreenPositionProvider : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ) = IntOffset.Zero
}
