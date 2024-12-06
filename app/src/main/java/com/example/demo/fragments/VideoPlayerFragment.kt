package com.example.demo.fragments

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import android.widget.TextView
import android.widget.Toast
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


class VideoPlayerFragment : Fragment() {

    private lateinit var binding: FragmentVideoPlayerBinding
    private val args: VideoPlayerFragmentArgs by navArgs()
    private lateinit var demoViewModel: DemoViewModel
    private lateinit var title: TextView
    private var player: ExoPlayer? = null
    private lateinit var concatenatingMediaSource: ConcatenatingMediaSource

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVideoPlayerBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        demoViewModel = (activity as MainActivity).viewModel
        title = requireActivity().findViewById(R.id.tvTitle)
        val video = args.video
        val position = args.position
        playVideo(video, position)

    }

    private fun playVideo(video: Video, position: Int) {
        Log.d("MyApp", position.toString())
        val id = video.originalPath.substringAfterLast("/")
        val videoUri = getMediaStoreUri(id)
        player = SimpleExoPlayer.Builder(requireContext()).build().also { exoPlayer ->
            binding.exoplayerView.player = exoPlayer
            binding.exoplayerView.keepScreenOn = true
            exoPlayer.setMediaItem(MediaItem.fromUri(videoUri))
            exoPlayer.prepare()
            exoPlayer.seekTo(position, C.TIME_UNSET)
            playError(exoPlayer)
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

}