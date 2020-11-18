package com.atmko.skiptoit.confirmation

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
import com.atmko.skiptoit.common.views.BaseBottomSheetDialogFragment
import com.atmko.skiptoit.databinding.FragmentConfirmationBinding
import com.atmko.skiptoit.model.USERNAME_KEY
import com.atmko.skiptoit.model.User
import com.atmko.skiptoit.utils.toEditable
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

class ConfirmationFragment : BaseBottomSheetDialogFragment(), ConfirmationViewModel.Listener {

    private var _binding: FragmentConfirmationBinding? = null
    private val binding get() = _binding!!

    private lateinit var message: String

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: ConfirmationViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)

        getPresentationComponent().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args: ConfirmationFragmentArgs by navArgs()
        message = args.message
        defineViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        configureViews()
        configureViewValues(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        viewModel.registerListener(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(USERNAME_KEY, binding.usernameEditText.text.toString())
    }

    override fun onStop() {
        super.onStop()
        viewModel.unregisterListener(this)
    }

    private fun configureViews() {
        binding.confirmationButton.apply {
            setOnClickListener {
                viewModel.updateUsernameAndNotify(binding.usernameEditText.text.toString())
            }
        }
    }

    private fun defineViewModel() {
        activity?.let {
            viewModel = ViewModelProvider(
                it,
                viewModelFactory
            ).get(ConfirmationViewModel::class.java)
        }
    }

    private fun configureViewValues(savedInstanceState: Bundle?) {
        binding.messageTextView.text = message
        if (savedInstanceState != null) {
            binding.usernameEditText.text =
                savedInstanceState.getString(USERNAME_KEY)?.toEditable()
        }
    }

    override fun notifyProcessing() {
        binding.errorAndLoading.loadingScreen.visibility = View.VISIBLE
    }

    override fun onUsernameUpdated(user: User) {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE
        getMasterActivity().masterActivityViewModel.currentUser = user
        findNavController().navigateUp()
    }

    override fun onUsernameUpdateFailed() {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE
        Snackbar.make(
            requireView(),
            getString(R.string.failed_to_update_username),
            Snackbar.LENGTH_LONG
        ).show()
    }
}
