package io.geeteshk.hyper.git;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;

public class CheckoutTask extends GitTask {

    private static final String TAG = CheckoutTask.class.getSimpleName();

    public CheckoutTask(Context context, File repo, GitCallback callback, String[] values) {
        super(context, repo, callback, values);
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        try {
            Git git = Giiit.getGit(mContext, mRepo);
            if (git != null) {
                git.checkout()
                        .setCreateBranch(Boolean.valueOf(strings[0]))
                        .setName(strings[1])
                        .call();
            }
        } catch (GitAPIException e) {
            Log.e(TAG, e.toString());
            Toast.makeText(mContext, e.toString(), Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if (aBoolean) {
            ((Activity) mContext).finish();
        }
    }
}
