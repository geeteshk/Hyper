package io.geeteshk.hyper.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import io.geeteshk.hyper.MainActivity;
import io.geeteshk.hyper.R;
import io.geeteshk.hyper.util.PreferenceUtil;

/**
 * Fragment to edit settings
 */
public class SettingsFragment extends Fragment {

    /**
     * Default empty constructor
     */
    public SettingsFragment() {
    }

    /**
     * Method used to inflate and setup view
     *
     * @param inflater           used to inflate layout
     * @param container          parent view
     * @param savedInstanceState restores state onResume
     * @return fragment view that is created
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        SwitchCompat switchCompat = (SwitchCompat) rootView.findViewById(R.id.dark_theme);
        switchCompat.setChecked(PreferenceUtil.get(getActivity(), "dark_theme", false));
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.changeTheme((AppCompatActivity) getActivity(), isChecked);
            }
        });

        return rootView;
    }
}
