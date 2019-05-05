package com.daljin.daljinnasandroid

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_upload.*
import java.io.File

class UploadActivity : AppCompatActivity() {

    private var uploadFileList = mutableListOf<File>()
    private lateinit var uploadViewAdapter : UploadViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        //메뉴바 설정
        setSupportActionBar(uploadToolbar)

        //리사이클러뷰 설정
        uploadViewAdapter = UploadViewAdapter(uploadFileList) {
            uploadFileList.removeAt(it)
            uploadViewAdapter.notifyItemRemoved(it)
        }
        val uploadRecyclerViewLinearLayoutManager = LinearLayoutManager(this@UploadActivity)
        uploadRecyclerView.layoutManager = uploadRecyclerViewLinearLayoutManager
        uploadRecyclerView.adapter = uploadViewAdapter
        uploadRecyclerView.addItemDecoration(RecyclerViewSpace(30 , 30 , 30 , 30))
        uploadRecyclerView.addItemDecoration(DividerItemDecoration(this@UploadActivity , uploadRecyclerViewLinearLayoutManager.orientation))
        uploadViewAdapter.notifyDataSetChanged()

        //업로드 버튼 클릭
        uploadButton.setOnClickListener {
            val returnIntent = Intent()
            val stringUploadFilePaths = ArrayList<String>()
            var totalSize = 0L
            uploadFileList.forEach{
                stringUploadFilePaths.add(it.path)
                totalSize += it.length()
            }
            returnIntent.putStringArrayListExtra("uploadFiles" , stringUploadFilePaths)
            returnIntent.putExtra("TotalSize" , totalSize)
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
                            uploadFileList.add(File(DutilJava.getPath(this@UploadActivity , it)))
                        }

                        data?.clipData?.let {
                            for(i in 0 until it.itemCount) {
                                uploadFileList.add(File(DutilJava.getPath(this@UploadActivity ,it.getItemAt(i).uri)))
                            }
                        }
                    }
                }
                uploadViewAdapter.notifyDataSetChanged()
            }
        }
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

                    // special intent for Samsung file manager
                    val sIntent = Intent("com.sec.android.app.myfiles.PICK_DATA")
                    // if you want any file type, you can skip next line

                    sIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    sIntent.putExtra("CONTENT_TYPE", "*/*")
                    sIntent.addCategory(Intent.CATEGORY_DEFAULT)

                    var chooserIntent = Intent()
                    if (packageManager.resolveActivity(sIntent, 0) != null){
                        // it is device with Samsung file manager
                        chooserIntent = Intent.createChooser(sIntent, "Open file")
                        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(intent))
                    } else {
                        chooserIntent = Intent.createChooser(intent, "Open file")
                    }

                    try {
                        startActivityForResult(chooserIntent, REQUEST_FILESELECT)
                    } catch (ex : ActivityNotFoundException) {
                        Toast.makeText(this@UploadActivity, "No suitable File Manager was found.", Toast.LENGTH_SHORT).show();
                    }
            }
            R.id.uploadAllDelete -> {

            }
        }
        return super.onOptionsItemSelected(item)
    }
}
