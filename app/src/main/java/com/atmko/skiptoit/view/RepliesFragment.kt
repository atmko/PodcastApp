package com.atmko.skiptoit.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.atmko.skiptoit.R
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

        configureBaseBackButtonFunctionality()
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

    private fun configureBaseBackButtonFunctionality() {
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val masterActivity = (activity as MasterActivity)
                if (masterActivity.isBottomSheetExpanded()) {
                    viewModel?.removeParentComment(commentId)
                    view?.findNavController()?.navigateUp()
                }
            }
        }

        activity?.onBackPressedDispatcher?.addCallback(this, callback)
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

        if (savedInstanceState == null) {
            viewModel?.getReplies(commentId, 0)
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
        viewModel?.repliesMap?.observe(viewLifecycleOwner, Observer { reliesMap ->
            binding.resultsFrameLayout.resultsRecyclerView.visibility = View.VISIBLE
            reliesMap?.get(commentId)?.let {
                repliesAdapter.updateComments(viewModel!!.retrieveReplies(commentId))
            }
        })

        viewModel?.repliesLoading?.observe(viewLifecycleOwner, Observer { isLoading ->
            isLoading?.let {
                binding.resultsFrameLayout.errorAndLoading.loadingScreen.visibility =
                    if (it) View.VISIBLE else View.GONE
                if (it) {
                    binding.resultsFrameLayout.errorAndLoading.errorScreen.visibility = View.GONE
                    binding.resultsFrameLayout.resultsRecyclerView.visibility = View.GONE
                }
            }
        })

        viewModel?.repliesLoadError?.observe(viewLifecycleOwner, Observer { isError ->
            isError.let {
                binding.resultsFrameLayout.errorAndLoading.errorScreen.visibility =
                    if (it) View.VISIBLE else View.GONE
            }
        })
    }

    override fun onReplyButtonClick(commentId: String, quotedText: String?) {
        Toast.makeText(context, "not yet implemented", Toast.LENGTH_SHORT).show()
    }

    override fun onRepliesButtonClick(comment: Comment) {
        Toast.makeText(context, "not yet implemented", Toast.LENGTH_SHORT).show()
    }
}
