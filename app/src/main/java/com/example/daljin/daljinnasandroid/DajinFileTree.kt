package com.example.daljin.daljinnasandroid

class DTreeItem (var value : String) {
    private var children : MutableList<DTreeItem>? = mutableListOf()

    fun add(child : DTreeItem) {
        this.children?.add(child)
    }

    fun add(children : MutableList<DTreeItem>) {
        this.children?.addAll(children)
    }
}