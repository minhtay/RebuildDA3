package com.example.doan3.view.acticity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
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
import de.hdodenhof.circleimageview.CircleImageView
import gun0912.tedimagepicker.builder.TedImagePicker
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale.getDefault


class PostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostBinding
    private lateinit var mAuth: FirebaseAuth
    private var uID: String? = null
    private var filePath: Uri? = null
    private var title: String? = null
    private var photo: String? = null


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        val idPost = intent.getStringExtra("idPost")
        val typePost = intent.getStringExtra("typePost")
        val idShare = intent.getStringExtra("idShare")
        val idUser = intent.getStringExtra("idUser")
        title = intent.getStringExtra("title")
        photo = intent.getStringExtra("photo")
        val dateCreate = intent.getStringExtra("dateCreate")
        intent.getStringExtra("dateUpdate")
        Log.d("testDateP", dateCreate.toString())

        uID = idUser

        if (title==null){
            binding.edtTitle.visibility = View.GONE
        }
        if (typePost == "Share") {
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
                    R.id.shareNow -> {
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

                    R.id.moreOption -> {
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
            popupMenu.inflate(R.menu.menu_post)
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
                    R.id.delete -> {
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Delete post ")
                        builder.setMessage("Are you sure you want to delete the post?")
                        builder.setPositiveButton("Delete") { _, _ ->
                            if (typePost == "Post") {
                                deletePost(idPost)
                            } else deletePostShare(idPost)
                        }
                        builder.setNeutralButton("Cancel") { _, _ ->
                        }
                        builder.show()

                    }
                    R.id.edit -> {
                        Log.d("edit Post", typePost.toString())
                        binding.edtTitle.visibility = View.VISIBLE
                        binding.edtTitle.isEnabled = true
                        binding.edtTitle.requestFocus()
                        binding.edtTitle.setSelection(binding.edtTitle.text.length)
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showSoftInput(binding.edtTitle, InputMethodManager.SHOW_FORCED)
                        binding.edtTitle.hint = "What are you thinking ?"
                        binding.btnBack.visibility = View.GONE
                        binding.btnMenu.visibility = View.GONE
                        binding.btnCancel.visibility = View.VISIBLE
                        binding.btnUpdate.visibility = View.VISIBLE
                        if (typePost == "Post") {
                            binding.btnClearImage.visibility = View.VISIBLE
                        }
                    }
                }
                false
            }
            popupMenu.inflate(R.menu.menu_comment)
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
                binding.imvPhoto.visibility = View.VISIBLE
                Log.d("filePath", filePath.toString())

            }
        }
        binding.btnClearImage.setOnClickListener {
            binding.imvPhoto.setImageResource(0)
            binding.imvPhoto.visibility = View.GONE
            binding.btnPickimage.visibility = View.VISIBLE
            binding.btnClearImage.visibility = View.GONE
            filePath = null
        }
        binding.layout.setOnClickListener { Utils.hideSoftKeyboard(this, binding.root) }
        binding.btnUpdate.setOnClickListener {
            if (typePost == "Post") {
                val text = binding.edtTitle.text.toString()
                if (filePath != null) {
                    updateImage(idPost)
                }else{
                    if (title!=binding.edtTitle.text.toString()&&binding.edtTitle.text.toString()!=null){
                        updatePostShare(idPost,text)
                    }
                }
            }else{
                val text = binding.edtTitle.text.toString()
                if (title!=null){
                    if (title!=binding.edtTitle.text.toString()&&binding.edtTitle.text.toString()!=null){
                        updatePostShare(idPost,text)
                    }
                }else {
                    if(binding.edtTitle.text.toString()!=null){
                        updatePostShare(idPost,text)
                    }
                }
            }
        }

        binding.imvPhoto.setOnClickListener {
            val builder =
                Dialog(this@PostActivity, android.R.style.Theme_Material_NoActionBar_Fullscreen)
            builder.setContentView(R.layout.dialog_image_view)
            Glide.with(this).load(photo.toString())
                .into(builder.findViewById(R.id.imageView))
            builder.setCancelable(true)
            builder.setCanceledOnTouchOutside(false)
            builder.show()
        }

        binding.btnComment.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.edtComment, InputMethodManager.SHOW_FORCED)
            binding.edtComment.requestFocus()
        }

    }

    private fun updatePostShare(idPost: String, text: String) {
        FirebaseDatabase.getInstance().getReference("Post/$idPost").child("title").setValue(text).addOnSuccessListener {
            Snackbar.make(binding.root,"Update post success",Snackbar.LENGTH_SHORT).show()
            reloadUi()
        }
    }

    private fun updateImage(idPost: String) {
        val fStorage = FirebaseStorage.getInstance().getReference("Post")
        fStorage.child(idPost).delete().addOnSuccessListener {
            val fStorage1 = FirebaseStorage.getInstance().getReference("Post/$idPost")
            fStorage1.putFile(filePath!!).addOnSuccessListener {
                fStorage1.downloadUrl.addOnSuccessListener {
                    if (title!=binding.edtTitle.text.toString()&&binding.edtTitle.text.toString()!=null){
                        uploadPost(it.toString(),idPost)
                    }else{
                        FirebaseDatabase.getInstance().getReference("Post/$idPost").child("photo").setValue(it.toString()).addOnSuccessListener {
                            Snackbar.make(binding.root,"Update post success",Snackbar.LENGTH_SHORT).show()
                            reloadUi()
                        }
                    }
                }

            }
        }

    }

    private fun uploadPost(it: String, idPost: String) {
        val data = UploadPost(idPost,"Post",null,mAuth.currentUser!!.uid,binding.edtTitle.text.toString(),it,ServerValue.TIMESTAMP,ServerValue.TIMESTAMP)
        FirebaseDatabase.getInstance().getReference("Post/$idPost").setValue(data).addOnSuccessListener {
            Snackbar.make(binding.root,"Update post success",Snackbar.LENGTH_SHORT).show()
            reloadUi()
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

    private fun loadDateCreate(dateCreate: String?, tvDateCreate: TextView) {
        val format = SimpleDateFormat("yyyy-MM-dd", getDefault())
        tvDateCreate.text = format.format(dateCreate!!.toLong())
    }

    private fun loadUser(idUser: String?, imvAvatar: CircleImageView, tvName: TextView) {
        val ref = FirebaseDatabase.getInstance().getReference("User")
        val profileList = ArrayList<ReadUser>()
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
        binding.btnMenu.visibility = View.VISIBLE
        binding.btnBack.visibility = View.VISIBLE
        binding.edtTitle.isEnabled = false
        binding.btnClearImage.visibility = View.GONE
        binding.btnPickimage.visibility = View.GONE
        binding.btnCancel.visibility = View.GONE
        binding.btnUpdate.visibility = View.GONE
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

    private fun deletePost(idPost: String?) {
        val fdata = FirebaseDatabase.getInstance().getReference("Post")
        fdata.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild(idPost!!)) {
                    Log.d("Delete post", "post")
                    fdata.child(idPost).removeValue().addOnSuccessListener {
                        idPost.deleteComment()
                        idPost.deleteLike()
                        deleteSharePost(idPost)
                        Snackbar.make(binding.root, "Delete post success", Snackbar.LENGTH_LONG)
                            .show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Delete post", error.toString())
            }

        })
    }

    private fun deletePostShare(idPost: String?) {
        val fdata = FirebaseDatabase.getInstance().getReference("Post")
        fdata.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild(idPost!!)) {
                    Log.d("Delete post", "post")
                    fdata.child(idPost).removeValue().addOnSuccessListener {
                        idPost.deleteComment()
                        idPost.deleteLike()
                        Snackbar.make(binding.root, "Delete post success", Snackbar.LENGTH_LONG)
                            .show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Delete post", error.toString())
            }

        })
    }

    private fun String.deleteLike() {
        val fDatabase = FirebaseDatabase.getInstance().getReference("Like")
        fDatabase.child(this).removeValue().addOnSuccessListener {
            Log.d("delete like", "success")
        }
    }

    private fun String.deleteComment() {
        val fDatabase = FirebaseDatabase.getInstance().getReference("Comment")
        fDatabase.child(this).removeValue().addOnSuccessListener {
            Log.d("delete comment", "success")
        }
    }

    private fun deleteSharePost(idPost: String) {
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
                    Log.d("Delete post", error.toString())
                }
            })
    }

}
