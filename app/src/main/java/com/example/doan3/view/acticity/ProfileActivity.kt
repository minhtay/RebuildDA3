package com.example.doan3.view.acticity

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
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
    private var idUser: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idUser = intent.getStringExtra("idUser")

        fAuth = FirebaseAuth.getInstance()
        loadDataUser()
        loadDataPost()
        drawableFollow(idUser)
        loadFollower()
        loadFollowing()


        binding.btnBack.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                finish()
            }
        })
        if (idUser != fAuth.currentUser!!.uid) {
            binding.btnEditProfile.visibility = View.GONE
            binding.btnFollow.visibility = View.VISIBLE
        } else {
            binding.btnFollow.visibility = View.GONE
            binding.btnEditProfile.visibility = View.VISIBLE
        }
        binding.btnEditProfile.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                val intent = Intent(this@ProfileActivity, EditprofileActivity::class.java)
                intent.putExtra("idUser", idUser)
                startActivity(intent)
            }
        })

        binding.btnFollow.setOnClickListener { Follow() }


    }
    override fun onResume() {
        super.onResume()
            loadDataUser()
            loadDataPost()
    }

    private fun loadFollowing() {
        val fDatabase = FirebaseDatabase.getInstance().getReference("Following")
        fDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild(idUser!!)) {
                    binding.tvFollowing.text =
                        snapshot.child(idUser!!).childrenCount.toString() + " Following"
                } else {
                    binding.tvFollowing.text = " 0 Following"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun loadFollower() {
        val fDatabase = FirebaseDatabase.getInstance().getReference("Follower")
        fDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild(idUser!!)) {
                    binding.tvFollow.text =
                        snapshot.child(idUser!!).childrenCount.toString() + " Follower"
                } else {
                    binding.tvFollow.text = " 0 Follower"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun loadDataPost() {
        val postList = ArrayList<ReadPost>()
        binding.rcvProfile.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        (binding.rcvProfile.layoutManager as LinearLayoutManager).reverseLayout = true
        (binding.rcvProfile.layoutManager as LinearLayoutManager).stackFromEnd = true
        binding.rcvProfile.setHasFixedSize(true)
        val fDatabase = FirebaseDatabase.getInstance().getReference("Post")
        fDatabase.orderByChild("idUser").equalTo(idUser)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (pSnapshot in snapshot.children) {
                            val data = pSnapshot.getValue(ReadPost::class.java)
                            postList.add(data!!)
                        }
                        val list = ArrayList(postList.sortedBy { it.dateCreate })
                        binding.rcvProfile.adapter = PostAdapter(this@ProfileActivity, list)
                        binding.tvPostNumber.text = postList.size.toString() + " Post"
                    } else {
                        binding.tvPostNumber.text = "0 Post"
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DataPost", error.message)
                }

            })


    }

    private fun loadDataUser() {
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

    private fun drawableFollow(idUser: String?) {
        val fDatabase = FirebaseDatabase.getInstance().getReference("Following")
        fDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(fAuth.currentUser!!.uid).hasChild(idUser!!)) {
                    binding.btnFollow.setBackgroundColor(
                        ContextCompat.getColor(
                            this@ProfileActivity,
                            R.color.follow
                        )
                    )
                    binding.btnFollow.setText("Following")
                    binding.btnFollow.setTextColor(Color.WHITE)
                } else {
                    binding.btnFollow.setBackgroundColor(Color.WHITE)
                    binding.btnFollow.setText("Follow")
                    binding.btnFollow.setTextColor(Color.BLACK)

                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun Follow() {
        val fDatabase = FirebaseDatabase.getInstance().getReference("Following")
        fDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(fAuth.currentUser!!.uid).hasChild(idUser!!)) {
                    fDatabase.child(fAuth.currentUser!!.uid).child(idUser!!).removeValue()
                    DeleteFollower()
                } else {
                    fDatabase.child(fAuth.currentUser!!.uid).child(idUser!!).setValue(true)
                    AddFollower()
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })

    }

    private fun AddFollower() {
        val fDatabase1 = FirebaseDatabase.getInstance().getReference("Follower")
        fDatabase1.child(idUser!!).child(fAuth.currentUser!!.uid).setValue(true)

    }

    private fun DeleteFollower() {
        val fDatabase1 = FirebaseDatabase.getInstance().getReference("Follower")
        fDatabase1.child(idUser!!).child(fAuth.currentUser!!.uid).removeValue()
    }


}