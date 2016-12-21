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
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.activity.ProjectActivity;
import io.geeteshk.hyper.helper.Project;
import io.geeteshk.hyper.helper.Soup;

/**
 * Adapter to list all projects
 */
public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.MyViewHolder> {

    /**
     * Context used for various purposes such as loading files and inflating layouts
     */
    private Context mContext;

    /**
     * Array of objects to fill list
     */
    private ArrayList<String> mObjects;

    private CoordinatorLayout mLayout;

    private RecyclerView mRecyclerView;

    /**
     * public Constructor
     *
     * @param context loading files and inflating etc
     * @param objects objects to fill list
     */
    public ProjectAdapter(Context context, ArrayList<String> objects, CoordinatorLayout layout, RecyclerView recyclerView) {
        this.mContext = context;
        this.mObjects = objects;
        this.mLayout = layout;
        this.mRecyclerView = recyclerView;
    }

    public void insert(String project) {
        mObjects.add(project);
        int position = mObjects.indexOf(project);
        notifyItemInserted(position);
        mRecyclerView.scrollToPosition(position);
    }

    public void remove(int position) {
        mObjects.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * When view holder is created
     *
     * @param parent   parent view
     * @param viewType type of view
     * @return ProjectAdapter.ViewHolder
     */
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Collections.sort(mObjects);
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false);
        return new MyViewHolder(itemView);
    }

    /**
     * Called when item is bound to position
     *
     * @param holder   view holder
     * @param position position of item
     */
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        String[] properties = Soup.getProperties(mObjects.get(holder.getAdapterPosition()));
        holder.setTitle(properties[0]);
        holder.setDescription(properties[2]);
        holder.setIcon(Project.getFavicon(mContext, mObjects.get(holder.getAdapterPosition())));

        holder.mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ProjectActivity.class);
                intent.putExtra("project", mObjects.get(holder.getAdapterPosition()));
                intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                if (Build.VERSION.SDK_INT >= 21) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                }

                ((AppCompatActivity) mContext).startActivityForResult(intent, 0);
            }
        });

        holder.mFavicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ProjectActivity.class);
                intent.putExtra("project", mObjects.get(holder.getAdapterPosition()));
                intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                if (Build.VERSION.SDK_INT >= 21) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                }

                ((AppCompatActivity) mContext).startActivityForResult(intent, 0);
            }
        });

        holder.mLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(mContext.getString(R.string.delete) + " " + mObjects.get(holder.getAdapterPosition()) + "?");
                builder.setMessage(R.string.change_undone);
                builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String project = mObjects.get(holder.getAdapterPosition());
                        Project.deleteProject(project);
                        remove(holder.getAdapterPosition());

                        Snackbar.make(
                                mLayout,
                                "Deleted " + project + ".",
                                Snackbar.LENGTH_LONG
                        ).show();
                    }
                });

                builder.setNegativeButton(R.string.cancel, null);
                builder.create().show();

                return true;
            }
        });

        holder.mFavicon.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(mContext.getString(R.string.delete) + " " + mObjects.get(holder.getAdapterPosition()) + "?");
                builder.setMessage(R.string.change_undone);
                builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String project = mObjects.get(holder.getAdapterPosition());
                        Project.deleteProject(project);
                        remove(holder.getAdapterPosition());

                        Snackbar.make(
                                mLayout,
                                "Deleted " + project + ".",
                                Snackbar.LENGTH_LONG
                        ).show();
                    }
                });

                builder.setNegativeButton(R.string.cancel, null);
                builder.create().show();

                return true;
            }
        });
    }

    /**
     * Gets number of projects
     *
     * @return array size
     */
    @Override
    public int getItemCount() {
        return mObjects.size();
    }

    /**
     * View holder class for logs
     */
    class MyViewHolder extends RecyclerView.ViewHolder {

        /**
         * Views for view holder
         */
        TextView mTitle, mDescription;
        ImageView mFavicon;
        LinearLayout mLayout;

        /**
         * public Constructor
         *
         * @param view root view
         */
        MyViewHolder(View view) {
            super(view);

            mTitle = (TextView) view.findViewById(R.id.title);
            mDescription = (TextView) view.findViewById(R.id.desc);
            mFavicon = (ImageView) view.findViewById(R.id.favicon);
            mLayout = (LinearLayout) view.findViewById(R.id.project_layout);
        }

        public void setTitle(String title) {
            mTitle.setText(title);
        }

        public void setDescription(String description) {
            mDescription.setText(description);
        }

        public void setIcon(Bitmap bitmap) {
            mFavicon.setImageBitmap(bitmap);
        }
    }
}
