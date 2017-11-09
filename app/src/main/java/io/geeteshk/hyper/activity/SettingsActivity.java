package io.geeteshk.hyper.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.geeteshk.hyper.R;
import io.geeteshk.hyper.fragment.SettingsFragment;
import io.geeteshk.hyper.helper.Styles;

public class SettingsActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Styles.getThemeInt(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_fragment, new SettingsFragment())
                .commit();
    }
}
