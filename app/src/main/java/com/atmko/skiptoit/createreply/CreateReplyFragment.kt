package com.atmko.skiptoit.createreply

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.atmko.skiptoit.R
import com.atmko.skiptoit.databinding.FragmentCreateReplyBinding
import com.atmko.skiptoit.model.BODY_KEY
import com.atmko.skiptoit.model.Comment
import com.atmko.skiptoit.util.toEditable
import com.atmko.skiptoit.view.common.BaseFragment
import com.atmko.skiptoit.view.MasterActivity
import com.atmko.skiptoit.viewmodel.common.ViewModelFactory
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

const val CREATED_REPLY_KEY = "create_reply"

class CreateReplyFragment: BaseFragment(), CreateReplyViewModel.Listener {

    private var _binding: FragmentCreateReplyBinding? = null
    private val binding get() = _binding!!

    private lateinit var username: String
    private lateinit var parentId: String
    private lateinit var quotedText: String

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
        quotedText = args.quotedText
        username = args.username
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureToolbar(binding.toolbar.toolbar)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        configureViews()
        configureValues(savedInstanceState)
    }

    override fun onPause() {
        super.onPause()
        viewModel.unregisterListener(this)
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
                val comment = binding.bodyEditText.text.toString()
                viewModel.createReplyAndNotify(parentId, comment)
            }
        }
    }

    private fun configureValues(savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this,
            viewModelFactory).get(CreateReplyViewModel::class.java)

        binding.usernameTextView.text = username
        binding.quotedText.text = quotedText
        if (savedInstanceState != null) {
            binding.bodyEditText.text = savedInstanceState.getString(BODY_KEY)?.toEditable()
        }
    }

    override fun notifyProcessing() {
        binding.createButton.isEnabled = false
        binding.errorAndLoading.loadingScreen.visibility = View.VISIBLE
        binding.errorAndLoading.errorScreen.visibility = View.GONE
    }

    override fun onReplyCreated(comment: Comment?) {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE
        binding.errorAndLoading.errorScreen.visibility = View.GONE

        val savedStateHandle = findNavController().previousBackStackEntry?.savedStateHandle
        savedStateHandle?.set(CREATED_REPLY_KEY, comment)

        val masterActivity: MasterActivity = (activity as MasterActivity)
        masterActivity.onBackPressedDispatcher.onBackPressed()

        view?.let { view -> masterActivity.hideSoftKeyboard(view) }
    }

    override fun onReplyCreateFailed() {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE
        binding.errorAndLoading.errorScreen.visibility = View.VISIBLE
        Snackbar.make(requireView(), getString(R.string.failed_to_create_comment), Snackbar.LENGTH_LONG).show()
    }
}
