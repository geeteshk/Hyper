package io.geeteshk.hyper.git;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;

public class CommitTask extends GitTask {

    private static final String TAG = CommitTask.class.getSimpleName();

    public CommitTask(Context context, File repo, String[] values) {
        super(context, repo, values);
        id = 4;
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
            return false;
        }

        return true;
    }
}
