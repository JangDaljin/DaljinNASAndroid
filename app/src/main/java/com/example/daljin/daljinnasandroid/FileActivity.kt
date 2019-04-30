package com.example.daljin.daljinnasandroid

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.NavigationView
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_file.*
import kotlinx.android.synthetic.main.rightsideheader.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class FileActivity : AppCompatActivity() {

    private var path: String = ""
    private var fileViewItemList = mutableListOf<FileViewItem>()
    private lateinit var fileViewAdapter: FileViewAdapter

    private var directoryViewItemList = mutableListOf<DirectoryViewItem>()
    private lateinit var directoryViewAdapter : DirectoryViewAdapter

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

    }

    override fun onStart() {
        super.onStart()
        invalidate()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        invalidate()
    }

    private val bottomNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navRefresh -> {
                invalidate()
            }


            R.id.navMkdir -> {

                return@OnNavigationItemSelectedListener true
            }
            R.id.navRmdir -> {

                return@OnNavigationItemSelectedListener true
            }
            R.id.navUpload -> {

                Log.d("DALJIN" , filesDir.absolutePath)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navDownload -> {

                fileViewItemList.filter{ it.isChecked }
                    .forEach {
                    DaljinNodeWebDownload(this@FileActivity , path , it.fullname , it.type) {
                        error , body ->
                        if(body is ResponseBody) {
                            var file = File(filesDir ,it.fullname)
                            if(file.exists()) {
                                file.delete()
                            }
                            file.createNewFile()

                            var ips = body.byteStream()
                            var fos = FileOutputStream(file , true)
                            var buffer = ByteArray(1024)
                            var len = 0
                            while(true) {
                                len = ips.read(buffer)
                                if(len == -1) break
                                fos.write(buffer , 0 , len)
                            }
                            fos.close()
                        }
                    }
                }

                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

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
            R.id.sideDownloadSetting -> {

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
                                            fileSizeConverter(size),
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

}













