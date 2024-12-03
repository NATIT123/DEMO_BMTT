package com.example.demo.fragments

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.demo.activities.ChangePasswordActivity
import com.example.demo.activities.EditProfileActivity
import com.example.demo.activities.SignInActivity
import com.example.demo.databinding.FragmentProfileBinding
import com.example.demo.utils.Constants.Companion.KEY_USER_EMAIL
import com.example.demo.utils.Constants.Companion.KEY_USER_FULL_NAME
import com.example.demo.utils.Constants.Companion.KEY_USER_IMAGE
import com.example.demo.utils.PreferenceManager

class ProfileFragment : Fragment() {


    private lateinit var binding: FragmentProfileBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(layoutInflater)
        return binding.root
    }

    //
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        preferenceManager = PreferenceManager(requireContext())
        preferenceManager.instance()


        Log.d("MyApp","dsadsa")

        loadData()


        ///Change Password
        binding.btnChangePassword.setOnClickListener {
            val intent = Intent(activity, ChangePasswordActivity::class.java);
            startActivity(intent)
        }

        //Edit Profile
        binding.btnEdit.setOnClickListener {
            val intent = Intent(activity, EditProfileActivity::class.java);
            startActivity(intent)
        }

        //Sign Out
        binding.btnSignOut.setOnClickListener {
            val dialog = AlertDialog.Builder(requireActivity())
            dialog.apply {
                setTitle("Confirm Logout")
                setMessage("Are you sure you want to logout?")
                setCancelable(false)
                setPositiveButton("Yes") { _, _ ->
                    preferenceManager.clear()
                    requireActivity().finishAffinity()
                    val intent = Intent(requireActivity(), SignInActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
                setNegativeButton("No") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                create()
                show()
            }
        }
    }

    private fun loadData() {
        binding.tvEmail.text = preferenceManager.getString(KEY_USER_EMAIL)
        binding.tvFullName.text = preferenceManager.getString(KEY_USER_FULL_NAME)
        val bytes = Base64.decode(preferenceManager.getString(KEY_USER_IMAGE), Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        Glide.with(requireContext()).load(bitmap)
            .into(binding.avatar)
    }

    override fun onStart() {
        super.onStart()
        loadData()
    }
}