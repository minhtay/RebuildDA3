package com.example.doan3

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.doan3.databinding.FragmentAddPostBinding

class AddPostFragment : Fragment() {

    private lateinit var binding: FragmentAddPostBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddPostBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btn.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                findNavController().navigate(R.id.action_addPostFragment_to_homeFragment)
            }

        })
    }
}