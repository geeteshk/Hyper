package io.geeteshk.hyper.helper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.BatchingProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.geeteshk.hyper.adapter.ProjectAdapter;

/**
 * Helper class to handle git functions
 */
public class Giiit {

    /**
     * Log TAG
     */
    private static final String TAG = Giiit.class.getSimpleName();

    /**
     * git init
     *
     * @param context context to make toast
     * @param repo repo to init
     */
    public static void init(Context context, File repo) {
        try {
            Git git = Git.init()
                    .setDirectory(repo)
                    .call();
            Toast.makeText(context, "Initialized repository at: " + git.getRepository().getDirectory(), Toast.LENGTH_LONG).show();
        } catch (GitAPIException e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * git add -A
     *
     * @param context context to make toast
     * @param repo repo to stage files
     */
    public static void add(Context context, File repo) {
        try {
            Git git = Git.open(repo);
            git.add()
                    .addFilepattern(".")
                    .call();
            Toast.makeText(context, "Added all files to stage.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * git commit -m 'message'
     *
     * @param context context to make toast
     * @param repo repo to commit to
     * @param message git commit message
     */
    public static void commit(Context context, File repo, String message) {
        new CommitTask(context, repo).execute(message);
    }

    /**
     * git status
     *
     * @param context context to make toast
     * @param repo repo to view status of
     * @param t text views to set status to
     */
    public static void status(Context context, File repo, TextView... t) {
        try {
            Git git = Git.open(repo);
            Status status = git.status()
                    .call();

            Set<String> conflicting = status.getConflicting();
            String conflictingOut = "";
            for (String conflict : conflicting) {
                conflictingOut = conflictingOut + conflict + "\n";
            }
            t[0].setText(conflictingOut);

            Set<String> added = status.getAdded();
            String addedOut = "";
            for (String add : added) {
                addedOut = addedOut + add + "\n";
            }
            t[1].setText(addedOut);

            Set<String> changed = status.getChanged();
            String changedOut = "";
            for (String change : changed) {
                changedOut = changedOut + change + "\n";
            }
            t[2].setText(changedOut);

            Set<String> missing = status.getMissing();
            String missingOut = "";
            for (String miss : missing) {
                missingOut = missingOut + miss + "\n";
            }
            t[3].setText(missingOut);

            Set<String> modified = status.getModified();
            String modifiedOut = "";
            for (String mod : modified) {
                modifiedOut = modifiedOut + mod + "\n";
            }
            t[4].setText(modifiedOut);

            Set<String> removed = status.getRemoved();
            String removedOut = "";
            for (String remove : removed) {
                removedOut = removedOut + remove + "\n";
            }
            t[5].setText(removedOut);

            Set<String> uncommitted = status.getUncommittedChanges();
            String uncommittedOut = "";
            for (String uncom : uncommitted) {
                uncommittedOut = uncommittedOut + uncom + "\n";
            }
            t[6].setText(uncommittedOut);

            Set<String> untracked = status.getUntracked();
            String untrackedOut = "";
            for (String untrack : untracked) {
                untrackedOut = untrackedOut + untrack + "\n";
            }
            t[7].setText(untrackedOut);

            Set<String> untrackedFolders = status.getUntrackedFolders();
            String untrackedFoldersOut = "";
            for (String untrackedf : untrackedFolders) {
                untrackedFoldersOut = untrackedFoldersOut + untrackedf + "\n";
            }
            t[8].setText(untrackedFoldersOut);
        } catch (GitAPIException | IOException e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * git log
     *
     * @param context context to make toast
     * @param repo to get commits from
     * @return list of commits
     */
    public static List<RevCommit> getCommits(Context context, File repo) {
        Iterable<RevCommit> log;
        List<RevCommit> revCommits = new ArrayList<>();
        try {
            Git git = Git.open(repo);
            log = git.log()
                    .call();
        } catch (IOException | GitAPIException e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }

        for (RevCommit commit : log) {
            revCommits.add(commit);
        }

        return revCommits;
    }

    /**
     * git branch
     *
     * @param context context to make toast
     * @param repo repo to get branches from
     * @return list of branches
     */
    public static List<Ref> getBranches(Context context, File repo) {
        List<Ref> branches;
        try {
            Git git = Git.open(repo);
            branches = git.branchList().call();
        } catch (IOException | GitAPIException e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }

        return branches;
    }

    /**
     * git branch branchName
     *
     * @param context context to make toast
     * @param repo to create branch
     * @param branchName name of branch
     * @param checked switch to branch if it exists
     */
    public static void createBranch(Context context, File repo, String branchName, boolean checked) {
        if (checked) {
            new CheckoutTask(context, repo).execute(String.valueOf(true), branchName);
        } else {
            try {
                Git git = Git.open(repo);
                git.branchCreate()
                        .setName(branchName)
                        .call();
            } catch (IOException | GitAPIException e) {
                Log.e(TAG, e.getMessage());
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * git branch -d branches
     *
     * @param context context to make toast
     * @param repo to delete branches from
     * @param branches to delete
     */
    public static void deleteBranch(Context context, File repo, String... branches) {
        try {
            Git git = Git.open(repo);
            git.branchDelete()
                    .setBranchNames(branches)
                    .call();
        } catch (IOException | GitAPIException e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * git checkout branch
     *
     * @param context context to make toast
     * @param repo to checkout to branch
     * @param branch to checkout to
     */
    public static void checkout(Context context, File repo, String branch) {
        new CheckoutTask(context, repo).execute(String.valueOf(false), branch);
    }

    public static String getCurrentBranch(Context context, File repo) {
        String branch;
        try {
            Git git = Git.open(repo);
            branch = git.getRepository()
                    .getFullBranch();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }

        return branch;
    }

    /**
     * git clone remoteUrl
     *
     * @param context context to make toast
     * @param repo to clone
     * @param progressDialog to display progress
     * @param adapter to refresh
     * @param remoteUrl to clone from
     */
    public static void clone(Context context, File repo, ProgressDialog progressDialog, ProjectAdapter adapter, String remoteUrl) {
        if (!repo.exists()) {
            new CloneTask(context, repo, progressDialog, adapter).execute(remoteUrl);
        } else {
            Toast.makeText(context, "The folder already exists.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * git clean
     *
     * @param context context to make toast
     * @param repo to clean
     * @return what has been cleaned
     */
    public static Set<String> clean(Context context, File repo) {
        Set<String> removed = null;
        try {
            Git git = Git.open(repo);
            removed = git.clean()
                    .setCleanDirectories(false)
                    .call();
        } catch (IOException | GitAPIException e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return removed;
    }

    /**
     * Task to commit repos
     */
    private static class CommitTask extends AsyncTask<String, Void, Boolean> {

        private Context mContext;
        private File mRepo;

        CommitTask(Context context, File repo) {
            mContext = context;
            mRepo = repo;
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                Git git = Git.open(mRepo);
                git.commit()
                        .setMessage(strings[0])
                        .call();
            } catch (GitAPIException e) {
                Log.e(TAG, e.toString());
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                return false;
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                Toast.makeText(mContext, "Committed successfully.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(mContext, "Unable to commit files.", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Task to switch branches
     */
    private static class CheckoutTask extends AsyncTask<String, Void, Boolean> {

        private Context mContext;
        private File mRepo;

        CheckoutTask(Context context, File repo) {
            mContext = context;
            mRepo = repo;
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                Git git = Git.open(mRepo);
                git.checkout()
                        .setCreateBranch(Boolean.valueOf(strings[0]))
                        .setName(strings[1])
                        .call();
            } catch (GitAPIException e) {
                Log.e(TAG, e.toString());
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                return false;
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                Toast.makeText(mContext, "Checked out successfully.", Toast.LENGTH_LONG).show();
                ((Activity) mContext).finish();
            } else {
                Toast.makeText(mContext, "Unable to checkout.", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Task to clone repo
     */
    private static class CloneTask extends AsyncTask<String, String, Boolean> {

        private Context mContext;
        private File mRepo;
        private ProgressDialog mProgressDialog;
        private ProjectAdapter mAdapter;

        CloneTask(Context context, File repo, ProgressDialog progressDialog, ProjectAdapter adapter) {
            mContext = context;
            mRepo = repo;
            mProgressDialog = progressDialog;
            mAdapter = adapter;
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                Git.cloneRepository()
                        .setURI(strings[0])
                        .setDirectory(mRepo)
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
                Log.e(TAG, e.getMessage());
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                return false;
            }

            return true;
        }

        @Override
        protected void onProgressUpdate(final String... values) {
            super.onProgressUpdate(values);
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressDialog.setTitle(values[0]);
                    mProgressDialog.setMessage(values[0]);
                    mProgressDialog.setMax(Integer.valueOf(values[2]));
                }
            });

            mProgressDialog.setProgress(Integer.valueOf(values[1]));
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            mProgressDialog.hide();
            if (aBoolean) {
                if (FirstAid.isBroken(mRepo.getName())) {
                    Toast.makeText(mContext, "The repo was successfully cloned but it doesn't sem to be a Hyper project. If it is then please run First Aid from Settings in order to repair it.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, "Successfully cloned.", Toast.LENGTH_SHORT).show();
                }

                mAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(mContext, "Unable to clone repo.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
