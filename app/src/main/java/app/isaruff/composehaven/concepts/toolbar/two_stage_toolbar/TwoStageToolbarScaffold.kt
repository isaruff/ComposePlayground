package app.isaruff.composehaven.concepts.toolbar.two_stage_toolbar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.SubcomposeMeasureScope

/**
 * A scaffold that provides a two stage toolbar experience with smooth animations.
 * The toolbar appears above the main content when the default toolbar is collapsed.
 *
 * @param modifier The modifier to apply to the scaffold
 * @param state The state controlling the toolbar's behavior
 * @param behavior The scroll behavior for the toolbar
 * @param firstStage The default toolbar content to show when expanded
 * @param secondStage The minimized toolbar content to show when collapsed
 * @param content The main content of the scaffold
 */
@Composable
fun TwoStageToolbarScaffold(
    modifier: Modifier = Modifier,
    state: TwoStageToolbarState = rememberTwoStageToolbarState(),
    behavior: TwoStageToolbarScrollBehavior = TwoStageToolbarScrollBehavior.ExitUntilCollapsed,
    firstStage: @Composable () -> Unit,
    secondStage: @Composable () -> Unit,
    content: @Composable (scrollConnection: NestedScrollConnection) -> Unit
) {
    val nestedScrollConnection = remember(state, behavior) {
        TwoStageNestedScrollConnection(state, behavior)
    }

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            FirstStageToolbarLayer(
                state = state,
                firstStageToolbar = firstStage
            )
            content(nestedScrollConnection)
        }
        SecondToolbarLayer(
            state = state,
            secondStageToolbar = secondStage
        )
    }
}

@Composable
private fun FirstStageToolbarLayer(
    state: TwoStageToolbarState,
    firstStageToolbar: @Composable () -> Unit
) {
    SubcomposeLayout(Modifier.fillMaxWidth()) { constraints ->
        val (firstStageHeight, firstStagePlaceable) = measureContent(
            "first_stage",
            firstStageToolbar,
            constraints
        )
        state.initialStageHeight = firstStageHeight.toFloat()

        // Calculate current height and position for default toolbar
        val currentHeight = (firstStageHeight - state.offset).coerceAtLeast(0f).toInt()

        layout(constraints.maxWidth, currentHeight) {
            firstStagePlaceable.forEach { placeable ->
                placeable.placeWithLayer(
                    x = 0,
                    y = -state.offset.toInt(),
                )
            }
        }
    }
}

@Composable
private fun SecondToolbarLayer(
    state: TwoStageToolbarState,
    secondStageToolbar: @Composable () -> Unit
) {
    SubcomposeLayout(Modifier.fillMaxWidth()) { constraints ->
        val (secondStageHeight, secondStagePlaceable) = measureContent(
            "second_stage",
            secondStageToolbar,
            constraints
        )
        state.secondStageHeight = secondStageHeight.toFloat()
        val secondStageProgress = state.secondStageProgress

        val secondStageContentY = (-secondStageHeight + (secondStageProgress * secondStageHeight)).toInt()

        layout(constraints.maxWidth, secondStageHeight) {
            secondStagePlaceable.forEach { placeable ->
                placeable.placeWithLayer(
                    x = 0,
                    y = secondStageContentY,
                    layerBlock = {
                        alpha = state.secondStageProgress * 1.5f
                    }
                )
            }
        }
    }
}

/**
 * Measures the content and returns its height and placeables.
 */
private fun SubcomposeMeasureScope.measureContent(
    slotId: String,
    content: @Composable () -> Unit,
    constraints: androidx.compose.ui.unit.Constraints
): Pair<Int, List<Placeable>> {
    val placeables = subcompose(slotId, content).map {
        it.measure(constraints.copy(minHeight = 0))
    }
    val height = placeables.maxOfOrNull { it.height } ?: 0
    return height to placeables
}