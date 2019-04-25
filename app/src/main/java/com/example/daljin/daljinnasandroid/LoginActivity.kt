package com.example.daljin.daljinnasandroid

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btn_Login.setOnClickListener {
            Login(this@LoginActivity,
                edt_ID.text.toString(),
                edt_PW.text.toString()
                ) { setResult(AppCompatActivity.RESULT_OK)
                    finish() }
        }


//            DRetrofit(this@LoginActivity).login(edt_ID.text.toString() , edt_PW.text.toString()).enqueue(object :
//                Callback<String> {
//                    override fun onFailure(call: Call<String>, t: Throwable) {
//                    Toast.makeText(this@LoginActivity , "서버와 연결이 되지 않습니다." , Toast.LENGTH_LONG).show()
//                }
//
//                override fun onResponse(call: Call<String>, response: Response<String>) {
//                    Log.d("DALJIN" , response.body().toString())
//                    if(response.isSuccessful) {
//                        val parser = JSONObject(response.body())
//
//                        when(parser.getBoolean("error")){
//                            true -> Toast.makeText(this@LoginActivity , "아이디 또는 비밀번호가 틀렸습니다." , Toast.LENGTH_SHORT).show()
//                            false -> {
//                                DaljinNodeWebLoginData.id = parser.getString("id")
//                                DaljinNodeWebLoginData.grade = parser.getString("grade")
//                                DaljinNodeWebLoginData.maxStorage = parser.getInt("max_storage")
//                                setResult(RESULT_OK)
//                                finish()
//                            }
//                        }
//                    }
//
//                }
//            })
//        }

        btn_Signup.setOnClickListener {  }
    }
}

fun Login(context : Context, ID : String = "" , PW : String = "" , func : ()-> Unit) {
    DRetrofit(context).login(ID , PW).enqueue(object :
        Callback<String> {
        override fun onFailure(call: Call<String>, t: Throwable) {
            Toast.makeText(context , "서버와 연결이 되지 않습니다." , Toast.LENGTH_LONG).show()
        }

        override fun onResponse(call: Call<String>, response: Response<String>) {
            Log.d("DALJIN" , response.body().toString())
            if(response.isSuccessful) {
                val parser = JSONObject(response.body())

                when(parser.getBoolean("error")){
                    true -> Toast.makeText(context , "로그인 실패" , Toast.LENGTH_SHORT).show()
                    false -> {
                        DaljinNodeWebLoginData.id = parser.getString("id")
                        DaljinNodeWebLoginData.grade = parser.getString("grade")
                        DaljinNodeWebLoginData.maxStorage = parser.getInt("max_storage")
                        func()
                    }
                }
            }
        }
    })
}
