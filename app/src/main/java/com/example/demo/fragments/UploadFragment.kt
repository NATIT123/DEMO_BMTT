package com.example.demo.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.demo.R
import com.example.demo.databinding.FragmentUploadBinding
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


class UploadFragment : Fragment() {

    private lateinit var binding: FragmentUploadBinding
    private var selectVideoUri: Uri? = null

    private val pickVideo =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                if (result.data != null) {
                    selectVideoUri = result.data?.data!!

                    Log.d("MyApp", selectVideoUri!!.path.toString())

                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUploadBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnPickVideo.setOnClickListener {
            checkPermissionVideoPicker()
        }

        binding.btnUpload.setOnClickListener {
            selectVideoUri?.let { uri ->
                val encryptedFilePath =
                    requireContext().getExternalFilesDir(null)?.absolutePath + "/encrypted_video.mp4"
                val success = encryptVideo(requireContext(), uri, encryptedFilePath)
                if (success) {
                    Log.d("MyApp", "Video encrypted and saved at $encryptedFilePath")
                } else {
                    Log.e("MyApp", "Encryption failed")
                }
            }
        }

        binding.btnDecrypt.setOnClickListener {
            val encryptedFilePath =
                requireContext().getExternalFilesDir(null)?.absolutePath + "/encrypted_video.mp4"
            val decryptedFilePath =
                requireContext().getExternalFilesDir(null)?.absolutePath + "/decrypted_video.mp4"
            val success = decryptVideo(requireContext(), encryptedFilePath, decryptedFilePath)
            if (success) {
                Log.d("MyApp", "Video decrypted and saved at $decryptedFilePath")
            } else {
                Log.e("MyApp", "Decryption failed")
            }
        }

        binding.btnShareVideo.setOnClickListener {
            selectVideoUri?.let {
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.type = "video/*"
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(selectVideoUri!!.path))
                ContextCompat.startActivity(
                    requireContext(),
                    Intent.createChooser(shareIntent, "Sharing video File!!"),
                    null
                )
            }

        }
    }

    private fun checkPermissionVideoPicker() {
        var readExternalVideo: String = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            readExternalVideo = android.Manifest.permission.READ_MEDIA_VIDEO
        } else {
            readExternalVideo = android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                readExternalVideo
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openVideoPicker()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(readExternalVideo), 100)
        }
    }

    @SuppressLint("IntentReset")
    private fun openVideoPicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        intent.type = "video/*"
        pickVideo.launch(intent)
    }

    // Mã hóa video
    private fun encryptVideo(context: Context, videoUri: Uri, outputFilePath: String): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(videoUri) ?: return false
            val encryptedFile = File(outputFilePath)

            val keyGenerator = KeyGenerator.getInstance("AES")
            keyGenerator.init(256)
            val secretKey = keyGenerator.generateKey()

            val iv = ByteArray(16).apply { SecureRandom().nextBytes(this) }
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding").apply {
                init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
            }

            FileOutputStream(encryptedFile).use { fileOut ->
                fileOut.write(iv)
                CipherOutputStream(fileOut, cipher).use { cipherOut ->
                    inputStream.copyTo(cipherOut)
                }
            }

            inputStream.close()
            saveKeyAndIv(context, secretKey, iv, encryptedFile.name)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Hàm lưu khóa AES và IV
    private fun saveKeyAndIv(context: Context, key: SecretKey, iv: ByteArray, fileName: String) {
        val sharedPrefs = context.getSharedPreferences("VideoKeys", Context.MODE_PRIVATE)
        sharedPrefs.edit().apply {
            putString("${fileName}_key", Base64.encodeToString(key.encoded, Base64.DEFAULT))
            putString("${fileName}_iv", Base64.encodeToString(iv, Base64.DEFAULT))
            apply()
        }

    }

    private fun decryptVideo(
        context: Context,
        encryptedFilePath: String,
        outputFilePath: String
    ): Boolean {
        return try {
            val sharedPrefs = context.getSharedPreferences("VideoKeys", Context.MODE_PRIVATE)
            val keyBase64 =
                sharedPrefs.getString("${File(encryptedFilePath).name}_key", null) ?: return false
            val ivBase64 =
                sharedPrefs.getString("${File(encryptedFilePath).name}_iv", null) ?: return false

            val keyBytes = Base64.decode(keyBase64, Base64.DEFAULT)
            val ivBytes = Base64.decode(ivBase64, Base64.DEFAULT)
            val secretKey = SecretKeySpec(keyBytes, "AES")

            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding").apply {
                init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(ivBytes))
            }

            val decryptedFile = File(outputFilePath)

            FileInputStream(File(encryptedFilePath)).use { fileIn ->
                FileOutputStream(decryptedFile).use { fileOut ->
                    fileIn.skip(16)
                    CipherInputStream(fileIn, cipher).use { cipherIn ->
                        cipherIn.copyTo(fileOut)
                    }
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


}