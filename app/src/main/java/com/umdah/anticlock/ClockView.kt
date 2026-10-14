package com.umdah.anticlock

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import java.time.LocalTime
import kotlin.math.min

/**
 * Full-screen reverse clock for the app itself. The seconds hand sweeps smoothly at the display's
 * refresh rate, but only while the view is on screen; it stops drawing the moment the app is hidden.
 */
class ClockView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    private var face: Bitmap? = null
    private var running = false

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        reloadFace()
    }

    /** Redraws the static face, e.g. after the background photo changed. */
    fun reloadFace() {
        face?.recycle()
        val size = min(width, height)
        face = if (size > 0) ClockFace.renderBitmap(context, size) else null
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val bmp = face ?: return
        val r = bmp.width / 2f
        val cx = width / 2f
        val cy = height / 2f
        canvas.drawBitmap(bmp, cx - r, cy - r, null)

        val t = LocalTime.now()
        val sec = t.second + t.nano / 1e9f
        val min = t.minute + sec / 60f
        val hour = (t.hour % 12) + min / 60f

        // Mirror horizontally so ordinary clockwise angles turn counter-clockwise.
        canvas.save()
        canvas.scale(-1f, 1f, cx, cy)

        paint.color = ClockFace.INK
        hand(canvas, cx, cy, r, hour * 30f, ClockFace.HOUR)
        hand(canvas, cx, cy, r, min * 6f, ClockFace.MINUTE)
        canvas.drawCircle(cx, cy, r * 0.04f, paint)

        paint.color = ClockFace.ACCENT
        hand(canvas, cx, cy, r, sec * 6f, ClockFace.SECOND)
        canvas.drawCircle(cx, cy, r * 0.035f, paint)
        paint.color = ClockFace.FACE_OUTER
        canvas.drawCircle(cx, cy, r * 0.015f, paint)

        canvas.restore()

        if (running) postInvalidateOnAnimation()
    }

    /** A rounded bar pointing to [deg] (clockwise from 12); [g] is half-width, tail, tip. */
    private fun hand(c: Canvas, cx: Float, cy: Float, r: Float, deg: Float, g: FloatArray) {
        c.save()
        c.rotate(deg, cx, cy)
        val hw = r * g[0]
        rect.set(cx - hw, cy - r * g[2], cx + hw, cy + r * g[1])
        c.drawRoundRect(rect, hw, hw, paint)
        c.restore()
    }

    override fun onVisibilityAggregated(isVisible: Boolean) {
        super.onVisibilityAggregated(isVisible)
        running = isVisible
        if (isVisible) postInvalidateOnAnimation()
    }

    override fun onDetachedFromWindow() {
        running = false
        super.onDetachedFromWindow()
    }
}
