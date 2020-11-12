package com.atmko.skiptoit

import android.os.Bundle
import android.util.Log
import com.atmko.skiptoit.common.ManagerViewModel
import com.atmko.skiptoit.model.database.EpisodesCache

class MasterActivityViewModel(
    loginManager: LoginManager,
    userEndpoint: UserEndpoint,
    episodesCache: EpisodesCache
) : ManagerViewModel(
    loginManager,
    userEndpoint,
    episodesCache
) {

    interface MasterListener {
        fun onShowBottomSheet()
        fun onHideBottomSheet()
        fun onExpandBottomSheet()
        fun onCollapseBottomSheet()
    }

    companion object {
        const val IS_BOTTOM_SHEET_EXPANDED_KEY = "is_bottom_sheet_expanded"
        const val IS_BOTTOM_SHEET_SHOWN_KEY = "is_bottom_sheet_shown"
    }

    private val masterListeners = mutableListOf<MasterListener>()

    var isBottomSheetShown = false
    var isBottomSheetExpanded = false

    // called every time bottom sheet is being dragged
    fun handleBottomSheetDrag(slideOffset: Float) {
        isBottomSheetExpanded = slideOffset == 1f
        isBottomSheetShown = true
    }

    fun handleSavedStateAndNotify(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            isBottomSheetShown = savedInstanceState.getBoolean(IS_BOTTOM_SHEET_SHOWN_KEY)
            isBottomSheetExpanded = savedInstanceState.getBoolean(IS_BOTTOM_SHEET_EXPANDED_KEY)
        }

        if (isBottomSheetShown) {
            notifyShowBottomSheet()
        } else {
            notifyHideBottomSheet()
        }

        if (isBottomSheetExpanded) {
            notifyExpandBottomSheet()
        } else {
            notifyCollapseBottomSheet()
        }
    }

    //todo: not tested
    fun saveState(outState: Bundle) {
        outState.putBoolean(IS_BOTTOM_SHEET_EXPANDED_KEY, isBottomSheetExpanded)
        outState.putBoolean(IS_BOTTOM_SHEET_SHOWN_KEY, isBottomSheetShown)
    }

    fun expandBottomSheetAndNotify() {
        notifyExpandBottomSheet()
        isBottomSheetExpanded = true
        isBottomSheetShown = true
    }

    fun collapseBottomSheetAndNotify() {
        notifyCollapseBottomSheet()
        isBottomSheetExpanded = false
        isBottomSheetShown = true
    }

    private fun notifyShowBottomSheet() {
        for (listener in masterListeners) {
            listener.onShowBottomSheet()
        }
    }

    private fun notifyHideBottomSheet() {
        for (listener in masterListeners) {
            listener.onHideBottomSheet()
        }
    }

    private fun notifyExpandBottomSheet() {
        for (listener in masterListeners) {
            listener.onExpandBottomSheet()
        }
    }

    private fun notifyCollapseBottomSheet () {
        for (listener in masterListeners) {
            listener.onCollapseBottomSheet()
        }
    }

    fun registerMasterListener(listener: MasterListener) {
        masterListeners.add(listener)
    }

    fun unregisterMasterListener(listener: MasterListener) {
        masterListeners.remove(listener)
    }

    private fun unregisterMasterListeners() {
        for (listener in masterListeners) {
            unregisterMasterListener(listener)
        }
    }

    private fun unregisterListeners() {
        for (listener in listeners) {
            unregisterListener(listener)
        }
    }

    override fun onCleared() {
        Log.d("CLEARING", "CLEARING")
        unregisterListeners()
        unregisterMasterListeners()
    }
}