package com.adevinta.spark

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.airbnb.android.showkase.models.Showkase
import com.airbnb.android.showkase.models.ShowkaseBrowserComponent
import com.android.ide.common.rendering.api.SessionParams
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
//@Ignore("Tests fails on CI, ignored until we figure out why it fails") // FIXME
class PreviewScreenshotTests {

    enum class BaseDeviceConfig(
        val deviceConfig: DeviceConfig,
    ) {
        PIXEL_5(
            DeviceConfig.PIXEL_5.copy(
                softButtons = false,
                locale = "fr-rFR",
            ),
        ),
        // TODO scott.rayapoulle.ext-19/09/2022: Enable when we can use Git LFS since it would duplicate the current
        //  weight of all pngs
//        PIXEL_C(DeviceConfig.PIXEL_C.copy(softButtons = false, screenHeight = 1)),
    }

    @get:Rule
    val paparazzi = Paparazzi(
        maxPercentDifference = 0.1, // We can't use 0 until https://github.com/cashapp/paparazzi/issues/554 is fixed
        theme = "android:Theme.MaterialComponent.Light.NoActionBar",
        renderingMode = SessionParams.RenderingMode.SHRINK,
        showSystemUi = false,
    )

    @Test
    fun preview_tests() {
        Showkase.getMetadata().componentList.groupBy {
            // Currently Compose Previews generated by PreviewParameters have the same name.
            // So we need to group them to later manually add their variants
            previewFullName(it)
        }.forEach { (name, components) ->
            components.forEachIndexed { index, showkaseComponent ->

                val previewNameWithVariants = when {
                    components.size > 3 -> name + partPro[index]
                    components.size > 1 -> name + lightDark[index]
                    else -> name
                }
                paparazzi.unsafeUpdateConfig(deviceConfig = BaseDeviceConfig.PIXEL_5.deviceConfig)
                paparazzi.snapshotPlus(name = previewNameWithVariants) {
                    showkaseComponent.component()
                }
            }
        }
    }
}

private val partPro = listOf("_part_light", "_pro_light", "_part_dark", "_pro_dark")
private val lightDark = listOf("_light", "_dark")
private fun previewFullName(preview: ShowkaseBrowserComponent): String {
    return (preview.group + "_" + preview.componentName).replace(" ", "")
}
