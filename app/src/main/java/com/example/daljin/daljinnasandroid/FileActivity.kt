package com.example.daljin.daljinnasandroid

import android.content.*
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.NavigationView
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_file.*
import kotlinx.android.synthetic.main.rightsideheader.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FileActivity : AppCompatActivity() {

    //탐색기 현재 위치
    private var path: String = ""

    //파일뷰 관련 변수
    private var fileViewItemList = mutableListOf<FileViewItem>()
    private lateinit var fileViewAdapter: FileViewAdapter

    //디렉토리뷰 관련 변수
    private var directoryViewItemList = mutableListOf<DirectoryViewItem>()
    private lateinit var directoryViewAdapter : DirectoryViewAdapter

    //다운로드 관련 변수
    private var downloadPath = ""
    private val OVERWIRTE = 1
    private val IGNORE = 2
    private var writingMode = 1
    private lateinit var downloadServiceConnection: ServiceConnection
    private lateinit var downloadService : DownloadService
    private lateinit var downloadServiceServiceBinder : DownloadService.DownloadServiceBinder
    private var isService = false

    private lateinit var notificationManager : NotificationManagerCompat
    private lateinit var downloadNotification : NotificationCompat.Builder


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file)


        //파일뷰 설정
        fileView.layoutManager = LinearLayoutManager(this@FileActivity)
        fileViewAdapter = FileViewAdapter(this@FileActivity , fileViewItemList) {

        }
        fileView.adapter = fileViewAdapter

        //디렉터리뷰 설정
        directoryView.layoutManager = LinearLayoutManager(this@FileActivity)
        directoryViewAdapter = DirectoryViewAdapter(this@FileActivity , directoryViewItemList) {
            when(it) {
                ".." -> {
                    path = path.substringBeforeLast("/")
                }
                else -> {
                    path = "$path/$it"
                }
            }
            invalidate()
        }
        directoryView.adapter = directoryViewAdapter

        navBottom.setOnNavigationItemSelectedListener(bottomNavigationItemSelectedListener)
        rightSideView.setNavigationItemSelectedListener(sideNavigationViewItemSelectedList)

        chbAll.setOnCheckedChangeListener { buttonView, isChecked ->
            fileViewAdapter.toggleAll(isChecked)
        }


        //다운로드 서비스 초기화
        downloadServiceConnection = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {
                isService = false
            }

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                downloadServiceServiceBinder = service as DownloadService.DownloadServiceBinder
                downloadService = downloadServiceServiceBinder.getService()
                isService = true
            }
        }

        //다운로드 서비스 시작
        bindService(
            Intent(this@FileActivity, DownloadService::class.java),
            downloadServiceConnection,
            Context.BIND_AUTO_CREATE
        )


        //알림 매니저 설정
        notificationManager = NotificationManagerCompat.from(this@FileActivity)

        //다운로드 알림 설정
        downloadNotification = NotificationCompat.Builder(this@FileActivity , "Daljin")
            .setSmallIcon(R.drawable.downloadicon)
            .setContentTitle("DaljinNAS")
            .setContentText("대기중")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(100 , 0 , false)

    }

    override fun onStart() {
        super.onStart()

        //우측 메뉴 설정
        if(!checkExternalStorageAvailable()) {
            rightSideView.menu.findItem(R.id.sideExternalStorage).isVisible = true
        }

        invalidate()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        invalidate()
    }

    //하단 메뉴
    private val bottomNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navRefresh -> {
                invalidate()
            }

            R.id.navMkdir -> {

                val et = EditText(this@FileActivity)
                AlertDialog.Builder(this@FileActivity)
                    .setTitle("폴더생성")
                    .setView(et)
                    .setPositiveButton("만들기") {
                        dialog , which ->
                        DaljinNodeWebMkdir(this@FileActivity , path , et.text.toString()) {
                            if(it) {
                                Toast.makeText(this@FileActivity , "파일 삭제 완료" , Toast.LENGTH_SHORT).show()
                            }
                            else {
                                Toast.makeText(this@FileActivity , "파일 삭제 실패" , Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .setNegativeButton("취소") {
                        dialog , which ->
                        dialog.cancel()
                    }
                    .show()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navRmdir -> {
                var checkedList = fileViewItemList.filter{it.isChecked}
                var removeList = List(checkedList.size) {
                    index ->
                    Pair(checkedList[index].type , checkedList[index].fullname)
                }

                AlertDialog.Builder(this@FileActivity)
                    .setTitle("삭제")
                    .setMessage("정말 삭제하시겠습니까?")
                    .setPositiveButton("네") {
                            dialog , which ->
                        DaljinNodeWebRemove(this@FileActivity , path , removeList) {
                            if(it) {
                                Toast.makeText(this@FileActivity , "파일 삭제 완료" , Toast.LENGTH_SHORT).show()
                            }
                            else {
                                Toast.makeText(this@FileActivity , "파일 삭제 실패" , Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .setNegativeButton("아니오") {
                            dialog , which ->
                        dialog.cancel()
                    }
                    .show()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navUpload -> {
                navBottom.menu.findItem(R.id.navDownload).isCheckable = false
                navBottom.menu.findItem(R.id.navUpload).isCheckable = false





                navBottom.menu.findItem(R.id.navDownload).isCheckable = true
                navBottom.menu.findItem(R.id.navUpload).isCheckable = true
                return@OnNavigationItemSelectedListener true
            }
            R.id.navDownload -> {
                navBottom.menu.findItem(R.id.navDownload).isCheckable = false
                navBottom.menu.findItem(R.id.navUpload).isCheckable = false
                downloadPath = filesDir.path
                val checkedFileList = fileViewItemList.filter{it.isChecked}
                val fileList = List(checkedFileList.size) { index -> Pair("$path/${checkedFileList[index].fullname}" , checkedFileList[index].type) }


                var totalSize = 0L
                var curSize = 0L
                var percentage = 0
                checkedFileList.forEach{ totalSize += it.size }


                downloadService.progressCallback = {
                    curSize += it
                    val curProgress = (100L * curSize / totalSize).toInt()
                    if(percentage < curProgress)
                    {
                        downloadNotification.setProgress(100 , curProgress , false)
                            .setContentText("${fileSizeConverter(curSize)} / ${fileSizeConverter(totalSize)}")
                        notificationManager.notify(2 , downloadNotification.build())
                        percentage = curProgress
                    }
                }

                downloadService.overWriteCallback = {
                    when(writingMode) {
                        IGNORE -> false
                        OVERWIRTE-> true
                        else -> true
                    }
                }

                downloadService.errorCallback = {
                    Toast.makeText(this@FileActivity , "다운로드 에러" , Toast.LENGTH_SHORT).show()
                }

                downloadService.downloadEndCallback = {
                    Toast.makeText(this@FileActivity , "다운로드 완료" , Toast.LENGTH_SHORT).show()
                    Thread.sleep(1000) // 푸시 알림 싱크를 위해 1초 후에 종료 알림
                    downloadNotification.setContentText("다운로드 완료")
                        .setProgress(0,0,false)
                        .setOngoing(false)
                    notificationManager.notify(2 , downloadNotification.build())

                    navBottom.menu.findItem(R.id.navDownload).isCheckable = true
                    navBottom.menu.findItem(R.id.navUpload).isCheckable = true
                }

                notificationManager.notify(2 , downloadNotification.build())
                downloadService.startDownload(fileList , downloadPath)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    //우측 메뉴
    private val sideNavigationViewItemSelectedList = NavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.sideLogin -> {
                if(!DaljinNodeWebLoginData.isAuthenticated) {
                    startLoginActivity()
                }
                else {
                    DRetrofit(this@FileActivity).logout().enqueue(object : Callback<String> {
                        override fun onFailure(call: Call<String>, t: Throwable) {
                            Toast.makeText(this@FileActivity, "서버 연결 불가", Toast.LENGTH_SHORT).show()
                        }

                        override fun onResponse(call: Call<String>, response: Response<String>) {
                            if (response.isSuccessful) {
                                var parser = JSONObject(response.body())
                                when (parser.getBoolean("error")) {
                                    true -> Toast.makeText(this@FileActivity, "로그아웃 불가", Toast.LENGTH_SHORT).show()
                                    false -> startLoginActivity()
                                }
                            }
                        }
                    })
                }
                true
            }
            R.id.sideExternalStorage -> {
                downloadPath = Environment.DIRECTORY_DOWNLOADS
                true
            }
            R.id.sideInternalStorage -> {
                downloadPath = filesDir.path
                true
            }
            R.id.sideOverwrite -> {
                writingMode = OVERWIRTE
                true
            }
            R.id.sideIgnore -> {
                writingMode = IGNORE
                true
            }
            else -> {
                false
            }
        }
    }


    private fun startLoginActivity() {
        startActivityForResult(Intent(this@FileActivity, LoginActivity::class.java), 100)
    }

    private fun invalidate() {
        DaljinNodeWebLogin(this@FileActivity , "" , "") { loginRes, loginBody ->
            //서버와 통신 OK
            if (loginRes) {
                //로그인 NO
                if(loginBody == null) {
                    LoginNOutMenuToogle(false)
                    startLoginActivity()
                }
                //로그인 OK
                else {
                    //로그인 정보 갱신
                    sideHeaderID.text = DaljinNodeWebLoginData.id
                    sideHeaderGrade.text = DaljinNodeWebLoginData.grade
                    sideHeaderMaxStorage.text = fileSizeConverter(DaljinNodeWebLoginData.maxStorage)
                    LoginNOutMenuToogle(true)


                    //파일 요청
                    DaljinNodeWebGetFileList(this@FileActivity, path) { res, body ->
                        //파일요청 결과가 있음
                        if (res) {
                            val parser = JSONObject(body)
                            when (parser.getBoolean("error")) {
                                true -> {
                                    Toast.makeText(this@FileActivity, "파일가져오기 오류", Toast.LENGTH_SHORT).show()
                                }
                                false -> {
                                    //프로그래스바 초기화
                                    progressbarInvalidate(parser.getLong("used_storage"))

                                    val fileAndDirData = parser.getJSONObject("files")

                                    val fileViewList = mutableListOf<FileViewItem>()
                                    val directoryViewList = mutableListOf<DirectoryViewItem>()
                                    if(path != "") {
                                        directoryViewList.add(DirectoryViewItem(".."))
                                    }
                                    //파일 파싱 후 표시
                                    for (i in 0 until fileAndDirData.length()) {
                                        var data= fileAndDirData.getJSONObject("$i")

                                        val size         = data.getLong("size")
                                        val ctime       = data.getString("ctime")
                                        val type        = data.getString("type")
                                        val name        = data.getString("name")
                                        val extension   = data.getString("extension")
                                        val fullname    = data.getString("fullname")


                                        val fileViewItem = FileViewItem(
                                            size,
                                            ctime,
                                            type,
                                            name,
                                            extension,
                                            fullname
                                        )
                                        fileViewList.add(fileViewItem)

                                        if(type == "directory") {
                                            val directoryViewItem = DirectoryViewItem(
                                                fullname
                                            )
                                            directoryViewList.add(directoryViewItem)
                                        }
                                    }

                                    fileViewItemList.clear()
                                    directoryViewItemList.clear()

                                    fileViewItemList.addAll(fileViewList)
                                    directoryViewItemList.addAll(directoryViewList)


                                    //리사이클러뷰 초기화
                                    fileViewAdapter.notifyDataSetChanged()
                                    directoryViewAdapter.notifyDataSetChanged()
                                }
                            }
                        }
                        //파일요청 결과 없음
                        else {

                        }
                    }
                }

            }
            else {
                LoginNOutMenuToogle(false)
                Toast.makeText(this@FileActivity, "서버와 통신 불가", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun progressbarInvalidate(usedStorage : Long) {
        val percentage = 100 * usedStorage / DaljinNodeWebLoginData.maxStorage
        tvStorage.text = "$percentage%"
        pgbStorage.progress = percentage.toInt()
    }

    private fun LoginNOutMenuToogle(login : Boolean) {
        if(login) {
            rightSideView.menu.findItem(R.id.sideLogin).title = "로그아웃"
        }
        else{
            rightSideView.menu.findItem(R.id.sideLogin).title = "로그인"
        }
    }

    private fun checkExternalStorageAvailable() : Boolean{
        when(Environment.getExternalStorageState()) {
            Environment.MEDIA_MOUNTED_READ_ONLY -> return false
            Environment.MEDIA_MOUNTED -> return true
            else -> return false
        }
    }

}
















