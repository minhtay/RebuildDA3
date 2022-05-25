package com.example.doan3.view.acticity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.doan3.R
import com.example.doan3.data.ReadPost
import com.example.doan3.data.ReadUser
import com.example.doan3.data.UpNofication
import com.example.doan3.data.UploadPost
import com.example.doan3.databinding.ActivitySharePostBinding
import com.example.doan3.util.NoficationClass
import com.example.doan3.util.Utils
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timerTask

class SharePostActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySharePostBinding
    private lateinit var mAuth: FirebaseAuth
    private var idPost: String? = null
    private var uID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySharePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        val uid = mAuth.currentUser!!.uid
        LoadUser(uid, binding.imvAvatar, binding.tvName)

        binding.scrollView2.setOnClickListener{Utils.hideSoftKeyboard(this@SharePostActivity,binding.root)
        }


        // lấy idPost từ intent
        idPost = intent.getStringExtra("idPost")
        Log.d("idPost", idPost.toString())

        // lấy thông tin từ post
        val fDatabase = FirebaseDatabase.getInstance().getReference("Post")
        val postList = ArrayList<ReadPost>()
        fDatabase.orderByChild("idPost").equalTo(idPost)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (uSnapshot in snapshot.children) {
                            val data = uSnapshot.getValue(ReadPost::class.java)
                            postList.add(data!!)
                            binding.tvTitle.text = postList[0].title
                            Glide.with(binding.root).load(postList[0].photo).into(binding.ivPhoto)
                            val dateCreate = postList[0].dateCreate!!
                            LoadDateCreate(dateCreate, binding.tvDateCreate)
                            uID = postList[0].idUser
                            LoadUser(
                                postList[0].idUser.toString(),
                                binding.imvAvatar1,
                                binding.tvName1
                            )
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}

            })

        binding.btnPost.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val id =  UUID.randomUUID().toString()
                var data = UploadPost(
                    id,
                    "Share",
                    idPost,
                    mAuth.currentUser!!.uid,
                    binding.edtTitle.text.toString(),
                    null,
                    ServerValue.TIMESTAMP,
                    ServerValue.TIMESTAMP
                )
                val ref = FirebaseDatabase.getInstance().getReference("Post")
                ref.child(id).setValue(data).addOnSuccessListener {
                    Utils.hideSoftKeyboard(this@SharePostActivity,binding.root)
                    Snackbar.make(
                        binding.root,
                        "Successful post sharing",
                        Snackbar.LENGTH_LONG
                    ).show()
                    Timer().schedule(timerTask {
                        finish()
                    },3000)
                    Nofication()
                }.addOnFailureListener{
                    Utils.hideSoftKeyboard(this@SharePostActivity,binding.root)
                    Snackbar.make(
                        binding.root,
                        "Post sharing failed",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }

        })

        binding.btnBack.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                finish()
            }

        })

    }

    private fun LoadUser(uid: String, avatar: CircleImageView, name: TextView) {
        // lấy avatar current user
        val ref = FirebaseDatabase.getInstance().getReference("User")
        val profileList = ArrayList<ReadUser>()
        ref.orderByChild("userId").equalTo(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (uSnapshot in snapshot.children) {
                            val data = uSnapshot.getValue(ReadUser::class.java)
                            profileList.add(data!!)
                            Glide.with(binding.root).load(profileList[0].userAvatar)
                                .into(avatar)
                            name.setText(profileList[0].userName.toString())

                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("DatabaseError", error.message)
                }

            })
    }

    private fun LoadDateCreate(date: Long?, tvDateCreate: TextView) {
        val format = SimpleDateFormat("dd/MM/yyyy")
        tvDateCreate.text = format.format(date)
    }
    private fun Nofication() {
        val id = UUID.randomUUID().toString()
        val data = UpNofication(id,uID,mAuth.currentUser!!.uid,"shared your post",false,"Share",ServerValue.TIMESTAMP,ServerValue.TIMESTAMP)
        NoficationClass().UpNofication(data)
    }

}