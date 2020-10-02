package com.atmko.skiptoit.view.common

import androidx.annotation.UiThread
import com.atmko.skiptoit.SkipToItApplication
import com.atmko.skiptoit.dependencyinjection.application.ApplicationComponent
import com.atmko.skiptoit.dependencyinjection.presentation.AdapterModule
import com.atmko.skiptoit.dependencyinjection.presentation.PagingModule
import com.atmko.skiptoit.dependencyinjection.presentation.PresentationComponent
import com.atmko.skiptoit.dependencyinjection.presentation.PresentationModule
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

open class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var isInjected: Boolean = false

    @UiThread
    protected fun getPresentationComponent(): PresentationComponent {
        if (!isInjected) {
            isInjected = true
            return getApplicationComponent()
                .newPresentationComponent(
                    PresentationModule(), AdapterModule(this)
                )
        }

        throw RuntimeException("getPresentationComponent() called more than once")
    }

    private fun getApplicationComponent(): ApplicationComponent {
        return (requireActivity().application as SkipToItApplication).appComponent
    }
}