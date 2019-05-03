package com.example.daljin.daljinnasandroid

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        edt_ID.setOnEditorActionListener {
            textView , actionId , keyEvent ->
            when(actionId) {
                EditorInfo.IME_ACTION_NEXT -> {
                    edt_PW.requestFocus()
                }
                else -> {
                    Log.d("DALJIN" , "$actionId")
                    return@setOnEditorActionListener false
                }
            }
            return@setOnEditorActionListener true
        }

        edt_PW.setOnEditorActionListener {
            v, actionId, event ->
            when(actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    btn_Login.performClick()
                }
                else -> {
                    return@setOnEditorActionListener false
                }
            }
            return@setOnEditorActionListener true
        }

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
        btn_Signup.setOnClickListener {




        }
    }
}




