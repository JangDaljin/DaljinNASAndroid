package com.example.daljin.daljinnasandroid

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class DSuccess(var RESULT : Boolean)
class DLoginInformation(val ID : String, val PW : String)

data class DUser(val id  : String , val max_storage : Int , val grade : String)



interface DRetrofitInterface {
    @POST("/loginNW")
    fun login(@Body info : DLoginInformation) : Call<DSuccess>

}


object DRetrofitController {
    private const val URL = "http://10.0.2.2:8000"

    private val retrofit = Retrofit.Builder()
                        .baseUrl(URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()

    val Instance = retrofit.create(DRetrofitInterface::class.java)
}