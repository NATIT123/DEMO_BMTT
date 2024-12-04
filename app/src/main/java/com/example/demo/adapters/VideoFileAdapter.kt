package com.example.demo.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.demo.databinding.VideoItemBinding
import com.example.demo.models.User

class VideoFileAdapter(
//    private val listVideo: MutableList<User>,
    private val context: Context,
    private val mOnClickListener: OnClickListener
) :
    RecyclerView.Adapter<VideoFileAdapter.VideoFileViewHolder>() {
    inner class VideoFileViewHolder(val layoutVideoItemBinding: VideoItemBinding) :
        RecyclerView.ViewHolder(layoutVideoItemBinding.root)

    interface OnClickListener {
        fun playVideo(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoFileViewHolder {
        val view = VideoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoFileViewHolder(view)
    }

    override fun getItemCount(): Int {
        return 10
    }

    override fun onBindViewHolder(holder: VideoFileViewHolder, position: Int) {
//        val video = listVideo[position]
        holder.itemView.setOnClickListener {
            mOnClickListener.playVideo(position)
        }
        holder.layoutVideoItemBinding.apply {
            tvSize.text = "5MB"
            tvVideoName.text = "Video1"
            tvVideoName.text = "5.02"
//            Glide.with(context).load().into(a)
        }
    }
}