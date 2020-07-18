package com.atmko.skiptoit.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.atmko.skiptoit.databinding.FragmentRepliesBinding
import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.util.loadNetworkImage
import com.atmko.skiptoit.view.adapters.CommentsAdapter
import com.atmko.skiptoit.viewmodel.CommentsViewModel

class RepliesFragment: Fragment(), CommentsAdapter.OnCommentItemClickListener {

    private var _binding: FragmentRepliesBinding? = null
    private val binding get() = _binding!!

    private lateinit var commentId: String
    private var parentId: String? = null

    private var viewModel: CommentsViewModel? = null
    private val repliesAdapter: CommentsAdapter = CommentsAdapter(arrayListOf(), this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args: RepliesFragmentArgs by navArgs()
        commentId = args.commentId
        parentId = args.parentId
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRepliesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        configureViews()
        configureValues(savedInstanceState)
        configureViewModel()
    }

    private fun configureViews() {
        binding.resultsFrameLayout.resultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = repliesAdapter
        }
    }

    private fun configureValues(savedInstanceState: Bundle?) {
        if (viewModel == null) {
            activity?.let {
                viewModel = ViewModelProviders.of(it).get(CommentsViewModel::class.java)
            }

            viewModel?.retrieveParentComment(commentId).let { comment ->
                setupParentComment(comment)
            }
        }
    }

    private fun setupParentComment(comment: Comment?) {
        binding.parentComment.replyButton.setOnClickListener {
            onReplyButtonClick(comment!!.commentId, comment.body)
        }
        binding.parentComment.replies.setOnClickListener {
            onRepliesButtonClick(comment!!)
        }
        binding.parentComment.user.text = comment?.username
        binding.parentComment.body.text = comment?.body
        binding.parentComment.votes.text = comment?.votes.toString()
        if (comment?.replies != 0) {
            binding.parentComment.replies.text =
                String.format(
                    binding.parentComment.replies.text.toString(),
                    comment?.replies.toString()
                )
        } else {
            binding.parentComment.replies.visibility = View.GONE
        }
        comment?.profileImage?.let { binding.parentComment.profileImageView.loadNetworkImage(it) }
    }

    private fun configureViewModel() {

    }

    override fun onReplyButtonClick(commentId: String, quotedText: String?) {
        Toast.makeText(context, "not yet implemented", Toast.LENGTH_SHORT).show()
    }

    override fun onRepliesButtonClick(comment: Comment) {
        Toast.makeText(context, "not yet implemented", Toast.LENGTH_SHORT).show()
    }
}
