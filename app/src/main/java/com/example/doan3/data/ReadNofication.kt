package com.example.doan3.data

data class ReadNofication(
    val idNofication: String? = null,
    val idUserReceive: String? = null,
    val idUserSend: String? = null,
    val message: String? = null,
    val status: Boolean?=false,
    val type : String?=null,
    val dateCreate: Long? = null,
    val dateUpdate: Long? = null
)
