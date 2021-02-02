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
    }
}