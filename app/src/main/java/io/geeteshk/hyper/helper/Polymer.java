package io.geeteshk.hyper.helper;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.polymer.Element;
import io.geeteshk.hyper.polymer.ElementsHolder;

/**
 * Helper class for polymer functions
 */
public class Polymer {

    /**
     * Log TAG
     */
    private static final String TAG = Polymer.class.getSimpleName();

    /**
     * Construct components url to download from
     *
     * @return url
     */
    private static String getComponentsUrl() {
        ArrayList<Element> elements = ElementsHolder.getInstance().getElements();
        StringBuilder builder = new StringBuilder("http://bowerarchiver.appspot.com/archive?");
        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            builder.append(element.getName())
                    .append("=")
                    .append(element.getPrefix())
                    .append("%2F")
                    .append(element.getName())
                    .append("%23%5E")
                    .append(element.getVersion())
                    .append("&");
        }

        return builder.toString();
    }

    /**
     * Add packaged to project
     *
     * @param progressBar to display progress
     * @param progressText to explain task
     * @param project project to add to
     * @param context context for files
     */
    public static void addPackages(ProgressBar progressBar, TextView progressText, String project, Context context) {
        new GetComponentsTask(progressBar, progressText, project, context).execute(getComponentsUrl());
    }

    /**
     * Task to download polymer components and set them up
     */
    private static class GetComponentsTask extends AsyncTask<String, String, String> {

        Context mContext;
        ProgressBar mProgressBar;
        String mProject;
        TextView mProgressText;

        GetComponentsTask(ProgressBar progressBar, TextView progressText, String project, Context context) {
            mContext = context;
            mProgressBar = progressBar;
            mProject = project;
            mProgressText = progressText;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressText.setText(R.string.downloading_components);
            mProgressBar.setIndeterminate(false);
        }

        @Override
        protected String doInBackground(String... strings) {
            int count;
            try {
                URL url = new URL(strings[0]);
                URLConnection connection = url.openConnection();
                connection.connect();

                int length = connection.getContentLength();
                InputStream inputStream = new BufferedInputStream(url.openStream(), 8192);
                OutputStream outputStream = new FileOutputStream(Constants.HYPER_ROOT + File.separator + mProject + File.separator + "/components.zip");

                byte[] data = new byte[1024];
                long total = 0;

                while ((count = inputStream.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / length));
                    outputStream.write(data, 0, count);
                }

                outputStream.flush();
                outputStream.close();
                inputStream.close();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            mProgressBar.setProgress(Integer.parseInt(values[0]));
        }

        @Override
        protected void onPostExecute(String s) {
            mProgressText.setText(R.string.setting_up_components);
            mProgressBar.setIndeterminate(true);

            unpackComponents();
            organizeFiles();
            setupImports();

            mProgressText.setText(R.string.finished);
            mProgressBar.setIndeterminate(false);
            mProgressBar.setProgress(100);

            ((Activity) mContext).finish();
        }

        private void unpackComponents() {
            InputStream inputStream;
            ZipInputStream zipInputStream;
            try {
                String fileName;
                inputStream = new FileInputStream(Constants.HYPER_ROOT + File.separator + mProject + File.separator + "/components.zip");
                zipInputStream = new ZipInputStream(new BufferedInputStream(inputStream));
                ZipEntry zipEntry;
                byte[] buffer = new byte[1024];
                int count;

                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    fileName = zipEntry.getName();
                    if (zipEntry.isDirectory()) {
                        File file = new File(Constants.HYPER_ROOT + File.separator + mProject + File.separator + fileName);
                        if (file.mkdirs()) {
                            mProgressText.setText("Unpacking " + fileName + "...");
                        }

                        continue;
                    }

                    FileOutputStream fileOutputStream = new FileOutputStream(Constants.HYPER_ROOT + File.separator + mProject + File.separator + fileName);
                    while ((count = zipInputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, count);
                    }

                    fileOutputStream.close();
                    zipInputStream.closeEntry();
                }

                zipInputStream.close();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }

        private void organizeFiles() {
            File componentsFolder = new File(Constants.HYPER_ROOT + File.separator + mProject + File.separator + "components/bower_components");
            File newFolder = new File(Constants.HYPER_ROOT + File.separator + mProject + File.separator + "bower_components");
            if (!newFolder.exists()) {
                if (newFolder.mkdir()) {
                    return;
                }
            }

            if (componentsFolder.isDirectory()) {
                File[] contents = componentsFolder.listFiles();
                for (File content : contents) {
                    if (content.renameTo(new File(newFolder, content.getName()))) {
                        mProgressText.setText("Organizing " + content.getPath() + "...");
                    }
                }
            }

            Project.deleteDirectory(mContext, new File(Constants.HYPER_ROOT + File.separator + mProject + File.separator + "components"));
            if (!new File(Constants.HYPER_ROOT + File.separator + mProject + File.separator + "components.zip").delete()) {
                Log.v(TAG, "Error while deleting.");
            }
        }

        private void setupImports() {
            mProgressText.setText(R.string.setting_up_imports);
            ArrayList<Element> elements = ElementsHolder.getInstance().getElements();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < elements.size(); i++) {
                builder.append("\t\t\t\t<link rel=\"import\" href=\"bower_components/")
                        .append(elements.get(i).getName())
                        .append("/")
                        .append(elements.get(i).getName())
                        .append(".html\">\n");
            }

            Project.createFile(mProject, "imports.txt", builder.toString());
            Project.createFile(mProject, "packages.hyper", Jason.generatePackages());
        }
    }
}
