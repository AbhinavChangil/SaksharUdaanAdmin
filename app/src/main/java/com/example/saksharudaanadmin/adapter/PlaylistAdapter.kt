package com.example.saksharudaanadmin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.persistableBundleOf
import androidx.recyclerview.widget.RecyclerView
import com.example.saksharudaanadmin.databinding.ActivityUploadPlaylistBinding
import com.example.saksharudaanadmin.databinding.HomeGvItemBinding
import com.example.saksharudaanadmin.databinding.PlaylistItemBinding
import com.example.saksharudaanadmin.model.PlaylistModel

class PlaylistAdapter(private var playlist: ArrayList<PlaylistModel>):RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {



    inner class PlaylistViewHolder(private val binding: PlaylistItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            val playlistItem = playlist[position]
            binding.tvVideoTitle.text = playlistItem.playlistVideoTitle
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlaylistAdapter.PlaylistViewHolder {
        val binding = PlaylistItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistAdapter.PlaylistViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = playlist.size
}