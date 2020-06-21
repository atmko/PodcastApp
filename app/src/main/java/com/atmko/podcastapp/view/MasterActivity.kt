package com.atmko.podcastapp.view

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.atmko.podcastapp.R
import com.atmko.podcastapp.databinding.ActivityMasterBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior

private const val IS_BOTTOM_SHEET_EXPANDED = "is_bottom_sheet_expanded_key"

class MasterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMasterBinding

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

        navView.setupWithNavController(navController)

        configureValues(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        val isBottomSheetExpanded: Boolean = bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED
        outState.putBoolean(IS_BOTTOM_SHEET_EXPANDED, isBottomSheetExpanded)
    }

    private fun configureValues(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
            if (savedInstanceState.getBoolean(IS_BOTTOM_SHEET_EXPANDED)) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
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
                        resources.getDimensionPixelSize(R.dimen.bottom_sheet_peek_height)

                    val episodeFragment: EpisodeFragment = EpisodeFragment.newInstance(episodeId)
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.bottomSheet, episodeFragment)
                        .commit()

                    bottomSheetBehavior.removeBottomSheetCallback(this)
                }
            }
        })

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun getBinding() = binding
}
