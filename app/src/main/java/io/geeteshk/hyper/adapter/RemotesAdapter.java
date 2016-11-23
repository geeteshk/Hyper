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

import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.activity.ProjectActivity;
import io.geeteshk.hyper.git.Giiit;

public class RemotesAdapter extends RecyclerView.Adapter<RemotesAdapter.RemotesHolder> {

    private ArrayList<String> mRemotes;
    private Context mContext;
    private View mView;
    private File mRepo;

    public RemotesAdapter(Context context, View view, File repo) {
        mRemotes = Giiit.getRemotes(view, repo);
        mContext = context;
        mView = view;
        mRepo = repo;
    }

    @Override
    public RemotesHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_remote, parent, false);
        return new RemotesHolder(view);
    }

    @Override
    public void onBindViewHolder(final RemotesHolder holder, final int position) {
        holder.mName.setText(mRemotes.get(position));
        holder.mUrl.setText(Giiit.getRemoteUrl(mView, mRepo, mRemotes.get(position)));
        holder.mRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder gitPullBuilder = new AlertDialog.Builder(mContext);
                gitPullBuilder.setTitle("Fetch from remote");

                View pullView = LayoutInflater.from(mContext)
                        .inflate(R.layout.dialog_pull, null, false);

                final Spinner spinner1 = (Spinner) pullView.findViewById(R.id.remotes_spinner);

                final TextInputEditText pullUsername = (TextInputEditText) pullView.findViewById(R.id.pull_username);
                final TextInputEditText pullPassword = (TextInputEditText) pullView.findViewById(R.id.pull_password);

                spinner1.setAdapter(new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, mRemotes));
                gitPullBuilder.setView(pullView);
                gitPullBuilder.setPositiveButton("FETCH", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        Giiit.fetch(mContext, mView, mRepo, (String) spinner1.getSelectedItem(), pullUsername.getText().toString(), pullPassword.getText().toString());
                    }
                });

                gitPullBuilder.setNegativeButton(R.string.cancel, null);
                gitPullBuilder.create().show();
            }
        });

        holder.mRootView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final int newPos = holder.getAdapterPosition();
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Remove " + mRemotes.get(newPos) + "?");
                builder.setMessage("This remote will be removed permanently.");
                builder.setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Giiit.removeRemote(mView, mRepo, mRemotes.get(newPos));
                        mRemotes.remove(mRemotes.get(newPos));
                        notifyDataSetChanged();
                    }
                });

                builder.setNegativeButton(R.string.cancel, null);
                builder.create().show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mRemotes.size();
    }

    public void add(String remote, String url) {
        Giiit.addRemote(mView, mRepo, remote, url);
        mRemotes.add(remote);
        notifyDataSetChanged();
    }

    class RemotesHolder extends RecyclerView.ViewHolder {

        TextView mName, mUrl;
        View mRootView;

        RemotesHolder(View view) {
            super(view);
            mRootView = view;
            mName = (TextView) view.findViewById(R.id.remote_name);
            mUrl = (TextView) view.findViewById(R.id.remote_url);
        }
    }
}
