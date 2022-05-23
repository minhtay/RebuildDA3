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
                            LoadUser(postList[0].idUser, binding.imvAvatar, binding.tvName)
                            Glide.with(binding.root).load(postList[0].photo)
                                .into(binding.imvPhoto)
                            if (postList[0].title == null) {
                                binding.edtTitle.visibility = View.GONE
                            } else {
                                binding.edtTitle.setText(postList[0].title)
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
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("DatabaseError", error.message)
                    }

                })
            }

        })

        // xét sự kiện cho btn comment
        binding.btnComment.setOnClickListener{Utils.hideSoftKeyboard(this,binding.root)}

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
                        Utils.hideSoftKeyboard(this@PostActivity,binding.root)
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


        binding.cardView1.setOnClickListener { Utils.hideSoftKeyboard(this@PostActivity,binding.root) }

        binding.btnMenu.setOnClickListener(object : View.OnClickListener {
            @SuppressLint("NewApi")
            override fun onClick(v: View?) {
                binding.btnMenu.isEnabled = false
                val popupMenu = PopupMenu(binding.root.context, binding.btnMenu)
                popupMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.delete -> {
                            if (mAuth.currentUser!!.uid == postList[0].idUser) {
                                /**/
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
                binding.btnMenu.isEnabled = true
                binding.tvNumberLayout.visibility = View.VISIBLE
                binding.linearLayout3.visibility = View.VISIBLE
                binding.linearLayout4.visibility = View.VISIBLE
                binding.view.visibility = View.VISIBLE
                binding.view1.visibility = View.VISIBLE
                binding.linearLayout5.visibility = View.VISIBLE
                binding.layout12.visibility = View.GONE
                binding.edtTitle.setHint("")
                binding.edtTitle.setText(postList[0].title)
                Glide.with(this@PostActivity).load(postList[0].photo).into(binding.imvPhoto)
                binding.btnClearImage.visibility = View.GONE
                binding.btnPickimage.visibility = View.GONE
                binding.edtTitle.isEnabled = false
                filePath = null
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

        binding.btnUpdate.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val nTitle = binding.edtTitle.text.toString()
                if (postList[0].typePost == "Post") {
                    if (nTitle != postList[0].title) {//title mới khác với title cũ
                        if (nTitle.length > 0) {// title mới khác null
                            if (postList[0].photo != null) {//photo khác null
                                if (filePath != null) {
                                    // thêm ảnh + title, xóa ảnh
                                    Log.d("testEditPost", "thêm ảnh + title, xóa ảnh 1")
                                    UpadteTitle(idPost, nTitle, 2)

                                } else {
                                    if (binding.imvPhoto.drawable != null) {
                                        // thêm title
                                        Log.d("testEditPost", "thêm title 2 ")
                                        UpadteTitle(idPost, nTitle, 0)
                                    } else {
                                        //thêm title,xóa ảnh
                                        Log.d("testEditPost", "thêm title,xóa ảnh 3 ")
                                        UpadteTitle(idPost, nTitle, 1)
                                    }
                                }
                            } else {// photo == null
                                if (filePath != null) {
                                    // thêm ảnh + title
                                    Log.d("testEditPost", "thêm ảnh + title 4")
                                    UpadteTitle(idPost, nTitle, 3)
                                } else {
                                    // thêm title
                                    Log.d("testEditPost", "thêm title 5")
                                    UpadteTitle(idPost, nTitle, 0)


                                }
                            }

                        } else {
                            if (postList[0].photo != null) {//photo khác null
                                if (filePath != null) {
                                    // thêm ảnh + title(title = null), xóa ảnh
                                    Log.d(
                                        "testEditPost",
                                        "thêm ảnh + title(title = null), xóa ảnh 6"
                                    )
                                    UpadteTitle(idPost, nTitle, 2)

                                } else {
                                    if (binding.imvPhoto.drawable != null) {
                                        // thêm title(title = null)
                                        Log.d("testEditPost", "thêm title(title = null) 7")
                                        UpadteTitle(idPost, nTitle, 0)

                                    } else {
                                        //lỗi
                                        Log.d("testEditPost", "lỗi 8")
                                    }
                                }
                            } else {// photo == null
                                if (filePath != null) {
                                    // thêm ảnh + title
                                    Log.d("testEditPost", "thêm ảnh + title 9")
                                    UpadteTitle(idPost, nTitle, 3)

                                } else {
                                    // lỗi
                                    Log.d("testEditPost", "lỗi 10")
                                }
                            }
                        }
                    } else { // title mới giống title cũ
                        if (nTitle.length > 0) {
                            if (postList[0].photo != null) {
                                if (filePath != null) {
                                    //xóa ảnh+thêm ảnh
                                    Log.d("testEditPost", "xóa ảnh+thêm ảnh")
                                    DeleTeImage(idPost, nTitle, 1)
                                } else {
                                    //xóa ảnh
                                    Log.d("testEditPost", "xóa ảnh")
                                    DeleTeImage(idPost, nTitle, 0)
                                }
                            } else {
                                if (filePath != null) {
                                    //thêm ảnh
                                    Log.d("testEditPost", "thêm ảnh")
                                    UpdateImage(idPost, nTitle)
                                } else {
                                    //lỗi
                                    Log.d("testEditPost", "lỗi")
                                }
                            }
                        } else {
                            //lỗi
                            Log.d("testEditPost", "lỗi")
                        }
                    }

                } else {
                    if (nTitle == postList[0].title) {
                        Snackbar.make(
                            binding.root,
                            "You haven't changed at all",
                            Snackbar.LENGTH_LONG
                        ).show()
                    } else {
                        val ref = FirebaseDatabase.getInstance().getReference("Post")
                        ref.child(idPost).child("title").setValue(nTitle)
                            .addOnSuccessListener {
                                Snackbar.make(
                                    binding.root,
                                    "Post update successfully",
                                    Snackbar.LENGTH_LONG
                                ).show()
                                binding.tvNumberLayout.visibility = View.VISIBLE
                                binding.linearLayout3.visibility = View.VISIBLE
                                binding.linearLayout4.visibility = View.VISIBLE
                                binding.view.visibility = View.VISIBLE
                                binding.view1.visibility = View.VISIBLE
                                binding.linearLayout5.visibility = View.VISIBLE
                                binding.layout12.visibility = View.GONE
                                binding.btnClearImage.visibility = View.GONE
                                binding.edtTitle.isEnabled = false
                                binding.edtTitle.setText(nTitle)
                            }
                    }
                }
            }

        })

        binding.layout.setOnClickListener {Utils.hideSoftKeyboard(this,binding.root) }

    }

    private fun UpadteTitle(idPost: String, nTitle: String, i: Int) {
        val ref = FirebaseDatabase.getInstance().getReference("Post")
        ref.child(idPost).child("title").setValue(binding.edtTitle.text.toString())
            .addOnSuccessListener {
                if (i == 0) {
                    Log.d("testEditPost1", "thêm title ")
                    Snackbar.make(binding.root, "Post update successfully", Snackbar.LENGTH_LONG)
                        .show()
                    binding.tvNumberLayout.visibility = View.VISIBLE
                    binding.linearLayout3.visibility = View.VISIBLE
                    binding.linearLayout4.visibility = View.VISIBLE
                    binding.view.visibility = View.VISIBLE
                    binding.view1.visibility = View.VISIBLE
                    binding.linearLayout5.visibility = View.VISIBLE
                    binding.layout12.visibility = View.GONE
                    binding.btnClearImage.visibility = View.GONE
                    binding.edtTitle.isEnabled = false
                    binding.edtTitle.setText(nTitle)
                    binding.btnMenu.isEnabled = true
                    binding.btnPickimage.visibility = View.GONE
                    binding.btnClearImage.visibility = View.GONE


                }

                if (i == 1) {
                    Log.d("testEditPost1", "thêm title + xóa ảnh")
                    DeleTeImage(idPost, nTitle, 0)

                }

                if (i == 2) {
                    Log.d("testEditPost1", "thêm title+ xóa ảnh + thêm ảnh")
                    DeleTeImage(idPost, nTitle, 1)

                }

                if (i == 3) {
                    Log.d("testEditPost1", "thêm title + thêm ảnh")
                    UpdateImage(idPost, nTitle)

                }
            }
    }

    private fun DeleTeImage(idPost: String, nTitle: String, i: Int) {
        val fStorage = FirebaseStorage.getInstance().getReference("Post")
        fStorage.child(idPost).delete().addOnSuccessListener {
            if (i == 0) {
                val ref = FirebaseDatabase.getInstance().getReference("Post")
                ref.child(idPost).child("photo").setValue(null)
                    .addOnSuccessListener {
                        Log.d("testEditPost2", "xóa ảnh")
                        binding.tvNumberLayout.visibility = View.VISIBLE
                        binding.linearLayout3.visibility = View.VISIBLE
                        binding.linearLayout4.visibility = View.VISIBLE
                        binding.view.visibility = View.VISIBLE
                        binding.view1.visibility = View.VISIBLE
                        binding.linearLayout5.visibility = View.VISIBLE
                        binding.layout12.visibility = View.GONE
                        binding.btnClearImage.visibility = View.GONE
                        binding.edtTitle.isEnabled = false
                        binding.edtTitle.setText(nTitle)
                        binding.btnMenu.isEnabled = true
                        binding.btnPickimage.visibility = View.GONE
                        binding.imvPhoto.setImageResource(0)
                        binding.btnClearImage.visibility = View.GONE
                        postList[0].photo = null
                    }.addOnFailureListener {
                        Log.d("error firestorage", it.toString())
                    }
            }

            if (i == 1) {
                Log.d("testEditPost2", "xóa ảnh + thêm ảnh")
                UpdateImage(idPost, nTitle)

            }
        }.addOnFailureListener {
            Log.d("error firestorage", it.toString())
        }

    }

    private fun UpdateImage(idPost: String, nTitle: String) {
        binding.imvPhoto.setImageResource(0)
        val fStorage = FirebaseStorage.getInstance().getReference("Post/$idPost")
        fStorage.putFile(filePath!!).addOnSuccessListener {
            Log.e("uploadImage", "success")
            fStorage.downloadUrl.addOnSuccessListener {
                val url = it.toString()
                Log.e("dowloadImage", "success")

                val ref = FirebaseDatabase.getInstance().getReference("Post")
                ref.child(idPost).child("photo").setValue(url)
                    .addOnSuccessListener {
                        Snackbar.make(
                            binding.root,
                            "Post update successfully",
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                        binding.tvNumberLayout.visibility = View.VISIBLE
                        binding.linearLayout3.visibility = View.VISIBLE
                        binding.linearLayout4.visibility = View.VISIBLE
                        binding.view.visibility = View.VISIBLE
                        binding.view1.visibility = View.VISIBLE
                        binding.linearLayout5.visibility = View.VISIBLE
                        binding.layout12.visibility = View.GONE
                        binding.btnClearImage.visibility = View.GONE
                        binding.edtTitle.isEnabled = false
                        binding.edtTitle.setText(nTitle)
                        Glide.with(binding.root).load(filePath).into(binding.imvPhoto)
                        binding.btnMenu.isEnabled = true
                        binding.btnPickimage.visibility = View.GONE
                        binding.btnClearImage.visibility = View.GONE
                        Glide.with(binding.root).load(filePath).into(binding.imvPhoto)
                        postList[0].photo = url
                    }.addOnFailureListener {
                        Log.d("error firestorage", it.toString())
                    }
                Log.d("testEditPost3", "thêm ảnh")

            }
        }.addOnFailureListener {
            Log.d("error firestorage", it.toString())
        }

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
                DeleTeDataImage(idPost)
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

    private fun DeleTeDataImage(idPost: String) {
        val fStorage = FirebaseStorage.getInstance().getReference("Post")
        fStorage.child(idPost).delete().addOnSuccessListener {
            Log.d("DeleteDataImage", "success")
        }
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

}