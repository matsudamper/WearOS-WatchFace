package net.matsudamper.watchface.setting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.CircularProgressIndicator
import net.matsudamper.watchface.complication.CustomComplicationSlot

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val previewViewModel = PreviewViewModel(
            activity = this,
            scope = this.lifecycleScope
        )
        setContent {
            BoxWithConstraints {
                val containerWidth by rememberUpdatedState(maxWidth)
                val containerHeight by rememberUpdatedState(maxHeight)
                Box(
                    modifier = Modifier.size(maxWidth, maxHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.TopStart
                        ) {
                            SlotOverlay(
                                modifier = Modifier.fillMaxSize(),
                                slot = CustomComplicationSlot.Slot0,
                                containerWidth = containerWidth,
                                containerHeight = containerHeight,
                                onClick = {
                                    previewViewModel.onClickSlot(CustomComplicationSlot.Slot0)
                                }
                            )
                            SlotOverlay(
                                modifier = Modifier.fillMaxSize(),
                                slot = CustomComplicationSlot.Slot1,
                                containerWidth = containerWidth,
                                containerHeight = containerHeight,
                                onClick = {
                                    previewViewModel.onClickSlot(CustomComplicationSlot.Slot1)
                                }
                            )
                            SlotOverlay(
                                modifier = Modifier.fillMaxSize(),
                                slot = CustomComplicationSlot.Slot2,
                                containerWidth = containerWidth,
                                containerHeight = containerHeight,
                                onClick = {
                                    previewViewModel.onClickSlot(CustomComplicationSlot.Slot2)
                                }
                            )
                            SlotOverlay(
                                modifier = Modifier.fillMaxSize(),
                                slot = CustomComplicationSlot.Slot3,
                                containerWidth = containerWidth,
                                containerHeight = containerHeight,
                                onClick = {
                                    previewViewModel.onClickSlot(CustomComplicationSlot.Slot3)
                                }
                            )
                            SlotOverlay(
                                modifier = Modifier.fillMaxSize(),
                                slot = CustomComplicationSlot.Slot4,
                                containerWidth = containerWidth,
                                containerHeight = containerHeight,
                                onClick = {
                                    previewViewModel.onClickSlot(CustomComplicationSlot.Slot4)
                                }
                            )
                            SlotOverlay(
                                modifier = Modifier.fillMaxSize(),
                                slot = CustomComplicationSlot.Slot5,
                                containerWidth = containerWidth,
                                containerHeight = containerHeight,
                                onClick = {
                                    previewViewModel.onClickSlot(CustomComplicationSlot.Slot5)
                                }
                            )
                        }
                        when (val state = previewViewModel.uiState.collectAsState().value) {
                            is PreviewViewModel.EditWatchFaceUiState.Error -> {}
                            is PreviewViewModel.EditWatchFaceUiState.Loading -> {
                                CircularProgressIndicator()
                            }

                            is PreviewViewModel.EditWatchFaceUiState.Success -> {
                                Image(
                                    modifier = Modifier.fillMaxSize(),
                                    bitmap = state.bitmap.asImageBitmap(),
                                    contentDescription = null,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SlotOverlay(
    modifier: Modifier = Modifier,
    slot: CustomComplicationSlot,
    containerWidth: Dp,
    containerHeight: Dp,
    onClick: () -> Unit,
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .offset(
                    x = containerWidth * slot.left,
                    y = containerHeight * slot.top,
                )
                .size(
                    width = containerWidth * (slot.right - slot.left),
                    height = containerHeight * (slot.bottom - slot.top)
                )
                .clickable {
                    onClick()
                }
        )
    }
}
