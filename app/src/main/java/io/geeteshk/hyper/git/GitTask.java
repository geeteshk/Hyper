package io.geeteshk.hyper.git;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.File;

public abstract class GitTask extends AsyncTask<String, String, Boolean> {

    public Context mContext;
    public File mRepo;
    public GitCallback mCallback;
    public String[] mValues;

    public GitTask(Context context, File repo, GitCallback callback, String[] values) {
        mContext = context;
        mRepo = repo;
        mCallback = callback;
        mValues = values;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mCallback.onPreExecute(mValues[0]);
    }

    @Override
    protected void onProgressUpdate(final String... values) {
        super.onProgressUpdate(values);
        mCallback.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        mCallback.onPostExecute();
        if (aBoolean) {
            Toast.makeText(mContext, mValues[1], Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(mContext, mValues[2], Toast.LENGTH_LONG).show();
        }
    }
}
