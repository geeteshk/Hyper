package io.geeteshk.hyper.adapter;

import android.animation.Animator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.IOException;

import io.geeteshk.hyper.helper.Constants;
import io.geeteshk.hyper.activity.EncryptActivity;
import io.geeteshk.hyper.activity.MainActivity;
import io.geeteshk.hyper.activity.ProjectActivity;
import io.geeteshk.hyper.R;
import io.geeteshk.hyper.activity.WebActivity;
import io.geeteshk.hyper.helper.Firebase;
import io.geeteshk.hyper.helper.Hyperion;
import io.geeteshk.hyper.helper.Jason;
import io.geeteshk.hyper.helper.Network;
import io.geeteshk.hyper.helper.Pref;
import io.geeteshk.hyper.helper.Project;

/**
 * Adapter to list all projects
 */
public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.MyViewHolder> {

    /**
     * Log TAG
     */
    private static final String TAG = ProjectAdapter.class.getSimpleName();

    /**
     * Context used for various purposes such as loading files and inflating layouts
     */
    private Context mContext;

    /**
     * Array of objects to fill list
     */
    private String[] mObjects;

    /**
     * Whether this adapter is used in
     * either ImproveFragment if true
     * of PilotFragment if false
     */
    private boolean mImprove;

    /**
     * Firebase class(es) to get user information
     * and perform specific Firebase functions
     */
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;

    /**
     * public Constructor
     *
     * @param context loading files and inflating etc
     * @param objects objects to fill list
     * @param improve see mImprove above
     * @param auth FirebaseAuth
     * @param storage FirebaseStorage
     */
    public ProjectAdapter(Context context, String[] objects, boolean improve, FirebaseAuth auth, FirebaseStorage storage) {
        this.mContext = context;
        this.mObjects = objects;
        this.mImprove = improve;
        this.mAuth = auth;
        this.mStorage = storage;
    }

    /**
     * When view holder is created
     *
     * @param parent parent view
     * @param viewType type of view
     * @return ProjectAdapter.ViewHolder
     */
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false);
        return new MyViewHolder(itemView);
    }

    /**
     * Called when item is bound to position
     *
     * @param holder view holder
     * @param position position of item
     */
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        int color = Color.parseColor(Jason.getProjectProperty(mObjects[position], "color"));
        final int newPos = holder.getAdapterPosition();

        holder.mTitle.setText(mObjects[position]);
        holder.mDescription.setText(Jason.getProjectProperty(mObjects[position], "description"));
        holder.mFavicon.setImageBitmap(Project.getFavicon(mObjects[position]));
        holder.mColor.setBackgroundColor(color);

        if (mImprove) {
            holder.mFavicon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent;
                    if (Pref.get(mContext, "pin", "").equals("")) {
                        intent = new Intent(mContext, ProjectActivity.class);
                        intent.putExtra("project", mObjects[newPos]);
                        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                        if (Build.VERSION.SDK_INT >= 21) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                        }

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
                    Network.setDrive(new Hyperion(mObjects[newPos]));

                    try {
                        Network.getDrive().start();
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }


                    if (Pref.get(mContext, "pin", "").equals("")) {
                        Intent intent = new Intent(mContext, WebActivity.class);

                        if (Network.getDrive().wasStarted() && Network.getDrive().isAlive() && Network.getIpAddress() != null) {
                            intent.putExtra("url", "http:///" + Network.getIpAddress() + ":8080");
                        } else {
                            intent.putExtra("url", "file:///" + Constants.HYPER_ROOT + File.separator + mObjects[newPos] + File.separator + "index.html");
                        }

                        intent.putExtra("name", mObjects[newPos]);
                        intent.putExtra("pilot", true);
                        mContext.startActivity(intent);
                    } else {
                        Intent intent = new Intent(mContext, EncryptActivity.class);

                        if (Network.getDrive().wasStarted() && Network.getDrive().isAlive() && Network.getIpAddress() != null) {
                            intent.putExtra("url", "http:///" + Network.getIpAddress() + ":8080");
                        } else {
                            intent.putExtra("url", "file:///" + Constants.HYPER_ROOT + File.separator + mObjects[newPos] + File.separator + "index.html");
                        }

                        intent.putExtra("name", mObjects[newPos]);
                        intent.putExtra("pilot", true);
                        mContext.startActivity(intent);
                    }
                }
            });
        }

        holder.mFavicon.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(mContext.getString(R.string.delete) + " " + mObjects[newPos] + "?");
                builder.setMessage(R.string.change_undone);
                builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Project.deleteProject(mContext, mObjects[newPos])) {
                            holder.itemView.animate().alpha(0).setDuration(300).setListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    Firebase.removeProject(mAuth, mStorage, mObjects[newPos]);
                                    Firebase.deleteProjectFiles(mAuth, mStorage, mObjects[newPos]);
                                    MainActivity.update(mContext, ((AppCompatActivity) mContext).getSupportFragmentManager(), 1);
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {
                                    Firebase.removeProject(mAuth, mStorage, mObjects[newPos]);
                                    Firebase.deleteProjectFiles(mAuth, mStorage, mObjects[newPos]);
                                    MainActivity.update(mContext, ((AppCompatActivity) mContext).getSupportFragmentManager(), 1);
                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {

                                }
                            });
                            Toast.makeText(mContext, mContext.getString(R.string.goodbye) + " " + mObjects[newPos] + ".", Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(mContext, mContext.getString(R.string.oops_delete) + " " + mObjects[newPos] + ".", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                builder.setNegativeButton(R.string.cancel, null);
                builder.show();

                return true;
            }
        });

        if (new File(Constants.HYPER_ROOT + File.separator + mObjects[position], ".git").exists() && new File(Constants.HYPER_ROOT + File.separator + mObjects[position], ".git").isDirectory()) {
            holder.mRepo.setVisibility(View.VISIBLE);
        }

    }

    /**
     * Gets number of projects
     *
     * @return array size
     */
    @Override
    public int getItemCount() {
        return mObjects.length;
    }

    /**
     * View holder class for logs
     */
    class MyViewHolder extends RecyclerView.ViewHolder {

        /**
         * Views for view holder
         */
        TextView mTitle, mDescription;
        ImageView mFavicon, mRepo;
        View mColor;

        /**
         * public Constructor
         *
         * @param view root view
         */
        MyViewHolder(View view) {
            super(view);

            mTitle = (TextView) view.findViewById(R.id.title);
            mDescription = (TextView) view.findViewById(R.id.desc);
            mRepo = (ImageView) view.findViewById(R.id.git_repo);
            mFavicon = (ImageView) view.findViewById(R.id.favicon);
            mColor = view.findViewById(R.id.project_color);
        }
    }
}
