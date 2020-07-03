package com.atmko.podcastapp.view

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
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.atmko.podcastapp.R
import com.atmko.podcastapp.databinding.ActivityMasterBinding
import com.atmko.podcastapp.model.EPISODE_ID_KEY
import com.atmko.podcastapp.services.PlaybackService
import com.atmko.podcastapp.util.loadNetworkImage
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior

private const val IS_BOTTOM_SHEET_EXPANDED_KEY = "is_bottom_sheet_expanded"
private const val IS_BOTTOM_SHEET_SHOWN_KEY = "is_bottom_sheet_shown"

class MasterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMasterBinding

    private var mIsBound: Boolean = false
    private var mPlaybackService: PlaybackService? = null

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

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_subscriptions,
                R.id.navigation_search
            )
        )

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

        navView.setupWithNavController(navController)

        configureValues(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        Intent(this, PlaybackService::class.java).also { intent ->
            startService(intent)
            bindService(intent, playbackServiceConnection, Context.BIND_AUTO_CREATE)
        }
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
            val episodePrefs = getSharedPreferences(EPISODE_FRAGMENT_KEY, Context.MODE_PRIVATE)
            episodePrefs?.let {
                val episodeId = episodePrefs.getString(EPISODE_ID_KEY, "")
                episodeId?.let { restoreEpisodeIntoBottomSheet(episodeId) }
            }
        }
    }

    fun loadEpisodeIntoBottomSheet(episodeId: String) {
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.peekHeight =
                        binding.navView.height + resources.getDimensionPixelSize(R.dimen.bottom_sheet_peek_height)

                    val episodeFragment: EpisodeFragment =
                        EpisodeFragment.newInstance(episodeId, false)
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.episodeFragmentFrameLayout, episodeFragment)
                        .commit()

                    bottomSheetBehavior.removeBottomSheetCallback(this)
                }
            }
        })

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun restoreEpisodeIntoBottomSheet(episodeId: String) {
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        binding.navView.post {
            bottomSheetBehavior.peekHeight =
                binding.navView.height + resources.getDimensionPixelSize(R.dimen.bottom_sheet_peek_height)
        }

        val episodeFragment: EpisodeFragment = EpisodeFragment.newInstance(episodeId, true)
        supportFragmentManager.beginTransaction()
            .replace(R.id.episodeFragmentFrameLayout, episodeFragment)
            .commit()
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
}
