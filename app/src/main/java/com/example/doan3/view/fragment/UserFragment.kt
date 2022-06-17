package com.example.doan3.view.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.doan3.data.ReadUser
import com.example.doan3.databinding.FragmentUserBinding
import com.example.doan3.view.acticity.LoginActivity
import com.example.doan3.view.acticity.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.ArrayList

class UserFragment : Fragment() {
    private lateinit var binding: FragmentUserBinding
    private lateinit var mAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAuth = FirebaseAuth.getInstance()

        loadDataUser()

        binding.btnLogout.setOnClickListener {
            buildDialog()!!.show()
        }

        binding.linearLayout.setOnClickListener {
            val intent = Intent(requireContext(),ProfileActivity::class.java)
            intent.putExtra("idUser",mAuth.currentUser!!.uid)
            requireContext().startActivity(intent)
        }

    }

    private fun loadDataUser() {
        val profileList = ArrayList<ReadUser>()
        val fDatabase = FirebaseDatabase.getInstance().getReference("User")
        fDatabase.orderByChild("userId").equalTo(mAuth.currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (uSnapshot in snapshot.children) {
                            val data = uSnapshot.getValue(ReadUser::class.java)
                            profileList.add(data!!)
                        }
                        Glide.with(requireActivity()).load(profileList[0].userAvatar)
                            .into(binding.imvAvatar)
                        binding.tvName.text = profileList[0].userName
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    private fun buildDialog(): AlertDialog.Builder? {
        val builder = AlertDialog.Builder(binding.root.context)
        builder.setTitle("Logout")
        builder.setMessage("Are you sure, do you want to logout?")//Bạn có chắc chắn, bạn có muốn đăng xuất không?
        builder.setPositiveButton("Logout") { dialog, which ->
            mAuth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
        builder.setNeutralButton("Cancel") { dialog, which -> }
        return builder
    }

}