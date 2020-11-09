package com.atmko.skiptoit.search.searchchild

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.atmko.skiptoit.R
import com.atmko.skiptoit.databinding.ItemPodcastListBinding
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.utils.loadNetworkImage

class PodcastAdapter(
    private val clickListener: OnPodcastItemClickListener,
    private val context: Context
) :
    PagedListAdapter<Podcast, PodcastAdapter.PodcastViewHolder>(Podcast.PodcastDiffCallback()) {

    interface OnPodcastItemClickListener {
        fun onItemClick(podcast: Podcast)
        fun onSubscriptionToggle(podcast: Podcast)
    }

    var subscriptions: HashMap<String, Unit?>? = HashMap()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PodcastViewHolder {
        return PodcastViewHolder(
            ItemPodcastListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    inner class PodcastViewHolder(var binding: ItemPodcastListBinding) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            clickListener.onItemClick(getItem(adapterPosition)!!)
        }
    }

    override fun onBindViewHolder(holder: PodcastViewHolder, position: Int) {
        val podcast: Podcast = getItem(holder.adapterPosition)!!
        holder.binding.title.text = podcast.title
        holder.binding.publisher.text = podcast.publisher
        holder.binding.podcastImageView.loadNetworkImage(podcast.image)
        holder.binding.totalEpisodes.text =
            String.format(holder.binding.totalEpisodes.text.toString(), podcast.totalEpisodes)
        holder.binding.toggleSubscriptionButton.setOnClickListener {
            clickListener.onSubscriptionToggle(podcast)
        }

        if (isSubscribed(podcast.id)) {
            setIsSubscribed(holder)
        } else {
            setIsNotSubscribed(holder)
        }
    }

    private fun isSubscribed(podcastId: String): Boolean {
        return subscriptions != null && subscriptions!!.containsKey(podcastId)
    }

    private fun setIsSubscribed(holder: PodcastViewHolder) {
        holder.binding.toggleSubscriptionButton.setImageDrawable(
            context.resources.getDrawable(R.drawable.ic_subscribed_button_black)
        )
    }

    private fun setIsNotSubscribed(holder: PodcastViewHolder) {
        holder.binding.toggleSubscriptionButton.setImageDrawable(
            context.resources.getDrawable(R.drawable.ic_subscribe_button_black)
        )
    }
}