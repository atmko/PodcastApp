package com.atmko.skiptoit.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.atmko.skiptoit.databinding.FragmentRepliesBinding
import com.atmko.skiptoit.model.*
import com.atmko.skiptoit.util.loadNetworkImage
import com.atmko.skiptoit.view.adapters.CommentsAdapter
import com.atmko.skiptoit.view.common.BaseFragment
import com.atmko.skiptoit.viewmodel.MasterActivityViewModel
import com.atmko.skiptoit.viewmodel.RepliesViewModel
import com.atmko.skiptoit.viewmodel.ViewModelFactory
import javax.inject.Inject

const val PARENT_COMMENT_POSITION = -1

class RepliesFragment: BaseFragment(), CommentsAdapter.OnCommentItemClickListener {

    private var _binding: FragmentRepliesBinding? = null
    private val binding get() = _binding!!

    private lateinit var parentComment: Comment

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private var viewModel: RepliesViewModel? = null

    private lateinit var masterActivityViewModel: MasterActivityViewModel
    private var user: User? = null

    @Inject
    lateinit var repliesAdapter: CommentsAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)

        getPresentationComponent().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args: RepliesFragmentArgs by navArgs()
        parentComment = args.parentComment

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
        observeEditCommentLiveData()
        configureMasterActivityViewModel()
    }

    private fun configureBaseBackButtonFunctionality() {
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val masterActivity = (activity as MasterActivity)
                if (masterActivity.isBottomSheetExpanded()) {
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
            viewModel = ViewModelProviders.of(this,
                viewModelFactory).get(RepliesViewModel::class.java)
        }

        setupParentComment(parentComment)

        //todo refactor other view models to follow this(with logic in view model)
        masterActivityViewModel = ViewModelProvider(requireActivity(),
            viewModelFactory).get(MasterActivityViewModel::class.java)
        masterActivityViewModel.getUser()

        if (savedInstanceState == null) {
            viewModel?.getReplies(parentComment.commentId, 0)
        }
    }

    private fun setupParentComment(comment: Comment?) {
        binding.parentComment.replyButton.setOnClickListener {
            onReplyButtonClick(comment!!.commentId, comment.body)
        }
        binding.parentComment.upVoteButton.setOnClickListener {
            onUpVoteClick(comment!!, PARENT_COMMENT_POSITION)
        }
        binding.parentComment.downVoteButton.setOnClickListener {
            onDownVoteClick(comment!!, PARENT_COMMENT_POSITION)
        }

        binding.parentComment.user.text = comment?.username
        binding.parentComment.body.text = comment?.body
        binding.parentComment.votes.text = comment?.voteTally.toString()
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
        viewModel?.localCommentVoteUpdate?.observe(viewLifecycleOwner, Observer { localCommentVoteUpdate ->
            if (localCommentVoteUpdate.adapterPosition == PARENT_COMMENT_POSITION) {
                setupParentComment(localCommentVoteUpdate.comment)
            } else {
                repliesAdapter.updateChangedComment(
                    localCommentVoteUpdate.comment, localCommentVoteUpdate.adapterPosition
                )
            }
        })

        viewModel?.commentReplies?.observe(viewLifecycleOwner, Observer { replies ->
            binding.resultsFrameLayout.resultsRecyclerView.visibility = View.VISIBLE
            replies?.let {
                repliesAdapter.updateComments(replies)
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

    private fun observeEditCommentLiveData() {
        val editCommentLiveData = findNavController()
            .currentBackStackEntry?.savedStateHandle?.getLiveData<BodyUpdate>(BODY_UPDATE_KEY)
        editCommentLiveData?.observe(viewLifecycleOwner, Observer {
            repliesAdapter.updateChangedCommentBody(it.body, it.adapterPosition)
        })
    }

    private fun configureMasterActivityViewModel() {
        masterActivityViewModel.currentUser.observe(viewLifecycleOwner, Observer {
            user = it
        })
    }

    private fun attemptToReplyComment(parentId: String, quotedText: String) {
        if (user != null) {
            navigateToReplyComment(user!!.username!!, parentId, quotedText)
        } else {
            masterActivityViewModel.signIn()
        }
    }

    private fun attemptToUpdateReply(comment: Comment, position: Int) {
        if (user != null) {
            navigateToUpdateReply(comment, user!!.username!!, position)
        } else {
            masterActivityViewModel.signIn()
        }
    }

    private fun navigateToReplyComment(username: String, parentId: String, quotedText: String) {
        val action = RepliesFragmentDirections
            .actionNavigationRepliesToNavigationCreateReply(
                parentId, quotedText, username)
        view?.findNavController()?.navigate(action)
    }

    private fun navigateToUpdateReply(comment: Comment, username: String, position: Int) {
        val action = RepliesFragmentDirections
            .actionNavigationEpisodeToNavigationUpdateReply(
                comment.commentId, username, binding.parentComment.body.text.toString(),
                comment.body, position)
        view?.findNavController()?.navigate(action)
    }

    override fun onReplyButtonClick(commentId: String, quotedText: String) {
        attemptToReplyComment(commentId, quotedText)
    }

    override fun onRepliesButtonClick(comment: Comment) {
        val action = RepliesFragmentDirections
            .actionNavigationRepliesToNavigationReplies(comment)
        view?.findNavController()?.navigate(action)
    }

    override fun onUpVoteClick(comment: Comment, position: Int) {
        viewModel?.onUpVoteClick(comment, position)
    }

    override fun onDownVoteClick(comment: Comment, position: Int) {
        viewModel?.onDownVoteClick(comment, position)
    }

    override fun onDeleteClick(comment: Comment, position: Int) {
        viewModel?.deleteComment(repliesAdapter, comment, position)
    }

    override fun onEditClick(comment: Comment, position: Int) {
        attemptToUpdateReply(comment, position)
    }
}
