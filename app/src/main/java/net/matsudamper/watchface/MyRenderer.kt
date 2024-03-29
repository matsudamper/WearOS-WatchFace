package net.matsudamper.watchface

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.view.SurfaceHolder
import androidx.core.graphics.withRotation
import androidx.wear.watchface.CanvasType
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.DrawMode
import androidx.wear.watchface.Renderer
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.style.CurrentUserStyleRepository
import androidx.wear.watchface.style.WatchFaceLayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import net.matsudamper.watchface.complication.CustomComplicationSlot
import java.lang.Integer.min
import java.lang.StrictMath.cos
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import kotlin.math.sin


class MyRenderer(
    context: Context,
    surfaceHolder: SurfaceHolder,
    currentUserStyleRepository: CurrentUserStyleRepository,
    private val complicationSlotsManager: ComplicationSlotsManager,
    watchState: WatchState
) : Renderer.CanvasRenderer2<Renderer.SharedAssets>(
    surfaceHolder = surfaceHolder,
    currentUserStyleRepository = currentUserStyleRepository,
    watchState = watchState,
    canvasType = CanvasType.HARDWARE,
    interactiveDrawModeUpdateDelayMillis = 16L,
    clearWithBackgroundTintBeforeRenderingHighlightLayer = false,
) {
    private val digitalTimeFormatter = DateTimeFormatterBuilder()
        .appendValue(ChronoField.HOUR_OF_DAY, 2)
        .appendLiteral(":")
        .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
        .toFormatter()

    private val outCirclePadding = 4f

    private val longTimeDiskPaint = Paint().also { paint ->
        paint.color = Color.WHITE
        paint.strokeWidth = 2f
        paint.isAntiAlias = true
    }
    private val shortTimeDiskPaint = Paint().also { paint ->
        paint.color = Color.DKGRAY
        paint.strokeWidth = 2f
        paint.isAntiAlias = true
    }
    private val digitalTimePaint = Paint().also { paint ->
        paint.color = Color.WHITE
        paint.textSize = 36f
        paint.isAntiAlias = true
    }
    private val outCirclePaint = Paint().also { paint ->
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = outCirclePadding
        paint.color = Color.MAGENTA
        paint.isAntiAlias = true
    }
    private val timeNumberPaint = Paint().also { paint ->
        paint.color = Color.WHITE
        paint.textSize = 24f
        paint.isAntiAlias = true
    }
    private val functionCirclePaint = Paint().also { paint ->
        paint.style = Paint.Style.STROKE
        paint.color = Color.LTGRAY
        paint.strokeWidth = 2f
    }
    private val hourHandPaint = Paint().also { paint ->
        paint.color = Color.GREEN
        paint.strokeWidth = 2f
        paint.style = Paint.Style.STROKE
        paint.isAntiAlias = true
    }
    private val minuteHandPaint = Paint().also { paint ->
        paint.color = Color.GREEN
        paint.strokeWidth = 2f
        paint.style = Paint.Style.STROKE
        paint.isAntiAlias = true
    }


    private val scope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override suspend fun createSharedAssets(): SharedAssets {
        return object : SharedAssets {
            override fun onDestroy() {
            }
        }
    }

    override fun renderHighlightLayer(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        sharedAssets: SharedAssets
    ) {
        for ((_, complication) in complicationSlotsManager.complicationSlots) {
            if (complication.enabled) {
                complication.renderHighlightLayer(canvas, zonedDateTime, renderParameters)
            }
        }
    }

    override fun render(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        sharedAssets: SharedAssets
    ) {
        val backgroundColor = if (renderParameters.drawMode == DrawMode.AMBIENT) {
            Color.DKGRAY
        } else {
            Color.GRAY
        }

        if (renderParameters.watchFaceLayers.contains(WatchFaceLayer.COMPLICATIONS_OVERLAY)) {
            drawOutCircle(
                canvas = canvas,
                bounds = bounds,
                fraction = zonedDateTime.second.toDouble()
                    .plus(zonedDateTime.get(ChronoField.MILLI_OF_SECOND) * 0.001)
                    .div(60)
                    .toFloat()
            )
            drawTimeCircle(
                canvas = canvas,
                bounds = bounds,
            )
            // 短針
            drawTimeRoundRect(
                canvas = canvas,
                bounds = bounds,
                degrees = run {
                    val oneHourAndre = 360f / 12f
                    val minAngle = oneHourAndre / 60f
                    val secAngle = minAngle / 60f

                    (oneHourAndre.times(zonedDateTime.hour % 12))
                        .plus(minAngle * zonedDateTime.minute)
                        .plus(secAngle * zonedDateTime.second)

                },
                widthFraction = 0.1f,
                innerPaddingFraction = 0.1f,
                outerPaddingFraction = 0.5f,
                paint = hourHandPaint
            )
            // 長針
            drawTimeRoundRect(
                canvas = canvas,
                bounds = bounds,
                degrees = run {
                    val minAngle = 360f / 60f
                    minAngle * zonedDateTime.minute
                },
                widthFraction = 0.05f,
                innerPaddingFraction = 0.1f,
                outerPaddingFraction = 0.15f,
                paint = minuteHandPaint
            )
            drawDigitalTime(
                y = bounds.height() * 0.5f,
                canvas = canvas,
                zonedDateTime = zonedDateTime,
                bounds = bounds,
            )
        }

        for ((_, complication) in complicationSlotsManager.complicationSlots) {
            if (complication.enabled) {
                complication.render(canvas, zonedDateTime, renderParameters)
            }
        }
    }

    private fun drawTimeRoundRect(
        canvas: Canvas,
        bounds: Rect,
        degrees: Float,
        widthFraction: Float,
        outerPaddingFraction: Float,
        innerPaddingFraction: Float,
        paint: Paint,
    ) {
        val radius: Float = min(bounds.width(), bounds.height()) / 2f
        val halfWidth = (radius * widthFraction) / 2

        canvas.withRotation(degrees, centerX, centerY) {
            canvas.drawRoundRect(
                RectF(
                    centerX - halfWidth,
                    radius * outerPaddingFraction,
                    centerX + halfWidth,
                    centerY + halfWidth,
                ),
                halfWidth,
                halfWidth,
                paint,
            )
        }
    }

    private fun drawTimeCircle(canvas: Canvas, bounds: Rect) {
        val lineCount = 12 * 5
        val betWeenAngle = 360.0f / lineCount

        val radius: Float = min(bounds.width(), bounds.height()) / 2f
        val longLineLengthFraction = 0.05f
        val innerLongLineFraction = radius * (1 - longLineLengthFraction)
        val shortLineLengthFraction = 0.05f
        val innerShortLineFraction = radius * (1 - shortLineLengthFraction)
        for (index in 0 until lineCount) {
            val drawAngle = (betWeenAngle * (index + (5 * 6))).toDouble()
            val sin = -sin(Math.toRadians(drawAngle)).toFloat()
            val cos = cos(Math.toRadians(drawAngle)).toFloat()

            if (index % 5 == 0) {
                val hour = (index / 5).let {
                    if (it == 0) 12 else it
                }.toString()
                val numberBounds = Rect().also {
                    timeNumberPaint.getTextBounds(hour, 0, hour.length, it)
                }

                canvas.drawText(
                    hour,
                    centerX + sin * (radius * (1 - longLineLengthFraction - 0.1f)) - (numberBounds.width() / 2f),
                    centerY + cos * (radius * (1 - longLineLengthFraction - 0.1f)) + (numberBounds.height() / 2f),
                    timeNumberPaint
                )
                canvas.drawLine(
                    centerX + sin * innerLongLineFraction,
                    centerY + cos * innerLongLineFraction,
                    centerX + sin * radius,
                    centerY + cos * radius,
                    longTimeDiskPaint,
                )
            } else {
                canvas.drawLine(
                    centerX + sin * innerShortLineFraction,
                    centerY + cos * innerShortLineFraction,
                    centerX + sin * radius,
                    centerY + cos * radius,
                    shortTimeDiskPaint,
                )
            }
        }
    }

    @Suppress("SameParameterValue")
    private fun drawDigitalTime(
        y: Float,
        canvas: Canvas,
        zonedDateTime: ZonedDateTime,
        bounds: Rect,
    ) {
        val text = zonedDateTime.format(digitalTimeFormatter)

        val textBounds = Rect().also { digitalTimePaint.getTextBounds(text, 0, text.length, it) }

        canvas.drawText(
            text,
            bounds.width().div(2f) - textBounds.width().div(2f), y,
            digitalTimePaint,
        )
    }

    private fun drawOutCircle(
        canvas: Canvas,
        bounds: Rect,
        fraction: Float,
    ) {
        val padding = outCirclePaint.strokeWidth
        val rectF = RectF(
            0f + padding,
            0f + padding,
            bounds.width().toFloat() - padding,
            bounds.height().toFloat() - padding
        )
        canvas.drawArc(rectF, -90f, 360 * fraction, false, outCirclePaint)
    }

    @Suppress("SameParameterValue")
    private fun drawCircle(
        canvas: Canvas,
        bounds: Rect,
        angle: Double,
        centerFraction: Float,
        sizeFraction: Float,
    ) {
        val screenRadius: Float = (min(bounds.width(), bounds.height()) / 2f)

        val sin = -sin(Math.toRadians(angle)).toFloat()
        val cos = cos(Math.toRadians(angle)).toFloat()

        val circleSize = screenRadius * sizeFraction

        val x = centerX + sin * screenRadius.minus(circleSize).minus(screenRadius * centerFraction)
        val y = centerY + cos * screenRadius.minus(circleSize).minus(screenRadius * centerFraction)

        val rectF = RectF(
            x - circleSize,
            y - circleSize,
            x + circleSize,
            y + circleSize,
        )
        canvas.drawArc(rectF, 0f, 360f, false, functionCirclePaint)
    }

    private fun drawCircle(
        canvas: Canvas,
        bounds: Rect,
        slot: CustomComplicationSlot
    ) {
        val rectF = RectF(
            bounds.width() * slot.left,
            bounds.height() * slot.top,
            bounds.width() * slot.right,
            bounds.height() * slot.bottom,
        )
        canvas.drawArc(rectF, 0f, 360f, false, functionCirclePaint)
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
