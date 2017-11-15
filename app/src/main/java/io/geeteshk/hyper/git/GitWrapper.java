/*
 * Copyright 2016 Geetesh Kalakoti <kalakotig@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.geeteshk.hyper.git;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.adapter.ProjectAdapter;

/**
 * Helper class to handle git functions
 */
public class GitWrapper {

    /**
     * Log TAG
     */
    private static final String TAG = GitWrapper.class.getSimpleName();

    /**
     * git init
     *
     * @param context context to make toast
     * @param repo repo to init
     */
    public static void init(Context context, File repo, View view) {
        try {
            Git git = Git.init()
                    .setDirectory(repo)
                    .call();
            Snackbar.make(view, context.getString(R.string.repo_init) + git.getRepository().getDirectory(), Snackbar.LENGTH_LONG).show();
        } catch (GitAPIException e) {
            Log.e(TAG, e.toString());
            Snackbar.make(view, e.toString(), Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * git add -A
     *
     * @param repo repo to stage files
     */
    public static void add(View view, File repo) {
        try {
            Git git = getGit(view, repo);
            if (git != null) {
                git.add()
                        .addFilepattern(".")
                        .call();
                Snackbar.make(view, R.string.added_to_stage, Snackbar.LENGTH_LONG).show();
            }
        } catch (GitAPIException e) {
            Log.e(TAG, e.toString());
            Snackbar.make(view, e.toString(), Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * git commit -m 'message'
     *
     * @param context context to make toast
     * @param repo repo to commit to
     * @param message git commit message
     */
    public static void commit(Context context, View view, File repo, String message) {
        new CommitTask(context, view, repo, new String[] {"Committing changes", "Committed successfully.", "Unable to commit files."}).execute(message);
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
     * @param repo repo to view status of
     * @param t text views to set status to
     */
    public static void status(View view, File repo, TextView... t) {
        try {
            Git git = getGit(view, repo);
            if (git != null) {
                Status status = git.status()
                        .call();

                Set<String> conflicting = status.getConflicting();
                StringBuilder conflictingOut = new StringBuilder();
                for (String conflict : conflicting) {
                    conflictingOut.append(conflict).append("\n");
                }
                t[0].setText(changeTextToNone(conflictingOut.toString()));

                Set<String> added = status.getAdded();
                StringBuilder addedOut = new StringBuilder();
                for (String add : added) {
                    addedOut.append(add).append("\n");
                }
                t[1].setText(changeTextToNone(addedOut.toString()));

                Set<String> changed = status.getChanged();
                StringBuilder changedOut = new StringBuilder();
                for (String change : changed) {
                    changedOut.append(change).append("\n");
                }
                t[2].setText(changeTextToNone(changedOut.toString()));

                Set<String> missing = status.getMissing();
                StringBuilder missingOut = new StringBuilder();
                for (String miss : missing) {
                    missingOut.append(miss).append("\n");
                }
                t[3].setText(changeTextToNone(missingOut.toString()));

                Set<String> modified = status.getModified();
                StringBuilder modifiedOut = new StringBuilder();
                for (String mod : modified) {
                    modifiedOut.append(mod).append("\n");
                }
                t[4].setText(changeTextToNone(modifiedOut.toString()));

                Set<String> removed = status.getRemoved();
                StringBuilder removedOut = new StringBuilder();
                for (String remove : removed) {
                    removedOut.append(remove).append("\n");
                }
                t[5].setText(changeTextToNone(removedOut.toString()));

                Set<String> uncommitted = status.getUncommittedChanges();
                StringBuilder uncommittedOut = new StringBuilder();
                for (String uncom : uncommitted) {
                    uncommittedOut.append(uncom).append("\n");
                }
                t[6].setText(changeTextToNone(uncommittedOut.toString()));

                Set<String> untracked = status.getUntracked();
                StringBuilder untrackedOut = new StringBuilder();
                for (String untrack : untracked) {
                    untrackedOut.append(untrack).append("\n");
                }
                t[7].setText(changeTextToNone(untrackedOut.toString()));

                Set<String> untrackedFolders = status.getUntrackedFolders();
                StringBuilder untrackedFoldersOut = new StringBuilder();
                for (String untrackedf : untrackedFolders) {
                    untrackedFoldersOut.append(untrackedf).append("\n");
                }
                t[8].setText(changeTextToNone(untrackedFoldersOut.toString()));
            }
        } catch (GitAPIException e) {
            Log.e(TAG, e.toString());
            Snackbar.make(view, e.toString(), Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * git log
     *
     * @param repo to get commits from
     * @return list of commits
     */
    public static List<RevCommit> getCommits(View view, File repo) {
        Iterable<RevCommit> log = null;
        List<RevCommit> revCommits = new ArrayList<>();
        try {
            Git git = getGit(view, repo);
            if (git != null) {
                log = git.log()
                        .call();
            }
        } catch (GitAPIException e) {
            Log.e(TAG, e.toString());
            Snackbar.make(view, e.toString(), Snackbar.LENGTH_LONG).show();
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
     * @param repo repo to get branches from
     * @return list of branches
     */
    public static List<Ref> getBranches(View view, File repo) {
        List<Ref> branches = null;
        try {
            Git git = getGit(view, repo);
            if (git != null) {
                branches = git.branchList()
                        .call();
            }
        } catch (GitAPIException e) {
            Log.e(TAG, e.toString());
            Snackbar.make(view, e.toString(), Snackbar.LENGTH_LONG).show();
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
    public static void createBranch(Context context, View view, File repo, String branchName, boolean checked) {
        if (checked) {
            new CheckoutTask(context, view, repo, new String[] {"Creating new branch", "Checked out successfully.", "Unable to checkout."}).execute(String.valueOf(true), branchName);
        } else {
            try {
                Git git = getGit(view, repo);
                if (git != null) {
                    git.branchCreate()
                            .setName(branchName)
                            .call();
                }
            } catch (GitAPIException e) {
                Log.e(TAG, e.toString());
                Snackbar.make(view, e.toString(), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    /**
     * git branch -d branches
     *
     * @param repo to delete branches from
     * @param branches to delete
     */
    public static void deleteBranch(View view, File repo, String... branches) {
        try {
            Git git = getGit(view, repo);
            if (git != null) {
                git.branchDelete()
                        .setBranchNames(branches)
                        .call();
            }
        } catch (GitAPIException e) {
            Log.e(TAG, e.toString());
            Snackbar.make(view, e.toString(), Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * git checkout branch
     *
     * @param context context to make toast
     * @param repo to checkout to branch
     * @param branch to checkout to
     */
    public static void checkout(Context context, View view, File repo, String branch) {
        new CheckoutTask(context, view, repo, new String[] {"Checking out", "Checked out successfully.", "Unable to checkout."}).execute(String.valueOf(false), branch);
    }

    public static String getCurrentBranch(View view, File repo) {
        String branch = "";
        try {
            Git git = getGit(view, repo);
            if (git != null) {
                branch = git.getRepository()
                        .getFullBranch();
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            Snackbar.make(view, e.toString(), Snackbar.LENGTH_LONG).show();
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
    public static void clone(Context context, View view, File repo, ProjectAdapter adapter, String remoteUrl, String username, String password) {
        if (!repo.exists()) {
            new CloneTask(context, view, repo, adapter).execute(remoteUrl, username, password);
        } else {
            Snackbar.make(view, R.string.folder_exists, Snackbar.LENGTH_LONG).show();
        }
    }

    public static void push(Context context, View view, File repo, String remoteUrl, boolean[] options, String username, String password) {
        new PushTask(context, view, repo, new String[] {"Pushing changes", "Successfully pushed commits to remote.", "There was a problem while pushing commits."}, options).execute(remoteUrl, username, password);
    }

    public static void pull(Context context, View view, File repo, String remote, String username, String password) {
        new PullTask(context, view, repo, new String[] {"Pulling changes", "Successfully pulled commits from remote.", "There was a problem while pulling commits."}).execute(remote, username, password);
    }

    public static void fetch(Context context, View view, File repo, String remote, String username, String password) {
        new FetchTask(context, view, repo, new String[] {"Fetching remote " + remote, "Successfully fetched from " + remote + ".", "There was a problem while fetching from " + remote + "."}).execute(remote, username, password);
    }

    static Git getGit(View view, File repo) {
        try {
            return Git.open(repo);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            Snackbar.make(view, e.toString(), Snackbar.LENGTH_LONG).show();
        }

        return null;
    }

    private static StoredConfig getConfig(View view, File repo) {
        Git git = getGit(view, repo);
        if (git != null) {
            return git.getRepository().getConfig();
        }

        return null;
    }

    public static String getRemoteUrl(View view, File repo, String remote) {
        String url = "";
        StoredConfig config = getConfig(view, repo);
        if (config != null) {
            url = config.getString("remote", remote, "url");
        }

        return url;
    }

    public static ArrayList<String> getRemotes(View view, File repo) {
        ArrayList<String> remotes = null;
        StoredConfig config = getConfig(view, repo);
        if (config != null) {
            remotes = new ArrayList<>(config.getSubsections("remote"));
        }

        return remotes;
    }

    public static void addRemote(View view, File repo, String remote, String url) {
        StoredConfig config = getConfig(view, repo);
        if (config != null) {
            config.setString("remote", remote, "url", url);
            try {
                config.save();
            } catch (IOException e) {
                Log.e(TAG, e.toString());
                Snackbar.make(view, e.toString(), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    public static void removeRemote(View view, File repo, String remote) {
        StoredConfig config = getConfig(view, repo);
        if (config != null) {
            config.unsetSection("remote", remote);
            try {
                config.save();
            } catch (IOException e) {
                Log.e(TAG, e.toString());
                Snackbar.make(view, e.toString(), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    public static boolean canCommit(View view, File repo) {
        try {
            Git git = getGit(view, repo);
            if (git != null) {
                return git.getRepository().getRepositoryState().canCommit()
                        && git.status().call().hasUncommittedChanges();
            }
        } catch (GitAPIException e) {
            Log.e(TAG, e.toString());
            Snackbar.make(view, e.toString(), Snackbar.LENGTH_LONG).show();
        }

        return false;
    }

    public static boolean canCheckout(View view, File repo) {
        Git git = getGit(view, repo);
        return git != null && git.getRepository().getRepositoryState().canCheckout();
    }

    public static SpannableString diff(View view, File repo, ObjectId hash1, ObjectId hash2) {
        SpannableString string = null;
        Git git = getGit(view, repo);
        try {
            if (git != null) {
                OutputStream out = new ByteArrayOutputStream();
                DiffFormatter formatter = new DiffFormatter(out);
                formatter.setRepository(git.getRepository());
                formatter.format(hash1, hash2);
                string = new SpannableString(out.toString());
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            Snackbar.make(view, e.toString(), Snackbar.LENGTH_LONG).show();
        }

        return string;
    }
}
