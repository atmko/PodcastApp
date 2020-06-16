package com.atmko.podcastapp.view

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.atmko.podcastapp.databinding.FragmentEpisodeBinding
import com.atmko.podcastapp.model.EPISODE_ID_KEY
import com.atmko.podcastapp.model.Episode
import com.atmko.podcastapp.util.loadNetworkImage
import com.atmko.podcastapp.viewmodel.EpisodeViewModel

class EpisodeFragment : Fragment() {
    private var _binding: FragmentEpisodeBinding? = null
    private val binding get() = _binding!!

    //fragment init variable
    private var episodeId: String? = null

    private lateinit var episodeDetails: Episode
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
    }

    private fun configureViewModel() {
        viewModel?.episodeDetails?.observe(viewLifecycleOwner, Observer { episodeDetails ->
            this.episodeDetails = episodeDetails
            this.episodeDetails.let {
                binding.title.text = it.title
                binding.expandedPodcastImageView.loadNetworkImage(it.image)
                binding.expandedTitle.text = it.podcast?.title
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    binding.description.text = Html.fromHtml(it.description, Html.FROM_HTML_MODE_COMPACT)
                } else {
                    binding.description.text = Html.fromHtml(it.description)
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
