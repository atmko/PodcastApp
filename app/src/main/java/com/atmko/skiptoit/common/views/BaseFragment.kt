package com.atmko.skiptoit.common.views

import android.content.Intent
import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.annotation.UiThread
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.atmko.skiptoit.MasterActivity
import com.atmko.skiptoit.R
import com.atmko.skiptoit.SkipToItApplication
import com.atmko.skiptoit.about.AboutActivity
import com.atmko.skiptoit.dependencyinjection.application.ApplicationComponent
import com.atmko.skiptoit.dependencyinjection.presentation.AdapterModule
import com.atmko.skiptoit.dependencyinjection.presentation.PresentationComponent
import com.atmko.skiptoit.dependencyinjection.presentation.PresentationModule

private const val STATUS_BAR_IDENTIFIER: String = "status_bar_height"
private const val STATUS_BAR_IDENTIFIER_TYPE: String = "dimen"
private const val STATUS_BAR_IDENTIFIER_PACKAGE: String = "android"

open class BaseFragment : Fragment() {

    private var isInjected: Boolean = false

    @UiThread
    protected fun getPresentationComponent(): PresentationComponent {
        if (!isInjected) {
            isInjected = true
            return getApplicationComponent()
                .newPresentationComponent(
                    PresentationModule(),
                    AdapterModule(this, requireContext())
                )
        }

        throw RuntimeException("getPresentationComponent() called more than once")
    }

    private fun getApplicationComponent(): ApplicationComponent {
        return (requireActivity().application as SkipToItApplication).appComponent
    }

    fun configureToolbar(toolbar: Toolbar) {
        setHasOptionsMenu(true)
        val navController = findNavController()

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_subscriptions,
                R.id.navigation_search
            )
        )
        getMasterActivity().setSupportActionBar(toolbar)
        toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (getMasterActivity().user != null) {
            inflater.inflate(R.menu.signed_in_options_menu, menu)
        } else {
            inflater.inflate(R.menu.signed_out_options_menu, menu)
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.log_out -> {
                getMasterActivity().masterActivityViewModel.signOutAndNotify()
            }
            R.id.log_in -> {
                getMasterActivity().masterActivityViewModel.silentSignInAndNotify()
            }
            R.id.about -> {
                val aboutActivityIntent = Intent(requireContext().applicationContext, AboutActivity::class.java)
                startActivity(aboutActivityIntent)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    protected fun getMasterActivity(): MasterActivity {
        return requireActivity() as MasterActivity
    }

    fun getBaseFragmentBottomMargin(): Int {
        val navHeight = getMasterActivity().navBarHeight()
        val peekHeight = getMasterActivity().bottomSheetPeekHeight()

        return if (peekHeight > navHeight) {
            peekHeight
        } else {
            navHeight
        }
    }

    fun getScreenWidth(): Int {
        val displayMetrics: DisplayMetrics = Resources.getSystem().displayMetrics
        return displayMetrics.widthPixels
    }

    fun getScreenHeight(): Int {
        val displayMetrics: DisplayMetrics = Resources.getSystem().displayMetrics
        return displayMetrics.heightPixels
    }

    fun getExtrasHeight(): Int {
        return getScreenHeight() - (getStatusBarHeight() + getToolbarHeight() + getBaseFragmentBottomMargin())
    }

    fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId: Int =
            resources.getIdentifier(
                STATUS_BAR_IDENTIFIER,
                STATUS_BAR_IDENTIFIER_TYPE,
                STATUS_BAR_IDENTIFIER_PACKAGE
            )
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    private fun getToolbarHeight(): Int {
        requireView().findViewById<View>(R.id.toolbar)?.let {
            return requireView().findViewById<View>(R.id.toolbar).height
        }
        return 0
    }
}