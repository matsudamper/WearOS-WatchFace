package net.matsudamper.watchface

import android.content.Context
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
import androidx.wear.watchface.style.UserStyleSchema
import androidx.wear.watchface.style.UserStyleSetting
import androidx.wear.watchface.style.WatchFaceLayer
import net.matsudamper.watchface.complication.CustomComplicationSlot

class MyWatchFaceService : WatchFaceService() {

//    override fun createUserStyleSchema(): UserStyleSchema {
//        return createUserStyleSchema(context = this)
//    }

    // https://github.com/android/wear-os-samples/blob/641c839ca3d86e685400ab96df5984ad0a85490a/WatchFaceKotlin/app/src/main/java/com/example/android/wearable/alpha/utils/ComplicationUtils.kt#L78
    override fun createComplicationSlotsManager(
        currentUserStyleRepository: CurrentUserStyleRepository,
    ): ComplicationSlotsManager {
        val defaultCanvasComplicationFactory = CanvasComplicationFactory { watchState, listener ->
            CanvasComplicationDrawable(
                ComplicationDrawable.getDrawable(this, R.drawable.complication_red_style)!!,
                watchState,
                listener
            )
        }

        println("${CustomComplicationSlot.Slot0}")
        println("${CustomComplicationSlot.Slot1}")

        val complicationSlot1 = ComplicationSlot.createRoundRectComplicationSlotBuilder(
            id = CustomComplicationSlot.Slot0.id,
            canvasComplicationFactory = defaultCanvasComplicationFactory,
            supportedTypes = listOf(
                ComplicationType.RANGED_VALUE,
                ComplicationType.MONOCHROMATIC_IMAGE,
                ComplicationType.SHORT_TEXT,
                ComplicationType.SMALL_IMAGE
            ),
            defaultDataSourcePolicy = DefaultComplicationDataSourcePolicy(
                SystemDataSources.DATA_SOURCE_DAY_OF_WEEK,
                ComplicationType.SHORT_TEXT
            ),
            bounds = ComplicationSlotBounds(
                RectF(
                    CustomComplicationSlot.Slot0.left,
                    CustomComplicationSlot.Slot0.top,
                    CustomComplicationSlot.Slot0.right,
                    CustomComplicationSlot.Slot0.bottom,
                )
            ),
        ).build()

        val complicationSlot2 = ComplicationSlot.createRoundRectComplicationSlotBuilder(
            id = CustomComplicationSlot.Slot1.id,
            canvasComplicationFactory = defaultCanvasComplicationFactory,
            supportedTypes = listOf(
                ComplicationType.RANGED_VALUE,
                ComplicationType.MONOCHROMATIC_IMAGE,
                ComplicationType.SHORT_TEXT,
                ComplicationType.SMALL_IMAGE
            ),
            defaultDataSourcePolicy = DefaultComplicationDataSourcePolicy(
                SystemDataSources.DATA_SOURCE_STEP_COUNT,
                ComplicationType.SHORT_TEXT
            ),
            bounds = ComplicationSlotBounds(
                RectF(
                    CustomComplicationSlot.Slot1.left,
                    CustomComplicationSlot.Slot1.top,
                    CustomComplicationSlot.Slot1.right,
                    CustomComplicationSlot.Slot1.bottom,
                )
            ),
        ).build()

        return ComplicationSlotsManager(
            listOf(complicationSlot1, complicationSlot2),
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

const val COLOR_STYLE_SETTING = "color_style_setting"
const val DRAW_HOUR_PIPS_STYLE_SETTING = "draw_hour_pips_style_setting"
const val WATCH_HAND_LENGTH_STYLE_SETTING = "watch_hand_length_style_setting"

/**
 * https://github.com/android/wear-os-samples/blob/641c839ca3d86e685400ab96df5984ad0a85490a/WatchFaceKotlin/app/src/main/java/com/example/android/wearable/alpha/utils/UserStyleSchemaUtils.kt#L41
 */
private fun createUserStyleSchema(context: Context): UserStyleSchema {
    // 1. Allows user to change the color styles of the watch face (if any are available).
    val colorStyleSetting =
        UserStyleSetting.ListUserStyleSetting(
            UserStyleSetting.Id(COLOR_STYLE_SETTING),
            context.resources,
            R.string.app_name,
            R.string.complication_label,
            null,
            listOf(),
            listOf(
                WatchFaceLayer.BASE,
                WatchFaceLayer.COMPLICATIONS,
                WatchFaceLayer.COMPLICATIONS_OVERLAY
            )
        )

//    // 2. Allows user to toggle on/off the hour pips (dashes around the outer edge of the watch
//    // face).
//    val drawHourPipsStyleSetting = UserStyleSetting.BooleanUserStyleSetting(
//        UserStyleSetting.Id(DRAW_HOUR_PIPS_STYLE_SETTING),
//        context.resources,
//        R.string.watchface_pips_setting,
//        R.string.watchface_pips_setting_description,
//        null,
//        listOf(WatchFaceLayer.BASE),
//        DRAW_HOUR_PIPS_DEFAULT
//    )

//    // 3. Allows user to change the length of the minute hand.
//    val watchHandLengthStyleSetting = UserStyleSetting.DoubleRangeUserStyleSetting(
//        UserStyleSetting.Id(WATCH_HAND_LENGTH_STYLE_SETTING),
//        context.resources,
//        R.string.watchface_hand_length_setting,
//        R.string.watchface_hand_length_setting_description,
//        null,
//        MINUTE_HAND_LENGTH_FRACTION_MINIMUM.toDouble(),
//        MINUTE_HAND_LENGTH_FRACTION_MAXIMUM.toDouble(),
//        listOf(WatchFaceLayer.COMPLICATIONS_OVERLAY),
//        MINUTE_HAND_LENGTH_FRACTION_DEFAULT.toDouble()
//    )

    // 4. Create style settings to hold all options.
    return UserStyleSchema(
        listOf(
            colorStyleSetting,
//            drawHourPipsStyleSetting,
//            watchHandLengthStyleSetting
        )
    )
}