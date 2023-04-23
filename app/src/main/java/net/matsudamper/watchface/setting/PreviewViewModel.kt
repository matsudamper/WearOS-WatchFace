package net.matsudamper.watchface.setting

import android.graphics.Bitmap
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.wear.watchface.DrawMode
import androidx.wear.watchface.RenderParameters
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.editor.EditorSession
import androidx.wear.watchface.style.UserStyle
import androidx.wear.watchface.style.WatchFaceLayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import net.matsudamper.watchface.complication.CustomComplicationSlot

class PreviewViewModel(
    private val scope: CoroutineScope,
    private val activity: ComponentActivity
) {
    lateinit var editorSession: EditorSession
    val uiState = MutableStateFlow<EditWatchFaceUiState>(EditWatchFaceUiState.Loading).also { uiStateFlow ->
        scope.launch {
            editorSession = EditorSession.createOnWatchEditorSession(
                activity = activity
            )
            uiStateFlow.emitAll(
                combine(
                    editorSession.userStyle,
                    editorSession.complicationsPreviewData
                ) { userStyle, complicationsPreviewData ->
                    yield()
                    EditWatchFaceUiState.Success(
                        bitmap = createWatchFacePreview(
                            editorSession,
                            userStyle,
                            complicationsPreviewData,
                        )
                    )
                }
            )
        }
    }

    private fun createWatchFacePreview(
        editorSession: EditorSession,
        userStyle: UserStyle,
        complicationsPreviewData: Map<Int, ComplicationData>
    ): Bitmap {
        val bitmap = editorSession.renderWatchFaceToBitmap(
            RenderParameters(
                DrawMode.INTERACTIVE,
                WatchFaceLayer.ALL_WATCH_FACE_LAYERS,
                RenderParameters.HighlightLayer(
                    RenderParameters.HighlightedElement.AllComplicationSlots,
                    Color.RED, // Red complication highlight.
                    Color.argb(128, 0, 0, 0) // Darken everything else.
                )
            ),
            editorSession.previewReferenceInstant,
            complicationsPreviewData
        )

        return bitmap
    }

    fun onClickSlot(slot: CustomComplicationSlot) {
        scope.launch(Dispatchers.Main.immediate) {
            editorSession.openComplicationDataSourceChooser(slot.id)
        }
    }

    sealed class EditWatchFaceUiState {
        data class Success(
            val bitmap: Bitmap,
        ) : EditWatchFaceUiState()

        object Loading : EditWatchFaceUiState()
        data class Error(val exception: Throwable) : EditWatchFaceUiState()
    }
}