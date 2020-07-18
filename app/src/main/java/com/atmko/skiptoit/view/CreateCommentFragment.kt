package com.atmko.skiptoit.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.atmko.skiptoit.databinding.FragmentCreateCommentBinding
import com.atmko.skiptoit.model.BODY_KEY
import com.atmko.skiptoit.util.toEditable
import com.atmko.skiptoit.viewmodel.CommentsViewModel

class CreateCommentFragment: Fragment() {

    private var _binding: FragmentCreateCommentBinding? = null
    private val binding get() = _binding!!

    private lateinit var username: String
    private lateinit var podcastId: String
    private lateinit var episodeId: String
    private var parentId: String? = null
    private var quotedText: String? = null

    private var viewModel: CommentsViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args: CreateCommentFragmentArgs by navArgs()
        podcastId = args.podcastId
        episodeId = args.episodeId
        parentId = args.parentId
        quotedText = args.quotedText
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
                viewModel?.createComment(podcastId, parentId, episodeId, comment)
            }
        }

        if (parentId == null) {
            binding.quotedText.visibility = View.GONE
            binding.quotedTextDivider.visibility = View.GONE
        } else{
            binding.quotedText.text = quotedText
        }
    }

    private fun configureValues(savedInstanceState: Bundle?) {
        binding.usernameTextView.text = username

        if (viewModel == null) {
            activity?.let {
                viewModel = ViewModelProviders.of(it).get(CommentsViewModel::class.java)
            }
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
