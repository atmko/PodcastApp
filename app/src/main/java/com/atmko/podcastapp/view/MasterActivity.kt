package com.atmko.podcastapp.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.atmko.podcastapp.R
import com.atmko.podcastapp.databinding.ActivityMasterBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior

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
    }

    fun expandBottomSheet() {
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior.peekHeight = resources.getDimensionPixelSize(R.dimen.bottom_sheet_peek_height)
    }

    fun loadEpisodeIntoBottomSheet(episodeId: String) {
        expandBottomSheet()
        val episodeFragment: EpisodeFragment = EpisodeFragment.newInstance(episodeId)
        supportFragmentManager.beginTransaction()
            .replace(R.id.bottomSheet, episodeFragment)
            .commit()
    }
}
