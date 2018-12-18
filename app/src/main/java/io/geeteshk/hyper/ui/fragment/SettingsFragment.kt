package io.geeteshk.hyper.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.SwitchPreference
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class SettingsFragment : PreferenceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.settings)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val prefs = defaultPrefs(activity)
        val darkTheme = preferenceManager.findPreference("dark_theme") as SwitchPreference
        darkTheme.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, o ->
            prefs["dark_theme"] = o
            val intent = Intent(activity, SettingsActivity::class.java)
            startActivity(intent)
            activity.finish()
            true
        }

        val darkThemeEditor = preferenceManager.findPreference("dark_theme_editor") as SwitchPreference
        darkThemeEditor.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, o ->
            prefs["dark_theme_editor"] = o
            true
        }

        val lineNumbers = preferenceManager.findPreference("show_line_numbers") as SwitchPreference
        lineNumbers.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, o ->
            prefs["show_line_numbers"] = o
            true
        }

        val factoryReset = preferenceManager.findPreference("factory_reset")
        val files = File(Constants.HYPER_ROOT).list()
        factoryReset!!.isEnabled = files != null && files.isNotEmpty()
        factoryReset.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            AlertDialog.Builder(activity)
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

        val notices = preferenceManager.findPreference("notices")
        notices!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            startActivity(Intent(activity, OssLicensesMenuActivity::class.java))
            true
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    companion object {

        private val TAG = SettingsFragment::class.java.simpleName
    }
}
