package app.isaruff.composehaven.concepts.toolbar.collapsing_toolbar

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

object CollapsingToolbarDefaults {
    val ExpandedHeight = 100.dp
    val CollapsedHeight = 48.dp

    val TitleTextStyle
        @Composable
        get() = TitleTextStyle(
            expandedTextStyle = MaterialTheme.typography.headlineLarge,
            collapsedTextStyle = MaterialTheme.typography.titleMedium
        )
}