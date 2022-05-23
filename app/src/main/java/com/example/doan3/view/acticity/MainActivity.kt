package com.example.doan3.view.acticity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.doan3.R
import com.example.doan3.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        val nav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        val navController = findNavController(R.id.fragmentContainerView)
        nav.setupWithNavController(navController)

        val popupMenu = PopupMenu(binding.root.context,binding.btnMenuHome)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId){
                R.id.profile->{
                    val intent = Intent(this,ProfileActivity::class.java)
                    intent.putExtra("idUser", mAuth.currentUser!!.uid)
                    startActivity(intent)
                }
                R.id.logout->{
                   buildDialog()!!.show()
                }
            }
            false
        }
        popupMenu.inflate(R.menu.menu_appbar)
        popupMenu.gravity = Gravity.RIGHT
        popupMenu.setForceShowIcon(true)


        binding.btnMenuHome.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                popupMenu.show()
            }

        })
        binding.btnSearch.setOnClickListener{searchActivity()}


    }

    private fun searchActivity() {
        val intent = Intent(this,SearchActivity::class.java)
        startActivity(intent)
    }

    private fun buildDialog(): AlertDialog.Builder? {
        val builder = AlertDialog.Builder(binding.root.context)
        builder.setTitle("Logout")
        builder.setMessage("Are you sure, do you want to logout?")//Bạn có chắc chắn, bạn có muốn đăng xuất không?
        builder.setPositiveButton("Logout") { dialog, which ->
            mAuth.signOut()
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        builder.setNeutralButton("Cancel") { dialog, which -> }
        return builder
    }

}