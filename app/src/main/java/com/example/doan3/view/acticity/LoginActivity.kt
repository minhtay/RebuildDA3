package com.example.doan3.view.acticity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.doan3.databinding.ActivityLoginBinding
import com.example.doan3.util.Utils
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var fAuth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // khai báo biến fAuth
        fAuth = FirebaseAuth.getInstance()

        // sét sự kiện click cho btn
        binding.btnLogin.setOnClickListener { loginToAccount() }
        binding.btnFacebook.setOnClickListener { loginToFaceBook() }
        binding.btnGoogle.setOnClickListener { loginToGoogle() }

        // tắt bàn phím ảo bằng click vào màn hình
        binding.constraint.setOnClickListener (object :View.OnClickListener {
            override fun onClick(p0: View?) {
                Utils.hideSoftKeyboard(this@LoginActivity,binding.root)
            }

        })




    }

    private fun loginToAccount() {
        if (checkValid()){

        }

    }

    private fun loginToFaceBook(){

    }

    private fun loginToGoogle(){

    }
    // check hợp lệ của email và pass word
    private fun checkValid():Boolean{
        var emailValidation = ("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
        if (binding.edtEmail.text.toString().isEmpty()){
            binding.tilEmail.error = "Email not entered"
            return false
        }else{
            binding.tilEmail.error = null
        }
        if (binding.edtEmail.text.toString().matches(emailValidation.toRegex())){
            binding.tilEmail.error = "Email invalid"
            return false
        }else{
            binding.tilEmail.error = null
        }
        if (binding.edtPass.text.toString().isEmpty()){
            binding.tilPass.error = "Password not entered"
            return false
        }else{
            binding.tilPass.error = null
        }
        if (binding.edtPass.text.toString().length<8){
            binding.tilPass.error = "Password not entered"
            return false
        }else{
            binding.tilPass.error = null
        }
        return true
    }
}