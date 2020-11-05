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
import com.atmko.skiptoit.databinding.FragmentCreateReplyBinding
import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.utils.toEditable
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

class UpdateReplyFragment : BaseFragment(), UpdateCommentViewModel.Listener {

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

    override fun onResume() {
        super.onResume()
        viewModel.registerListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateReplyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        configureViews()
        configureValues()
    }

    override fun onPause() {
        super.onPause()
        viewModel.unregisterListener(this)
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

    private fun configureValues() {
        viewModel = ViewModelProvider(
            this,
            viewModelFactory
        ).get(UpdateCommentViewModel::class.java)

        viewModel.getCachedCommentAndNotify(commentId)

        binding.usernameTextView.text = username
        binding.quotedText.text = quotedText
    }

    override fun notifyProcessing() {
        binding.errorAndLoading.loadingScreen.visibility = View.VISIBLE
        binding.errorAndLoading.errorScreen.visibility = View.GONE
    }

    override fun onLoadComment(fetchedComment: Comment) {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE
        binding.errorAndLoading.errorScreen.visibility = View.GONE

        this.comment = fetchedComment
        binding.bodyEditText.text = comment.body.toEditable()
        binding.createButton.isEnabled = true
    }

    override fun onLoadCommentFailed() {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE
        binding.errorAndLoading.errorScreen.visibility = View.VISIBLE

        binding.createButton.isEnabled = false
        Snackbar.make(requireView(), getString(R.string.failed_to_load_comment), Snackbar.LENGTH_LONG).show()
    }

    override fun onCommentUpdated() {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE
        binding.errorAndLoading.errorScreen.visibility = View.GONE
        findNavController().navigateUp()
    }

    override fun onCommentUpdateFailed() {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE
        binding.errorAndLoading.errorScreen.visibility = View.VISIBLE
        Snackbar.make(requireView(), getString(R.string.failed_to_update_comment), Snackbar.LENGTH_LONG).show()
    }
}
