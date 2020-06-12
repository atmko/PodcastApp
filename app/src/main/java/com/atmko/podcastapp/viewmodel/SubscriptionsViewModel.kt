package com.atmko.podcastapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SubscriptionsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is subscriptions Fragment"
    }
    val text: LiveData<String> = _text
}