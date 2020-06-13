package com.atmko.podcastapp.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.atmko.podcastapp.model.Podcast
import com.atmko.podcastapp.util.loadNetworkImage
import kotlinx.android.synthetic.main.item_podcast_list.view.*
import kotlinx.android.synthetic.main.item_podcast_square.view.podcastImageView

class PodcastAdapter(var podcasts: ArrayList<Podcast>, private val layoutResource: Int,
                     private val clickListener: OnPodcastItemClickListener):
    RecyclerView.Adapter<PodcastAdapter.PodcastViewHolder>() {

    interface OnPodcastItemClickListener {
        fun onItemClick(podcast: Podcast)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PodcastViewHolder {
        val view: View = LayoutInflater
            .from(parent.context)
            .inflate(layoutResource, parent, false)
        return PodcastViewHolder(view)
    }

    override fun getItemCount(): Int {
        return podcasts.size
    }

    inner class PodcastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val podcastImageView: ImageView = itemView.podcastImageView
        val title: TextView = itemView.title
        val publisher: TextView = itemView.publisher
        val totalEpisodes: TextView = itemView.totalEpisodes

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            clickListener.onItemClick(podcasts[adapterPosition])
        }
    }

    override fun onBindViewHolder(holder: PodcastViewHolder, position: Int) {
        val podcast: Podcast = podcasts[position]
        holder.title.text = podcast.title
        holder.publisher.text = podcast.publisher
        holder.podcastImageView.loadNetworkImage(podcast.image)
        holder.totalEpisodes.text =
            String.format(holder.totalEpisodes.text.toString(), podcast.totalEpisodes)
    }

    fun updatePodcasts(updatedPodcasts: List<Podcast>) {
        podcasts.clear()
        podcasts.addAll(updatedPodcasts)
        notifyDataSetChanged()
    }
}