package net.matsudamper.watchface

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import android.view.SurfaceHolder
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
    interactiveDrawModeUpdateDelayMillis = 8L,
    clearWithBackgroundTintBeforeRenderingHighlightLayer = false,
) {
    private val outCirclePadding = 4f

    private val longTimeDiskPaint = Paint().also {
        it.color = Color.WHITE
        it.strokeWidth = 2f
    }
    private val shortTimeDiskPaint = Paint().also {
        it.color = Color.DKGRAY
        it.strokeWidth = 2f
    }
    private val digitalTimePaint = Paint().also {
        it.color = Color.WHITE
        it.textSize = 36f
    }
    private val outCirclePaint = Paint().also { paint ->
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = outCirclePadding
        paint.color = Color.MAGENTA
    }
    private val timeNumberPaint = Paint().also {
        it.color = Color.WHITE
        it.textSize = 24f
    }
    private val functionCirclePaint = Paint().also {
        it.style = Paint.Style.STROKE
        it.color = Color.LTGRAY
        it.strokeWidth = 2f
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
    }

    override fun render(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        sharedAssets: SharedAssets
    ) {
        Log.d("LOG", "render: ${renderParameters.watchFaceLayers.toList()}")
        val backgroundColor = if (renderParameters.drawMode == DrawMode.AMBIENT) {
            Color.DKGRAY
        } else {
            Color.GRAY
        }

        if (renderParameters.watchFaceLayers.contains(WatchFaceLayer.COMPLICATIONS_OVERLAY)) {
            drawDigitalTime(
                y = 170f,
                canvas = canvas,
                zonedDateTime = zonedDateTime,
                bounds = bounds,
            )
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
            (1..6).forEach { index ->
                val betWeenAngle: Double = (360 / 6f).toDouble()
                drawCircle(
                    canvas = canvas,
                    bounds = bounds,
                    angle = betWeenAngle * index,
                    centerFraction = 0.25f,
                    sizeFraction = 0.2f,
                )
            }
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
        val text = zonedDateTime.format(
            DateTimeFormatterBuilder()
                .appendValue(ChronoField.HOUR_OF_DAY, 2)
                .appendLiteral(":")
                .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                .toFormatter()
        )

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

        val x = centerX + sin * (screenRadius * 1f).minus(circleSize).minus(screenRadius * centerFraction)
        val y = centerY + cos * (screenRadius * 1f).minus(circleSize).minus(screenRadius * centerFraction)

        canvas.drawCircle(
            x,
            y,
            circleSize,
            functionCirclePaint
        )
        val rectF = RectF(
            x - circleSize,
            y - circleSize,
            x + circleSize,
            y + circleSize,
        )
        canvas.drawArc(rectF, 0f, 360f, false, functionCirclePaint)
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
