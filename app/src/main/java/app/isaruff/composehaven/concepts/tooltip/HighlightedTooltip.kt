package app.isaruff.composehaven.concepts.tooltip

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.roundToIntRect
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

@Composable
fun HighlightedTooltip(
    showTooltip: Boolean,
    modifier: Modifier = Modifier,
    cutoutType: CutoutType = CutoutType.RoundedRect(12f),
    content: @Composable () -> Unit,
    tooltip: @Composable () -> Unit,
    onDismiss: () -> Unit = {}
) {
    var anchorBounds by remember { mutableStateOf<IntRect?>(null) }
    val systemBarOffsets = systemBarsOffset()

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                anchorBounds = coordinates.boundsInWindow().roundToIntRect()
            }
    ) {
        content()

        anchorBounds?.let { anchor ->
            if (showTooltip) {
                ScrimPopup(
                    anchorBounds = anchor,
                    cutoutType = cutoutType,
                    systemBarOffsets = systemBarOffsets,
                    onDismiss = onDismiss
                )

                TooltipPopup(
                    anchorBounds = anchor,
                    onDismiss = onDismiss,
                    content = tooltip
                )
            }
        }
    }
}

/** Helper to measure system bar offsets in pixels */
@Composable
private fun systemBarsOffset(): SystemBarOffsets {
    val insets = WindowInsets.systemBars
    val density = LocalDensity.current
    val paddingValues = insets.asPaddingValues()
    return SystemBarOffsets(
        statusBarHeight = with(density) { paddingValues.calculateTopPadding().roundToPx() },
        navBarHeight = with(density) { paddingValues.calculateBottomPadding().roundToPx() }
    )
}

private data class SystemBarOffsets(val statusBarHeight: Int, val navBarHeight: Int)

/** Full-screen scrim with cutout */
@Composable
private fun ScrimPopup(
    anchorBounds: IntRect,
    cutoutType: CutoutType,
    systemBarOffsets: SystemBarOffsets,
    onDismiss: () -> Unit
) {
    Popup(
        properties = PopupProperties(
            focusable = false,
            excludeFromSystemGesture = true,
            clippingEnabled = true
        ),
        popupPositionProvider = FullScreenPositionProvider,
        onDismissRequest = onDismiss
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = scrimCutoutShape(
                        cutoutLeft = anchorBounds.left.toFloat() - 20,
                        cutoutTop = (anchorBounds.top - systemBarOffsets.statusBarHeight).toFloat() - 10,
                        cutoutRight = anchorBounds.right.toFloat() + 20,
                        cutoutBottom = (anchorBounds.bottom - systemBarOffsets.navBarHeight).toFloat() + 10,
                        type = cutoutType
                    )
                )
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() }
        )
    }
}

/** Tooltip popup positioned relative to the anchor */
@Composable
private fun TooltipPopup(
    anchorBounds: IntRect,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Popup(
        popupPositionProvider = TooltipPositionProvider(anchorBounds),
        onDismissRequest = onDismiss,
        content = content
    )
}

/** PopupPositionProvider for full-screen scrim */
private object FullScreenPositionProvider : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ) = IntOffset(0, 0)
}

/** PopupPositionProvider for tooltip positioning rules */
private class TooltipPositionProvider(private val anchorBounds: IntRect) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val anchorCenterX = anchorBounds.left + anchorBounds.width / 2
        val anchorBottom = anchorBounds.bottom
        val anchorTop = anchorBounds.top

        // X position (center with fallback)
        var x = anchorCenterX - popupContentSize.width / 2
        if (x < 0) x = anchorBounds.left
        if (x + popupContentSize.width > windowSize.width) {
            x = anchorBounds.right - popupContentSize.width
        }

        // Y position (below or above anchor)
        var y = anchorBottom
        if (y + popupContentSize.height > windowSize.height) {
            y = anchorTop - popupContentSize.height
        }

        return IntOffset(x.coerceAtLeast(0), y.coerceAtLeast(0))
    }
}


@Preview
@Composable
private fun HighlightedTooltipPrev() {
    var show by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        HighlightedTooltip(
            showTooltip = show,
            tooltip = {
                Column(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Here is the title"
                    )
                    Text(
                        text = "Here is the tutorial info"
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Text(
                            text = "1/2"
                        )
                        Spacer(modifier = Modifier.width(40.dp))
                        Button(onClick = { show = false }) {
                            Text("Next")
                        }
                    }
                }
            },
            onDismiss = {
                show = false
            },
            content = {
                Button(
                    modifier = Modifier,
                    onClick = {
                        show = true
                    }
                ) {
                    Text(
                        text = "Here I am"
                    )
                }
            }
        )
    }
}
