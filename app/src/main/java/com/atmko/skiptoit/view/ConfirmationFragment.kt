package com.atmko.skiptoit.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.atmko.skiptoit.databinding.FragmentConfirmationBinding
import com.atmko.skiptoit.model.User
import com.atmko.skiptoit.view.common.BaseBottomSheetDialogFragment
import com.atmko.skiptoit.viewmodel.MasterActivityViewModel
import com.atmko.skiptoit.viewmodel.common.ViewModelFactory
import javax.inject.Inject

class ConfirmationFragment : BaseBottomSheetDialogFragment() {
    private var _binding: FragmentConfirmationBinding? = null
    private val binding get() = _binding!!

    private lateinit var message: String

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: MasterActivityViewModel
    private var user: User? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        getPresentationComponent().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args: ConfirmationFragmentArgs by navArgs()
        message = args.message
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
        configureValues()
        configureViewModel()
    }

    private fun configureViews() {
        binding.messageTextView.text = message
        binding.confirmationButton.apply {
            setOnClickListener {
                viewModel.updateUsername(binding.usernameEditText.text.toString())
            }
        }
    }

    private fun configureValues() {
        activity?.let {
            viewModel = ViewModelProvider(it,
                viewModelFactory).get(MasterActivityViewModel::class.java)
        }
    }

    private fun configureViewModel() {
        viewModel.currentUser.observe(viewLifecycleOwner, Observer {currentUser->
            if (user != null && currentUser != null) {
                activity?.supportFragmentManager?.beginTransaction()?.remove(this)
            }
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer { isProcessing ->
            isProcessing?.let {
                binding.errorAndLoading.loadingScreen.visibility =
                    if (it) View.VISIBLE else View.GONE
                if (it) {
                    binding.errorAndLoading.errorScreen.visibility = View.GONE
                }
            }
        })

        viewModel.loadError.observe(viewLifecycleOwner, Observer { isError ->
            isError.let {
                binding.errorAndLoading.errorScreen.visibility =
                    if (it) View.VISIBLE else View.GONE
            }
        })
    }
}
