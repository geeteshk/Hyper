package io.geeteshk.hyper.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.git.Giiit;

public class RemotesAdapter extends RecyclerView.Adapter<RemotesAdapter.RemotesHolder> {

    private ArrayList<String> mRemotes;
    private Context mContext;
    private File mRepo;

    public RemotesAdapter(Context context, File repo) {
        mRemotes = Giiit.getRemotes(context, repo);
        mContext = context;
        mRepo = repo;
    }

    @Override
    public RemotesHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_remote, parent, false);
        return new RemotesHolder(view);
    }

    @Override
    public void onBindViewHolder(final RemotesHolder holder, int position) {
        holder.mName.setText(mRemotes.get(position));
        holder.mUrl.setText(Giiit.getRemoteUrl(mContext, mRepo, mRemotes.get(position)));
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
                        Giiit.removeRemote(mContext, mRepo, mRemotes.get(newPos));
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
        Giiit.addRemote(mContext, mRepo, remote, url);
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
