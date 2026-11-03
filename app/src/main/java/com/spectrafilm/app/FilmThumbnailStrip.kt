/*
 * Spektrafilm for Android — film thumbnail strip. GPLv3.
 * Film modeling powered by spektrafilm.
 *
 * 28 film stocks used to be reachable only through a sheet of text cards: to know what a stock
 * does to THIS photograph you read a sentence, closed the sheet, tapped "change", and read
 * another one. This strip renders the open frame through every stock at a small edge, so the
 * choice is made by looking, and a tap applies the stock immediately — one tap, no sheet.
 *
 * The rendering itself deliberately does not live here. The editor owns the engine and the
 * decoded source, and it must be able to stop this work the instant an interactive render
 * needs the cores; that is the whole reason upstream shipped text instead of thumbnails
 * (see StockBrowser.kt's header). This file is the surface; MainActivity owns the pump.
 */
package com.spectrafilm.app

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Long edge of one strip cell, in pixels. Small enough that a stock costs tens of milliseconds
 * rather than a second, large enough that grain character is still readable.
 */
internal const val FILM_THUMB_EDGE = 224

/**
 * How many decoded thumbnails to keep. 28 stocks plus headroom; each cell is ~224x168 RGBA8,
 * so the whole strip is a few MB at worst.
 */
internal const val FILM_THUMB_CACHE_MAX = 32

@Composable
internal fun FilmThumbnailStrip(
    options: List<DropdownOption>,
    selectedId: String,
    thumbs: Map<String, Bitmap>,
    modifier: Modifier = Modifier,
    onSelect: (String) -> Unit,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 2.dp),
    ) {
        items(options, key = { it.id }) { option ->
            FilmThumbCard(
                label = option.label,
                bitmap = thumbs[option.id],
                selected = option.id == selectedId,
                onClick = { onSelect(option.id) },
            )
        }
    }
}

@Composable
private fun FilmThumbCard(
    label: String,
    bitmap: Bitmap?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val accent = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier = Modifier
            .width(96.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .then(if (selected) Modifier.border(1.5.dp, accent, shape) else Modifier)
            .clickable(onClick = onClick)
            // One node per stock: the picture, the name and the selection state read together.
            .semantics(mergeDescendants = true) {
                role = Role.RadioButton
                this.selected = selected
            }
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            val frame = bitmap
            if (frame != null && !frame.isRecycled) {
                Image(
                    bitmap = frame.asImageBitmap(),
                    contentDescription = label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                // Pending: the pump has not reached this stock yet. Deliberately quiet rather
                // than a spinner per cell — 28 spinners is noise, not information.
                Text(
                    "…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = if (selected) accent else MaterialTheme.colorScheme.onSurface,
        )
    }
}
