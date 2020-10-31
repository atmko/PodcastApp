package com.atmko.skiptoit.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.atmko.skiptoit.BuildConfig
import com.atmko.skiptoit.R
import com.google.android.material.snackbar.Snackbar

class AboutFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_settings)

        val aboutAppPreference: Preference? =
            findPreference(getString(R.string.settings_key_about_app))
        if (aboutAppPreference != null) {
            aboutAppPreference.summary =
                getString(R.string.settings_about_app_summary, BuildConfig.VERSION_NAME)
        }

        val contactPreference: Preference? =
            findPreference(getString(R.string.settings_key_support))
        if (contactPreference != null) {
            contactPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                val sentTo =
                    arrayOfNulls<String>(1)
                sentTo[0] = getString(R.string.support_email_address)
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.data = Uri.parse("mailto:") // only email apps should handle this
                intent.putExtra(Intent.EXTRA_EMAIL, sentTo)
                activity?.let {
                    if (intent.resolveActivity(it.packageManager) != null) {
                        startActivity(intent)
                    }
                }
                true
            }
        }

        val privacyPreference: Preference? =
            findPreference(getString(R.string.settings_key_privacy))
        if (privacyPreference != null) {
            privacyPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                launchBrowserIntent(getString(R.string.privacy_url))
                true
            }
        }

        val termsPreference: Preference? =
            findPreference(getString(R.string.settings_key_terms))
        if (termsPreference != null) {
            termsPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                launchBrowserIntent(getString(R.string.terms_url))
                true
            }
        }
    }

    private fun launchBrowserIntent(url: String) {
        val webPage = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webPage)
        activity?.let {
            if (intent.resolveActivity(it.packageManager) != null) {
                startActivity(intent)
            } else {
                Snackbar.make(
                    requireView(),
                    getString(R.string.no_browser_error_message),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }
}