package app.isaruff.composehaven.concepts.toolbar.two_stage_toolbar

/**
 * Enum defining the scroll behaviors for the Two Stage toolbar.
 */
enum class TwoStageToolbarScrollBehavior {
    /**
     * Toolbar expands immediately when scrolling down and collapses when scrolling up
     * */
    EnterAlways,

    /**
     * Toolbar collapses when scrolling up but only expands when scrolled to top
     * */
    ExitUntilCollapsed,

    /**
     * Toolbar doesn't respond to scroll events
     * */
    Fixed
}
