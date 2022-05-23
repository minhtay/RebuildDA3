package com.example.doan3.view.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doan3.R
import com.example.doan3.adapter.PostAdapter
import com.example.doan3.data.ReadPost
import com.example.doan3.databinding.FragmentHomeBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = activity as Context

        binding.rcvHome.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        (binding.rcvHome.layoutManager as LinearLayoutManager).reverseLayout = true
        (binding.rcvHome.layoutManager as LinearLayoutManager).stackFromEnd = true
        binding.rcvHome.setHasFixedSize(true)
        val fDatabase = FirebaseDatabase.getInstance().getReference("Post")
        fDatabase.orderByChild("dateCreate").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val postList = ArrayList<ReadPost>()
                if (snapshot.exists()) {
                    for (pSnapshot in snapshot.children) {
                        val data = pSnapshot.getValue(ReadPost::class.java)
                        postList.add(data!!)
                    }
                }
                binding.rcvHome.adapter = PostAdapter(activity, postList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DataPost", error.message)
            }

        })

    }

}