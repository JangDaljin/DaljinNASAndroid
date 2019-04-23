package com.example.daljin.daljinnasandroid

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.io.IOException




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

class AddCookiesInterceptor(val context : Context) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        // Preference에서 cookies를 가져오는 작업을 수행
        val sharedPreferences = context.getSharedPreferences("DaljinNAS" , Context.MODE_PRIVATE)
        val sharedPreferencesData = sharedPreferences.getStringSet("Cookie" , mutableSetOf())



        for(cookie in sharedPreferencesData) {
            builder.addHeader("Cookie", cookie)
        }



        // Web,Android,iOS 구분을 위해 User-Agent세팅
        builder.removeHeader("User-Agent").addHeader("User-Agent", "Android")
        return chain.proceed(builder.build())
    }
}

 class ReceivedCookiesInterceptor(val context : Context) : Interceptor {
     @Throws(IOException::class)
     override fun intercept(chain: Interceptor.Chain): Response {
         val originalResponse = chain.proceed(chain.request())



         if (originalResponse.headers("Set-Cookie").isNotEmpty()) {
             val cookies = mutableSetOf<String>()
             for (header in originalResponse.headers("set-cookie")) {
                 cookies.add(header)
             }

             // Preference에 cookies를 넣어주는 작업을 수행
             val sharedPreferences = context.getSharedPreferences("DaljinNAS" , Context.MODE_PRIVATE)
             val editor = sharedPreferences.edit()
             editor.putStringSet("Cookie" , cookies)
             editor.commit()
         }
         return originalResponse
     }
 }


fun DRetrofit(context : Context) : DRetrofitInterface
{

    val client = OkHttpClient().newBuilder()
        .addNetworkInterceptor(ReceivedCookiesInterceptor(context))
        .addNetworkInterceptor(AddCookiesInterceptor(context))
        .build()


    return Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8000")
        //.baseUrl("http://daljin.dlinkddns.com")
        .client(client)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(DRetrofitInterface::class.java)
}


