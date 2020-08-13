package com.atmko.skiptoit.view

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.atmko.skiptoit.R
import com.atmko.skiptoit.databinding.FragmentLaunchBinding
import com.atmko.skiptoit.view.common.BaseFragment
import com.atmko.skiptoit.viewmodel.LaunchFragmentViewModel
import com.atmko.skiptoit.viewmodel.MasterActivityViewModel
import com.atmko.skiptoit.viewmodel.ViewModelFactory
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.security.ProviderInstaller
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

const val LAUNCH_FRAGMENT_KEY = "launch_fragment"
const val IS_FIRST_SETUP_KEY = "is_first_set_up"
private const val ERROR_DIALOG_REQUEST_CODE = 1

class LaunchFragment : BaseFragment(),
    ProviderInstaller.ProviderInstallListener,
    MasterActivityViewModel.ViewNavigation {

    private var _binding: FragmentLaunchBinding? = null
    private val binding get() = _binding!!

    private var isProviderUpdated = false

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private var masterActivityViewModel: MasterActivityViewModel? = null

    private lateinit var launchFragmentViewModel: LaunchFragmentViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentLaunchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        configureViews()
        configureValues()
        retryProviderInstall()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onProviderInstallFailed(errorCode: Int, recoveryIntent: Intent?) {
        val availability = GoogleApiAvailability.getInstance()
        if (availability.isUserResolvableError(errorCode)) {
            // Recoverable error. Show a dialog prompting the user to
            // install/update/enable Google Play services.
            activity?.let {
                availability.showErrorDialogFragment(
                    it,
                    errorCode,
                    ERROR_DIALOG_REQUEST_CODE
                ) {
                    // The user chose not to take the recovery action
                    onProviderInstallerNotAvailable()
                }
            }
        } else { // Google Play services is not available.
            onProviderInstallerNotAvailable()
        }
    }

    override fun onProviderInstalled() {
        isProviderUpdated = true
        if (!launchFragmentViewModel.isFirstSetUp()) {
            startApp()
        } else {
            configureMasterActivityViewModel()
        }
    }

    private fun configureViews() {
        (activity as MasterActivity).hideBottomPanels()

        val spannableString = SpannableString(binding.termsTextView.text.toString())
        val termsClickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                launchBrowserIntent(getString(R.string.terms_url));
            }
        }
        val privacyClickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                launchBrowserIntent(getString(R.string.privacy_url));
            }
        }
        val clickableSpans: IntArray  = resources.getIntArray(R.array.terms_clickable_spans);

        spannableString.setSpan(
            termsClickableSpan,
            clickableSpans[0],
            clickableSpans[1],
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            privacyClickableSpan,
            clickableSpans[2],
            clickableSpans[3],
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.termsTextView.text = spannableString
        binding.termsTextView.movementMethod = LinkMovementMethod.getInstance()
        binding.termsTextView.highlightColor = Color.TRANSPARENT

        binding.googleContinue.setOnClickListener {
            if (isProviderUpdated) {
                context?.let {context ->
                    masterActivityViewModel!!.signIn()
                }
            }
        }

        binding.guestContinue.setOnClickListener {
            if (isProviderUpdated) {
                startApp()
            }
        }
    }

    private fun configureValues() {
        if (masterActivityViewModel == null) {
            activity?.let {
                masterActivityViewModel = ViewModelProviders.of(it).get(MasterActivityViewModel::class.java)
            }
        }

        //todo: refactor other view models to follow this(with logic in view model)
        launchFragmentViewModel = ViewModelProvider(requireActivity(),
            viewModelFactory).get(LaunchFragmentViewModel::class.java)
    }

    private fun configureMasterActivityViewModel() {
        masterActivityViewModel!!.messageEvent.setEventReceiver(this, this)
        masterActivityViewModel!!.currentUser.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                startApp()
            }
        })

        masterActivityViewModel!!.loadError.observe(viewLifecycleOwner, Observer {
            if (it) {
                Snackbar.make(binding.topLayout,
                    getString(R.string.couldnt_connect_to_server), Snackbar.LENGTH_SHORT).show()
            }
        })
    }

    private fun retryProviderInstall() {
        context?.let {
            ProviderInstaller.installIfNeededAsync(it, this)
        }
    }

    private fun startApp() {
        launchFragmentViewModel.setIsFirstSetUpFalse()
        val action = LaunchFragmentDirections.actionNavigationLaunchToNavigationSubscriptions()
        view?.findNavController()?.navigate(action)
    }

    private fun launchBrowserIntent(url: String) {
        val webPage: Uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webPage)
        activity?.let {
            if (intent.resolveActivity(it.packageManager) != null) {
                startActivity(intent);
            } else  {
                Snackbar.make(binding.topLayout,
                    R.string.no_browser_error_message, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun onProviderInstallerNotAvailable() {
        context?.let {
            Toast.makeText(
                it, getString(R.string.couldnt_update_google_play_services),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}