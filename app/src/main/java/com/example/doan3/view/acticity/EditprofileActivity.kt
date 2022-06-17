package com.example.doan3.view.acticity

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.doan3.data.ReadUser
import com.example.doan3.data.UpUserData
import com.example.doan3.databinding.ActivityEditprofileBinding
import com.example.doan3.util.Utils
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import gun0912.tedimagepicker.builder.TedImagePicker
import kotlin.collections.ArrayList

class EditprofileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditprofileBinding
    private lateinit var fAuth: FirebaseAuth
    private var filePath: Uri? = null
    private lateinit var profileList: ArrayList<ReadUser>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditprofileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fAuth = FirebaseAuth.getInstance()

        loadProfile()

        binding.btnBack.setOnClickListener { finish() }
        binding.btnPickAvatar.setOnClickListener { pickAvatar() }
        binding.btnUpdate.setOnClickListener {
            updateProfile()
        }
    }

    private fun loadProfile() {
        profileList = ArrayList<ReadUser>()
        val fDatabase = FirebaseDatabase.getInstance().getReference("User")
        fDatabase.orderByChild("userId").equalTo(fAuth.currentUser!!.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (uSnapshot in snapshot.children) {
                            val data = uSnapshot.getValue(ReadUser::class.java)
                            profileList.add(data!!)
                        }
                        Glide.with(applicationContext).load(profileList[0].userAvatar)
                            .into(binding.imvAvatar)
                        binding.edtUserName.setText(profileList[0].userName)
                        if (profileList[0].bio != null) {
                            binding.edtBio.setText(profileList[0].userName)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    "Error".buildDialogError(
                        mess = "An error has occurred during data processing. Please exit and restart the app"
                    )
                }

            })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!this.isDestroyed()) {
            Glide.with(this@EditprofileActivity).pauseRequests()
        }
    }

    private fun updateProfile() {
        /*if (filePath != null) {
            val fStorage =
                FirebaseStorage.getInstance().getReference("User/${fAuth.currentUser!!.uid}")
            fStorage.putFile(filePath!!).addOnSuccessListener {
                fStorage.downloadUrl.addOnSuccessListener {
                    FirebaseDatabase.getInstance().getReference("User/${fAuth.currentUser!!.uid}")
                        .child("userAvatar").setValue(it.toString())
                }
            }
        }
        if (userName!=binding.edtUserName.text.toString()&&binding.edtUserName.text.isNotEmpty()){
            FirebaseDatabase.getInstance().getReference("User/${fAuth.currentUser!!.uid}")
                .child("userName").setValue(binding.edtUserName.text).addOnSuccessListener {
                    binding.edtUserName.clearFocus()
                    Utils.hideSoftKeyboard(this, binding.root)
                }
        }
        if (bio!=null){
            if (binding.edtBio.text.isNotEmpty()&&binding.edtBio.text.toString() != bio){
                FirebaseDatabase.getInstance().getReference("User/${fAuth.currentUser!!.uid}")
                    .child("bio").setValue(binding.edtBio.text).addOnSuccessListener {
                        binding.edtBio.clearFocus()
                        Utils.hideSoftKeyboard(this, binding.root)
                    }
            }
        }else{
            if (binding.edtBio.text.isNotEmpty()){
                FirebaseDatabase.getInstance().getReference("User/${fAuth.currentUser!!.uid}")
                    .child("bio").setValue(binding.edtBio.text).addOnSuccessListener {
                        binding.edtBio.clearFocus()
                        Utils.hideSoftKeyboard(this, binding.root)
                    }
            }else
        }*/
        if (filePath != null) {
            val fStorage =
                FirebaseStorage.getInstance().getReference("User/${fAuth.currentUser!!.uid}")
            fStorage.putFile(filePath!!).addOnSuccessListener {
                fStorage.downloadUrl.addOnSuccessListener {
                    val avatar = it.toString()
                    updateName(avatar)
                }
            }
        } else updateName(profileList[0].userAvatar.toString())


    }

    private fun updateName(avatar: String) {
        if (profileList[0].userName != binding.edtUserName.text.toString() && binding.edtUserName.text.isNotEmpty()) {
            val name = binding.edtUserName.text.toString()
            binding.edtUserName.clearFocus()
            Utils.hideSoftKeyboard(this, binding.root)
            updateBio(avatar, name)
        } else {

        }
    }

    private fun updateBio(avatar: String, name: String) {
        if (profileList[0].bio != null) {
            if (profileList[0].bio != binding.edtBio.text.toString() && binding.edtBio.text.isNotEmpty()) {
                val data = UpUserData(
                    ServerValue.TIMESTAMP,
                    ServerValue.TIMESTAMP,
                    profileList[0].userId,
                    name,
                    avatar,
                    profileList[0].userEmail,
                    binding.edtBio.text.toString()
                )
                Upload(data)
            }
        } else {
            if (binding.edtBio.text.isNotEmpty()) {
                val data = UpUserData(
                    ServerValue.TIMESTAMP,
                    ServerValue.TIMESTAMP,
                    profileList[0].userId,
                    name,
                    avatar,
                    profileList[0].userEmail,
                    binding.edtBio.text.toString()
                )
                Upload(data)
            }
        }
    }

    private fun Upload(data: UpUserData) {
        FirebaseDatabase.getInstance().getReference("User/${fAuth.currentUser!!.uid}")
            .setValue(data).addOnSuccessListener {
                Snackbar.make(binding.root,"Update success",Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun pickAvatar() {
        TedImagePicker.with(this).start { uri ->
            Glide.with(this).load(uri).into(binding.imvAvatar)
            filePath = uri
            Log.d("editprofile", "uri: $filePath")
        }
    }

    private fun String.buildDialogError(mess: String): AlertDialog.Builder {
        val builder = AlertDialog.Builder(binding.root.context)
        builder.setTitle(this)
        builder.setMessage(mess)
        return builder
    }
}