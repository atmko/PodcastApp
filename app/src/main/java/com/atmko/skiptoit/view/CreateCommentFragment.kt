package com.atmko.skiptoit.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.atmko.skiptoit.databinding.FragmentCreateCommentBinding
import com.atmko.skiptoit.model.BODY_KEY
import com.atmko.skiptoit.util.toEditable
import com.atmko.skiptoit.view.common.BaseFragment
import com.atmko.skiptoit.viewmodel.CreateCommentViewModel
import com.atmko.skiptoit.viewmodel.common.ViewModelFactory
import javax.inject.Inject

class CreateCommentFragment: BaseFragment() {

    private var _binding: FragmentCreateCommentBinding? = null
    private val binding get() = _binding!!

    private lateinit var username: String
    private lateinit var podcastId: String
    private lateinit var episodeId: String

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private var viewModel: CreateCommentViewModel? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        getPresentationComponent().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args: CreateCommentFragmentArgs by navArgs()
        podcastId = args.podcastId
        episodeId = args.episodeId
        username = args.username
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateCommentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        configureViews()
        configureValues(savedInstanceState)
        configureViewModel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(BODY_KEY, binding.bodyEditText.text.toString())
    }

    private fun configureViews() {
        binding.createButton.apply {
            setOnClickListener {
                val comment = binding.bodyEditText.text.toString()
                viewModel?.createComment(podcastId, episodeId, comment)
            }
        }
    }

    private fun configureValues(savedInstanceState: Bundle?) {
        binding.usernameTextView.text = username

        if (viewModel == null) {
            viewModel = ViewModelProviders.of(this,
                viewModelFactory).get(CreateCommentViewModel::class.java)
        }

        if (savedInstanceState != null) {
            binding.bodyEditText.text = savedInstanceState.getString(BODY_KEY)?.toEditable()
        }
    }

    private fun configureViewModel() {
        viewModel?.isCreated?.observe(viewLifecycleOwner, Observer { isCreated ->
            isCreated?.let {
                if (isCreated) {
                    val masterActivity: MasterActivity = (activity as MasterActivity)
                    masterActivity.onBackPressedDispatcher.onBackPressed()
                    view?.let { view -> masterActivity.hideSoftKeyboard(view) }
                }
            }
        })

        viewModel?.processing?.observe(viewLifecycleOwner, Observer { isProcessing ->
            isProcessing?.let {
                binding.errorAndLoading.loadingScreen.visibility =
                    if (it) View.VISIBLE else View.GONE
                if (it) {
                    binding.errorAndLoading.errorScreen.visibility = View.GONE
                }
            }
        })

        viewModel?.createError?.observe(viewLifecycleOwner, Observer { isError ->
            isError.let {
                binding.errorAndLoading.errorScreen.visibility =
                    if (it) View.VISIBLE else View.GONE
            }
        })
    }
}
