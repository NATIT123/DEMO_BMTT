package com.example.demo.activities

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.widget.Toast
import com.example.demo.utils.PreferenceManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.demo.MainActivity
import com.example.demo.R
import com.example.demo.databinding.ActivitySignInBinding
import com.example.demo.models.User
import com.example.demo.utils.Constants.Companion.KEY_USER_EMAIL
import com.example.demo.utils.Constants.Companion.KEY_USER_FULL_NAME
import com.example.demo.utils.Constants.Companion.KEY_USER_IMAGE

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private var showPassword: Boolean = false
    private lateinit var preferenceManager: PreferenceManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(applicationContext)
        preferenceManager.instance()

        preferenceManager = PreferenceManager(applicationContext)
        preferenceManager.instance()

        preferenceManager.getString(KEY_USER_EMAIL)?.let {
            val intent = Intent(this@SignInActivity, MainActivity::class.java);
            startActivity(intent);
            finishAffinity();
        };

        //Toggle IconPassword
        toggleIconPassword()

        binding.btnSignUp.setOnClickListener {
            val intent = Intent(this@SignInActivity, SignUpActivity::class.java)
            startActivity(intent)
        }

        //Handle Sign In
        binding.btnSignIn.setOnClickListener {
            isValidSignIn()
        }
    }

    private fun toggleIconPassword() {
        binding.btnTogglePassword.setOnClickListener {
            if (showPassword) {
                showPassword = false
                binding.edtPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.btnTogglePassword.setImageResource(R.drawable.baseline_visibility_off_24)
            } else {
                showPassword = true
                R.drawable.baseline_visibility_off_24
                binding.edtPassword.inputType =
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.btnTogglePassword.setImageResource(R.drawable.baseline_visibility_24)
            }

            binding.edtPassword.setSelection(binding.edtPassword.text.length)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this@SignInActivity, message, Toast.LENGTH_SHORT).show()
    }

    private fun isValidSignIn() {
        if (binding.edtEmail.text.toString().isEmpty()) {
            showToast("Please enter Email")
            return
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.edtEmail.text.toString()).matches()) {
            showToast("Email not valid")
            return
        } else if (binding.edtPassword.text.toString().isEmpty()) {
            showToast("Please enter Password")
            return
        } else {
            isLoading(true)
            signIn()
        }
    }

    private fun isLoading(isLoading: Boolean) {
        binding.isLoading = isLoading
    }


    private fun signIn() {
        val email = binding.edtEmail.text.toString().trim()
        val password = binding.edtPassword.text.toString().trim()
        val user = User(
            email = email,
            password = password,
            image = "",
            fullName = "",
        )
        handleLoginUser(user)

    }

    private fun handleLoginUser(user: User) {
        preferenceManager.putString(
            KEY_USER_EMAIL,
            user.email
        )
        preferenceManager.putString(
            KEY_USER_FULL_NAME,
            user.email
        )
        preferenceManager.putString(KEY_USER_IMAGE, user.image)
        val intent = Intent(this@SignInActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        showToast("Login Successfully")
    }

}