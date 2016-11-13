package io.geeteshk.hyper.helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;

import io.geeteshk.hyper.R;

/**
 * Helper class to fix broken projects
 */
public class FirstAid {

    /**
     * Status array to identify errors
     */
    private static int[] mStatus = new int[]{0, 0, 0, 0, 0, 0};

    /**
     * Repair project
     *
     * @param context context to copy files
     * @param name project title
     * @param author project author
     * @param description project description
     * @param keywords project keywords separated by commas
     * @return true if successfully repaired
     */
    private static boolean repair(Context context, String name, String author, String description, String keywords) {
        boolean success = true;
        if (mStatus[0] == 1) {
            success = Jason.createProjectFile(name, author, description, keywords, "#000000");
            mStatus[0] = 0;
        }

        if (mStatus[1] == 1) {
            success = success && Project.createFile(name, "index.html", Project.INDEX.replace("@name", name).replace("@author", author).replace("@description", description).replace("@keywords", keywords).replace("@color", "#000000"));
            mStatus[1] = 0;
        }

        if (mStatus[2] == 1) {
            success = success && Project.createDirectory(name + File.separator + "js");
            success = success && Project.createFile(name, "js" + File.separator + "main.js", Project.MAIN);
            mStatus[2] = 0;
        }

        if (mStatus[3] == 1) {
            success = success && Project.createDirectory(name + File.separator + "css");
            success = success && Project.createFile(name, "css" + File.separator + "style.css", Project.STYLE);
            mStatus[3] = 0;
        }

        if (mStatus[4] == 1) {
            success = success && Project.copyIcon(context, name);
            mStatus[4] = 0;
        }

        if (mStatus[5] == 1) {
            success = success && Project.createDirectory(name + File.separator + "fonts");
            mStatus[5] = 0;
        }

        return success;
    }

    /**
     * Repair all projects with given context
     *
     * @param context to copy files
     */
    public static void repairAll(final Context context) {
        final String[] objects = new File(Constants.HYPER_ROOT).list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return dir.isDirectory() && !name.equals(".git") && isBroken(name, false);
            }
        });

        if (objects.length == 0) return;

        final String[] objectToRepair = {""};
        AlertDialog.Builder choiceBuilder = new AlertDialog.Builder(context);
        choiceBuilder.setSingleChoiceItems(objects, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                objectToRepair[0] = objects[which];
            }
        });
        choiceBuilder.setNegativeButton("CANCEL", null);
        choiceBuilder.setPositiveButton("REPAIR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String object = objectToRepair[0];
                if (object.equals("")) return;
                if (isBroken(object, false)) {
                    if (mStatus[0] == 1 || mStatus[1] == 1) {
                        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                        @SuppressLint("InflateParams") View layout = inflater.inflate(R.layout.dialog_repair, null);

                        final TextInputLayout authorLayout, descLayout, keyLayout;
                        authorLayout = (TextInputLayout) layout.findViewById(R.id.author_layout);
                        descLayout = (TextInputLayout) layout.findViewById(R.id.description_layout);
                        keyLayout = (TextInputLayout) layout.findViewById(R.id.keywords_layout);

                        AlertDialog.Builder builder;
                        if (Pref.get(context, "dark_theme", false)) {
                            builder = new AlertDialog.Builder(context, R.style.Hyper_Dark);
                        } else {
                            builder = new AlertDialog.Builder(context);
                        }

                        builder.setTitle(context.getString(R.string.repair_key) + " " + object);
                        builder.setView(layout);
                        builder.setPositiveButton(R.string.repair_key, null);

                        final AppCompatDialog dialog2 = builder.create();
                        dialog2.show();

                        Button button = ((AlertDialog) dialog2).getButton(AlertDialog.BUTTON_POSITIVE);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                assert authorLayout.getEditText() != null;
                                assert descLayout.getEditText() != null;
                                assert keyLayout.getEditText() != null;
                                if (Validator.validate(context, null, authorLayout, descLayout, keyLayout)) {
                                    if (repair(context, object, authorLayout.getEditText().getText().toString(), descLayout.getEditText().getText().toString(), keyLayout.getEditText().getText().toString())) {
                                        Toast.makeText(context, object + " " + context.getString(R.string.repaired) + ".", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(context, object + " " + context.getString(R.string.failed) + ".", Toast.LENGTH_SHORT).show();
                                    }

                                    dialog2.dismiss();
                                }
                            }
                        });
                    } else {
                        if (repair(context, object, "", "", "")) {
                            Toast.makeText(context, object + " " + context.getString(R.string.repaired) + ".", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, object + " " + context.getString(R.string.failed) + ".", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        choiceBuilder.create().show();
    }

    /**
     * Check if project is broken
     *
     * @param string project title
     * @return true if project is broken
     */
    public static boolean isBroken(String string, boolean project) {
        boolean out = false;
        if (!new File(Constants.HYPER_ROOT + File.separator + string + File.separator + string + ".hyper").exists()
                || Jason.getProjectProperty(string, "name") == null
                || Jason.getProjectProperty(string, "author") == null
                || Jason.getProjectProperty(string, "description") == null
                || Jason.getProjectProperty(string, "keywords") == null
                || Jason.getProjectProperty(string, "color") == null) {
            out = true;
            mStatus[0] = 1;
        }

        if (!new File(Constants.HYPER_ROOT + File.separator + string + File.separator + "index.html").exists()) {
            out = true;
            mStatus[1] = 1;
        }

        if (!new File(Constants.HYPER_ROOT + File.separator + string + File.separator + "js" + File.separator + "main.js").exists()) {
            out = true;
            mStatus[2] = 1;
        }

        if (!new File(Constants.HYPER_ROOT + File.separator + string + File.separator + "css" + File.separator + "style.css").exists()) {
            out = true;
            mStatus[3] = 1;
        }

        if (!new File(Constants.HYPER_ROOT + File.separator + string + File.separator + "images" + File.separator + "favicon.ico").exists()) {
            out = true;
            mStatus[4] = 1;
        }

        if (!new File(Constants.HYPER_ROOT + File.separator + string + File.separator + "fonts").isDirectory()) {
            if (project) {
                new File(Constants.HYPER_ROOT + File.separator + string + File.separator + "fonts").mkdir();
            } else {
                out = true;
                mStatus[5] = 1;
            }
        }

        return out;
    }
}
