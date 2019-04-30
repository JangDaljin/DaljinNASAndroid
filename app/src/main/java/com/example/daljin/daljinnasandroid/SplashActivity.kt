package com.example.daljin.daljinnasandroid

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Thread().run{
            Thread.sleep(3000)
            startActivity(Intent(this@SplashActivity , FileActivity::class.java))
            finish()
        }
    }
}
