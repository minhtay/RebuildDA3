package com.example.doan3.view.fragment

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.doan3.adapter.PostAdapter
import com.example.doan3.data.ReadPost
import com.example.doan3.data.ReadUser
import com.example.doan3.data.UploadPost
import com.example.doan3.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import gun0912.tedimagepicker.builder.TedImagePicker
import java.util.*
import kotlin.collections.ArrayList

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val userList = ArrayList<ReadUser>()
    private val postList = ArrayList<ReadPost>()
    private lateinit var fAuth: FirebaseAuth
    private var filePath: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    @SuppressLint("ResourceAsColor")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fAuth = FirebaseAuth.getInstance()


        getDataPost()
        createRecycler()
        getDataUser()

        // set even click view
        binding.btnChoosePhoto.setOnClickListener { btnChoosePhoto() }
        binding.btnPost.setOnClickListener { btnPost() }

    }

    private fun btnPost() {
        if (binding.edtTitle.text!!.isNotEmpty() && filePath != null) {
            upImagePost()
        } else {
            Snackbar.make(
                binding.root,
                "Title and image have not been imported. Please check again",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun upImagePost() {
        val id = UUID.randomUUID().toString()
        val fStorage = FirebaseStorage.getInstance().getReference("Post/$id")
        fStorage.putFile(filePath!!)
            .addOnSuccessListener {
                fStorage.downloadUrl.addOnSuccessListener {
                    upDataPost(id, it.toString())
                }.addOnFailureListener {
                    Snackbar.make(
                        binding.root,
                        "Upload post is fail. Please restart the app and check again",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }.addOnFailureListener {
                Snackbar.make(
                    binding.root,
                    "Upload post is fail. Please restart the app and check again",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
    }

    private fun upDataPost(id: String, url: String) {
        val data = UploadPost(
            id,
            "Post",
            null,
            fAuth.currentUser!!.uid,
            binding.edtTitle.text.toString(),
            url,
            ServerValue.TIMESTAMP,
            ServerValue.TIMESTAMP
        )
        FirebaseDatabase.getInstance().getReference("Post/$id").setValue(data)
            .addOnSuccessListener {
                Snackbar.make(
                    binding.root,
                    "Successfully added new post",
                    Snackbar.LENGTH_LONG
                ).show()
                resetView()
            }.addOnFailureListener {
                Snackbar.make(
                    binding.root,
                    "Upload post is fail. Please restart the app and check again",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
    }

    private fun resetView() {
        binding.edtTitle.text = null
        binding.imPhoto.setImageResource(0)
        binding.imPhoto.visibility = View.GONE
    }


    private fun btnChoosePhoto() {
        TedImagePicker.with(requireContext()).start { uri ->
            binding.imPhoto.visibility = View.VISIBLE
            Glide.with(requireContext()).load(uri).into(binding.imPhoto)
            filePath = uri
        }
    }

    private fun getDataUser() {
        FirebaseDatabase.getInstance().getReference("User").orderByChild("userId")
            .equalTo(fAuth.currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (uSnapshot in snapshot.children) {
                            val data = uSnapshot.getValue(ReadUser::class.java)
                            userList.add(data!!)
                        }
                    }
                    Glide.with(this@HomeFragment).load(userList[0].userAvatar)
                        .into(binding.imgAvatar)
                    binding.tvName.text = userList[0].userName
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DataUser", error.message)
                }
            })
    }

    private fun createRecycler() {
        binding.rcvHome.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        (binding.rcvHome.layoutManager as LinearLayoutManager).reverseLayout = true
        (binding.rcvHome.layoutManager as LinearLayoutManager).stackFromEnd = true
        binding.rcvHome.setHasFixedSize(true)
        binding.rcvHome.isNestedScrollingEnabled = false
    }

    private fun getDataPost() {
        FirebaseDatabase.getInstance().getReference("Post").orderByChild("dateCreate")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        postList.clear()
                        for (uSnapshot in snapshot.children) {
                            val data = uSnapshot.getValue(ReadPost::class.java)
                            postList.add(data!!)
                        }

                    }
                    binding.rcvHome.adapter = PostAdapter(requireActivity(), postList)
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("DataPost", error.message)
                }

            })
    }
}