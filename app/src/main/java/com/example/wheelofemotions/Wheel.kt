package com.example.wheelofemotions

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.layout.LazyLayout
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.example.wheelofemotions.wheel.WheelItemProvider
import kotlin.math.roundToInt

data class WheelItem(
    val item: String,
    val initialRotation: Double,
)

data class ViewBoundaries(
    val fromX: Int,
    val toX: Int,
    val fromY: Int,
    val toY: Int
)

typealias ComposableItemContent = @Composable WheelScope.(item: String) -> Unit

data class WheelItemContent(
    val wheelItem: WheelItem,
    val itemContent: ComposableItemContent
)

interface WheelScope {

    fun items(items: List<String>, itemContent: ComposableItemContent)
}

class WheelScopeImpl() : WheelScope {

    private val _items = mutableListOf<WheelItemContent>()
    val items: List<WheelItemContent> = _items

    override fun items(
        items: List<String>,
        itemContent: ComposableItemContent,
    ) {
        items.forEachIndexed { index, item ->
            _items.add(
                WheelItemContent(
                    WheelItem(
                        item = item,
                        initialRotation = index / 2.0
                    ),
                    itemContent
                )
            )
        }
    }
}

@Composable
fun rememberWheelState(): WheelState {
    return remember {
        WheelState()
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.spinable(
    state: WheelState,
): Modifier = composed {
    this then scrollable(
        state = rememberScrollableState { delta ->
            state.onDrag(delta)
            delta
        },
        orientation = Orientation.Vertical,
        overscrollEffect = null,
        enabled = true,
        reverseDirection = false,
        flingBehavior = null,
        interactionSource = null
    ) then scrollable(
        state = rememberScrollableState { delta ->
            state.onDrag(delta)
            delta
        },
        orientation = Orientation.Horizontal,
        overscrollEffect = null,
        enabled = true,
        reverseDirection = false,
        flingBehavior = null,
        interactionSource = null
    )
}

@Stable
class WheelState {
    private val degreesOffsetState = mutableFloatStateOf(45f)

    fun onDrag(delta: Float) {
        val y = (degreesOffsetState.floatValue + (delta / 5000) * 360).mod(360f)
        degreesOffsetState.floatValue = y
    }

    fun getSelectedIndex(total: Int): Int {
        return ((degreesOffsetState.floatValue / 360) * total).roundToInt()
    }

    fun getBoundaries(
        constraints: Constraints,
        threshold: Int = 500
    ): ViewBoundaries {
        return ViewBoundaries(
            fromX = -threshold,
            toX = constraints.maxWidth + threshold,
            fromY = -threshold,
            toY = constraints.maxHeight + threshold
        )
    }

    fun getRotationForIndex(index: Int, total: Int): Float {
        val percentage = index.toFloat() / total

        require(percentage in 0.0..1.0)

        return (percentage * 360 + degreesOffsetState.floatValue).mod(360f)
    }
}

@Composable
fun rememberItemProvider(
    wheelScopeContent: WheelScope.() -> Unit,
    wheelState: WheelState,
): WheelItemProvider {
    val customLazyListScopeState = remember { mutableStateOf(wheelScopeContent) }.apply {
        value = wheelScopeContent
    }

    val scope = WheelScopeImpl()

    return remember {
        WheelItemProvider(
            wheelState = wheelState,
            itemsState = derivedStateOf {
                val layoutScope = WheelScopeImpl().apply(
                    customLazyListScopeState.value
                )
                layoutScope.items
            },
            wheelScope = scope,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Wheel(
    modifier: Modifier = Modifier,
    state: WheelState = rememberWheelState(),
    content: WheelScope.() -> Unit
) {

    val itemProvider = rememberItemProvider(
        wheelScopeContent = content,
        wheelState = state,
    )

    Row {

    }

    LazyLayout(
        itemProvider = itemProvider,
        modifier = modifier
            .clipToBounds()
            .spinable(
                state,
            ),
        prefetchState = null,
    ) { constraints ->

        val boundaries = state.getBoundaries(constraints)
        val indexes = itemProvider.getItemIndexesInRange(boundaries)

        val indexesWithPlaceables = indexes.associateWith {
            measure(it, Constraints())
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            indexesWithPlaceables.forEach { (index, placeables) ->
                val item = itemProvider.getItem(index)
                item?.let { placeItem(state, item, placeables) }
            }

        }

        // This may work?
//        layout(constraints.maxWidth, constraints.maxHeight) {
//            placeable?.placeRelative(offsetX, offsetY)
//
//            // Rotate the canvas
//            val rotationAngleDegrees = 45f
//            val radians = Math.toRadians(rotationAngleDegrees.toDouble())
//            val rotationMatrix = Matrix().apply {
//                setRotate(rotationAngleDegrees, placeableWidth / 2f, placeableHeight / 2f)
//            }
//            withTransform({ canvas ->
//                canvas.concat(rotationMatrix)
//            }) {
//                placeable?.draw(this)
//            }
//        }
    }
}

private fun Placeable.PlacementScope.placeItem(
    state: WheelState,
    listItem: WheelItem,
    placeables: List<Placeable>
) {
    val xPosition = 250
    val yPosition = 250

    placeables.forEach { placeable ->
        placeable.placeRelative(xPosition, yPosition)
    }
}



@Preview
@Composable
fun WheelPreview() {
    Surface {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Wheel(
                modifier = Modifier.background(Color.Red)
            ) {
                items(listOf("Peter", "Karl", "John", "Martha", "Lauren")) { item ->
                    Text(text = item)
                }
            }
        }
    }
}

@Composable
fun Modifier.wedge(): Modifier = this then composed {
    val density = LocalDensity.current

    Modifier.drawBehind {
        val width = size.width
        val height = size.height

//        // Left border
//        drawLine(
//            color = color,
//            start = Offset(x = offset, y = height),
//            end = Offset(x = offset, y = cornerRadiusPx),
//            strokeWidth = strokeWidthPx
//        )
//
//        // Top left corner
//        drawArc(
//            color = color,
//            startAngle = 180f,
//            sweepAngle = 90f,
//            useCenter = false,
//            topLeft = Offset(offset, offset),
//            size = Size(cornerRadiusPx * 2, cornerRadiusPx * 2),
//            style = Stroke(width = strokeWidthPx)
//        )
//
//        // Top border
//        drawLine(
//            color = color,
//            start = Offset(x = cornerRadiusPx, y = offset),
//            end = Offset(x = width - cornerRadiusPx, y = offset),
//            strokeWidth = strokeWidthPx
//        )
//
//        // Top right corner
//        drawArc(
//            color = color,
//            startAngle = 270f,
//            sweepAngle = 90f,
//            useCenter = false,
//            topLeft = Offset(x = width - cornerRadiusPx * 2 - offset, y = offset),
//            size = Size(cornerRadiusPx * 2, cornerRadiusPx * 2),
//            style = Stroke(width = strokeWidthPx)
//        )
//
//        // Left border
//        drawLine(
//            color = color,
//            start = Offset(x = width - offset, y = height),
//            end = Offset(x = width - offset, y = cornerRadiusPx),
//            strokeWidth = strokeWidthPx
//        )
//
//        // Bottom border
//        if (hasBottomSeparator) {
//            drawLine(
//                color = color,
//                start = Offset(x = 0f, y = height - offset),
//                end = Offset(x = width, y = height - offset),
//                strokeWidth = strokeWidthPx
//            )
//        }
    }
}
