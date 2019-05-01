package com.example.daljin.daljinnasandroid

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import java.io.IOException


interface DRetrofitInterface {
    @FormUrlEncoded
    @POST("/loginNW")
    fun login(@Field("ID") ID : String , @Field("PW") PW : String) : Call<String>

    @FormUrlEncoded
    @POST("/fileList")
    fun getFileList(@Field("path") path : String) : Call<String>

    @POST("/logoutNW")
    fun logout() : Call<String>

    @FormUrlEncoded
    @POST("/Download")
    fun download(@Field("n_itemPath") path : String , @Field("n_downloadItem") downloadItem : String , @Field("n_itemType") type : String) : Call<ResponseBody>
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
                 //download시 cookie header 값 변경되므로 다운로드시 제외
                 if(header.indexOf("fileDownload" , 0) == -1) {
                     cookies.add(header)
                 }
             }

             if(cookies.size !=0) {
                 // Preference에 cookies를 넣어주는 작업을 수행
                 val sharedPreferences = context.getSharedPreferences("DaljinNAS" , Context.MODE_PRIVATE)
                 val editor = sharedPreferences.edit()
                 editor.putStringSet("Cookie" , cookies)
                 editor.commit()
             }
         }
         return originalResponse
     }
 }

//레트로핏 불러오기
fun DRetrofit(context : Context) : DRetrofitInterface
{
    val client = OkHttpClient().newBuilder()
        .addNetworkInterceptor(AddCookiesInterceptor(context))
        .addNetworkInterceptor(ReceivedCookiesInterceptor(context))
        .build()


    return Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8000")
        //.baseUrl("http://daljin.dlinkddns.com") // 릴리즈 용
        .client(client)
        .addConverterFactory(ScalarsConverterFactory.create())
        //.addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(DRetrofitInterface::class.java)
}

//연결 관리 데이터
object DaljinNodeWebLoginData {
    var id : String = ""
    var grade : String = ""
    var maxStorage : Long = 0L
    var isAuthenticated = false

    fun Authenticate(id : String = "", grade : String = "" , maxStorage : Long = 0) : Boolean{
        if(id != "" && grade != "" && maxStorage > 0) {
            this.id = id
            this.grade = grade
            this.maxStorage = maxStorage
            isAuthenticated = true
        }
        else {
            isAuthenticated = false
        }
        return isAuthenticated
    }
}

//로그인 요청
fun DaljinNodeWebLogin(context : Context, ID : String = "", PW : String = "" , callback : (Boolean , String?)-> Unit) {
    DRetrofit(context).login(ID , PW).enqueue(object : Callback<String> {
        override fun onFailure(call: Call<String>, t: Throwable) {
            DaljinNodeWebLoginData.Authenticate()
            callback.invoke(false , null)
        }
        override fun onResponse(call: Call<String>, response: retrofit2.Response<String>) {
            if(response.isSuccessful) {
                val parser = JSONObject(response.body())
                when(parser.getBoolean("error")){
                    true -> {
                        DaljinNodeWebLoginData.Authenticate()
                        callback.invoke(true , null)
                    }
                    false -> {
                        DaljinNodeWebLoginData.Authenticate(parser.getString("id") , parser.getString("grade") , parser.getLong("max_storage"))
                        callback.invoke(true , response.body())
                    }
                }
            }
        }
    })
}

//파일 리스트 얻기
fun DaljinNodeWebGetFileList(context : Context, path : String = "", callback: (Boolean, String?) -> Unit) {
    DRetrofit(context).getFileList(path).enqueue(object : Callback<String> {
        override fun onFailure(call: Call<String>, t: Throwable) {
            callback.invoke(false , null)
        }

        override fun onResponse(call: Call<String>, response: retrofit2.Response<String>) {
            if(response.isSuccessful) {
                callback.invoke(true , response.body())
            }
        }
    })
}

fun DaljinNodeWebDownload(context : Context , pathNitem : String , type : String , callback: (Boolean , ResponseBody?)->Unit) {
    val path = pathNitem.substringBeforeLast('/')
    val filename = pathNitem.substringAfterLast('/')
    DaljinNodeWebDownload(context , path , filename , type , callback)
}

//다운로드 요청
fun DaljinNodeWebDownload(context : Context , path : String , item : String , type : String , callback: (Boolean , ResponseBody?)->Unit) {
    DRetrofit(context).download(path , item , type).enqueue(object : Callback<ResponseBody> {
        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            callback(false , null)
        }

        override fun onResponse(call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>) {
            if(response.isSuccessful) {
               callback(true , response.body())
            }
        }
    })
}



