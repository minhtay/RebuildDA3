package com.example.doan3.data

data class UploadPost(
    val idPost: String? = null,
    val typePost: String? = null,
    val idShare: String? = null,
    val idUser: String? = null,
    val title: String? = null,
    val photo: String? = null,
    val dateCreate: MutableMap<String, String>? = null,
    val dateUpdate: MutableMap<String, String>? = null
)
