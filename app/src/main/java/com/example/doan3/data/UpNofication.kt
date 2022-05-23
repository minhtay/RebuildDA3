package com.example.doan3.data

data class UpNofication(
    val idUser: String? = null,
    val idNofication: String? = null,
    val message: String? = null,
    val status: Boolean,
    val dateCreate: MutableMap<String, String>? = null,
    val dateUpdate: MutableMap<String, String>? = null

)
