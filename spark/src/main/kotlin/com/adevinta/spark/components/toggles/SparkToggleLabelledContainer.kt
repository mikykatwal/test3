package com.adevinta.spark.components.toggles

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adevinta.spark.PreviewTheme
import com.adevinta.spark.SparkTheme
import com.adevinta.spark.components.spacer.HorizontalSpacer
import com.adevinta.spark.tools.modifiers.SlotArea
import com.adevinta.spark.tools.modifiers.minimumTouchTargetSize
import com.adevinta.spark.tools.modifiers.sparkUsageOverlay

@Composable
internal fun SparkToggleLabelledContainer(
    state: ToggleableState,
    toggle: @Composable () -> Unit,
    role: Role,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    startContent: @Composable() (RowScope.() -> Unit)? = null,
    endContent: @Composable() (RowScope.() -> Unit)? = null,
) {
    val toggleableModifier = if (onClick != null) {
        Modifier.triStateToggleable(
            state = state,
            onClick = onClick,
            enabled = enabled,
            role = role,
        )
    } else {
        Modifier
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .minimumTouchTargetSize()
            .clip(SparkTheme.shapes.small)
            .then(toggleableModifier)
            .sparkUsageOverlay(),
    ) {

        startContent?.let {
            HorizontalSpacer(8.dp)
            it()
            HorizontalSpacer(ToggleLabelledPadding)
        }

        toggle()

        endContent?.let {
            HorizontalSpacer(ToggleLabelledPadding)
            it()
            HorizontalSpacer(8.dp)
        }
    }
}

private val ToggleLabelledPadding = 16.dp

@Preview(
    group = "Toggles",
    name = "LabelledSlot",
)
@Composable
internal fun TogglesLabelledSlotPreview() {
    PreviewTheme {
        SparkToggleLabelledContainer(
            state = ToggleableState(true),
            toggle = {
                RadioButton(
                    modifier = Modifier.minimumTouchTargetSize(),
                    selected = true,
                    onClick = null,
                )
            },
            role = Role.Checkbox,
            onClick = {},
            startContent = {
                SlotArea(color = LocalContentColor.current) {
                    Text("CheckBox On")
                }
            },
        )

        SparkToggleLabelledContainer(
            state = ToggleableState(true),
            toggle = {
                RadioButton(
                    modifier = Modifier.minimumTouchTargetSize(),
                    selected = true,
                    onClick = null,
                )
            },
            role = Role.Checkbox,
            onClick = {},
            endContent = {
                SlotArea(color = LocalContentColor.current) {
                    Text("CheckBox On")
                }
            },
        )

        SparkToggleLabelledContainer(
            state = ToggleableState(true),
            toggle = {
                Checkbox(
                    modifier = Modifier.minimumTouchTargetSize(),
                    state = ToggleableState(true),
                    onClick = null,
                )
            },
            role = Role.Checkbox,
            onClick = {},
            endContent = {
                SlotArea(color = LocalContentColor.current) {
                    Text("CheckBox On")
                }
            },
        )
    }
}

@Preview
@Composable
private fun SparkToggleLabelledContainerPreview() {
    PreviewTheme {
        var state by remember { mutableStateOf(true) }
        SparkToggleLabelledContainer(
            state = ToggleableState(state),
            toggle = {
                RadioButton(
                    modifier = Modifier.minimumTouchTargetSize(),
                    selected = true,
                    onClick = null,
                )
            },
            role = Role.Checkbox,
            onClick = {
                state = !state
            },
            startContent = {
                Text("CheckBox On")
            },
        ) {
            Text("CheckBox On")
        }
        SparkToggleLabelledContainer(
            state = ToggleableState(state),
            toggle = {
                RadioButton(
                    modifier = Modifier.minimumTouchTargetSize(),
                    selected = state,
                    onClick = null,
                )
            },
            role = Role.Checkbox,
            onClick = {
                state = !state
            },
            startContent = {
                Text(
                    "CheckBox OnRadioButton Off \nRadioButton Off \nRadioButton Off \n",
                )
            },
        ) {
            Text(
                "CheckBox OnRadioButton Off \nRadioButton Off \nRadioButton Off \n",
            )
        }
    }
}
