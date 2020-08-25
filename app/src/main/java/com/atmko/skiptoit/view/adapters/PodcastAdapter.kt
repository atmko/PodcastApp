package com.atmko.skiptoit.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.atmko.skiptoit.R
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.util.loadNetworkImage
import kotlinx.android.synthetic.main.item_podcast_list.view.*
import kotlinx.android.synthetic.main.item_podcast_square.view.podcastImageView

class PodcastAdapter(private val clickListener: OnPodcastItemClickListener) :
    PagedListAdapter<Podcast, PodcastAdapter.PodcastViewHolder>(Podcast.PodcastDiffCallback()) {

    interface OnPodcastItemClickListener {
        fun onItemClick(podcast: Podcast)
        fun onSubscriptionToggle(podcast: Podcast)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PodcastViewHolder {
        val view: View = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_podcast_list, parent, false)
        return PodcastViewHolder(view)
    }

    inner class PodcastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val podcastImageView: ImageView = itemView.podcastImageView
        val title: TextView = itemView.title
        val publisher: TextView = itemView.publisher
        val totalEpisodes: TextView = itemView.totalEpisodes
        val toggleSubscriptionButton: ImageButton = itemView.toggleSubscriptionButton

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            clickListener.onItemClick(getItem(adapterPosition)!!)
        }
    }

    override fun onBindViewHolder(holder: PodcastViewHolder, position: Int) {
        val podcast: Podcast = getItem(holder.adapterPosition)!!
        holder.title.text = podcast.title
        holder.publisher.text = podcast.publisher
        holder.podcastImageView.loadNetworkImage(podcast.image)
        holder.totalEpisodes.text =
            String.format(holder.totalEpisodes.text.toString(), podcast.totalEpisodes)
        holder.toggleSubscriptionButton.setOnClickListener {
            clickListener.onSubscriptionToggle(podcast)
        }
    }
}