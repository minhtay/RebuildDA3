package com.example.doan3.data

data class UpComment(
    val idComment: String? = null,
    val idPost: String? = null,
    val idUser: String? = null,
    val comment: String? = null,
    val dateCreate: MutableMap<String, String>? = null,
    val dateUpdate: MutableMap<String, String>? = null
)

