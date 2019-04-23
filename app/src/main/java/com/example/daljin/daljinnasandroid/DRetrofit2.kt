package com.example.daljin.daljinnasandroid

import android.webkit.CookieManager
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import javax.xml.datatype.DatatypeConstants.SECONDS
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit


//로그인
data class DSuccess(var RESULT : Boolean)
class DLoginInformation(val ID : String, val PW : String)



//데이터 요청
data class DInfomation(val id  : String ,
                       val path : String ,
                       val files : JSONObject ,
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

fun DRetrofit() : DRetrofitInterface
{
    val interceptor = HttpLoggingInterceptor()
    interceptor.level = HttpLoggingInterceptor.Level.BODY


    val cookieHandler = java.net.CookieManager()

    val client = OkHttpClient.Builder().addNetworkInterceptor(interceptor)
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .cookieJar(JavaNetCookieJar(cookieHandler))
        .build()


    return Retrofit.Builder()
        .baseUrl("http://daljin.dlinkddns.com")
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()
        .create(DRetrofitInterface::class.java)
}


