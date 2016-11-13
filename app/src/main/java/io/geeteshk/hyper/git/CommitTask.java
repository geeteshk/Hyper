package io.geeteshk.hyper.git;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;

public class CommitTask extends GitTask {

    private static final String TAG = CommitTask.class.getSimpleName();

    public CommitTask(Context context, File repo, GitCallback callback, String[] values) {
        super(context, repo, callback, values);
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        try {
            Git git = Giiit.getGit(mContext, mRepo);
            if (git != null) {
                git.commit()
                        .setMessage(strings[0])
                        .call();
            }
        } catch (GitAPIException e) {
            Log.e(TAG, e.toString());
            Toast.makeText(mContext, e.toString(), Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }
}
