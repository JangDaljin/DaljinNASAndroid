package com.daljin.daljinnasandroid

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.fileview_item.view.*


class FileViewItem(val size : Long
                   , val ctime : String
                   , val type : String
                   , val name : String
                   , val extension : String
                   , val fullname : String
                   , var isChecked : Boolean = false
)

class FileViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
    val img = itemView.fileViewImage
    val chbItem = itemView.fileViewChbItem
    val tvName = itemView.fileViewTvName
    val tvSize  = itemView.fileViewTvSize
    val tvDate  = itemView.fileViewTvDate
    val Layout = itemView.fileViewLayout
}

class FileViewAdapter(context : Context, var items : MutableList<FileViewItem>, var callback : ((String)->Unit)?): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val directoryImage = ContextCompat.getDrawable(context , R.drawable.directoryicon)
    private val fileImage = ContextCompat.getDrawable(context , R.drawable.fileicon)


    override fun onCreateViewHolder(parent : ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var layoutInflater = LayoutInflater.from(parent.context)
        return FileViewHolder(layoutInflater.inflate(R.layout.fileview_item , parent , false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[holder.adapterPosition]
        var viewHolder = holder as FileViewHolder

        if(item.type == "directory") {
            viewHolder.img.setImageDrawable(directoryImage)
            viewHolder.tvName.apply {
                setTextColor(ContextCompat.getColor(context , R.color.dodgerblue))
            }
        }
        else {
            viewHolder.img.setImageDrawable(fileImage)
            viewHolder.tvName.apply {
                setTextColor(ContextCompat.getColor(context , R.color.dimgray))
            }
        }

        viewHolder.tvName.text = item.fullname


        viewHolder.Layout.apply {
            setOnClickListener {
                item.isChecked = !item.isChecked
                viewHolder.chbItem.isChecked = item.isChecked
                viewHolder.chbItem.background = ContextCompat.getDrawable(context , R.drawable.fileviewcheckbox)
                it.startAnimation(AnimationUtils.loadAnimation(context , R.anim.largeviewclickanim))
            }
        }

        viewHolder.chbItem.apply {
            isChecked = item.isChecked
            setOnClickListener {
                it.startAnimation(AnimationUtils.loadAnimation(context , R.anim.clickanim))
                item.isChecked = isChecked
            }
        }
        viewHolder.tvSize.text = fileSizeConverter(item.size)
        viewHolder.tvDate.text = item.ctime
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun changeAllCheck(t : Boolean) {
        items.forEach { it.isChecked = t }
        notifyDataSetChanged()
    }
}