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
import androidx.fragment.app.Fragment
import com.atmko.skiptoit.R
import com.atmko.skiptoit.databinding.FragmentLaunchBinding
import com.atmko.skiptoit.viewmodel.MasterActivityViewModel
import com.google.android.material.snackbar.Snackbar

const val LAUNCH_FRAGMENT_KEY = "launch_fragment"
const val IS_FIRST_SETUP_KEY = "is_first_set_up"

class LaunchFragment : Fragment(),
    MasterActivityViewModel.ViewNavigation {

    private var _binding: FragmentLaunchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentLaunchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        configureViews()
        configureValues()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
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
            Snackbar.make(binding.topLayout,
                "Not yet implemented", Snackbar.LENGTH_SHORT).show()
        }

        binding.guestContinue.setOnClickListener {
            Snackbar.make(binding.topLayout,
                "Not yet implemented", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun configureValues() {
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
}