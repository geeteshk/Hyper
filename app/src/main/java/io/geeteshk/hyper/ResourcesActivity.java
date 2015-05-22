package io.geeteshk.hyper;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Activity to list resources of a certain project
 */
public class ResourcesActivity extends AppCompatActivity {

    /**
     * Called when the activity is created
     *
     * @param savedInstanceState restored when onResume is called
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resources);

        final String name = getIntent().getStringExtra("project");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(name + " Resources");
        }

        ListView imagesList = (ListView) findViewById(R.id.images_list);
        ListView fontsList = (ListView) findViewById(R.id.fonts_list);
        ListView cssList = (ListView) findViewById(R.id.css_list);
        ListView jsList = (ListView) findViewById(R.id.js_list);

        imagesList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Resources.get(this, name, "images")));
        fontsList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Resources.get(this, name, "fonts")));
        cssList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Resources.get(this, name, "css")));
        jsList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Resources.get(this, name, "js")));

        imagesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ResourcesActivity.this);
                builder.setTitle("Delete Resource?");
                builder.setMessage("Are you sure?");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Resources.remove(ResourcesActivity.this, name, "images", ((TextView) view).getText().toString())) {
                            Toast.makeText(ResourcesActivity.this, "Successfully deleted.", Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                            recreate();
                        } else {
                            Toast.makeText(ResourcesActivity.this, "Unable to delete resource.", Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                            recreate();
                        }
                    }
                });
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AppCompatDialog dialog = builder.create();
                dialog.show();

                return false;
            }
        });
    }

    /**
     * Helper class to get and remove resources
     */
    private static class Resources {

        /**
         * Gets a list of resources
         *
         * @param context  used to read directory
         * @param project  name of project
         * @param resource type of resource to list
         * @return list of resources
         */
        public static String[] get(Context context, String project, String resource) {
            return new File(context.getFilesDir() + File.separator + project + File.separator + resource).list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return !filename.equals("favicon.ico");

                }
            });
        }

        /**
         * Removes a specific resource from project
         *
         * @param context  used to delete file
         * @param project  name of project
         * @param resource type of resource to delete
         * @param name     f resource
         * @return true if deleted
         */
        public static boolean remove(Context context, String project, String resource, String name) {
            return new File(context.getFilesDir() + File.separator + project + File.separator + resource + File.separator + name).delete();
        }
    }
}
