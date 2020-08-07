//package com.atmko.skiptoit.viewmodel
//
//import android.content.Context
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import com.atmko.skiptoit.model.Podcast
//import com.atmko.skiptoit.model.database.SkipToItDatabase
//import com.google.android.gms.auth.api.signin.GoogleSignIn
//
//class DetailsViewModelFactory(
//    val context: Context,
//    val podcastDetails: Podcast
//) :
//    ViewModelProvider.NewInstanceFactory() {
//
//    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
//        @Suppress("UNCHECKED_CAST")
//        return DetailsViewModel(
//            GoogleSignIn.getLastSignedInAccount(context),
//            SkipToItDatabase.getInstance(context),
//            podcastDetails) as (T)
//    }
//}
