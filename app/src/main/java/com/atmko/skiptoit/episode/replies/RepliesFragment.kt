package com.atmko.skiptoit.episode.replies

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.atmko.skiptoit.common.BaseBoundaryCallback
import com.atmko.skiptoit.common.ViewModelFactory
import com.atmko.skiptoit.common.views.BaseFragment
import com.atmko.skiptoit.databinding.FragmentRepliesBinding
import com.atmko.skiptoit.episode.CommentsAdapter
import com.atmko.skiptoit.episode.common.CommentsViewModel
import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.utils.loadNetworkImage
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

class RepliesFragment : BaseFragment(), CommentsAdapter.OnCommentItemClickListener,
    CommentsViewModel.Listener, BaseBoundaryCallback.Listener {

    private var _binding: FragmentRepliesBinding? = null
    private val binding get() = _binding!!

    lateinit var parentComment: Comment

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var repliesViewModel: RepliesViewModel

    @Inject
    lateinit var repliesAdapter: CommentsAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)

        getPresentationComponent().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args: RepliesFragmentArgs by navArgs()
        //todo replace with database call in view model
        parentComment = args.parentComment

        configureBaseBackButtonFunctionality()
    }

    override fun onResume() {
        super.onResume()
        repliesViewModel.registerListener(this)
        repliesViewModel.registerBoundaryCallbackListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRepliesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureToolbar(binding.toolbar.toolbar)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        configureViews()
        configureValues()
        configureViewModel()
    }

    override fun onPause() {
        super.onPause()
        repliesViewModel.unregisterListener(this)
        repliesViewModel.unregisterBoundaryCallbackListener(this)
    }

    private fun configureBaseBackButtonFunctionality() {
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (getMasterActivity().isBottomSheetExpanded()) {
                    view?.findNavController()?.navigateUp()
                }
            }
        }

        activity?.onBackPressedDispatcher?.addCallback(this, callback)
    }

    private fun configureViews() {
        binding.resultsRecyclerView.resultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = repliesAdapter
        }
    }

    private fun configureValues() {
        setupParentComment(parentComment)

        repliesViewModel = ViewModelProvider(
            this,
            viewModelFactory
        ).get(RepliesViewModel::class.java)

        repliesViewModel.getReplies(parentComment.commentId)
    }

    private fun setupParentComment(comment: Comment) {
        binding.parentComment.replyButton.setOnClickListener {
            onReplyButtonClick(comment.commentId, comment.body)
        }
        binding.parentComment.upVoteButton.setOnClickListener {
            onUpVoteClick(comment)
        }
        binding.parentComment.downVoteButton.setOnClickListener {
            onDownVoteClick(comment)
        }
        binding.parentComment.deleteButton.setOnClickListener {
            onDeleteClick(comment)
        }
        binding.parentComment.editButton.setOnClickListener {
            onEditClick(comment)
        }

        repliesAdapter
            .configureUpDownVoteButtonResource(
                comment,
                binding.parentComment.upVoteButton,
                binding.parentComment.downVoteButton
            )

        repliesAdapter
            .configureDeleteEditButtonVisibility(
                comment,
                binding.parentComment.editButton,
                binding.parentComment.deleteButton
            )

        binding.parentComment.user.text = comment.username
        binding.parentComment.body.text = comment.body
        binding.parentComment.votes.text = comment.voteTally.toString()
        if (comment.replies != 0) {
            binding.parentComment.replies.text =
                String.format(
                    binding.parentComment.replies.text.toString(),
                    comment.replies.toString()
                )
        } else {
            binding.parentComment.replies.visibility = View.GONE
        }
        comment.profileImage?.let { binding.parentComment.profileImageView.loadNetworkImage(it) }
    }

    private fun configureViewModel() {
        repliesViewModel.retrievedComments!!.observe(viewLifecycleOwner, Observer { replies ->
            binding.errorAndLoading.loadingScreen.visibility = View.GONE
            replies?.let {
                repliesAdapter.submitList(replies)
                repliesAdapter.notifyDataSetChanged()
            }
        })
    }

    private fun attemptToReplyComment(parentId: String, quotedText: String) {
        val user = getMasterActivity().user
        if (user?.username != null) {
            navigateToReplyComment(user.username, parentId, quotedText)
        } else {
            getMasterActivity().masterActivityViewModel.silentSignInAndNotify()
        }
    }

    private fun attemptToUpdateReply(comment: Comment) {
        val user = getMasterActivity().user
        if (user?.username != null) {
            navigateToUpdateReply(comment, user.username)
        } else {
            getMasterActivity().masterActivityViewModel.silentSignInAndNotify()
        }
    }

    private fun navigateToReplyComment(username: String, parentId: String, quotedText: String) {
        val action =
            RepliesFragmentDirections.actionNavigationRepliesToNavigationCreateReply(
                parentId, quotedText, username
            )
        view?.findNavController()?.navigate(action)
    }

    private fun navigateToUpdateReply(comment: Comment, username: String) {
        val action =
            RepliesFragmentDirections.actionNavigationEpisodeToNavigationUpdateReply(
                comment.commentId, username, binding.parentComment.body.text.toString()
            )
        view?.findNavController()?.navigate(action)
    }

    override fun onReplyButtonClick(commentId: String, quotedText: String) {
        attemptToReplyComment(commentId, quotedText)
    }

    override fun onRepliesButtonClick(comment: Comment) {
        val action =
            RepliesFragmentDirections.actionNavigationRepliesToNavigationReplies(
                comment
            )
        view?.findNavController()?.navigate(action)
    }

    override fun onUpVoteClick(comment: Comment) {
        repliesViewModel.upVoteAndNotify(comment)
    }

    override fun onDownVoteClick(comment: Comment) {
        repliesViewModel.downVoteAndNotify(comment)
    }

    override fun onDeleteClick(comment: Comment) {
        repliesViewModel.deleteCommentAndNotify(comment)
    }

    override fun onEditClick(comment: Comment) {
        attemptToUpdateReply(comment)
    }

    override fun notifyProcessing() {
        binding.pageLoading.pageLoading.visibility = View.INVISIBLE
        binding.errorAndLoading.errorScreen.visibility = View.GONE
    }

    override fun onVoteUpdate() {
        binding.pageLoading.pageLoading.visibility = View.INVISIBLE
        binding.errorAndLoading.errorScreen.visibility = View.GONE
    }

    override fun onVoteUpdateFailed() {
        binding.pageLoading.pageLoading.visibility = View.INVISIBLE
        Snackbar.make(requireView(), "Vote Update Failed", Snackbar.LENGTH_LONG).show()
    }

    override fun onDeleteComment() {
        binding.pageLoading.pageLoading.visibility = View.INVISIBLE
        binding.errorAndLoading.errorScreen.visibility = View.GONE
    }

    override fun onDeleteCommentFailed() {
        binding.pageLoading.pageLoading.visibility = View.INVISIBLE
        Snackbar.make(requireView(), "Failed to delete comment", Snackbar.LENGTH_LONG).show()
    }

    override fun onPageLoading() {
        binding.pageLoading.pageLoading.visibility = View.VISIBLE
        binding.errorAndLoading.errorScreen.visibility = View.GONE
    }

    override fun onPageLoad() {
        binding.pageLoading.pageLoading.visibility = View.INVISIBLE
        binding.errorAndLoading.errorScreen.visibility = View.GONE
    }

    override fun onPageLoadFailed() {
        binding.pageLoading.pageLoading.visibility = View.INVISIBLE
        Snackbar.make(requireView(), "Failed to load page", Snackbar.LENGTH_LONG).show()
    }
}
