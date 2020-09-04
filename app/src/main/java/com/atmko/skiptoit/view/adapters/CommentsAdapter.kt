package com.atmko.skiptoit.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.paging.PagedListAdapter
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
    PagedListAdapter<Comment, CommentsAdapter.CommentViewHolder>(Comment.CommentDiffCallback()) {

    interface OnCommentItemClickListener {
        fun onReplyButtonClick(commentId: String, quotedText: String)
        fun onRepliesButtonClick(comment: Comment)
        fun onUpVoteClick(comment: Comment)
        fun onDownVoteClick(comment: Comment)
        fun onDeleteClick(comment: Comment)
        fun onEditClick(comment: Comment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        return CommentViewHolder(
            ItemCommentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    inner class CommentViewHolder(var binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        if (position >= itemCount) return
        val comment: Comment = getItem(position) ?: return
        holder.binding.replyButton.setOnClickListener {
            clickListener.onReplyButtonClick(comment.commentId, comment.body)
        }
        holder.binding.replies.setOnClickListener {
            clickListener.onRepliesButtonClick(comment)
        }
        holder.binding.upVoteButton.setOnClickListener {
            clickListener.onUpVoteClick(comment)
        }
        holder.binding.downVoteButton.setOnClickListener {
            clickListener.onDownVoteClick(comment)
        }
        holder.binding.deleteButton.setOnClickListener {
            clickListener.onDeleteClick(comment)
        }
        holder.binding.editButton.setOnClickListener {
            clickListener.onEditClick(comment)
        }

        holder.binding.user.text = comment.username
        holder.binding.body.text = comment.body

        configureUpDownVoteButtonResource(
            comment,
            holder.binding.upVoteButton,
            holder.binding.downVoteButton
        )

        configureDeleteEditButtonVisibility(
            comment,
            holder.binding.editButton,
            holder.binding.deleteButton
        )

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

    fun configureUpDownVoteButtonResource(
        comment: Comment,
        upVoteButton: ImageButton,
        downVoteButton: ImageButton
    ) {
        when (comment.voteWeight) {
            VOTE_WEIGHT_UP_VOTE -> {
                upVoteButton.setImageResource(R.drawable.ic_up_vote_color)
                downVoteButton.setImageResource(R.drawable.ic_down_vote)
            }
            VOTE_WEIGHT_DOWN_VOTE -> {
                downVoteButton.setImageResource(R.drawable.ic_down_vote_color)
                upVoteButton.setImageResource(R.drawable.ic_up_vote)
            }
            else -> {
                upVoteButton.setImageResource(R.drawable.ic_up_vote)
                downVoteButton.setImageResource(R.drawable.ic_down_vote)
            }
        }
    }

    fun configureDeleteEditButtonVisibility(
        comment: Comment,
        editButton: ImageButton,
        deleteButton: ImageButton
    ) {
        if (comment.isUserComment) {
            editButton.visibility = View.VISIBLE
            deleteButton.visibility = View.VISIBLE
        } else {
            editButton.visibility = View.GONE
            deleteButton.visibility = View.GONE
        }
    }
}