package com.atmko.skiptoit

import android.content.*
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.IBinder
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.atmko.skiptoit.common.ViewModelFactory
import com.atmko.skiptoit.common.views.BaseActivity
import com.atmko.skiptoit.common.ManagerViewModel
import com.atmko.skiptoit.databinding.ActivityMasterBinding
import com.atmko.skiptoit.episode.EPISODE_FRAGMENT_KEY
import com.atmko.skiptoit.episode.EpisodeFragmentDirections
import com.atmko.skiptoit.episode.replies.RepliesFragmentDirections
import com.atmko.skiptoit.launch.LaunchActivity
import com.atmko.skiptoit.model.EPISODE_ID_KEY
import com.atmko.skiptoit.model.PODCAST_ID_KEY
import com.atmko.skiptoit.model.User
import com.atmko.skiptoit.services.PlaybackService
import com.atmko.skiptoit.utils.loadNetworkImage
import com.google.android.exoplayer2.Player
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

private const val IS_BOTTOM_SHEET_EXPANDED_KEY = "is_bottom_sheet_expanded"
private const val IS_BOTTOM_SHEET_SHOWN_KEY = "is_bottom_sheet_shown"

class MasterActivity : BaseActivity(), ManagerViewModel.Listener {

    private lateinit var binding: ActivityMasterBinding

    private var mIsBound: Boolean = false
    private var mPlaybackService: PlaybackService? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    lateinit var viewModel: MasterActivityViewModel
    var user: User? = null

    private var navBarOriginalYPosition: Float? = null

    private val playbackListeners = ArrayList<PlayerListener>()

    interface PlayerListener {
        fun onPlaybackStateChanged(isPlaying: Boolean)
    }

    fun registerPlaybackListener(playbackListener: PlayerListener) {
        playbackListeners.add(playbackListener)
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
        super.onCreate(savedInstanceState)
        binding = ActivityMasterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getPresentationComponent().inject(this)

        configureOrientationRestrictions()
        configureBaseNavigationChangedListener()
        configureBaseBackButtonFunctionality()
        configureViews()
        configureValues(savedInstanceState)
        if (viewModel.isFirstSetUp()) {
            openLaunchFragment()
            return
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.registerListener(this)
    }

    override fun onStart() {
        super.onStart()
        Intent(this, PlaybackService::class.java).also { intent ->
            startService(intent)
            bindService(intent, playbackServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.let { viewModel.onRequestResultReceived(requestCode, resultCode, it) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)

        val isBottomSheetShown: Boolean = bottomSheetBehavior.peekHeight > 0
        outState.putBoolean(IS_BOTTOM_SHEET_SHOWN_KEY, isBottomSheetShown)

        outState.putBoolean(IS_BOTTOM_SHEET_EXPANDED_KEY, isBottomSheetExpanded())
    }

    override fun onPause() {
        super.onPause()
        viewModel.unregisterListener(this)
    }

    override fun onStop() {
        super.onStop()
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

    private fun configureBaseNavigationChangedListener() {
        findNavController(R.id.base_nav_host_fragment)
            .addOnDestinationChangedListener{navController , destination, arguments ->
                if (!isBottomPanelsShown()){
                    showBottomPanels()
                }
            }
    }

    private fun configureBaseBackButtonFunctionality() {
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isBottomSheetExpanded()) {
                    val currentDestination: NavDestination? =
                        findNavController(R.id.episode_nav_host_fragment).currentDestination
                    if (currentDestination?.id == R.id.navigation_episode) {
                        collapseBottomSheet()
                    } else {
                        findNavController(R.id.episode_nav_host_fragment).navigateUp()
                    }
                } else {
                    if (!findNavController(R.id.base_nav_host_fragment).navigateUp()){
                        finish()
                    }
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun configureViews() {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE
        binding.errorAndLoading.errorScreen.visibility = View.GONE
        configureBottomSheet()
        configureAppBar()
    }

    private fun configureValues(savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this,
            viewModelFactory).get(MasterActivityViewModel::class.java)
        viewModel.getMatchingUserAndNotify()

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean(IS_BOTTOM_SHEET_SHOWN_KEY)) {
                val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
                binding.navView.post {
                    bottomSheetBehavior.peekHeight =
                        binding.navView.height + resources.getDimensionPixelSize(R.dimen.bottom_sheet_peek_height)
                }
            }

            if (savedInstanceState.getBoolean(IS_BOTTOM_SHEET_EXPANDED_KEY)) {
                expandBottomSheet()
                binding.collapsedBottomSheet.visibility = View.GONE
                binding.collapsedBottomSheet.alpha = 0f
                binding.episodeFragmentFrameLayout.alpha = 1f
            } else {
                collapseBottomSheet()
                binding.collapsedBottomSheet.visibility = View.VISIBLE
                binding.collapsedBottomSheet.alpha = 1f
                binding.episodeFragmentFrameLayout.alpha = 0f
            }
        } else {
            val episodePrefs = getSharedPreferences(EPISODE_FRAGMENT_KEY, Context.MODE_PRIVATE)
            episodePrefs?.let {
                val podcastId = episodePrefs.getString(PODCAST_ID_KEY, null)
                val episodeId = episodePrefs.getString(EPISODE_ID_KEY, null)
                if (episodeId != null && podcastId !== null) {
                    restoreEpisodeIntoBottomSheet(podcastId, episodeId)
                }
            }
        }
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

        val navController = findNavController(R.id.base_nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        navView.setupWithNavController(navController)

        navView.setOnNavigationItemReselectedListener(
            object : BottomNavigationView.OnNavigationItemReselectedListener {
                override fun onNavigationItemReselected(item: MenuItem) {
                    if (navView.selectedItemId != item.itemId) {
                        onNavigationItemReselected(item)
                    }
                }
            })
    }

    fun navBarHeight(): Int {
        return if (isNavViewShown()) {
            resources.getDimension(R.dimen.bottom_sheet_peek_height).toInt()
        } else {
            0
        }
    }

    fun bottomSheetPeekHeight(): Int {
        return if (isBottomSheetShown()) {
            resources.getDimension(R.dimen.bottom_sheet_peek_height).toInt()
        } else {
            0
        }
    }

    fun isBottomSheetExpanded(): Boolean {
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        return bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED
    }

    private fun isBottomPanelsShown(): Boolean {
        return isNavViewShown() || isBottomSheetShown()
    }

    private fun showBottomPanels() {
        binding.navView.visibility = View.VISIBLE
        binding.bottomSheet.visibility = View.VISIBLE
    }

    private fun isNavViewShown(): Boolean {
        return binding.navView.visibility == View.VISIBLE
    }

    private fun isBottomSheetShown(): Boolean {
        return binding.bottomSheet.visibility == View.VISIBLE
    }

    private fun hideBottomPanels() {
        binding.navView.visibility = View.GONE
        binding.bottomSheet.visibility = View.GONE
    }

    private fun expandBottomSheet() {
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun collapseBottomSheet() {
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
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

        expandBottomSheet()
    }

    fun loadEpisodeIntoBottomSheet(podcastId: String, episodeId: String) {
        when (findNavController(R.id.episode_nav_host_fragment).currentDestination?.id) {
            R.id.navigation_episode -> {
                findNavController(R.id.episode_nav_host_fragment).navigate(
                    EpisodeFragmentDirections
                        .actionNavigationEpisodeToNavigationEpisode(
                            podcastId, episodeId, false)
                )
            }
            R.id.navigation_replies -> {
                findNavController(R.id.episode_nav_host_fragment).navigate(
                    RepliesFragmentDirections
                        .actionNavigationRepliesToNavigationEpisode(
                            podcastId, episodeId, false)
                )
            }
        }
    }

    private fun restoreEpisodeIntoBottomSheet(podcastId: String, episodeId: String) {
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        binding.navView.post {
            bottomSheetBehavior.peekHeight =
                binding.navView.height + resources.getDimensionPixelSize(R.dimen.bottom_sheet_peek_height)
        }

        val action = EpisodeFragmentDirections
            .actionNavigationEpisodeToNavigationEpisode(
                podcastId, episodeId, true)

        findNavController(R.id.episode_nav_host_fragment).navigate(action)
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
            imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS);
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
        viewModel.getMatchingUserAndNotify()
    }

    override fun onSilentSignInFailed(googleSignInIntent: Intent, googleSignInRequestCode: Int) {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE
        startActivityForResult(googleSignInIntent, googleSignInRequestCode)
    }

    override fun onSignInSuccess() {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE
        binding.errorAndLoading.errorScreen.visibility = View.GONE
        viewModel.getMatchingUserAndNotify()
    }

    override fun onSignInFailed() {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE
        binding.errorAndLoading.errorScreen.visibility = View.VISIBLE
        Snackbar.make(binding.topLayout, "Failed to sign in", Snackbar.LENGTH_LONG).show()
    }

    override fun onUserFetchSuccess(user: User) {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE
        binding.errorAndLoading.errorScreen.visibility = View.GONE
        this.user = user
    }

    override fun onUserFetchFailed() {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE
        binding.errorAndLoading.errorScreen.visibility = View.VISIBLE
        Snackbar.make(binding.topLayout, "Failed to retrieve user", Snackbar.LENGTH_LONG).show()
    }

    override fun onSignOutSuccess() {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE
        binding.errorAndLoading.errorScreen.visibility = View.GONE
        openLaunchFragment()
    }

    override fun onSignOutFailed() {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE
        binding.errorAndLoading.errorScreen.visibility = View.VISIBLE
        Snackbar.make(binding.topLayout, "Failed to sign out", Snackbar.LENGTH_LONG).show()
    }
}