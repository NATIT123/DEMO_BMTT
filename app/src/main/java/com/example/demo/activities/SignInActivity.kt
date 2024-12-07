package com.example.demo.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.example.demo.utils.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.demo.MainActivity
import com.example.demo.R
import com.example.demo.database.DemoDatabase
import com.example.demo.databinding.ActivitySignInBinding
import com.example.demo.models.User
import com.example.demo.utils.Constants.Companion.KEY_USER_EMAIL
import com.example.demo.utils.Constants.Companion.KEY_USER_FULL_NAME
import com.example.demo.utils.Constants.Companion.KEY_USER_ID
import com.example.demo.utils.Constants.Companion.KEY_USER_IMAGE
import com.example.demo.viewModel.DemoViewModel
import com.example.demo.viewModel.DemoViewModelFactory

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private var showPassword: Boolean = false
    private lateinit var preferenceManager: PreferenceManager


    private val userViewModel: DemoViewModel by lazy {
        val demoDatabase = DemoDatabase.getInstance(this)
        val demoViewModelFactory = DemoViewModelFactory(demoDatabase)
        ViewModelProvider(this, demoViewModelFactory)[DemoViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)


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
        userViewModel.observerUser(user.email, user.password).observe(this) { currentUser ->
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                isLoading(false)
                currentUser?.let {
                    preferenceManager.putLong(KEY_USER_ID, currentUser.id!!)



                    preferenceManager.putString(
                        KEY_USER_EMAIL,
                        currentUser.email
                    )
                    preferenceManager.putString(
                        KEY_USER_FULL_NAME,
                        currentUser.fullName
                    )
                    preferenceManager.putString(KEY_USER_IMAGE, currentUser.image)

                    val intent = Intent(this@SignInActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    showToast("Login Successfully")
                } ?: kotlin.run {
                    showToast("Email or Password is not correct")
                }
            }, 3000)

        }

    }

}