package io.geeteshk.hyper.git;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.StringUtils;

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
            Log.e(TAG, e.toString());
            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
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
            Git git = getGit(context, repo);
            if (git != null) {
                git.add()
                        .addFilepattern(".")
                        .call();
                Toast.makeText(context, "Added all files to stage.", Toast.LENGTH_LONG).show();
            }
        } catch (GitAPIException e) {
            Log.e(TAG, e.toString());
            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * git commit -m 'message'
     *
     * @param context context to make toast
     * @param repo repo to commit to
     * @param message git commit message
     */
    public static void commit(Context context, File repo, GitCallback callback, String message) {
        new CommitTask(context, repo, callback, new String[] {"Committing changes", "Committed successfully.", "Unable to commit files."}).execute(message);
    }

    private static String changeTextToNone(String text) {
        if (StringUtils.isEmptyOrNull(text)) {
            return "None\n";
        }

        return text;
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
            Git git = getGit(context, repo);
            if (git != null) {
                Status status = git.status()
                        .call();

                Set<String> conflicting = status.getConflicting();
                String conflictingOut = "";
                for (String conflict : conflicting) {
                    conflictingOut = conflictingOut + conflict + "\n";
                }
                t[0].setText(changeTextToNone(conflictingOut));

                Set<String> added = status.getAdded();
                String addedOut = "";
                for (String add : added) {
                    addedOut = addedOut + add + "\n";
                }
                t[1].setText(changeTextToNone(addedOut));

                Set<String> changed = status.getChanged();
                String changedOut = "";
                for (String change : changed) {
                    changedOut = changedOut + change + "\n";
                }
                t[2].setText(changeTextToNone(changedOut));

                Set<String> missing = status.getMissing();
                String missingOut = "";
                for (String miss : missing) {
                    missingOut = missingOut + miss + "\n";
                }
                t[3].setText(changeTextToNone(missingOut));

                Set<String> modified = status.getModified();
                String modifiedOut = "";
                for (String mod : modified) {
                    modifiedOut = modifiedOut + mod + "\n";
                }
                t[4].setText(changeTextToNone(modifiedOut));

                Set<String> removed = status.getRemoved();
                String removedOut = "";
                for (String remove : removed) {
                    removedOut = removedOut + remove + "\n";
                }
                t[5].setText(changeTextToNone(removedOut));

                Set<String> uncommitted = status.getUncommittedChanges();
                String uncommittedOut = "";
                for (String uncom : uncommitted) {
                    uncommittedOut = uncommittedOut + uncom + "\n";
                }
                t[6].setText(changeTextToNone(uncommittedOut));

                Set<String> untracked = status.getUntracked();
                String untrackedOut = "";
                for (String untrack : untracked) {
                    untrackedOut = untrackedOut + untrack + "\n";
                }
                t[7].setText(changeTextToNone(untrackedOut));

                Set<String> untrackedFolders = status.getUntrackedFolders();
                String untrackedFoldersOut = "";
                for (String untrackedf : untrackedFolders) {
                    untrackedFoldersOut = untrackedFoldersOut + untrackedf + "\n";
                }
                t[8].setText(changeTextToNone(untrackedFoldersOut));
            }
        } catch (GitAPIException e) {
            Log.e(TAG, e.toString());
            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
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
        Iterable<RevCommit> log = null;
        List<RevCommit> revCommits = new ArrayList<>();
        try {
            Git git = getGit(context, repo);
            if (git != null) {
                log = git.log()
                        .call();
            }
        } catch (GitAPIException e) {
            Log.e(TAG, e.toString());
            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
            return null;
        }

        if (log != null) {
            for (RevCommit commit : log) {
                revCommits.add(commit);
            }
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
        List<Ref> branches = null;
        try {
            Git git = getGit(context, repo);
            if (git != null) {
                branches = git.branchList()
                        .call();
            }
        } catch (GitAPIException e) {
            Log.e(TAG, e.toString());
            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
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
    public static void createBranch(Context context, File repo, GitCallback callback, String branchName, boolean checked) {
        if (checked) {
            new CheckoutTask(context, repo, callback, new String[] {"Creating new branch", "Checked out successfully.", "Unable to checkout."}).execute(String.valueOf(true), branchName);
        } else {
            try {
                Git git = getGit(context, repo);
                if (git != null) {
                    git.branchCreate()
                            .setName(branchName)
                            .call();
                }
            } catch (GitAPIException e) {
                Log.e(TAG, e.toString());
                Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
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
            Git git = getGit(context, repo);
            if (git != null) {
                git.branchDelete()
                        .setBranchNames(branches)
                        .call();
            }
        } catch (GitAPIException e) {
            Log.e(TAG, e.toString());
            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * git checkout branch
     *
     * @param context context to make toast
     * @param repo to checkout to branch
     * @param branch to checkout to
     */
    public static void checkout(Context context, File repo, GitCallback callback, String branch) {
        new CheckoutTask(context, repo, callback, new String[] {"Checking out", "Checked out successfully.", "Unable to checkout."}).execute(String.valueOf(false), branch);
    }

    public static String getCurrentBranch(Context context, File repo) {
        String branch = "";
        try {
            Git git = getGit(context, repo);
            if (git != null) {
                branch = git.getRepository()
                        .getFullBranch();
            }
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
     * @param adapter to refresh
     * @param remoteUrl to clone from
     */
    public static void clone(Context context, File repo, GitCallback callback, ProjectAdapter adapter, String remoteUrl, String username, String password) {
        if (!repo.exists()) {
            new CloneTask(context, repo, callback, adapter).execute(remoteUrl, username, password);
        } else {
            Toast.makeText(context, "The folder already exists.", Toast.LENGTH_LONG).show();
        }
    }

    public static void push(Context context, File repo, GitCallback callback, String remoteUrl, boolean[] options, String username, String password) {
        new PushTask(context, repo, callback, new String[] {"Pushing changes", "Successfully pushed commits to remote.", "There was a problem while pushing commits."}, options).execute(remoteUrl, username, password);
    }

    public static void pull(Context context, File repo, GitCallback callback, String remote, String username, String password) {
        new PullTask(context, repo, callback, new String[] {"Pulling changes", "Successfully pulled commits from remote.", "There was a problem while pulling commits."}).execute(remote, username, password);
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
            Git git = getGit(context, repo);
            if (git != null) {
                removed = git.clean()
                        .setCleanDirectories(false)
                        .call();
            }
        } catch (GitAPIException e) {
            Log.e(TAG, e.toString());
            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
        }

        return removed;
    }

    public static Git getGit(Context context, File repo) {
        try {
            return Git.open(repo);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return null;
    }

    private static StoredConfig getConfig(Context context, File repo) {
        Git git = getGit(context, repo);
        if (git != null) {
            return git.getRepository().getConfig();
        }

        return null;
    }

    public static String getRemoteUrl(Context context, File repo, String remote) {
        String url = "";
        StoredConfig config = getConfig(context, repo);
        if (config != null) {
            url = config.getString("remote", remote, "url");
        }

        return url;
    }

    public static ArrayList<String> getRemotes(Context context, File repo) {
        ArrayList<String> remotes = null;
        StoredConfig config = getConfig(context, repo);
        if (config != null) {
            remotes = new ArrayList<>(config.getSubsections("remote"));
        }

        return remotes;
    }

    public static void addRemote(Context context, File repo, String remote, String url) {
        StoredConfig config = getConfig(context, repo);
        if (config != null) {
            config.setString("remote", remote, "url", url);
            try {
                config.save();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public static void removeRemote(Context context, File repo, String remote) {
        StoredConfig config = getConfig(context, repo);
        if (config != null) {
            config.unsetSection("remote", remote);
            try {
                config.save();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
