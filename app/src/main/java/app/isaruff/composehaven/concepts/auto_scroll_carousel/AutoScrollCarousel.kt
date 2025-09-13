package app.isaruff.composehaven.concepts.auto_scroll_carousel

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

/**
 * An infinitely auto-scrolling carousel list.
 *
 * This composable displays a virtually infinite list of [items] that auto-scrolls
 * either horizontally (via [LazyRow]) or vertically (via [LazyColumn]) depending
 * on the provided [movement]. It supports smooth, frame-based scrolling in both
 * directions (left/right or top/bottom), and automatically pauses when the user
 * interacts with the list. Scrolling resumes after [interactionDelayMillis].
 *
 * ### Scrolling behavior
 * Auto-scroll is implemented using frame timing via [withFrameNanos], ensuring
 * precise per-frame control over pixel deltas. This approach provides smoother
 * and more consistent movement compared to animation APIs like `animate*AsState`.
 *
 * Auto-scroll direction is determined by [movement]:
 * - [Movement.Horizontal.Left] and [Movement.Vertical.Top] scroll in the negative direction.
 * - [Movement.Horizontal.Right] and [Movement.Vertical.Bottom] scroll in the positive direction.
 *
 * ### Indexing
 * The list size is virtually infinite, but indices are mapped back to the real
 * item list using modulo arithmetic. This guarantees seamless looping over the
 * provided [items]. The starting point is calculated with [calculateStartIndex]
 * so that the initial item aligns with [firstVisibleItemIndex], while still
 * maintaining infinite scroll in both directions.
 *
 * @param items The list of data items to display in the carousel.
 * @param movement The scroll orientation and direction, either [Movement.Horizontal] or [Movement.Vertical].
 * @param modifier A [Modifier] to apply to the carousel container.
 * @param isScrolling Whether the list should auto-scroll. Defaults to `true`.
 * @param initialFirstVisibleItemScrollOffset The initial pixel offset for the first visible item.
 *        See [AutoScrollCarouselDefaults.FirstVisibleItemOffset].
 * @param scrollSpeedPxPerMillis The speed of scrolling in pixels per millisecond.
 *        See [AutoScrollCarouselDefaults.DefaultSpeedPxPerMillis].
 * @param interactionDelayMillis The delay (in milliseconds) before resuming auto-scroll
 *        after user interaction. See [AutoScrollCarouselDefaults.InteractionDelayMillis].
 * @param firstVisibleItemIndex The logical index of the item to start from.
 *        Defaults to [AutoScrollCarouselDefaults.FirstVisibleItemIndex].
 * @param itemContent A composable lambda that defines how each item should be displayed.
 *        Provides both the resolved list index and the item value.
 *
 * @see Movement
 * @see AutoScrollCarouselDefaults
 * @see calculateStartIndex
 *
 * @author Isa Rufullazada
 */

@Composable
fun <T> AutoScrollCarouselList(
    items: List<T>,
    movement: Movement,
    modifier: Modifier = Modifier,
    isScrolling: Boolean = true,
    initialFirstVisibleItemScrollOffset: Int = AutoScrollCarouselDefaults.FirstVisibleItemOffset,
    scrollSpeedPxPerMillis: Float = AutoScrollCarouselDefaults.DefaultSpeedPxPerMillis,
    interactionDelayMillis: Long = AutoScrollCarouselDefaults.InteractionDelayMillis,
    firstVisibleItemIndex: Int = AutoScrollCarouselDefaults.FirstVisibleItemIndex,
    itemSpacing: Dp = AutoScrollCarouselDefaults.ItemSpacing,
    itemContent: @Composable LazyItemScope.(index: Int, value: T) -> Unit,
) {
    val lazyListState = rememberLazyListState(
        initialFirstVisibleItemIndex = calculateStartIndex(items.size, firstVisibleItemIndex),
        initialFirstVisibleItemScrollOffset = initialFirstVisibleItemScrollOffset
    )

    LaunchedEffect(
        key1 = Pair(isScrolling, lazyListState.isScrollInProgress),
        key2 = movement,
        key3 = scrollSpeedPxPerMillis,
    ) {
        if (!isScrolling) return@LaunchedEffect
        var lastTimeNanos: Long

        snapshotFlow { lazyListState.isScrollInProgress }
            .filter { inProgress -> !inProgress }
            .first() // suspend until not scrolling

        delay(interactionDelayMillis)

        lastTimeNanos = withFrameNanos { it }

        while (!lazyListState.isScrollInProgress) {
            val currentTimeNanos = withFrameNanos { it }
            val deltaMillis = (currentTimeNanos - lastTimeNanos) / 1_000_000f

            val directionFactor = when (movement) {
                Movement.Horizontal.Left,
                Movement.Vertical.Top -> -1

                Movement.Horizontal.Right,
                Movement.Vertical.Bottom -> 1
            }

            lazyListState.scrollBy(directionFactor * scrollSpeedPxPerMillis * deltaMillis)
            lastTimeNanos = currentTimeNanos
        }
    }

    when (movement) {
        is Movement.Horizontal -> {
            LazyRow(
                modifier = modifier,
                state = lazyListState,
                horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                content = {
                    items(VeryLargeItemCount) { virtualIndex ->
                        val actualIndex = (virtualIndex % items.size + items.size) % items.size
                        val item = items[actualIndex]
                        itemContent(actualIndex, item)
                    }
                }
            )
        }

        is Movement.Vertical -> {
            LazyColumn(
                modifier = modifier,
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(itemSpacing),
                content = {
                    items(VeryLargeItemCount) { virtualIndex ->
                        val actualIndex = (virtualIndex % items.size + items.size) % items.size
                        val item = items[actualIndex]
                        itemContent(actualIndex, item)
                    }
                }
            )
        }
    }
}

private const val VeryLargeItemCount = Int.MAX_VALUE

private fun calculateStartIndex(itemsSize: Int, startIndex: Int = 0): Int {
    require(startIndex >= 0) { "Index must start from 0" }
    require(startIndex < itemsSize) { "Given index cannot exceed the items size" }
    val midPoint = VeryLargeItemCount / 2
    return midPoint - (midPoint % itemsSize) + startIndex
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun AutoScrollCarouselListPreview() {
    val items = remember { (1..10).toList() }

    // Control states
    var isScrollingEnabled by remember { mutableStateOf(true) }
    var scrollSpeed by remember { mutableFloatStateOf(AutoScrollCarouselDefaults.DefaultSpeedPxPerMillis) }
    var firstCarouselMovement by remember { mutableStateOf(Movement.Horizontal.Left) }
    var secondCarouselDirection by remember { mutableStateOf(Movement.Horizontal.Right) }
    var thirdCarouselDirection by remember { mutableStateOf(Movement.Vertical.Top) }

    fun colorFromIndex(index: Int): Color {
        // Generate RGB values based on index
        val r = (index * 70) % 256
        val g = (index * 150) % 256
        val b = (index * 230) % 256
        return Color(r, g, b)
    }

    fun getDirectionText(movement: Movement): String {
        return when (movement) {
            Movement.Horizontal.Left -> "←"
            Movement.Horizontal.Right -> "→"
            Movement.Vertical.Top -> "↑"
            Movement.Vertical.Bottom -> "↓"
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Global Controls Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Global Controls",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Scrolling Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Auto Scroll: ${if (isScrollingEnabled) "ON" else "OFF"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = { isScrollingEnabled = !isScrollingEnabled },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isScrollingEnabled)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Text(if (isScrollingEnabled) "Disable" else "Enable")
                        }
                    }

                    // Speed Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Scroll Speed",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${scrollSpeed.toInt()} px/ms",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Slider(
                            value = scrollSpeed,
                            onValueChange = { scrollSpeed = it },
                            valueRange = 0.1f..1f,
                            steps = 98,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "0.05",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "1f",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // First Carousel
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Horizontal Carousel ${getDirectionText(firstCarouselMovement)}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Button(
                        onClick = {
                            firstCarouselMovement = if (firstCarouselMovement == Movement.Horizontal.Left) {
                                Movement.Horizontal.Right
                            } else {
                                Movement.Horizontal.Left
                            }
                        }
                    ) {
                        Text("Change Direction")
                    }
                }
                AutoScrollCarouselList(
                    items = items,
                    movement = firstCarouselMovement,
                    isScrolling = isScrollingEnabled,
                    scrollSpeedPxPerMillis = scrollSpeed,
                    modifier = Modifier.height(120.dp),
                    itemContent = { index, item ->
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(colorFromIndex(index)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Item $item",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                )
            }
        }

        // Second Carousel
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Horizontal Carousel ${getDirectionText(secondCarouselDirection)}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Button(
                        onClick = {
                            secondCarouselDirection = if (secondCarouselDirection == Movement.Horizontal.Left) {
                                Movement.Horizontal.Right
                            } else {
                                Movement.Horizontal.Left
                            }
                        }
                    ) {
                        Text("Change Direction")
                    }
                }
                AutoScrollCarouselList(
                    items = items,
                    movement = secondCarouselDirection,
                    isScrolling = isScrollingEnabled,
                    scrollSpeedPxPerMillis = scrollSpeed,
                    modifier = Modifier.height(120.dp),
                    itemContent = { index, item ->
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(colorFromIndex(index)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Item $item",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                )
            }
        }

        // Third Carousel (Vertical)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Vertical Carousel ${getDirectionText(thirdCarouselDirection)}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Button(
                        onClick = {
                            thirdCarouselDirection = if (thirdCarouselDirection == Movement.Vertical.Top) {
                                Movement.Vertical.Bottom
                            } else {
                                Movement.Vertical.Top
                            }
                        }
                    ) {
                        Text("Change Direction")
                    }
                }
                AutoScrollCarouselList(
                    items = items,
                    movement = thirdCarouselDirection,
                    isScrolling = isScrollingEnabled,
                    scrollSpeedPxPerMillis = scrollSpeed,
                    firstVisibleItemIndex = 0,
                    modifier = Modifier.height(300.dp), // Fixed height for vertical carousel
                    itemContent = { index, item ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(colorFromIndex(index)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Item $item",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                )
            }
        }

        // Status Information
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Current Settings",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• Auto Scroll: ${if (isScrollingEnabled) "Enabled" else "Disabled"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Speed: ${String.format("%.1f", scrollSpeed)} px/ms",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Horizontal 1: ${getDirectionText(firstCarouselMovement)} direction",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Horizontal 2: ${getDirectionText(secondCarouselDirection)} direction",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Vertical: ${getDirectionText(thirdCarouselDirection)} direction",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
