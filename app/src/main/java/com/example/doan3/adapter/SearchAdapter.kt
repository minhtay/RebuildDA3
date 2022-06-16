package com.example.doan3.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
        holder.binding.tvUserName.text = name

        holder.binding.view.setOnClickListener(object :View.OnClickListener{
            override fun onClick(p0: View?) {
                val intent = Intent(activity,ProfileActivity::class.java)
                intent.putExtra("idUser",idUser)
                holder.binding.root.context.startActivity(intent)

            }

        })
        mAth = FirebaseAuth.getInstance()
        if (idUser==mAth.currentUser!!.uid){
            holder.binding.tvFollow.visibility = View.VISIBLE
            holder.binding.tvFollow.text = "You"
        }else checkFollowing(idUser,holder)

    }

    private fun checkFollowing(idUser: String?, holder: ViewHolder) {
        var follow = 0
        val fDatabase = FirebaseDatabase.getInstance().getReference("Following")
        fDatabase.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(mAth.currentUser!!.uid).hasChild(idUser!!)){
                    holder.binding.tvFollow.visibility = View.VISIBLE
                    holder.binding.tvFollow.text = "Following"
                    follow += 1
                    checkFollower(idUser,holder,follow)
                }else{
                    checkFollower(idUser,holder,follow)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun checkFollower(idUser: String?, holder: ViewHolder, follow: Int) {
       val fDatabase = FirebaseDatabase.getInstance().getReference("Follower")
       fDatabase.addValueEventListener(object :ValueEventListener{
           override fun onDataChange(snapshot: DataSnapshot) {
               if (snapshot.child(mAth.currentUser!!.uid).hasChild(idUser!!)){
                   holder.binding.tvFollow.visibility = View.VISIBLE
                   if (follow==1){
                       holder.binding.tvFollow.visibility = View.VISIBLE
                       holder.binding.tvFollow.text = "Followed"
                   }else{
                       holder.binding.tvFollow.visibility = View.VISIBLE
                       holder.binding.tvFollow.text = "Follower"
                   }
               }
           }

           override fun onCancelled(error: DatabaseError) {
           }
       })
   }
}