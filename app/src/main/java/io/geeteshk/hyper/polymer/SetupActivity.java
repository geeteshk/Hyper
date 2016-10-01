package io.geeteshk.hyper.polymer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;
import android.widget.TextView;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.helper.Decor;
import io.geeteshk.hyper.helper.Polymer;
import io.geeteshk.hyper.helper.Pref;

/**
 * Activity to setup polymer elements
 */
@SuppressLint("StaticFieldLeak")
public class SetupActivity extends AppCompatActivity {

    /**
     * Setup views
     */
    public static ProgressBar mProgressBar;
    public static TextView mProgressText;

    /**
     * Method called when activity is created
     *
     * @param savedInstanceState previously stored state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Pref.get(this, "dark_theme", false)) {
            setTheme(R.style.Hyper_Dark);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        Decor.setStatusBarColor(this, -1);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressText = (TextView) findViewById(R.id.progress_text);

        mProgressText.setText(R.string.creating_files);
        mProgressBar.setIndeterminate(true);
        Polymer.addPackages(mProgressBar, mProgressText, ElementsHolder.getInstance().getProject(), this);
    }
}

