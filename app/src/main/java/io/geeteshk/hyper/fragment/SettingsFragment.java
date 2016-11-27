package io.geeteshk.hyper.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pavelsikun.seekbarpreference.SeekBarPreference;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.License;
import de.psdev.licensesdialog.model.Notice;
import io.geeteshk.hyper.R;
import io.geeteshk.hyper.activity.SettingsActivity;
import io.geeteshk.hyper.helper.Constants;
import io.geeteshk.hyper.helper.Pref;
import io.geeteshk.hyper.helper.Theme;

public class SettingsFragment extends PreferenceFragment {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final SwitchPreference darkTheme = (SwitchPreference) getPreferenceManager().findPreference("dark_theme");
        darkTheme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                Pref.store(getActivity(), "dark_theme", (boolean) o);
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                getActivity().finish();
                return true;
            }
        });

        final SwitchPreference darkThemeEditor = (SwitchPreference) getPreferenceManager().findPreference("dark_theme_editor");
        darkThemeEditor.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                Pref.store(getActivity(), "dark_theme_editor", (boolean) o);
                return true;
            }
        });

        SeekBarPreference autoSaveFreq = (SeekBarPreference) getPreferenceManager().findPreference("auto_save_freq");
        autoSaveFreq.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                Pref.store(getActivity(), "auto_save_freq", ((SeekBarPreference) preference).getCurrentValue());
                return true;
            }
        });

        final SwitchPreference lineNumbers = (SwitchPreference) getPreferenceManager().findPreference("show_line_numbers");
        lineNumbers.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                Pref.store(getActivity(), "show_line_numbers", (boolean) o);
                return true;
            }
        });

        final Preference factoryReset = getPreferenceManager().findPreference("factory_reset");
        factoryReset.setEnabled(new File(Constants.HYPER_ROOT).list().length > 0);
        factoryReset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder resetBuilder = new AlertDialog.Builder(getActivity());
                resetBuilder.setTitle("Factory Reset");
                resetBuilder.setMessage("Are you sure you want to delete ALL of your projects? This change cannot be undone!");
                resetBuilder.setPositiveButton("RESET", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            FileUtils.cleanDirectory(new File(Constants.HYPER_ROOT));
                            factoryReset.setEnabled(false);
                        } catch (IOException e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                });

                resetBuilder.setNegativeButton("CANCEL", null);
                resetBuilder.create().show();
                return true;
            }
        });

        Preference notices  = getPreferenceManager().findPreference("notices");
        notices.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new LicensesDialog.Builder(getActivity())
                        .setNotices(R.raw.notices)
                        .setThemeResourceId(Theme.getThemeInt(getActivity()))
                        .build()
                        .show();

                return true;
            }
        });

        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
