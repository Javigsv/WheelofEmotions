package com.example.wheelofemotions.wheel

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.wheelofemotions.ViewBoundaries
import com.example.wheelofemotions.WheelItem
import com.example.wheelofemotions.WheelItemContent
import com.example.wheelofemotions.WheelScope
import com.example.wheelofemotions.WheelState


@OptIn(ExperimentalFoundationApi::class)
class WheelItemProvider(
    private val wheelState: WheelState,
    private val itemsState: State<List<WheelItemContent>>,
    private val wheelScope: WheelScope,
) : LazyLayoutItemProvider {


    override val itemCount
        get() = itemsState.value.size

    @Composable
    override fun Item(index: Int, key: Any) {
        val content = itemsState.value.getOrNull(index)
        // MAKE HERE THE WEDGE COMPOSABLE
        val degrees = wheelState.getRotationForIndex(index, itemsState.value.size)
        Box(
            modifier = Modifier
                .graphicsLayer {
                    this.transformOrigin = TransformOrigin(0f, 0.5f)
                    this.rotationZ = degrees
                }
                .background(Color.Cyan)
                .padding(10.dp)
        ) {
            content?.itemContent?.invoke(wheelScope, content.wheelItem.item)
        }
    }

    fun getItemIndexesInRange(boundaries: ViewBoundaries): List<Int> {
        val result = mutableListOf<Int>()

        result.addAll(0 until itemsState.value.size)

        return result
    }

    fun getItem(index: Int): WheelItem? {
        return itemsState.value.getOrNull(index)?.wheelItem
    }
}