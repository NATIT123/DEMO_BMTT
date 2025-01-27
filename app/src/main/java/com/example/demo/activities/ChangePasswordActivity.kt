package com.example.demo.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.demo.R
import com.example.demo.database.DemoDatabase
import com.example.demo.databinding.ActivityChangePasswordBinding
import com.example.demo.utils.Constants
import com.example.demo.utils.PreferenceManager
import com.example.demo.viewModel.DemoViewModel
import com.example.demo.viewModel.DemoViewModelFactory
import com.toxicbakery.bcrypt.Bcrypt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangePasswordBinding
    private lateinit var preferenceManager: PreferenceManager
    private var showPassword: Boolean = false
    private var showPasswordConfirm: Boolean = false
    private var showNewPassword: Boolean = false

    private val userViewModel: DemoViewModel by lazy {
        val demoDatabase = DemoDatabase.getInstance(this)
        val demoViewModelFactory = DemoViewModelFactory(demoDatabase)
        ViewModelProvider(this, demoViewModelFactory)[DemoViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(applicationContext)
        preferenceManager.instance()

        //Handle Back Button
        binding.btnBack.setOnClickListener {
            finish()
        }

        toggleIconPassword(binding.btnToggleOldPassword, binding.edtOldPassword)
        toggleIconPasswordConfirm(
            binding.btnToggleConfirmNewPassword,
            binding.edtConfirmNewPassword
        )
        toggleIconNewPassword(binding.btnToggleNewPassword, binding.edtNewPassword)

        //Handle Change Password
        binding.btnChangePassword.setOnClickListener {
            isValidChangePassword()
        }

    }

    private fun toggleIconPassword(buttonIcon: ImageButton, buttonPassword: EditText) {
        buttonIcon.setOnClickListener {
            if (showPassword) {
                showPassword = false
                buttonPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                buttonIcon.setImageResource(R.drawable.baseline_visibility_off_24)
            } else {
                showPassword = true
                buttonPassword.inputType =
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                buttonIcon.setImageResource(R.drawable.baseline_visibility_24)
            }

            buttonPassword.setSelection(buttonPassword.text.length)
        }
    }

    private fun toggleIconPasswordConfirm(buttonIcon: ImageButton, buttonPassword: EditText) {
        buttonIcon.setOnClickListener {
            if (showPasswordConfirm) {
                showPasswordConfirm = false
                buttonPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                buttonIcon.setImageResource(R.drawable.baseline_visibility_off_24)
            } else {
                showPasswordConfirm = true
                buttonPassword.inputType =
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                buttonIcon.setImageResource(R.drawable.baseline_visibility_24)
            }

            buttonPassword.setSelection(buttonPassword.text.length)
        }
    }

    private fun toggleIconNewPassword(buttonIcon: ImageButton, buttonPassword: EditText) {
        buttonIcon.setOnClickListener {
            if (showNewPassword) {
                showNewPassword = false
                buttonPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                buttonIcon.setImageResource(R.drawable.baseline_visibility_off_24)
            } else {
                showNewPassword = true
                buttonPassword.inputType =
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                buttonIcon.setImageResource(R.drawable.baseline_visibility_24)
            }

            buttonPassword.setSelection(buttonPassword.text.length)
        }
    }

    private fun isLoading(isLoading: Boolean) {
        binding.isLoading = isLoading
    }

    private fun showToast(message: String) {
        Toast.makeText(this@ChangePasswordActivity, message, Toast.LENGTH_SHORT).show()
    }

    private fun isValidChangePassword() {
        if (binding.edtOldPassword.text.isEmpty()) {
            showToast("Please enter old password")
            return
        } else if (!Bcrypt.verify(
                binding.edtOldPassword.text.toString(),
                getPasswordHash()
            )
        ) {
            showToast("Old Password is not correct")
            return
        } else if (binding.edtNewPassword.text.toString().isEmpty()) {
            showToast("Please enter new password")
            return
        } else if (binding.edtConfirmNewPassword.text.toString().isEmpty()) {
            showToast("Please enter confirm new password")
            return
        } else if (binding.edtNewPassword.text.toString() != binding.edtConfirmNewPassword.text.toString()
        ) {
            showToast("New password and confirm password must be the same")
            return
        } else {
            isLoading(true)
            changePassword()
        }
    }

    private fun changePassword() {
        val newPassword = binding.edtNewPassword.text.toString()
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            userViewModel.changePassword(newPassword)
            val passwordHashed = Bcrypt.hash(
                newPassword,
                Constants.SALT_ROUNDS
            )
            runBlocking {
                val file = File(filesDir, "my_file.bin")
                withContext(Dispatchers.IO) {
                    FileOutputStream(file).use { output ->
                        output.write(passwordHashed)
                    }
                }
            }
            showToast("Please Sign in again.")
            finishAffinity()
            preferenceManager.clear()
            val intent =
                Intent(
                    this@ChangePasswordActivity,
                    SignInActivity::class.java
                )
            startActivity(intent)
        }, 3000)

    }

    private fun getPasswordHash(): ByteArray {
        val file = File(filesDir, "my_file.bin")
        lateinit var byteArray: ByteArray
        try {
            if (file.exists()) {
                val inputStream = FileInputStream(file)
                byteArray = inputStream.readBytes()
                inputStream.close()
            } else {
                println("File không tồn tại")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return byteArray

    }

}