package com.daljin.daljinnasandroid

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.daljin.daljinnasandroid.*
import kotlinx.android.synthetic.main.activity_file.*
import kotlinx.android.synthetic.main.rightsidebody.*
import kotlinx.android.synthetic.main.rightsideheader.*
import org.json.JSONObject

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

    private var writingMode = 1
    private lateinit var downloadServiceConnection: ServiceConnection
    private lateinit var downloadService : DownloadService
    private lateinit var downloadServiceServiceBinder : DownloadService.DownloadServiceBinder
    private var isDoingDownloadService = false

    //업로드 관련 변수
    private lateinit var uploadServiceConnection: ServiceConnection
    private lateinit var uploadService : UploadService
    private lateinit var uploadServiceServiceBinder : UploadService.UploadServiceBinder

    //알림 관련 변수
    private lateinit var notificationManager : NotificationManagerCompat
    private lateinit var downloadNotification : NotificationCompat.Builder
    private lateinit var uploadNotification : NotificationCompat.Builder
    private var isDoingUploadService = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file)

        //상단 메뉴바
        setSupportActionBar(toolbar)

        //하단메뉴바
        navBottom.setOnNavigationItemSelectedListener(bottomNavigationItemSelectedListener)

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //오른쪽 메뉴 설정
        sideLoginNOut.setOnClickListener {
            if(!DaljinNodeWebLoginData.isAuthenticated) {
                startNewActivity(REQUEST_LOGIN)
            }
            else {
                DaljinNodeWebLogout(this@FileActivity) {
                    if(it) {
                        invalidate()
                    }
                    else {
                        Toast.makeText(this@FileActivity , "로그아웃 불가" , Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        sidePathSetting.setOnCheckedChangeListener{
            group, checkedId ->
            when(checkedId) {
                R.id.sideExternalStorageStorage -> {
                    downloadPath = Environment.DIRECTORY_DOWNLOADS
                }
                R.id.sideInternalStorage -> {
                    downloadPath = filesDir.path

                }
            }
            getSharedPreferences(SP_NAME , Context.MODE_PRIVATE).edit().putString(SP_KEY_DOWNLOADPATH , downloadPath).commit()
        }

        sideSaveSetting.setOnCheckedChangeListener {
            group, checkedId ->
            when(checkedId) {
                R.id.sideOverwrite -> {
                    writingMode = SAVE_OVERWIRTE
                }
                R.id.sideIgnore -> {
                    writingMode = SAVE_IGNORE
                }
            }
            getSharedPreferences(SP_NAME , Context.MODE_PRIVATE).edit().putInt(SP_KEY_WRITEMODE , writingMode).commit()
        }
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

        //다운로드 서비스 초기화
        downloadServiceConnection = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {
                isDoingDownloadService = false
            }

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                downloadServiceServiceBinder = service as DownloadService.DownloadServiceBinder
                downloadService = downloadServiceServiceBinder.getService()
                isDoingDownloadService = true
            }
        }
        uploadServiceConnection = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {
                isDoingUploadService = false
            }

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                uploadServiceServiceBinder = service as UploadService.UploadServiceBinder
                uploadService = uploadServiceServiceBinder.getService()
                isDoingUploadService = true
            }
        }

        //다운로드, 업로드 서비스 시작
        bindService(Intent(this@FileActivity, DownloadService::class.java), downloadServiceConnection, Context.BIND_AUTO_CREATE)
        bindService(Intent(this@FileActivity , UploadService::class.java), uploadServiceConnection , Context.BIND_AUTO_CREATE)

        //알림 설정
        notificationManager = NotificationManagerCompat.from(this@FileActivity)
        downloadNotification = NotificationCompat.Builder(this@FileActivity , NC_DOWNLOAD)
        uploadNotification = NotificationCompat.Builder(this@FileActivity , NC_UPLOAD)
    }

    override fun onStart() {
        super.onStart()

        val sharePreference = getSharedPreferences(SP_NAME , Context.MODE_PRIVATE)

        writingMode = sharePreference.getInt(SP_KEY_WRITEMODE, SAVE_OVERWIRTE)
        when(writingMode) {
            SAVE_OVERWIRTE -> sideOverwrite.isChecked = true
            SAVE_IGNORE -> sideIgnore.isChecked = true
        }

        downloadPath = sharePreference.getString(SP_KEY_DOWNLOADPATH , filesDir.path)
        when(downloadPath) {
            filesDir.path -> sideInternalStorage.isChecked = true
            Environment.DIRECTORY_DOWNLOADS -> sideExternalStorageStorage.isChecked = true
        }

        //외부저장소 사용가능 확인
        if(checkExternalStorageAvailable()) {
            sideExternalStorageStorage.visibility = View.VISIBLE
        }
        else {
            sideInternalStorage.isChecked = true
            sideExternalStorageStorage.visibility = View.GONE
        }

        invalidate()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.optionmenu , menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.optionAllCheck -> {
                fileViewAdapter.changeAllCheck(true)
            }
            R.id.optionAllNotCheck -> {
                fileViewAdapter.changeAllCheck(false)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            REQUEST_LOGIN-> {
                when(resultCode) {
                    RESULT_LOGIN -> {

                    }
                    RESULT_FINISH -> {
                        finish()
                    }
                }
            }
            REQUEST_UPLOAD-> {

            }
        }

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
                    .setPositiveButton("만들기") { dialog, which ->
                        if (et.text.toString().isNullOrBlank()) {
                            Toast.makeText(this@FileActivity , "파일명을 입력해주세요" , Toast.LENGTH_SHORT).show()
                        } else {
                            DaljinNodeWebMkdir(this@FileActivity, path, et.text.toString()) {
                                if (it) {
                                    Toast.makeText(this@FileActivity, "파일 생성 완료", Toast.LENGTH_SHORT).show()
                                    invalidate()
                                } else {
                                    Toast.makeText(this@FileActivity, "파일 생성 실패", Toast.LENGTH_SHORT).show()
                                }
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

                if(checkedList.isEmpty()) {
                    return@OnNavigationItemSelectedListener  false
                }

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
                                invalidate()
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
                startNewActivity(REQUEST_UPLOAD)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navDownload -> {
                downloadPath = filesDir.path
                val checkedFileList = fileViewItemList.filter{it.isChecked}
                val fileList = List(checkedFileList.size) { index -> Pair("$path/${checkedFileList[index].fullname}" , checkedFileList[index].type) }


                var totalSize = 0L
                var curSize = 0L
                var percentage = 0
                checkedFileList.forEach{ totalSize += it.size }

                if(totalSize == 0L) {
                    Toast.makeText(this@FileActivity , "사이즈가 0입니다" , Toast.LENGTH_SHORT).show()
                    return@OnNavigationItemSelectedListener true
                }

                navBottom.menu.findItem(R.id.navDownload).isCheckable = false
                navBottom.menu.findItem(R.id.navUpload).isCheckable = false

                downloadService.progressCallback = {
                    curSize += it
                    val curProgress = (100L * curSize / totalSize).toInt()
                    if(percentage < curProgress)
                    {
                        downloadNotification.setProgress(100 , curProgress , false)
                            .setContentText("${fileSizeConverter(curSize)} / ${fileSizeConverter(totalSize)}")
                        notificationManager.notify(N_DOWNLOAD_ID , downloadNotification.build())
                        percentage = curProgress
                    }
                }

                downloadService.overWriteCallback = {
                    when(writingMode) {
                        SAVE_IGNORE -> false
                        SAVE_OVERWIRTE-> true
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
                    notificationManager.notify(N_DOWNLOAD_ID , downloadNotification.build())

                    navBottom.menu.findItem(R.id.navDownload).isCheckable = true
                    navBottom.menu.findItem(R.id.navUpload).isCheckable = true
                }

                //다운로드 시작
                Toast.makeText(this@FileActivity , "${fileSizeConverter(totalSize)} 다운로드를 시작합니다." , Toast.LENGTH_SHORT).show()

                downloadNotification
                    .setSmallIcon(R.drawable.downloadicon)
                    .setContentIntent(PendingIntent.getActivity(this@FileActivity , 200 , Intent(this@FileActivity , FileActivity::class.java) , PendingIntent.FLAG_UPDATE_CURRENT))
                    .setContentText("대기중")
                    .setContentTitle("DaljinNAS")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setProgress(100 , 0 , false)

                notificationManager.notify(N_DOWNLOAD_ID , downloadNotification.build())
                downloadService.startDownload(fileList , downloadPath)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private fun startNewActivity(requestCode: Int) {
        var intent : Intent?  = null
        when(requestCode) {
            REQUEST_LOGIN -> intent = Intent(this@FileActivity , LoginActivity::class.java)
            REQUEST_UPLOAD -> intent = Intent(this@FileActivity , UploadActivity::class.java)
        }
        if(intent != null) {
            startActivityForResult(intent, requestCode)
        }
    }

    private fun invalidate() {
        DaljinNodeWebLogin(this@FileActivity , "" , "") { loginRes, loginBody ->
            //서버와 통신 OK
            if (loginRes) {
                //로그인 NO
                if(loginBody == null) {
                    loginNOutMenuToogle(false)
                    startNewActivity(REQUEST_LOGIN)
                }
                //로그인 OK
                else {
                    //로그인 정보 갱신
                    loginNOutMenuToogle(true)


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
                loginNOutMenuToogle(false)
                Toast.makeText(this@FileActivity, "서버와 통신 불가", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun progressbarInvalidate(usedStorage : Long) {
        val percentage = 100 * usedStorage / DaljinNodeWebLoginData.maxStorage
        tvStorage.text = "$percentage%"
        pgbStorage.progress = percentage.toInt()
    }

    private fun loginNOutMenuToogle(login : Boolean) {
        if(login) {
            sideHeaderID.apply {
                text = DaljinNodeWebLoginData.id
                visibility = View.VISIBLE
            }
            sideHeaderGrade.apply {
                text = DaljinNodeWebLoginData.grade
                visibility = View.VISIBLE
            }
            sideHeaderMaxStorage.apply {
                text = fileSizeConverter(DaljinNodeWebLoginData.maxStorage)
                visibility = View.VISIBLE
            }
            sideHeaderLogoutText.visibility = View.INVISIBLE
            sideLoginNOut.text = "로그아웃"
        }
        else{
            sideHeaderID.apply {
                text = ""
                visibility = View.INVISIBLE
            }
            sideHeaderGrade.apply {
                text = ""
                visibility = View.INVISIBLE
            }
            sideHeaderMaxStorage.apply {
                text = "0B"
                visibility = View.INVISIBLE
            }
            sideHeaderLogoutText.visibility = View.VISIBLE
            sideLoginNOut.text = "로그인"
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
















