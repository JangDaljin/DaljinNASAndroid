package com.daljin.daljinnasandroid

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream

class DownloadService : Service(){

    private var mBinder = DownloadServiceBinder()

    var progressCallback : ((downloadByte : Int)->Unit)? = null
    var downloadEndCallback : (() -> Unit)? = null
    var overWriteCallback : ((name : String)->Boolean)? = null
    var errorCallback : (()->Unit)? = null

    inner class DownloadServiceBinder : Binder() {

        fun getService() = this@DownloadService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    fun startDownload(fileList : List<Pair<String , String>>, downloadPath : String, cnt : Int = 0) {

        if(cnt > fileList.size -1) {
            downloadEndCallback?.invoke()
            return
        }

        val serverPath = fileList[cnt].first.substringBeforeLast('/')
        val filename = fileList[cnt].first.substringAfterLast('/')
        val type = fileList[cnt].second

        DaljinNodeWebDownload(this@DownloadService , serverPath , filename , type) {
                received , body ->
            if(received) {
                if(body is ResponseBody) {
                    //파일이름설정
                    val addZip : String = if(type == "directory") { ".zip" } else { "" }
                    val downloadFileName = "$filename$addZip"

                    //파일생성
                    var file = File(downloadPath , downloadFileName)

                    //파일 존재시
                    if(file.exists()) {
                        when(overWriteCallback?.invoke(downloadFileName)) {
                            //띄어넘기
                            false -> { startDownload(fileList , downloadPath , cnt +1); return@DaljinNodeWebDownload }
                            //덮어쓰기
                            else -> file.delete()
                        }
                    }

                    //파일 다운로드 시작
                    file.createNewFile()
                    var inputStream = body.byteStream()
                    var fileOutputStream = FileOutputStream(file , true)
                    var buffer = ByteArray(1024)
                    var len = 0
                    while(true) {
                        len = inputStream.read(buffer)
                        if(len == -1) break
                        fileOutputStream.write(buffer , 0 , len)
                        progressCallback?.invoke(len)
                    }
                    fileOutputStream.close()
                    startDownload(fileList , downloadPath , cnt +1)
                }
            }
            else {
                errorCallback?.invoke()
            }


        }
    }





}



