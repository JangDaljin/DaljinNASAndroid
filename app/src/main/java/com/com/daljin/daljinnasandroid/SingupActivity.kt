package com.daljin.daljinnasandroid

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_singup.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SingupActivity : AppCompatActivity() {

    private var ID_isChecked = false
    private var chckedID = ""
    private var PW_isChecked = false
    private var PWCheck_isChecked = false
    private var CODE_isChecked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_singup)

        val wrongColor = ContextCompat.getColor(this@SingupActivity , R.color.wrong)
        val rightColor = ContextCompat.getColor(this@SingupActivity , R.color.right)

        singupButtonIDcheck.apply {
            setOnClickListener {
                thisview ->
                DaljinNodeWebCheckId(this@SingupActivity, signupEditTextID.text.toString()) {
                    if (it) {
                        chckedID = signupEditTextID.text.toString()
                        ID_isChecked = true
                        signupTvID.setTextColor(rightColor)
                        Toast.makeText(this@SingupActivity, "사용 가능합니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        ID_isChecked = false
                        signupTvID.setTextColor(wrongColor)
                        Toast.makeText(this@SingupActivity, "사용 불가능한 아이디입니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            setOnFocusChangeListener { v, hasFocus ->
                if(!hasFocus) {
                    if(chckedID != signupEditTextID.text.toString()) {
                        ID_isChecked = false
                        signupTvID.setTextColor(wrongColor)
                    }
                }
            }

            setOnEditorActionListener { textView, actionId, keyEvent ->
                when (actionId) {
                    EditorInfo.IME_ACTION_NEXT -> {
                        signupEditTextPW.requestFocus()
                    }
                    else -> {
                        return@setOnEditorActionListener false
                    }
                }
                return@setOnEditorActionListener true
            }
        }

        signupEditTextPW.apply {
            setOnFocusChangeListener { v, hasFocus ->
                if(!hasFocus) {
                    if(Regex("^.*(?=^.{8,15}\$)(?=.*\\d)(?=.*[a-zA-Z])(?=.*[!@#\$%^&+=]).*\$").matches(signupEditTextPW.text.toString())) {
                        PW_isChecked = true
                        signupTvPW.setTextColor(rightColor)
                    }
                    else {
                        PW_isChecked = false
                        signupTvPW.setTextColor(wrongColor)
                    }
                }
            }
            setOnEditorActionListener { textView, actionId, keyEvent ->
                when (actionId) {
                    EditorInfo.IME_ACTION_NEXT -> {
                        signupEditTextPWcheck.requestFocus()
                    }
                    else -> {
                        return@setOnEditorActionListener false
                    }
                }
                return@setOnEditorActionListener true
            }
        }

        signupEditTextPWcheck.apply {
            setOnFocusChangeListener { v, hasFocus ->
                if(!hasFocus) {
                    if(signupEditTextPW.text.toString() == signupEditTextPWcheck.text.toString() && !signupEditTextPW.text.toString().isNullOrBlank() && !signupEditTextPWcheck.text.toString().isNullOrBlank()) {
                        PWCheck_isChecked = true
                        signupTvPWcheck.setTextColor(rightColor)
                    }
                    else {
                        PWCheck_isChecked = false
                        signupTvPWcheck.setTextColor(wrongColor)
                    }
                }
            }
            setOnEditorActionListener { textView, actionId, keyEvent ->
                when (actionId) {
                    EditorInfo.IME_ACTION_NEXT -> {
                        signupEditTextCode.requestFocus()
                    }
                    else -> {
                        return@setOnEditorActionListener false
                    }
                }
                return@setOnEditorActionListener true
            }
        }

        signupEditTextCode.apply {
            setOnFocusChangeListener { v, hasFocus ->
                if(!hasFocus) {
                    if(!signupEditTextCode.text.toString().isNullOrBlank()) {
                        CODE_isChecked = true
                        signupTvCode.setTextColor(rightColor)
                    }
                    else {
                        CODE_isChecked = false
                        signupTvCode.setTextColor(wrongColor)
                    }
                }
            }
            setOnEditorActionListener {
                    v, actionId, event ->
                when(actionId) {
                    EditorInfo.IME_ACTION_DONE -> {

                        if(!signupEditTextCode.text.toString().isNullOrBlank()) {
                            CODE_isChecked = true
                            signupTvCode.setTextColor(rightColor)
                            signupButtonSubmit.performClick()
                        }
                        else {
                            CODE_isChecked = false
                            signupTvCode.setTextColor(wrongColor)
                        }
                    }
                    else -> {
                        return@setOnEditorActionListener false
                    }
                }
                return@setOnEditorActionListener true
            }
        }

        signupButtonSubmit.setOnClickListener {
            if(!signupEditTextCode.text.toString().isNullOrBlank()) {
                CODE_isChecked = true
                signupTvCode.setTextColor(rightColor)
            }
            else {
                CODE_isChecked = false
                signupTvCode.setTextColor(wrongColor)
            }

            if(ID_isChecked && PW_isChecked && PWCheck_isChecked && CODE_isChecked) {
                DaljinNodeWebSignup(this@SingupActivity ,signupEditTextID.text.toString() , signupEditTextPW.text.toString() , signupEditTextCode.text.toString()) {
                    result , msg ->
                    Toast.makeText(this@SingupActivity , msg , Toast.LENGTH_SHORT).show()
                    if(result) {
                        GlobalScope.launch {
                            delay(1000)
                            finish()
                        }
                    }
                }
            }
            else {
                Toast.makeText(this@SingupActivity , "항목을 확인해주세요" , Toast.LENGTH_SHORT).show()
            }
        }




    }
}

