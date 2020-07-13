package com.atmko.skiptoit.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.atmko.skiptoit.databinding.ItemEpisodeBinding
import com.atmko.skiptoit.model.Episode

class EpisodeAdapter(
    var episodes: ArrayList<Episode>,
    private val clickListener: OnEpisodeItemClickListener
) :
    RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder>() {

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

    override fun getItemCount(): Int {
        return episodes.size
    }

    inner class EpisodeViewHolder(var binding: ItemEpisodeBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            clickListener.onItemClick(episodes[adapterPosition])
        }
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        val episode: Episode = episodes[position]
        holder.binding.title.text = episode.title
        holder.binding.date.text = episode.getFormattedPublishDate()
        holder.binding.length.text = episode.getFormattedAudioLength()
    }

    fun updateEpisodes(updatedEpisodes: List<Episode>) {
        episodes.clear()
        episodes.addAll(updatedEpisodes)
        notifyDataSetChanged()
    }
}