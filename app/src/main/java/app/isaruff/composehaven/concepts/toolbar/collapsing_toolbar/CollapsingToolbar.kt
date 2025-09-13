@file:OptIn(ExperimentalMaterial3Api::class)

package app.isaruff.composehaven.concepts.toolbar.collapsing_toolbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.lerp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp
import kotlin.math.max
import kotlin.math.roundToInt

private enum class LayoutId {
    NavigationIcon, Title, Actions
}

data class TitleTextStyle(
    val expandedTextStyle: TextStyle,
    val collapsedTextStyle: TextStyle
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsingToolbar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    expandedHeight: Dp = CollapsingToolbarDefaults.ExpandedHeight,
    collapsedHeight: Dp = CollapsingToolbarDefaults.CollapsedHeight,
    titleTextStyle: TitleTextStyle = CollapsingToolbarDefaults.TitleTextStyle,
) {
    val collapseFraction = scrollBehavior?.state?.collapsedFraction ?: 0f
    val currentHeight = lerp(expandedHeight, collapsedHeight, collapseFraction)
    val textStyle = lerp(
        start = titleTextStyle.expandedTextStyle,
        stop = titleTextStyle.collapsedTextStyle,
        fraction = collapseFraction
    )

    val heightOffsetLimit = with(LocalDensity.current) {
        remember(expandedHeight, collapsedHeight) {
            -(expandedHeight - collapsedHeight).toPx()
        }
    }
    scrollBehavior?.state?.heightOffsetLimit = heightOffsetLimit

    CollapsingToolbarLayout(
        modifier = modifier
            .windowInsetsPadding(windowInsets)
            .height(currentHeight),
        collapseFraction = collapseFraction,
        navigationIcon = navigationIcon,
        title = {
            CompositionLocalProvider(
                LocalTextStyle provides textStyle,
                content = title
            )
        },
        actions = actions,
    )
}

@Composable
private fun CollapsingToolbarLayout(
    collapseFraction: Float,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit,
    title: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit,
) {
    Layout(
        modifier = modifier,
        content = {
            Box(
                modifier = Modifier
                    .layoutId(LayoutId.NavigationIcon)
            ) {
                navigationIcon()
            }
            Box(
                modifier = Modifier
                    .layoutId(LayoutId.Title)
            ) {
                title()
            }
            Row(
                modifier = Modifier
                    .layoutId(LayoutId.Actions),
                horizontalArrangement = Arrangement.End,
            ) {
                actions()
            }
        }
    ) { measurables, constraints ->

        val navigationIconPlaceable =
            measurables.first { it.layoutId == LayoutId.NavigationIcon }
                .measure(constraints.copy(minWidth = 0))
        val actionsPlaceable =
            measurables.first { it.layoutId == LayoutId.Actions }
                .measure(constraints.copy(minWidth = 0))

        val titleMaxWidth =
            (constraints.maxWidth - collapseFraction * (navigationIconPlaceable.width - actionsPlaceable.width))
                .roundToInt().coerceAtLeast(0)
        val titlePlaceable =
            measurables.first { it.layoutId == LayoutId.Title }
                .measure(constraints.copy(minWidth = 0, maxWidth = titleMaxWidth))

        val layoutHeight = constraints.maxHeight

        layout(constraints.maxWidth, layoutHeight) {
            navigationIconPlaceable.placeRelative(
                x = 0,
                y = 0
            )

            actionsPlaceable.placeRelative(
                x = constraints.maxWidth - actionsPlaceable.width,
                y = 0
            )

            val titleExpandedX = 0
            val titleCollapsedX = (constraints.maxWidth - titlePlaceable.width) / 2
            val titleX = lerp(
                start = titleExpandedX,
                stop = titleCollapsedX,
                fraction = collapseFraction
            )

            val topBarHeight =
                max(navigationIconPlaceable.height, actionsPlaceable.height)
            val titleExpandedY = (titlePlaceable.height) / 2
            val titleCollapsedY = (topBarHeight - titlePlaceable.height / 2) / 2
            val titleY = lerp(
                start = titleExpandedY,
                stop = titleCollapsedY,
                fraction = collapseFraction
            )

            titlePlaceable.placeRelative(
                x = titleX,
                y = titleY
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun CollapsingToolbarExample() {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    MaterialTheme {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .systemBarsPadding(),
            topBar = {
                CollapsingToolbar(
                    modifier = Modifier.background(Color.Red),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "My Title Very very long verrrryy",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                    },
                    navigationIcon = {
                        Row {
                            IconButton(onClick = { }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = paddingValues
            ) {
                items(100) { index ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Item $index - This is a sample item to demonstrate scrolling behavior",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

    }
}
