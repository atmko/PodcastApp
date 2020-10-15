package com.atmko.skiptoit.launch

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.atmko.skiptoit.MasterActivity
import com.atmko.skiptoit.R
import com.atmko.skiptoit.common.ManagerViewModel
import com.atmko.skiptoit.common.ViewModelFactory
import com.atmko.skiptoit.common.views.BaseActivity
import com.atmko.skiptoit.databinding.ActivityLaunchBinding
import com.atmko.skiptoit.model.User
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.security.ProviderInstaller
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

const val IS_FIRST_SETUP_KEY = "is_first_set_up"
private const val ERROR_DIALOG_REQUEST_CODE = 1

class LaunchActivity : BaseActivity(),
    ProviderInstaller.ProviderInstallListener,
    ManagerViewModel.Listener {

    private lateinit var binding: ActivityLaunchBinding

    private var isProviderUpdated = false
    private var googleSignInIntent: Intent? = null
    private var googleSignInRequestCode: Int? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: LaunchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaunchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getPresentationComponent().inject(this)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.let { viewModel.onRequestResultReceived(requestCode, resultCode, it) }
    }

    override fun onProviderInstallFailed(errorCode: Int, recoveryIntent: Intent?) {
        val availability = GoogleApiAvailability.getInstance()
        if (availability.isUserResolvableError(errorCode)) {
            // Recoverable error. Show a dialog prompting the user to
            // install/update/enable Google Play services.
            availability.showErrorDialogFragment(
                this,
                errorCode,
                ERROR_DIALOG_REQUEST_CODE
            ) {
                // The user chose not to take the recovery action
                onProviderInstallerNotAvailable()
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
        ).get(LaunchViewModel::class.java)

        viewModel.silentSignInAndNotify()
    }

    private fun retryProviderInstall() {
        ProviderInstaller.installIfNeededAsync(this, this)
    }

    private fun startApp() {
        viewModel.setIsFirstSetUp(false)

        val masterActivityIntent = Intent(applicationContext, MasterActivity::class.java)
        startActivity(masterActivityIntent)
        finish()
    }

    private fun launchBrowserIntent(url: String) {
        val webPage: Uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webPage)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Snackbar.make(
                binding.topLayout,
                R.string.no_browser_error_message, Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun onProviderInstallerNotAvailable() {
        Toast.makeText(
            this, getString(R.string.couldnt_update_google_play_services),
            Toast.LENGTH_LONG
        ).show()
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
        Snackbar.make(binding.topLayout, "Failed to sign in", Snackbar.LENGTH_LONG).show()
    }

    override fun onUserFetchSuccess(user: User) {
        startApp()
    }

    override fun onUserFetchFailed() {
        Snackbar.make(binding.topLayout, "Failed to retrieve user", Snackbar.LENGTH_LONG).show()
    }

    override fun onSignOutSuccess() {

    }

    override fun onSignOutFailed() {

    }
}