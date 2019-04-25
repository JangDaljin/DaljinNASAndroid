package com.example.daljin.daljinnasandroid

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
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
                DaljinNodeWebLogin(this@LoginActivity, edt_ID.text.toString(), edt_PW.text.toString())
                {
                    setResult(RESULT_OK)
                    finish()
                }
        }
        btn_Signup.setOnClickListener {  } //미구현
    }
}



//로그인 요청
fun DaljinNodeWebLogin(context : Context, ID : String = "", PW : String = "" , callback : (Boolean)-> Unit) {
    DRetrofit(context).login(ID , PW).enqueue(object : Callback<String> {
        override fun onFailure(call: Call<String>, t: Throwable) {
            Toast.makeText(context, "서버와 통신 불가", Toast.LENGTH_SHORT).show()
        }
        override fun onResponse(call: Call<String>, response: Response<String>) {
            if(response.isSuccessful) {
                val parser = JSONObject(response.body())

                when(parser.getBoolean("error")){
                    true -> {
                        Toast.makeText(context, "로그인 실패", Toast.LENGTH_SHORT).show()
                        callback.invoke(false)
                    }
                    false -> {
                        Toast.makeText(context, "로그인 성공", Toast.LENGTH_SHORT).show()
                        DaljinNodeWebLoginData.id = parser.getString("id")
                        DaljinNodeWebLoginData.grade = parser.getString("grade")
                        DaljinNodeWebLoginData.maxStorage = parser.getLong("max_storage")
                        callback.invoke(true)
                    }
                }
            }
        }
    })
}

