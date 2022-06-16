package com.example.doan3.view.fragment

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.doan3.data.ReadUser
import com.example.doan3.data.UploadPost
import com.example.doan3.databinding.FragmentAddPostBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import gun0912.tedimagepicker.builder.TedImagePicker
import java.util.*

class AddPostFragment : Fragment() {

    private lateinit var binding: FragmentAddPostBinding
    private lateinit var fAuth: FirebaseAuth
    private var filePath: Uri? = null
    private var urlImage: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddPostBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fAuth = FirebaseAuth.getInstance()

        binding.constraintLayout5.visibility = View.GONE

        binding.btnPickimage.setOnClickListener { pickImage() }
        binding.btnClear.setOnClickListener { clearImage() }
        binding.btnPost.setOnClickListener { Post() }

        // lấy data user profile
        LoadDatabaseUser()




    }

    private fun LoadDatabaseUser() {
        val profileList = ArrayList<ReadUser>()
        val fDatabase = FirebaseDatabase.getInstance().getReference("User")
        fDatabase.orderByChild("userId").equalTo(fAuth.currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (uSnapshot in snapshot.children) {
                            val data = uSnapshot.getValue(ReadUser::class.java)
                            profileList.add(data!!)
                        }
                        Glide.with(requireActivity()).load(profileList[0].userAvatar)
                            .into(binding.imvAvatar)
                        binding.tvUserName.text = profileList[0].userName
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    buildDialog(
                        tittle = "Error",
                        mess = "An error has occurred during data processing. Please exit and restart the app"
                    )
                }

            })
    }

    private fun Post() {
        if (CheckValidPost()) {
            val idPost = UUID.randomUUID().toString()
            UploadImage(idPost)
        }
    }

    private fun UploadImage(idPost: String) {
        val fStorage = FirebaseStorage.getInstance().getReference("Post/$idPost")
        fStorage.putFile(filePath!!).addOnSuccessListener {
            fStorage.downloadUrl.addOnSuccessListener {
                urlImage  = it.toString()
                Log.d("dowloadUrlImage", "Dowload url image success")
                uploadData(idPost)
            }.addOnFailureListener {
                Log.e("dowloadUrlImage", "Dowload url image failure : $it " )
                buildDialog(
                    tittle = "Error",
                    mess = "There was an error while posting. Please try again later"
                )

            }
        }
    }

    private fun uploadData(id: String) {
        val data = UploadPost(id,"Post",null, fAuth.currentUser!!.uid,binding.edtTitle.text.toString(),urlImage,ServerValue.TIMESTAMP,ServerValue.TIMESTAMP)
        val fDatabase = FirebaseDatabase.getInstance().getReference("Post/$id")
        fDatabase.setValue(data).addOnSuccessListener {
            Log.d("uploadPost", "Upload post success")
            Snackbar.make(
                binding.root,
                "Successfully added new post",
                Snackbar.LENGTH_LONG
            ).show()
/*
            findNavController().navigate(com.example.doan3.R.id.action_addPostFragment_to_homeFragment)
*/
        }
    }

    private fun CheckValidPost(): Boolean {
        if (binding.edtTitle.text.isEmpty()) {
            Snackbar.make(binding.root, "TitLe not entered", Snackbar.LENGTH_SHORT).show()
            return false
        }
        if (filePath == null) {
            Snackbar.make(
                binding.root,
                "The post's photo has not been selected",
                Snackbar.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }

    private fun pickImage() {
        TedImagePicker.with(requireContext()).start { uri ->
            Glide.with(requireActivity()).load(uri).into(binding.imPhoto)
            filePath = uri
            binding.btnPickimage.visibility = View.GONE
            binding.constraintLayout5.visibility = View.VISIBLE

        }
    }

    private fun clearImage() {
        binding.imPhoto.setImageResource(0)
        binding.constraintLayout5.visibility = View.GONE
        binding.btnPickimage.visibility = View.VISIBLE
        filePath = null
    }

    private fun buildDialog(tittle: String, mess: String): AlertDialog.Builder? {
        val builder = AlertDialog.Builder(binding.root.context)
        builder.setTitle(tittle)
        builder.setMessage(mess)//đang xảy ra lỗi trong quá trình xử lí dữ liệu. vui lòng thoát và khởi động lại app
        return builder
    }


}