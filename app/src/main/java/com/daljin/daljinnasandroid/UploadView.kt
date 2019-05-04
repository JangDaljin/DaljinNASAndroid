package com.daljin.daljinnasandroid

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.upload_item.view.*
import java.io.File




class UploadViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
    val Imgbutton = itemView.uploadItemImage
    val tvName = itemView.uploadFileName
    val tvSize = itemView.uploadFileSize
}

class UploadViewAdapter(var items : MutableList<File>, var callback : (Int)->Unit): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent : ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var layoutInflater = LayoutInflater.from(parent.context)
        return UploadViewHolder(layoutInflater.inflate(R.layout.upload_item , parent , false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[holder.adapterPosition]
        var viewHolder = holder as UploadViewHolder

        viewHolder.Imgbutton.setOnClickListener {
            callback.invoke(holder.adapterPosition)
        }
        viewHolder.tvName.text = item.name
        viewHolder.tvSize.text = fileSizeConverter(item.length())
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}