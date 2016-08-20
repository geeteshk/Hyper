package io.geeteshk.hyper.polymer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;
import android.widget.TextView;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.util.PolymerUtil;
import io.geeteshk.hyper.util.PreferenceUtil;

public class SetupActivity extends AppCompatActivity {

    public static ProgressBar mProgressBar;
    public static TextView mProgressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (PreferenceUtil.get(this, "dark_theme", false)) {
            setTheme(R.style.Hyper_Dark);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressText = (TextView) findViewById(R.id.progress_text);

        mProgressText.setText("Creating project files...");
        mProgressBar.setIndeterminate(true);
        PolymerUtil.addPackages(mProgressBar, mProgressText, ElementsHolder.getInstance().getProject(), this);
    }
}

