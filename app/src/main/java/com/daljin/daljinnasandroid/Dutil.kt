package com.daljin.daljinnasandroid

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View




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


class RecyclerViewSpace(var top : Int = 0 , var right : Int = 0 , var bottom : Int = 0 , var left : Int = 0): RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.top = top
        outRect.right = right
        outRect.bottom = bottom
        outRect.left = left
    }

}