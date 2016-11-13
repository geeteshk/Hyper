package io.geeteshk.hyper.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.helper.Pref;
import io.geeteshk.hyper.helper.Theme;
import io.geeteshk.hyper.helper.Typefacer;

/**
 * Activity for application splash
 */
public class SplashActivity extends AppCompatActivity {

    /**
     * Layout to handle snackbars
     */
    CoordinatorLayout mLayout;

    /**
     * Method called when activity is created
     *
     * @param savedInstanceState previously stored state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Typefacer.setDefaultFont(getApplicationContext(), "SERIF", "fonts/Roboto-Medium.ttf");
        Typefacer.setDefaultFont(getApplicationContext(), "SANS_SERIF", "fonts/RobotoCondensed-Regular.ttf");
        Typefacer.setDefaultFont(getApplicationContext(), "NORMAL", "fonts/Consolas.ttf");
        Theme.setNavigationColor(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ImageView logo = (ImageView) findViewById(R.id.hyper_logo);
        TextView logoText = (TextView) findViewById(R.id.hyper_logo_text);
        logo.animate().alpha(1).setDuration(800);
        logoText.animate().alpha(1).setDuration(800);

        mLayout = (CoordinatorLayout) findViewById(R.id.splash_layout);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Class classTo = IntroActivity.class;
                if (Pref.get(SplashActivity.this, "intro_done", false)) {
                    classTo = MainActivity.class;
                }

                Intent intent = new Intent(SplashActivity.this, classTo);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }, 1000);
    }
}
