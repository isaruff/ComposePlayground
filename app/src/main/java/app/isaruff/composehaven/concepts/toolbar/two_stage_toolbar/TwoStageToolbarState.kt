package app.isaruff.composehaven.concepts.toolbar.two_stage_toolbar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/**
 * State class for [TwoStageToolbarScaffold] that holds the current scroll offset and measured heights.
 *
 * @property offset The current scroll offset (0 when in the first stage, initialStageHeight when fully collapsed)
 * @property initialStageHeight The measured height of the initial stage toolbar
 * @property secondStageHeight The measured height of the second stage toolbar
 * @property totalOffset Total offset including both default and minimized transitions
 */
@Stable
class TwoStageToolbarState(
    offset: Float = 0f,
    initialStageHeight: Float = 0f,
    secondStageHeight: Float = 0f,
    totalOffset: Float = 0f
) {
    var offset by mutableFloatStateOf(offset)
    var initialStageHeight by mutableFloatStateOf(initialStageHeight)
    var secondStageHeight by mutableFloatStateOf(secondStageHeight)
    var totalOffset by mutableFloatStateOf(totalOffset)

    /**
     * Progress of the first stage toolbar transition from 0 to 1 where:
     * - 0 => fully expanded (offset == 0)
     * - 1 => fully collapsed (offset == initialStageHeight)
     */
    val firstStageProgress: Float
        get() = if (initialStageHeight > 0f) {
            (offset / initialStageHeight).coerceIn(0f, 1f)
        } else 0f

    /**
     * Progress of the second stage toolbar transition from 0 to 1 where:
     * - 0 => not visible (totalOffset == initialStageHeight)
     * - 1 => fully visible (totalOffset == initialStageHeight + secondStageHeight)
     */
    val secondStageProgress: Float
        get() = if (secondStageHeight > 0f) {
            ((totalOffset - initialStageHeight) / secondStageHeight).coerceIn(
                0f,
                1f
            )
        } else 0f


    companion object Companion {
        /**
         * Saver for preserving state across configuration changes.
         */
        val Saver: Saver<TwoStageToolbarState, *> = listSaver(
            save = {
                listOf(
                    it.offset,
                    it.initialStageHeight,
                    it.secondStageHeight,
                    it.totalOffset
                )
            },
            restore = {
                TwoStageToolbarState(
                    it[0],
                    it[1],
                    it[2],
                    it[3]
                )
            }
        )
    }
}

/**
 * Creates and remembers a [TwoStageToolbarState] that is preserved across configuration changes.
 */
@Composable
fun rememberTwoStageToolbarState(): TwoStageToolbarState {
    return rememberSaveable(saver = TwoStageToolbarState.Saver) {
        TwoStageToolbarState()
    }
}

