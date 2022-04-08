package com.example.doan3.view.acticity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.example.doan3.R
import com.google.firebase.auth.FirebaseAuth
import java.util.*
import kotlin.concurrent.timerTask

class SplashActivity : AppCompatActivity() {
    private lateinit var fAuth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // khai báo biến fAuth cho firebase authencaiton
        fAuth = FirebaseAuth.getInstance()

       if (isConnected()){
           checkCurrentUser()
       }else{
           Timer().schedule(timerTask { // hàm delay time show alertDialog
              alertDialog().show()
           },3000 )// chuyển màn hình sau 3s(đơn vị tính trên code ms)
       }

    }

    //check kết nối internet
    @SuppressLint("ServiceCast")
    private fun isConnected(): Boolean {
        val connectivityManager =
            this.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnectedOrConnecting) {
            val wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            val mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            return mobile != null && mobile.isConnectedOrConnecting || wifi != null && wifi.isConnectedOrConnecting
        }
        return false
    }

    // check user đã đăng nhập
    private fun checkCurrentUser(){
        if (fAuth.currentUser!=null){
            // có user đăng nhập trên thiết bị, trả về màn hình main acticity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }else{
            // chưa có user đăng nhập trên thiết bị, trả về màn hình login acticity
            val intent = Intent(this, LoginActivity::class.java)
            Timer().schedule(timerTask { // hàm delay time chuyển màn hình
                startActivity(intent)
                finish()
            },3000 )// chuyển màn hình sau 3s(đơn vị tính trên code ms)
        }
    }
    private fun alertDialog():AlertDialog.Builder{
        val builder = AlertDialog.Builder(this)
        builder.setTitle("No internet connection")
        builder.setMessage("Internet connection is slow or unavailable. Please check your internet settings")
        builder.setPositiveButton("Ok") { _, _ ->
            startActivity(Intent(android.provider.Settings.Panel.ACTION_INTERNET_CONNECTIVITY));
        }
        return builder
    }
}