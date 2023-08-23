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
package com.adevinta.spark.catalog.icons

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import androidx.navigation.NavController
import com.adevinta.spark.SparkTheme
import com.adevinta.spark.catalog.R
import com.adevinta.spark.components.icons.Icon
import com.adevinta.spark.components.icons.IconSize
import com.adevinta.spark.components.spacer.VerticalSpacer
import com.adevinta.spark.components.text.Text
import com.adevinta.spark.components.textfields.TextField
import com.adevinta.spark.icons.DeleteFill
import com.adevinta.spark.icons.Search
import com.adevinta.spark.icons.SparkIcon
import com.adevinta.spark.icons.SparkIcons
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.withContext
import java.util.Locale
import com.adevinta.spark.icons.R as IconR

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
public fun IconsScreen(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var icons: List<NamedIcon> by remember {
        mutableStateOf(emptyList())
    }
    LaunchedEffect(Unit) {
        icons = getAllIconsRes(context)
    }
    var query: String by rememberSaveable { mutableStateOf("") }
    val filteredIcons by remember {
        derivedStateOf {
            if (query.isEmpty()) icons else icons.filter { it.name.contains(query, ignoreCase = true) }
        }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        TextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = stringResource(id = R.string.icons_screen_search_helper),
            leadingContent = {
                Icon(sparkIcon = SparkIcons.Search, contentDescription = null)
            },
            trailingContent = {
                Icon(
                    modifier = Modifier.clickable { query = "" },
                    sparkIcon = SparkIcons.DeleteFill,
                    contentDescription = "Clear",
                )
            },
        )
        VerticalSpacer(space = 16.dp)
        LazyVerticalGrid(
            modifier = modifier
                .consumeWindowInsets(contentPadding)
                .fillMaxSize()
                .clickable(
                    // no ripple effect is needed as this onClick is just to clear the focus of the search field
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) {
                    focusManager.clearFocus()
                },

            contentPadding = contentPadding,
            columns = GridCells.Adaptive(minSize = 60.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(filteredIcons.size) { index ->
                val (drawableRes, iconName) = filteredIcons[index]
                Column(
                    modifier = Modifier
                        .clip(SparkTheme.shapes.small)
                        .combinedClickable(
                            onLongClick = { copyToClipboard(context, iconName) },
                            onClick = {
                                navController.navigate(
                                    route = "$IconDemoRoute/$drawableRes/$iconName",
                                )
                            },
                        )
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        sparkIcon = SparkIcon.DrawableRes(drawableRes),
                        contentDescription = null,
                        size = IconSize.Large,
                    )
                    Text(
                        text = iconName,
                        style = SparkTheme.typography.caption,
                    )
                }
            }
        }
    }
}

private data class NamedIcon(
    @DrawableRes val drawableRes: Int,
    val name: String,
)

private suspend fun getAllIconsRes(context: Context) = withContext(Default) {
    IconR.drawable::class.java.declaredFields.mapNotNull { field ->
        val prefix = "spark_icons_"
        val icon = field.getInt(null)
        val name = context.resources.getResourceEntryName(icon)
        if (!name.startsWith(prefix)) return@mapNotNull null
        NamedIcon(
            drawableRes = icon,
            name = name.removePrefix(prefix).toPascalCase(),
        )
    }
}

private fun String.toPascalCase(): String = split("_").joinToString(separator = "") { str ->
    str.replaceFirstChar {
        if (it.isLowerCase()) {
            it.titlecase(Locale.ROOT)
        } else {
            it.toString()
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService<ClipboardManager>() ?: return
    val clip = ClipData.newPlainText("spark_icon_name", text)
    clipboardManager.setPrimaryClip(clip)
}
