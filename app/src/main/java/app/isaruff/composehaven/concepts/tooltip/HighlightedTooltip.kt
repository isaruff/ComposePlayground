@file:OptIn(ExperimentalMaterial3Api::class)

package app.isaruff.composehaven.concepts.tooltip

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.roundToIntRect
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

@Composable
fun HighlightedTooltip(
    showTooltip: Boolean,
    modifier: Modifier = Modifier,
    scrimColor: Color = Color.Black.copy(0.6f),
    cutoutType: CutoutType = CutoutType.RoundedRect(40f),
    onDismiss: () -> Unit = {},
    tooltip: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val insets = WindowInsets.safeDrawing.asPaddingValues()
    var anchorBounds by remember { mutableStateOf(IntRect.Zero) }
    val transition = updateTransition(showTooltip)
    val isVisible = transition.currentState || transition.targetState || transition.isRunning

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                anchorBounds = coordinates.boundsInWindow().roundToIntRect()
            }
    ) {
        content()
        if (isVisible) {
            ScrimPopup(
                insets = insets,
                transition = transition,
                scrimColor = scrimColor,
                anchorBounds = anchorBounds,
                cutoutType = cutoutType,
                onDismiss = onDismiss
            )
            TooltipPopup(
                transition = transition,
                onDismiss = onDismiss,
                content = tooltip
            )
        }
    }
}

/**Scrim background with the Highlighted content cut*/
@Composable
private fun ScrimPopup(
    insets: PaddingValues,
    transition: Transition<Boolean>,
    scrimColor: Color,
    anchorBounds: IntRect,
    cutoutType: CutoutType,
    onDismiss: () -> Unit
) {
    val safeAnchorBounds = anchorBounds.safeBounds(insets)
    Popup(
        properties = PopupProperties(
            focusable = false,
            excludeFromSystemGesture = true,
            clippingEnabled = true
        ),
        popupPositionProvider = FullScreenPositionProvider,
        onDismissRequest = onDismiss
    ) {
        val fraction by transition.animateFloat(
            transitionSpec = {
                spring(stiffness = Spring.StiffnessMediumLow)
            },
            targetValueByState = { show ->
                if (show) 1f else 0f
            }
        )
        val scrimColorTransition = transition.animateColor(
            transitionSpec = { spring(stiffness = Spring.StiffnessLow) },
            targetValueByState = { show ->
                if (show) scrimColor else Color.Transparent
            }
        )
        Box(
            Modifier
                .fillMaxSize()
                .drawBehind {
                    val outline = animatedScrimShape(fraction, safeAnchorBounds, cutoutType)
                        .createOutline(size, layoutDirection, this)

                    drawOutline(
                        outline = outline,
                        color = scrimColorTransition.value,
                        style = Fill
                    )
                }
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onDismiss
                )
        )
    }
}

/**Tooltip content*/
@Composable
private fun TooltipPopup(
    transition: Transition<Boolean>,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val fraction by transition.animateFloat(
        transitionSpec = {
            spring(stiffness = Spring.StiffnessLow)
        }
    ) { show ->
        if (show) 1f else 0f
    }
    Popup(
        popupPositionProvider = TooltipPositionProvider,
        onDismissRequest = onDismiss,
        content = {
            Box(
                modifier = Modifier.graphicsLayer {
                    alpha = fraction
                    translationY = 75 * (1f - fraction)
                },
                contentAlignment = Alignment.Center,
                content = { content() }
            )
        }
    )
}

//This is the preview area. Do not focus on here much.
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HighlightedTooltipFullPreview() {
    var currentStep by remember { mutableIntStateOf(0) }
    val totalSteps = 5

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Highlighted Tooltip Demo") },
                    actions = {
                        HighlightedTooltip(
                            showTooltip = currentStep == 4,
                            cutoutType = CutoutType.Circle,
                            tooltip = {
                                TutorialTooltip(
                                    title = "Settings Menu",
                                    description = "Access app settings and preferences from here. You can customize your experience!",
                                    step = 5,
                                    totalSteps = totalSteps,
                                    onNext = { currentStep = 0 },
                                    onSkip = { currentStep = 0 }
                                )
                            },
                            onDismiss = { currentStep = 0 },
                            content = {
                                IconButton(onClick = {}) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings"
                                    )
                                }
                            }
                        )
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Welcome Card - Step 1
                HighlightedTooltip(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 32.dp),
                    showTooltip = currentStep == 1,
                    cutoutType = CutoutType.RoundedRect(16f),
                    tooltip = {
                        TutorialTooltip(
                            title = "Welcome!",
                            description = "This is your dashboard. Here you can see an overview of your activity and quick actions.",
                            step = 2,
                            totalSteps = totalSteps,
                            onNext = { currentStep = 2 },
                            onSkip = { currentStep = 0 }
                        )
                    },
                    onDismiss = { currentStep = 0 },
                    content = {
                        Card(
                            modifier = Modifier
                                .width(300.dp)
                                .padding(16.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Dashboard",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Your activity at a glance",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                )

                // Action Buttons - Steps 2 & 3
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HighlightedTooltip(
                        showTooltip = currentStep == 2,
                        cutoutType = CutoutType.RoundedRect(30f),
                        tooltip = {
                            TutorialTooltip(
                                title = "Add New Item",
                                description = "Tap here to create a new item. You can add photos, text, and more!",
                                step = 3,
                                totalSteps = totalSteps,
                                onNext = { currentStep = 3 },
                                onSkip = { currentStep = 0 }
                            )
                        },
                        onDismiss = { currentStep = 0 },
                        content = {
                            Button(
                                onClick = {},
                                modifier = Modifier.height(56.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add New")
                            }
                        }
                    )

                    HighlightedTooltip(
                        showTooltip = currentStep == 3,
                        cutoutType = CutoutType.RoundedRect(30f),
                        tooltip = {
                            TutorialTooltip(
                                title = "Search",
                                description = "Use search to quickly find what you're looking for across all your content.",
                                step = 4,
                                totalSteps = totalSteps,
                                onNext = { currentStep = 4 },
                                onSkip = { currentStep = 0 }
                            )
                        },
                        onDismiss = { currentStep = 0 },
                        content = {
                            FloatingActionButton(
                                onClick = {},
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                        }
                    )
                }

                // Start Tutorial Button
                if (currentStep == 0) {
                    Button(
                        onClick = { currentStep = 1 },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 48.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Tutorial")
                    }
                }
            }
        }
    }
}

@Composable
private fun TutorialTooltip(
    title: String,
    description: String,
    step: Int,
    totalSteps: Int,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .widthIn(max = 320.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "$step/$totalSteps",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Progress indicator
            LinearProgressIndicator(
                progress = { step.toFloat() / totalSteps },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onSkip) {
                    Text("Skip Tutorial")
                }
                Button(onClick = onNext) {
                    Text(if (step == totalSteps) "Finish" else "Next")
                    if (step != totalSteps) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}