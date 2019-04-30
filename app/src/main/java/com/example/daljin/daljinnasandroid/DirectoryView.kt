package com.example.daljin.daljinnasandroid

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.directoryview_item.view.*

class DirectoryViewItem(
    val Name : String
)

class DirectoryViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
    val tvName = itemView.directoryViewTvName
    val linearLyaout = itemView.linearLayout
}

class DirectoryViewAdapter(context : Context, var items : MutableList<DirectoryViewItem>, var callback : (String)->Unit): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val directoryImage = ContextCompat.getDrawable(context , R.drawable.directoryicon)

    override fun onCreateViewHolder(parent : ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var layoutInflater = LayoutInflater.from(parent.context)
        return DirectoryViewHolder(layoutInflater.inflate(R.layout.directoryview_item , parent , false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        var viewHolder = holder as DirectoryViewHolder

        val iconWidth= viewHolder.tvName.textSize.toInt()
        val iconHeight  = viewHolder.tvName.textSize.toInt()
        val iconPadding : Int = viewHolder.tvName.textSize.toInt()/2


        viewHolder.linearLyaout.apply {
            setOnClickListener {
                callback.invoke(viewHolder.tvName.text.toString())
            }
        }

        directoryImage?.apply { setBounds(0 , 0 , iconWidth, iconHeight ) }
        viewHolder.tvName.apply {
            compoundDrawablePadding = iconPadding
            setCompoundDrawables(directoryImage, null, null, null)
            text = item.Name
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}
