package com.example.doan3.adapter

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.doan3.R
import com.example.doan3.data.readCommennt
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

class CommentAdapter(val activity: Context, val commentList: ArrayList<readCommennt>) :
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
                            holder.binding.tvUserName.text = profileList[0].userName
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("DatabaseError", error.message)
                }

            })

        holder.binding.tvComent.text = comment

        val format = SimpleDateFormat("MM/dd/yyyy")
        holder.binding.tvDateCreate.text = format.format(dateCreate)

        if (idUser != mAth.currentUser!!.uid) {
            holder.binding.btnMenu.isEnabled = false
            holder.binding.btnMenu.setImageResource(0)
        }
        holder.binding.btnMenu.setOnClickListener(object : View.OnClickListener {
            @RequiresApi(Build.VERSION_CODES.Q)
            override fun onClick(p0: View?) {
                val popupMenu = PopupMenu(holder.binding.root.context, holder.binding.btnMenu)
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
                            /* holder.binding.linearLayout3.visibility = View.GONE
                             holder.binding.btnMenu.visibility = View.GONE
                             holder.binding.constraint1.visibility = View.VISIBLE
                             holder.binding.btnUpdate.visibility = View.VISIBLE
                             holder.binding.btnCancel.visibility = View.VISIBLE
                             holder.binding.edtEditComment.setText(comment)*/
                            holder.binding.layoutBtnEdit.visibility = View.VISIBLE
                            holder.binding.edtEditComment.visibility = View.VISIBLE
                            holder.binding.tvComent.visibility = View.GONE
                            holder.binding.tvUserName.visibility = View.GONE
                            holder.binding.btnMenu.visibility = View.GONE
                            holder.binding.edtEditComment.setText(comment)

                        }
                    }
                    false
                }
                popupMenu.inflate(R.menu.menu_comment)
                popupMenu.gravity = Gravity.BOTTOM
                popupMenu.setForceShowIcon(true)
                popupMenu.show()
            }
        })

        holder.binding.btnUpdate.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val text = holder.binding.edtEditComment.text.toString()
                if (holder.binding.edtEditComment.text.length > 0) {
                    val eDatabase = FirebaseDatabase.getInstance().getReference("Comment")
                    eDatabase.child(idPost!!).child(idComment!!).child("comment")
                        .setValue(text)
                        .addOnSuccessListener {
                            dialogSuccess()
                            Utils.hideSoftKeyboard(activity, holder.binding.root)
                            holder.binding.layoutBtnEdit.visibility = View.GONE
                            holder.binding.edtEditComment.visibility = View.GONE
                            holder.binding.tvComent.visibility = View.VISIBLE
                            holder.binding.tvUserName.visibility = View.VISIBLE
                            holder.binding.tvComent.setText(text)

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

        holder.binding.btnCancel.setOnClickListener {
            holder.binding.layoutBtnEdit.visibility = View.GONE
            holder.binding.btnMenu.visibility = View.GONE
            holder.binding.edtEditComment.visibility = View.GONE
            holder.binding.tvComent.visibility = View.VISIBLE
            holder.binding.tvUserName.visibility = View.VISIBLE

        }
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
        builder.setNeutralButton("Cancel") { dialog, which -> }

        return builder
    }

    private fun dialogSuccess() {
        val diaolog = Dialog(activity)
        diaolog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        diaolog.setContentView(R.layout.dialog_success)
        diaolog.setCancelable(true)
        val view = diaolog.findViewById<ConstraintLayout>(R.id.layoutDialog)
        view.setOnClickListener { diaolog.dismiss() }
        diaolog.show()
        Log.d("Resut", "share finish")
    }

}
