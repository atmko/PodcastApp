package com.atmko.skiptoit.dependencyinjection.presentation

import com.atmko.skiptoit.view.*
import dagger.Subcomponent

@Subcomponent(modules = [PresentationModule::class, ViewModelModule::class])
interface PresentationComponent {
    fun inject(masterActivity: MasterActivity)

    fun inject(subscriptionsFragment: SubscriptionsFragment)
    fun inject(searchParentFragment: SearchParentFragment)
    fun inject(searchFragment: SearchFragment)
    fun inject(detailsFragment: DetailsFragment)
    fun inject(episodeFragment: EpisodeFragment)

    fun inject(repliesFragment: RepliesFragment)
    fun inject(confirmationFragment: ConfirmationFragment)
    fun inject(createCommentFragment: CreateCommentFragment)
    fun inject(createReplyFragment: CreateReplyFragment)
}