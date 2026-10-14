package com.umdah.anticlock

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import kotlin.math.cos
import kotlin.math.sin

/**
 * Draws the static clock face (everything except the hands).
 *
 * Numbers run counter-clockwise: 12 at the top, 1 where 11 normally is, 2 where 10 is, and so on.
 * All sizes are fractions of the face radius so the face looks the same at any size.
 */
object ClockFace {
    const val FACE_INNER = 0xFF262932.toInt()
    const val FACE_OUTER = 0xFF14161B.toInt()
    const val INK = 0xFFECE6DA.toInt()      // warm cream for numbers and hands
    const val ACCENT = 0xFFFF7A59.toInt()   // coral for 12 and the seconds hand

    // Hand geometry as fractions of the radius: half-width, tail, tip. The vector drawables
    // hand_*.xml use the same numbers on a 400x400 canvas (radius 200).
    val HOUR = floatArrayOf(0.035f, 0.08f, 0.50f)
    val MINUTE = floatArrayOf(0.0225f, 0.06f, 0.86f)
    val SECOND = floatArrayOf(0.0075f, 0.18f, 0.92f)

    private const val HANDLE = "umdah_crafts"

    /** Set to true to also mirror each digit glyph (a true "looking-glass" clock). */
    private const val MIRROR_DIGITS = false

    private val numeralTypeface: Typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
    private val handleTypeface: Typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)

    fun renderBitmap(context: Context, sizePx: Int): Bitmap {
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val photo = Background.load(context)
        draw(Canvas(bmp), sizePx / 2f, sizePx / 2f, sizePx / 2f, photo)
        photo?.recycle()
        return bmp
    }

    fun draw(canvas: Canvas, cx: Float, cy: Float, r: Float, photo: Bitmap?) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        if (photo != null) {
            // Photo cropped to the circle, then darkened toward the rim so the numbers stay readable.
            val m = Matrix()
            val s = 2 * r / photo.width
            m.setScale(s, s)
            m.postTranslate(cx - r, cy - r)
            p.shader = BitmapShader(photo, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP).apply { setLocalMatrix(m) }
            canvas.drawCircle(cx, cy, r, p)
            p.shader = RadialGradient(
                cx, cy, r,
                intArrayOf(0x40000000, 0x66000000, 0xB3000000.toInt()),
                floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP
            )
            canvas.drawCircle(cx, cy, r, p)
        } else {
            // Plain face with a soft radial glow.
            p.shader = RadialGradient(cx, cy - r * 0.25f, r * 1.25f, FACE_INNER, FACE_OUTER, Shader.TileMode.CLAMP)
            canvas.drawCircle(cx, cy, r, p)
        }
        p.shader = null

        // On a photo, a soft shadow lifts the marks off busy backgrounds.
        if (photo != null) p.setShadowLayer(r * 0.025f, 0f, 0f, 0x99000000.toInt())

        // Hairline rim.
        p.style = Paint.Style.STROKE
        p.strokeWidth = r * 0.012f
        p.color = if (photo != null) 0x55FFFFFF else 0x22FFFFFF
        canvas.drawCircle(cx, cy, r * 0.965f, p)
        p.style = Paint.Style.FILL

        // Minute dots, with slightly larger dots at the hours.
        val dotR = r * 0.88f
        for (i in 0 until 60) {
            val a = Math.toRadians(i * 6.0)
            val hour = i % 5 == 0
            p.color = when {
                hour -> 0xCCECE6DA.toInt()
                photo != null -> 0x80ECE6DA.toInt()
                else -> 0x40ECE6DA
            }
            canvas.drawCircle(
                cx + dotR * sin(a).toFloat(), cy - dotR * cos(a).toFloat(),
                r * (if (hour) 0.016f else 0.008f), p
            )
        }

        // Reversed numerals: number n sits at angle -n * 30 degrees.
        p.typeface = numeralTypeface
        p.textSize = r * 0.16f
        p.textAlign = Paint.Align.CENTER
        val baselineShift = -(p.ascent() + p.descent()) / 2f
        for (n in 1..12) {
            val a = Math.toRadians(-n * 30.0)
            val x = cx + r * 0.72f * sin(a).toFloat()
            val y = cy - r * 0.72f * cos(a).toFloat()
            p.color = if (n == 12) ACCENT else INK
            canvas.save()
            if (MIRROR_DIGITS) canvas.scale(-1f, 1f, x, y)
            canvas.drawText(n.toString(), x, y + baselineShift, p)
            canvas.restore()
        }

        drawHandle(canvas, p, cx, cy + r * 0.40f, r, if (photo != null) 0xE6ECE6DA.toInt() else 0x99ECE6DA.toInt())
    }

    /** Instagram glyph followed by the handle, centered as one line at ([cx], [cy]). */
    private fun drawHandle(canvas: Canvas, p: Paint, cx: Float, cy: Float, r: Float, color: Int) {
        p.typeface = handleTypeface
        p.textSize = r * 0.062f
        p.letterSpacing = 0.04f
        p.textAlign = Paint.Align.LEFT
        p.color = color

        val icon = r * 0.075f
        val gap = r * 0.028f
        val textW = p.measureText(HANDLE)
        val left = cx - (icon + gap + textW) / 2f

        // Glyph: rounded square, lens, flash dot.
        val stroke = icon * 0.1f
        p.style = Paint.Style.STROKE
        p.strokeWidth = stroke
        val box = RectF(left, cy - icon / 2, left + icon, cy + icon / 2)
        box.inset(stroke / 2, stroke / 2)
        canvas.drawRoundRect(box, icon * 0.28f, icon * 0.28f, p)
        canvas.drawCircle(box.centerX(), box.centerY(), icon * 0.22f, p)
        p.style = Paint.Style.FILL
        canvas.drawCircle(box.right - icon * 0.22f, box.top + icon * 0.22f, icon * 0.055f, p)

        canvas.drawText(HANDLE, left + icon + gap, cy - (p.ascent() + p.descent()) / 2f, p)
        p.letterSpacing = 0f
    }
}
