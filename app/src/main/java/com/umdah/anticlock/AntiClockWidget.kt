package com.umdah.anticlock

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.RemoteViews

/**
 * Home-screen widget. After the widget is placed, the app does nothing: no service, no alarm and
 * no periodic update. The launcher's built-in AnalogClock moves the hands. The layout mirrors it
 * (scaleX = -1) so it runs counter-clockwise. The face is drawn once and passed in as a bitmap,
 * and redrawn only when the background photo changes.
 */
open class AntiClockWidget : AppWidgetProvider() {

    protected open val layoutId: Int = R.layout.widget_clock

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        push(context, manager, ids, layoutId, ClockFace.renderBitmap(context, FACE_PX))
    }

    companion object {
        private const val FACE_PX = 640

        /** Redraws every placed widget, e.g. after the background photo changed. */
        fun refreshAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val face by lazy { ClockFace.renderBitmap(context, FACE_PX) }
            for ((cls, layout) in listOf(
                AntiClockWidget::class.java to R.layout.widget_clock,
                AntiClockWidgetMinimal::class.java to R.layout.widget_clock_minimal,
            )) {
                val ids = manager.getAppWidgetIds(ComponentName(context, cls))
                if (ids.isNotEmpty()) push(context, manager, ids, layout, face)
            }
        }

        private fun push(context: Context, manager: AppWidgetManager, ids: IntArray, layout: Int, face: Bitmap) {
            val views = RemoteViews(context.packageName, layout)
            views.setImageViewBitmap(R.id.dial, face)
            views.setOnClickPendingIntent(
                R.id.root,
                PendingIntent.getActivity(
                    context, 0,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            manager.updateAppWidget(ids, views)
        }
    }
}

/** Same widget without the seconds hand. */
class AntiClockWidgetMinimal : AntiClockWidget() {
    override val layoutId: Int = R.layout.widget_clock_minimal
}
