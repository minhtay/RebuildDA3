package com.example.doan3.view.acticity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.doan3.R
import com.example.doan3.adapter.CommentAdapter
import com.example.doan3.data.*
import com.example.doan3.databinding.ActivityPostBinding
import com.example.doan3.util.NoficationClass
import com.example.doan3.util.Utils
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import gun0912.tedimagepicker.builder.TedImagePicker
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timerTask

class PostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostBinding
    private lateinit var mAuth: FirebaseAuth
    private var filePath: Uri? = null
    private lateinit var postList: ArrayList<ReadPost>
    private var uID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostBinding.inflate(layoutInflater)
        setContentView(binding.root)


        mAuth = FirebaseAuth.getInstance()
        LoadUser(mAuth.currentUser!!.uid, binding.imvAvatar3, binding.tvName3)


        val idPost = intent.getStringExtra("idPost")

        val pDatabase = FirebaseDatabase.getInstance().getReference("Post")
        pDatabase.orderByChild("idPost").equalTo(idPost)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    postList = ArrayList<ReadPost>()
                    if (snapshot.exists()) {
                        for (uSnapshot in snapshot.children) {
                            val data = uSnapshot.getValue(ReadPost::class.java)
                            postList.add(data!!)
                            uID = postList[0].idUser
                            LoadUser(postList[0].idUser, binding.imvAvatar, binding.tvName)
                            Glide.with(binding.root).load(postList[0].photo)
                                .into(binding.imvPhoto)
                            if (postList[0].title == null) {
                                binding.edtTitle.visibility = View.GONE
                            } else {
                                binding.edtTitle.setText(postList[0].title)
                            }
                            if (mAuth.currentUser!!.uid != postList[0].idUser) {
                                binding.btnMenu.visibility = View.GONE
                            }

                            if (postList[0].typePost == "Post") {
                                binding.layoutShare.visibility = View.GONE


                            } else {
                                val pDatabase1 = FirebaseDatabase.getInstance().getReference("Post")
                                val postList1 = ArrayList<ReadPost>()
                                pDatabase1.orderByChild("idPost").equalTo(postList[0].idShare)
                                    .addValueEventListener(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            if (snapshot.exists()) {
                                                for (uSnapshot in snapshot.children) {
                                                    val data =
                                                        uSnapshot.getValue(ReadPost::class.java)
                                                    postList1.add(data!!)
                                                    LoadUser(
                                                        postList1[0].idUser,
                                                        binding.imvAvatar2,
                                                        binding.tvName2
                                                    )
                                                    binding.edtTitle2.setText(postList1[0].title)
                                                    Glide.with(binding.root)
                                                        .load(postList1[0].photo)
                                                        .into(binding.ivPhoto2)
                                                }
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            TODO("Not yet implemented")
                                        }

                                    })
                            }

                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

        binding.rcvCommentPostDetails.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        (binding.rcvCommentPostDetails.layoutManager as LinearLayoutManager).reverseLayout = true
        (binding.rcvCommentPostDetails.layoutManager as LinearLayoutManager).stackFromEnd = true
        binding.rcvCommentPostDetails.setHasFixedSize(true)

        val cDatabase = FirebaseDatabase.getInstance().getReference("Comment")
        cDatabase.child(idPost!!).orderByChild("dateCreate")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val commentList = ArrayList<ReadCommennt>()
                    if (snapshot.exists()) {
                        for (pSnapshot in snapshot.children) {
                            val data = pSnapshot.getValue(ReadCommennt::class.java)
                            commentList.add(data!!)
                        }
                    }
                    binding.rcvCommentPostDetails.adapter =
                        CommentAdapter(this@PostActivity, commentList)
                    Log.d("commentList p", commentList.size.toString())


                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Database", error.message)
                }

            })


        binding.btnBack.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                finish()
            }
        })


        // Lấy số like của bài viết
        LoadLikeNumber(idPost)
        // lấy số comment của bài viết
        LoadCommentNumber(idPost)
        // lấy số share của bài viết
        LoadShareNumber(idPost)


        // Xét sự kiện cho btn Like
        binding.btnLike.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                var like = true
                val fDatabase = FirebaseDatabase.getInstance().getReference("Like")
                fDatabase.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (like == true) {
                            if (snapshot.child(idPost!!).hasChild(mAuth.currentUser?.uid!!)) {
                                fDatabase.child(idPost).child(mAuth.currentUser!!.uid).removeValue()
                                like = false
                            } else {
                                fDatabase.child(idPost).child(mAuth.currentUser!!.uid)
                                    .setValue(true)
                                like = false
                                Nofication("liked your post", "Like")
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("DatabaseError", error.message)
                    }

                })
            }

        })



        binding.btnShare.setOnClickListener {
            val popupMenu = PopupMenu(binding.root.context, binding.btnShare)
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.shareNow -> {
                        Log.d("menuShare", "1")
                        val id = UUID.randomUUID().toString()
                        if (postList[0].typePost == "Post") {
                            var data = UploadPost(
                                id,
                                "Share",
                                idPost,
                                mAuth.currentUser?.uid,
                                null,
                                null,
                                ServerValue.TIMESTAMP,
                                ServerValue.TIMESTAMP
                            )
                            ShareNow(id, data)
                        } else {
                            var data = UploadPost(
                                id,
                                "Share",
                                postList[0].idShare,
                                mAuth.currentUser?.uid,
                                null,
                                null,
                                ServerValue.TIMESTAMP,
                                ServerValue.TIMESTAMP
                            )
                            ShareNow(id, data)


                        }
                    }

                    R.id.moreOption -> {
                        Log.d("menuShare", "2")
                        val intent =
                            Intent(this, SharePostActivity::class.java)
                        if (postList[0].typePost == "Post") {
                            intent.putExtra("idPost", idPost)
                        } else {
                            intent.putExtra("idPost", postList[0].idShare)
                        }
                        startActivity(intent)
                    }

                }
                false
            }
            popupMenu.inflate(R.menu.menu_post)
            popupMenu.gravity = Gravity.RIGHT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                popupMenu.setForceShowIcon(true)
            }
            popupMenu.show()
        }

        binding.btnPostComment.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (!binding.edtComment.text.isEmpty()) {
                    val id = UUID.randomUUID().toString()
                    val data = UpComment(
                        id,
                        idPost,
                        mAuth.currentUser!!.uid,
                        binding.edtComment.text.toString(),
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
                        Utils.hideSoftKeyboard(this@PostActivity, binding.root)
                        Nofication("commented your post", "Comment")
                        binding.edtComment.text.clear()
                    }.addOnFailureListener {
                        Snackbar.make(
                            binding.root,
                            "Add new comment failed",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                } else Snackbar.make(
                    binding.root,
                    "Please enter comment",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        })


        binding.cardView1.setOnClickListener {
            Utils.hideSoftKeyboard(
                this@PostActivity,
                binding.root
            )
        }


        binding.btnMenu.setOnClickListener(object : View.OnClickListener {
            @SuppressLint("NewApi")
            override fun onClick(v: View?) {
                val popupMenu = PopupMenu(binding.root.context, binding.btnMenu)
                popupMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.delete -> {
                            if (mAuth.currentUser!!.uid == postList[0].idUser) {
                                buildDialog(idPost)!!.show()


                            } else {
                                Snackbar.make(
                                    binding.root,
                                    "You cannot delete posts",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }

                        }
                        R.id.edit -> {
                            binding.edtTitle.requestFocus()
                            binding.btnMenu.isEnabled = false
                            if (mAuth.currentUser!!.uid == postList[0].idUser) {
                                binding.tvNumberLayout.visibility = View.GONE
                                binding.linearLayout3.visibility = View.GONE
                                binding.linearLayout4.visibility = View.GONE
                                binding.view.visibility = View.GONE
                                binding.view1.visibility = View.GONE
                                binding.linearLayout5.visibility = View.GONE
                                binding.layout12.visibility = View.VISIBLE
                                if (binding.edtTitle.length() > 0) {
                                    binding.edtTitle.isEnabled = true
                                } else {
                                    binding.edtTitle.visibility = View.VISIBLE
                                    binding.edtTitle.isEnabled = true
                                    binding.edtTitle.setHint("what are you thinking ?")
                                }
                                if (postList[0].typePost == "Post") {
                                    if (binding.imvPhoto.drawable == null) {
                                        binding.btnPickimage.visibility = View.VISIBLE
                                    } else {
                                        binding.btnClearImage.visibility = View.VISIBLE
                                    }
                                }
                            } else {
                                Snackbar.make(
                                    binding.root,
                                    "You cannot edit posts",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }

                        }
                    }
                    false
                }
                popupMenu.inflate(R.menu.menu_comment)
                popupMenu.gravity = Gravity.RIGHT
                popupMenu.setForceShowIcon(true)
                popupMenu.show()
            }
        })

        binding.btnCancel.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                binding.edtTitle.setText(postList[0].title)
                reloadUi()
            }

        })
        binding.btnPickimage.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                TedImagePicker.with(this@PostActivity).start { uri ->
                    Glide.with(binding.root).load(uri).into(binding.imvPhoto)
                    binding.btnClearImage.visibility = View.VISIBLE
                    binding.btnPickimage.visibility = View.GONE
                    filePath = uri
                    binding.layout12.visibility = View.VISIBLE
                    Log.d("filePath", filePath.toString())

                }
            }

        })
        binding.btnClearImage.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                binding.imvPhoto.setImageResource(0)
                binding.btnPickimage.visibility = View.VISIBLE
                binding.btnClearImage.visibility = View.GONE
                filePath = null
            }

        })
        binding.layout.setOnClickListener { Utils.hideSoftKeyboard(this, binding.root) }
        binding.btnUpdate.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                if (CheckValidUpadte()){
                    if (filePath != null) {
                        val text = binding.edtTitle.text.toString()
                        val data = UploadPost(
                            idPost,
                            postList[0].typePost,
                            postList[0].idShare,
                            postList[0].idUser,
                            text,
                            postList[0].photo,
                            ServerValue.TIMESTAMP,
                            ServerValue.TIMESTAMP
                        )
                        UpdatePost(idPost, data)
                        binding.edtTitle.setText(text)

                    } else {
                        deleteImage(idPost)
                    }
            }}
        })


    }

    private fun CheckValidUpadte(): Boolean {
        if (binding.edtTitle.text.isEmpty()) {
            Snackbar.make(
                binding.root,
                "TitLe not entered",
                Snackbar.LENGTH_SHORT
            ).show()
            return false
        }
        if (binding.imvPhoto.drawable == null){
            Snackbar.make(
                binding.root,
                "The post's photo has not been selected",
                Snackbar.LENGTH_SHORT
            ).show()
            return false
        }

        return true
    }

    private fun deleteImage(idPost: String) {
        val fStorage = FirebaseStorage.getInstance().getReference("Post")
        fStorage.child(idPost).delete().addOnSuccessListener {
            UploadImage(idPost)
        }
    }

    private fun UploadImage(idPost: String) {
        val text = binding.edtTitle.text.toString()

        val fStorage = FirebaseStorage.getInstance().getReference("Post/$idPost")
        fStorage.putFile(filePath!!).addOnSuccessListener {
            fStorage.downloadUrl.addOnSuccessListener {
                val url = it.toString()
                val data = UploadPost(
                    idPost,
                    postList[0].typePost,
                    postList[0].idShare,
                    postList[0].idUser,
                    text,
                    url,
                    ServerValue.TIMESTAMP,
                    ServerValue.TIMESTAMP
                )
                UpdatePost(idPost, data)
                binding.edtTitle.setText(text)
                reloadUi()
            }
        }
    }

    private fun UpdatePost(idPost: String, text: UploadPost) {
        val fDatabase = FirebaseDatabase.getInstance().getReference("Post")
        fDatabase.child(idPost).setValue(text)

    }

    private fun reloadUi() {
        binding.btnMenu.isEnabled = true
        binding.tvNumberLayout.visibility = View.VISIBLE
        binding.linearLayout3.visibility = View.VISIBLE
        binding.linearLayout4.visibility = View.VISIBLE
        binding.view.visibility = View.VISIBLE
        binding.view1.visibility = View.VISIBLE
        binding.linearLayout5.visibility = View.VISIBLE
        binding.layout12.visibility = View.GONE
        binding.edtTitle.setHint("")
        Glide.with(this@PostActivity).load(postList[0].photo).into(binding.imvPhoto)
        binding.btnClearImage.visibility = View.GONE
        binding.btnPickimage.visibility = View.GONE
        binding.edtTitle.isEnabled = false
        filePath = null
    }

    private fun LoadUser(idUser: String?, imvAvatar: CircleImageView, tvName: TextView) {
        val ref = FirebaseDatabase.getInstance().getReference("User")
        val profileList = java.util.ArrayList<ReadUser>()
        ref.orderByChild("userId").equalTo(idUser)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (uSnapshot in snapshot.children) {
                            val data = uSnapshot.getValue(ReadUser::class.java)
                            profileList.add(data!!)
                            Glide.with(binding.root).load(profileList[0].userAvatar)
                                .into(imvAvatar)
                            tvName.setText(profileList[0].userName.toString())

                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("DatabaseError", error.message)
                }

            })
    }

    private fun ShareNow(id: String, data: UploadPost) {
        val ref = FirebaseDatabase.getInstance().getReference("Post")
        ref.child(id).setValue(data).addOnSuccessListener {
            Snackbar.make(
                binding.root,
                "Share post success",
                Snackbar.LENGTH_SHORT
            ).show()
            Nofication("shared your post", "Share")
        }.addOnFailureListener {
            Log.e("Sharenow", "Share post failed")
        }
    }

    private fun LoadLikeNumber(idPost: String?) {
        val fDatabase = FirebaseDatabase.getInstance().getReference("Like")
        fDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(idPost!!).hasChild(mAuth.currentUser!!.uid)) {
                    binding.tvLikeNumber.text =
                        snapshot.child(idPost).childrenCount.toString() + " Like"
                    binding.like.setImageResource(R.drawable.ic_like_red)
                } else {
                    binding.tvLikeNumber.text =
                        snapshot.child(idPost).childrenCount.toString() + " Like"
                    binding.like.setImageResource(R.drawable.ic_like)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("DatabaseError", error.message)
            }
        })
    }

    private fun LoadShareNumber(idPost: String) {
        val fDatabase = FirebaseDatabase.getInstance().getReference("Post")
        fDatabase.orderByChild("idShare").equalTo(idPost)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.tvShareNumber.text = snapshot.childrenCount.toString() + " Share"
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("DatabaseError", error.message)
                }
            })
    }

    private fun LoadCommentNumber(idPost: String) {
        val fDatabase = FirebaseDatabase.getInstance().getReference("Comment")
        fDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.tvCommentNumber.text =
                    snapshot.child(idPost!!).childrenCount.toString() + " Comment"
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("DatabaseError", error.message)
            }

        })
    }


    private fun buildDialog(
        idPost: String?
    ): AlertDialog.Builder? {
        val builder = AlertDialog.Builder(binding.root.context)
        builder.setTitle("Delete comment")
        builder.setMessage("Do you want to delete this comment?")
        builder.setPositiveButton("Delete") { dialog, which ->
            val dFirebaseDatabase =
                FirebaseDatabase.getInstance().getReference("Post")
            dFirebaseDatabase.child(idPost!!).removeValue().addOnSuccessListener {
                DeleteDataComment(idPost)
                DeleteDataLike(idPost)
                DeleteShare(idPost)
                Snackbar.make(
                    binding.root,
                    "Post deleted",
                    Snackbar.LENGTH_SHORT
                ).show()
                Timer().schedule(timerTask {
                    finish()
                }, 1000)
            }

        }
        builder.setNeutralButton("Cancel") { dialog, which -> }
        return builder
    }

    private fun DeleteShare(idPost: String) {
        val fDatabase = FirebaseDatabase.getInstance().getReference("Post")
        fDatabase.orderByChild("idShare").equalTo(idPost)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (u in snapshot.children) {
                            val data = u.getValue(ReadPost::class.java)
                            val id = data!!.idPost
                            deleteShare(id!!)
                        }


                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun deleteShare(id: String) {
        val fDatabase = FirebaseDatabase.getInstance().getReference("Post")
        fDatabase.child(id).removeValue()
    }


    private fun DeleteDataLike(idPost: String) {
        val fDatabase = FirebaseDatabase.getInstance().getReference("Like")
        fDatabase.child(idPost!!).removeValue().addOnSuccessListener {
            Log.d("DeleteDataLike", "success")
        }
    }

    private fun DeleteDataComment(idPost: String) {
        val fDatabase = FirebaseDatabase.getInstance().getReference("Comment")
        fDatabase.child(idPost!!).removeValue().addOnSuccessListener {
            Log.d("DeleteDataLike", "success")
        }
    }

    private fun Nofication(mess: String, type: String) {
        val id = UUID.randomUUID().toString()
        val data = UpNofication(
            id,
            uID,
            mAuth.currentUser!!.uid,
            "commented your post",
            false,
            "Comment",
            ServerValue.TIMESTAMP,
            ServerValue.TIMESTAMP
        )
        NoficationClass().UpNofication(data)
    }

}