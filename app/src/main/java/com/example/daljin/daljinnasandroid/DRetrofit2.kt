package com.example.daljin.daljinnasandroid

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Path
import java.io.IOException


interface DRetrofitInterface {
    @FormUrlEncoded
    @POST("/loginNW")
    fun login(@Field("ID") ID : String , @Field("PW") PW : String) : Call<String>

    @POST("/fileList/{path}")
    fun getFileList(@Path("path") path : String) : Call<String>

    @POST("/logoutNW")
    fun logout() : Call<String>
}

//세션 유지를 위한 쿠키 헤더 추가
class AddCookiesInterceptor(val context : Context) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        // Preference에서 cookies를 가져오는 작업을 수행
        val sharedPreferences = context.getSharedPreferences("DaljinNAS" , Context.MODE_PRIVATE)
        val sharedPreferencesData  = sharedPreferences.getStringSet("Cookie" , mutableSetOf())


        if(sharedPreferencesData != null) {
            for(cookie in sharedPreferencesData) {
                builder.addHeader("Cookie", cookie)
            }
        }




        // Web,Android,iOS 구분을 위해 User-Agent세팅
        builder.removeHeader("User-Agent").addHeader("User-Agent", "Android")
        return chain.proceed(builder.build())
    }
}

//세션 유지를 위한 쿠키 갱신
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

//레트로핏 불러오기
fun DRetrofit(context : Context) : DRetrofitInterface
{
    val client = OkHttpClient().newBuilder()
        .addNetworkInterceptor(ReceivedCookiesInterceptor(context))
        .addNetworkInterceptor(AddCookiesInterceptor(context))
        .build()


    return Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8000")
        //.baseUrl("http://daljin.dlinkddns.com") // 릴리즈 용
        .client(client)
        .addConverterFactory(ScalarsConverterFactory.create())
        //addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(DRetrofitInterface::class.java)
}

//연결 관리 데이터
object DaljinNodeWebLoginData {
    var id : String = ""
    var grade : String = ""
    var maxStorage : Long = 0L

    var isAuthenticated = (id != "" && grade != "" && maxStorage != 0L )
}




