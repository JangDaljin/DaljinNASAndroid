package com.daljin.daljinnasandroid

import android.content.Context
import android.net.Uri
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

    @GET(URL_NAVER_LOGIN)
    fun naverlogin(@Query(NAVER_TOKEN) token : String) : Call<String>

    @GET(URL_SESSION_CHECK)
    fun sessioncheck() : Call<String>

    @FormUrlEncoded
    @POST(URL_FILELIST)
    fun getFileList(@Field(FORM_NAME_FILELIST_PATH) path : String) : Call<String>

    @POST(URL_LOGOUT)
    fun logout() : Call<String>

    @FormUrlEncoded
    @POST(URL_DOWNLOAD)
    fun download(@Field(FORM_NAME_DOWNLOAD_PATH) path : String, @Field(FORM_NAME_DOWNLOAD_ITEM) downloadItem : String, @Field(FORM_NAME_DOWNLOAD_TYPE) type : String) : Call<ResponseBody>

    @FormUrlEncoded
    @POST(URL_MKDIR)
    fun mkdir(@Field(FORM_NAME_MKDIR_PATH) path : String , @Field(FORM_NAME_MKDIR_NAME) name : String) : Call<String>

    @FormUrlEncoded
    @POST(URL_DELETE)
    fun remove(@Field(FORM_NAME_DELETE_PATH) path : String, @Field(FORM_NAME_DELETE_LIST) list : String) : Call<String> // list = { i : {type : "" , name : ""} }

    @FormUrlEncoded
    @POST(URL_USER_INFO_UPADTE)
    fun userInfoUpdate(@Field(FORM_NAME_USER_INFO_UPDATE_NICKNAME) nickname : String , @Field(FORM_NAME_USER_INFO_UPDATE_CODE) code: String) : Call<String>

    @Multipart
    @POST(URL_UPLOAD)
    fun upload(@Part(FORM_NAME_UPLOAD_PATH) path : String , @Part file : MultipartBody.Part) : Call<String>

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
                 editor.apply {
                     putStringSet("Cookie" , cookies)
                     commit()
                 }
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
        .baseUrl(SERVER_URL)    // 릴리즈용
        .client(client)
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
        .create(DRetrofitInterface::class.java)
}

//연결 관리 데이터
object DaljinNodeWebLoginData {
    var email : String = ""
    var nickname : String = ""
    var code : String = ""
    var grade : String = ""
    var maxStorage : Long = 0L
    var isAuthenticated = false

    fun Authenticate(email : String = "",  nickname : String = "" , code : String = "" , grade : String = "" , maxStorage : Long = 0L) : Boolean{
        //code는 NULL일 수도 있음 maxStorage도 0일 수 있음
        if(email != "" && nickname != ""  && grade != "") {
            this.email = email
            this.nickname = nickname
            this.code = code
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
        email = ""
        nickname = ""
        code = ""
        grade = ""
        maxStorage = 0
        isAuthenticated = false
    }
}
fun DaljinNodeWebNaverLogin(context : Context , token : String , callback : (Boolean)->Unit) {

    DRetrofit(context).naverlogin(token).enqueue(object : Callback<String> {
        override fun onFailure(call: Call<String>, t: Throwable) {
            DaljinNodeWebLoginData.Authenticate()
            callback.invoke(false)
        }
        override fun onResponse(call: Call<String>, response: retrofit2.Response<String>) {
            if(response.isSuccessful) {
                var parser = JSONObject(response.body())
                when(parser.getBoolean("result")) {
                    true -> {
                        DaljinNodeWebLoginData.Authenticate(
                            parser.getString("email"),
                            parser.getString("nickname"),
                            parser.getString("code"),
                            parser.getString("grade"),
                            parser.getLong("max_storage")
                        )
                        callback.invoke(true)
                    }
                    false -> {
                        DaljinNodeWebLoginData.Authenticate()
                        callback.invoke(false)
                    }
                }
            }
        }
    })
}


//네이버로그인 요청
fun DaljinNodeWebSessionCheck(context : Context, callback : (Boolean , String?)-> Unit) {
    DRetrofit(context).sessioncheck().enqueue(object : Callback<String> {
        override fun onFailure(call: Call<String>, t: Throwable) {
            DaljinNodeWebLoginData.Authenticate()
            callback.invoke(false , null)
        }
        override fun onResponse(call: Call<String>, response: retrofit2.Response<String>) {
            if(response.isSuccessful) {
                val parser = JSONObject(response.body())
                when(parser.getBoolean("result")){
                    true -> {
                        DaljinNodeWebLoginData.Authenticate(
                        parser.getString("email") ,
                        parser.getString("nickname") ,
                        parser.getString("code"),
                        parser.getString("grade") ,
                        parser.getLong("max_storage"))
                        callback.invoke(true , response.body()
                        )
                    }
                    false -> {
                        DaljinNodeWebLoginData.Authenticate()
                        callback.invoke(true , null)
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


fun DaljinNodeWebUserInfoUpdate(context : Context , nickname : String = "" , code : String , callback : (Boolean)->Unit) {
    DRetrofit(context).userInfoUpdate(nickname , code).enqueue(object : Callback<String> {
        override fun onFailure(call: Call<String>, t: Throwable) {
            callback.invoke(false)
        }

        override fun onResponse(call: Call<String>, response: retrofit2.Response<String>) {
            if(response.isSuccessful) {
                val parser = JSONObject(response.body())
                callback.invoke(!parser.getBoolean("error"))
            }
        }
    })
}

fun DaljinNodeWebUpload(context : Context , uploadPath : String , filePathNName : String , progressCallback: ((Int) -> Unit)? = null , uploadEndCallback : ((Boolean , String)->Unit)? = null) {
    val file = File(filePathNName)
    val fileUri = Uri.fromFile(file)
    val fileBody = ProgressRequestBody(file, context.contentResolver.getType(fileUri) , progressCallback)
    val filePart = MultipartBody.Part.createFormData(FORM_NAME_UPLOAD_FILES , URLEncoder.encode(file.name , "UTF-8").replace("+" , "%20") , fileBody) // 인코딩 변환(UTF 8 공백 Java는 '+' 그외 '%20'


    DRetrofit(context).upload(uploadPath , filePart).enqueue(object : Callback<String> {
        override fun onFailure(call: Call<String>, t: Throwable) {
            uploadEndCallback?.invoke(false , "서버와 연결이 불가합니다.")
        }

        override fun onResponse(call: Call<String>, response: retrofit2.Response<String>) {
            if(response.isSuccessful) {
                var parser = JSONObject(response.body())
                uploadEndCallback?.invoke(!parser.getBoolean("error") , parser.getString("msg"))
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


