package com.example.demo.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.demo.R
import com.example.demo.utils.Constants.Companion.IS_STARTED
import com.example.demo.utils.PreferenceManager

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var preferenceManager: PreferenceManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        preferenceManager = PreferenceManager(applicationContext)
        preferenceManager.instance()

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            goToSignInActivity()
        }, 3000)
    }

    private fun goToSignInActivity() {
        val intent = Intent(this@SplashScreenActivity, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}