package com.example.daljin.daljinnasandroid

fun fileSizeConverter(size : Int , count : Int = 0) : String =
    if(size / 1024 == 0) {
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