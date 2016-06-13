package io.geeteshk.hyper.adapter;

import android.animation.Animator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import io.geeteshk.hyper.Constants;
import io.geeteshk.hyper.EncryptActivity;
import io.geeteshk.hyper.MainActivity;
import io.geeteshk.hyper.ProjectActivity;
import io.geeteshk.hyper.R;
import io.geeteshk.hyper.WebActivity;
import io.geeteshk.hyper.helper.HyperDrive;
import io.geeteshk.hyper.util.JsonUtil;
import io.geeteshk.hyper.util.NetworkUtil;
import io.geeteshk.hyper.util.PreferenceUtil;
import io.geeteshk.hyper.util.ProjectUtil;

/**
 * Adapter to list all projects
 */
public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.MyViewHolder> {

    private static final String TAG = ProjectAdapter.class.getSimpleName();

    /**
     * Context used for various purposes such as loading files and inflating layouts
     */
    Context mContext;

    /**
     * Array of objects to fill list
     */
    String[] mObjects;

    boolean mImprove;

    public ProjectAdapter(Context context, String[] objects, boolean improve) {
        this.mContext = context;
        this.mObjects = objects;
        this.mImprove = improve;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        int color = Color.parseColor(JsonUtil.getProjectProperty(mObjects[position], "color"));
        final int newPos = holder.getAdapterPosition();

        holder.mTitle.setText(mObjects[position]);
        holder.mDescription.setText(JsonUtil.getProjectProperty(mObjects[position], "description"));
        holder.mFavicon.setImageBitmap(ProjectUtil.getFavicon(mObjects[position]));
        holder.mCardView.setCardBackgroundColor(color);

        if ((Color.red(color) * 0.299 + Color.green(color) * 0.587 + Color.blue(color) * 0.114) > 186) {
            holder.mTitle.setTextColor(Color.BLACK);
            holder.mDescription.setTextColor(Color.BLACK);
        } else {
            holder.mTitle.setTextColor(Color.WHITE);
            holder.mDescription.setTextColor(Color.WHITE);
        }

        if (mImprove) {
            holder.mFavicon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent;
                    if (PreferenceUtil.get(mContext, "pin", "").equals("")) {
                        intent = new Intent(mContext, ProjectActivity.class);
                        intent.putExtra("project", mObjects[newPos]);
                        ((AppCompatActivity) mContext).startActivityForResult(intent, 0);
                    } else {
                        intent = new Intent(mContext, EncryptActivity.class);
                        intent.putExtra("project", mObjects[newPos]);
                        mContext.startActivity(intent);
                    }
                }
            });
        } else {
            holder.mFavicon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NetworkUtil.setDrive(new HyperDrive(mObjects[newPos]));

                    try {
                        NetworkUtil.getDrive().start();
                    } catch (IOException e) {
                        Log.e(TAG, e.toString());
                    }

                    Intent intent = new Intent(mContext, WebActivity.class);

                    if (NetworkUtil.getDrive().wasStarted() && NetworkUtil.getDrive().isAlive() && NetworkUtil.getIpAddress() != null) {
                        intent.putExtra("url", "http:///" + NetworkUtil.getIpAddress() + ":8080");
                    } else {
                        intent.putExtra("url", "file:///" + Constants.HYPER_ROOT + File.separator + mObjects[newPos] + File.separator + "index.html");
                    }

                    intent.putExtra("name", mObjects[newPos]);
                    intent.putExtra("pilot", true);
                    mContext.startActivity(intent);
                }
            });
        }

        holder.mFavicon.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Delete " + mObjects[newPos] + "?");
                builder.setMessage("This change cannot be undone.");
                builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (ProjectUtil.deleteProject(mObjects[newPos])) {
                            holder.itemView.animate().alpha(0).setDuration(300).setListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    MainActivity.update(mContext, ((AppCompatActivity) mContext).getSupportFragmentManager(), 1);
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {
                                    MainActivity.update(mContext, ((AppCompatActivity) mContext).getSupportFragmentManager(), 1);
                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {

                                }
                            });
                            Toast.makeText(mContext, "Goodbye " + mObjects[newPos] + ".", Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(mContext, "Oops! Something went wrong while deleting " + mObjects[newPos] + ".", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                builder.setNegativeButton("CANCEL", null);
                builder.show();

                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return mObjects.length;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView mTitle, mDescription;
        public ImageView mFavicon;
        public CardView mCardView;

        public MyViewHolder(View view) {
            super(view);

            mTitle = (TextView) view.findViewById(R.id.title);
            mDescription = (TextView) view.findViewById(R.id.desc);
            mFavicon = (ImageView) view.findViewById(R.id.favicon);
            mCardView = (CardView) view.findViewById(R.id.card_view);
        }
    }
}
