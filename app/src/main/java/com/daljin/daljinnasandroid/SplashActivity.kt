package com.daljin.daljinnasandroid

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        splashLogo.startAnimation(AnimationUtils.loadAnimation(this@SplashActivity , R.anim.showlogo))

        GlobalScope.launch {
            delay(2000)
            startActivity(Intent(this@SplashActivity , PerActivity::class.java))
            finish()
        }


        //startActivity(Intent(this@SplashActivity , TestActivity::class.java))
    }
}
