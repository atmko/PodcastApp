package com.atmko.skiptoit.episode

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
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
import com.atmko.skiptoit.databinding.FragmentEpisodeBinding
import com.atmko.skiptoit.model.*
import com.atmko.skiptoit.services.PlaybackService
import com.atmko.skiptoit.view.adapters.CommentsAdapter
import com.atmko.skiptoit.view.common.BaseFragment
import com.atmko.skiptoit.viewmodel.EpisodeViewModel
import com.atmko.skiptoit.viewmodel.MasterActivityViewModel
import com.atmko.skiptoit.episode.common.CommentsViewModel
import com.atmko.skiptoit.util.loadNetworkImage
import com.atmko.skiptoit.util.showFullText
import com.atmko.skiptoit.util.showLimitedText
import com.atmko.skiptoit.view.MasterActivity
import com.atmko.skiptoit.viewmodel.common.BaseBoundaryCallback
import com.atmko.skiptoit.viewmodel.common.ViewModelFactory
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

const val EPISODE_FRAGMENT_KEY = "episode_fragment"

const val SCRUBBER_ANIM_LENGTH: Long = 100
const val SCRUBBER_HIDE_LENGTH: Long = 2000

private const val SHOW_MORE_KEY = "show_more"

class EpisodeFragment : BaseFragment(), CommentsAdapter.OnCommentItemClickListener,
    CommentsViewModel.Listener, BaseBoundaryCallback.Listener {

    private var _binding: FragmentEpisodeBinding? = null
    private val binding get() = _binding!!

    //fragment init variable
    private lateinit var podcastId: String
    private lateinit var episodeId: String
    private var isRestoringEpisode: Boolean = false

    private var mIsBound: Boolean = false
    private var mPlaybackService: PlaybackService? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var episodeViewModel: EpisodeViewModel
    private var episodeDetails: Episode? = null
    private var nextEpisodeDetails: Episode? = null
    private var prevEpisodeDetails: Episode? = null

    private lateinit var parentCommentsViewModel: ParentCommentsViewModel

    private lateinit var masterActivityViewModel: MasterActivityViewModel
    private var user: User? = null

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
        isRestoringEpisode = args.isRestoringEpisode
    }

    override fun onResume() {
        super.onResume()
        parentCommentsViewModel.registerListener(this)
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

        if (podcastId != "" && episodeId != "") {
            configureViews()
            configureValues(savedInstanceState)
            configureEpisodeViewModel()
            configureMasterActivityViewModel()
            configureCommentsViewModel()
        }
    }

    override fun onStart() {
        super.onStart()
        context?.let {
            Intent(context, PlaybackService::class.java).also { intent ->
                it.startService(intent)
                it.bindService(intent, playbackServiceConnection, Context.BIND_AUTO_CREATE)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        parentCommentsViewModel.unregisterListener(this)
    }

    override fun onStop() {
        super.onStop()
        if (mIsBound) {
            context?.unbindService(playbackServiceConnection)
        }
        mIsBound = false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean(SHOW_MORE_KEY, showMore)
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
            mPlaybackService = (service as PlaybackService.PlaybackServiceBinder).getService()
            episodeDetails?.let {
                mPlaybackService!!.prepareMediaForPlayback(Uri.parse(it.audio))
            }
            binding.playPanel.player = mPlaybackService?.player
            binding.playPanel.showController()
        }
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

        binding.nextEpisodeButton.apply {
            isEnabled = false
            setOnClickListener {
                (activity as MasterActivity).loadEpisodeIntoBottomSheet(
                    podcastId,
                    nextEpisodeDetails!!.episodeId
                )
            }
        }

        binding.previousEpisodeButton.apply {
            isEnabled = false
            setOnClickListener {
                (activity as MasterActivity).loadEpisodeIntoBottomSheet(
                    podcastId,
                    prevEpisodeDetails!!.episodeId
                )
            }
        }

        binding.showMore.setOnClickListener {
            toggleFullOrLimitedDescription()
        }

        binding.addCommentButton.apply {
            setOnClickListener {
                attemptToCreateComment()
            }
        }

        binding.resultsFrameLayout.resultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = commentsAdapter
        }
    }

    private fun attemptToCreateComment() {
        if (user != null) {
            navigateToCreateComment(user!!.username!!)
        } else {
            masterActivityViewModel.signIn()
        }
    }

    private fun attemptToUpdateComment(comment: Comment) {
        if (user != null) {
            navigateToUpdateComment(comment, user!!.username!!)
        } else {
            masterActivityViewModel.signIn()
        }
    }

    private fun attemptToReplyComment(parentId: String, quotedText: String) {
        if (user != null) {
            navigateToReplyComment(user!!.username!!, parentId, quotedText)
        } else {
            masterActivityViewModel.signIn()
        }
    }

    private fun navigateToCreateComment(username: String) {
        val action =
            EpisodeFragmentDirections.actionNavigationEpisodeToNavigationCrateComment(
                podcastId, episodeId, username
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

    private fun navigateToReplyComment(username: String, parentId: String, quotedText: String) {
        val action =
            EpisodeFragmentDirections.actionNavigationEpisodeToNavigationCreateReply(
                parentId, quotedText, username
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

    private fun configureValues(savedInstanceState: Bundle?) {
        episodeViewModel = ViewModelProvider(
            this,
            viewModelFactory
        ).get(EpisodeViewModel::class.java)

        episodeViewModel.clearPodcastCache(podcastId)

        if (isRestoringEpisode) {
            episodeViewModel.restoreEpisode()
        } else {
            episodeViewModel.refresh(episodeId)
        }

        parentCommentsViewModel = ViewModelProvider(
            this,
            viewModelFactory
        ).get(ParentCommentsViewModel::class.java)

        parentCommentsViewModel.getComments(episodeId)

        masterActivityViewModel = ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(MasterActivityViewModel::class.java)

        masterActivityViewModel.getUser()

        if (savedInstanceState != null) {
            showMore = savedInstanceState.getBoolean(SHOW_MORE_KEY)
        }
    }

    private fun configureEpisodeViewModel() {
        episodeViewModel.episodeDetails.observe(viewLifecycleOwner, Observer { episodeDetails ->
            this.episodeDetails = episodeDetails
            episodeDetails?.let { details ->

                observeNextAndPrevEpisodes()

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
                activity?.let {
                    (activity as MasterActivity)
                        .setCollapsedSheetValues(
                            details.image,
                            details.podcast?.title,
                            details.title
                        )
                }

                context?.let {
                    mPlaybackService?.prepareMediaForPlayback(Uri.parse(episodeDetails.audio))

                    if (!isRestoringEpisode) {
                        mPlaybackService?.play()
                        val sharedPrefs = activity?.getSharedPreferences(
                            EPISODE_FRAGMENT_KEY,
                            Context.MODE_PRIVATE
                        )
                        sharedPrefs?.let {
                            sharedPrefs.edit()
                                .putString(PODCAST_ID_KEY, details.podcast?.id)
                                .putString(EPISODE_ID_KEY, details.episodeId)
                                .putString(EPISODE_TITLE_KEY, details.title)
                                .putString(EPISODE_DESCRIPTION_KEY, details.description)
                                .putString(EPISODE_IMAGE_KEY, details.image)
                                .putString(EPISODE_AUDIO_KEY, details.audio)
                                .putLong(EPISODE_PUBLISH_DATE_KEY, details.publishDate)
                                .putInt(EPISODE_LENGTH_IN_SECONDS_KEY, details.lengthInSeconds)
                                .putString(PODCAST_TITLE_KEY, details.podcast?.title)
                                .commit()
                        }
                    }
                }
            }
        })

        episodeViewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            isLoading?.let {
                binding.errorAndLoading.loadingScreen.visibility =
                    if (it) View.VISIBLE else View.GONE
                if (it) {
                    binding.errorAndLoading.errorScreen.visibility = View.GONE
                }
            }
        })

        episodeViewModel.loadError.observe(viewLifecycleOwner, Observer { isError ->
            isError.let {
                binding.errorAndLoading.errorScreen.visibility =
                    if (it) View.VISIBLE else View.GONE
            }
        })
    }

    private fun observeNextAndPrevEpisodes() {
        episodeViewModel.fetchNextEpisode(podcastId, episodeDetails!!)
        episodeViewModel.nextEpisode!!.observe(viewLifecycleOwner, Observer {
            nextEpisodeDetails = it
            binding.nextEpisodeButton.isEnabled = it != null
        })

        episodeViewModel.fetchPrevEpisode(episodeDetails!!)
        episodeViewModel.prevEpisode!!.observe(viewLifecycleOwner, Observer {
            prevEpisodeDetails = it
            binding.previousEpisodeButton.isEnabled = it != null
        })
    }

    private fun configureCommentsViewModel() {
        parentCommentsViewModel.retrievedComments!!.observe(viewLifecycleOwner, Observer {
            binding.resultsFrameLayout.errorAndLoading.loadingScreen.visibility = View.GONE
            commentsAdapter.submitList(it)
            commentsAdapter.notifyDataSetChanged()
        })
    }

    private fun configureMasterActivityViewModel() {
        masterActivityViewModel.currentUser.observe(viewLifecycleOwner, Observer {
            user = it
        })
    }

    override fun onReplyButtonClick(commentId: String, quotedText: String) {
        attemptToReplyComment(commentId, quotedText)
    }

    override fun onRepliesButtonClick(comment: Comment) {
        val action =
            EpisodeFragmentDirections.actionNavigationEpisodeToNavigationReplies(
                comment
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

    override fun notifyProcessing() {
        binding.pageLoading.visibility = View.VISIBLE
        binding.resultsFrameLayout.errorAndLoading.errorScreen.visibility = View.GONE
    }

    override fun onVoteUpdate() {
        binding.pageLoading.visibility = View.INVISIBLE
        binding.resultsFrameLayout.errorAndLoading.errorScreen.visibility = View.GONE
    }

    override fun onVoteUpdateFailed() {
        binding.pageLoading.visibility = View.INVISIBLE
        Snackbar.make(requireView(), "Vote Update Failed", Snackbar.LENGTH_LONG).show()
    }

    override fun onDeleteComment() {
        binding.pageLoading.visibility = View.INVISIBLE
        binding.resultsFrameLayout.errorAndLoading.errorScreen.visibility = View.GONE
    }

    override fun onDeleteCommentFailed() {
        binding.pageLoading.visibility = View.INVISIBLE
        Snackbar.make(requireView(), "Failed to delete comment", Snackbar.LENGTH_LONG).show()
    }

    override fun onPageLoading() {
        binding.pageLoading.visibility = View.VISIBLE
        binding.resultsFrameLayout.errorAndLoading.errorScreen.visibility = View.GONE
    }

    override fun onPageLoad() {
        binding.pageLoading.visibility = View.INVISIBLE
        binding.resultsFrameLayout.errorAndLoading.errorScreen.visibility = View.GONE
    }

    override fun onPageLoadFailed() {
        binding.pageLoading.visibility = View.INVISIBLE
        Snackbar.make(requireView(), "Failed to load page", Snackbar.LENGTH_LONG).show()
    }
}
