package com.atmko.skiptoit.createreply

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
import com.atmko.skiptoit.databinding.FragmentCreateReplyBinding
import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.utils.toEditable
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

class CreateReplyFragment : BaseFragment(), CreateReplyViewModel.Listener {

    private var _binding: FragmentCreateReplyBinding? = null
    private val binding get() = _binding!!

    private lateinit var username: String
    private lateinit var parentId: String

    private lateinit var parentComment: Comment

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: CreateReplyViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)

        getPresentationComponent().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args: CreateReplyFragmentArgs by navArgs()
        parentId = args.parentId
        username = args.username
        defineViewModel()
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
        configureViewValues()
    }

    override fun onStart() {
        super.onStart()
        viewModel.registerListener(this)

        viewModel.getCachedParentCommentAndNotify(parentId)
    }

    override fun onStop() {
        super.onStop()
        viewModel.unregisterListener(this)
    }

    private fun defineViewModel() {
        viewModel = ViewModelProvider(
            this,
            viewModelFactory
        ).get(CreateReplyViewModel::class.java)
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
                val comment = binding.bodyEditText.text.toString()
                viewModel.createReplyAndNotify(parentId, comment)
            }
        }
    }

    private fun configureViewValues() {
        binding.usernameTextView.text = username
    }

    override fun notifyProcessing() {
        binding.createButton.isEnabled = false
        binding.errorAndLoading.loadingScreen.visibility = View.VISIBLE
    }

    override fun onLoadParentComment(fetchedComment: Comment) {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE

        parentComment = fetchedComment
        binding.parentText.text =
            if (parentComment.body != null) parentComment.body!!.toEditable() else "".toEditable()
        binding.createButton.isEnabled = true
    }

    override fun onLoadParentCommentFailed() {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE

        binding.createButton.isEnabled = false
        Snackbar.make(
            requireView(),
            getString(R.string.failed_to_load_parent_comment),
            Snackbar.LENGTH_LONG
        ).show()
    }

    override fun onReplyCreated() {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE

        getMasterActivity().onBackPressedDispatcher.onBackPressed()
        view?.let { view -> getMasterActivity().hideSoftKeyboard(view) }
    }

    override fun onReplyCreateFailed() {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE
        Snackbar.make(
            requireView(),
            getString(R.string.failed_to_create_comment),
            Snackbar.LENGTH_LONG
        ).show()
    }

    override fun onPageTrackerFetchFailed() {
        viewModel.updateParentCommentReplyCountAndNotify(parentId)
        Log.d(this.javaClass.simpleName, "error getting page tracker")
    }

    override fun onReplyPageDeleteFailed() {
        viewModel.updateParentCommentReplyCountAndNotify(parentId)
        Log.d(this.javaClass.simpleName, "error deleting reply page")
    }

    override fun onUpdateReplyCountFailed() {
        Log.d(this.javaClass.simpleName, "error to increase reply count")
    }
}
