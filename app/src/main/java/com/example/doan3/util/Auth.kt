package com.example.doan3.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object Auth {
    fun auth(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }
}