package com.atmko.podcastapp.view

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.atmko.podcastapp.R
import com.atmko.podcastapp.databinding.FragmentEpisodeBinding
import com.atmko.podcastapp.model.EPISODE_ID_KEY
import com.atmko.podcastapp.util.loadNetworkImage
import com.atmko.podcastapp.viewmodel.EpisodeViewModel
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util


class EpisodeFragment : Fragment() {
    private var _binding: FragmentEpisodeBinding? = null
    private val binding get() = _binding!!

    //fragment init variable
    private var episodeId: String? = null

    private lateinit var player: SimpleExoPlayer
    private var viewModel: EpisodeViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            episodeId = it.getString(EPISODE_ID_KEY)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEpisodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        configureViews()
        configureValues(savedInstanceState)
        configureViewModel()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(episodeId: String) =
            EpisodeFragment().apply {
                arguments = Bundle().apply {
                    putString(EPISODE_ID_KEY, episodeId)
                }
            }
    }

    private fun configureViews() {
        binding.collapsedBottomSheet.visibility = View.INVISIBLE
        binding.expandedBottomSheet.visibility = View.VISIBLE
    }

    private fun configureValues(savedInstanceState: Bundle?) {
        if (viewModel == null) {
            viewModel = ViewModelProviders.of(this).get(EpisodeViewModel::class.java)
        }

        if (savedInstanceState == null) {
            episodeId?.let { viewModel?.refresh(it) }
        }

        context?.let {
            player = SimpleExoPlayer.Builder(it).build()
            binding.playPanel.player = player
        }
    }

    private fun configureViewModel() {
        viewModel?.episodeDetails?.observe(viewLifecycleOwner, Observer { episodeDetails ->
            episodeDetails?.let {details ->
                //set expanded values
                binding.expandedPodcastImageView.loadNetworkImage(details.image)
                binding.expandedTitle.text = details.podcast?.title
                binding.expandedEpisodeNumber.text = details.title
                binding.title.text = details.title
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    binding.description.text = Html.fromHtml(details.description, Html.FROM_HTML_MODE_COMPACT)
                } else {
                    binding.description.text = Html.fromHtml(details.description)
                }

                //set collapsed values
                binding.collapsedPodcastImageView.loadNetworkImage(details.image)
                binding.collapsedTitle.text = details.podcast?.title
                binding.collapsedEpisodeNumber.text = details.title

                context?.let {context ->
                    // Produces DataSource instances through which media data is loaded.
                    val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
                        context,
                        Util.getUserAgent(context, getString(R.string.app_name))
                    )
                    // This is the MediaSource representing the media to be played.
                    val audioSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(Uri.parse(details.audio))
                    // Prepare the player with the source.
                    player.prepare(audioSource)
                    player.playWhenReady = true
                }
            }
        })

        viewModel?.loading?.observe(viewLifecycleOwner, Observer { isLoading ->
            isLoading?.let {
                binding.errorAndLoading.loadingScreen.visibility =
                    if (it) View.VISIBLE else View.GONE
                if (it) {
                    binding.errorAndLoading.errorScreen.visibility = View.GONE
                }
            }
        })

        viewModel?.loadError?.observe(viewLifecycleOwner, Observer { isError ->
            isError.let {
                binding.errorAndLoading.errorScreen.visibility =
                    if (it) View.VISIBLE else View.GONE
            }
        })
    }
}
