package com.example.doan3.view.acticity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.doan3.R
import com.example.doan3.adapter.PostAdapter
import com.example.doan3.data.ReadPost
import com.example.doan3.data.ReadUser
import com.example.doan3.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.ArrayList

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var fAuth: FirebaseAuth
    private var idUser : String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idUser = intent.getStringExtra("idUser")

        fAuth = FirebaseAuth.getInstance()
        LoadDataUser()
        LoadDataPost()

        binding.btnBack.setOnClickListener(object :View.OnClickListener{
            override fun onClick(p0: View?) {
                finish()

            }
        })
        if (idUser!= fAuth.currentUser!!.uid){
            binding.btnEditProfile.visibility = View.GONE
        }
        binding.btnEditProfile.setOnClickListener(object :View.OnClickListener{
            override fun onClick(p0: View?) {
                val intent = Intent(this@ProfileActivity,EditprofileActivity::class.java)
                intent.putExtra("idUser",idUser)
                startActivity(intent)
                finish()
            }
        })
    }

    private fun LoadDataPost() {
        binding.rcvProfile.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        (binding.rcvProfile.layoutManager as LinearLayoutManager).reverseLayout = true
        (binding.rcvProfile.layoutManager as LinearLayoutManager).stackFromEnd = true
        binding.rcvProfile.setHasFixedSize(true)
        val fDatabase = FirebaseDatabase.getInstance().getReference("Post")
        fDatabase.orderByChild("idUser").equalTo(idUser).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val postList = ArrayList<ReadPost>()
                if (snapshot.exists()) {
                    for (pSnapshot in snapshot.children) {
                        val data = pSnapshot.getValue(ReadPost::class.java)
                        postList.add(data!!)
                    }
                }
                val list = ArrayList(postList.sortedBy { it.dateCreate })
                binding.rcvProfile.adapter = PostAdapter(this@ProfileActivity, list)
                binding.tvPostNumber.text = list.size.toString()+" Post"
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DataPost", error.message)
            }

        })


    }

    private fun LoadDataUser() {
        val profileList = ArrayList<ReadUser>()
        val fDatabase = FirebaseDatabase.getInstance().getReference("User")
        fDatabase.orderByChild("userId").equalTo(idUser)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (uSnapshot in snapshot.children) {
                            val data = uSnapshot.getValue(ReadUser::class.java)
                            profileList.add(data!!)
                        }
                        Glide.with(applicationContext).load(profileList[0].userAvatar)
                            .into(binding.imvAvatar)
                        binding.tvFullName.text = profileList[0].userName
                        binding.appBar.text = profileList[0].userName
                        binding.tvBio.text = profileList[0].bio


                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    buildDialog(
                        tittle = "Error",
                        mess = "An error has occurred during data processing. Please exit and restart the app"
                    )
                }

            })
    }

    private fun buildDialog(tittle: String, mess: String): AlertDialog.Builder? {
        val builder = AlertDialog.Builder(binding.root.context)
        builder.setTitle(tittle)
        builder.setMessage(mess)
        return builder
    }

}