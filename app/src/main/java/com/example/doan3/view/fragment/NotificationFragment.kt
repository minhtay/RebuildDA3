package com.example.doan3.view.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doan3.adapter.NofiticationAdapter
import com.example.doan3.data.ReadNofication
import com.example.doan3.databinding.FragmentNotificationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotificationFragment : Fragment() {
    private lateinit var binding: FragmentNotificationBinding
    private lateinit var mAuth :FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNotificationBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = activity as Context

        mAuth = FirebaseAuth.getInstance()
        binding.rcvNofication.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
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
                binding.rcvNofication.adapter = NofiticationAdapter(activity, noficationList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DataPost", error.message)
            }

        })
    }

}