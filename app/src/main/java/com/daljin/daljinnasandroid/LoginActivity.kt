package com.daljin.daljinnasandroid

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    private val naverLoginHandler = NaverLoginHandler(this@LoginActivity )
    {
        if(it) {
            DaljinNodeWebNaverLogin(this@LoginActivity , NaverLoginInfo.accessToken as String) {
                if(it) {
                    setResult(RESULT_LOGIN)
                    finish()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        NaverLoginModule.init(this@LoginActivity , NAVER_CLIENT_ID , NAVER_CLIENT_SECRET , NAVER_CLIENT_NAME)
        btn_naverlogin.setOAuthLoginHandler(naverLoginHandler)

        btn_naverlogin.setOnClickListener {
            NaverLoginModule.startOauthLoginActivity(this@LoginActivity , naverLoginHandler)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when(event?.action) {
            KeyEvent.ACTION_DOWN -> {
                setResult(RESULT_FINISH)
            }
        }
        return super.onKeyDown(keyCode, event)
    }


}




