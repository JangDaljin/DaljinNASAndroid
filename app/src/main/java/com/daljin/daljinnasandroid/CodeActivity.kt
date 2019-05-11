package com.daljin.daljinnasandroid

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_code.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CodeActivity : AppCompatActivity() {

    private var CODE_isChecked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_code)

        val wrongColor = ContextCompat.getColor(this@CodeActivity , R.color.wrong)
        val rightColor = ContextCompat.getColor(this@CodeActivity , R.color.right)


        EdtCode.apply {
            setOnFocusChangeListener { v, hasFocus ->
                if(!hasFocus) {
                    if(!EdtCode.text.toString().isNullOrBlank()) {
                        CODE_isChecked = true
                        TvCode.setTextColor(rightColor)
                    }
                    else {
                        CODE_isChecked = false
                        TvCode.setTextColor(wrongColor)
                    }
                }
            }
            setOnEditorActionListener {
                    v, actionId, event ->
                when(actionId) {
                    EditorInfo.IME_ACTION_DONE -> {

                        if(!EdtCode.text.toString().isNullOrBlank()) {
                            CODE_isChecked = true
                            TvCode.setTextColor(rightColor)
                            codeSubmit.performClick()
                        }
                        else {
                            CODE_isChecked = false
                            TvCode.setTextColor(wrongColor)
                        }
                    }
                    else -> {
                        return@setOnEditorActionListener false
                    }
                }
                return@setOnEditorActionListener true
            }
        }

        codeSubmit.setOnClickListener {
            if(CODE_isChecked) {
                DaljinNodeWebUserInfoUpdate(this@CodeActivity , "" , EdtCode.text.toString()) {
                    result->
                    if(result) {
                        Toast.makeText(this@CodeActivity , "인증 성공" , Toast.LENGTH_SHORT).show()
                        GlobalScope.launch {
                            delay(500)
                            setResult(RESULT_CODE_UPDATE)
                            finish()
                        }
                    }
                    else {
                        Toast.makeText(this@CodeActivity , "인증 실패" , Toast.LENGTH_SHORT).show()
                        EdtCode.text.clear()
                    }
                }
            }
            else {
                Toast.makeText(this@CodeActivity , "코드를 입력해주세요" , Toast.LENGTH_SHORT).show()
            }
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

