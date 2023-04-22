package net.matsudamper.watchface.complication

import kotlin.math.cos
import kotlin.math.sin
sealed class CustomComplicationSlot {
    private val sizeFraction = 0.1f // 直径に対する割合
    private val centerFraction = 0.25f

    abstract val id: Int
    abstract val angle: Double

    private val sin: Double by lazy { -sin(Math.toRadians(angle)) }
    private val cos: Double by lazy { cos(Math.toRadians(angle)) }

    private val x: Double by lazy {
        sin * 0.5.minus(sizeFraction).minus(0.5 * centerFraction)
    }
    private val y: Double by lazy {
        cos * 0.5.minus(sizeFraction).minus(0.5 * centerFraction)
    }

    val left: Float by lazy { (0.5f + x - sizeFraction).toFloat() }
    val top: Float by lazy { (0.5f + y - sizeFraction).toFloat() }
    val right: Float by lazy { (0.5f + x + sizeFraction).toFloat() }
    val bottom: Float by lazy { (0.5f + y + sizeFraction).toFloat() }

    override fun toString(): String {
        return "CustomComplicationSlot(id=$id, angle=$angle, left=$left, top=$top, right=$right, bottom=$bottom, sin=$sin, cos=$cos, x=$x, y=$y)"
    }

    object Slot0 : CustomComplicationSlot() {
        override val id: Int = 101
        override val angle: Double = (360 / 6f) * 3.toDouble()
    }

    object Slot1 : CustomComplicationSlot() {
        override val id: Int = 102
        override val angle: Double = (360 / 6f) * 4.toDouble()
    }
}
