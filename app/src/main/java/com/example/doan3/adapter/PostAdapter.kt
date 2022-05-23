package com.example.doan3.adapter

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.doan3.R
import com.example.doan3.data.ReadPost
import com.example.doan3.data.ReadUser
import com.example.doan3.data.UpNofication
import com.example.doan3.data.UploadPost
import com.example.doan3.databinding.ItemPostBinding
import com.example.doan3.view.acticity.CommentActivity
import com.example.doan3.view.acticity.PostActivity
import com.example.doan3.view.acticity.SharePostActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PostAdapter(val activity: Context, val postList: ArrayList<ReadPost>) :
    RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    private lateinit var mAuth: FirebaseAuth

    class ViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemPostBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        Log.d("DatabaseError", postList.size.toString())

        return postList.size
    }

    @RequiresApi(Build.VERSION_CODES.Q)
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

        LoadUser(holder.binding.root, idUser, holder.binding.imvAvatar, holder.binding.tvName)

        // chuyển đổi và hiển thị dateCreate
        LoadDate(dateCreate, holder.binding.tvDateCreate)

        LoadLikeNumber(idPost, holder.binding.like, holder.binding.tvLikeNumber)

        LoadCommentNumber(idPost, holder.binding.tvCommentNumber)

        LoadShareNumber(idPost, holder.binding.tvShareNumber)

        holder.binding.detailsLayout.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val intent = Intent(holder.binding.root.context, PostActivity::class.java)
                intent.putExtra("idPost", idPost)
                holder.binding.root.context.startActivity(intent)
            }
        })

        // xét sự kiện cho btn like
        holder.binding.btnLike.setOnClickListener(object : View.OnClickListener {
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
                                UpNofication("liked your post", idUser!!)
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

        holder.binding.btnComment.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                val intent = Intent(holder.binding.root.context, CommentActivity::class.java)
                intent.putExtra("idPost", idPost)
                holder.binding.root.context.startActivity(intent)
            }

        })

        if (title == null) {
            holder.binding.tvTitle.visibility = View.GONE
        }

        if (typePost == "Post") {
            holder.binding.layoutShare.visibility = View.GONE
            holder.binding.tvTitle.text = title
            Glide.with(holder.binding.root).load(photo).into(holder.binding.imPhoto)

        } else {
            holder.binding.tvTypePost.text = "Shared a post"
            holder.binding.tvTitle.text = title
            LoadPostShare(holder, idShare)
        }

        holder.binding.btnShare.setOnClickListener {
            val popupMenu = PopupMenu(holder.binding.btnLike.context, holder.binding.root)
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.shareNow -> {
                        Log.d("menuShare", "1")
                        val id = UUID.randomUUID().toString()
                        if (typePost == "Post") {
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
                            ShareNow(holder, id, data)

                        } else {
                            var data = UploadPost(
                                id,
                                "Share",
                                idShare,
                                mAuth.currentUser?.uid,
                                null,
                                null,
                                ServerValue.TIMESTAMP,
                                ServerValue.TIMESTAMP
                            )
                            ShareNow(holder, id, data)

                        }
                    }
                    R.id.moreOption -> {
                        Log.d("menuShare", "2")
                        val intent =
                            Intent(holder.binding.root.context, SharePostActivity::class.java)
                        if (typePost == "Post") {
                            intent.putExtra("idPost", idPost)
                        } else {
                            intent.putExtra("idPost", idShare)
                        }
                        holder.binding.root.context.startActivity(intent)
                    }
                }
                false
            }
            popupMenu.inflate(R.menu.menu_post)
            popupMenu.gravity = Gravity.RIGHT
            popupMenu.setForceShowIcon(true)
            popupMenu.show()
        }

    }

    private fun LoadUser(
        context: CardView,
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

    private fun ShareNow(holder: ViewHolder, id: String, data: UploadPost) {
        val ref = FirebaseDatabase.getInstance().getReference("Post")
        ref.child(id).setValue(data).addOnSuccessListener {
            Snackbar.make(
                holder.binding.root,
                "Share post success",
                Snackbar.LENGTH_SHORT
            ).show()
        }.addOnFailureListener {
            Log.e("Sharenow", "Share post failed")
        }
    }

    private fun LoadPostShare(holder: ViewHolder, idShare: String?) {
        val ref = FirebaseDatabase.getInstance().getReference("Post")
        val postlist = ArrayList<ReadPost>()
        ref.orderByChild("idPost").equalTo(idShare)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (uSnapshot in snapshot.children) {
                            val data = uSnapshot.getValue(ReadPost::class.java)
                            postlist.add(data!!)
                            LoadUser(
                                holder.binding.root,
                                postlist[0].idUser,
                                holder.binding.imvAvatar2,
                                holder.binding.tvName2
                            )

                        }
                        holder.binding.tvTitle2.text = postlist[0].title
                        Log.d("1234", postlist[0].title!!)
                        LoadDate(postlist[0].dateCreate, holder.binding.tvDateCreate2)
                        Glide.with(holder.binding.root).load(postlist[0].photo)
                            .into(holder.binding.ivPhoto2)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("DatabaseError", error.message)
                }

            })

    }

    private fun LoadDate(dateCreate: Long?, tvDateCreate: TextView) {
        val format = SimpleDateFormat("dd/MM/yyyy")
        tvDateCreate.text = format.format(dateCreate)
    }

    private fun LoadLikeNumber(idPost: String?, like: ImageView, tvLikeNumber: TextView) {
        val fDatabase = FirebaseDatabase.getInstance().getReference("Like")
        fDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(idPost!!).hasChild(mAuth.currentUser!!.uid)) {
                    tvLikeNumber.text = snapshot.child(idPost).childrenCount.toString() + " Like"
                    like.setImageResource(R.drawable.ic_like_red)
                } else {
                    tvLikeNumber.text = snapshot.child(idPost).childrenCount.toString() + " Like"
                    like.setImageResource(R.drawable.ic_like)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("DatabaseError", error.message)
            }
        })
    }

    private fun LoadCommentNumber(idPost: String?, tvCommentNumber: TextView) {
        val fDatabase = FirebaseDatabase.getInstance().getReference("Comment")
        fDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tvCommentNumber.text =
                    snapshot.child(idPost!!).childrenCount.toString() + " Comment"
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("DatabaseError", error.message)
            }

        })
    }

    private fun LoadShareNumber(idPost: String?, tvShareNumber: TextView) {
        val fDatabase = FirebaseDatabase.getInstance().getReference("Post")
        fDatabase.orderByChild("idShare").equalTo(idPost)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    tvShareNumber.text = snapshot.childrenCount.toString() + " Share"
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("DatabaseError", error.message)
                }

            })
    }
    private fun UpNofication(mess:String, userId:String){
        val id = UUID.randomUUID().toString()
        val data = UpNofication(mAuth.currentUser!!.uid,id,mess,false,ServerValue.TIMESTAMP,ServerValue.TIMESTAMP)
        val fDatabase = FirebaseDatabase.getInstance().getReference("Notification/$userId/$id")
        fDatabase.setValue(data).addOnSuccessListener {
            Log.d("uploadPost", "Upload nofication success")
        }
    }

    /*private fun DeleteNofication()*/

}
