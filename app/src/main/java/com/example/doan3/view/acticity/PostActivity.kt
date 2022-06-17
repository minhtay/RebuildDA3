package com.example.doan3.view.acticity

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity.END
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
import com.ortiz.touchview.TouchImageView
import de.hdodenhof.circleimageview.CircleImageView
import gun0912.tedimagepicker.builder.TedImagePicker
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale.getDefault
import kotlin.concurrent.timerTask


class PostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostBinding
    private lateinit var mAuth: FirebaseAuth
    private var uID: String? = null
    private var filePath: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        val idPost = intent.getStringExtra("idPost")
        val typePost = intent.getStringExtra("typePost")
        val idShare = intent.getStringExtra("idShare")
        val idUser = intent.getStringExtra("idUser")
        val title = intent.getStringExtra("title")
        val photo = intent.getStringExtra("photo")
        val dateCreate = intent.getStringExtra("dateCreate")
        intent.getStringExtra("dateUpdate")
        Log.d("testDateP", dateCreate.toString())

        uID = idUser

        if (typePost == "Share") {
            binding.edtTitle.visibility = View.GONE
            binding.imvPhoto.visibility = View.GONE
        }

        binding.edtTitle.setText(title)
        if (photo != null) {
            binding.imvPhoto.visibility = View.VISIBLE
        }
        loadUser(idUser, binding.imvAvatar, binding.tvName)
        loadDateCreate(dateCreate, binding.tvDateCreate)
        Log.d("url Photo", photo.toString())
        if (typePost == "Post") {
            binding.layoutShare.visibility = View.GONE
            Glide.with(binding.root).load(photo).into(binding.imvPhoto)
            loadUser(idUser, binding.imvAvatar, binding.tvName)
            binding.tvTypePost.text = "Post"
        } else {
            binding.tvTypePost.text = "Share"
            val fDatabase = FirebaseDatabase.getInstance().getReference("Post")
            val postList = ArrayList<ReadPost>()
            fDatabase.orderByChild("idPost").equalTo(idShare)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (uSnapshot in snapshot.children) {
                                val data =
                                    uSnapshot.getValue(ReadPost::class.java)
                                postList.add(data!!)
                            }
                            loadUser(
                                postList[0].idUser,
                                binding.imvAvatar2,
                                binding.tvName2
                            )
                            binding.edtTitle2.setText(postList[0].title)
                            Glide.with(binding.root)
                                .load(postList[0].photo)
                                .into(binding.ivPhoto2)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}

                })
        }

        loadUser(mAuth.currentUser!!.uid, binding.imvAvatar3, binding.tvnull)

        //
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
        // Lấy số like của bài viết
        loadLikeNumber(idPost)
        // lấy số comment của bài viết
        loadCommentNumber(idPost)
        // lấy số share của bài viết
        loadShareNumber(idPost)

        binding.btnLike.setOnClickListener {
            val fDatabase = FirebaseDatabase.getInstance().getReference("Like")
            with(fDatabase) {
                addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.child(idPost).hasChild(mAuth.currentUser?.uid!!)) {
                            child(idPost).child(mAuth.currentUser!!.uid).removeValue()
                        } else {
                            child(idPost).child(mAuth.currentUser!!.uid)
                                .setValue(true)
                            uploadNofication("liked your post", "Like")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("DatabaseError", error.message)
                    }

                })
            }
        }

        binding.btnPostComment.setOnClickListener {
            if (binding.edtComment.text.isNotEmpty()) {
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
                    FirebaseDatabase.getInstance().getReference("Comment").child(idPost)
                fDatabase.child(id).setValue(data).addOnSuccessListener {
                    Snackbar.make(
                        binding.root,
                        "Add new comment uccess",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    Utils.hideSoftKeyboard(this@PostActivity, binding.root)
                    uploadNofication("commented your post", "Comment")
                    binding.edtComment.text.clear()
                }.addOnFailureListener {
                    Snackbar.make(
                        binding.root,
                        "Add new comment failed",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            } else {
                Snackbar.make(
                    binding.root,
                    "Please enter comment",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.btnShare.setOnClickListener {
            val popupMenu = PopupMenu(binding.root.context, binding.btnShare)
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    com.example.doan3.R.id.shareNow -> {
                        val id = UUID.randomUUID().toString()
                        if (typePost == "Post") {
                            UploadPost(
                                id,
                                "Share",
                                idPost,
                                mAuth.currentUser?.uid,
                                null,
                                null,
                                ServerValue.TIMESTAMP,
                                ServerValue.TIMESTAMP
                            ).apply {
                                shareNow(id, this)
                            }
                        } else {
                            UploadPost(
                                id,
                                "Share",
                                idShare,
                                mAuth.currentUser?.uid,
                                null,
                                null,
                                ServerValue.TIMESTAMP,
                                ServerValue.TIMESTAMP
                            ).apply {
                                shareNow(id, this)
                            }


                        }
                    }

                    com.example.doan3.R.id.moreOption -> {
                        val intent =
                            Intent(this, SharePostActivity::class.java)
                        if (typePost == "Post") {
                            intent.putExtra("idPost", idPost)
                        } else {
                            intent.putExtra("idPost", idShare)
                        }
                        startActivity(intent)
                    }

                }
                false
            }
            popupMenu.inflate(com.example.doan3.R.menu.menu_post)
            END.also { popupMenu.gravity = it }
            popupMenu.setForceShowIcon(true)
            popupMenu.show()
        }

        binding.cardView1.setOnClickListener {
            Utils.hideSoftKeyboard(
                this@PostActivity,
                binding.root
            )
        }

        if (mAuth.currentUser!!.uid != idUser) {
            binding.btnMenu.visibility = View.GONE
        }
        binding.btnMenu.setOnClickListener {
            val popupMenu = PopupMenu(binding.root.context, binding.btnMenu)
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    com.example.doan3.R.id.delete -> {
                        deleteDialog(idPost)

                    }
                    com.example.doan3.R.id.edit -> {
                        Log.d("edit Post", typePost.toString())

                        if (typePost.toString() == "Post") {
                            Log.d("edit Post", typePost.toString())
                            binding.edtTitle.isEnabled = true
                            binding.edtTitle.hint = "What are you thinking ?"
                            binding.btnClearImage.visibility = View.VISIBLE
                        }
                    }
                }
                false
            }
            popupMenu.inflate(com.example.doan3.R.menu.menu_comment)
            END.also { popupMenu.gravity = it }
            popupMenu.setForceShowIcon(true)
            popupMenu.show()
        }

        binding.btnCancel.setOnClickListener {
            binding.edtTitle.setText(title)
            Glide.with(binding.root).load(photo).into(binding.imvPhoto)
            reloadUi()
        }
        binding.btnPickimage.setOnClickListener {
            TedImagePicker.with(this@PostActivity).start { uri ->
                Glide.with(binding.root).load(uri).into(binding.imvPhoto)
                binding.btnClearImage.visibility = View.VISIBLE
                binding.btnPickimage.visibility = View.GONE
                filePath = uri
                binding.layout12.visibility = View.VISIBLE
                Log.d("filePath", filePath.toString())

            }
        }
        binding.btnClearImage.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                binding.imvPhoto.setImageResource(0)
                binding.btnPickimage.visibility = View.VISIBLE
                binding.btnClearImage.visibility = View.GONE
                filePath = null
            }

        })
        binding.layout.setOnClickListener { Utils.hideSoftKeyboard(this, binding.root) }
        binding.btnUpdate.setOnClickListener {
            with(idPost) { updateTitLe() }
            if (typePost == "Post") {
                if (filePath != null)
                    updateImage(idPost)
            }
        }

        binding.imvPhoto.setOnClickListener {
            val builder =
                Dialog(this@PostActivity, android.R.style.Theme_Material_NoActionBar_Fullscreen)
            builder.setContentView(com.example.doan3.R.layout.dialog_image_view)
            val image = builder.findViewById<TouchImageView>(com.example.doan3.R.id.imageView)
            val url: String = photo.toString()
            Glide.with(this).load(photo.toString())
                .into(builder.findViewById(com.example.doan3.R.id.imageView))
            builder.setCancelable(true)
            builder.setCanceledOnTouchOutside(false)
            builder.show()
        }

        binding.btnComment.setOnClickListener {
            val inputMethodManager: InputMethodManager =
                getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.toggleSoftInputFromWindow(
                binding.edtComment.getApplicationWindowToken(),
                InputMethodManager.SHOW_FORCED,
                0
            )
            binding.edtComment.requestFocus()
        }

    }

    private fun String.updateTitLe() {
        val text = binding.edtTitle.text.toString()
        val fDatabse = FirebaseDatabase.getInstance().getReference("Post/${this}")
        fDatabse.child("title").setValue(text)
        binding.linearLayout3.visibility = View.VISIBLE
        binding.linearLayout4.visibility = View.VISIBLE
        binding.linearLayout5.visibility = View.VISIBLE
        binding.view.visibility = View.VISIBLE
        binding.view1.visibility = View.VISIBLE
        binding.layout12.visibility = View.GONE
        binding.tvNumberLayout.visibility = View.VISIBLE
        binding.btnClearImage.visibility = View.GONE


    }

    private fun updateImage(idPost: String) {
        val fStorage = FirebaseStorage.getInstance().getReference("Post")
        fStorage.child(idPost).delete().addOnSuccessListener {
            val fStorage1 = FirebaseStorage.getInstance().getReference("Post/$idPost")
            fStorage1.putFile(filePath!!).addOnSuccessListener {
                fStorage1.downloadUrl.addOnSuccessListener {
                    val fDatabse = FirebaseDatabase.getInstance().getReference("Post/$idPost")
                    fDatabse.child("photo").setValue(it.toString())
                    Glide.with(this).load(it.toString()).into(binding.imvPhoto)
                }

            }
        }

    }

    private fun deleteDialog(
        idPost: String?
    ): AlertDialog.Builder {
        val builder = AlertDialog.Builder(binding.root.context)
        builder.setTitle("Delete comment")
        builder.setMessage("Do you want to delete this comment?")
        builder.setPositiveButton("Delete") { _, _ ->
            val dFirebaseDatabase =
                FirebaseDatabase.getInstance().getReference("Post")
            dFirebaseDatabase.child(idPost!!).removeValue().addOnSuccessListener {
                deleteDataComment(idPost)
                deleteDataLike(idPost)
                deleteShare(idPost)
                deleteImage(idPost)
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
        builder.setNeutralButton("Cancel") { _, _ -> }
        return builder
    }

    private fun deleteImage(idPost: String) {
        val fStorage = FirebaseStorage.getInstance().getReference("Post")
        fStorage.child(idPost).delete()
    }

    private fun deleteDataLike(idPost: String) {
        val fDatabase = FirebaseDatabase.getInstance().getReference("Like")
        fDatabase.child(idPost).removeValue().addOnSuccessListener {
            Log.d("DeleteDataLike", "success")
        }
    }

    private fun deleteShare(idPost: String) {
        val fDatabase = FirebaseDatabase.getInstance().getReference("Post")
        fDatabase.orderByChild("idShare").equalTo(idPost)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (u in snapshot.children) {
                            val data = u.getValue(ReadPost::class.java)
                            val id = data!!.idPost
                            val fDatabase1 = FirebaseDatabase.getInstance().getReference("Post")
                            fDatabase1.child(id!!).removeValue()
                        }


                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun deleteDataComment(idPost: String) {
        val fDatabase = FirebaseDatabase.getInstance().getReference("Comment")
        fDatabase.child(idPost).removeValue().addOnSuccessListener {
            Log.d("DeleteDataLike", "success")
        }
    }


    private fun loadDateCreate(dateCreate: String?, tvDateCreate: TextView) {
        val format = SimpleDateFormat("yyyy-MM-dd", getDefault())
        tvDateCreate.text = format.format(dateCreate!!.toLong())
    }

    private fun loadUser(idUser: String?, imvAvatar: CircleImageView, tvName: TextView) {
        val ref = FirebaseDatabase.getInstance().getReference("User")
        val profileList = java.util.ArrayList<ReadUser>()
        ref.orderByChild("userId").equalTo(idUser)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (uSnapshot in snapshot.children) {
                            val data = uSnapshot.getValue(ReadUser::class.java)
                            profileList.add(data!!)
                            Glide.with(applicationContext).load(profileList[0].userAvatar)
                                .into(imvAvatar)
                            tvName.text = profileList[0].userName.toString()

                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("DatabaseError", error.message)
                }

            })
    }

    private fun loadLikeNumber(idPost: String?) {
        val fDatabase = FirebaseDatabase.getInstance().getReference("Like")
        fDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(idPost!!).hasChild(mAuth.currentUser!!.uid)) {
                    (snapshot.child(idPost).childrenCount.toString() + " Like").also {
                        binding.tvLikeNumber.text = it
                    }
                    binding.imgLike.setImageResource(R.drawable.ic_like_red)
                } else {
                    (snapshot.child(idPost).childrenCount.toString() + " Like").also {
                        binding.tvLikeNumber.text = it
                    }
                    binding.imgLike.setImageResource(R.drawable.ic_like)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("DatabaseError", error.message)
            }
        })
    }

    private fun loadShareNumber(idPost: String) {
        val fDatabase = FirebaseDatabase.getInstance().getReference("Post")
        fDatabase.orderByChild("idShare").equalTo(idPost)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    (snapshot.childrenCount.toString() + " Share").also {
                        binding.tvShareNumber.text = it
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("DatabaseError", error.message)
                }
            })
    }

    private fun loadCommentNumber(idPost: String) {
        val fDatabase = FirebaseDatabase.getInstance().getReference("Comment")
        fDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (snapshot.child(idPost).childrenCount.toString() + " Comment").also {
                    binding.tvCommentNumber.text = it
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("DatabaseError", error.message)
            }

        })
    }

    private fun uploadNofication(mess: String, type: String) {
        val id = UUID.randomUUID().toString()
        val data = UpNofication(
            id,
            uID,
            mAuth.currentUser!!.uid,
            mess,
            false,
            type,
            ServerValue.TIMESTAMP,
            ServerValue.TIMESTAMP
        )
        NoficationClass().UpNofication(data)
    }

    private fun reloadUi() {
        binding.btnMenu.isEnabled = true
        binding.edtTitle.isEnabled = false
        binding.tvNumberLayout.visibility = View.VISIBLE
        binding.linearLayout3.visibility = View.VISIBLE
        binding.linearLayout4.visibility = View.VISIBLE
        binding.view.visibility = View.VISIBLE
        binding.view1.visibility = View.VISIBLE
        binding.linearLayout5.visibility = View.VISIBLE
        binding.layout12.visibility = View.GONE
        binding.edtTitle.hint = ""
        binding.btnClearImage.visibility = View.GONE
        binding.btnPickimage.visibility = View.GONE
        binding.edtTitle.isEnabled = false
        filePath = null
    }

    private fun shareNow(id: String, data: UploadPost) {
        val ref = FirebaseDatabase.getInstance().getReference("Post")
        ref.child(id).setValue(data).addOnSuccessListener {
            Snackbar.make(
                binding.root,
                "Share post success",
                Snackbar.LENGTH_SHORT
            ).show()
            uploadNofication("shared your post", "Share")
        }.addOnFailureListener {
            Log.e("Sharenow", "Share post failed")
        }
    }

}
