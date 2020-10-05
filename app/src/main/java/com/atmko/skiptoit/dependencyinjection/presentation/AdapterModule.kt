package com.atmko.skiptoit.dependencyinjection.presentation

import androidx.lifecycle.LifecycleOwner
import com.atmko.skiptoit.episode.CommentsAdapter
import com.atmko.skiptoit.details.EpisodeAdapter
import com.atmko.skiptoit.search.searchchild.PodcastAdapter
import dagger.Module
import dagger.Provides

@Module
class AdapterModule(private val lifecycleOwner: LifecycleOwner) {
    @Provides
    fun providePodcastAdapter(): PodcastAdapter {
        return PodcastAdapter(
            lifecycleOwner as PodcastAdapter.OnPodcastItemClickListener
        )
    }

    @Provides
    fun provideEpisodeAdapter(): EpisodeAdapter {
        return EpisodeAdapter(lifecycleOwner as EpisodeAdapter.OnEpisodeItemClickListener)
    }

    @Provides
    fun provideCommentsAdapter(): CommentsAdapter {
        return CommentsAdapter(lifecycleOwner as CommentsAdapter.OnCommentItemClickListener)
    }
}