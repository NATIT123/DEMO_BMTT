package com.example.demo.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import com.example.demo.models.User
import androidx.activity.result.contract.ActivityResultContracts
import com.example.demo.databinding.ActivityEditProfileBinding
import com.example.demo.utils.Constants
import com.example.demo.utils.Constants.Companion.KEY_USER_EMAIL
import com.example.demo.utils.Constants.Companion.KEY_USER_ID
import com.example.demo.utils.Constants.Companion.KEY_USER_IMAGE
import com.example.demo.utils.PreferenceManager
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var encodeImage: String

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                if (result.data != null) {
                    val imageUri = result.data!!.data
                    try {
                        val inputStream = imageUri?.let { contentResolver.openInputStream(it) }
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        binding.img.setImageBitmap(bitmap)
                        encodeImage = encodeImage(bitmap)
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    }
                }
            }
        }

    private fun encodeImage(bitmap: Bitmap): String {
        val previewWidth = 150
        val previewHeight = bitmap.height * previewWidth / bitmap.width
        val previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)
        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)



        preferenceManager = PreferenceManager(applicationContext)
        preferenceManager.instance()
        loadData()

        encodeImage = preferenceManager.getString(
            KEY_USER_IMAGE
        )!!

        binding.btnBack.setOnClickListener {
            finish()
        }

        //Update profile
        binding.btnSave.setOnClickListener {
            isLoading(true)
            val id = preferenceManager.getString(KEY_USER_ID)
            val fullName = binding.edtFullName.text.toString()
            val image = encodeImage
            val email = preferenceManager.getString(KEY_USER_EMAIL)
            updateUser(
                id!!,
                User(fullName = fullName,  image = image,email=email!!)
            )
        }


        //Select Image
        binding.img.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pickImage.launch(intent)
        }
    }

    private fun isLoading(isLoading: Boolean) {
        binding.isLoading = isLoading
    }



    private fun updateUser(id: String, user: User) {

    }

    private fun getUserImage(url: String): Bitmap {
        val bytes = Base64.decode(url, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun loadData() {
        val fullName = preferenceManager.getString(Constants.KEY_USER_FULL_NAME)!!
        val email = preferenceManager.getString(KEY_USER_EMAIL)!!
        val image = preferenceManager.getString(KEY_USER_IMAGE)!!
        val userDetail = User(
            fullName = fullName,
            email = email,
        )
        binding.img.setImageBitmap(getUserImage(image))
        binding.user = userDetail
    }
}