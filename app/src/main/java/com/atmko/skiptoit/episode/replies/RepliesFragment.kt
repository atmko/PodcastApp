package com.atmko.skiptoit.episode.replies

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.atmko.skiptoit.R
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

class RepliesFragment : BaseFragment(),
    RepliesViewModel.Listener,
    CommentsAdapter.OnCommentItemClickListener,
    CommentsViewModel.Listener, BaseBoundaryCallback.Listener {

    private var _binding: FragmentRepliesBinding? = null
    private val binding get() = _binding!!

    lateinit var parentCommentId: String

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var repliesViewModel: RepliesViewModel

    private lateinit var parentComment: Comment

    @Inject
    lateinit var repliesAdapter: CommentsAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)

        getPresentationComponent().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args: RepliesFragmentArgs by navArgs()
        parentCommentId = args.parentCommentId

        configureBaseBackButtonFunctionality()
        defineViewModel()
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
    }

    override fun onStart() {
        super.onStart()
        repliesViewModel.registerRepliesViewModelListener(this)
        repliesViewModel.registerListener(this)
        repliesViewModel.registerBoundaryCallbackListener(this)

        repliesViewModel.getParentCommentAndNotify(parentCommentId)
        repliesViewModel.getReplies(parentCommentId)

        configureViewModel()
    }

    override fun onStop() {
        super.onStop()
        repliesViewModel.unregisterRepliesViewModelListener(this)
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

    private fun defineViewModel() {
        repliesViewModel = ViewModelProvider(
            this,
            viewModelFactory
        ).get(RepliesViewModel::class.java)
    }

    private fun configureViews() {
        binding.resultsRecyclerView.resultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = repliesAdapter
        }
    }

    private fun setupParentComment() {
        binding.parentComment.replyButton.setOnClickListener {
            onReplyButtonClick(parentComment.commentId)
        }
        binding.parentComment.upVoteButton.setOnClickListener {
            onUpVoteClick(parentComment)
        }
        binding.parentComment.downVoteButton.setOnClickListener {
            onDownVoteClick(parentComment)
        }
        binding.parentComment.deleteButton.setOnClickListener {
            onDeleteClick(parentComment)
        }
        binding.parentComment.editButton.setOnClickListener {
            // todo: move logic to view model
            if (parentComment.parentId != null) {
                attemptToUpdateReply(parentComment)
            } else {
                attemptToUpdateComment(parentComment)
            }
        }

        //todo: user listeners instead
        repliesAdapter
            .configureUpDownVoteButtonResource(
                parentComment,
                binding.parentComment.upVoteButton,
                binding.parentComment.downVoteButton
            )

        //todo: user listeners instead
        repliesAdapter
            .configureDeleteEditButtonVisibility(
                parentComment,
                binding.parentComment.editButton,
                binding.parentComment.deleteButton
            )

        binding.parentComment.user.text = parentComment.username ?: ""
        binding.parentComment.body.text = parentComment.body ?: ""
        binding.parentComment.votes.text = parentComment.voteTally.toString()
        if (parentComment.replies != 0) {
            binding.parentComment.replies.text =
                String.format(
                    getString(R.string.replies_format),
                    parentComment.replies.toString()
                )
        } else {
            binding.parentComment.replies.visibility = View.GONE
        }
        parentComment.profileImage?.let { binding.parentComment.profileImageView.loadNetworkImage(it) }
    }

    private fun configureViewModel() {
        repliesViewModel.retrievedComments!!.observe(viewLifecycleOwner, Observer { replies ->
            replies?.let {
                repliesAdapter.submitList(replies)
                repliesAdapter.notifyDataSetChanged()
            }
        })
    }

    private fun attemptToReplyComment(parentId: String) {
        val user = getMasterActivity().masterActivityViewModel.currentUser
        if (user?.username != null) {
            navigateToReplyComment(user.username, parentId)
        } else {
            getMasterActivity().masterActivityViewModel.silentSignInAndNotify()
        }
    }

    private fun attemptToUpdateReply(comment: Comment) {
        val user = getMasterActivity().masterActivityViewModel.currentUser
        // todo: move logic to view model
        if (user?.username != null) {
            navigateToUpdateReply(comment, user.username)
        } else {
            getMasterActivity().masterActivityViewModel.silentSignInAndNotify()
        }
    }

    private fun attemptToUpdateComment(comment: Comment) {
        val user = getMasterActivity().masterActivityViewModel.currentUser
        if (user?.username != null) {
            navigateToUpdateComment(comment, user.username)
        } else {
            getMasterActivity().masterActivityViewModel.silentSignInAndNotify()
        }
    }

    private fun navigateToReplyComment(username: String, parentId: String) {
        val action =
            RepliesFragmentDirections.actionNavigationRepliesToNavigationCreateReply(
                parentId, username
            )
        view?.findNavController()?.navigate(action)
    }

    private fun navigateToUpdateReply(comment: Comment, username: String) {
        val action =
            RepliesFragmentDirections.actionNavigationRepliesToNavigationUpdateReply(
                comment.parentId!!,
                comment.commentId,
                username,
                binding.parentComment.body.text.toString()
            )
        view?.findNavController()?.navigate(action)
    }

    private fun navigateToUpdateComment(comment: Comment, username: String) {
        val action =
            RepliesFragmentDirections.actionNavigationRepliesToNavigationUpdateComment(
                comment.commentId, username
            )
        view?.findNavController()?.navigate(action)
    }

    override fun onReplyButtonClick(commentId: String) {
        attemptToReplyComment(commentId)
    }

    override fun onRepliesButtonClick(comment: Comment) {
        val action =
            RepliesFragmentDirections.actionNavigationRepliesToNavigationReplies(
                comment.commentId
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

    override fun onParentCommentFetched(comment: Comment) {
        parentComment = comment
        setupParentComment()
    }

    override fun onParentCommentFetchFailed() {
        Snackbar.make(
            requireView(),
            getString(R.string.failed_to_retrieve_comment),
            Snackbar.LENGTH_LONG
        ).show()
    }

    override fun onVoteParentUpdate() {
        repliesViewModel.getParentCommentAndNotify(parentCommentId)
    }

    override fun onDeleteParentComment() {
        getMasterActivity().onBackPressedDispatcher.onBackPressed()
    }

    override fun onWipeParentComment() {
        repliesViewModel.getParentCommentAndNotify(parentCommentId)
    }

    override fun onVoteUpdate() {
        binding.pageLoading.pageLoading.visibility = View.INVISIBLE
    }

    override fun onVoteUpdateFailed() {
        binding.pageLoading.pageLoading.visibility = View.INVISIBLE
        Snackbar.make(requireView(), getString(R.string.vote_update_failed), Snackbar.LENGTH_LONG)
            .show()
    }

    override fun onDeleteComment() {
        binding.pageLoading.pageLoading.visibility = View.INVISIBLE
        repliesViewModel.getParentCommentAndNotify(parentCommentId)
    }

    override fun onWipeComment() {
        binding.pageLoading.pageLoading.visibility = View.INVISIBLE
        repliesViewModel.getParentCommentAndNotify(parentCommentId)
    }

    override fun onDeleteCommentFailed() {
        binding.pageLoading.pageLoading.visibility = View.INVISIBLE
        Snackbar.make(
            requireView(),
            getString(R.string.failed_to_delete_comment),
            Snackbar.LENGTH_LONG
        ).show()
    }

    override fun onUpdateReplyCountFailed() {
        Log.d(this.javaClass.name, "failed to decrease reply count")
    }

    override fun onPageLoading() {
        binding.pageLoading.pageLoading.visibility = View.VISIBLE
    }

    override fun onPageLoad() {
        binding.pageLoading.pageLoading.visibility = View.INVISIBLE
    }

    override fun onPageLoadFailed() {
        binding.pageLoading.pageLoading.visibility = View.INVISIBLE
        Snackbar.make(requireView(), getString(R.string.failed_to_load_page), Snackbar.LENGTH_LONG)
            .show()
    }
}
