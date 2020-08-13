package com.atmko.skiptoit.viewmodel

import android.content.Intent
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.atmko.skiptoit.view.IS_FIRST_SETUP_KEY
import io.reactivex.disposables.CompositeDisposable

class LaunchFragmentViewModel(
    private val sharedPreferences: SharedPreferences
): ViewModel() {

    val loadError: MutableLiveData<Boolean> = MutableLiveData()
    val loading: MutableLiveData<Boolean> = MutableLiveData()

    private val disposable: CompositeDisposable = CompositeDisposable()

    interface ViewNavigation {
        fun startActivityForResult(intent: Intent, requestCode: Int)
    }

    fun isFirstSetUp(): Boolean {
        return sharedPreferences.getBoolean(IS_FIRST_SETUP_KEY, true)
    }

    fun setIsFirstSetUpFalse() {
        sharedPreferences.edit().putBoolean(IS_FIRST_SETUP_KEY, false).apply()
    }

    override fun onCleared() {
        disposable.clear()
    }
}