/*
 * Copyright (c) 2023 Adevinta
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
package com.adevinta.spark

import app.cash.paparazzi.DeviceConfig
import com.airbnb.android.showkase.models.Showkase
import com.airbnb.android.showkase.models.ShowkaseBrowserComponent
import org.junit.Rule
import org.junit.Test

internal class PreviewScreenshotTests {

    enum class BaseDeviceConfig(
        val deviceConfig: DeviceConfig,
    ) {
        PIXEL_5(
            DeviceConfig.PIXEL_5.copy(
                softButtons = false,
                locale = "fr-rFR",
            ),
        ),
    }

    @get:Rule
    val paparazzi = paparazziRule()

    @Test
    fun preview_tests() {
        var failure: Throwable? = null
        Showkase.getMetadata().componentList.groupBy {
            // Currently Compose Previews generated by PreviewParameters have the same name.
            // So we need to group them to later manually add their variants
            previewFullName(it)
        }.forEach { (name, components) ->
            components.forEachIndexed { index, showkaseComponent ->
                try {
                    val previewNameWithVariants = when {
                        components.size > 3 -> name + partPro[index]
                        components.size > 1 -> name + lightDark[index]
                        else -> name
                    }
                    paparazzi.unsafeUpdateConfig(deviceConfig = BaseDeviceConfig.PIXEL_5.deviceConfig)
                    paparazzi.sparkSnapshot(name = previewNameWithVariants) {
                        showkaseComponent.component()
                    }
                } catch (t: Throwable) {
                    // TODO-@soulcramer (22-48-2023): Should we display the throwable message here too?
                    failure = t
                }
            }
        }
        if (failure != null) throw failure
    }
}

private val partPro = listOf("_part_light", "_pro_light", "_part_dark", "_pro_dark")
private val lightDark = listOf("_light", "_dark")
private fun previewFullName(preview: ShowkaseBrowserComponent): String {
    return (preview.group + "_" + preview.componentName).replace(" ", "")
}
