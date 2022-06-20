package com.example.example_learn.retrofit.model

data class LoginMessage(
    val access_token: String,
    val active: String,
    val api_status: Int,
    val membership: Boolean,
    val timezone: String,
    val user_id: String,
    val errors: Errors
)