/*
 * Copyright (c) 2023-2024 Adevinta
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.adevinta.spark.components.progress.tracker

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MultiContentMeasurePolicy
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import com.adevinta.spark.PreviewTheme
import com.adevinta.spark.SparkTheme
import com.adevinta.spark.components.IntentColor
import com.adevinta.spark.components.badge.BadgeIntent
import com.adevinta.spark.components.divider.Divider
import com.adevinta.spark.components.divider.VerticalDivider
import com.adevinta.spark.components.icons.Icon
import com.adevinta.spark.components.progress.tracker.LayoutOrientation.Horizontal
import com.adevinta.spark.components.progress.tracker.LayoutOrientation.Vertical
import com.adevinta.spark.components.surface.Surface
import com.adevinta.spark.components.text.Text
import com.adevinta.spark.components.textfields.LabelId
import com.adevinta.spark.icons.Check
import com.adevinta.spark.icons.SparkIcons
import com.adevinta.spark.tokens.dim1
import com.adevinta.spark.tokens.disabled
import com.adevinta.spark.tokens.highlight
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun ProgressTrackerRow(
    items: ImmutableList<ProgressStep>,
    modifier: Modifier = Modifier,
    intent: BadgeIntent = BadgeIntent.Main,
    onStepClick: ((index: Int) -> Unit)? = null,
    selectedStep: Int = 0,
) {
    ProgressTracker(
        items = items,
        orientation = Horizontal,
        modifier = modifier,
        intent = intent,
        onStepClick = onStepClick,
        selectedStep = selectedStep,
    )
}

@Composable
internal fun ProgressTrackerColumn(
    items: ImmutableList<ProgressStep>,
    modifier: Modifier = Modifier,
    intent: BadgeIntent = BadgeIntent.Main,
    onStepClick: ((index: Int) -> Unit)? = null,
    selectedStep: Int = 0,
) {
    ProgressTracker(
        items = items,
        orientation = Vertical,
        modifier = modifier,
        intent = intent,
        onStepClick = onStepClick,
        selectedStep = selectedStep,
    )
}

@Composable
private fun ProgressTracker(
    items: ImmutableList<ProgressStep>,
    orientation: LayoutOrientation,
    modifier: Modifier = Modifier,
    intent: BadgeIntent = BadgeIntent.Basic,
    onStepClick: ((index: Int) -> Unit)? = null,
    selectedStep: Int = 0,
) {
    val colors = intent.colors()

    val progressTracks = @Composable {
        items.forEach {
            ProgressTrack(enabled = it.enabled, color = colors.color, orientation = orientation)
        }
    }

    val interactionSources = remember { items.map { MutableInteractionSource() } }

    val stepLabels = @Composable {
        items.fastForEachIndexed { index, progressStep ->
            StepLabel(
                label = progressStep.label,
                enabled = progressStep.enabled,
                orientation = orientation,
                selected = index == selectedStep,
                interactionSource = interactionSources[index],
                onClick = { onStepClick?.invoke(index) },
            )
        }
    }
    val stepIndicators = @Composable {
        items.forEachIndexed { index, progressStep ->
            val isDone = index < selectedStep
            StepIndicator(
                colors = colors,
                index = index,
                enabled = progressStep.enabled,
                selected = index == selectedStep,
                done = isDone,
                onClick = onStepClick?.let {
                    { onStepClick.invoke(index) }
                },
                interactionSource = interactionSources[index],
            )
        }
    }
    val measurePolicy = progressTrackerMeasurePolicy(orientation)
    ProvideTextStyle(value = SparkTheme.typography.body2.highlight) {
        Layout(
            modifier = modifier.selectableGroup(),
            measurePolicy = measurePolicy,
            contents = listOf(progressTracks, stepLabels, stepIndicators),
        )
    }
}

/**
 * [Row] will be [Horizontal], [Column] is [Vertical].
 */
internal enum class LayoutOrientation {
    Horizontal,
    Vertical
}

private val DefaultRowMeasurePolicy: MultiContentMeasurePolicy = ProgressTrackerMeasurePolicy(
    orientation = Horizontal,
    arrangementSpacing = 8.dp,
)

private val DefaultColumnMeasurePolicy: MultiContentMeasurePolicy = ProgressTrackerMeasurePolicy(
    orientation = Vertical,
    arrangementSpacing = 8.dp,
)

@Composable
internal fun progressTrackerMeasurePolicy(
    orientation: LayoutOrientation,
): MultiContentMeasurePolicy =
    if (orientation == Horizontal) {
        DefaultRowMeasurePolicy
    } else {
        DefaultColumnMeasurePolicy
    }

@Composable
private fun StepLabel(
    onClick: () -> Unit,
    orientation: LayoutOrientation,
    modifier: Modifier = Modifier,
    label: CharSequence = "",
    selected: Boolean = false,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val labelColor by animateColorAsState(
        targetValue = if (enabled) SparkTheme.colors.onSurface else SparkTheme.colors.onSurface.dim1,
        label = "Label color",
    )
    CompositionLocalProvider(LocalContentColor provides labelColor) {
        val labelModifier = modifier
            .layoutId(LabelId)
            .paddingFromBaseline(top = 16.dp)
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                enabled = enabled,
            ) {
                onClick()
            }
        val textAlign = if (orientation == Horizontal) TextAlign.Center else TextAlign.Start
        when (label) {
            is AnnotatedString -> Text(
                text = label,
                textAlign = textAlign,
                modifier = labelModifier,
            )

            else -> Text(
                text = label.toString(),
                textAlign = textAlign,
                modifier = labelModifier,
            )
        }
    }
}

@Composable
private fun ProgressTrack(
    color: Color,
    orientation: LayoutOrientation,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val trackColor by animateColorAsState(
        targetValue = if (enabled) color else color.disabled,
        label = "Track color",
    )
    if (orientation == Horizontal) {
        Divider(modifier = modifier, color = trackColor)
    } else {
        VerticalDivider(modifier = modifier, color = trackColor)
    }
}

@Composable
private fun StepIndicator(
    colors: IntentColor,
    index: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = true,
    done: Boolean = false,
    onClick: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val elevation = animateStepElevation(enabled, interactionSource).value
    val indicatorColor by animateColorAsState(
        targetValue = if (selected) colors.color else colors.containerColor,
        label = "Indicator color",
    )
    val indicatorAlpha by animateFloatAsState(
        targetValue = if (enabled) 1f else SparkTheme.colors.dim3,
        label = "Indicator color",
    )

    Surface(
        shape = SparkTheme.shapes.full,
        modifier = modifier.graphicsLayer {
            alpha = indicatorAlpha
        },
        color = indicatorColor,
        elevation = elevation,
        enabled = enabled,
        onClick = { onClick?.invoke() },
    ) {
        Box(
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(
                targetState = done,
                contentAlignment = Alignment.Center,
                label = "Step status indicator",
            ) { isStepDone ->
                if (isStepDone) {
                    Icon(
                        sparkIcon = SparkIcons.Check,
                        modifier = Modifier.size(16.dp),
                        contentDescription = null, // content description is handle on the Layout
                    )
                } else {
                    val stepPosition = index + 1
                    Text(
                        text = "$stepPosition",
                        style = SparkTheme.typography.body2.highlight.copy(lineHeight = 16.sp),
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewProgressStep() {
    PreviewTheme(padding = PaddingValues(0.dp)) {
        var selectedStep by remember { mutableIntStateOf(0) }
        ProgressTrackerRow(
            modifier = Modifier.width(260.dp),
            items = persistentListOf(
                ProgressStep("Lorem ipsume", true),
                ProgressStep("Lorem ipsume dolar sit amet", true),
                ProgressStep("Lorem ipsume", false),
            ),
            onStepClick = {
                selectedStep = it
            },
            selectedStep = selectedStep,
        )
        ProgressTrackerColumn(
            modifier = Modifier.width(300.dp),
            items = persistentListOf(
                ProgressStep(
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.Ut enim ad minim veniam, quis nostrud exercitation.",
                    true,
                ),
                ProgressStep(
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                    true,
                ),
                ProgressStep("Lorem ipsume dolar sit amet", true),
            ),
            onStepClick = {
                selectedStep = it
            },
            selectedStep = selectedStep,
        )
    }
}
