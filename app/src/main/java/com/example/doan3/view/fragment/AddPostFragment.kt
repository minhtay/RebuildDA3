package com.example.doan3.view.fragment

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.doan3.R
import com.example.doan3.databinding.FragmentAddPostBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import gun0912.tedimagepicker.builder.TedImagePicker
import java.util.*
import kotlin.collections.ArrayList

class AddPostFragment : Fragment() {

    private lateinit var binding: FragmentAddPostBinding
    private lateinit var fAuth: FirebaseAuth
    private var filePath: Uri? = null
    private var uriPhoto: String? = null

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

        binding.btnPickimage.setOnClickListener{ pickImage()}
        binding.btnClear.setOnClickListener { clearImage() }


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


}