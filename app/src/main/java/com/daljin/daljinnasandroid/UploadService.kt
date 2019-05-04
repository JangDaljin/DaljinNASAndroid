package com.daljin.daljinnasandroid

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class UploadService : Service() {

    private val mBinder = UploadServiceBinder()

    inner class UploadServiceBinder : Binder() {
        fun getService() = this@UploadService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }

    fun upload() {

    }

}