package com.atmko.skiptoit.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.atmko.skiptoit.databinding.ItemCommentBinding
import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.util.loadNetworkImage

class CommentsAdapter(
    private val clickListener: OnCommentItemClickListener
) :
    RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    private val comments = arrayListOf<Comment>()

    interface OnCommentItemClickListener {
        fun onReplyButtonClick(commentId: String, quotedText: String)
        fun onRepliesButtonClick(comment: Comment)
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
        RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment: Comment = comments[position]

        holder.binding.replyButton.setOnClickListener {
            clickListener.onReplyButtonClick(comment.commentId, comment.body)
        }
        holder.binding.replies.setOnClickListener {
            clickListener.onRepliesButtonClick(comment)
        }
        holder.binding.user.text = comment.username
        holder.binding.body.text = comment.body
        holder.binding.votes.text = comment.votes.toString()
        if (comment.replies != 0) {
            holder.binding.replies.text =
                String.format(holder.binding.replies.text.toString(), comment.replies.toString())
            holder.binding.replies.visibility = View.VISIBLE
        } else {
            holder.binding.replies.visibility = View.GONE
        }
        comment.profileImage?.let { holder.binding.profileImageView.loadNetworkImage(it) }
    }

    fun updateComments(updatedComments: List<Comment>) {
        comments.clear()
        comments.addAll(updatedComments)
        notifyDataSetChanged()
    }
}