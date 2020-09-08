package com.atmko.skiptoit.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.atmko.skiptoit.databinding.ItemEpisodeBinding
import com.atmko.skiptoit.model.Episode

class EpisodeAdapter(
    private val clickListener: OnEpisodeItemClickListener
) : PagedListAdapter<Episode, EpisodeAdapter.EpisodeViewHolder>(Episode.EpisodeDiffCallback()) {

    interface OnEpisodeItemClickListener {
        fun onItemClick(episode: Episode)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        return EpisodeViewHolder(
            ItemEpisodeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    inner class EpisodeViewHolder(var binding: ItemEpisodeBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        if (position >= itemCount) return
        val episode: Episode = getItem(position) ?: return

        holder.binding.topLayout.setOnClickListener {
            clickListener.onItemClick(episode)
        }
        holder.binding.title.text = episode.title
        holder.binding.date.text = episode.getFormattedPublishDate()
        holder.binding.length.text = episode.getFormattedAudioLength()
    }
}