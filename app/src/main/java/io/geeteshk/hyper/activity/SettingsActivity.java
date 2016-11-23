package io.geeteshk.hyper.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.fragment.SettingsFragment;
import io.geeteshk.hyper.helper.Theme;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Theme.getThemeInt(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_fragment, new SettingsFragment())
                .commit();
    }
}
