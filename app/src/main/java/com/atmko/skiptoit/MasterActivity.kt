package com.atmko.skiptoit

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.atmko.skiptoit.common.ManagerViewModel
import com.atmko.skiptoit.common.ViewModelFactory
import com.atmko.skiptoit.common.views.BaseActivity
import com.atmko.skiptoit.databinding.ActivityMasterBinding
import com.atmko.skiptoit.details.DetailsFragment
import com.atmko.skiptoit.details.DetailsFragmentDirections
import com.atmko.skiptoit.episode.EpisodeFragment
import com.atmko.skiptoit.episode.EpisodeFragmentDirections
import com.atmko.skiptoit.episode.replies.RepliesFragment
import com.atmko.skiptoit.episode.replies.RepliesFragmentDirections
import com.atmko.skiptoit.launch.LaunchActivity
import com.atmko.skiptoit.model.User
import com.atmko.skiptoit.search.searchparent.SearchParentFragment
import com.atmko.skiptoit.search.searchparent.SearchParentFragmentDirections
import com.atmko.skiptoit.services.PlaybackService
import com.atmko.skiptoit.subcriptions.SubscriptionsFragment
import com.atmko.skiptoit.subcriptions.SubscriptionsFragmentDirections
import com.atmko.skiptoit.subcriptions.SubscriptionsViewModel
import com.atmko.skiptoit.utils.loadNetworkImage
import com.google.android.exoplayer2.Player
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

class MasterActivity : BaseActivity(), ManagerViewModel.Listener,
    MasterActivityViewModel.MasterListener {

    private lateinit var binding: ActivityMasterBinding

    private var mIsBound: Boolean = false
    private var mPlaybackService: PlaybackService? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    lateinit var masterActivityViewModel: MasterActivityViewModel
    lateinit var subscriptionsViewModel: SubscriptionsViewModel
    var user: User? = null

    private var navBarOriginalYPosition: Float? = null

    private val playbackListeners = ArrayList<PlayerListener>()

    private var mSavedInstanceState: Bundle? = null

    interface PlayerListener {
        fun onPlaybackStateChanged(isPlaying: Boolean)
    }

    fun registerPlaybackListener(playbackListener: PlayerListener) {
        playbackListeners.add(playbackListener)
        mPlaybackService?.let {
            mPlaybackService!!.player?.let {
                playbackListener.onPlaybackStateChanged(mPlaybackService!!.player!!.isPlaying)
            }
        }
    }

    fun unregisterPlaybackListener(playbackListener: PlayerListener) {
        playbackListeners.remove(playbackListener)
    }

    fun togglePlayPause() {
        mPlaybackService?.togglePlayPause()
    }

    private val playbackServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            mIsBound = false
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mIsBound = true
            mPlaybackService = (service as PlaybackService.PlaybackServiceBinder).getService()
            binding.collapsedBottomSheet.player = mPlaybackService!!.player
            mPlaybackService!!.player!!.addListener(object: Player.EventListener {
                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    super.onPlayerStateChanged(playWhenReady, playbackState)
                    for (listener in playbackListeners) {
                        listener.onPlaybackStateChanged(playWhenReady)
                    }
                }
            })
            binding.collapsedBottomSheet.showController()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        binding = ActivityMasterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mSavedInstanceState = savedInstanceState

        getPresentationComponent().inject(this)

        configureOrientationRestrictions()
        configureBaseBackButtonFunctionality()
        defineViewModel()
        configureViews()
        configureValues()
        launchEpisodeFragment()
        if (masterActivityViewModel.isFirstSetUp()) {
            openLaunchFragment()
            return
        }
    }

    override fun onStart() {
        super.onStart()
        masterActivityViewModel.registerListener(this)
        masterActivityViewModel.registerMasterListener(this)
        Intent(this, PlaybackService::class.java).also { intent ->
            startService(intent)
            bindService(intent, playbackServiceConnection, Context.BIND_AUTO_CREATE)
        }
        masterActivityViewModel.handleSavedStateAndNotify(mSavedInstanceState)

        masterActivityViewModel.getMatchingUserAndNotify()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.let { masterActivityViewModel.onRequestResultReceived(requestCode, resultCode, it) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        masterActivityViewModel.saveState(outState)
    }

    override fun onStop() {
        super.onStop()
        masterActivityViewModel.unregisterListener(this)
        masterActivityViewModel.unregisterMasterListener(this)
        if (mIsBound) {
            unbindService(playbackServiceConnection)
        }
        mIsBound = false
    }

    private fun openLaunchFragment() {
        val launchActivityIntent = Intent(applicationContext, LaunchActivity::class.java)
        startActivity(launchActivityIntent)
        finish()
    }

    private fun configureOrientationRestrictions() {
        if (resources.getBoolean(R.bool.isPhone)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    private fun configureBaseBackButtonFunctionality() {
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isBottomSheetExpanded()) {
                    val currentDestination: NavDestination? =
                        getEpisodeNavHostFragment().navController.currentDestination
                    if (currentDestination?.id == R.id.navigation_episode) {
                        masterActivityViewModel.collapseBottomSheetAndNotify()
                    } else {
                        getEpisodeNavHostFragment().navController.navigateUp()
                    }
                } else {
                    if (!getBaseNavHostFragment().navController.navigateUp()){
                        finish()
                    }
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun defineViewModel() {
        masterActivityViewModel = ViewModelProvider(this,
            viewModelFactory).get(MasterActivityViewModel::class.java)

        subscriptionsViewModel = ViewModelProvider(this,
            viewModelFactory).get(SubscriptionsViewModel::class.java)
    }

    private fun configureViews() {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE
        binding.errorAndLoading.errorScreen.visibility = View.GONE
        configureBottomSheet()
        configureAppBar()
    }

    private fun configureValues() {
        masterActivityViewModel = ViewModelProvider(this,
            viewModelFactory).get(MasterActivityViewModel::class.java)
        masterActivityViewModel.getMatchingUserAndNotify()

        subscriptionsViewModel = ViewModelProvider(this,
            viewModelFactory).get(SubscriptionsViewModel::class.java)
    }

    private fun launchEpisodeFragment() {
        val action = EpisodeFragmentDirections
            .actionNavigationEpisodeToNavigationEpisode()

        getEpisodeNavHostFragment().navController.navigate(action)
    }

    private fun configureBottomSheet() {
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                when (slideOffset) {
                    1f -> {
                        //hide collapsedBottomSheet and navView
                        binding.collapsedBottomSheet.visibility = View.GONE
                        binding.navView.visibility = View.GONE
                    }
                    0f -> {
                        //hide collapsedBottomSheet
                        binding.episodeFragmentFrameLayout.visibility = View.GONE
                    }
                    else -> {
                        //show collapsedBottomSheet and episodeFragmentFrameLayout
                        binding.collapsedBottomSheet.visibility = View.VISIBLE
                        binding.episodeFragmentFrameLayout.visibility = View.VISIBLE

                        //hide nav view
                        binding.navView.visibility = View.VISIBLE
                    }
                }

                //adjust collapsedBottomSheet alpha values
                //adjust episodeFragmentFrameLayout alpha values
                binding.collapsedBottomSheet.alpha = 1 - slideOffset
                binding.episodeFragmentFrameLayout.alpha = slideOffset

                //adjust navView alpha values
                //adjust navView y translation values
                binding.navView.apply {
                    alpha = 1 - slideOffset
                    if (navBarOriginalYPosition == null) {
                        navBarOriginalYPosition = y
                    }

                    navBarOriginalYPosition?.let {
                        y = it + (slideOffset / 1 * height)
                    }
                }
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {

            }
        })
    }

    private fun configureAppBar() {
        val navView: BottomNavigationView = binding.navView
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        navView.setupWithNavController(getBaseNavHostFragment().navController)


        navView.setOnNavigationItemReselectedListener {
            //perform no action on menu item reselected
        }
    }

    fun navBarHeight(): Int {
        return if (isNavViewShown()) {
            resources.getDimension(R.dimen.bottom_sheet_peek_height).toInt()
        } else {
            0
        }
    }

    private fun getBaseNavHostFragment(): NavHostFragment {
        return supportFragmentManager.findFragmentById(R.id.base_nav_host_fragment) as NavHostFragment
    }

    private fun getEpisodeNavHostFragment(): NavHostFragment {
        return supportFragmentManager.findFragmentById(R.id.episode_nav_host_fragment) as NavHostFragment
    }

    fun bottomSheetPeekHeight(): Int {
        return BottomSheetBehavior.from(binding.bottomSheet).peekHeight
    }

    fun isBottomSheetExpanded(): Boolean {
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        return bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED
    }

    private fun showBottomPanels() {
        binding.navView.visibility = View.VISIBLE
        binding.bottomSheet.visibility = View.VISIBLE
    }

    private fun isNavViewShown(): Boolean {
        return binding.navView.visibility == View.VISIBLE
    }

    private fun isBottomSheetVisible(): Boolean {
        return binding.bottomSheet.visibility == View.VISIBLE
    }

    fun loadEpisodeIntoCollapsedBottomSheet(podcastId: String, episodeId: String) {
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.peekHeight =
                        binding.navView.height + resources.getDimensionPixelSize(R.dimen.bottom_sheet_peek_height)

                    loadEpisodeIntoBottomSheet(podcastId, episodeId)

                    bottomSheetBehavior.removeBottomSheetCallback(this)
                }
            }
        })

        masterActivityViewModel.expandBottomSheetAndNotify()
    }

    fun loadEpisodeIntoBottomSheet(podcastId: String, episodeId: String) {
        when (getEpisodeNavHostFragment().navController.currentDestination?.id) {
            R.id.navigation_episode -> {
                getEpisodeNavHostFragment().navController.navigate(
                    EpisodeFragmentDirections
                        .actionNavigationEpisodeToNavigationEpisode(
                            podcastId, episodeId)
                )
            }
            R.id.navigation_replies -> {
                getEpisodeNavHostFragment().navController.navigate(
                    RepliesFragmentDirections
                        .actionNavigationRepliesToNavigationEpisode(
                            podcastId, episodeId)
                )
            }
        }
    }

    fun configureBottomSheetState() {
        if (!isBottomSheetExpanded()) {
            val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
            val hasPeekHeight: Boolean = bottomSheetBehavior.peekHeight > 0
            if (!hasPeekHeight || !isBottomSheetVisible() ) {
                binding.navView.post {
                    bottomSheetBehavior.peekHeight =
                        binding.navView.height + resources.getDimensionPixelSize(R.dimen.bottom_sheet_peek_height)
                }
            }
        }

        showBottomPanels()
    }

    fun setCollapsedSheetValues(image: String?, podcastTitle: String?, episodeTitle: String?) {
        image?.let {
            val collapsedPodcastImageView: ImageView =
                binding.collapsedBottomSheet.findViewById(R.id.collapsedPodcastImageView)
            collapsedPodcastImageView.loadNetworkImage(it)
        }

        val collapsedTitle: TextView =
            binding.collapsedBottomSheet.findViewById(R.id.collapsedTitle)
        collapsedTitle.text = podcastTitle
        val collapsedEpisodeTitle: TextView =
            binding.collapsedBottomSheet.findViewById(R.id.collapsedEpisodeTitle)
        collapsedEpisodeTitle.text = episodeTitle
    }

    //hide soft keyboard and update keyboard visibility property
    fun hideSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm: InputMethodManager =
                view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    //show soft keyboard and update keyboard visibility property
    fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm: InputMethodManager =
                view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun notifyProcessing() {
        binding.errorAndLoading.loadingScreen.visibility = View.VISIBLE
        binding.errorAndLoading.errorScreen.visibility = View.GONE
    }

    override fun onSilentSignInSuccess() {
        masterActivityViewModel.getMatchingUserAndNotify()
    }

    override fun onSilentSignInFailed(googleSignInIntent: Intent, googleSignInRequestCode: Int) {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE
        startActivityForResult(googleSignInIntent, googleSignInRequestCode)
    }

    override fun onSignInSuccess() {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE
        binding.errorAndLoading.errorScreen.visibility = View.GONE
        masterActivityViewModel.getMatchingUserAndNotify()
    }

    override fun onSignInFailed() {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE
        binding.errorAndLoading.errorScreen.visibility = View.VISIBLE
        Snackbar.make(binding.topLayout, getString(R.string.failed_to_sign_in), Snackbar.LENGTH_LONG).show()
    }

    override fun onUserFetchSuccess(user: User) {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE
        binding.errorAndLoading.errorScreen.visibility = View.GONE
        this.user = user

        reloadCurrentFragment()

        subscriptionsViewModel.checkSyncStatusAndNotify()
    }

    private fun reloadCurrentFragment() {
        val baseNavHostFragment: NavHostFragment? = getBaseNavHostFragment()
        val baseCurrentFragment = baseNavHostFragment!!.childFragmentManager.fragments[0]
        val baseNavController = baseNavHostFragment.navController

        when (baseCurrentFragment) {
            is SubscriptionsFragment -> {
                baseNavController.navigate(
                    SubscriptionsFragmentDirections
                        .actionNavigationSubscriptionsToNavigationSubscriptions()
                )
            }
            is SearchParentFragment -> {
                baseNavController.navigate(
                    SearchParentFragmentDirections
                        .actionNavigationSearchToNavigationSearch()
                )
            }
            is DetailsFragment -> {
                baseNavController.navigate(
                    DetailsFragmentDirections
                        .actionNavigationDetailsToNavigationDetails(baseCurrentFragment.podcast)
                )
            }
        }

        val episodeNavHostFragment: NavHostFragment? = getEpisodeNavHostFragment()
        val episodeCurrentFragment = episodeNavHostFragment!!.childFragmentManager.fragments[0]
        val episodeNavController = episodeNavHostFragment.navController

        if (episodeCurrentFragment is EpisodeFragment) {
            episodeNavController.navigate(
                EpisodeFragmentDirections
                    .actionNavigationEpisodeToNavigationEpisode(
                        episodeCurrentFragment.podcastId,
                        episodeCurrentFragment.episodeId
                    )
            )
        } else if (episodeCurrentFragment is RepliesFragment) {
            episodeNavController.navigate(
                RepliesFragmentDirections
                    .actionNavigationRepliesToNavigationReplies(episodeCurrentFragment.parentCommentId)
            )
        }
    }

    override fun onUserFetchFailed() {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE
        binding.errorAndLoading.errorScreen.visibility = View.VISIBLE
        Snackbar.make(binding.topLayout, getString(R.string.failed_to_retrieve_user), Snackbar.LENGTH_LONG).show()
    }

    override fun onSignOutSuccess() {
        val playbackService = Intent(this@MasterActivity, PlaybackService::class.java)
        stopService(playbackService)
        binding.errorAndLoading.loadingScreen.visibility = View.GONE
        binding.errorAndLoading.errorScreen.visibility = View.GONE
        openLaunchFragment()
    }

    override fun onSignOutFailed() {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE
        binding.errorAndLoading.errorScreen.visibility = View.VISIBLE
        Snackbar.make(binding.topLayout, getString(R.string.failed_to_sign_out), Snackbar.LENGTH_LONG).show()
    }

    override fun onShowBottomSheet() {
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        binding.navView.post {
            bottomSheetBehavior.peekHeight =
                binding.navView.height + resources.getDimensionPixelSize(R.dimen.bottom_sheet_peek_height)
        }
    }

    override fun onHideBottomSheet() {

    }

    override fun onExpandBottomSheet() {
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onCollapseBottomSheet() {
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }
}