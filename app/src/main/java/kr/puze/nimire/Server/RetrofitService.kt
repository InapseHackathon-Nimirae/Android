package kr.puze.nimire.Server

import kr.puze.nimire.Data.Account
import kr.puze.nimire.Data.All
import kr.puze.nimire.Data.User
import retrofit2.http.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Multipart



interface RetrofitService {
    @GET("/login")
    fun login_facebook(@Query("access_token") token: String): retrofit2.Call<User>

    @GET("/call")
    fun call(@Query("access_token") token: String): retrofit2.Call<All>

    @Multipart
    @POST("/upload")
    fun upload(
            @Part("access_token") token: RequestBody,
            @Part file: MultipartBody.Part
    ): Call<All>
}