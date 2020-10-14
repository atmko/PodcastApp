package com.atmko.skiptoit.common.views

import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.View
import androidx.annotation.UiThread
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.atmko.skiptoit.R
import com.atmko.skiptoit.SkipToItApplication
import com.atmko.skiptoit.dependencyinjection.application.ApplicationComponent
import com.atmko.skiptoit.dependencyinjection.presentation.AdapterModule
import com.atmko.skiptoit.dependencyinjection.presentation.PresentationComponent
import com.atmko.skiptoit.dependencyinjection.presentation.PresentationModule
import com.atmko.skiptoit.MasterActivity

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
                    AdapterModule(this)
                )
        }

        throw RuntimeException("getPresentationComponent() called more than once")
    }

    private fun getApplicationComponent(): ApplicationComponent {
        return (requireActivity().application as SkipToItApplication).appComponent
    }

    fun configureToolbar(toolbar: Toolbar) {
        val navController = findNavController()

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_subscriptions,
                R.id.navigation_search
            )
        )
        toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    fun getBaseFragmentBottomMargin(): Int {
        val masterActivity = (activity as MasterActivity)
        return masterActivity.bottomSheetPeekHeight() + masterActivity.navBarHeight()
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