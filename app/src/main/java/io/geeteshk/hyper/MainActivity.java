package io.geeteshk.hyper;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.io.File;

import io.geeteshk.hyper.fragment.ContributeFragment;
import io.geeteshk.hyper.fragment.CreateFragment;
import io.geeteshk.hyper.fragment.DonateFragment;
import io.geeteshk.hyper.fragment.DrawerFragment;
import io.geeteshk.hyper.fragment.HelpFragment;
import io.geeteshk.hyper.fragment.ImproveFragment;
import io.geeteshk.hyper.fragment.PilotFragment;
import io.geeteshk.hyper.fragment.SettingsFragment;
import io.geeteshk.hyper.util.PreferenceUtil;
import io.geeteshk.hyper.util.TypefaceUtil;

/**
 * Main activity to show all main content
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Layout that holds NavigationDrawer
     */
    private static DrawerLayout mDrawerLayout;
    /**
     * Toolbar object for activity
     */
    private static Toolbar mToolbar;
    /**
     * Items to be listed in drawer
     */
    private static String[] mItems;
    /**
     * Drawer toggle displayed on toolbar
     */
    private ActionBarDrawerToggle mDrawerToggle;

    /**
     * Used to select items in drawer
     *
     * @param context  used to store in preferences
     * @param manager  used to update fragments
     * @param position of selected item
     */
    public static void update(Context context, FragmentManager manager, int position) {
        mDrawerLayout.closeDrawer(GravityCompat.START);
        Fragment fragment = null;

        switch (position) {
            case 0:
                fragment = new CreateFragment();
                break;
            case 1:
                fragment = new ImproveFragment();
                break;
            case 2:
                fragment = new PilotFragment();
                break;
            case 3:
                fragment = new ContributeFragment();
                break;
            case 4:
                fragment = new DonateFragment();
                break;
            case 5:
                fragment = new SettingsFragment();
                break;
            case 6:
                fragment = new HelpFragment();
                break;
        }

        manager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();

        PreferenceUtil.store(context, "last_fragment", position);
        mToolbar.setTitle(mItems[position]);
    }

    /**
     * Used to change theme of application
     *
     * @param activity that is being changed
     * @param dark     theme
     */
    public static void changeTheme(AppCompatActivity activity, boolean dark) {
        PreferenceUtil.store(activity, "dark_theme", dark);
        activity.recreate();
    }

    /**
     * Called when the activity is first created
     *
     * @param savedInstanceState restore when onResume is called
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        File projectDir = new File(Environment.getExternalStorageDirectory() + File.separator + "Hyper");
        if (!projectDir.exists()) {
            projectDir.mkdir();
        }

        TypefaceUtil.setDefaultFont(getApplicationContext(), "SERIF", "fonts/Roboto-Medium.ttf");
        TypefaceUtil.setDefaultFont(getApplicationContext(), "MONOSPACE", "fonts/RobotoCondensed-BoldItalic.ttf");
        TypefaceUtil.setDefaultFont(getApplicationContext(), "SANS_SERIF", "fonts/RobotoCondensed-Regular.ttf");

        if (PreferenceUtil.get(this, "dark_theme", false)) {
            setTheme(R.style.Hyper_Dark);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mItems = getResources().getStringArray(R.array.drawer_items);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        setSupportActionBar(mToolbar);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.action_drawer_open, R.string.action_drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        update(this, getSupportFragmentManager(), PreferenceUtil.get(this, "last_fragment", 0));
        DrawerFragment.select(this, PreferenceUtil.get(this, "last_fragment", 0));
    }

    /**
     * Called after activity is created
     *
     * @param savedInstanceState restored when onResume is called
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    /**
     * Called when config is changed
     *
     * @param newConfig new configuration
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Called when activity is resumed
     */
    @Override
    protected void onResume() {
        super.onResume();
        mToolbar.setTitle(mItems[PreferenceUtil.get(this, "last_fragment", 0)]);
    }

    /**
     * Called when back button is pressed
     */
    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
            return;
        }

        super.onBackPressed();
    }
}
