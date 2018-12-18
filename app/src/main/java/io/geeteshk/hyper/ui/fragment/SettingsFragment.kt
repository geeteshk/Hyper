/*
 * Copyright 2016 Geetesh Kalakoti <kalakotig@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.geeteshk.hyper.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import io.geeteshk.hyper.R
import io.geeteshk.hyper.ui.activity.SettingsActivity
import io.geeteshk.hyper.util.Constants
import io.geeteshk.hyper.util.Prefs.defaultPrefs
import io.geeteshk.hyper.util.Prefs.set
import org.apache.commons.io.FileUtils
import timber.log.Timber
import java.io.File
import java.io.IOException

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val prefs = defaultPrefs(activity!!)
        val darkTheme = preferenceManager.findPreference<SwitchPreference>("dark_theme")
        darkTheme.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, o ->
            prefs["dark_theme"] = o
            val intent = Intent(activity, SettingsActivity::class.java)
            startActivity(intent)
            activity!!.finish()
            true
        }

        val darkThemeEditor = preferenceManager.findPreference<SwitchPreference>("dark_theme_editor")
        darkThemeEditor.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, o ->
            prefs["dark_theme_editor"] = o
            true
        }

        val lineNumbers = preferenceManager.findPreference<SwitchPreference>("show_line_numbers")
        lineNumbers.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, o ->
            prefs["show_line_numbers"] = o
            true
        }

        val factoryReset = preferenceManager.findPreference<Preference>("factory_reset")
        val files = File(Constants.HYPER_ROOT).list()
        factoryReset!!.isEnabled = files != null && files.isNotEmpty()
        factoryReset.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            AlertDialog.Builder(activity!!)
                    .setTitle("Factory Reset")
                    .setMessage("Are you sure you want to delete ALL of your projects? This change cannot be undone!")
                    .setPositiveButton("RESET") { _, _ ->
                        try {
                            FileUtils.cleanDirectory(File(Constants.HYPER_ROOT))
                            factoryReset.isEnabled = false
                        } catch (e: IOException) {
                            Timber.e(e)
                        }
                    }
                    .setNegativeButton("CANCEL", null)
                    .show()

            true
        }

        val notices = preferenceManager.findPreference<Preference>("notices")
        notices!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            startActivity(Intent(activity, OssLicensesMenuActivity::class.java))
            true
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
