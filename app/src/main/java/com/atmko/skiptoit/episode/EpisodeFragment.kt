package com.atmko.skiptoit.episode

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.atmko.skiptoit.R
import com.atmko.skiptoit.common.BaseBoundaryCallback
import com.atmko.skiptoit.common.ViewModelFactory
import com.atmko.skiptoit.common.views.BaseFragment
import com.atmko.skiptoit.databinding.FragmentEpisodeBinding
import com.atmko.skiptoit.episode.common.CommentsViewModel
import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.model.Episode
import com.atmko.skiptoit.services.PlaybackService
import com.atmko.skiptoit.utils.loadNetworkImage
import com.atmko.skiptoit.utils.showFullText
import com.atmko.skiptoit.utils.showLimitedText
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

const val EPISODE_FRAGMENT_KEY = "episode_fragment"

const val SCRUBBER_ANIM_LENGTH: Long = 100
const val SCRUBBER_HIDE_LENGTH: Long = 2000

private const val SHOW_MORE_KEY = "show_more"

class EpisodeFragment : BaseFragment(),
    CommentsAdapter.OnCommentItemClickListener,
    EpisodeViewModel.Listener,
    CommentsViewModel.Listener,
    BaseBoundaryCallback.Listener {

    private var _binding: FragmentEpisodeBinding? = null
    private val binding get() = _binding!!

    //fragment init variable
    var podcastId: String? = null
    var episodeId: String? = null

    private var mIsBound: Boolean = false
    private var mPlaybackService: PlaybackService? = null
    private var isRestoringEpisode: Boolean = false

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var episodeViewModel: EpisodeViewModel
    private var episodeDetails: Episode? = null
    private var nextEpisodeDetails: Episode? = null
    private var prevEpisodeDetails: Episode? = null

    private lateinit var parentCommentsViewModel: ParentCommentsViewModel

    @Inject
    lateinit var commentsAdapter: CommentsAdapter

    private var showMore: Boolean = false

    override fun onAttach(context: Context) {
        super.onAttach(context)

        getPresentationComponent().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args: EpisodeFragmentArgs by navArgs()
        podcastId = args.podcastId
        episodeId = args.episodeId
        defineViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEpisodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        configureViews()
        configureViewValues(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        episodeViewModel.registerListener(this)
        parentCommentsViewModel.registerListener(this)
        parentCommentsViewModel.registerBoundaryCallbackListener(this)

        episodeViewModel.getDetailsAndNotify(episodeId, podcastId)
    }

    override fun onStop() {
        super.onStop()
        if (mIsBound) {
            context?.unbindService(playbackServiceConnection)
        }
        mIsBound = false
        episodeViewModel.unregisterListener(this)
        parentCommentsViewModel.unregisterListener(this)
        parentCommentsViewModel.unregisterBoundaryCallbackListener(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean(SHOW_MORE_KEY, showMore)
        val lastPlaybackPosition = mPlaybackService?.player?.contentPosition
        episodeViewModel.saveState(lastPlaybackPosition)
    }

    //todo nullify binding in on destroy view instead of on destroy
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val playbackServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            mIsBound = false
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mIsBound = true
            mPlaybackService =
                (service as PlaybackService.PlaybackServiceBinder).getService()
            mPlaybackService!!.prepareMediaForPlayback(Uri.parse(episodeDetails!!.audio))
            binding.playPanel.player = mPlaybackService!!.player

            if (!isRestoringEpisode) {
                mPlaybackService!!.play()
            } else {
                mPlaybackService!!.restorePlayback(episodeDetails!!.lastPlaybackPosition)
            }
        }
    }

    private fun defineViewModel() {
        episodeViewModel = ViewModelProvider(
            this,
            viewModelFactory
        ).get(EpisodeViewModel::class.java)

        parentCommentsViewModel = ViewModelProvider(
            this,
            viewModelFactory
        ).get(ParentCommentsViewModel::class.java)
    }

    private fun configureViews() {
        configureDetailExtrasSize()

        //configure time bar click guard
        val timeBar: DefaultTimeBar = binding.playPanel.findViewById(R.id.exo_progress)
        timeBar.hideScrubber()
        val timeBarOverlayButton: Button = binding.playPanel.findViewById(R.id.timeBarOverlayButton)
        timeBarOverlayButton.setOnClickListener {
            timeBarOverlayButton.visibility = View.GONE
            timeBar.showScrubber(SCRUBBER_ANIM_LENGTH)
            Handler().postDelayed({
                timeBar.hideScrubber(SCRUBBER_ANIM_LENGTH)
                timeBarOverlayButton.visibility = View.VISIBLE

            }, SCRUBBER_HIDE_LENGTH)
        }

        // show player controller (prevents black flashes upon loading)
        binding.playPanel.showController()

        binding.nextEpisodeButton.apply {
            isEnabled = false
            setOnClickListener {
                getMasterActivity().loadEpisodeIntoBottomSheet(
                    podcastId!!,
                    nextEpisodeDetails!!.episodeId
                )
            }
        }

        binding.previousEpisodeButton.apply {
            isEnabled = false
            setOnClickListener {
                getMasterActivity().loadEpisodeIntoBottomSheet(
                    podcastId!!,
                    prevEpisodeDetails!!.episodeId
                )
            }
        }

        binding.showMore.setOnClickListener {
            toggleFullOrLimitedDescription()
        }

        binding.addCommentButton.apply {
            isEnabled = false
            setOnClickListener {
                attemptToCreateComment()
            }
        }

        binding.resultsRecyclerView.resultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = commentsAdapter
        }
    }

    private fun attemptToCreateComment() {
        val user = getMasterActivity().masterActivityViewModel.currentUser
        if (user?.username != null) {
            navigateToCreateComment(user.username)
        } else {
            getMasterActivity().masterActivityViewModel.silentSignInAndNotify()
        }
    }

    // todo: consolidate method with method in replies fragment
    private fun attemptToUpdateComment(comment: Comment) {
        val user = getMasterActivity().masterActivityViewModel.currentUser
        //todo: move logic to view model
        if (user?.username != null) {
            navigateToUpdateComment(comment, user.username)
        } else {
            getMasterActivity().masterActivityViewModel.silentSignInAndNotify()
        }
    }

    private fun attemptToReplyComment(parentId: String) {
        val user = getMasterActivity().masterActivityViewModel.currentUser
        if (user?.username != null) {
            navigateToReplyComment(user.username, parentId)
        } else {
            getMasterActivity().masterActivityViewModel.silentSignInAndNotify()
        }
    }

    private fun navigateToCreateComment(username: String) {
        val action =
            EpisodeFragmentDirections.actionNavigationEpisodeToNavigationCrateComment(
                podcastId!!, episodeId!!, username
            )
        view?.findNavController()?.navigate(action)
    }

    private fun navigateToUpdateComment(comment: Comment, username: String) {
        val action =
            EpisodeFragmentDirections.actionNavigationEpisodeToNavigationUpdateComment(
                comment.commentId, username
            )
        view?.findNavController()?.navigate(action)
    }

    private fun navigateToReplyComment(username: String, parentId: String) {
        val action =
            EpisodeFragmentDirections.actionNavigationEpisodeToNavigationCreateReply(
                parentId, username
            )
        view?.findNavController()?.navigate(action)
    }

    private fun configureDetailExtrasSize() {
        val includeDetailsExtras: ConstraintLayout? =
            view?.findViewById(R.id.playPanelConstraintLayout)

        val sideMargins = resources.getDimension(R.dimen.list_excess_margin).toInt() * 2

        //get total weightedWidth
        val weightedWidth: Int = getScreenWidth() - sideMargins

        val detailExtrasParams = FrameLayout.LayoutParams(
            weightedWidth, getScreenHeight() - getStatusBarHeight()
        )

        includeDetailsExtras?.layoutParams = detailExtrasParams
    }

    private fun configureViewValues(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            showMore = savedInstanceState.getBoolean(SHOW_MORE_KEY)
        }
    }

    private fun setupEpisodeDetailsViewData() {
        episodeDetails?.let { details ->
            //set expanded values
            details.image?.let { binding.expandedPodcastImageView.loadNetworkImage(it) }
            binding.expandedTitle.text = details.podcast?.title
            binding.expandedEpisodeTitle.text = details.title
            binding.title.text = details.title

            if (showMore) {
                binding.description.showFullText(details.description)
                binding.showMore.text = getString(R.string.show_less)
            } else {
                val maxLines = resources.getInteger(R.integer.max_lines_details_description)
                binding.description.showLimitedText(maxLines, details.description)
                binding.showMore.text = getString(R.string.show_more)
            }

            //set collapsed values
            getMasterActivity().setCollapsedSheetValues(
                details.image,
                details.podcast?.title,
                details.title
            )
        }
    }

    private fun configureCommentsViewModel() {
        parentCommentsViewModel.retrievedComments!!.observe(viewLifecycleOwner, Observer {
            binding.commentsErrorAndLoading.loadingScreen.visibility = View.GONE
            commentsAdapter.submitList(it)
            commentsAdapter.notifyDataSetChanged()
        })
    }

    override fun onReplyButtonClick(commentId: String) {
        attemptToReplyComment(commentId)
    }

    override fun onRepliesButtonClick(comment: Comment) {
        val action =
            EpisodeFragmentDirections.actionNavigationEpisodeToNavigationReplies(
                comment.commentId
            )
        view?.findNavController()?.navigate(action)
    }

    override fun onUpVoteClick(comment: Comment) {
        parentCommentsViewModel.upVoteAndNotify(comment)
    }

    override fun onDownVoteClick(comment: Comment) {
        parentCommentsViewModel.downVoteAndNotify(comment)
    }

    override fun onDeleteClick(comment: Comment) {
        parentCommentsViewModel.deleteCommentAndNotify(comment)
    }

    override fun onEditClick(comment: Comment) {
        attemptToUpdateComment(comment)
    }

    //limit long / short description text
    private fun toggleFullOrLimitedDescription() {
        if (!showMore) {
            binding.description.showFullText(episodeDetails?.description)
            binding.showMore.text = getString(R.string.show_less)
        } else {
            val maxLines = resources.getInteger(R.integer.max_lines_details_description)
            binding.description.showLimitedText(maxLines, episodeDetails?.description)
            binding.showMore.text = getString(R.string.show_more)
        }
        showMore = !showMore
    }

    override fun onDetailsFetched(episode: Episode?, isRestoringEpisode: Boolean) {
        binding.detailsErrorAndLoading.loadingScreen.visibility = View.GONE

        episodeDetails = episode
        this.isRestoringEpisode = isRestoringEpisode
        episodeDetails?.let { details ->
            episodeId = details.episodeId
            podcastId = details.podcastId!!

            binding.addCommentButton.isEnabled = true

            parentCommentsViewModel.getComments(episodeId!!)
            configureCommentsViewModel()

            getMasterActivity().configureBottomSheetState()

            startPlaybackService()
            attemptServiceBind()

            episodeViewModel.fetchNextEpisodeAndNotify(episodeDetails!!)
            episodeViewModel.fetchPrevEpisodeAndNotify(episodeDetails!!)

            setupEpisodeDetailsViewData()
        }
    }

    private fun startPlaybackService() {
        val intent = Intent(context, PlaybackService::class.java)
        requireContext().startService(intent)
    }

    private fun attemptServiceBind() {
        val intent = Intent(context, PlaybackService::class.java)
        requireContext().bindService(intent, playbackServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onDetailsFetchFailed() {
        binding.detailsErrorAndLoading.loadingScreen.visibility = View.GONE
        Snackbar.make(
            requireView(),
            getString(R.string.failed_to_get_details),
            Snackbar.LENGTH_LONG
        ).show()
    }

    override fun onNextEpisodeFetched(episode: Episode) {
        nextEpisodeDetails = episode
        binding.nextEpisodeButton.isEnabled = true
    }

    override fun onNextEpisodeFetchFailed() {
        Snackbar.make(
            requireView(),
            getString(R.string.failed_to_load_next_episode),
            Snackbar.LENGTH_LONG
        ).show()
    }

    override fun onPreviousEpisodeFetched(episode: Episode) {
        prevEpisodeDetails = episode
        binding.previousEpisodeButton.isEnabled = true
    }

    override fun onPreviousEpisodeFetchFailed() {
        Snackbar.make(
            requireView(),
            getString(R.string.failed_to_load_previous_episode),
            Snackbar.LENGTH_LONG
        ).show()
    }

    override fun notifyProcessing() {
        binding.detailsErrorAndLoading.loadingScreen.visibility = View.VISIBLE
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
    }

    override fun onWipeComment() {
        // todo: method never called in episode fragment but onlu in replies fragment
        //  make replies view model not extend comments view model to prevent redundant code
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
