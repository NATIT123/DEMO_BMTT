package com.example.demo.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.demo.MainActivity
import com.example.demo.R
import com.example.demo.databinding.FragmentVideoPlayerBinding
import com.example.demo.models.Video
import com.example.demo.viewModel.DemoViewModel
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.Util
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


class VideoPlayerFragment : Fragment(), View.OnClickListener {


    private lateinit var binding: FragmentVideoPlayerBinding
    private val args: VideoPlayerFragmentArgs by navArgs()
    private lateinit var demoViewModel: DemoViewModel
    private lateinit var title: TextView
    private var player: ExoPlayer? = null
    private var listVideo = listOf<Video>()
    private var position = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVideoPlayerBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableFullscreen()
        demoViewModel = (activity as MainActivity).viewModel
        getListVideo()
        val video = args.video
        position = args.position
        playVideo(video)


        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (player?.isPlaying == true) {
                        player?.stop()
                    }
                }

            })

        //Handle Prev Button
        requireActivity().findViewById<ImageButton>(R.id.exo_prev_).setOnClickListener(this)
        //Handle Next Button
        requireActivity().findViewById<ImageButton>(R.id.exo_next_).setOnClickListener(this)


        //Handle Back Button
        requireActivity().findViewById<ImageButton>(R.id.btnReturn).setOnClickListener {
            findNavController().popBackStack()
        }

    }

    private fun getListVideo() {
        demoViewModel.observerListVideo().observe(requireActivity()) { data ->
            listVideo = data
        }
    }

    override fun onResume() {
        super.onResume()
        player?.playWhenReady = true
        player?.playbackState
    }


    override fun onPause() {
        super.onPause()
        player?.playWhenReady = false
        player?.playbackState
    }

    @SuppressLint("SetTextI18n")
    private fun playVideo(video: Video) {
//        val id = video.originalPath.substringAfterLast("/")
//        val videoUri = getMediaStoreUri(id)
        val decryptedFilePath = decryptVideo(video)
        decryptedFilePath?.let {
            player = SimpleExoPlayer.Builder(requireContext()).build()
            player?.let { exoPlayer ->
                requireActivity().findViewById<TextView>(R.id.tvTitle).text =
                    "${video.fileName}.mp4"
                binding.exoplayerView.player = exoPlayer
                binding.exoplayerView.keepScreenOn = true
                exoPlayer.setMediaItem(MediaItem.fromUri(Uri.fromFile(File(decryptedFilePath))))
                exoPlayer.prepare()
                playError(exoPlayer)
            }
        }
    }

    private fun createFolder(folderName: String) {
        val externalFilesDir = context?.getExternalFilesDir(null)

        val sharedVideoDir = File(externalFilesDir, folderName)

        if (!sharedVideoDir.exists()) {
            val created = sharedVideoDir.mkdirs()
            if (created) {
                android.util.Log.d("FileProvider", "Folder $folderName is created.")
            } else {
                android.util.Log.e("FileProvider", "Can not create $folderName.")
            }
        }
    }

    private fun decryptVideo(video: Video): String? {
        val encryptedFilePath =
            video.encryptedFilePath
        createFolder("decryptedVideo")
        val decryptedFilePath =
            requireContext().getExternalFilesDir(null)?.absolutePath + "/decryptedVideo/${video.fileName}.mp4"
        val success = decryptVideo(
            requireContext(),
            encryptedFilePath,
            decryptedFilePath,
            video.iv,
            video.secretKey
        )
        if (success) {
            return decryptedFilePath
        } else {
            return null
        }
    }

    private fun decryptVideo(
        context: Context,
        encryptedFilePath: String,
        outputFilePath: String,
        iv: String,
        key: String
    ): Boolean {
        return try {
            val sharedPrefs = context.getSharedPreferences("VideoKeys", Context.MODE_PRIVATE)

            val keyBytes = Base64.decode(key, Base64.DEFAULT)
            val ivBytes = Base64.decode(iv, Base64.DEFAULT)
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

    private fun playError(player: ExoPlayer) {
        player.addListener(object : Player.EventListener {
            override fun onPlayerError(error: ExoPlaybackException) {
                Toast.makeText(requireContext(), "Video Playing Error", Toast.LENGTH_SHORT).show()
            }
        })
        player.playWhenReady = true
    }

    private fun getMediaStoreUri(id: String): Uri {
        return Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player?.release()
        player = null
    }

    private fun enableFullscreen() {
        requireActivity().window.apply {
            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                insetsController?.hide(WindowInsets.Type.statusBars())
            } else {
                setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                )
            }
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.exo_next_ -> {
                try {
                    position += 1
                    player?.stop()
                    playVideo(listVideo[position])
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "No Next Video", Toast.LENGTH_SHORT).show()
                    onBackPressed()
                }
            }

            R.id.exo_prev_ -> {
                try {
                    player?.stop()
                    position -= 1
                    playVideo(listVideo[position - 1])
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "No Previous Video", Toast.LENGTH_SHORT).show()
                    onBackPressed()
                }
            }
        }
    }

    private fun onBackPressed() {
        findNavController().popBackStack()
    }

}