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
import com.atmko.skiptoit.model.BODY_KEY
import com.atmko.skiptoit.model.BodyUpdate
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
    private lateinit var oldCommentBody: String
    private var commentAdapterPosition: Int = 0

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
        oldCommentBody = args.oldCommentBody
        commentAdapterPosition = args.commentAdapterPosition
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
        configureValues(savedInstanceState)
        configureViewModel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(BODY_KEY, binding.bodyEditText.text.toString())
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
            setOnClickListener {
                val masterActivity: MasterActivity = (activity as MasterActivity)
                masterActivity.hideSoftKeyboard(requireView())

                val commentBody = binding.bodyEditText.text.toString()
                viewModel.updateCommentBody(
                    commentId,
                    BodyUpdate(commentBody, commentAdapterPosition)
                )
            }
        }
    }

    private fun configureValues(savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(
            this,
            viewModelFactory
        ).get(UpdateCommentViewModel::class.java)

        binding.usernameTextView.text = username
        binding.quotedText.text = quotedText
        if (savedInstanceState != null) {
            binding.bodyEditText.text = savedInstanceState.getString(BODY_KEY)?.toEditable()
        } else {
            binding.bodyEditText.text = oldCommentBody.toEditable()
        }
    }

    private fun configureViewModel() {
        viewModel.bodyUpdateLiveData.observe(viewLifecycleOwner, Observer { bodyUpdate ->
            val savedStateHandle = findNavController().previousBackStackEntry?.savedStateHandle
            savedStateHandle?.set(BODY_UPDATE_KEY, bodyUpdate)
            findNavController().navigateUp()
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
