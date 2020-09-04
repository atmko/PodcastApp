package com.atmko.skiptoit.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.atmko.skiptoit.databinding.FragmentCreateReplyBinding
import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.util.toEditable
import com.atmko.skiptoit.view.common.BaseFragment
import com.atmko.skiptoit.viewmodel.UpdateCommentViewModel
import com.atmko.skiptoit.viewmodel.common.ViewModelFactory
import javax.inject.Inject

class UpdateReplyFragment : BaseFragment() {

    private var _binding: FragmentCreateReplyBinding? = null
    private val binding get() = _binding!!

    private lateinit var commentId: String
    private lateinit var username: String
    private lateinit var quotedText: String

    private lateinit var comment: Comment

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: UpdateCommentViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)

        getPresentationComponent().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args: UpdateReplyFragmentArgs by navArgs()
        commentId = args.commentId
        username = args.username
        quotedText = args.quotedText
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateReplyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureToolbar(binding.toolbar.toolbar)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        configureViews()
        configureValues()
        configureViewModel()
    }

    private fun configureViews() {
        binding.cancelButton.apply {
            setOnClickListener {
                val masterActivity: MasterActivity = (activity as MasterActivity)
                masterActivity.onBackPressedDispatcher.onBackPressed()

                masterActivity.hideSoftKeyboard(requireView())
            }
        }

        binding.createButton.apply {
            isEnabled = false
            setOnClickListener {
                val masterActivity: MasterActivity = (activity as MasterActivity)
                masterActivity.hideSoftKeyboard(requireView())

                val updatedBody = binding.bodyEditText.text.toString()
                viewModel.updateCommentBody(updatedBody)
            }
        }
    }

    private fun configureValues() {
        viewModel = ViewModelProvider(
            this,
            viewModelFactory
        ).get(UpdateCommentViewModel::class.java)

        viewModel.loadComment(commentId)

        binding.usernameTextView.text = username
        binding.quotedText.text = quotedText
    }

    private fun configureViewModel() {
        viewModel.isUpdated.observe(viewLifecycleOwner, Observer { isUpdated ->
            isUpdated?.let {
                findNavController().navigateUp()
            }
        })

        viewModel.commentLiveData.observe(viewLifecycleOwner, Observer { comment ->
            comment?.let {
                this.comment = comment
                binding.bodyEditText.text = comment.body.toEditable()
                binding.createButton.isEnabled = true
            }
        })

        viewModel.processing.observe(viewLifecycleOwner, Observer { isProcessing ->
            isProcessing?.let {
                binding.errorAndLoading.loadingScreen.visibility =
                    if (it) View.VISIBLE else View.GONE
                if (it) {
                    binding.errorAndLoading.errorScreen.visibility = View.GONE
                }
            }
        })

        viewModel.updateError.observe(viewLifecycleOwner, Observer { isError ->
            isError.let {
                binding.errorAndLoading.errorScreen.visibility =
                    if (it) View.VISIBLE else View.GONE
            }
        })
    }
}
