package com.example.doan3.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.doan3.R
import com.example.doan3.data.ReadUser
import com.example.doan3.databinding.ItemUserBinding
import com.example.doan3.view.acticity.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SearchAdapter(val activity: Context, val searchList: ArrayList<ReadUser>) :
    RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    private lateinit var mAth: FirebaseAuth

    class ViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemUserBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return searchList.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val avatar = searchList[position].userAvatar
        val name = searchList[position].userName
        val idUser = searchList[position].userId

        Glide.with(holder.binding.root.context).load(avatar).into(holder.binding.imvAvatar)
        holder.binding.tvname.text = name

        holder.binding.itemClick.setOnClickListener(object :View.OnClickListener{
            override fun onClick(p0: View?) {
                val intent = Intent(activity,ProfileActivity::class.java)
                intent.putExtra("idUser",idUser)
                holder.binding.root.context.startActivity(intent)

            }

        })
        mAth = FirebaseAuth.getInstance()
        holder.binding.btnFollow.setOnClickListener{Follow(idUser)}
        DrawableFollow(idUser,holder)
    }

    private fun DrawableFollow(idUser: String?, holder: ViewHolder) {
        val fDatabase = FirebaseDatabase.getInstance().getReference("Following")
        fDatabase.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(mAth.currentUser!!.uid).hasChild(idUser!!)){
                    holder.binding.btnFollow.setBackgroundColor(ContextCompat.getColor(activity, R.color.follow))
                    holder.binding.btnFollow.setText("Following")
                    holder.binding.btnFollow.setTextColor(Color.WHITE)
                }else{
                    holder.binding.btnFollow.setBackgroundColor(Color.WHITE)
                    holder.binding.btnFollow.setText("Follow")
                    holder.binding.btnFollow.setTextColor(Color.BLACK)

                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun Follow(idUser: String?) {
        var like = true
        val fDatabase = FirebaseDatabase.getInstance().getReference("Following")
        fDatabase.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
               if(like == true){
                   if (snapshot.child(mAth.currentUser!!.uid).hasChild(idUser!!)){
                       fDatabase.child(mAth.currentUser!!.uid).child(idUser!!).removeValue()
                       DeleteFollower(idUser)
                       like = false

                   }else{
                       fDatabase.child(mAth.currentUser!!.uid).child(idUser!!).setValue(true)
                       AddFollower(idUser)
                       like = false
                   }
               }
            }



            override fun onCancelled(error: DatabaseError) {
            }
        })

    }

    private fun AddFollower(idUser: String) {
        val fDatabase1 = FirebaseDatabase.getInstance().getReference("Follower")
        fDatabase1.child(idUser!!).child(mAth.currentUser!!.uid).setValue(true)

    }

    private fun DeleteFollower(idUser: String) {
        val fDatabase2 = FirebaseDatabase.getInstance().getReference("Follower")
        fDatabase2.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(mAth.currentUser!!.uid).hasChild(idUser!!)){
                    fDatabase2.child(idUser!!).child(mAth.currentUser!!.uid).removeValue()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}