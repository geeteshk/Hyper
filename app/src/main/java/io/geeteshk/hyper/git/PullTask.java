package io.geeteshk.hyper.git;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.BatchingProgressMonitor;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;

public class PullTask extends GitTask {

    private static final String TAG = PullTask.class.getSimpleName();

    public PullTask(Context context, File repo, GitCallback callback, String[] values) {
        super(context, repo, callback, values);
    }

    @Override
    protected Boolean doInBackground(String... params) {
        Git git = Giiit.getGit(mContext, mRepo);
        if (git != null) {
            try {
                git.pull()
                        .setRemote(params[0])
                        .setCredentialsProvider(new UsernamePasswordCredentialsProvider(params[1], params[2]))
                        .setProgressMonitor(new BatchingProgressMonitor() {
                            @Override
                            protected void onUpdate(String taskName, int workCurr) {

                            }

                            @Override
                            protected void onEndTask(String taskName, int workCurr) {

                            }

                            @Override
                            protected void onUpdate(String taskName, int workCurr, int workTotal, int percentDone) {
                                publishProgress(taskName, String.valueOf(percentDone), String.valueOf(workCurr), String.valueOf(workTotal));
                            }

                            @Override
                            protected void onEndTask(String taskName, int workCurr, int workTotal, int percentDone) {
                                publishProgress(taskName, String.valueOf(workCurr), String.valueOf(workTotal));
                            }
                        })
                        .call();
            } catch (GitAPIException e) {
                Log.e(TAG, e.toString());
                Toast.makeText(mContext, e.toString(), Toast.LENGTH_LONG).show();
                return false;
            }

            return true;
        }

        return false;
    }
}
