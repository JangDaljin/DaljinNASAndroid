package com.example.daljin.daljinnasandroid

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.NavigationView
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_file.*
import kotlinx.android.synthetic.main.item_main.view.*
import kotlinx.android.synthetic.main.sideheader.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FileActivity : AppCompatActivity() {

    private var path : String = ""
    private var usedStorage : Long = 0L
    private var fileList = mutableListOf<DataItem>()

    private lateinit var recyclerViewAdapter : RecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file)

        navBottom.setOnNavigationItemSelectedListener(bottomNavigationItemSelectedListener)
        navSide.setNavigationItemSelectedListener(sideNavigationViewItemSelectedList)

        recyclerView.layoutManager = LinearLayoutManager(this@FileActivity)
    }

    override fun onStart() {
        super.onStart()
        invalidate()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            invalidate()
        }
    }

    private val bottomNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navMkdir -> {

                return@OnNavigationItemSelectedListener true
            }
            R.id.navRmdir -> {

                return@OnNavigationItemSelectedListener true
            }
            R.id.navUpload -> {

                return@OnNavigationItemSelectedListener true
            }
            R.id.navDownload -> {

                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private val sideNavigationViewItemSelectedList = NavigationView.OnNavigationItemSelectedListener { item ->
        when(item.itemId) {
            R.id.sideLogin -> {
                startLoginActivity()
                true
            }
            R.id.sideLogout -> {
                DRetrofit(this@FileActivity).logout().enqueue(object : Callback<String> {
                    override fun onFailure(call: Call<String>, t: Throwable) {
                        Toast.makeText(this@FileActivity , "서버 연결 불가" , Toast.LENGTH_SHORT).show()
                    }

                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if(response.isSuccessful) {
                            var parser = JSONObject(response.body())
                            when(parser.getBoolean("error")) {
                                true -> Toast.makeText(this@FileActivity , "로그아웃 불가" , Toast.LENGTH_SHORT).show()
                                false -> startLoginActivity()
                            }
                        }
                    }
                })
                true
            }
            else -> {
                false
            }
        }
    }






    private fun startLoginActivity() {
        startActivityForResult(Intent(this@FileActivity , LoginActivity::class.java),100)
    }

    private fun invalidate() {
        DaljinNodeWebLogin(this@FileActivity) {
            if(it) {
                sideHeaderID.text = DaljinNodeWebLoginData.id
                sideHeaderGrade.text = DaljinNodeWebLoginData.grade
                sideHeaderMaxStorage.text = fileSizeConverter(DaljinNodeWebLoginData.maxStorage)
            }
            getFileList(path)
        }
    }

    private fun getFileList(newPath : String = "") {
        DRetrofit(this@FileActivity).getFileList(newPath).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(this@FileActivity , "서버와 연결이 불가능합니다." , Toast.LENGTH_SHORT).show()

            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if(response.isSuccessful) {
                    val parser =  JSONObject(response.body())
                    val error = parser.getBoolean("error")

                    when(error) {
                        true -> {
                            startLoginActivity()
                        }
                        false -> {
                            path = newPath
                            //리스트 초기화
                            fileList.clear()

                            //프로그래스바 초기화
                            usedStorage = parser.getLong("used_storage")
                            val percentage = usedStorage / DaljinNodeWebLoginData.maxStorage
                            tvStorage.text = "$percentage%"
                            pgbStorage.progress = percentage.toInt()

                            val files = parser.getJSONObject("files")

                            //파일 파싱 후 표시
                            for (i in 0 until files.length()) {
                                var file = files.getJSONObject("$i")
                                var item = DataItem(
                                    fileSizeConverter(file.getLong("size"))
                                    ,file.getString("ctime")
                                    ,file.getString("type")
                                    ,file.getString("name")
                                    ,file.getString("extension")
                                    ,file.getString("fullname")
                                    ,null
                                )
                                fileList.add(item)
                            }
                            //리사이클러뷰 초기화
                            var adapter = RecyclerAdapter(fileList)
                            recyclerView.adapter = adapter
                            for(i in 0 until adapter.tvNameList.size) {
                                adapter.tvNameList[i].setOnClickListener {
                                    Toast.makeText(this@FileActivity , "HELLO" , Toast.LENGTH_SHORT).show()
                                    if(fileList[i].type == "directory") {
                                        if(it is TextView) {
                                            getFileList("$path/${it.text}")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        })
    }
}




class DataItem( val size : String
                ,val ctime : String
                ,val type : String
                ,val name : String
                ,val extension : String
                ,val fullname : String
                ,var checkbox : CheckBox?
                )

private class ItemViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
    val chbItem = itemView.chbItem
    val tvName = itemView.tvName
    val tvSize  = itemView.tvSize
    val tvDate  = itemView.tvDate
}

private class RecyclerAdapter(var items : MutableList<DataItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var checkBoxList = mutableListOf<CheckBox>()
    var tvNameList = mutableListOf<TextView>()
    var tvSizeList = mutableListOf<TextView>()

    override fun onCreateViewHolder(parent : ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var layoutInflater = LayoutInflater.from(parent.context)
        return ItemViewHolder(layoutInflater.inflate(R.layout.item_main , parent , false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        var viewHolder = holder as ItemViewHolder
        items[position].checkbox = viewHolder.chbItem

        when(item.type) {
            "directory" -> {
                viewHolder.chbItem.visibility = View.INVISIBLE
                viewHolder.tvName.setTextColor(Color.BLUE)
            }
        }

        viewHolder.tvDate.text = item.ctime
        viewHolder.tvName.text = item.fullname
        viewHolder.tvSize.text = item.size

        checkBoxList.add(viewHolder.chbItem)
        tvNameList.add(viewHolder.tvName)
        tvSizeList.add(viewHolder.tvSize)
    }

    override fun getItemCount(): Int {
        return items.size
    }


}
