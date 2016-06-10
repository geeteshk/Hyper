package io.geeteshk.hyper;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import io.geeteshk.hyper.adapter.FileAdapter;
import io.geeteshk.hyper.helper.HyperDrive;
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

    /**
     * Called when the activity is created
     *
     * @param savedInstanceState restored when onResume is called
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (PreferenceUtil.get(this, "dark_theme", false)) {
            setTheme(R.style.Hyper_Dark);
        }

        NetworkUtil.setDrive(new HyperDrive(getIntent().getStringExtra("project")));
        super.onCreate(savedInstanceState);

        try {
            NetworkUtil.getDrive().start();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }

        KeyboardDetectorLayout layout = new KeyboardDetectorLayout(this, null);
        setContentView(layout);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(0xFFE64A19);
        }

        RelativeLayout projectLayout = (RelativeLayout) findViewById(R.id.project_layout_snack);
        if (PreferenceUtil.get(this, "pin", "").equals("")) {
            Snackbar snackbar = Snackbar.make(projectLayout, "It is recommended you set a PIN for security in the Settings.", Snackbar.LENGTH_LONG)
                    .setAction("SET PIN", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setResult(1337);
                            finish();
                        }
                    });
            snackbar.show();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getIntent().getStringExtra("project"));
        }

        FileAdapter adapter = new FileAdapter(getSupportFragmentManager());
        adapter.setProject(getIntent().getStringExtra("project"));
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        if (pager != null) {
            pager.setAdapter(adapter);
        }

        TabLayout tabStrip = (TabLayout) findViewById(R.id.tabs);
        if (tabStrip != null) {
            tabStrip.setupWithViewPager(pager);
        }

        FloatingActionButton button = (FloatingActionButton) findViewById(R.id.fab_build);
        assert button != null;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProjectActivity.this, WebActivity.class);

                if (NetworkUtil.getDrive().wasStarted() && NetworkUtil.getDrive().isAlive() && NetworkUtil.getIpAddress() != null) {
                    intent.putExtra("url", "http:///" + NetworkUtil.getIpAddress() + ":8080");
                } else {
                    intent.putExtra("url", "file:///" + Constants.HYPER_ROOT + File.separator + getIntent().getStringExtra("project") + File.separator + "index.html");
                }

                intent.putExtra("name", getIntent().getStringExtra("project"));
                startActivity(intent);
            }
        });

        layout.setButton(button);

        button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(ProjectActivity.this, "Test", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
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
                cssIntent.setType("file/*");
                if (cssIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(cssIntent, IMPORT_CSS);
                }
                return true;
            case R.id.action_import_js:
                Intent jsIntent = new Intent(Intent.ACTION_GET_CONTENT);
                jsIntent.setType("file/*");
                if (jsIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(jsIntent, IMPORT_JS);
                }
                return true;
            case R.id.action_view_resources:
                Intent resourceIntent = new Intent(ProjectActivity.this, ResourcesActivity.class);
                resourceIntent.putExtra("project", getIntent().getStringExtra("project"));
                startActivity(resourceIntent);
                return true;
            case R.id.action_about:
                showAbout();
                return true;
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMPORT_IMAGE && resultCode == RESULT_OK) {
            final Uri imageUri = data.getData();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Name");
            final EditText editText = new EditText(this);
            editText.setHint("Resource name");
            builder.setView(editText);
            builder.setCancelable(false);
            builder.setPositiveButton("IMPORT", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!editText.getText().toString().isEmpty() && ProjectUtil.importImage(ProjectActivity.this, getIntent().getStringExtra("project"), imageUri, editText.getText().toString())) {
                        Toast.makeText(ProjectActivity.this, "Successfully imported image.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProjectActivity.this, "There was a problem while importing this image.", Toast.LENGTH_SHORT).show();
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
            dialog.show();
        }

        if (requestCode == IMPORT_FONT && resultCode == RESULT_OK) {
            final Uri fontUri = data.getData();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Name");
            final EditText editText = new EditText(this);
            editText.setHint("Resource name");
            builder.setView(editText);
            builder.setCancelable(false);
            builder.setPositiveButton("IMPORT", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!editText.getText().toString().isEmpty() && ProjectUtil.importFont(ProjectActivity.this, getIntent().getStringExtra("project"), fontUri, editText.getText().toString())) {
                        Toast.makeText(ProjectActivity.this, "Successfully imported font.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProjectActivity.this, "There was a problem while importing this font.", Toast.LENGTH_SHORT).show();
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
                    if (!editText.getText().toString().isEmpty() && ProjectUtil.importCss(ProjectActivity.this, getIntent().getStringExtra("project"), cssUri, editText.getText().toString())) {
                        Toast.makeText(ProjectActivity.this, "Successfully imported CSS file.", Toast.LENGTH_SHORT).show();
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
            dialog.show();
        }

        if (requestCode == IMPORT_JS && resultCode == RESULT_OK) {
            final Uri jsUri = data.getData();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Name");
            final EditText editText = new EditText(this);
            editText.setHint("Resource name");
            builder.setView(editText);
            builder.setCancelable(false);
            builder.setPositiveButton("IMPORT", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!editText.getText().toString().isEmpty() && ProjectUtil.importJs(ProjectActivity.this, getIntent().getStringExtra("project"), jsUri, editText.getText().toString())) {
                        Toast.makeText(ProjectActivity.this, "Successfully imported JS file.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProjectActivity.this, "There was a problem while importing this JS file.", Toast.LENGTH_SHORT).show();
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
            dialog.show();
        }
    }

    /**
     * Method to show about dialog holding project information
     */
    @SuppressLint("InflateParams")
    private void showAbout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_about, null);
        builder.setTitle("About " + getIntent().getStringExtra("project"));
        builder.setView(layout);

        TextView name = (TextView) layout.findViewById(R.id.project_name);
        TextView author = (TextView) layout.findViewById(R.id.project_author);
        TextView description = (TextView) layout.findViewById(R.id.project_description);
        TextView keywords = (TextView) layout.findViewById(R.id.project_keywords);
        TextView color = (TextView) layout.findViewById(R.id.project_color);

        name.setText(JsonUtil.getProjectProperty(getIntent().getStringExtra("project"), "name"));
        author.setText(JsonUtil.getProjectProperty(getIntent().getStringExtra("project"), "author"));
        description.setText(JsonUtil.getProjectProperty(getIntent().getStringExtra("project"), "description"));
        keywords.setText(JsonUtil.getProjectProperty(getIntent().getStringExtra("project"), "keywords"));
        color.setText(JsonUtil.getProjectProperty(getIntent().getStringExtra("project"), "color"));
        color.setTextColor(Color.parseColor(JsonUtil.getProjectProperty(getIntent().getStringExtra("project"), "color")));

        AppCompatDialog dialog = builder.create();
        dialog.show();
    }
}
