package com.umdah.anticlock

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var clock: ClockView
    private lateinit var removeButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        clock = findViewById(R.id.clock)
        removeButton = findViewById(R.id.remove_photo)

        findViewById<View>(R.id.pick_photo).setOnClickListener { pickPhoto() }
        removeButton.setOnClickListener {
            Background.clear(this)
            onBackgroundChanged()
        }
        findViewById<View>(R.id.add_widget).setOnClickListener { addWidget() }
        updateButtons()
    }

    private fun pickPhoto() {
        val intent = if (Build.VERSION.SDK_INT >= 33) {
            Intent(MediaStore.ACTION_PICK_IMAGES)
        } else {
            Intent(Intent.ACTION_GET_CONTENT).setType("image/*")
        }
        startActivityForResult(intent, REQUEST_PHOTO)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val uri = data?.data
        if (requestCode != REQUEST_PHOTO || resultCode != RESULT_OK || uri == null) return
        Thread {
            val ok = runCatching { Background.save(this, uri) }.isSuccess
            runOnUiThread {
                if (ok) onBackgroundChanged()
                else Toast.makeText(this, R.string.photo_failed, Toast.LENGTH_SHORT).show()
            }
        }.start()
    }

    private fun onBackgroundChanged() {
        clock.reloadFace()
        updateButtons()
        Thread { AntiClockWidget.refreshAll(applicationContext) }.start()
    }

    private fun updateButtons() {
        removeButton.visibility = if (Background.exists(this)) View.VISIBLE else View.GONE
    }

    private fun addWidget() {
        val manager = AppWidgetManager.getInstance(this)
        if (manager.isRequestPinAppWidgetSupported) {
            manager.requestPinAppWidget(ComponentName(this, AntiClockWidget::class.java), null, null)
        } else {
            Toast.makeText(this, R.string.hint, Toast.LENGTH_LONG).show()
        }
    }

    private companion object {
        const val REQUEST_PHOTO = 1
    }
}
