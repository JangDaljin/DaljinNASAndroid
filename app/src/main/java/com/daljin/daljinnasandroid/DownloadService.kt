package com.daljin.daljinnasandroid

import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.IBinder
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.LinkedBlockingDeque
import java.util.jar.Manifest


data class DownloadInfo(val serverPath : String , val fileName : String , val fileType : String , val fileSize : Long , val downloadPath : String)

class DownloadService : Service(){

    private var mBinder = DownloadServiceBinder()

    var progressCallback : ((downloadSize : Long , totalSize : Long)->Unit)? = null
    var downloadEndCallback : (() -> Unit)? = null
    var overWriteCallback : ((name : String)->Boolean)? = null
    var errorCallback : (()->Unit)? = null


    private var isDownloading = false
    private val queue = LinkedBlockingDeque<DownloadInfo>()

    private var totalSize = 0L
    private var downloadSize = 0L

    inner class DownloadServiceBinder : Binder() {

        fun getService() = this@DownloadService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }

    fun startDownload(downloadInfoList : List<DownloadInfo>) {
        queue.addAll(downloadInfoList)
        downloadInfoList.forEach{ totalSize += it.fileSize}
        if(!isDownloading) {
            downloadNextFile()
        }

    }

    private fun downloadNextFile() {
        isDownloading = true

        if(queue.size == 0) {
            isDownloading = false
            totalSize = 0L
            downloadSize = 0L
            downloadEndCallback?.invoke()
            return
        }
        val downloadInfo = queue.pop()

        DaljinNodeWebDownload(this@DownloadService, downloadInfo.serverPath , downloadInfo.fileName, downloadInfo.fileType) { received, body ->
            if (received) {
                if (body is ResponseBody) {
                    //폴더 다운로드 경우 .zip 추가
                    val addZip: String = if (downloadInfo.fileType == "directory") {
                        ".zip"
                    } else {
                        ""
                    }
                    val downloadFileName = "${downloadInfo.fileName}$addZip"

                    //다운로드 경로에 폴더 없으면 만들기
                    var pathList = downloadInfo.downloadPath.split('/')
                    var tempPath = ""
                    for (i in 1 until pathList.size) {
                        if (!pathList[i].isNullOrEmpty()) {
                            tempPath = "$tempPath/${pathList[i]}"
                        }
                        val tempFile = File(tempPath)
                        if (!tempFile.exists()) {

                            if(!tempFile.mkdir()) {
                                Toast.makeText(this@DownloadService , "${tempFile.path} mkdir error" , Toast.LENGTH_SHORT).show()
                                return@DaljinNodeWebDownload
                            }
                        }
                    }

                    //파일생성
                    var file = File(downloadInfo.downloadPath, downloadFileName)

                    //파일 존재시
                    if (file.exists()) {
                        when (overWriteCallback?.invoke(downloadFileName)) {
                            //띄어넘기
                            false ->  {
                                downloadNextFile()
                                return@DaljinNodeWebDownload
                            }
                            //덮어쓰기
                            else -> file.delete()
                        }
                    }
                    //파일 다운로드 시작
                    file.createNewFile()
                    var inputStream = body.byteStream()
                    var fileOutputStream = FileOutputStream(file, true)
                    var buffer = ByteArray(8192)
                    var len : Int
                    while (true) {
                        len = inputStream.read(buffer)
                        if (len == -1) break
                        fileOutputStream.write(buffer, 0, len)
                        downloadSize += len
                        progressCallback?.invoke(downloadSize , totalSize)
                    }
                    fileOutputStream.close()
                    downloadNextFile()
                }
            }
            else {
                errorCallback?.invoke()
            }
        }
    }


}



