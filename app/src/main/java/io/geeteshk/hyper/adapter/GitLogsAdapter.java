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

package io.geeteshk.hyper.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Typeface;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.eclipse.jgit.revwalk.RevCommit;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.geeteshk.hyper.R;

/**
 * Adapter to display git logs
 */
public class GitLogsAdapter extends RecyclerView.Adapter<GitLogsAdapter.ViewHolder> {

    /**
     * Adapter context
     */
    private Context context;

    /**
     * Git logs
     */
    private List<RevCommit> gitLogs;

    /**
     * public Constructor
     *
     * @param context adapter context
     * @param logs git logs as list
     */
    public GitLogsAdapter(Context context, List<RevCommit> logs) {
        this.context = context;
        gitLogs = logs;
    }

    /**
     * When view holder is created
     *
     * @param parent parent view
     * @param viewType type of view
     * @return GitLogsAdapter.ViewHolder
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_git_log, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Called when item is bound to position
     *
     * @param holder view holder
     * @param position position of item
     */
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final RevCommit commit = gitLogs.get(position);
        final SpannableString string = new SpannableString(commit.getFullMessage());
        int index = commit.getFullMessage().indexOf('\n') + 1;
        if (index == 0) index = commit.getFullMessage().length();
        final boolean[] fullShown = {false};
        string.setSpan(new StyleSpan(Typeface.BOLD), 0, index, 0);
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!fullShown[0]) {
                    holder.commitName.setTypeface(Typeface.DEFAULT);
                    holder.commitName.setText(string);
                    fullShown[0] = true;
                } else {
                    holder.commitName.setTypeface(Typeface.DEFAULT_BOLD);
                    holder.commitName.setText(commit.getShortMessage());
                    fullShown[0] = false;
                }
            }
        });

        holder.view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("hash", commit.getId().getName());
                clipboard.setPrimaryClip(clip);
                Snackbar.make(holder.view, R.string.commit_hash_copy, Snackbar.LENGTH_SHORT).show();
                return true;
            }
        });

        holder.commitName.setText(commit.getShortMessage());
        holder.commitName.setTypeface(Typeface.DEFAULT_BOLD);
        holder.commitAuthor.setText(commit.getAuthorIdent().getName() + " <" + commit.getAuthorIdent().getEmailAddress() + ">");
        holder.commitDate.setText(commit.getAuthorIdent().getWhen().toString());
        holder.commitHash.setText(commit.getId().getName());
    }

    /**
     * Gets log count
     *
     * @return list size
     */
    @Override
    public int getItemCount() {
        if (gitLogs != null) {
            return gitLogs.size();
        } else {
            return 0;
        }
    }

    /**
     * View holder class for logs
     */
    static class ViewHolder extends RecyclerView.ViewHolder {

        /**
         * View holder views
         */
        @BindView(R.id.commit_name) TextView commitName;
        @BindView(R.id.commit_date) TextView commitDate;
        @BindView(R.id.commit_author) TextView commitAuthor;
        @BindView(R.id.commit_hash) TextView commitHash;
        View view;

        /**
         * Constructor
         * @param v view for holder
         */
        ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
            view = v;
        }
    }
}
