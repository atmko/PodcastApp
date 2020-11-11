package com.atmko.skiptoit.details

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.atmko.skiptoit.R
import com.atmko.skiptoit.databinding.ItemEpisodeBinding
import com.atmko.skiptoit.model.Episode

class EpisodeAdapter(
    private val clickListener: OnEpisodeItemClickListener
) : PagedListAdapter<Episode, EpisodeAdapter.EpisodeViewHolder>(Episode.EpisodeDiffCallback()) {

    private lateinit var context: Context

    interface OnEpisodeItemClickListener {
        fun onItemClick(episode: Episode)
        fun onPlayClicked(episode: Episode)
    }

    var currentlyLoadedEpisodeId: String? = null
    var isPlaying: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        context = parent.context
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
        holder.binding.playButton.setOnClickListener {
            clickListener.onPlayClicked(episode)
        }

        holder.binding.title.text = episode.title
        holder.binding.date.text = episode.getFormattedPublishDate()
        holder.binding.length.text = episode.getFormattedAudioLength()

        if (isEpisodePlaying(episode.episodeId)) {
            setPauseButton(holder)
        } else {
            setPlayButton(holder)
        }
    }

    private fun isCurrentlyLoaded(episodeId: String): Boolean {
        return currentlyLoadedEpisodeId == episodeId
    }

    private fun isEpisodePlaying(episodeId: String): Boolean {
        return isCurrentlyLoaded(episodeId) && isPlaying
    }

    private fun setPlayButton(holder: EpisodeViewHolder) {
        holder.binding.playButton.setImageDrawable(
            context.resources.getDrawable(R.drawable.ic_play_button_sharp)
        )
    }

    private fun setPauseButton(holder: EpisodeViewHolder) {
        holder.binding.playButton.setImageDrawable(
            context.resources.getDrawable(R.drawable.ic_pause_button_sharp)
        )
    }
}