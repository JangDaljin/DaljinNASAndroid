package com.daljin.daljinnasandroid

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.daljin.daljinnasandroid.REQUEST_PICTURE
import com.daljin.daljinnasandroid.UploadItem
import kotlinx.android.synthetic.main.activity_upload.*

class UploadActivity : AppCompatActivity() {

    private var uploadFileList = mutableListOf<UploadItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        //메뉴바 설정
        setSupportActionBar(uploadToolbar)


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode) {
            REQUEST_PICTURE -> {

                when(resultCode) {
                    RESULT_OK -> {


                        data?.data?.let {
                            uploadFileList.add(UploadItem(it))
                        }

                        data?.clipData?.let {
                            for(i in 0 until it.itemCount) {
                                uploadFileList.add(UploadItem(it.getItemAt(i).uri))
                            }
                        }





                    }

                }


            }



        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.uploadmenu , menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.uploadImage ->{
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
                        startActivityForResult(chooserIntent, REQUEST_PICTURE);
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
