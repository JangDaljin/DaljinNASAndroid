package com.daljin.daljinnasandroid

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.directoryview_item.view.*

class DirectoryViewItem(
    val Name : String
)

class DirectoryViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
    val img = itemView.directoryViewImg
    val tvName = itemView.directoryViewTvName
    val Layout = itemView.linearLayout
}

class DirectoryViewAdapter(context : Context, var items : MutableList<DirectoryViewItem>, var callback : (String)->Unit): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val returnImage = ContextCompat.getDrawable(context , R.drawable.backicon)
    val directoryImage = ContextCompat.getDrawable(context , R.drawable.directoryicon)

    override fun onCreateViewHolder(parent : ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var layoutInflater = LayoutInflater.from(parent.context)
        return DirectoryViewHolder(layoutInflater.inflate(R.layout.directoryview_item , parent , false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[holder.adapterPosition]
        var viewHolder = holder as DirectoryViewHolder

        if(item.Name == "..") {
            viewHolder.img.setImageDrawable(returnImage)
        }
        else {
            viewHolder.img.setImageDrawable(directoryImage)
        }

        viewHolder.Layout.apply {
            setOnClickListener {
                it.startAnimation(AnimationUtils.loadAnimation(context , R.anim.mediumviewclickanim))
                callback.invoke(viewHolder.tvName.text.toString())
            }
        }
        viewHolder.tvName.apply {
            text = item.Name
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}
