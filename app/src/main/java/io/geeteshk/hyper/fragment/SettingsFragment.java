package io.geeteshk.hyper.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.License;
import de.psdev.licensesdialog.model.Notice;
import io.geeteshk.hyper.R;
import io.geeteshk.hyper.activity.SettingsActivity;
import io.geeteshk.hyper.helper.Pref;
import io.geeteshk.hyper.helper.Theme;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SwitchPreference darkTheme = (SwitchPreference) getPreferenceManager().findPreference("dark_theme");
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

        SwitchPreference darkThemeEditor = (SwitchPreference) getPreferenceManager().findPreference("dark_theme_editor");
        darkThemeEditor.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                Pref.store(getActivity(), "dark_theme_editor", (boolean) o);
                return true;
            }
        });

        Preference preference = getPreferenceManager().findPreference("notices");
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
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
