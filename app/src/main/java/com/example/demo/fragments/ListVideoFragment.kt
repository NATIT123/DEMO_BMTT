package com.example.demo.fragments

import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.demo.MainActivity
import com.example.demo.adapters.VideoFileAdapter
import com.example.demo.databinding.FragmentListVideoBinding
import com.example.demo.models.Video
import com.example.demo.viewModel.DemoViewModel


class ListVideoFragment : Fragment(), VideoFileAdapter.OnClickListener {

    private lateinit var binding: FragmentListVideoBinding
    private lateinit var mVideoFileAdapter: VideoFileAdapter
    private var listVideo = listOf<Video>()
    private lateinit var demoViewModel: DemoViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentListVideoBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        demoViewModel = (activity as MainActivity).viewModel

        demoViewModel.observerListVideo().observe(requireActivity()) { data ->
            binding.isLoading = true
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                if (data.isNotEmpty()) {
                    listVideo = data
                    mVideoFileAdapter = VideoFileAdapter(data, requireActivity(), this)
                    binding.rcvVideo.apply {
                        adapter = mVideoFileAdapter
                        layoutManager =
                            LinearLayoutManager(
                                requireContext(),
                                LinearLayoutManager.VERTICAL,
                                false
                            )
                        addItemDecoration(
                            DividerItemDecoration(
                                requireContext(),
                                DividerItemDecoration.VERTICAL
                            )
                        )
                    }
                } else {
                    binding.isEmpty = true
                }
                binding.isLoading = false
            }, 3000)

        }
    }

    override fun playVideo(position: Int) {
        Toast.makeText(requireContext(), listVideo[position].fileName, Toast.LENGTH_SHORT)
            .show()

    }
}