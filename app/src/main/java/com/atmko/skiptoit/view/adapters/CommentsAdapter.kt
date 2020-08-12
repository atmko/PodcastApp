package com.atmko.skiptoit.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.atmko.skiptoit.R
import com.atmko.skiptoit.databinding.ItemCommentBinding
import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.model.VOTE_WEIGHT_DOWN_VOTE
import com.atmko.skiptoit.model.VOTE_WEIGHT_UP_VOTE
import com.atmko.skiptoit.util.loadNetworkImage

class CommentsAdapter(
    private val clickListener: OnCommentItemClickListener
) :
    RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    private val comments = arrayListOf<Comment>()

    interface OnCommentItemClickListener {
        fun onReplyButtonClick(commentId: String, quotedText: String)
        fun onRepliesButtonClick(comment: Comment)
        fun onUpVoteClick(comment: Comment, position: Int)
        fun onDownVoteClick(comment: Comment, position: Int)
        fun onDeleteClick(comment: Comment, position: Int)
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
        holder.binding.upVoteButton.setOnClickListener {
            clickListener.onUpVoteClick(comment, position)
        }
        holder.binding.downVoteButton.setOnClickListener {
            clickListener.onDownVoteClick(comment, position)
        }
        holder.binding.deleteButton.setOnClickListener {
            clickListener.onDeleteClick(comment, position)
        }

        holder.binding.user.text = comment.username
        holder.binding.body.text = comment.body

        when (comment.voteWeight) {
            VOTE_WEIGHT_UP_VOTE -> {
                holder.binding.upVoteButton.setImageResource(R.drawable.ic_up_vote_color)
                holder.binding.downVoteButton.setImageResource(R.drawable.ic_down_vote)
            }
            VOTE_WEIGHT_DOWN_VOTE -> {
                holder.binding.downVoteButton.setImageResource(R.drawable.ic_down_vote_color)
                holder.binding.upVoteButton.setImageResource(R.drawable.ic_up_vote)
            }
            else -> {
                holder.binding.upVoteButton.setImageResource(R.drawable.ic_up_vote)
                holder.binding.downVoteButton.setImageResource(R.drawable.ic_down_vote)
            }
        }

        holder.binding.votes.text = comment.voteTally.toString()

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

    fun updateChangedComment(commentUpdate: Comment, position: Int) {
        comments[position] = commentUpdate
        notifyItemChanged(position)
    }

    fun updateRemovedComment(position: Int) {
        comments.removeAt(position)
        notifyItemRemoved(position)
    }
}