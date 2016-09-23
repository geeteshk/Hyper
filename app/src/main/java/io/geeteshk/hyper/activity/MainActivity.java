package io.geeteshk.hyper.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.fragment.AssistFragment;
import io.geeteshk.hyper.fragment.CreateFragment;
import io.geeteshk.hyper.fragment.HelpFragment;
import io.geeteshk.hyper.fragment.ImproveFragment;
import io.geeteshk.hyper.fragment.PilotFragment;
import io.geeteshk.hyper.fragment.SettingsFragment;
import io.geeteshk.hyper.helper.Constants;
import io.geeteshk.hyper.helper.Firebase;
import io.geeteshk.hyper.helper.Pref;
import io.geeteshk.hyper.helper.Project;

/**
 * Main activity to show all main content
 */
@SuppressLint("StaticFieldLeak")
public class MainActivity extends AppCompatActivity {

    private static Context mContext;
    private static FragmentManager mManager;

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
    private static NavigationView mDrawer;
    /**
     * Drawer toggle displayed on toolbar
     */
    private ActionBarDrawerToggle mDrawerToggle;

    FirebaseAuth mAuth;
    FirebaseStorage mStorage;

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
                fragment = new AssistFragment();
                break;
            case 4:
                fragment = new SettingsFragment();
                break;
            case 5:
                fragment = new HelpFragment();
                break;
        }

        if (context == null) {
            context = mContext;
        }

        if (manager == null) {
            manager = mManager;
        }

        manager.beginTransaction()
                .replace(R.id.container, fragment)
                .commitAllowingStateLoss();

        Pref.store(context, "last_fragment", position);
        mToolbar.setTitle(mItems[position]);
        mDrawer.setCheckedItem(mDrawer.getMenu().getItem(position).getItemId());
    }

    /**
     * Used to change theme of application
     *
     * @param activity that is being changed
     * @param dark     theme
     */
    public static void changeTheme(AppCompatActivity activity, boolean dark) {
        Pref.store(activity, "dark_theme", dark);
        activity.recreate();
    }

    FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }
    };

    /**
     * Called when the activity is first created
     *
     * @param savedInstanceState restore when onResume is called
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();

        if (Pref.get(this, "dark_theme", false)) {
            setTheme(R.style.Hyper_Dark);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mItems = getResources().getStringArray(R.array.drawer_items);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        setSupportActionBar(mToolbar);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.action_drawer_open, R.string.action_drawer_close);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mDrawer = (NavigationView) findViewById(R.id.drawer);
        final List<MenuItem> items = new ArrayList<>();
        Menu menu = mDrawer.getMenu();

        for (int i = 0; i < menu.size(); i++) {
            items.add(menu.getItem(i));
        }

        mDrawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (!item.isChecked()) {
                    item.setChecked(true);
                }

                mDrawerLayout.closeDrawers();
                update(MainActivity.this, getSupportFragmentManager(), items.indexOf(item));
                return true;
            }
        });

        mContext = this;
        mManager = getSupportFragmentManager();
        update(this, getSupportFragmentManager(), Pref.get(this, "last_fragment", 0));
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authListener != null) {
            mAuth.removeAuthStateListener(authListener);
        }
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
        mToolbar.setTitle(mItems[Pref.get(this, "last_fragment", 0)]);
    }

    /**
     * Called when back button is pressed
     */
    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case 1337:
                MainActivity.update(this, getSupportFragmentManager(), 4);
                break;
            case 1:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.new_pin);
                EditText editText = new EditText(MainActivity.this);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setPadding(60, 14, 60, 24);
                final TextInputLayout layout = new TextInputLayout(MainActivity.this);
                layout.addView(editText);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(60, 16, 60, 16);
                layout.setLayoutParams(params);
                builder.setView(layout);
                builder.setPositiveButton(R.string.accept, null);
                final AppCompatDialog dialog = builder.create();
                dialog.show();

                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        assert layout.getEditText() != null;
                        String newPin = layout.getEditText().getText().toString();
                        if (newPin.length() != 4) {
                            layout.setError(getString(R.string.pin_four_digits));
                        } else {
                            Pref.store(MainActivity.this, "pin", newPin);
                            dialog.dismiss();
                        }
                    }
                });
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
