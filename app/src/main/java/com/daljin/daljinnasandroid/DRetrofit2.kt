package com.daljin.daljinnasandroid

import android.content.Context
import android.net.Uri
import android.util.Log
import okhttp3.*
import okio.BufferedSink
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.URLEncoder


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

    @FormUrlEncoded
    @POST("/makeDirectoryNW")
    fun mkdir(@Field("n_makeDirectory_path") path : String , @Field("n_makeDirectory_Name") name : String) : Call<String>

    @FormUrlEncoded
    @POST("/DeleteNW")
    fun remove(@Field("n_deletePath") path : String , @Field("n_deleteList") list : String) : Call<String> // list = { i : {type : "" , name : ""} }

    @FormUrlEncoded
    @POST("/checkid")
    fun checkId(@Field("ID") ID : String) : Call<String>

    @FormUrlEncoded
    @POST("/adduserNW")
    fun addUser(@Field("ID") ID : String , @Field("PW") PW : String , @Field("CODE") CODE: String) : Call<String>


    @Multipart
    @POST("/fileUpload")
    fun upload(@Part("n_upload_path") path : String , @Part file : MultipartBody.Part) : Call<String>
}

//세션 유지를 위한 쿠키 헤더 추가
class AddCookiesInterceptor(val context : Context) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        // Preference에서 cookies를 가져오는 작업을 수행
        val sharedPreferences = context.getSharedPreferences(SP_NAME , Context.MODE_PRIVATE)
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
                 val sharedPreferences = context.getSharedPreferences(SP_NAME , Context.MODE_PRIVATE)
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
        //.baseUrl("http://10.0.2.2:8000")
        .baseUrl("http://daljin.dlinkddns.com") // 릴리즈 용
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

    fun Logout() {
        id = ""
        grade = ""
        maxStorage = 0
        isAuthenticated = false
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

fun DaljinNodeWebMkdir(context : Context , path : String , name : String , callback : (Boolean)->Unit) {
    DRetrofit(context).mkdir(path , name).enqueue(object : Callback<String> {
        override fun onFailure(call: Call<String>, t: Throwable) {
            callback.invoke(false)
        }
        override fun onResponse(call: Call<String>, response: retrofit2.Response<String>) {
            if(response.isSuccessful) {
                val parser = JSONObject(response.body())
                if(parser.getBoolean("error")) {
                    callback.invoke(false)
                }
                else {
                    callback.invoke(true)
                }
            }
        }
    })
}

fun DaljinNodeWebRemove(context : Context , path : String , list : List<Pair<String , String>> , callback : (Boolean)->Unit ) {

    val sb = StringBuilder()
    sb.append("[ ")
    for(i in 0 until list.size) {
        sb.append( "{ \"type\" : \"${list[i].first}\" , \"name\" : \"${list[i].second}\" }" )
        if(i != list.size-1) {
            sb.append(" , ")
        }
    }
    sb.append(" ]")

    Log.d("DALJIN" , sb.toString())
    DRetrofit(context).remove(path , sb.toString()).enqueue(object : Callback<String> {
        override fun onFailure(call: Call<String>, t: Throwable) {
            callback.invoke(false)
        }

        override fun onResponse(call: Call<String>, response: retrofit2.Response<String>) {
            if(response.isSuccessful) {
                callback.invoke(true)
            }
        }
    })
}

fun DaljinNodeWebLogout(context : Context , callback : (Boolean)->Unit) {
    DRetrofit(context).logout().enqueue(object : Callback<String> {
        override fun onFailure(call: Call<String>, t: Throwable) {
            callback(false)
        }

        override fun onResponse(call: Call<String>, response: retrofit2.Response<String>) {
            if (response.isSuccessful) {
                var parser = JSONObject(response.body())
                when (parser.getBoolean("error")) {
                    true -> callback(false)
                    false -> {
                        DaljinNodeWebLoginData.Logout()
                        callback(true)
                    }
                }
            }
        }
    })
}

fun DaljinNodeWebCheckId(context : Context , ID : String , callback : (Boolean)->Unit) {
    DRetrofit(context).checkId(ID).enqueue(object :  Callback<String> {
        override fun onFailure(call: Call<String>, t: Throwable) {
            callback.invoke(false)
        }

        override fun onResponse(call: Call<String>, response: retrofit2.Response<String>) {
            if(response.isSuccessful) {
                callback.invoke(JSONObject(response.body()).getBoolean("result"))
            }
        }
    })
}

fun DaljinNodeWebSignup(context : Context , ID : String , PW : String , CODE : String , callback : (Boolean , String)->Unit) {
    DRetrofit(context).addUser(ID , PW , CODE).enqueue(object : Callback<String> {
        override fun onFailure(call: Call<String>, t: Throwable) {
            callback.invoke(false , "서버와 연결 불가")
        }

        override fun onResponse(call: Call<String>, response: retrofit2.Response<String>) {
            if(response.isSuccessful) {
                val parser = JSONObject(response.body())
                callback.invoke(!parser.getBoolean("error") , parser.getString("msg"))
            }
        }
    })
}

fun DaljinNodeWebUpload(context : Context , uploadPath : String , filePathNName : String , progressCallback: ((Int) -> Unit)? = null , uploadEndCallback : ((Boolean , String)->Unit)? = null) {
    val file = File(filePathNName)
    val fileUri = Uri.fromFile(file)
    val fileBody = ProgressRequestBody(file, context.contentResolver.getType(fileUri) , progressCallback)
    val filePart = MultipartBody.Part.createFormData("n_upload_files" , URLEncoder.encode(file.name , "UTF-8") , fileBody)


    DRetrofit(context).upload(uploadPath , filePart).enqueue(object : Callback<String> {
        override fun onFailure(call: Call<String>, t: Throwable) {
            uploadEndCallback?.invoke(false , "서버와 연결이 불가합니다.")
        }

        override fun onResponse(call: Call<String>, response: retrofit2.Response<String>) {
            if(response.isSuccessful) {
                uploadEndCallback?.invoke(true , response.body().toString())
            }
        }
    })
}

class ProgressRequestBody(var file : File , var contentType : String? ,  var callback : ((Int)->Unit)?) : RequestBody(){

    override fun writeTo(sink: BufferedSink) {
        val fis = FileInputStream(file)
        val buffer = ByteArray(8192)
        var read : Int

        while(true) {
            read = fis.read(buffer)

            if(read == -1) {
                break
            }

            sink.write(buffer , 0 , read)
            callback?.invoke(read)
        }
        fis.close()
    }

    override fun contentType(): MediaType? = MediaType.parse("$contentType/*")
    override fun contentLength(): Long = file.length()
}


