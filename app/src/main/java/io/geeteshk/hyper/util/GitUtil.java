package io.geeteshk.hyper.util;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GitUtil {

    private static final String TAG = GitUtil.class.getSimpleName();

    public static boolean isGitRepo(File repo) {
        return new File(repo, ".git").exists() &&
                new File(repo, ".git").isDirectory();
    }

    public static boolean isCommit(File repo) {
        Git git = null;
        try {
            git = Git.open(repo);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return false;
        }

        return git.getRepository().getRepositoryState().canCommit();
    }

    public static void init(Context context, File repo) {
        try {
            Git git = Git.init()
                    .setDirectory(repo)
                    .call();
            Toast.makeText(context, "Created repository at: " + git.getRepository().getDirectory(), Toast.LENGTH_SHORT).show();
        } catch (GitAPIException e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(context, "Unable to create git repository.", Toast.LENGTH_SHORT).show();
        }
    }

    public static void add(Context context, File repo) {
        try {
            Git git = Git.open(repo);
            git.add()
                    .addFilepattern(".")
                    .call();
            Toast.makeText(context, "Added all files to stage.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(context, "Couldn't add files to stage.", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(context, "Unable to read status of repo.", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(context, "Unable to read commits.", Toast.LENGTH_SHORT).show();
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
            git.getRepository().getFullBranch();
            branches = git.branchList().call();
        } catch (IOException | GitAPIException e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(context, "Error while fetching branches.", Toast.LENGTH_SHORT).show();
            return null;
        }

        return branches;
    }

    public static void createBranch(Context context, File repo, String branch) {
        new CheckoutTask(context, repo).execute(String.valueOf(true), branch);
    }

    public static void checkout(Context context, File repo, String branch) {
        new CheckoutTask(context, repo).execute(String.valueOf(false), branch);
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
                return false;
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                Toast.makeText(mContext, "Committed successfully.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "Unable to commit files.", Toast.LENGTH_SHORT).show();
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
                return false;
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                Toast.makeText(mContext, "Checked out successfully.", Toast.LENGTH_SHORT).show();
                ((Activity) mContext).finish();
            } else {
                Toast.makeText(mContext, "Unable to checkout.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
