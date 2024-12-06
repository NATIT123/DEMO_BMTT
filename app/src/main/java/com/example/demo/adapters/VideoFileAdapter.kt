package com.example.demo.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.demo.R
import com.example.demo.databinding.VideoItemBinding
import com.example.demo.models.Video
import java.io.File
import kotlin.math.log10
import kotlin.math.pow

class VideoFileAdapter(
    private val listVideo: List<Video>,
    private val context: Context,
    private val mOnClickListener: OnClickListener
) :
    RecyclerView.Adapter<VideoFileAdapter.VideoFileViewHolder>() {
    inner class VideoFileViewHolder(val layoutVideoItemBinding: VideoItemBinding) :
        RecyclerView.ViewHolder(layoutVideoItemBinding.root)

    interface OnClickListener {
        fun playVideo(position: Int)
        fun moreVideo(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoFileViewHolder {
        val view = VideoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoFileViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listVideo.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VideoFileViewHolder, position: Int) {
        val video = listVideo[position]
        holder.itemView.setOnClickListener {
            mOnClickListener.playVideo(position)
        }
        holder.layoutVideoItemBinding.apply {
            tvSize.text = formatFileSize(video.originalSize)
            tvDuration.text = timeConversion(video.duration)
            tvVideoName.text = "${video.fileName}.mp4"
            val id = video.originalPath.substringAfterLast("/")
            val videoUri = getMediaStoreUri(id)
            Glide.with(context).load(videoUri).error(R.drawable.avatar)
                .into(thumbnail)
            btnMore.setOnClickListener {
                mOnClickListener.moreVideo(position)
            }
        }
    }

    private fun getMediaStoreUri(id: String): Uri {
        return Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
    }


    private fun formatFileSize(sizeInBytes: Long): String {
        if (sizeInBytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(sizeInBytes.toDouble()) / log10(1024.0)).toInt()
        return String.format(
            "%.2f %s",
            sizeInBytes / 1024.0.pow(digitGroups.toDouble()),
            units[digitGroups]
        )
    }

    private fun timeConversion(value: Long): String {
        var videoTime: String?
        val duration = value.toInt()
        val hrs = (duration / 3600000)
        val mns = (duration / 60000) % 60000
        val sc = duration % 60000 / 1000
        if (hrs > 0) {
            videoTime = String.format("%02d:%02d:%02d", hrs, mns, sc)
        } else {
            videoTime = String.format("%02d:%02d", mns, sc)
        }
        return videoTime
    }
}