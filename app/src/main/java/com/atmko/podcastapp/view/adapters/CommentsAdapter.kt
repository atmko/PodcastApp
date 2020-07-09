package com.atmko.podcastapp.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.atmko.podcastapp.databinding.ItemCommentBinding
import com.atmko.podcastapp.model.Comment
import com.atmko.podcastapp.util.loadNetworkImage

class CommentsAdapter(
    var comments: ArrayList<Comment>,
    private val clickListener: OnCommentItemClickListener
) :
    RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    interface OnCommentItemClickListener {
        fun onItemClick(comment: Comment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        return CommentViewHolder(
            ItemCommentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return comments.size
    }

    inner class CommentViewHolder(var binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            clickListener.onItemClick(comments[adapterPosition])
        }
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment: Comment = comments[position]
        holder.binding.user.text = comment.username
        holder.binding.body.text = comment.body
        comment.profileImage?.let { holder.binding.profileImageView.loadNetworkImage(it) }
    }

    fun updateComments(updatedComments: List<Comment>) {
        comments.clear()
        comments.addAll(updatedComments)
        notifyDataSetChanged()
    }
}