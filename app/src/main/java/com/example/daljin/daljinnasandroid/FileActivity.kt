package com.example.daljin.daljinnasandroid

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import com.github.treeview.*
import com.github.treeview.TreeViewAdapter
import kotlinx.android.synthetic.main.activity_file.*
import kotlinx.android.synthetic.main.item_main.view.*
import kotlinx.android.synthetic.main.rightsideheader.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class FileActivity : AppCompatActivity() {

    private var path: String = ""
    private var usedStorage: Long = 0L
    private var fileList = mutableListOf<DataItem>()
    private lateinit var fileViewAdapter: FileViewAdapter

    private var treeNodes = mutableListOf<TreeNode<*>>()
    private lateinit var treeViewAdapter: TreeViewAdapter
    private var selectedItemView : TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file)

        //파일뷰 setting
        fileView.layoutManager = LinearLayoutManager(this@FileActivity)
        fileViewAdapter = FileViewAdapter(this@FileActivity , fileList) {

        }
        fileView.adapter = fileViewAdapter

        //트리뷰 setting
        treeRecyclerView.layoutManager = LinearLayoutManager(this@FileActivity)
        treeViewAdapter = TreeViewAdapter(treeNodes , Arrays.asList(FileNodeBinder() , DirectoryNodeBinder()))
        treeViewAdapter.setOnTreeNodeListener(object : TreeViewAdapter.OnTreeNodeListener{
            override fun onClick(node: TreeNode<*>?, holder: RecyclerView.ViewHolder?): Boolean {
                path = "/${(node?.content as Dir).dirName}"
                var pNode = node.parent
                while(pNode != null) {
                    path = "/${(pNode.content as Dir).dirName}$path"
                    pNode = pNode.parent
                }
                when(node.isLeaf) {
                    false -> {
                        onToggle(!node.isExpand , holder)
                    }
                }
                invalidate(node)
                return false
            }

            override fun onToggle(isExpand: Boolean, holder: RecyclerView.ViewHolder?) {
                val dirViewHolder = holder as DirectoryNodeBinder.ViewHolder
                val ivArrow = dirViewHolder.ivArrow
                val rotateDegree = if(isExpand) 90f else -90f
                ivArrow.animate().rotationBy(rotateDegree).start()
            }
        })
        treeRecyclerView.adapter = treeViewAdapter




        navBottom.setOnNavigationItemSelectedListener(bottomNavigationItemSelectedListener)
        rightSizeView.setNavigationItemSelectedListener(sideNavigationViewItemSelectedList)

        chbAll.setOnCheckedChangeListener { buttonView, isChecked ->
            fileViewAdapter.toggleAll(isChecked)
        }

    }

    override fun onStart() {
        super.onStart()
        invalidate()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
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
        when (item.itemId) {
            R.id.sideLogin -> {
                startLoginActivity()
                true
            }
            R.id.sideLogout -> {
                DRetrofit(this@FileActivity).logout().enqueue(object : Callback<String> {
                    override fun onFailure(call: Call<String>, t: Throwable) {
                        Toast.makeText(this@FileActivity, "서버 연결 불가", Toast.LENGTH_SHORT).show()
                    }

                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if (response.isSuccessful) {
                            var parser = JSONObject(response.body())
                            when (parser.getBoolean("error")) {
                                true -> Toast.makeText(this@FileActivity, "로그아웃 불가", Toast.LENGTH_SHORT).show()
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
        startActivityForResult(Intent(this@FileActivity, LoginActivity::class.java), 100)
    }

    private fun invalidate(node : TreeNode<*>? = null) {
        DaljinNodeWebLogin(this@FileActivity , "" , "") { loginRes, loginBody ->
            if (loginRes) {
                when (loginBody) {
                    null -> {}
                    else -> {
                        sideHeaderID.text = DaljinNodeWebLoginData.id
                        sideHeaderGrade.text = DaljinNodeWebLoginData.grade
                        sideHeaderMaxStorage.text = fileSizeConverter(DaljinNodeWebLoginData.maxStorage)
                    }
                }
            }
            else {
                Toast.makeText(this@FileActivity, "서버와 통신 불가", Toast.LENGTH_SHORT).show()
            }

            DaljinNodeWebGetFileList(this@FileActivity, path) { res, body ->
                if (res) {
                    val parser = JSONObject(body)
                    val error = parser.getBoolean("error")
                    when (error) {
                        true -> {
                            startLoginActivity()
                        }
                        false -> {
                            //리스트 초기화
                            fileList.clear()

                            //프로그래스바 초기화
                            usedStorage = parser.getLong("used_storage")
                            val percentage = usedStorage / DaljinNodeWebLoginData.maxStorage
                            tvStorage.text = "$percentage%"
                            pgbStorage.progress = percentage.toInt()

                            val files = parser.getJSONObject("files")
                            var dirs = mutableListOf<String>()
                            //파일 파싱 후 표시
                            for (i in 0 until files.length()) {
                                var file = files.getJSONObject("$i")
                                var item = DataItem(
                                    fileSizeConverter(file.getLong("size"))
                                    , file.getString("ctime")
                                    , file.getString("type")
                                    , file.getString("name")
                                    , file.getString("extension")
                                    , file.getString("fullname")
                                    , false
                                )
                                fileList.add(item)
                                if(item.type=="directory") {
                                    dirs.add(item.name)
                                }
                            }
                            when(node)
                            {
                                //최초실행(onStart())
                                null -> {
                                    treeNodes.clear()
                                    val rootNode = TreeNode(Dir("/"))
                                    for(i in 0 until dirs.size) {
                                        rootNode.addChild(TreeNode(Dir(dirs[i])))
                                    }
                                    treeNodes.add(rootNode)
                                    treeViewAdapter.refresh(treeNodes)
                                }
                                //일반 실행
                                else -> {
                                    node.childList.clear()
                                    for(i in 0 until dirs.size) {
                                        node.addChild(TreeNode(Dir(dirs[i])))
                                    }
                                    treeViewAdapter.refresh(treeNodes)
                                }
                            }

                            //리사이클러뷰 초기화
                            fileViewAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
        }
    }

}


class DataItem( val size : String
                ,val ctime : String
                ,val type : String
                ,val name : String
                ,val extension : String
                ,val fullname : String
                ,var isChecked : Boolean
                )

private class FileViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
    val chbItem = itemView.chbItem
    val tvName = itemView.tvName
    val tvSize  = itemView.tvSize
    val tvDate  = itemView.tvDate
}

private class FileViewAdapter(context : Context, var items : MutableList<DataItem> , var callback : (String)->Unit): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val fileImage = ContextCompat.getDrawable(context , R.drawable.fileicon)
    private val directoryImage = ContextCompat.getDrawable(context , R.drawable.directoryicon)

    override fun onCreateViewHolder(parent : ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var layoutInflater = LayoutInflater.from(parent.context)
        return FileViewHolder(layoutInflater.inflate(R.layout.item_main , parent , false))
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









