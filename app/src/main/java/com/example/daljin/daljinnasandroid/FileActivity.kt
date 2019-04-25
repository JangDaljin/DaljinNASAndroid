package com.example.daljin.daljinnasandroid

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.NavigationView
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
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
    private var usedStorage : Int = 0
    private var fileList = mutableListOf<DataItem>()

    private lateinit var recyclerViewAdapter : RecyclerAdapter

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file)

        navBottom.setOnNavigationItemSelectedListener(bottomNavigationItemSelectedListener)
        navSide.setNavigationItemSelectedListener(sideNavigationViewItemSelectedList)

        recyclerView.layoutManager = LinearLayoutManager(this@FileActivity)
        recyclerViewAdapter = RecyclerAdapter(fileList)
        recyclerView.adapter = recyclerViewAdapter
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




    private fun startLoginActivity() {
        startActivityForResult(Intent(this@FileActivity , LoginActivity::class.java),100)
    }

    private fun invalidate() {

        val sideHeaderInvalidate = {
            sideHeaderID.text = DaljinNodeWebLoginData.id
            sideHeaderGrade.text = DaljinNodeWebLoginData.grade
            sideHeaderMaxStorage.text = DaljinNodeWebLoginData.maxStorage.toString()
        }

        if(!DaljinNodeWebLoginData.isAuthenticated) {
            DaljinNodeWebLogin(this@FileActivity){
                sideHeaderInvalidate
            }
        } else {
            sideHeaderInvalidate
        }


        getFileList(path)
    }

    private fun getFileList(newPath : String = "") {

        DRetrofit(this@FileActivity).getFileList("$newPath").enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                //Toast.makeText(this@FileActivity , "getFileList() FAIL" , Toast.LENGTH_SHORT).show()

            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                //Toast.makeText(this@FileActivity , "getFileList() SUCCESS" , Toast.LENGTH_SHORT).show()

                if(response.isSuccessful) {
                    val parser =  JSONObject(response.body())
                    val error = parser.getBoolean("error")

                    when(error) {
                        true -> {
                            startLoginActivity()
                        }
                        false -> {
                            //리스트 초기화
                            fileList.clear()

                            //기본 저장 데이터
                            usedStorage = parser.getInt("used_storage")
                            val files = parser.getJSONObject("files")

                            //파일 파싱 후 표시
                            for (i in 0 until files.length()) {
                                var file = files.getJSONObject("$i")
                                var item = DataItem(
                                    fileSizeConverter(file.getInt("size"))
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
                            recyclerViewAdapter.notifyDataSetChanged()
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

private class RecyclerAdapter(val list : MutableList<DataItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent : ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var layoutInflater = LayoutInflater.from(parent.context)
        return ItemViewHolder(layoutInflater.inflate(R.layout.item_main , parent , false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val dataItem = list[position]

        var viewHolder = holder as ItemViewHolder

        list[position].checkbox = viewHolder.chbItem

        when(dataItem.type) {
            "directory" -> {

            }
        }

        viewHolder.tvDate.text = dataItem.ctime
        viewHolder.tvName.text = dataItem.fullname
        viewHolder.tvSize.text = dataItem.size
    }

    override fun getItemCount(): Int {
        return list.size
    }


}
