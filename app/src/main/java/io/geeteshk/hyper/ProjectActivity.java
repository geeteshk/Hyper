package io.geeteshk.hyper;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;

import io.geeteshk.hyper.adapter.FileAdapter;
import io.geeteshk.hyper.util.JsonUtil;
import io.geeteshk.hyper.util.ProjectUtil;

/**
 * Activity to list projects
 */
public class ProjectActivity extends AppCompatActivity {

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getIntent().getStringExtra("project"));
        }

        FileAdapter adapter = new FileAdapter(getSupportFragmentManager());
        adapter.setProject(getIntent().getStringExtra("project"));
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        PagerSlidingTabStrip tabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabStrip.setViewPager(pager);
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
            editText.setHint("Name of your resource...");
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
            editText.setHint("Name of your resource...");
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
            editText.setHint("Name of your resource...");
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
            editText.setHint("Name of your resource...");
            builder.setView(editText);
            builder.setCancelable(false);
            builder.setPositiveButton("IMPORT", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!editText.getText().toString().isEmpty() && ProjectUtil.importCss(ProjectActivity.this, getIntent().getStringExtra("project"), jsUri, editText.getText().toString())) {
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
    private void showAbout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProjectActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_about, null);
        builder.setTitle("About " + getIntent().getStringExtra("project"));
        builder.setView(layout);

        TextView name = (TextView) layout.findViewById(R.id.project_name);
        TextView author = (TextView) layout.findViewById(R.id.project_author);
        TextView description = (TextView) layout.findViewById(R.id.project_description);
        TextView keywords = (TextView) layout.findViewById(R.id.project_keywords);

        name.setText(JsonUtil.getProjectProperty(ProjectActivity.this, getIntent().getStringExtra("project"), "name"));
        author.setText(JsonUtil.getProjectProperty(ProjectActivity.this, getIntent().getStringExtra("project"), "author"));
        description.setText(JsonUtil.getProjectProperty(ProjectActivity.this, getIntent().getStringExtra("project"), "description"));
        keywords.setText(JsonUtil.getProjectProperty(ProjectActivity.this, getIntent().getStringExtra("project"), "keywords"));

        AppCompatDialog dialog = builder.create();
        dialog.show();
    }
}
