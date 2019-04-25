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
            DaljinNodeWebLogin(this@LoginActivity,
                edt_ID.text.toString(),
                edt_PW.text.toString()
                ) { setResult(AppCompatActivity.RESULT_OK)
                    finish() }
        }


        btn_Signup.setOnClickListener {  } //미구현
    }
}


