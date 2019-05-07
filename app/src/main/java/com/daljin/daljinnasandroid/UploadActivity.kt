package com.daljin.daljinnasandroid

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_upload.*
import java.io.File

class UploadActivity : AppCompatActivity() {

    private var uploadFileList = mutableListOf<File>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        //메뉴바 설정
        setSupportActionBar(uploadToolbar)


        intent.getStringArrayListExtra(REUPLOADLIST)?.let {
            it.forEach{
                uploadPath ->
                uploadFileList.add(File(uploadPath))
            }
        }
        intent.getStringExtra(REUPLOADERRORMSG)?.let {
                Toast.makeText(this@UploadActivity , it , Toast.LENGTH_LONG).show()
        }

        //리사이클러뷰 설정
        val uploadRecyclerViewLinearLayoutManager = LinearLayoutManager(this@UploadActivity)
        uploadRecyclerView.layoutManager = uploadRecyclerViewLinearLayoutManager
        uploadRecyclerView.adapter = UploadViewAdapter(uploadFileList) {
            uploadFileList.removeAt(it)
            uploadRecyclerView.adapter?.notifyItemRemoved(it)
        }
        uploadRecyclerView.addItemDecoration(RecyclerViewSpace(30 , 30 , 30 , 30))
        uploadRecyclerView.addItemDecoration(DividerItemDecoration(this@UploadActivity , uploadRecyclerViewLinearLayoutManager.orientation))
        uploadRecyclerView.adapter?.notifyDataSetChanged()

        //업로드 버튼 클릭
        uploadButton.setOnClickListener {
            val returnIntent = Intent()
            val stringUploadFilePaths = ArrayList<String>()
            var totalSize = 0L
            uploadFileList.forEach{
                stringUploadFilePaths.add(it.path)
                totalSize += it.length()
            }
            returnIntent.putStringArrayListExtra(EXTRA_UPLOAD_FILES , stringUploadFilePaths)
            returnIntent.putExtra(EXTRA_UPLOAD_TOTALSIZE , totalSize)
            setResult(RESULT_UPLOAD , returnIntent)
            finish()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode) {
            REQUEST_FILESELECT -> {
                when(resultCode) {
                    RESULT_OK -> {
                        data?.data?.let {
                            val filepath : String? = DutilJava.getPath(this@UploadActivity , it)
                            if(filepath.isNullOrEmpty()) {
                                Toast.makeText(this@UploadActivity , "업로드 할 수 없는 파일입니다." , Toast.LENGTH_SHORT).show()
                            }
                            else {
                                if(uploadFileList.count{ file -> file.path == filepath} == 0) {
                                    uploadFileList.add(File(filepath))
                                }
                                else {
                                    Toast.makeText(this@UploadActivity , "이미 선택된 파일입니다." , Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        data?.clipData?.let {
                            var errorCnt = 0
                            var alreadyAddedCnt = 0
                            for(i in 0 until it.itemCount) {
                                val filepath : String? = DutilJava.getPath(this@UploadActivity , it.getItemAt(i).uri)
                                if(!filepath.isNullOrEmpty()) {
                                    if(uploadFileList.count{ file -> file.path == filepath} == 0) {
                                        uploadFileList.add(File(filepath))
                                    }
                                    else {
                                        alreadyAddedCnt++
                                    }
                                }
                                else {
                                    errorCnt++
                                }
                            }
                            if(errorCnt != 0 || alreadyAddedCnt != 0)
                                Toast.makeText(this@UploadActivity , "이미 추가된 파일 : ${alreadyAddedCnt}개\n접근 불가능한 파일 : ${errorCnt}개 " , Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            }
        }
    }

    override fun onResume() {
        super.onResume()
        invalidate()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.uploadmenu , menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.menuSelectFile ->{
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                intent.type = "*/*"
                intent.addCategory(Intent.CATEGORY_OPENABLE)

                // 삼성전용
                val sIntent = Intent("com.sec.android.app.myfiles.PICK_DATA")
                sIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                sIntent.putExtra("CONTENT_TYPE", "*/*")
                sIntent.addCategory(Intent.CATEGORY_DEFAULT)

                val chooserIntent : Intent
                if (packageManager.resolveActivity(sIntent, 0) != null){
                    //삼성 파일매니저
                    chooserIntent = Intent.createChooser(sIntent, "파일 선택 프로그램")
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(intent))
                } else {
                    //일반 안드로이드
                    chooserIntent = Intent.createChooser(intent, "파일 선택 프로그램")
                }

                try {
                    startActivityForResult(chooserIntent, REQUEST_FILESELECT)
                } catch (ex : ActivityNotFoundException) {
                    Toast.makeText(this@UploadActivity, "적합한 파일매니저가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
            R.id.uploadAllDelete -> {
                uploadFileList.clear()
                invalidate()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun invalidate() {
        if(uploadFileList.isEmpty()) {
            addedLayout.visibility = View.GONE
            notAddedLayout.visibility = View.VISIBLE
        }
        else {
            addedLayout.visibility = View.VISIBLE
            notAddedLayout.visibility = View.GONE
        }

        uploadRecyclerView.adapter?.notifyDataSetChanged()
    }
}
