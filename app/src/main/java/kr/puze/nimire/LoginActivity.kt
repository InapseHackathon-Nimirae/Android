package kr.puze.nimire

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.facebook.*
import com.facebook.login.LoginResult
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_login.*
import kr.puze.nimire.Data.User
import kr.puze.nimire.Server.RetrofitService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class LoginActivity : AppCompatActivity() {

    companion object {
        lateinit var token: String
        lateinit var call: Call<User>
        lateinit var retrofitService: RetrofitService
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        lateinit var editor: SharedPreferences.Editor
        lateinit var sharedPreferences: SharedPreferences
        lateinit var loginIntent: Intent
        lateinit var callbackManager: CallbackManager

    }

    @SuppressLint("CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_login)
        supportActionBar!!.hide()
        context = applicationContext
        loginIntent = Intent(this@LoginActivity, MainActivity::class.java)
        facebook_login.visibility = View.GONE

        sharedPreferences = getSharedPreferences("Login", 0)
        editor = sharedPreferences.edit()

        callbackManager = CallbackManager.Factory.create()!!

        retrofitSetting()

        facebook_login.setReadPermissions("email")
        facebook_login.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                val accessToken: AccessToken = result!!.accessToken
                token = accessToken.token
                Log.d("facebook_login", token)
                login(token)
                Toast.makeText(this@LoginActivity, "로그인 성공", Toast.LENGTH_SHORT).show();
            }

            override fun onCancel() {
                Log.d("facebook_login", "onCancel")
                Toast.makeText(this@LoginActivity, "로그인 취소", Toast.LENGTH_SHORT).show();
            }

            override fun onError(error: FacebookException?) {
                Log.d("facebook_login", "onError")
                error!!.printStackTrace()
                Toast.makeText(this@LoginActivity, "로그인 실패", Toast.LENGTH_SHORT).show();
            }
        })

        button_facebook_login.setOnClickListener {
            facebook_login.performClick()
        }
    }

    fun login(token: String) {
        Log.d("login_fun", "running")
        call = retrofitService.login_facebook(token)
        call.enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>?, response: Response<User>?) {
                Log.d("login_call", "onResponse")
                Log.d("login_token", token)
                if (response?.code() == 200) {
                    val user = response.body()
                    Log.d("response", user?.name)
                    if (user != null) {
                        editor.putString("name", user.name)
                        editor.putString("token", user.token)
                        editor.putBoolean("isUser", true)
                        editor.apply()
                        loginIntent.putExtra("name", user.name)
                        loginIntent.putExtra("token", user.token)
                        startActivity(loginIntent)
                        finish()
                    }
                } else if (response?.code() == 401) {
                    Log.d("login_call", "401")
                } else {
                    Log.d("login_call", "else")
                    Log.d("login_code", response!!.code().toString())
                }
            }

            override fun onFailure(call: Call<User>?, t: Throwable?) {
                Log.d("login_call", "onFailure")
                Log.d("login_call", t.toString())
            }
        })
    }

    private fun retrofitSetting() {
        val gson: Gson = GsonBuilder()
                .setLenient()
                .create()

        val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl("http://172.20.10.4:3000")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        retrofitService = retrofit.create(RetrofitService::class.java)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}