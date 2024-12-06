package com.example.demo.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.demo.MainActivity
import com.example.demo.R
import com.example.demo.activities.SignInActivity
import com.example.demo.adapters.VideoFileAdapter
import com.example.demo.databinding.FragmentListVideoBinding
import com.example.demo.models.Video
import com.example.demo.viewModel.DemoViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.File


class ListVideoFragment : Fragment(), VideoFileAdapter.OnClickListener {

    private lateinit var binding: FragmentListVideoBinding
    private lateinit var mVideoFileAdapter: VideoFileAdapter
    private var listVideo = listOf<Video>()
    private lateinit var demoViewModel: DemoViewModel
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var dialogRename: AlertDialog.Builder

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

        demoViewModel.observerListVideo().observe(viewLifecycleOwner) { data ->
            binding.isLoading = true
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                if (data.isNotEmpty()) {
                    binding.isEmpty = false
                    listVideo = data
                    mVideoFileAdapter = VideoFileAdapter(data, requireContext(), this)
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
        val video = listVideo[position]
        val bundle = Bundle().apply {
            putSerializable("video", video)
            putInt("position", position)
        }
        findNavController().navigate(R.id.action_listVideoFragment_to_videoPlayerFragment, bundle)

    }

    @SuppressLint("PrivateResource", "NotifyDataSetChanged")
    override fun moreVideo(position: Int) {
        bottomSheetDialog = BottomSheetDialog(
            requireContext(),
            com.google.android.material.R.style.Base_ThemeOverlay_Material3_BottomSheetDialog
        )
        val bsView = LayoutInflater.from(requireContext())
            .inflate(R.layout.video_bs_layout, activity?.findViewById(R.id.bottom_sheet))
        bottomSheetDialog.setContentView(bsView)
        bottomSheetDialog.show()

        bsView.findViewById<LinearLayout>(R.id.playVideo).setOnClickListener {
            playVideo(position)
            bottomSheetDialog.dismiss()
        }
        bsView.findViewById<LinearLayout>(R.id.shareVideo).setOnClickListener {
            shareVideo(listVideo[position].encryptedFilePath)
            bottomSheetDialog.dismiss()
        }
        bsView.findViewById<LinearLayout>(R.id.deleteVideo).setOnClickListener {
            val dialog = androidx.appcompat.app.AlertDialog.Builder(requireActivity())
            dialog.apply {
                setTitle("Confirm Delete")
                setMessage("Are you sure you want to delete video ${listVideo[position].fileName}?")
                setCancelable(false)
                setPositiveButton("Yes") { _, _ ->
                    val success = deleteVideo(listVideo[position].encryptedFilePath)
                    if (success) {
                        demoViewModel.deleteVideo(listVideo[position].id!!)
                        val updatedList = listVideo.toMutableList()
                        updatedList.removeAt(position)
                        listVideo = updatedList

                        //Adapter
                        mVideoFileAdapter.notifyItemRemoved(position)
                        mVideoFileAdapter.notifyDataSetChanged()
                        bottomSheetDialog.dismiss()
                        Toast.makeText(requireContext(), "Delete Video Success", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        bottomSheetDialog.dismiss()
                        Toast.makeText(requireContext(), "Delete Video Failed", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                setNegativeButton("No") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                create()
                show()
            }
        }
        bsView.findViewById<LinearLayout>(R.id.renameVideo).setOnClickListener {
            dialogRename = AlertDialog.Builder(requireContext())
            dialogRename.setTitle("Rename Video")
            val dialogView = layoutInflater.inflate(R.layout.dialog_rename, null)
            dialogRename.setView(dialogView)

            dialogRename.setCancelable(false)

            val newName = dialogView.findViewById<EditText>(R.id.edtName);
            newName.setText(listVideo[position].fileName)
            val alertDialog = dialogRename.create()
            alertDialog.show()

            dialogView.findViewById<AppCompatButton>(R.id.btnCancel).setOnClickListener {
                alertDialog.dismiss()
            }
            dialogView.findViewById<AppCompatButton>(R.id.btnOK).setOnClickListener {
                if (newName.text.isNotEmpty()) {
                    val uri = getUriFromFilePath(listVideo[position].encryptedFilePath)
                    val success = renameFileFromUri(uri, "${newName.text}.mp4")
                    if (success) {
                        val fileParent = File(uri.path!!).parent
                        val newPath = "${fileParent}/${newName.text}.mp4"
                        demoViewModel.updateVideo(
                            listVideo[position].id!!,
                            newName.text.toString(),
                            newPath
                        )
                        mVideoFileAdapter.notifyDataSetChanged()
                        mVideoFileAdapter.notifyItemChanged(position)
                        Toast.makeText(requireContext(), "Update name success", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(requireContext(), "Update name failed", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Please fill name", Toast.LENGTH_SHORT).show()
                }
                alertDialog.dismiss()

            }

            bottomSheetDialog.dismiss()
        }


    }

    private fun deleteVideo(filePath: String): Boolean {
        val file = File(filePath)
        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }

    private fun renameFileFromUri(fileUri: Uri, newFileName: String): Boolean {
        val file = File(fileUri.path!!)
        val newFile = File(file.parent, newFileName)
        return file.renameTo(newFile)
    }

    private fun getUriFromFilePath(filePath: String): Uri {
        val file = File(filePath)
        return Uri.fromFile(file)
    }

    private fun shareVideo(pathVideo: String) {
        val videoFile = File(pathVideo)
        if (videoFile.exists()) {
            val videoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                videoFile
            )
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "video/*"
            shareIntent.putExtra(
                Intent.EXTRA_STREAM,
                videoUri
            )
            startActivity(Intent.createChooser(shareIntent, "Sharing video File!!"))
        } else {
            Toast.makeText(requireContext(), "File is not exist", Toast.LENGTH_SHORT).show()
        }

    }


}