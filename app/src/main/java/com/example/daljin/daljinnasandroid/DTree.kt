package com.example.daljin.daljinnasandroid

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.treeitem.view.*
import java.util.*


class DTreeNode (val value : String , val parent : DTreeNode? = null) {

    var children : MutableList<DTreeNode> = mutableListOf()
    var isVisible = false
    var isExpand = false

    fun addChild(child : DTreeNode) {
        this.children.add(child)
    }

    fun getPath() : String
    {
        var path = "/$value"
        var nowPos = parent
        while(nowPos != null) {
            path = "/${nowPos.value}$path"
            nowPos = nowPos.parent
        }
        return path
    }
    fun Expand(toggle : Boolean) {
        isExpand = toggle
        for(i in 0 until children.size) {
            children[i].toggleVisible(toggle)
        }
    }

    fun toggleVisible(toggle : Boolean) {
        isVisible = toggle
        if(isExpand) {
            for(i in 0 until children.size) {
                if(children[i].isExpand)
                    children[i].toggleVisible(true)
            }
        }
    }
}


class TreeViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
    val treeArrowButton = itemView.treeArrowButton
    val treeItemName = itemView.treeItemName
    val linearLayout = itemView.linearLayout
}

class TreeViewAdapter(private val context : Context, var Items : List<DTreeNode> , var callback : (Boolean , Int)->Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var layoutInflater = LayoutInflater.from(parent.context)
        return TreeViewHolder(layoutInflater.inflate(R.layout.treeitem , parent , false))
    }



    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = Items[position]
        var viewHolder = holder as TreeViewHolder

        viewHolder.treeItemName.text = item.value

        viewHolder.linearLayout.setOnClickListener{
            callback.invoke(!item.isExpand , position)
        }
    }

    override fun getItemCount(): Int = Items.size

}