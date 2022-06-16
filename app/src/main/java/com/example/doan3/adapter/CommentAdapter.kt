package com.example.doan3.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.doan3.R
import com.example.doan3.data.ReadCommennt
import com.example.doan3.data.ReadUser
import com.example.doan3.databinding.ItemCommentBinding
import com.example.doan3.util.Utils
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat

class CommentAdapter (val activity: Context, val commentList: ArrayList<ReadCommennt>) :
    RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    private lateinit var mAth: FirebaseAuth

    class ViewHolder(val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemCommentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return commentList.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val idComment = commentList[position].idComment
        val idPost = commentList[position].idPost
        val idUser = commentList[position].idUser
        val comment = commentList[position].comment
        val dateCreate = commentList[position].dateCreate
        val dateUpdate = commentList[position].dateUpdate

        mAth = FirebaseAuth.getInstance()

        val ref = FirebaseDatabase.getInstance().getReference("User")
        val profileList = ArrayList<ReadUser>()
        ref.orderByChild("userId").equalTo(idUser)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (uSnapshot in snapshot.children) {
                            val data = uSnapshot.getValue(ReadUser::class.java)
                            profileList.add(data!!)
                            Glide.with(holder.binding.root.context).load(profileList[0].userAvatar)
                                .into(holder.binding.imvAvatar)
                            val name = profileList[0].userName
                            holder.binding.tvComment.text = SpanString(name, comment)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("DatabaseError", error.message)
                }

            })

        val format = SimpleDateFormat("MM/dd/yyyy")
        holder.binding.tvDateCreate.text = format.format(dateCreate)

        holder.binding.btnMenu.setOnClickListener(object : View.OnClickListener {
            @RequiresApi(Build.VERSION_CODES.Q)
            override fun onClick(p0: View?) {
                val popupMenu = PopupMenu(holder.binding.btnMenu.context, holder.binding.root)
                popupMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.delete -> {
                            if (mAth.currentUser!!.uid == idUser) {
                                buildDialog(holder, p0, idPost, idComment)!!.show()
                            } else {
                                Snackbar.make(
                                    holder.binding.root,
                                    "You cannot delete comments",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }

                        }
                        R.id.edit -> {
                            if (mAth.currentUser!!.uid == idUser) {
                                holder.binding.linearLayout3.visibility = View.GONE
                                holder.binding.btnMenu.visibility = View.GONE
                                holder.binding.constraint1.visibility = View.VISIBLE
                                holder.binding.btnEditComent.visibility = View.VISIBLE
                                holder.binding.btnCancel.visibility = View.VISIBLE
                                holder.binding.edtEditComment.setText(comment)
                            } else {
                                Snackbar.make(
                                    holder.binding.root,
                                    "You cannot delete comments",
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

        holder.binding.btnEditComent.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (holder.binding.edtEditComment.text.length > 0) {
                    val eDatabase = FirebaseDatabase.getInstance().getReference("Comment")
                    eDatabase.child(idPost!!).child(idComment!!).child("comment")
                        .setValue(holder.binding.edtEditComment.text.toString())
                        .addOnSuccessListener {
                            Snackbar.make(
                                holder.binding.root,
                                "Update comment successfully",
                                Snackbar.LENGTH_SHORT
                            ).show()
                            Utils.hideSoftKeyboard(activity,holder.binding.root)
                            LayoutDefault(holder)
                        }
                } else {
                    Snackbar.make(
                        holder.binding.root,
                        "No comments have been entered yet",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        })

        holder.binding.btnCancel.setOnClickListener { LayoutDefault(holder) }
    }



    private fun LayoutDefault(holder: CommentAdapter.ViewHolder) {
        holder.binding.linearLayout3.visibility = View.VISIBLE
        holder.binding.btnMenu.visibility = View.VISIBLE
        holder.binding.constraint1.visibility = View.GONE
        holder.binding.btnEditComent.visibility = View.GONE
        holder.binding.btnCancel.visibility = View.GONE
    }

    private fun SpanString(name: String?, comment: String?): SpannableStringBuilder {
        val span = SpannableStringBuilder(name + "  " + comment)
        span.setSpan(StyleSpan(Typeface.BOLD), 0, name!!.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            .toString()
        span.setSpan(RelativeSizeSpan(1.2f), 0, name!!.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            .toString()
        span.setSpan(
            ForegroundColorSpan(Color.BLACK),
            0,
            name!!.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        ).toString()
        return span
    }

    private fun buildDialog(
        holder: ViewHolder,
        p0: View?,
        idPost: String?,
        idComment: String?
    ): AlertDialog.Builder? {
        val builder = AlertDialog.Builder(holder.binding.root.context)
        builder.setTitle("Delete comment")
        builder.setMessage("Do you want to delete this comment?")
        builder.setPositiveButton("Delete") { dialog, which ->
            val ref = FirebaseDatabase.getInstance().getReference("Comment")
            ref.child(idPost!!).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild(idComment!!)) {
                        ref.child(idPost).child(idComment).removeValue()
                        Snackbar.make(
                            holder.binding.root,
                            "Delete comment successfully",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("DatabaseError", error.message)
                }
            })
        }
        builder.setNeutralButton("Cancel"){ dialog, which ->}

        return builder
    }

}
