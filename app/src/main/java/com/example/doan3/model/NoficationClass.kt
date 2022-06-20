package com.example.doan3.model

import com.example.doan3.data.UpNofication
import com.google.firebase.database.FirebaseDatabase

class NoficationClass() {
    fun UpNofication(list: UpNofication){
        val fDatabase = FirebaseDatabase.getInstance().getReference("Nofication/${list.idUserReceive}")
        fDatabase.child(list.idNofication!!).setValue(list)
    }

}