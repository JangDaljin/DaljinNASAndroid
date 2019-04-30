package com.example.daljin.daljinnasandroid

import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fileview_item.view.*


class FileViewItem(val size : String
                   , val ctime : String
                   , val type : String
                   , val name : String
                   , val extension : String
                   , val fullname : String
                   , var isChecked : Boolean = false
)

class FileViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
    val chbItem = itemView.fileViewChbItem
    val tvName = itemView.fileViewTvName
    val tvSize  = itemView.fileViewTvSize
    val tvDate  = itemView.fileViewTvDate
}

class FileViewAdapter(context : Context, var items : MutableList<FileViewItem>, var callback : (String)->Unit): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val fileImage = ContextCompat.getDrawable(context , R.drawable.fileicon)
    private val directoryImage = ContextCompat.getDrawable(context , R.drawable.directoryicon)

    override fun onCreateViewHolder(parent : ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var layoutInflater = LayoutInflater.from(parent.context)
        return FileViewHolder(layoutInflater.inflate(R.layout.fileview_item , parent , false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        var viewHolder = holder as FileViewHolder

        val iconWidth= viewHolder.tvName.textSize.toInt()
        val iconHeight  = viewHolder.tvName.textSize.toInt()
        val iconPadding : Int = viewHolder.tvName.textSize.toInt()/2

        when(item.type) {
            "directory" -> {
                directoryImage?.apply { setBounds(0 , 0 , iconWidth, iconHeight ) }
                viewHolder.tvName.apply {
                    compoundDrawablePadding = iconPadding
                    setCompoundDrawables(directoryImage, null, null, null)
                    setTextColor(Color.BLUE)
                    setOnClickListener {
                        callback.invoke(viewHolder.tvName.text.toString())
                    }
                }
            }
            else -> {
                fileImage?.apply { setBounds(0 , 0 , iconWidth, iconHeight) }
                viewHolder.tvName.apply{
                    setTextColor(Color.BLACK)
                    compoundDrawablePadding = iconPadding
                    setCompoundDrawables(fileImage , null , null , null)
                }
            }
        }

        viewHolder.tvName.text = item.fullname

        viewHolder.chbItem.apply {
            isChecked = item.isChecked
            setOnClickListener {
                item.isChecked = isChecked
            }
        }
        viewHolder.tvSize.text = item.size
        viewHolder.tvDate.text = item.ctime
    }

    override fun getItemCount(): Int {
        return items.size
    }


    fun toggleAll(t : Boolean) {
        items.forEach { it.isChecked = t }
        notifyDataSetChanged()
    }
}