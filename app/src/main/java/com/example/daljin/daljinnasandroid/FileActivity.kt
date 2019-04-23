package com.example.daljin.daljinnasandroid

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_file.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FileActivity : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                message.setText(R.string.title_home)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                message.setText(R.string.title_dashboard)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                message.setText(R.string.title_notifications)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        getFileList()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            getFileList()
        }
    }

    private fun getFileList() {
        DRetrofit(this@FileActivity).getFileList().enqueue(object : Callback<DInfomation> {
            override fun onFailure(call: Call<DInfomation>, t: Throwable) {
                Toast.makeText(this@FileActivity , "FAILURE" , Toast.LENGTH_SHORT).show()

                startActivityForResult(Intent(this@FileActivity , MainActivity::class.java),100)

            }

            override fun onResponse(call: Call<DInfomation>, response: Response<DInfomation>) {
                Toast.makeText(this@FileActivity , "RESPONSE" , Toast.LENGTH_SHORT).show()
            }
        })
    }

}
