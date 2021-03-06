package com.atmko.skiptoit.dependencyinjection.presentation

import com.atmko.skiptoit.MasterActivity
import com.atmko.skiptoit.confirmation.ConfirmationFragment
import com.atmko.skiptoit.episode.EpisodeFragment
import com.atmko.skiptoit.episode.replies.RepliesFragment
import com.atmko.skiptoit.createcomment.CreateCommentFragment
import com.atmko.skiptoit.createreply.CreateReplyFragment
import com.atmko.skiptoit.details.DetailsFragment
import com.atmko.skiptoit.launch.LaunchActivity
import com.atmko.skiptoit.search.searchchild.SearchFragment
import com.atmko.skiptoit.search.searchparent.SearchParentFragment
import com.atmko.skiptoit.subcriptions.SubscriptionsFragment
import com.atmko.skiptoit.updatecomment.UpdateCommentFragment
import com.atmko.skiptoit.updatecomment.UpdateReplyFragment
import dagger.Subcomponent

@Subcomponent(modules = [
    PresentationModule::class,
    AdapterModule::class,
    ViewModelModule::class,
    PagingModule::class])
interface PresentationComponent {
    fun inject(masterActivity: MasterActivity)

    fun inject(subscriptionsFragment: SubscriptionsFragment)
    fun inject(searchParentFragment: SearchParentFragment)
    fun inject(searchFragment: SearchFragment)
    fun inject(detailsFragment: DetailsFragment)
    fun inject(episodeFragment: EpisodeFragment)

    fun inject(launchActity: LaunchActivity)
    fun inject(repliesFragment: RepliesFragment)
    fun inject(confirmationFragment: ConfirmationFragment)
    fun inject(createCommentFragment: CreateCommentFragment)
    fun inject(createReplyFragment: CreateReplyFragment)
    fun inject(updateCommentFragment: UpdateCommentFragment)
    fun inject(updateReplyFragment: UpdateReplyFragment)
}