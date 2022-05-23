package com.example.doan3.data

data class ReadPost(
    val idPost: String? = null,
    val typePost: String? = null,
    val idShare: String? = null,
    val idUser: String? = null,
    val title: String? = null,
    var photo: String? = null,
    val dateCreate: Long? = null,
    val dateUpdate: Long? = null
)
