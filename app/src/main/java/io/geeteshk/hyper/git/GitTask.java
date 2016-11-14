package io.geeteshk.hyper.git;

import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import java.io.File;

import io.geeteshk.hyper.R;

public abstract class GitTask extends AsyncTask<String, String, Boolean> {

    private NotificationManager mManager;
    private NotificationCompat.Builder mBuilder;
    public int id = 1;

    public Context mContext;
    public File mRepo;
    public String[] mValues;

    public GitTask(Context context, File repo, String[] values) {
        mContext = context;
        mRepo = repo;
        mValues = values;
        mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mBuilder.setContentTitle(mValues[0])
            .setSmallIcon(R.drawable.ic_git_small);
    }

    @Override
    protected void onProgressUpdate(final String... values) {
        super.onProgressUpdate(values);
        mBuilder.setContentText(values[0])
                .setProgress(Integer.valueOf(values[2]), Integer.valueOf(values[1]), false);

        mManager.notify(id, mBuilder.build());
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if (aBoolean) {
            mBuilder.setContentText(mValues[1]);
        } else {
            mBuilder.setContentText(mValues[2]);
        }

        mBuilder.setProgress(0, 0, false);
        mManager.notify(id, mBuilder.build());
    }
}
