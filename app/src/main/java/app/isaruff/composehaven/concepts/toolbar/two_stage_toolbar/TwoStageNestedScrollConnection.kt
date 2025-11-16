package app.isaruff.composehaven.concepts.toolbar.two_stage_toolbar

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity

/**
 * A nested scroll connection that handles toolbar collapsing and expanding based on scroll behavior.
 *
 * @param state The state of [TwoStageToolbarState]
 * @param behavior The scroll behavior to apply
 */
internal class TwoStageNestedScrollConnection(
    private val state: TwoStageToolbarState,
    private val behavior: TwoStageToolbarScrollBehavior
) : NestedScrollConnection {

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (behavior == TwoStageToolbarScrollBehavior.Fixed) return Offset.Zero

        val delta = available.y
        return when {
            delta < 0f -> handleScrollUp(delta)
            delta > 0f -> handleScrollDown(delta)
            else -> Offset.Zero
        }
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        return handlePostScroll(consumed, available)
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        return handlePostFling(available)
    }

    /**
     * Handles upward scroll by collapsing the toolbar.
     *
     * @param dy The scroll delta (negative for upward scroll)
     * @return The consumed scroll offset
     */
    private fun handleScrollUp(dy: Float): Offset {
        // dy is negative for upward scroll; work with positive magnitude
        val delta = -dy
        val maxOffset = state.initialStageHeight + state.secondStageHeight

        // --- VISUAL: second stage + totalOffset always advance by full delta (no consumption)
        val newTotalOffset = (state.totalOffset + delta).coerceAtMost(maxOffset)
        state.totalOffset = newTotalOffset

        // --- CONSUMPTION: only consume what collapses the FIRST stage
        // first-stage offset goes from 0 -> initialStageHeight (0 = expanded, initial = collapsed)
        val prevFirstOffset = state.offset
        val remainToCollapseFirst = state.initialStageHeight - prevFirstOffset
        val consumeForFirst = delta.coerceAtMost(remainToCollapseFirst).coerceAtLeast(0f)

        // apply consumed amount to first stage offset
        state.offset = (prevFirstOffset + consumeForFirst).coerceAtMost(state.initialStageHeight)

        // return the amount consumed (negative because we received negative dy)
        return Offset(0f, -consumeForFirst)
    }

    /**
     * Handles downward scroll based on the current behavior.
     *
     * @param dy The scroll delta (positive for downward scroll)
     * @return The consumed scroll offset
     */
    private fun handleScrollDown(dy: Float): Offset {
        return when (behavior) {
            TwoStageToolbarScrollBehavior.EnterAlways -> expandToolbar(dy)
            TwoStageToolbarScrollBehavior.ExitUntilCollapsed -> {
                // Only expand if we're at the top (detected in onPostScroll)
                Offset.Zero
            }

            TwoStageToolbarScrollBehavior.Fixed -> Offset.Zero
        }
    }

    /**
     * Expands the toolbar by reducing the offset.
     *
     * @param dy The amount to expand
     * @return The consumed scroll offset
     */
    private fun expandToolbar(dy: Float): Offset {
        // dy is positive for downward scroll
        val delta = dy

        // --- VISUAL: always reduce totalOffset by full delta (second stage starts disappearing immediately)
        val newTotal = (state.totalOffset - delta).coerceAtLeast(0f)
        state.totalOffset = newTotal

        // --- CONSUMPTION: only consume what expands the FIRST stage
        val prevFirstOffset = state.offset
        val consumeForFirst = delta.coerceAtMost(prevFirstOffset).coerceAtLeast(0f)

        // apply consumed amount (expanding first stage)
        state.offset = (prevFirstOffset - consumeForFirst).coerceAtLeast(0f)

        // return consumed amount (positive)
        return Offset(0f, consumeForFirst)
    }

    /**
     * Handles post-scroll events, particularly for ExitUntilCollapsed behavior.
     *
     * @param consumed The scroll amount consumed by the child
     * @param available The remaining scroll amount available after child consumption
     * @return The consumed scroll offset by the parent
     */
    private fun handlePostScroll(consumed: Offset, available: Offset): Offset {
        if (behavior == TwoStageToolbarScrollBehavior.ExitUntilCollapsed) {
            // If we're trying to scroll down but nothing was consumed, we're at the top
            if (available.y > 0f && consumed.y == 0f && state.totalOffset > 0f) {
                return expandToolbar(available.y)
            }
        }
        return Offset.Zero
    }

    /**
     * Handles post-fling events to ensure smooth toolbar expansion.
     *
     * @param available The remaining fling velocity after child consumption
     * @return The consumed fling velocity
     */
    private fun handlePostFling(available: Velocity): Velocity {
        if (behavior == TwoStageToolbarScrollBehavior.ExitUntilCollapsed &&
            available.y > 0f &&
            state.totalOffset > 0f
        ) {
            state.totalOffset = 0f
            state.offset = 0f
            return Velocity(0f, 0f)
        }
        return available
    }
}
