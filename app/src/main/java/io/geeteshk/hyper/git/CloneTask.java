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
import io.geeteshk.hyper.helper.FirstAid;

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
            if (FirstAid.isBroken(mRepo.getName(), true)) {
                Toast.makeText(mContext, "The repo was successfully cloned but it doesn't sem to be a Hyper project. If it is then please run First Aid from Settings in order to repair it.", Toast.LENGTH_SHORT).show();
            } else {
                mAdapter.add(mRepo.getPath().substring(mRepo.getPath().lastIndexOf("/"), mRepo.getPath().length()));
                Toast.makeText(mContext, "Successfully cloned.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(mContext, "Unable to clone repo.", Toast.LENGTH_LONG).show();
        }
    }
}
