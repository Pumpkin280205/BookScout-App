package com.anjor.bookscout.ui.components

import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

/**
 * Animated shimmer surface used as a loading placeholder. Slides a soft highlight across
 * the composable so the user sees "something is loading" instead of a blank screen.
 */
@Composable
fun ShimmerBox(modifier: Modifier = Modifier) {
    val base = MaterialTheme.colorScheme.surfaceVariant
    val highlight = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translate = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(tween(1300)),
        label = "shimmerTranslate"
    )

    Box(
        modifier = modifier
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(base, highlight, base),
                    start = Offset(translate.value - 300f, 0f),
                    end = Offset(translate.value, 0f)
                )
            )
    )
}

/** A single skeleton card approximating the shape of [BookCoverCard]. */
@Composable
private fun ShimmerCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    ) {
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
        )
        ShimmerBox(
            modifier = Modifier
                .padding(top = 8.dp, start = 8.dp, end = 8.dp)
                .fillMaxWidth(0.9f)
                .height(14.dp)
        )
        ShimmerBox(
            modifier = Modifier
                .padding(top = 6.dp, start = 8.dp, end = 8.dp, bottom = 8.dp)
                .fillMaxWidth(0.6f)
                .height(12.dp)
        )
    }
}

/** Grid of skeleton cards shown while the first page loads. */
@Composable
fun BooksShimmerGrid(count: Int = 6) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = false
    ) {
        items(count) { ShimmerCard() }
    }
}

/** A single wide shimmer block, e.g. for the detail screen header. */
@Composable
fun ShimmerBlock(modifier: Modifier = Modifier) {
    ShimmerBox(modifier = modifier)
}
