package com.daljin.daljinnasandroid

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_per.*

class PerActivity : AppCompatActivity() {

    private var useInternet = false
    private var useWriteExternalStoage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_per)
        setSupportActionBar(perToolbar)


        internetSwitch.setOnCheckedChangeListener {
            buttonView, isChecked ->

            checkPermission(android.Manifest.permission.INTERNET)
        }
        writeExternalSwitch.setOnCheckedChangeListener {
            buttonView, isChecked ->
            checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }


    }

    override fun onStart() {
        super.onStart()
        checkPermission(listOf(android.Manifest.permission.INTERNET , android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            REQUEST_PERM-> {
                if(grantResults.isNotEmpty() && permissions.isNotEmpty()) {
                    for (i in 0 until grantResults.size) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            toggleState(permissions[i], true)
                        }
                    }
                }
                startFileActivity()
            }
        }
    }


    private fun checkPermission(inputPermission : String) {
        if(ContextCompat.checkSelfPermission(this@PerActivity , inputPermission) != PackageManager.PERMISSION_GRANTED) {
            toggleState(inputPermission , false)
            ActivityCompat.requestPermissions(this , arrayOf(inputPermission) , REQUEST_PERM)
        }
    }

    private fun checkPermission(inputPermission : List<String>) {
        val outputPermission = mutableListOf<String>()
        for(i in 0 until inputPermission.size) {
            if(ContextCompat.checkSelfPermission(this@PerActivity , inputPermission[i]) != PackageManager.PERMISSION_GRANTED) {
                toggleState(inputPermission[i] , false)
                outputPermission.add(inputPermission[i])
            }
            else {
                toggleState(inputPermission[i] , true)
            }
        }

        if(outputPermission.isEmpty()) {
            startFileActivity()
        }
        else {
            ActivityCompat.requestPermissions(this, outputPermission.toTypedArray(), REQUEST_PERM)
        }

    }

    fun toggleState(permission : String , toggle : Boolean) {
        when(permission){
            android.Manifest.permission.INTERNET -> { useInternet = toggle; internetSwitch.isChecked = toggle; if(toggle) { internetSwitch.isClickable = false } }
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE -> {useWriteExternalStoage = toggle; writeExternalSwitch.isChecked = toggle; if(toggle){ writeExternalSwitch.isClickable = false } }
        }
    }

    fun startFileActivity() {
        if (useInternet && useWriteExternalStoage) {
            startActivity(Intent(this@PerActivity, FileActivity::class.java))
            finish()
        }
    }
}

