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
                { res , body ->
                    if(res) {
                        setResult(RESULT_OK)
                        finish()
                    }
                    else {
                        Toast.makeText(this@LoginActivity , "서버와 통신 불가" , Toast.LENGTH_SHORT)
                    }
                }
        }
        btn_Signup.setOnClickListener {  } //미구현
    }
}




