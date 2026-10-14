package com.umdah.anticlock

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import java.io.File
import kotlin.math.max
import kotlin.math.min

/** The optional background photo, stored as a small square JPEG in the app's private storage. */
object Background {
    private const val SIZE = 720

    private fun file(context: Context) = File(context.filesDir, "background.jpg")

    fun exists(context: Context) = file(context).exists()

    fun load(context: Context): Bitmap? {
        val f = file(context)
        return if (f.exists()) BitmapFactory.decodeFile(f.path) else null
    }

    fun clear(context: Context) {
        file(context).delete()
    }

    /** Decodes the picked image (respecting rotation), center-crops it to a square and saves it. */
    fun save(context: Context, uri: Uri) {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        val decoded = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            // Decode at a reduced size: no need to hold a 50 MP photo in memory.
            val shortSide = min(info.size.width, info.size.height)
            decoder.setTargetSampleSize(max(1, shortSide / (SIZE * 2)))
        }
        val side = min(decoded.width, decoded.height)
        val square = Bitmap.createBitmap(decoded, (decoded.width - side) / 2, (decoded.height - side) / 2, side, side)
        val scaled = Bitmap.createScaledBitmap(square, SIZE, SIZE, true)
        file(context).outputStream().use { scaled.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        decoded.recycle()
        if (square !== decoded) square.recycle()
        if (scaled !== square) scaled.recycle()
    }
}
