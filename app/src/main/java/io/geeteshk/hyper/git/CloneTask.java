package io.geeteshk.hyper.git;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.BatchingProgressMonitor;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;

import io.geeteshk.hyper.adapter.ProjectAdapter;
import io.geeteshk.hyper.helper.Project;

public class CloneTask extends GitTask {

    private static final String TAG = CloneTask.class.getSimpleName();
    private ProjectAdapter mAdapter;

    public CloneTask(Context context, File repo, ProjectAdapter adapter) {
        super(context, repo, new String[]{"Cloning repository", "", ""});
        mAdapter = adapter;
        id = 3;
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        try {
            Git.cloneRepository()
                    .setURI(strings[0])
                    .setDirectory(mRepo)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(strings[1], strings[2]))
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
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if (aBoolean) {
            if (!Project.isValid(mRepo.getName())) {
                mBuilder.setContentText("The repo was successfully cloned but it doesn't seem to be a Hyper project.");
            } else {
                mAdapter.add(mRepo.getPath().substring(mRepo.getPath().lastIndexOf("/") + 1, mRepo.getPath().length()));
                mBuilder.setContentText("Successfully cloned.");
            }
        } else {
            mBuilder.setContentText("Unable to clone repo.");
        }

        mBuilder.setProgress(0, 0, false);
        mManager.notify(id, mBuilder.build());
    }
}
