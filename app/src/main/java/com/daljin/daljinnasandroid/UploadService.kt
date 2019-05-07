package com.daljin.daljinnasandroid

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import java.util.concurrent.LinkedBlockingDeque

data class UploadInfo(val uploadPath : String , val filePath : String)

class UploadService : Service() {

    private val mBinder = UploadServiceBinder()

    var progressCallback : ((Long , Long)->Unit)? = null
    var uploadEndCallback : ((Boolean , String)->Unit)? = null
    var errorCallback : ((ArrayList<String> , String) -> Unit)? = null


    private val queue = LinkedBlockingDeque<UploadInfo>()
    private var isUploading = false
    private var totalSize = 0L
    private var uploadSize = 0L

    inner class UploadServiceBinder : Binder() {
        fun getService() = this@UploadService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }

    fun startUpload(uploadInfoList : List<UploadInfo> , totalSize : Long? = 0L) {
        if(uploadInfoList.isEmpty() || totalSize == 0L || totalSize == null) {
            return
        }

        queue.addAll(uploadInfoList)
        this.totalSize += totalSize

        if(!isUploading) {
            nextUpload()
        }
    }

    private fun nextUpload() {
        isUploading = true
        if(queue.isEmpty()) {
            isUploading = false
            totalSize = 0L
            uploadSize = 0L
            return
        }

        val uploadInfo = queue.pop()

        DaljinNodeWebUpload(this@UploadService , uploadInfo.uploadPath , uploadInfo.filePath ,
            {
                uploadSize += it
                progressCallback?.invoke(uploadSize , totalSize)
            } ,

            {
                result , msg ->
                if(result) {
                    if(queue.isEmpty()) {
                        uploadEndCallback?.invoke(result , msg)
                    }
                }
                else {
                    val arrayList = ArrayList<String>()
                    arrayList.add(uploadInfo.filePath)
                    while(queue.isNotEmpty()) {
                        arrayList.add(queue.pop().filePath)
                    }
                    errorCallback?.invoke(arrayList , msg)
                }
                nextUpload()
            }
        )
    }
}