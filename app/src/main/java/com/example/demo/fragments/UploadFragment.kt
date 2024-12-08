package com.example.demo.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
import androidx.core.content.FileProvider
import com.example.demo.MainActivity
import com.example.demo.R
import com.example.demo.databinding.FragmentUploadBinding
import com.example.demo.models.Video
import com.example.demo.utils.Constants.Companion.KEY_USER_ID
import com.example.demo.utils.PreferenceManager
import com.example.demo.viewModel.DemoViewModel
import java.io.ByteArrayOutputStream
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
import kotlin.math.log10
import kotlin.math.pow


class UploadFragment : Fragment() {

    private lateinit var binding: FragmentUploadBinding
    private var selectVideoUri: Uri? = null
    private var thumbnail: Bitmap? = null

    private lateinit var demoViewModel: DemoViewModel
    private lateinit var preferenceManager: PreferenceManager

    private val pickVideo =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                if (result.data != null) {
                    selectVideoUri = result.data?.data!!
                    binding.tvVideoLink.text = selectVideoUri!!.path

                    thumbnail = getVideoThumbnail(requireContext(), selectVideoUri!!)
                    if (thumbnail != null) {
                        binding.imgVideo.visibility = View.VISIBLE
                        binding.imgVideo.setImageBitmap(thumbnail)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Cannot generate thumbnail",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
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

        preferenceManager = PreferenceManager(requireContext())
        preferenceManager.instance()

        demoViewModel = (activity as MainActivity).viewModel

        binding.btnPickVideo.setOnClickListener {
            checkPermissionVideoPicker()
        }

        binding.btnUpload.setOnClickListener {
            val name = binding.edtVideoName.text.toString()
            if (name.isNotEmpty()) {
                binding.isLoading = true
                uploadVideo(name)
            } else {
                Toast.makeText(requireContext(), "Please fill the name video", Toast.LENGTH_SHORT)
                    .show()
            }
        }


    }

    private fun getVideoThumbnail(context: Context, videoUri: Uri): Bitmap? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, videoUri)
            val bitmap = retriever.frameAtTime
            retriever.release()
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun uploadVideo(videoName: String) {
        selectVideoUri?.let { uri ->
            createFolder("encryptedVideo")
            val encryptedFilePath =
                requireContext().getExternalFilesDir(null)?.absolutePath + "/encryptedVideo/$videoName.mp4"
            val success =
                encryptVideo(videoName, encryptedFilePath, requireContext(), uri, encryptedFilePath)
            if (success) {
                binding.edtVideoName.setText("")
                binding.tvVideoLink.text = ""
                binding.imgVideo.setImageBitmap(null)
                Toast.makeText(requireContext(), "Upload Video Success", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Upload Video Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun getFileSize(context: Context, uri: Uri): Long {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
                if (cursor.moveToFirst()) {
                    cursor.getLong(sizeIndex)
                } else {
                    0L
                }
            } ?: 0L
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }

    private fun getVideoDuration(context: Context, uri: Uri): Long {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val duration =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
                    ?: 0L
            retriever.release()
            duration
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }


    private fun createFolder(folderName: String) {
        val externalFilesDir = context?.getExternalFilesDir(null)

        val sharedVideoDir = File(externalFilesDir, folderName)

        if (!sharedVideoDir.exists()) {
            val created = sharedVideoDir.mkdirs()
            if (created) {
                Log.d("FileProvider", "Folder $folderName is created.")
            } else {
                Log.e("FileProvider", "Can not create $folderName.")
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
    private fun encryptVideo(
        videoName: String,
        encryptedFilePath: String,
        context: Context,
        videoUri: Uri,
        outputFilePath: String
    ): Boolean {
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
            val currentVideo = Video(
                fileName = videoName,
                encryptedFilePath = encryptedFilePath,
                originalPath = videoUri.path.toString(),
                originalSize = getFileSize(requireContext(), videoUri),
                duration = getVideoDuration(requireContext(), videoUri),
                secretKey = Base64.encodeToString(secretKey.encoded, Base64.DEFAULT),
                iv = Base64.encodeToString(iv, Base64.DEFAULT),
                userId = preferenceManager.getLong(KEY_USER_ID)
            )
            demoViewModel.addVideo(currentVideo)
            binding.isLoading = false
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


}