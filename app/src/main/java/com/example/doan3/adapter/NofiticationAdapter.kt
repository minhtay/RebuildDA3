package com.example.doan3.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.doan3.R
import com.example.doan3.data.ReadNofication
import com.example.doan3.data.ReadUser
import com.example.doan3.databinding.ItemNoficationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat

class NofiticationAdapter(val activity: Context, val noficationList: ArrayList<ReadNofication>) :
    RecyclerView.Adapter<NofiticationAdapter.ViewHolder>() {

    private lateinit var mAth: FirebaseAuth

    class ViewHolder(val binding: ItemNoficationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemNoficationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return noficationList.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val idNofication = noficationList[position].idNofication
        val idUserReceive = noficationList[position].idUserReceive
        val idUserSend = noficationList[position].idUserSend
        val message = noficationList[position].message
        val dateCreate = noficationList[position].dateCreate
        val status = noficationList[position].status

        val format = SimpleDateFormat("dd/MM/yyyy")
        holder.binding.tvDateCreate.text = format.format(dateCreate)
        if(status== false) {
            holder.binding.layout.setBackgroundColor(ContextCompat.getColor(activity,R.color.nofication))
        }

        mAth = FirebaseAuth.getInstance()

        val ref = FirebaseDatabase.getInstance().getReference("User")
        val profileList = ArrayList<ReadUser>()
        ref.orderByChild("userId").equalTo(idUserSend)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (uSnapshot in snapshot.children) {
                            val data = uSnapshot.getValue(ReadUser::class.java)
                            profileList.add(data!!)
                            Glide.with(holder.binding.root.context).load(profileList[0].userAvatar)
                                .into(holder.binding.imvAvatar)
                            val name = profileList[0].userName
                            holder.binding.tvMess.text = SpanString(name, message)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("DatabaseError", error.message)
                }

            })

        holder.binding.layout.setOnClickListener {
            if (status==false){
                FirebaseDatabase.getInstance().getReference("Nofication/${mAth.currentUser!!.uid}/$idNofication").child("status").setValue(true)
            }
        }

    }



    private fun SpanString(name: String?, mess: String?): SpannableStringBuilder {
        val span = SpannableStringBuilder(name + "  " + mess)
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
}