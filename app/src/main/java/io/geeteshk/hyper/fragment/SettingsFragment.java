package io.geeteshk.hyper.fragment;

import android.annotation.SuppressLint;
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

import com.google.firebase.auth.FirebaseAuth;

import io.geeteshk.hyper.activity.AccountActivity;
import io.geeteshk.hyper.activity.EncryptActivity;
import io.geeteshk.hyper.activity.MainActivity;
import io.geeteshk.hyper.R;
import io.geeteshk.hyper.helper.Firebase;
import io.geeteshk.hyper.helper.FirstAid;
import io.geeteshk.hyper.helper.Pref;

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
    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        TextView firebaseAccount = (TextView) rootView.findViewById(R.id.firebase_account);
        firebaseAccount.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        RelativeLayout firebaseAccountLayout = (RelativeLayout) rootView.findViewById(R.id.firebase_account_layout);
        firebaseAccountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AccountActivity.class));
            }
        });

        final SwitchCompat darkTheme = (SwitchCompat) rootView.findViewById(R.id.dark_theme);
        darkTheme.setChecked(Pref.get(getActivity(), "dark_theme", false));
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
        Log.i("PIN: ", Pref.get(getActivity(), "pin", ""));
        if (!Pref.get(getActivity(), "pin", "").equals("")) {
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
        autoSave.setText(String.valueOf(Pref.get(getActivity(), "auto_save_freq", 2)) + "s");
        AppCompatSeekBar seekBar = (AppCompatSeekBar) rootView.findViewById(R.id.auto_save_freq);
        seekBar.setProgress(Pref.get(getActivity(), "auto_save_freq", 2) - 1);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Pref.store(getActivity(), "auto_save_freq", progress + 1);
                autoSave.setText(String.valueOf(progress + 1) + "s");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        RelativeLayout repairLayout = (RelativeLayout) rootView.findViewById(R.id.repair_layout);
        repairLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirstAid.repairAll(getActivity());
            }
        });

        RelativeLayout disableFileLayout = (RelativeLayout) rootView.findViewById(R.id.disable_file_ending_warn_layout);
        final SwitchCompat disableFile = (SwitchCompat) rootView.findViewById(R.id.disable_file_ending_warn);
        disableFile.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Pref.store(getActivity(), "show_toast_file_ending", b);
            }
        });

        disableFileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disableFile.setChecked(!disableFile.isChecked());
            }
        });

        return rootView;
    }

    private void updatePin() {
        if (Pref.get(getActivity(), "pin", "").equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.pin_request);
            EditText editText = new EditText(getActivity());
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            final TextInputLayout layout = new TextInputLayout(getActivity());
            layout.addView(editText);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(60, 16, 60, 16);
            layout.setLayoutParams(params);
            builder.setView(layout);
            builder.setPositiveButton(R.string.accept, null);
            builder.setCancelable(false);
            final AppCompatDialog dialog = builder.create();
            dialog.show();

            Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    assert layout.getEditText() != null;
                    String newPin = layout.getEditText().getText().toString();
                    if (newPin.length() != 4) {
                        layout.setError(getString(R.string.error_pin));
                    } else {
                        setPin.setText("****");
                        Pref.store(getActivity(), "pin", newPin);
                        dialog.dismiss();
                    }
                }
            });
        } else {
            Toast.makeText(getActivity(), R.string.current_pin, Toast.LENGTH_SHORT).show();
            startActivityForResult(new Intent(getActivity(), EncryptActivity.class), 0);
        }
    }
}
