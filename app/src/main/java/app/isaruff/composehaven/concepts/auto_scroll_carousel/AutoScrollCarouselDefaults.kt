package app.isaruff.composehaven.concepts.auto_scroll_carousel

import androidx.compose.ui.unit.dp

object AutoScrollCarouselDefaults {
    const val DefaultSpeedPxPerMillis: Float = 0.05f
    const val FirstVisibleItemOffset: Int = 0
    const val InteractionDelayMillis: Long = 500
    const val FirstVisibleItemIndex: Int = 0

    val ItemSpacing = 10.dp
}


sealed interface Movement {
    enum class Vertical : Movement { Top, Bottom }
    enum class Horizontal : Movement { Left, Right }
}