package com.example.doan3.view.acticity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.doan3.R
import com.example.doan3.data.UpUserData
import com.example.doan3.databinding.ActivitySignupBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.util.*
import kotlin.concurrent.timerTask

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var fAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fAuth = FirebaseAuth.getInstance()

        binding.btnSignup.setOnClickListener { Signup() }
        binding.btnLogin.setOnClickListener{Login()}
    }

    private fun Login() {
        val intent = Intent(this,LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun Signup() {
        if (checkValid()) {
            RegisterFirebase(binding.edtEmail.text.toString(), binding.edtPass.text.toString())
        }
    }

    private fun checkValid(): Boolean {
        var emailValidation = ("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
        if (binding.edtEmail.text.toString().isEmpty()) {
            binding.tilEmail.error = "Email not entered"
            return false
        } else {
            binding.tilEmail.error = null
        }
        if (binding.edtEmail.text.toString().matches(emailValidation.toRegex())) {
            binding.tilEmail.error = "Email invalid"
            return false
        } else {
            binding.tilEmail.error = null
        }
        if (binding.edtPass.text.toString().isEmpty()) {
            binding.tilPass.error = "Password not entered"
            return false
        } else {
            binding.tilPass.error = null
        }
        if (binding.edtPass.text.toString().length < 8) {
            binding.tilPass.error = "Password not entered"
            return false
        } else {
            binding.tilPass.error = null
        }
        return true
    }

    private fun RegisterFirebase(email: String, pass: String) {
        fAuth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("FirebaseAuthencation", "createUserWithEmail:success")
                    val user = fAuth.currentUser
                    createDataProfile(user)
                    Timer().schedule(timerTask {
                        val intent = Intent(this@SignupActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }, 3000)
                } else {
                    Log.w("TAG", "createUserWithEmail:failure", task.exception)
                    Snackbar.make(
                        binding.root, "Authentication failed.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun createDataProfile(user: FirebaseUser?) {
        if (user != null) {
            val email = binding.edtEmail.text.toString()
            val uName = email.substring(0, email.indexOf("@"))
            val uId = user.uid
            val uAvatar =
                "https://firebasestorage.googleapis.com/v0/b/blog-93a0b.appspot.com/o/t%E1%BA%A3i%20xu%E1%BB%91ng.png?alt=media&token=55aaf24a-0285-40ee-8587-0f53a310f499"
            val data =
                UpUserData(ServerValue.TIMESTAMP, ServerValue.TIMESTAMP, uId, uName, uAvatar, email)
            val ref = FirebaseDatabase.getInstance().getReference("User")
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.hasChild(user.uid)) {
                        ref.child(user.uid).setValue(data).addOnSuccessListener {
                            Log.d("addUserProfile", "success")
                        }.addOnFailureListener {
                            Log.d("addUserProfile", "failure")
                        }
                    } else Log.e("addUserProfile ", "Already have a profile" + user.uid)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("addUserProfile ", error.message)
                }

            })
        }
    }
}