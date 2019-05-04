package com.daljin.daljinnasandroid

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.daljin.daljinnasandroid.REQUEST_PERM_IN
import com.daljin.daljinnasandroid.REQUEST_PERM_RE
import com.daljin.daljinnasandroid.REQUEST_PERM_WE
import java.util.jar.Manifest


fun fileSizeConverter(size : Long , count : Int = 0) : String =
    if(size / 1024 == 0L) {
        "$size${when(count) {
            0 -> "B"
            1 -> "KB"
            2 -> "MB"
            3 -> "GB"
            else -> "TB"
        }}"
    }
    else {
        fileSizeConverter(size / 1024 , count + 1)
    }

fun checkPermission(context : Context , activity : Activity) {
    if(ContextCompat.checkSelfPermission(context , android.Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(activity , arrayOf(android.Manifest.permission.INTERNET) , REQUEST_PERM_IN)
    }

    if(ContextCompat.checkSelfPermission(context , android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(activity , arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) , REQUEST_PERM_WE)
    }

    if(ContextCompat.checkSelfPermission(context , android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(activity , arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE) , REQUEST_PERM_RE)
    }
}