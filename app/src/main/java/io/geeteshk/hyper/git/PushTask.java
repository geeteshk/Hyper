package io.geeteshk.hyper.git;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.BatchingProgressMonitor;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;

public class PushTask extends GitTask {

    private static final String TAG = PushTask.class.getSimpleName();
    private boolean[] mOptions;

    public PushTask(Context context, File repo, GitCallback callback, String[] values, boolean[] options) {
        super(context, repo, callback, values);
        mOptions = options;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        Git git = Giiit.getGit(mContext, mRepo);
        if (git != null) {
            try {
                if (mOptions[3]) {
                    git.push()
                            .setRemote(params[0])
                            .setDryRun(mOptions[0])
                            .setForce(mOptions[1])
                            .setThin(mOptions[2])
                            .setPushTags()
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
                } else {
                    git.push()
                            .setRemote(params[0])
                            .setDryRun(mOptions[0])
                            .setForce(mOptions[1])
                            .setThin(mOptions[2])
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
                }
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
