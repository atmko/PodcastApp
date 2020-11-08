package com.atmko.skiptoit.createcomment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.atmko.skiptoit.R
import com.atmko.skiptoit.common.ViewModelFactory
import com.atmko.skiptoit.common.views.BaseFragment
import com.atmko.skiptoit.databinding.FragmentCreateCommentBinding
import com.atmko.skiptoit.model.BODY_KEY
import com.atmko.skiptoit.utils.toEditable
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

class CreateCommentFragment: BaseFragment(), CreateCommentViewModel.Listener {

    private var _binding: FragmentCreateCommentBinding? = null
    private val binding get() = _binding!!

    private lateinit var username: String
    private lateinit var podcastId: String
    private lateinit var episodeId: String

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: CreateCommentViewModel

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
        defineViewModel()
    }

    override fun onStart() {
        super.onStart()
        viewModel.registerListener(this)
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
        configureViewValues(savedInstanceState)
    }

    override fun onStop() {
        super.onStop()
        viewModel.unregisterListener(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(BODY_KEY, binding.bodyEditText.text.toString())
    }

    private fun defineViewModel() {
        viewModel = ViewModelProvider(this,
            viewModelFactory).get(CreateCommentViewModel::class.java)
    }

    private fun configureViews() {
        binding.cancelButton.apply {
            setOnClickListener {
                getMasterActivity().onBackPressedDispatcher.onBackPressed()
                getMasterActivity().hideSoftKeyboard(requireView())
            }
        }

        binding.createButton.apply {
            setOnClickListener {
                val comment = binding.bodyEditText.text.toString()
                viewModel.createCommentAndNotify(podcastId, episodeId, comment)
            }
        }
    }

    private fun configureViewValues(savedInstanceState: Bundle?) {
        binding.usernameTextView.text = username
        if (savedInstanceState != null) {
            binding.bodyEditText.text = savedInstanceState.getString(BODY_KEY)?.toEditable()
        }
    }

    override fun notifyProcessing() {
        binding.createButton.isEnabled = false
        binding.errorAndLoading.loadingScreen.visibility = View.VISIBLE
        binding.errorAndLoading.errorScreen.visibility = View.GONE
    }

    override fun onCommentCreated() {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE
        binding.errorAndLoading.errorScreen.visibility = View.GONE

        getMasterActivity().onBackPressedDispatcher.onBackPressed()
        view?.let { view -> getMasterActivity().hideSoftKeyboard(view) }
    }

    override fun onCommentCreateFailed() {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE
        binding.errorAndLoading.errorScreen.visibility = View.VISIBLE
        Snackbar.make(requireView(), getString(R.string.failed_to_create_comment), Snackbar.LENGTH_LONG).show()
    }

    override fun onPageTrackerFetchFailed() {
        Log.d(this.javaClass.simpleName, "error getting page tracker")
    }

    override fun onCommentPageDeleteFailed() {
        Log.d(this.javaClass.simpleName, "error deleting comment page")
    }
}
