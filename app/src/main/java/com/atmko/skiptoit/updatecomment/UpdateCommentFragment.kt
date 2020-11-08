package com.atmko.skiptoit.updatecomment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.atmko.skiptoit.R
import com.atmko.skiptoit.common.ViewModelFactory
import com.atmko.skiptoit.common.views.BaseFragment
import com.atmko.skiptoit.databinding.FragmentCreateCommentBinding
import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.utils.toEditable
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

class UpdateCommentFragment : BaseFragment(), UpdateCommentViewModel.Listener {

    private var _binding: FragmentCreateCommentBinding? = null
    private val binding get() = _binding!!

    private lateinit var commentId: String
    private lateinit var username: String

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

        val args: UpdateCommentFragmentArgs by navArgs()
        commentId = args.commentId
        username = args.username
        defineViewModel()
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
        configureViewValues()
    }

    override fun onStart() {
        super.onStart()
        viewModel.registerListener(this)

        viewModel.getCachedCommentAndNotify(commentId)
    }

    override fun onStop() {
        super.onStop()
        viewModel.unregisterListener(this)
    }

    private fun defineViewModel() {
        viewModel = ViewModelProvider(
            this,
            viewModelFactory
        ).get(UpdateCommentViewModel::class.java)
    }

    private fun configureViews() {
        binding.cancelButton.apply {
            setOnClickListener {
                getMasterActivity().onBackPressedDispatcher.onBackPressed()
                getMasterActivity().hideSoftKeyboard(requireView())
            }
        }

        binding.createButton.apply {
            isEnabled = false
            setOnClickListener {
                getMasterActivity().hideSoftKeyboard(requireView())

                val updatedBody = binding.bodyEditText.text.toString()
                viewModel.updateCommentBodyAndNotify(updatedBody)
            }
        }
    }

    private fun configureViewValues() {
        binding.usernameTextView.text = username
    }

    override fun notifyProcessing() {
        binding.createButton.isEnabled = false
        binding.errorAndLoading.loadingScreen.visibility = View.VISIBLE
        binding.errorAndLoading.errorScreen.visibility = View.GONE
    }

    override fun onLoadComment(fetchedComment: Comment) {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE
        binding.errorAndLoading.errorScreen.visibility = View.GONE

        this.comment = fetchedComment
        binding.bodyEditText.text =
            if (comment.body != null) comment.body!!.toEditable() else "".toEditable()
        binding.createButton.isEnabled = true
    }

    override fun onLoadCommentFailed() {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE
        binding.errorAndLoading.errorScreen.visibility = View.VISIBLE

        binding.createButton.isEnabled = false
        Snackbar.make(
            requireView(),
            getString(R.string.failed_to_load_comment),
            Snackbar.LENGTH_LONG
        ).show()
    }

    override fun onCommentUpdated() {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE
        binding.errorAndLoading.errorScreen.visibility = View.GONE
        findNavController().navigateUp()
    }

    override fun onCommentUpdateFailed() {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE
        binding.errorAndLoading.errorScreen.visibility = View.VISIBLE
        Snackbar.make(
            requireView(),
            getString(R.string.failed_to_update_comment),
            Snackbar.LENGTH_LONG
        ).show()
    }
}
