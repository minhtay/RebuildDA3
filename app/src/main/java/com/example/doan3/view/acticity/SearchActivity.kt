package com.example.doan3.view.acticity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doan3.adapter.SearchAdapter
import com.example.doan3.data.ReadUser
import com.example.doan3.databinding.ActivitySearchBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var fAuth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fAuth = FirebaseAuth.getInstance()

        binding.rcvSearch.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        binding.edtSearch.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                var text = binding.edtSearch.text
                if (text.isNotEmpty()){
                    binding.rcvSearch.visibility = View.VISIBLE
                    SearchUser(binding.edtSearch.text.toString())
                }else binding.rcvSearch.visibility = View.GONE

            }
            override fun afterTextChanged(p0: Editable?) {}

        })

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun SearchUser(userName: String) {
        val profileList = ArrayList<ReadUser>()
        val fDatabase = FirebaseDatabase.getInstance().getReference("User")
        fDatabase.orderByChild("userName").startAt(userName).endAt(userName + "\uf8ff")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (uSnapshot in snapshot.children) {
                            val data = uSnapshot.getValue(ReadUser::class.java)
                            profileList.add(data!!)
                        }
                        Log.d("tesstSearch",profileList.size.toString())
                    }
                    binding.rcvSearch.adapter = SearchAdapter(this@SearchActivity,profileList)
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }
}