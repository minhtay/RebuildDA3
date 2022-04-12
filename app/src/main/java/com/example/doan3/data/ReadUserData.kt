package com.example.doan3.data

data class ReadUserData(
    val dateCreate: Long,
    val dateUpdate: Long,
    val userId: String? = null,
    val userName: String? = null,
    val userAvatar: String? = null,
    val userEmail: String? = null,
    val userPhoneNumber: String? = null,
    val userBirthday: String? = null,
    val userAddress: String? = null
)