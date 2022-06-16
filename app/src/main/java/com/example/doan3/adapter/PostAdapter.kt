package com.example.doan3.adapter

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.Gravity.END
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.doan3.R
import com.example.doan3.data.ReadPost
import com.example.doan3.data.ReadUser
import com.example.doan3.data.UpNofication
import com.example.doan3.data.UploadPost
import com.example.doan3.databinding.ItemPostBinding
import com.example.doan3.util.NoficationClass
import com.example.doan3.view.acticity.CommentActivity
import com.example.doan3.view.acticity.PostActivity
import com.example.doan3.view.acticity.SharePostActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale.getDefault
import kotlin.collections.ArrayList


class PostAdapter(val activity: Context, private val postList: ArrayList<ReadPost>) :
    RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    private lateinit var mAuth: FirebaseAuth
    private var idPost: String? = null


    class ViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        Log.d("DatabaseError", postList.size.toString())
        return postList.size
    }

    fun clear() {
        postList.clear()
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        mAuth = FirebaseAuth.getInstance()

        // đọc dữ liệu từ post List
        val idPost = postList[position].idPost
        val typePost = postList[position].typePost
        val idShare = postList[position].idShare
        val idUser = postList[position].idUser
        val title = postList[position].title
        val photo = postList[position].photo
        val dateCreate = postList[position].dateCreate
        val dateUpdate = postList[position].dateUpdate

        loadUser(holder.binding.root, idUser, holder.binding.imvAvatar, holder.binding.tvName)

        // chuyển đổi và hiển thị dateCreate*/
        this.loadDate(dateCreate, holder.binding.tvDateCreate)

        loadLikeNumber(idPost, holder.binding.imgLike, holder.binding.tvLikeNumber)

        loadCommentNumber(idPost, holder.binding.tvCommentNumber)

        loadShareNumber(idPost, holder.binding.tvShareNumber)

        if (photo != null) {
            holder.binding.imPhoto.visibility = View.VISIBLE
        }

        holder.binding.detailsLayout.setOnClickListener {
            val intent = Intent(holder.binding.root.context, PostActivity::class.java)
            intent.putExtra("idPost", idPost)
            intent.putExtra("typePost", typePost)
            intent.putExtra("idShare", idShare)
            intent.putExtra("idUser", idUser)
            intent.putExtra("title", title)
            intent.putExtra("photo", photo)
            intent.putExtra("dateCreate", dateCreate.toString())
            intent.putExtra("dateUpdate", dateUpdate.toString())
            holder.binding.root.context.startActivity(intent)
        }

        // xét sự kiện cho btn like
        holder.binding.btnLike.setOnClickListener {
            val fDatabase = FirebaseDatabase.getInstance().getReference("Like")
            fDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child(idPost!!).hasChild(mAuth.currentUser?.uid!!)) {
                        fDatabase.child(idPost).child(mAuth.currentUser!!.uid).removeValue()
                    } else {
                        fDatabase.child(idPost).child(mAuth.currentUser!!.uid)
                            .setValue(true)
                        uploadNofication("liked your post", idUser!!)
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("DatabaseError", error.message)
                }

            })
        }

        holder.binding.btnComment.setOnClickListener {
            val intent = Intent(holder.binding.root.context, CommentActivity::class.java)
            intent.putExtra("idPost", idPost)
            intent.putExtra("idUser", idUser)

            holder.binding.root.context.startActivity(intent)
        }

        if (title == null) {
            holder.binding.tvTitle.visibility = View.GONE
        }

        if (typePost == "Post") {
            holder.binding.layoutShare.visibility = View.GONE
            holder.binding.tvTitle.text = title
            Glide.with(holder.binding.root).load(photo).into(holder.binding.imPhoto)

        } else {
            "Shared a post".also { holder.binding.tvTypePost.text = it }
            holder.binding.tvTitle.text = title
            loadPostShare(holder, idShare)
        }

        holder.binding.btnShare.setOnClickListener {
            val popupMenu = PopupMenu(holder.binding.btnShare.context, holder.binding.root)
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.shareNow -> {
                        Log.d("Resut","share start")
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
                                shareNow(holder.binding.root, id, this)
                            }
                            uploadNofication("shared your post", idUser!!)

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
                                shareNow(holder.binding.root, id, this)
                            }

                        }
                    }
                    R.id.moreOption -> {
                        Log.d("menuShare", "2")
                        val intent =
                            Intent(holder.binding.root.context, SharePostActivity::class.java)
                        if (typePost == "Post") {
                            intent.putExtra("idPost", idPost)
                        } else intent.putExtra("idPost", idShare)
                        holder.binding.root.context.startActivity(intent)
                    }
                }
                false
            }
            popupMenu.inflate(R.menu.menu_post)
            END.also { popupMenu.gravity = it }
            popupMenu.setForceShowIcon(true)
            popupMenu.show()
        }


        holder.binding.btnMenu.setOnClickListener {
            val popupMenu = PopupMenu(holder.binding.btnMenu.context, holder.binding.root)
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.delete -> {
                        if (idUser != mAuth.currentUser!!.uid){
                            if (typePost == "Post") {

                            } else {
                                buildDialog(holder.binding.root)?.show()
                            }
                        }else{

                        }
                    }
                    R.id.favorite -> {}
                    R.id.save_post -> {}
                }
                false
            }
            popupMenu.inflate(R.menu.menu_item_post)
            END.also { popupMenu.gravity = it }
            popupMenu.setForceShowIcon(true)
            popupMenu.show()
        }

    }

    private fun loadUser(
        context: ConstraintLayout,
        idUser: String?,
        imAvatar: CircleImageView,
        tvName: TextView
    ) {
        val ref = FirebaseDatabase.getInstance().getReference("User")
        val profileList = ArrayList<ReadUser>()
        ref.orderByChild("userId").equalTo(idUser)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (uSnapshot in snapshot.children) {
                            val data = uSnapshot.getValue(ReadUser::class.java)
                            profileList.add(data!!)
                            Glide.with(context).load(profileList[0].userAvatar)
                                .into(imAvatar)
                            tvName.text = profileList[0].userName
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("DatabaseError", error.message)
                }

            })
    }

    private fun shareNow(holder: ConstraintLayout, id: String, data: UploadPost) {
        val ref = FirebaseDatabase.getInstance().getReference("Post")
        ref.child(id).setValue(data).addOnSuccessListener {
            Log.d("Resut","share resuft")
            dialogSuccess()
        }.addOnFailureListener {
            Log.e("Sharenow", "Share post failed")
        }

    }

    private fun dialogSuccess() {
        val diaolog = Dialog(activity)
        diaolog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        diaolog.setContentView(R.layout.custom_success_dialog)
        diaolog.setCancelable(true)
        val view = diaolog.findViewById<ConstraintLayout>(R.id.layoutDialog)
        view.setOnClickListener { diaolog.dismiss() }
        diaolog.show()
        Log.d("Resut","share finish")
    }

    private fun loadPostShare(holder: ViewHolder, idShare: String?) {
        val ref = FirebaseDatabase.getInstance().getReference("Post")
        val postlist = ArrayList<ReadPost>()
        ref.orderByChild("idPost").equalTo(idShare)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (uSnapshot in snapshot.children) {
                            val data = uSnapshot.getValue(ReadPost::class.java)
                            postlist.add(data!!)
                            loadUser(
                                holder.binding.root,
                                postlist[0].idUser,
                                holder.binding.imvAvatar2,
                                holder.binding.tvName2
                            )

                        }
                        holder.binding.tvTitle2.text = postlist[0].title
                        loadDate(postlist[0].dateCreate, holder.binding.tvDateCreate2)
                        Glide.with(holder.binding.root).load(postlist[0].photo)
                            .into(holder.binding.ivPhoto2)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("DatabaseError", error.message)
                }

            })

    }

    private fun loadDate(dateCreate: Long?, tvDateCreate: TextView) {
        val format = SimpleDateFormat("dd/MM/yyyy", getDefault())
        tvDateCreate.text = format.format(dateCreate)
    }

    private fun loadLikeNumber(idPost: String?, like: ImageView, tvLikeNumber: TextView) {
        val fDatabase = FirebaseDatabase.getInstance().getReference("Like")
        fDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(idPost!!).hasChild(mAuth.currentUser!!.uid)) {
                    (snapshot.child(idPost).childrenCount.toString() + " Like").also {
                        tvLikeNumber.text = it
                    }
                    like.setImageResource(R.drawable.ic_like_red)
                } else {
                    (snapshot.child(idPost).childrenCount.toString() + " Like").also {
                        tvLikeNumber.text = it
                    }
                    like.setImageResource(R.drawable.ic_like)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("DatabaseError", error.message)
            }
        })
    }

    private fun loadCommentNumber(idPost: String?, tvCommentNumber: TextView) {
        val fDatabase = FirebaseDatabase.getInstance().getReference("Comment")
        fDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (snapshot.child(idPost!!).childrenCount.toString() + " Comment").also {
                    tvCommentNumber.text = it
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("DatabaseError", error.message)
            }

        })
    }

    private fun loadShareNumber(idPost: String?, tvShareNumber: TextView) {
        val fDatabase = FirebaseDatabase.getInstance().getReference("Post")
        fDatabase.orderByChild("idShare").equalTo(idPost)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    (snapshot.childrenCount.toString() + " Share").also { tvShareNumber.text = it }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("DatabaseError", error.message)
                }

            })
    }

    private fun uploadNofication(mess: String, userId: String) {
        val id = UUID.randomUUID().toString()
        val data = UpNofication(
            id,
            userId,
            mAuth.currentUser!!.uid,
            mess,
            false,
            "Like",
            ServerValue.TIMESTAMP,
            ServerValue.TIMESTAMP
        )
        NoficationClass().UpNofication(data)
    }

    private fun buildDialog(context: ConstraintLayout): AlertDialog.Builder? {
        val builder = AlertDialog.Builder(context.context)
        builder.setTitle("Delete post ")
        builder.setMessage("Are you sure you want to delete the post?")//Bạn có chắc chắn, bạn có muốn đăng xuất không?
        builder.setPositiveButton("Delete") { dialog, which ->
            deletePost(context)
        }
        builder.setNeutralButton("Cancel") { dialog, which -> }
        return builder
    }

    private fun deletePost(context: ConstraintLayout) {
        FirebaseDatabase.getInstance().getReference("Post/$idPost").removeValue()
            .addOnSuccessListener {
                Snackbar.make(context, "Delete post success", Snackbar.LENGTH_SHORT).show()
            }
    }
}
