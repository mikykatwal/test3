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
package com.adevinta.spark.catalog.showkase

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.adevinta.spark.tokens.Layout
import com.airbnb.android.showkase.models.ShowkaseBrowserComponent

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ComponentDetailScreen(
    groupedComponentMap: Map<String, List<ShowkaseBrowserComponent>>,
    showkaseBrowserScreenMetadata: MutableState<ShowkaseBrowserScreenMetadata>,
    navController: NavHostController,
    contentPadding: PaddingValues,
) {
    val componentMetadataList =
        groupedComponentMap[showkaseBrowserScreenMetadata.value.currentGroup] ?: return
    val componentMetadata = componentMetadataList.find {
        it.componentKey == showkaseBrowserScreenMetadata.value.currentComponentKey
    } ?: return
    LazyColumn(
        modifier = Modifier
            .testTag("ShowkaseComponentDetailList")
            .fillMaxSize()
            .consumeWindowInsets(contentPadding),
        contentPadding = PaddingValues(
            start = Layout.bodyMargin / 2 + contentPadding.calculateLeftPadding(LocalLayoutDirection.current),
            end = Layout.bodyMargin / 2 + contentPadding.calculateRightPadding(LocalLayoutDirection.current),
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding(),
        ),
    ) {
        items(
            items = listOf(componentMetadata),
            itemContent = {
                val composableModifier = Modifier.generateComposableModifier(componentMetadata)
                Box(modifier = composableModifier) {
                    componentMetadata.component()
                }
            },
        )
    }
    BackHandler {
        back(showkaseBrowserScreenMetadata, navController)
    }
}

internal fun Modifier.generateComposableModifier(metadata: ShowkaseBrowserComponent) = composed {
    val baseModifier = sizeIn(maxHeight = Dp(LocalConfiguration.current.screenHeightDp.toFloat()))
    when {
        metadata.heightDp != null && metadata.widthDp != null -> baseModifier.size(
            width = metadata.widthDp!!.dp,
            height = metadata.heightDp!!.dp,
        )

        metadata.heightDp != null -> baseModifier.height(Dp(metadata.heightDp!!.toFloat()))
        metadata.widthDp != null -> baseModifier.width(Dp(metadata.widthDp!!.toFloat()))
        else -> baseModifier.fillMaxWidth()
    }
}

private fun back(
    showkaseBrowserScreenMetadata: MutableState<ShowkaseBrowserScreenMetadata>,
    navController: NavHostController,
) {
    showkaseBrowserScreenMetadata.update {
        copy(
            currentComponentStyleName = null,
            isSearchActive = false,
            searchQuery = null,
        )
    }
    navController.popBackStack()
}
