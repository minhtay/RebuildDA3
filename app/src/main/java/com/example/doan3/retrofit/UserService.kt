package com.example.example_learn.retrofit.api

import com.example.example_learn.retrofit.model.LoginMessage
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface UserService {
    @FormUrlEncoded
    @POST("api/auth")
    fun login(@Field("server_key") server_key: String,
              @Field("username") username:String,
              @Field("password") password: String,): Call<LoginMessage>
}

