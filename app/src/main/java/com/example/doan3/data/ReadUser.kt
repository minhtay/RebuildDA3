package com.example.doan3.data

data class ReadUser(
    val dateCreate: Long?=0,
    val dateUpdate: Long?=0,
    val userId: String? = null,
    val userName: String? = null,
    val userAvatar: String? = null,
    val userEmail: String? = null
)