package com.example.doan3.view.acticity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doan3.R
import com.example.doan3.adapter.NofiticationAdapter
import com.example.doan3.data.ReadNofication
import com.example.doan3.databinding.ActivityNoficationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NoficationActivity : AppCompatActivity() {

    private lateinit var binding : ActivityNoficationBinding
    private lateinit var mAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoficationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        mAuth = FirebaseAuth.getInstance()
        binding.rcvNofication.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        (binding.rcvNofication.layoutManager as LinearLayoutManager).reverseLayout = true
        (binding.rcvNofication.layoutManager as LinearLayoutManager).stackFromEnd = true
        binding.rcvNofication.setHasFixedSize(true)
        val fDatabase = FirebaseDatabase.getInstance().getReference("Nofication/${mAuth.currentUser!!.uid}")
        fDatabase.orderByChild("dateCreate").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val noficationList = ArrayList<ReadNofication>()
                if (snapshot.exists()) {
                    for (pSnapshot in snapshot.children) {
                        val data = pSnapshot.getValue(ReadNofication::class.java)
                        noficationList.add(data!!)
                    }
                }
                noficationList.removeAll { it.idUserReceive == it.idUserSend }
                binding.rcvNofication.adapter = NofiticationAdapter(this@NoficationActivity, noficationList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DataPost", error.message)
            }

        })
    }
}