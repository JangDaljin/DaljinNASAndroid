package com.example.daljin.daljinnasandroid

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_file.*
import kotlinx.android.synthetic.main.item_main.view.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FileActivity : AppCompatActivity() {

    /*
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                message.setText(R.string.title_home)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                message.setText(R.string.title_dashboard)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                message.setText(R.string.title_notifications)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }
    */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file)

        //navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    override fun onStart() {
        super.onStart()
        getFileList()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            getFileList()
        }
    }

    private fun getFileList() {
        DRetrofit(this@FileActivity).getFileList().enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(this@FileActivity , "FAILURE" , Toast.LENGTH_SHORT).show()


            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                Log.d("DALJIN" , response.body().toString())
                Toast.makeText(this@FileActivity , "RESPONSE" , Toast.LENGTH_SHORT).show()

                if(response.isSuccessful) {
                    val parser =  JSONObject(response.body())
                    val error = parser.getBoolean("error")

                    when(error) {
                        true -> {
                            startActivityForResult(Intent(this@FileActivity , LoginActivity::class.java),100)
                        }
                        false -> {
                            val id = parser.getString("id")
                            val path = parser.getString("path")
                            val files = parser.getJSONObject("files")
                            val max_storage = parser.getInt("max_storage")
                            val used_storage = parser.getInt("used_storage")
                            val grade = parser.getString("grade")

                            val fileList = mutableListOf<DataItem>()

                            for (i in 0 until files.length()) {
                                var file = files.getJSONObject("$i")
                                var item = DataItem(
                                    fileSizeConverter(file.getInt("size"))
                                    ,file.getString("ctime")
                                    ,file.getString("type")
                                    ,file.getString("name")
                                    ,file.getString("extension")
                                    ,file.getString("fullname")
                                )
                                fileList.add(item)
                            }

                            recyclerView.layoutManager = LinearLayoutManager(this@FileActivity)
                            recyclerView.adapter = RecyclerAdapter(fileList)

                        }
                    }
                }
            }
        })
    }

}

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


class DataItem( val size : String
                ,val ctime : String
                ,val type : String
                ,val name : String
                ,val extension : String
                ,val fullname : String
                )

private class ItemViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
    val tvName = itemView.tvName
    val tvSize  = itemView.tvSize
    val tvDate  = itemView.tvDate
}

private class RecyclerAdapter(val list : MutableList<DataItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent : ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var layoutInflater = LayoutInflater.from(parent?.context)
        return ItemViewHolder(layoutInflater.inflate(R.layout.item_main , parent , false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val dataItem = list[position]

        var viewHolder = holder as ItemViewHolder
        viewHolder.tvDate.text = dataItem.ctime
        viewHolder.tvName.text = dataItem.fullname
        viewHolder.tvSize.text = dataItem.size
    }

    override fun getItemCount(): Int {
        return list.size
    }


}
