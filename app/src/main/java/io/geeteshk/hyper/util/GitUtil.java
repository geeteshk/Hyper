package io.geeteshk.hyper.util;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;
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

public class GitUtil {

    private static final String TAG = GitUtil.class.getSimpleName();

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

    public static void commit(Context context, File repo, String message) {
        new CommitTask(context, repo).execute(message);
    }

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

    public static void createBranch(Context context, File repo, String branch, boolean checked) {
        if (checked) {
            new CheckoutTask(context, repo).execute(String.valueOf(true), branch);
        } else {
            try {
                Git git = Git.open(repo);
                git.branchCreate()
                        .setName(branch)
                        .call();
            } catch (IOException | GitAPIException e) {
                Log.e(TAG, e.getMessage());
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

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

    public static void clone(Context context, File repo, ProgressBar progressBar, ProjectAdapter adapter, String remoteUrl) {
        if (!repo.exists()) {
            new CloneTask(context, repo, progressBar, adapter).execute(remoteUrl);
        } else {
            Toast.makeText(context, "The folder already exists.", Toast.LENGTH_LONG).show();
        }
    }

    static class CommitTask extends AsyncTask<String, Void, Boolean> {

        private Context mContext;
        private File mRepo;

        public CommitTask(Context context, File repo) {
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

    static class CheckoutTask extends AsyncTask<String, Void, Boolean> {

        private Context mContext;
        private File mRepo;

        public CheckoutTask(Context context, File repo) {
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

    static class CloneTask extends AsyncTask<String, Integer, Boolean> {

        private Context mContext;
        private File mRepo;
        private ProgressBar mProgressBar;
        private ProjectAdapter mAdapter;

        public CloneTask(Context context, File repo, ProgressBar progressBar, ProjectAdapter adapter) {
            mContext = context;
            mRepo = repo;
            mProgressBar = progressBar;
            mAdapter = adapter;
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                final Git git = Git.cloneRepository()
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
                                publishProgress(percentDone);
                            }

                            @Override
                            protected void onEndTask(String taskName, int workCurr, int workTotal, int percentDone) {
                                publishProgress(percentDone);
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
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            mProgressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                Toast.makeText(mContext, "Successfully cloned: " + git.getRepository().getDirectory(), Toast.LENGTH_SHORT).show();
                Toast.makeText(mContext, "Please run First Aid from the Settings if your cloned projects do not show up.", Toast.LENGTH_LONG).show();
                mAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(mContext, "Unable to clone repo.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
