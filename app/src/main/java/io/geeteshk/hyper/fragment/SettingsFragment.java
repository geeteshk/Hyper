package io.geeteshk.hyper.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.SwitchCompat;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import io.geeteshk.hyper.EncryptActivity;
import io.geeteshk.hyper.MainActivity;
import io.geeteshk.hyper.R;
import io.geeteshk.hyper.util.PreferenceUtil;

/**
 * Fragment to edit settings
 */
public class SettingsFragment extends Fragment {

    TextView setPin;

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

        final SwitchCompat darkTheme = (SwitchCompat) rootView.findViewById(R.id.dark_theme);
        darkTheme.setChecked(PreferenceUtil.get(getActivity(), "dark_theme", false));
        darkTheme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.changeTheme((AppCompatActivity) getActivity(), isChecked);
            }
        });

        RelativeLayout darkThemeLayout = (RelativeLayout) rootView.findViewById(R.id.dark_theme_layout);
        darkThemeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                darkTheme.setChecked(!darkTheme.isChecked());
            }
        });

        setPin = (TextView) rootView.findViewById(R.id.set_pin);
        Log.i("PIN: ", PreferenceUtil.get(getActivity(), "pin", ""));
        if (!PreferenceUtil.get(getActivity(), "pin", "").equals("")) {
            setPin.setText("****");
        }

        RelativeLayout setPinLayout = (RelativeLayout) rootView.findViewById(R.id.set_pin_layout);
        setPinLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePin();
            }
        });

        final TextView autoSave = (TextView) rootView.findViewById(R.id.auto_save_freq_text);
        autoSave.setText(String.valueOf(PreferenceUtil.get(getActivity(), "auto_save_freq", 2)) + "s");
        AppCompatSeekBar seekBar = (AppCompatSeekBar) rootView.findViewById(R.id.auto_save_freq);
        seekBar.setProgress(PreferenceUtil.get(getActivity(), "auto_save_freq", 2) - 1);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                PreferenceUtil.store(getActivity(), "auto_save_freq", progress + 1);
                autoSave.setText(String.valueOf(progress + 1) + "s");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        return rootView;
    }

    private void updatePin() {
        if (PreferenceUtil.get(getActivity(), "pin", "").equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Please enter a new PIN");
            EditText editText = new EditText(getActivity());
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            final TextInputLayout layout = new TextInputLayout(getActivity());
            layout.addView(editText);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(60, 16, 60, 16);
            layout.setLayoutParams(params);
            builder.setView(layout);
            builder.setPositiveButton("ACCEPT", null);
            builder.setCancelable(false);
            final AppCompatDialog dialog = builder.create();
            dialog.show();

            Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String newPin = layout.getEditText().getText().toString();
                    if (newPin.length() != 4) {
                        layout.setError("The pin must consist only of 4 digits.");
                    } else {
                        setPin.setText("****");
                        PreferenceUtil.store(getActivity(), "pin", newPin);
                        dialog.dismiss();
                    }
                }
            });
        } else {
            Toast.makeText(getActivity(), "Please enter your current PIN.", Toast.LENGTH_SHORT).show();
            startActivityForResult(new Intent(getActivity(), EncryptActivity.class), 0);
        }
    }
}
