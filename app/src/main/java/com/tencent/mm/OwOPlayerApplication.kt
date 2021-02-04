package com.tencent.mm

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class OwOPlayerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        context = this
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

    }

    override fun onLowMemory() {
        super.onLowMemory()

    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        const val REQUEST_NOTIFICATION_CODE_PREVIOUS_PLAY = 20
        const val REQUEST_NOTIFICATION_CODE_PAUSE_PLAY = 21
        const val REQUEST_NOTIFICATION_CODE_NEXT_PLAY = 22
        const val SERVICE_NOTIFICATION_ID = 20
    }
}