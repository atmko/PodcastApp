package com.atmko.skiptoit.view

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.atmko.skiptoit.R
import com.atmko.skiptoit.databinding.ActivityMasterBinding
import com.atmko.skiptoit.model.EPISODE_ID_KEY
import com.atmko.skiptoit.model.PODCAST_ID_KEY
import com.atmko.skiptoit.model.User
import com.atmko.skiptoit.services.PlaybackService
import com.atmko.skiptoit.util.loadNetworkImage
import com.atmko.skiptoit.viewmodel.MasterActivityViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior

private const val IS_BOTTOM_SHEET_EXPANDED_KEY = "is_bottom_sheet_expanded"
private const val IS_BOTTOM_SHEET_SHOWN_KEY = "is_bottom_sheet_shown"

class MasterActivity : AppCompatActivity(), MasterActivityViewModel.ViewNavigation {
    private lateinit var binding: ActivityMasterBinding

    private var mIsBound: Boolean = false
    private var mPlaybackService: PlaybackService? = null

    private var viewModel: MasterActivityViewModel? = null
    var user: User? = null

    private var navBarOriginalYPosition: Float? = null

    private val playbackServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            mIsBound = false
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mIsBound = true
            mPlaybackService = (service as PlaybackService.PlaybackServiceBinder).getService()
            binding.collapsedBottomSheet.player = mPlaybackService?.player
            binding.collapsedBottomSheet.showController()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMasterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configureViews()
        configureValues(savedInstanceState)
        configureViewModel()
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
        data?.let { viewModel?.onRequestResultReceived(requestCode, resultCode, it) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)

        val isBottomSheetShown: Boolean = bottomSheetBehavior.peekHeight > 0
        outState.putBoolean(IS_BOTTOM_SHEET_SHOWN_KEY, isBottomSheetShown)

        val isBottomSheetExpanded: Boolean =
            bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED
        outState.putBoolean(IS_BOTTOM_SHEET_EXPANDED_KEY, isBottomSheetExpanded)
    }

    override fun onStop() {
        super.onStop()
        if (mIsBound) {
            unbindService(playbackServiceConnection)
        }
        mIsBound = false
    }

    private fun configureViews() {
        configureBottomSheet()
        configureAppBar()
    }

    private fun configureValues(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)

            if (savedInstanceState.getBoolean(IS_BOTTOM_SHEET_SHOWN_KEY)) {
                binding.navView.post {
                    bottomSheetBehavior.peekHeight =
                        binding.navView.height + resources.getDimensionPixelSize(R.dimen.bottom_sheet_peek_height)
                }
            }

            if (savedInstanceState.getBoolean(IS_BOTTOM_SHEET_EXPANDED_KEY)) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                binding.collapsedBottomSheet.visibility = View.GONE
                binding.collapsedBottomSheet.alpha = 0f
                binding.episodeFragmentFrameLayout.alpha = 1f
            } else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                binding.collapsedBottomSheet.visibility = View.VISIBLE
                binding.collapsedBottomSheet.alpha = 1f
                binding.episodeFragmentFrameLayout.alpha = 0f
            }
        } else {
            if (viewModel == null) {
                viewModel = ViewModelProviders.of(this).get(MasterActivityViewModel::class.java)
            }

            val episodePrefs = getSharedPreferences(EPISODE_FRAGMENT_KEY, Context.MODE_PRIVATE)
            episodePrefs?.let {
                val podcastId = episodePrefs.getString(PODCAST_ID_KEY, "")
                val episodeId = episodePrefs.getString(EPISODE_ID_KEY, "")
                if (episodeId != null && podcastId !== null) {
                    restoreEpisodeIntoBottomSheet(podcastId, episodeId)
                }
            }
        }
    }

    private fun configureViewModel() {
        viewModel?.messageEvent?.setEventReceiver(this, this)
        viewModel?.currentUser?.observe(this, Observer {
            user = it
        })
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
        AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_subscriptions,
                R.id.navigation_search
            )
        )
        navView.setupWithNavController(navController)
    }

    fun isSignedIn(): Boolean {
        viewModel?.isSignedIn()?.let {
            return it
        }
        return false
    }

    fun signIn() {
        viewModel?.signIn(this)
    }

    fun signOut() {
        viewModel?.signOut(this)
    }

    fun loadEpisodeIntoBottomSheet(podcastId: String, episodeId: String) {
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.peekHeight =
                        binding.navView.height + resources.getDimensionPixelSize(R.dimen.bottom_sheet_peek_height)

                    val action = EpisodeFragmentDirections
                        .actionNavigationEpisodeToNavigationEpisode(
                            podcastId, episodeId, false)

                    findNavController(R.id.episode_nav_host_fragment).navigate(action)

                    bottomSheetBehavior.removeBottomSheetCallback(this)
                }
            }
        })

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
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

    override fun onBackPressed() {
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            val currentDestination: NavDestination? =
                findNavController(R.id.episode_nav_host_fragment).currentDestination
            if (currentDestination?.id == R.id.navigation_episode) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            } else {
                findNavController(R.id.episode_nav_host_fragment).navigateUp()
            }
        } else {
            findNavController(R.id.base_nav_host_fragment).navigateUp()
        }
    }
}
