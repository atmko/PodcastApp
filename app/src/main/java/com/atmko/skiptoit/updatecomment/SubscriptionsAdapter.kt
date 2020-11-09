package com.atmko.skiptoit.updatecomment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.atmko.skiptoit.databinding.ItemPodcastListBinding
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.utils.loadNetworkImage

class SubscriptionsAdapter(
    private val clickListener: OnSubscriptionItemClickListener
) : RecyclerView.Adapter<SubscriptionsAdapter.PodcastViewHolder>() {

    interface OnSubscriptionItemClickListener {
        fun onItemClick(podcast: Podcast)
        fun onSubscriptionToggle(podcast: Podcast)
    }

    var subscriptions = arrayListOf<Podcast>()

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
            clickListener.onItemClick(subscriptions[adapterPosition])
        }
    }

    override fun onBindViewHolder(holder: PodcastViewHolder, position: Int) {
        val podcast: Podcast = subscriptions[holder.adapterPosition]
        holder.binding.title.text = podcast.title
        holder.binding.publisher.text = podcast.publisher
        holder.binding.podcastImageView.loadNetworkImage(podcast.image)
        holder.binding.totalEpisodes.text =
            String.format(holder.binding.totalEpisodes.text.toString(), podcast.totalEpisodes)
        holder.binding.toggleSubscriptionButton.setOnClickListener {
            clickListener.onSubscriptionToggle(podcast)
        }
    }

    override fun getItemCount(): Int {
        return subscriptions.size
    }


    fun updateList(newSubscriptions: List<Podcast>) {
        val diffResult =
            DiffUtil.calculateDiff(SubscriptionDiffCallback(this.subscriptions, newSubscriptions))
        subscriptions.clear()
        subscriptions.addAll(newSubscriptions)
        diffResult.dispatchUpdatesTo(this)
    }

    class SubscriptionDiffCallback(
        private val oldSubscriptions: List<Podcast>,
        private val newSubscriptions: List<Podcast>
    ) :
        DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldSubscriptions[oldItemPosition].id == newSubscriptions[newItemPosition].id
        }

        override fun getOldListSize(): Int {
            return oldSubscriptions.size
        }

        override fun getNewListSize(): Int {
            return newSubscriptions.size
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldSubscriptions[oldItemPosition] == newSubscriptions[newItemPosition];
        }

        @Nullable
        override fun getChangePayload(oldPosition: Int, newPosition: Int): Any? {
            return super.getChangePayload(oldPosition, newPosition)
        }
    }
}