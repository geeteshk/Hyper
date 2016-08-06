package io.geeteshk.hyper;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.geeteshk.hyper.adapter.FileAdapter;
import io.geeteshk.hyper.helper.HyperDrive;
import io.geeteshk.hyper.util.DecorUtil;
import io.geeteshk.hyper.util.JsonUtil;
import io.geeteshk.hyper.util.NetworkUtil;
import io.geeteshk.hyper.util.PreferenceUtil;
import io.geeteshk.hyper.util.ProjectUtil;
import io.geeteshk.hyper.widget.KeyboardDetectorLayout;

/**
 * Activity to list projects
 */
public class ProjectActivity extends AppCompatActivity {

    private static final String TAG = ProjectActivity.class.getSimpleName();

    /**
     * Intent code to import image
     */
    private static final int IMPORT_IMAGE = 101;

    /**
     * Intent code to import font
     */
    private static final int IMPORT_FONT = 102;

    /**
     * Intent code to import css
     */
    private static final int IMPORT_CSS = 103;

    /**
     * Intent code to import js
     */
    private static final int IMPORT_JS = 104;

    private List<String> mFiles;
    private FileAdapter mAdapter;
    private ViewPager mPager;
    private TabLayout mTabStrip;

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private NavigationView mDrawer;

    private String mProject;

    /**
     * Called when the activity is created
     *
     * @param savedInstanceState restored when onResume is called
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mProject = getIntent().getStringExtra("project");
        if (PreferenceUtil.get(this, "dark_theme", false)) {
            setTheme(R.style.Hyper_Dark);
        }

        NetworkUtil.setDrive(new HyperDrive(mProject));
        super.onCreate(savedInstanceState);

        try {
            NetworkUtil.getDrive().start();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }

        KeyboardDetectorLayout layout = new KeyboardDetectorLayout(this, null);
        setContentView(layout);

        float[] hsv = new float[3];
        int color = Color.parseColor(JsonUtil.getProjectProperty(mProject, "color"));
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        color = Color.HSVToColor(hsv);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(color);
        }

        RelativeLayout projectLayout = (RelativeLayout) findViewById(R.id.project_layout_snack);
        if (PreferenceUtil.get(this, "pin", "").equals("")) {
            assert projectLayout != null;
            Snackbar snackbar = Snackbar.make(projectLayout, R.string.pin_snack_bar, Snackbar.LENGTH_LONG)
                    .setAction(R.string.set_pin, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setResult(1337);
                            finish();
                        }
                    });
            snackbar.show();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (PreferenceUtil.get(this, "dark_theme", false)) {
            assert toolbar != null;
            toolbar.setPopupTheme(R.style.Hyper_Dark);
        }

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(mProject);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(JsonUtil.getProjectProperty(mProject, "color"))));
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.action_drawer_open, R.string.action_drawer_close);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mDrawer = (NavigationView) findViewById(R.id.drawer);
        setupMenu(mProject, null);

        View headerView = mDrawer.getHeaderView(0);
        RelativeLayout headerLayout = (RelativeLayout) headerView.findViewById(R.id.header_background);
        ImageView headerIcon = (ImageView) headerView.findViewById(R.id.header_icon);
        TextView headerTitle = (TextView) headerView.findViewById(R.id.header_title);
        TextView headerDesc = (TextView) headerView.findViewById(R.id.header_desc);

        headerLayout.setBackgroundColor(Color.parseColor(JsonUtil.getProjectProperty(mProject, "color")));
        headerIcon.setImageBitmap(ProjectUtil.getFavicon(mProject));
        headerTitle.setText(JsonUtil.getProjectProperty(mProject, "name"));
        headerDesc.setText(JsonUtil.getProjectProperty(mProject, "description"));

        mDrawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                if (!item.isChecked()) {
                    item.setChecked(true);
                }

                String file;
                if (item.getIntent() != null) {
                    file = item.getIntent().getStringExtra("location") + "/" + item.getTitle();
                } else {
                    file = item.getTitle().toString();
                }

                if (mFiles.contains(file)) {
                    mPager.setCurrentItem(mFiles.indexOf(file));
                } else {
                    if (!ProjectUtil.isBinaryFile(new File(Constants.HYPER_ROOT + File.separator + mProject + File.separator + file))) {
                        mFiles.add(file);
                        refreshMenu();
                        mPager.setCurrentItem(mFiles.indexOf(file));
                    } else {
                        Toast.makeText(ProjectActivity.this, R.string.not_text_file, Toast.LENGTH_SHORT).show();
                    }
                }

                mDrawerLayout.closeDrawers();
                return true;
            }
        });

        mFiles = new ArrayList<>();
        mFiles.add("index.html");
        mFiles.add("css/style.css");
        mFiles.add("js/main.js");

        mAdapter = new FileAdapter(getSupportFragmentManager(), mProject, mFiles);
        mPager = (ViewPager) findViewById(R.id.pager);
        if (mPager != null) {
            mPager.setAdapter(mAdapter);
        }

        mTabStrip = (TabLayout) findViewById(R.id.tabs);
        assert mTabStrip != null;
        mTabStrip.setupWithViewPager(mPager);
        mTabStrip.setBackgroundColor(Color.parseColor(JsonUtil.getProjectProperty(mProject, "color")));
        mTabStrip.setSelectedTabIndicatorColor(getComplementaryColor(Color.parseColor(JsonUtil.getProjectProperty(mProject, "color"))));

        int newColor = Color.parseColor(JsonUtil.getProjectProperty(mProject, "color"));
        if ((Color.red(newColor) * 0.299 + Color.green(newColor) * 0.587 + Color.blue(newColor) * 0.114) > 186) {
            getSupportActionBar().setTitle((Html.fromHtml("<font color=\"#000000\">" + mProject + "</font>")));
            mTabStrip.setTabTextColors(0x80000000, 0xFF000000);
            PorterDuffColorFilter filter = new PorterDuffColorFilter(0xFF000000, PorterDuff.Mode.MULTIPLY);
            DecorUtil.setOverflowButtonColor(ProjectActivity.this, filter);
            headerTitle.setTextColor(0xff000000);
            headerDesc.setTextColor(0xff000000);
            mDrawerToggle.setDrawerIndicatorEnabled(false);
            toolbar.setNavigationIcon(R.drawable.ic_action_navigation_menu);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
            });
        } else {
            getSupportActionBar().setTitle((Html.fromHtml("<font color=\"#FFFFFF\">" + mProject + "</font>")));
            mTabStrip.setTabTextColors(0x80FFFFFF, 0xFFFFFFFF);
        }

        if (Build.VERSION.SDK_INT >= 21) {
            ActivityManager.TaskDescription description = new ActivityManager.TaskDescription(mProject, ProjectUtil.getFavicon(mProject), Color.parseColor(JsonUtil.getProjectProperty(mProject, "color")));
            this.setTaskDescription(description);
        }
    }

    private void setupMenu(String project, @Nullable SubMenu menu) {
        File projectDir = new File(Constants.HYPER_ROOT + File.separator + project);
        File[] files = projectDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                if (menu == null) {
                    setupMenu(project + File.separator + file.getName(), mDrawer.getMenu().addSubMenu(file.getName()));
                } else {
                    setupMenu(project + File.separator + file.getName(), menu.addSubMenu(file.getName()));
                }
            } else {
                if (!file.getName().endsWith(".hyper")) {
                    if (menu == null) {
                        mDrawer.getMenu().add(file.getName());
                    } else {
                        MenuItem item = menu.add(file.getName());
                        Intent intent = new Intent();
                        intent.putExtra("location", menu.getItem().getTitle());
                        item.setIntent(intent);
                    }
                }
            }
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

    private int getComplementaryColor(int colorToInvert) {
        float[] hsv = new float[3];
        Color.RGBToHSV(Color.red(colorToInvert), Color.green(colorToInvert),
                Color.blue(colorToInvert), hsv);
        hsv[0] = (hsv[0] + 180) % 360;
        return Color.HSVToColor(hsv);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (NetworkUtil.getDrive() != null) {
            NetworkUtil.getDrive().stop();
        }
    }

    /**
     * Called when menu is created
     *
     * @param menu object that holds menu
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_project, menu);
        return true;
    }

    /**
     * Called when menu item is selected
     *
     * @param item selected menu item
     * @return true if handled
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_run:
                Intent runIntent = new Intent(ProjectActivity.this, WebActivity.class);

                if (NetworkUtil.getDrive().wasStarted() && NetworkUtil.getDrive().isAlive() && NetworkUtil.getIpAddress() != null) {
                    runIntent.putExtra("url", "http:///" + NetworkUtil.getIpAddress() + ":8080");
                } else {
                    runIntent.putExtra("url", "file:///" + Constants.HYPER_ROOT + File.separator + mProject + File.separator + "index.html");
                }

                runIntent.putExtra("name", mProject);
                startActivity(runIntent);
                return true;
            case R.id.action_import_image:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, IMPORT_IMAGE);
                }
                return true;
            case R.id.action_import_font:
                Intent fontIntent = new Intent(Intent.ACTION_GET_CONTENT);
                fontIntent.setType("file/*");
                if (fontIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(fontIntent, IMPORT_FONT);
                }
                return true;
            case R.id.action_import_css:
                Intent cssIntent = new Intent(Intent.ACTION_GET_CONTENT);
                cssIntent.setType("text/css");
                if (cssIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(cssIntent, IMPORT_CSS);
                }
                return true;
            case R.id.action_import_js:
                Intent jsIntent = new Intent(Intent.ACTION_GET_CONTENT);
                jsIntent.setType("text/javascript");
                if (jsIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(jsIntent, IMPORT_JS);
                }
                return true;
            case R.id.action_create_html:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.new_not_java);
                final EditText editText = new EditText(this);
                editText.setHint(R.string.resource_name);
                builder.setView(editText);
                builder.setCancelable(false);
                builder.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!editText.getText().toString().isEmpty() && ProjectUtil.createFile(mProject, editText.getText().toString() + ".html", ProjectUtil.INDEX.replace("@name", JsonUtil.getProjectProperty(mProject, "name")).replace("author", JsonUtil.getProjectProperty(mProject, "author")).replace("@description", JsonUtil.getProjectProperty(mProject, "description")).replace("@keywords", JsonUtil.getProjectProperty(mProject, "keywords")).replace("@color", JsonUtil.getProjectProperty(mProject, "color")))) {
                            Toast.makeText(ProjectActivity.this, R.string.file_success, Toast.LENGTH_SHORT).show();
                            mFiles.add(editText.getText().toString());
                            refreshMenu();
                            mPager.setCurrentItem(mFiles.indexOf(editText.getText().toString()));
                        } else {
                            Toast.makeText(ProjectActivity.this, R.string.file_fail, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AppCompatDialog dialog = builder.create();
                if (PreferenceUtil.get(ProjectActivity.this, "show_toast_file_ending", true))
                    showToast(true);
                dialog.show();
                return true;
            case R.id.action_create_css:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                builder2.setTitle(R.string.new_not_java);
                final EditText editText2 = new EditText(this);
                editText2.setHint(R.string.resource_name);
                builder2.setView(editText2);
                builder2.setCancelable(false);
                builder2.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!editText2.getText().toString().isEmpty() && ProjectUtil.createFile(mProject, "css" + File.separator + editText2.getText().toString() + ".css", ProjectUtil.STYLE)) {
                            Toast.makeText(ProjectActivity.this, R.string.file_success, Toast.LENGTH_SHORT).show();
                            mFiles.add("css" + File.separator + editText2.getText().toString());
                            refreshMenu();
                            mPager.setCurrentItem(mFiles.indexOf("css" + File.separator + editText2.getText().toString()));
                        } else {
                            Toast.makeText(ProjectActivity.this, R.string.file_fail, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder2.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AppCompatDialog dialog2 = builder2.create();
                if (PreferenceUtil.get(ProjectActivity.this, "show_toast_file_ending", true))
                    showToast(true);
                dialog2.show();
                return true;
            case R.id.action_create_js:
                AlertDialog.Builder builder3 = new AlertDialog.Builder(this);
                builder3.setTitle(R.string.new_not_java);
                final EditText editText3 = new EditText(this);
                editText3.setHint(R.string.resource_name);
                builder3.setView(editText3);
                builder3.setCancelable(false);
                builder3.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!editText3.getText().toString().isEmpty() && ProjectUtil.createFile(mProject, "js" + File.separator + editText3.getText().toString() + ".js", ProjectUtil.MAIN)) {
                            Toast.makeText(ProjectActivity.this, R.string.file_success, Toast.LENGTH_SHORT).show();
                            mFiles.add("js" + File.separator + editText3.getText().toString());
                            refreshMenu();
                            mPager.setCurrentItem(mFiles.indexOf("js" + File.separator + editText3.getText().toString()));
                        } else {
                            Toast.makeText(ProjectActivity.this, R.string.file_fail, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder3.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AppCompatDialog dialog3 = builder3.create();
                if (PreferenceUtil.get(ProjectActivity.this, "show_toast_file_ending", true))
                    showToast(true);
                dialog3.show();
                return true;
            case R.id.action_view_resources:
                Intent resourceIntent = new Intent(ProjectActivity.this, ResourcesActivity.class);
                resourceIntent.putExtra("project", mProject);
                startActivity(resourceIntent);
                return true;
            case R.id.action_about:
                showAbout();
                return true;
        }

        return false;
    }

    private void showToast(boolean image) {
        if (!image) {
            Toast.makeText(this, R.string.file_ending_warn, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.file_ending_warn_image, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMPORT_IMAGE && resultCode == RESULT_OK) {
            final Uri imageUri = data.getData();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.name);
            final EditText editText = new EditText(this);
            editText.setHint(R.string.resource_name);
            builder.setView(editText);
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.import_not_java, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!editText.getText().toString().isEmpty() && ProjectUtil.importImage(ProjectActivity.this, mProject, imageUri, editText.getText().toString())) {
                        Toast.makeText(ProjectActivity.this, R.string.image_success, Toast.LENGTH_SHORT).show();
                        refreshMenu();
                    } else {
                        Toast.makeText(ProjectActivity.this, R.string.image_fail, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AppCompatDialog dialog = builder.create();
            if (PreferenceUtil.get(ProjectActivity.this, "show_toast_file_ending", true))
                showToast(true);
            dialog.show();
        }

        if (requestCode == IMPORT_FONT && resultCode == RESULT_OK) {
            final Uri fontUri = data.getData();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.name);
            final EditText editText = new EditText(this);
            editText.setHint(R.string.resource_name);
            builder.setView(editText);
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.import_not_java, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!editText.getText().toString().isEmpty() && ProjectUtil.importFont(ProjectActivity.this, mProject, fontUri, editText.getText().toString())) {
                        Toast.makeText(ProjectActivity.this, R.string.font_success, Toast.LENGTH_SHORT).show();
                        refreshMenu();
                    } else {
                        Toast.makeText(ProjectActivity.this, R.string.font_fail, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AppCompatDialog dialog = builder.create();
            if (PreferenceUtil.get(ProjectActivity.this, "show_toast_file_ending", true))
                showToast(true);
            dialog.show();
        }

        if (requestCode == IMPORT_CSS && resultCode == RESULT_OK) {
            final Uri cssUri = data.getData();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Name");
            final EditText editText = new EditText(this);
            editText.setHint("Resource name");
            builder.setView(editText);
            builder.setCancelable(false);
            builder.setPositiveButton("IMPORT", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!editText.getText().toString().isEmpty() && ProjectUtil.importCss(ProjectActivity.this, mProject, cssUri, editText.getText().toString() + ".css")) {
                        Toast.makeText(ProjectActivity.this, "Successfully imported CSS file.", Toast.LENGTH_SHORT).show();
                        refreshMenu();
                    } else {
                        Toast.makeText(ProjectActivity.this, "There was a problem while importing this CSS file.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AppCompatDialog dialog = builder.create();
            if (PreferenceUtil.get(ProjectActivity.this, "show_toast_file_ending", true))
                showToast(false);
            dialog.show();
        }

        if (requestCode == IMPORT_JS && resultCode == RESULT_OK) {
            final Uri jsUri = data.getData();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.name);
            final EditText editText = new EditText(this);
            editText.setHint(R.string.resource_name);
            builder.setView(editText);
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.import_not_java, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!editText.getText().toString().isEmpty() && ProjectUtil.importJs(ProjectActivity.this, mProject, jsUri, editText.getText().toString() + ".js")) {
                        Toast.makeText(ProjectActivity.this, R.string.js_success, Toast.LENGTH_SHORT).show();
                        refreshMenu();
                    } else {
                        Toast.makeText(ProjectActivity.this, R.string.js_fail, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AppCompatDialog dialog = builder.create();
            if (PreferenceUtil.get(ProjectActivity.this, "show_toast_file_ending", true))
                showToast(false);
            dialog.show();
        }
    }

    /**
     * Method to show about dialog holding project information
     */
    @SuppressLint("InflateParams")
    private void showAbout() {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.sheet_about, null);

        TextView name = (TextView) layout.findViewById(R.id.project_name);
        TextView author = (TextView) layout.findViewById(R.id.project_author);
        TextView description = (TextView) layout.findViewById(R.id.project_description);
        TextView keywords = (TextView) layout.findViewById(R.id.project_keywords);
        TextView color = (TextView) layout.findViewById(R.id.project_color);

        name.setText(JsonUtil.getProjectProperty(mProject, "name"));
        author.setText(JsonUtil.getProjectProperty(mProject, "author"));
        description.setText(JsonUtil.getProjectProperty(mProject, "description"));
        keywords.setText(JsonUtil.getProjectProperty(mProject, "keywords"));
        color.setText(JsonUtil.getProjectProperty(mProject, "color"));
        color.setTextColor(Color.parseColor(JsonUtil.getProjectProperty(mProject, "color")));

        if (PreferenceUtil.get(this, "dark_theme", false)) {
            layout.setBackgroundColor(0xFF333333);
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(layout);
        dialog.show();
    }

    private void refreshMenu() {
        mDrawer.getMenu().clear();
        setupMenu(mProject, null);
        mAdapter = new FileAdapter(getSupportFragmentManager(), mProject, mFiles);
        mPager.setAdapter(mAdapter);
        mTabStrip.setupWithViewPager(mPager);
    }
}
