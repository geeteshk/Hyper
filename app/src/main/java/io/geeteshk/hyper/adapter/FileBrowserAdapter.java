package io.geeteshk.hyper.adapter;

import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.fragment.EditorFragment;
import io.geeteshk.hyper.fragment.ImageFragment;
import io.geeteshk.hyper.helper.Constants;
import io.geeteshk.hyper.helper.Decor;
import io.geeteshk.hyper.helper.Project;

public class FileBrowserAdapter extends RecyclerView.Adapter<FileBrowserAdapter.FileHolder> {

    ArrayAdapter<String> mFileAdapter;
    List<String> mFiles;
    AppCompatActivity mActivity;
    Spinner mSpinner;
    DrawerLayout mDrawerLayout;
    FilenameFilter projectFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return !name.endsWith(".hyper") && !name.startsWith(".");
        }
    };
    private String mProject;
    private String mOriginalProject;
    private List<File> mDataset;

    public FileBrowserAdapter(String project, ArrayAdapter<String> adapter, List<String> files, AppCompatActivity activity, Spinner spinner, DrawerLayout drawerLayout) {
        mFileAdapter = adapter;
        mFiles = files;
        mActivity = activity;
        mSpinner = spinner;
        mProject = project;
        mOriginalProject = project;
        mDrawerLayout = drawerLayout;
        mDataset = new LinkedList<>(Arrays.asList(new File(Constants.HYPER_ROOT + File.separator + mProject).listFiles(projectFilter)));
    }

    @Override
    public FileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_browser, parent, false);
        return new FileHolder(view);
    }

    @Override
    public void onBindViewHolder(FileHolder holder, final int position) {
        if (position == 0) {
            holder.mName.setText(mProject);
            holder.mName.setTypeface(Typeface.SERIF);
            holder.mName.setTextColor(0xFFFFFFFF);
            holder.mIcon.setImageResource(R.drawable.ic_folder);
            holder.mIcon.setColorFilter(0xFF68EFAD, PorterDuff.Mode.SRC_ATOP);
            holder.mBackground.setBackgroundColor(0xFF9B26AF);
            holder.mBackground.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mProject.equals(mOriginalProject)) {
                        mProject = mOriginalProject;
                        mDataset.clear();
                        mDataset.addAll(new LinkedList<>(Arrays.asList(new File(Constants.HYPER_ROOT + File.separator + mProject).listFiles(projectFilter))));

                        notifyDataSetChanged();
                    }
                }
            });
        } else {
            final File currentFile = mDataset.get(position - 1);
            if (currentFile.isDirectory()) {
                holder.mName.setText(currentFile.getName());
                holder.mIcon.setImageResource(R.drawable.ic_folder);
                holder.mBackground.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mProject = mProject + " › " + currentFile.getName();
                        mDataset.clear();
                        mDataset.addAll(new LinkedList<>(Arrays.asList(new File(Constants.HYPER_ROOT + File.separator + mProject.replace(" › ", File.separator)).listFiles(projectFilter))));

                        notifyDataSetChanged();
                    }
                });
            } else {
                holder.mName.setText(currentFile.getName());
                holder.mIcon.setImageResource(Decor.getIcon(currentFile.getName(), mProject));
                holder.mBackground.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String location = currentFile.getPath().substring(currentFile.getPath().indexOf(mOriginalProject) + mOriginalProject.length() + 1, currentFile.getPath().length());
                        openFragment(location);
                        mDrawerLayout.closeDrawers();
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size() + 1;
    }

    /**
     * Open file when selected by setting the correct fragment
     *
     * @param file file to open
     * @param add  whether to add to adapter
     */
    private void setFragment(String file, boolean add) {
        if (add) {
            mFileAdapter.add(file);
            mFileAdapter.notifyDataSetChanged();
        }

        if (mSpinner.getSelectedItemPosition() != mFileAdapter.getPosition(file)) {
            mSpinner.setSelection(mFileAdapter.getPosition(file), true);
            mActivity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.editor_fragment, getFragment(file))
                    .commit();
        }
    }

    /**
     * Method to get the type of fragment dependent on the file type
     *
     * @param title file name
     * @return fragment to be committed
     */
    public Fragment getFragment(String title) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", mFileAdapter.getCount());
        bundle.putString("location", mProject + File.separator + title);
        if (Project.isImageFile(new File(Constants.HYPER_ROOT + File.separator + mProject, title))) {
            ImageFragment imageFragment = (ImageFragment) Fragment.instantiate(mActivity, ImageFragment.class.getName(), bundle);
            return imageFragment;
        } else {
            EditorFragment editorFragment = (EditorFragment) Fragment.instantiate(mActivity, EditorFragment.class.getName(), bundle);
            return editorFragment;
        }
    }

    public void openFragment(String file) {
        if (mFiles.contains(file)) {
            setFragment(file, false);
        } else {
            if (!Project.isBinaryFile(new File(Constants.HYPER_ROOT + File.separator + mProject + File.separator + file))) {
                setFragment(file, true);
            } else if (Project.isImageFile(new File(Constants.HYPER_ROOT + File.separator + mProject + File.separator + file))) {
                setFragment(file, true);
            } else {
                Toast.makeText(mActivity, R.string.not_text_file, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static class FileHolder extends RecyclerView.ViewHolder {

        public TextView mName;
        public ImageView mIcon;
        public View mBackground;

        public FileHolder(View view) {
            super(view);
            mBackground = view;
            mName = (TextView) view.findViewById(R.id.file_browser_name);
            mIcon = (ImageView) view.findViewById(R.id.file_browser_icon);
        }
    }
}
