package com.example.doan3.view.acticity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.doan3.data.ReadUser
import com.example.doan3.data.UpUserData
import com.example.doan3.databinding.ActivityEditprofileBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import gun0912.tedimagepicker.builder.TedImagePicker
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timerTask

class EditprofileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditprofileBinding
    private lateinit var fAuth : FirebaseAuth
    private var filePath: Uri? = null
    private var urlImage: String? = null
    private var email: String? = null
    private var avatar: String? = null
    private var bio: String? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditprofileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fAuth = FirebaseAuth.getInstance()

        LoadProfile()

        binding.btnBack.setOnClickListener { buildDialog()!!.show() }
        binding.btnPickAvatar.setOnClickListener { PickAvatar() }
        binding.btnUpdate.setOnClickListener { DeleteAvatar() }
    }

    private fun LoadProfile() {
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
                        avatar = profileList[0].userAvatar
                        Glide.with(this@EditprofileActivity).load(profileList[0].userAvatar)
                            .into(binding.imvAvatar)
                        binding.edtUserName.setText(profileList[0].userName)
                        bio = profileList[0].bio
                        binding.edtBio.setText(bio)

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    buildDialogError(
                        tittle = "Error",
                        mess = "An error has occurred during data processing. Please exit and restart the app"
                    )
                }

            })
    }

    private fun DeleteAvatar() {
        if (filePath==null){
            uploadData(avatar!!)
        }else{
        val fStorage = FirebaseStorage.getInstance().getReference("User")
        fStorage.child(fAuth.currentUser!!.uid).delete().addOnSuccessListener{
            UploadImage()
        }}
    }

    private fun PickAvatar() {
        TedImagePicker.with(this).start { uri ->
            Glide.with(this).load(uri).into(binding.imvAvatar)
            filePath = uri
        }
    }
    private fun UploadImage() {
        val fStorage = FirebaseStorage.getInstance().getReference("User/${fAuth.currentUser!!.uid}")
        fStorage.putFile(filePath!!).addOnSuccessListener {
            fStorage.downloadUrl.addOnSuccessListener {
                urlImage  = it.toString()
                Log.d("dowloadUrlImage", "Dowload url image success")
                uploadData( urlImage!!)
            }.addOnFailureListener {
                Log.e("dowloadUrlImage", "Dowload url image failure : $it " )
                buildDialogError(
                    tittle = "Error",
                    mess = "There was an error while posting. Please try again later"
                )

            }
        }
    }

    private fun uploadData(avatar:String) {
        bio = binding.edtBio.text.toString()
        val data =
        UpUserData(ServerValue.TIMESTAMP, ServerValue.TIMESTAMP, fAuth.currentUser!!.uid, binding.edtUserName.text.toString(), avatar,bio)
        val ref = FirebaseDatabase.getInstance().getReference("User")
        ref.child(fAuth.currentUser!!.uid).setValue(data!!).addOnSuccessListener{
            Snackbar.make(
                binding.root,
                "Successfully update profile",
                Snackbar.LENGTH_LONG
            ).show()
            binding.edtBio.setText(bio)
            val intent = Intent(this,ProfileActivity::class.java)
            Timer().schedule(timerTask { // hàm delay time chuyển màn hình
                intent.putExtra("idUser",fAuth.currentUser!!.uid)
                startActivity(intent)
                finish()
            },1500 )
        }
    }

    private fun buildDialogError(tittle: String, mess: String): AlertDialog.Builder {
        val builder = AlertDialog.Builder(binding.root.context)
        builder.setTitle(tittle)
        builder.setMessage(mess)//đang xảy ra lỗi trong quá trình xử lí dữ liệu. vui lòng thoát và khởi động lại app
        return builder
    }

    private fun buildDialog(): AlertDialog.Builder? {
        val builder = AlertDialog.Builder(binding.root.context)
        builder.setTitle("Quit")
        builder.setMessage("Are you sure, do you want to quit?")//Bạn có chắc chắn, bạn có muốn đăng xuất không?
        builder.setPositiveButton("Quit") { dialog, which ->
            finish()
        }
        builder.setNeutralButton("Cancel") { dialog, which -> }
        return builder
    }
}