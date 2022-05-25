package com.example.doan3.view.acticity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.doan3.adapter.CommentAdapter
import com.example.doan3.data.ReadCommennt
import com.example.doan3.data.ReadUser
import com.example.doan3.data.UpComment
import com.example.doan3.data.UpNofication
import com.example.doan3.databinding.ActivityCommentBinding
import com.example.doan3.util.NoficationClass
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList

class CommentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommentBinding
    private lateinit var mAuth: FirebaseAuth
    private var idPost: String? = null
    private var uID: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        uID = intent.getStringExtra("idUser")
        binding.btnBack.setOnClickListener { finish() }

        val ref = FirebaseDatabase.getInstance().getReference("User")
        val profileList = ArrayList<ReadUser>()
        ref.orderByChild("userId").equalTo(mAuth.currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (uSnapshot in snapshot.children) {
                            val data = uSnapshot.getValue(ReadUser::class.java)
                            profileList.add(data!!)
                            Glide.with(binding.root).load(profileList[0].userAvatar)
                                .into(binding.imAvatar)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("DatabaseError", error.message)
                }

            })


        idPost = intent.getStringExtra("idPost")
        binding.btnPostComment.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                if (!binding.tvComment.text.isEmpty()) {
                    val id = UUID.randomUUID().toString()
                    val data = UpComment(
                        id,
                        idPost,
                        mAuth.currentUser!!.uid,
                        binding.tvComment.text.toString(),
                        ServerValue.TIMESTAMP,
                        ServerValue.TIMESTAMP
                    )
                    val fDatabase =
                        FirebaseDatabase.getInstance().getReference("Comment").child(idPost!!)
                    fDatabase.child(id).setValue(data).addOnSuccessListener {
                        Snackbar.make(
                            binding.root,
                            "Add new comment uccess",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        com.example.doan3.util.Utils.hideSoftKeyboard(this@CommentActivity,binding.root)
                        binding.tvComment.text.clear()
                        Nofication()
                    }.addOnFailureListener {
                        Snackbar.make(
                            binding.root,
                            "Add new comment failed",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }else Snackbar.make(
                    binding.root,
                    "Please enter comment",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        })


        binding.rcvComment.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        (binding.rcvComment.layoutManager as LinearLayoutManager).reverseLayout = true
        (binding.rcvComment.layoutManager as LinearLayoutManager).stackFromEnd = true
        binding.rcvComment.setHasFixedSize(true)
        val fDatabase = FirebaseDatabase.getInstance().getReference("Comment")
        fDatabase.child(idPost!!).orderByChild("dateCreate").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val commentList = ArrayList<ReadCommennt>()
                if (snapshot.exists()) {
                    for (pSnapshot in snapshot.children) {
                        val data = pSnapshot.getValue(ReadCommennt::class.java)
                        commentList.add(data!!)
                    }
                }
                binding.rcvComment.adapter = CommentAdapter(this@CommentActivity, commentList)
                Log.d("commentList", commentList.size.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Database", error.message)
            }

        })

    }

    private fun Nofication() {
        val id = UUID.randomUUID().toString()
        val data = UpNofication(id,uID,mAuth.currentUser!!.uid,"commented your post",false,"Comment",ServerValue.TIMESTAMP,ServerValue.TIMESTAMP)
        NoficationClass().UpNofication(data)
    }


}