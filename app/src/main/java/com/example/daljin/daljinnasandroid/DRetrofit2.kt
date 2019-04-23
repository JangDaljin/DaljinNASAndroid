package com.example.daljin.daljinnasandroid

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

//로그인
data class DSuccess(var RESULT : Boolean)
class DLoginInformation(val ID : String, val PW : String)



//데이터 요청
data class DInfomation(val id  : String ,
                       val path : String ,
                       val files : DFileDescription ,
                       val max_storage : Int ,
                       val used_storage : Int ,
                       val grade : String)

data class DFileDescription(
    val size : Int,
    val ctime : String ,
    val type : String,
    val name : String,
    val extension : String ,
    val fullname : String
)



interface DRetrofitInterface {
    @POST("/loginNW")
    fun login(@Body info : DLoginInformation) : Call<DSuccess>

    @POST("/fileList")
    fun getFileList() : Call<DInfomation>


}


object DRetrofitController {
    private const val URL = "http://daljin.dlinkddns.com"

    private val retrofit = Retrofit.Builder()
                        .baseUrl(URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()

    val Instance = retrofit.create(DRetrofitInterface::class.java)
}