package com.atmko.skiptoit.confirmation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.atmko.skiptoit.MasterActivity
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
    }

    override fun onResume() {
        super.onResume()
        viewModel.registerListener(this)
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
        configureValues(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(USERNAME_KEY, binding.usernameEditText.text.toString())
    }

    override fun onPause() {
        super.onPause()
        viewModel.unregisterListener(this)
    }

    private fun configureViews() {
        binding.confirmationButton.apply {
            setOnClickListener {
                viewModel.updateUsernameAndNotify(binding.usernameEditText.text.toString())
            }
        }
    }

    private fun configureValues(savedInstanceState: Bundle?) {
        activity?.let {
            viewModel = ViewModelProvider(it,
                viewModelFactory).get(ConfirmationViewModel::class.java)
        }

        binding.messageTextView.text = message
        if (savedInstanceState != null) {
            binding.usernameEditText.text =
                savedInstanceState.getString(USERNAME_KEY)?.toEditable()
        }
    }

    override fun notifyProcessing() {
        binding.errorAndLoading.loadingScreen.visibility = View.VISIBLE
        binding.errorAndLoading.errorScreen.visibility = View.GONE
    }

    override fun onUsernameUpdated(user: User) {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE
        binding.errorAndLoading.errorScreen.visibility = View.GONE
        (requireActivity() as MasterActivity).user = user
        findNavController().navigateUp()
    }

    override fun onUsernameUpdateFailed() {
        binding.errorAndLoading.loadingScreen.visibility = View.GONE
        binding.errorAndLoading.errorScreen.visibility = View.VISIBLE
        Snackbar.make(requireView(), "Failed to update username", Snackbar.LENGTH_LONG).show()
    }
}
