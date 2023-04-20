package net.matsudamper.watchface

import android.graphics.RectF
import android.view.SurfaceHolder
import androidx.wear.watchface.CanvasComplicationFactory
import androidx.wear.watchface.ComplicationSlot
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.WatchFace
import androidx.wear.watchface.WatchFaceService
import androidx.wear.watchface.WatchFaceType
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.complications.ComplicationSlotBounds
import androidx.wear.watchface.complications.DefaultComplicationDataSourcePolicy
import androidx.wear.watchface.complications.SystemDataSources
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.rendering.CanvasComplicationDrawable
import androidx.wear.watchface.complications.rendering.ComplicationDrawable
import androidx.wear.watchface.style.CurrentUserStyleRepository

object CustomComplicationSlot {
    object ID1 {
//        private val LEFT =
//        private val TOP =
//        private val RIGHT =
//        private val BOTTOM =
    }
}

class MyWatchFaceService : WatchFaceService() {

    // https://github.com/android/wear-os-samples/blob/641c839ca3d86e685400ab96df5984ad0a85490a/WatchFaceKotlin/app/src/main/java/com/example/android/wearable/alpha/utils/ComplicationUtils.kt#L78
    override fun createComplicationSlotsManager(
        currentUserStyleRepository: CurrentUserStyleRepository,
    ): ComplicationSlotsManager {
        return super.createComplicationSlotsManager(currentUserStyleRepository)
        val defaultCanvasComplicationFactory = CanvasComplicationFactory { watchState, listener ->
            CanvasComplicationDrawable(
                ComplicationDrawable.getDrawable(this, R.drawable.complication_red_style)!!,
                watchState,
                listener
            )
        }

        val complicationSlot1 = ComplicationSlot.createRoundRectComplicationSlotBuilder(
            id = 1,
            canvasComplicationFactory = defaultCanvasComplicationFactory,
            supportedTypes = listOf(
                ComplicationType.RANGED_VALUE,
                ComplicationType.MONOCHROMATIC_IMAGE,
                ComplicationType.SHORT_TEXT,
                ComplicationType.SMALL_IMAGE,
            ),
            defaultDataSourcePolicy = DefaultComplicationDataSourcePolicy(
                SystemDataSources.NO_DATA_SOURCE,
                ComplicationType.SHORT_TEXT
            ),
            bounds = ComplicationSlotBounds(
                RectF(
                    0.1f, 0.1f, 0.2f, 0.2f
                )
            ),
        ).build()

        return ComplicationSlotsManager(
            listOf(complicationSlot1),
            currentUserStyleRepository
        )
    }

    override suspend fun createWatchFace(
        surfaceHolder: SurfaceHolder,
        watchState: WatchState,
        complicationSlotsManager: ComplicationSlotsManager,
        currentUserStyleRepository: CurrentUserStyleRepository
    ): WatchFace {
        return WatchFace(
            watchFaceType = WatchFaceType.ANALOG,
            renderer = MyRenderer(
                context = this,
                surfaceHolder = surfaceHolder,
                currentUserStyleRepository = currentUserStyleRepository,
                watchState = watchState,
                complicationSlotsManager = complicationSlotsManager,
            ),
        )
    }
}
