package com.example.daljin.daljinnasandroid

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.treeitem.view.*
import java.util.*

class DTreeRoot() {

    val nodeList = mutableListOf<DTreeNode>()

    private val queue : Queue<DTreeNode> = LinkedList()

    fun addNodes(treeNodes : List<DTreeNode>) {

        for(i in 0 until treeNodes.size)
        {
            queue.add(treeNodes[i])
        }

        while(queue.isNotEmpty()) {
            val item = queue.remove()
            nodeList.add(item)
            for(i in 0 until item.children.size) {
                queue.add(item.children[i])
            }
        }
    }



}

class DTreeNode (val value : String , var level : Int = 0 , var visible : Boolean = true) {

    var children : MutableList<DTreeNode> = mutableListOf()

    fun addChild(child : DTreeNode) {
        child.changeLevel(level+1)
        this.children.add(child)
    }

    fun changeLevel(_level : Int) {
        level = _level
        for(i in 0 until children.size) {
            children[i].changeLevel(level+1)
        }
    }

    fun childVisibleToggle(vis : Boolean) {
        for(i in 0 until children.size) {
            children[i].visible = vis
        }
    }
}

class TreeViewHodler(itemView : View) : RecyclerView.ViewHolder(itemView) {
    val treeArrowButton = itemView.treeArrowButton
    val treeItemName = itemView.treeItemName
}

class TreeViewAdapter(var Items : MutableList<DataItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var layoutInflater = LayoutInflater.from(parent.context)
        return TreeViewHodler(layoutInflater.inflate(R.layout.item_main , parent , false))
    }



    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = Items[position]
        var viewHolder = holder as TreeViewHodler

    }

    override fun getItemCount(): Int = Items.size

}