package com.orlove101.android.mvvmnewsapp.ui.fragments

import android.os.Bundle
import androidx.preference.*
import com.orlove101.android.mvvmnewsapp.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        findPreference<SwitchPreferenceCompat>("theme")?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue == true) {

            } else {

            }
            true
        }
    }
}

private const val TAG = "SettingsFragment"