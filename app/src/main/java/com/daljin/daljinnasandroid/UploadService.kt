package com.daljin.daljinnasandroid

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class UploadService : Service() {

    private val mBinder = UploadServiceBinder()

    var progressCallback : ((Int)->Unit)? = null
    var uploadEndCallback : ((Boolean , String)->Unit)? = null

    inner class UploadServiceBinder : Binder() {
        fun getService() = this@UploadService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }

    fun upload(uploadPath : String , FilePaths : List<String> , cnt : Int = 0) {

        if(cnt >= FilePaths.size) {
            return
        }

        if(cnt == FilePaths.size) {
            DaljinNodeWebUpload(this@UploadService , uploadPath , FilePaths[cnt] , progressCallback , uploadEndCallback)
        }
        else {
            DaljinNodeWebUpload(this@UploadService , uploadPath , FilePaths[cnt] , progressCallback , {
                result , msg ->
                uploadEndCallback?.invoke(result , msg)
                if(result) {
                    upload(uploadPath , FilePaths  , cnt+1)
                }
            })
        }

    }
}