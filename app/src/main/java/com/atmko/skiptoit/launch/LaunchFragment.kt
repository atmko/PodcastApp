package com.atmko.skiptoit.launch

import android.content.Context
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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.atmko.skiptoit.R
import com.atmko.skiptoit.databinding.FragmentLaunchBinding
import com.atmko.skiptoit.common.views.BaseFragment
import com.atmko.skiptoit.common.ViewModelFactory
import com.atmko.skiptoit.common.ManagerViewModel
import com.atmko.skiptoit.model.User
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.security.ProviderInstaller
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

const val LAUNCH_FRAGMENT_KEY = "launch_fragment"
const val IS_FIRST_SETUP_KEY = "is_first_set_up"
private const val ERROR_DIALOG_REQUEST_CODE = 1

class LaunchFragment : BaseFragment(),
    ProviderInstaller.ProviderInstallListener,
    ManagerViewModel.Listener {

    private var _binding: FragmentLaunchBinding? = null
    private val binding get() = _binding!!

    private var isProviderUpdated = false
    private var googleSignInIntent: Intent? = null
    private var googleSignInRequestCode: Int? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: LaunchFragmentViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)

        getPresentationComponent().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLaunchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        configureViews()
        configureValues()
        retryProviderInstall()
    }

    override fun onStart() {
        super.onStart()
        viewModel.registerListener(this)
    }

    override fun onStop() {
        super.onStop()
        viewModel.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.let { viewModel.onRequestResultReceived(requestCode, resultCode, it) }
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
        if (!viewModel.isFirstSetUp()) {
            startApp()
        }
    }

    private fun configureViews() {
        val spannableString = SpannableString(binding.termsTextView.text.toString())
        val termsClickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                launchBrowserIntent(getString(R.string.terms_url))
            }
        }
        val privacyClickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                launchBrowserIntent(getString(R.string.privacy_url))
            }
        }
        val clickableSpans: IntArray = resources.getIntArray(R.array.terms_clickable_spans)

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
                if (googleSignInIntent != null && googleSignInRequestCode != null) {
                    startActivityForResult(googleSignInIntent!!, googleSignInRequestCode!!)
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
        viewModel = ViewModelProvider(
            this,
            viewModelFactory
        ).get(LaunchFragmentViewModel::class.java)

        viewModel.silentSignInAndNotify()
    }

    private fun retryProviderInstall() {
        context?.let {
            ProviderInstaller.installIfNeededAsync(it, this)
        }
    }

    private fun startApp() {
        viewModel.setIsFirstSetUp(false)
        val action =
            LaunchFragmentDirections.actionNavigationLaunchToNavigationSubscriptions()
        view?.findNavController()?.navigate(action)
    }

    private fun launchBrowserIntent(url: String) {
        val webPage: Uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webPage)
        activity?.let {
            if (intent.resolveActivity(it.packageManager) != null) {
                startActivity(intent)
            } else {
                Snackbar.make(
                    binding.topLayout,
                    R.string.no_browser_error_message, Snackbar.LENGTH_SHORT
                ).show()
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

    override fun notifyProcessing() {

    }

    override fun onSilentSignInSuccess() {
        viewModel.getMatchingUserAndNotify()
    }

    override fun onSilentSignInFailed(googleSignInIntent: Intent, googleSignInRequestCode: Int) {
        this.googleSignInIntent = googleSignInIntent
        this.googleSignInRequestCode = googleSignInRequestCode
    }

    override fun onSignInSuccess() {
        viewModel.getMatchingUserAndNotify()
    }

    override fun onSignInFailed() {
        Snackbar.make(requireView(), "Failed to sign in", Snackbar.LENGTH_LONG).show()
    }

    override fun onUserFetchSuccess(user: User) {
        startApp()
    }

    override fun onUserFetchFailed() {
        Snackbar.make(requireView(), "Failed to retrieve user", Snackbar.LENGTH_LONG).show()
    }

    override fun onSignOutSuccess() {

    }

    override fun onSignOutFailed() {

    }
}