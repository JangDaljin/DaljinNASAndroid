package com.example.daljin.daljinnasandroid

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_Login.setOnClickListener {
            DRetrofitController.Instance.login(DLoginInformation(edt_ID.text.toString() , edt_PW.text.toString())).enqueue(object :
                Callback<DSuccess> {
                    override fun onFailure(call: Call<DSuccess>, t: Throwable) {
                    Toast.makeText(this@MainActivity , "서버와 연결이 되지 않습니다." , Toast.LENGTH_LONG).show()
                    Log.d("DALJIN" , t.message + "111" + t.localizedMessage)
                }

                override fun onResponse(call: Call<DSuccess>, response: Response<DSuccess>) {
                    Log.d("DALJIN" , response.body().toString())
                    if(response.isSuccessful) {
                        when(response.body()?.RESULT){
                            null -> Toast.makeText(this@MainActivity , "값이 없습니다." , Toast.LENGTH_SHORT).show()
                            false -> Toast.makeText(this@MainActivity , "아이디 또는 비밀번호가 틀렸습니다." , Toast.LENGTH_SHORT).show()
                            true -> startActivity(Intent(this@MainActivity , FileActivity::class.java))
                            //else -> Log.d("DALJIN" , "ERROR")
                        }
                    }

                }
            })
        }

        btn_Signup.setOnClickListener {  }
    }

    /*
    private fun getServerResponse() {
        var retrofit = Retrofit.Builder().baseUrl("http://10.0.2.2:8000")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        var retrofitInterface = retrofit.create(DRetrofitInterface::class.java)


        //Toast.makeText(this , "COM" , Toast.LENGTH_SHORT).show()


        retrofitInterface.getResult("HELLO").enqueue(object : Callback<DModel> {
            override fun onFailure(call: Call<DModel>, t: Throwable) {
                //tv_Content.text = "ERROR"

                Log.d("DALJIN" , "FAIL")
            }

            override fun onResponse(call: Call<DModel>, response: Response<DModel>) {
                if (response.isSuccessful) {
                    Log.d("Daljin" , response.body()?.result)
                }
            }
        })
    }
    */
}
