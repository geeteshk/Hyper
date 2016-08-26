package io.geeteshk.hyper.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

import io.geeteshk.hyper.Constants;
import io.geeteshk.hyper.MainActivity;
import io.geeteshk.hyper.R;
import io.geeteshk.hyper.adapter.ProjectAdapter;
import io.geeteshk.hyper.util.DecorUtil;
import io.geeteshk.hyper.util.GitUtil;
import io.geeteshk.hyper.util.ValidatorUtil;

public class ImproveFragment extends Fragment {

    ArrayList mObjectsList;
    ProjectAdapter mProjectAdapter;

    public ImproveFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_improve, container, false);

        final String[] objects = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "Hyper").list();
        mObjectsList = new ArrayList<>(Arrays.asList(objects));
        ValidatorUtil.removeBroken(mObjectsList);
        mProjectAdapter = new ProjectAdapter(getActivity(), (String[]) mObjectsList.toArray(new String[mObjectsList.size()]), true);
        final RecyclerView projectsList = (RecyclerView) rootView.findViewById(R.id.project_list);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        projectsList.setLayoutManager(layoutManager);
        projectsList.addItemDecoration(new DecorUtil.GridSpacingItemDecoration(2, DecorUtil.dpToPx(getActivity(), 4), true));
        projectsList.setItemAnimator(new DefaultItemAnimator());
        projectsList.setAdapter(mProjectAdapter);

        final ProgressBar cloneProgress = (ProgressBar) rootView.findViewById(R.id.clone_progress);

        FloatingActionButton createButton = (FloatingActionButton) rootView.findViewById(R.id.fab_create);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.update(getActivity(), getActivity().getSupportFragmentManager(), 0);
            }
        });

        FloatingActionButton cloneButton = (FloatingActionButton) rootView.findViewById(R.id.fab_clone);
        cloneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Clone repository");
                final EditText file = new EditText(getActivity());
                file.setMaxLines(1);
                file.setSingleLine(true);
                file.setHint("Project name");
                final EditText remote = new EditText(getActivity());
                remote.setMaxLines(1);
                remote.setSingleLine(true);
                remote.setHint("Remote url");
                LinearLayout layout = new LinearLayout(getActivity());
                layout.addView(file);
                layout.addView(remote);
                builder.setView(layout);
                builder.setPositiveButton("CLONE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        GitUtil.clone(getActivity(), new File(Constants.HYPER_ROOT + File.separator + file.getText().toString()), cloneProgress, mProjectAdapter, remote.getText().toString());
                    }
                });
                builder.setNegativeButton(R.string.cancel, null);
                builder.create().show();
            }
        });

        final EditText projectSearch = (EditText) rootView.findViewById(R.id.project_search);
        projectSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mObjectsList = new ArrayList<>(Arrays.asList(objects));
                ValidatorUtil.removeBroken(mObjectsList);
                for (Iterator iterator = mObjectsList.iterator(); iterator.hasNext(); ) {
                    String string = (String) iterator.next();
                    if (!string.toLowerCase(Locale.getDefault()).startsWith(s.toString())) {
                        iterator.remove();
                    }
                }

                mProjectAdapter = new ProjectAdapter(getActivity(), (String[]) mObjectsList.toArray(new String[mObjectsList.size()]), true);
                projectsList.setAdapter(mProjectAdapter);
            }
        });

        projectSearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_ENTER) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(projectSearch.getApplicationWindowToken(), 0);

                    return true;
                }

                return false;
            }
        });

        ImageButton clearSearch = (ImageButton) rootView.findViewById(R.id.clear_search);
        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                projectSearch.setText("");
            }
        });

        TextView emptyView = (TextView) rootView.findViewById(R.id.empty_view);
        if (mObjectsList.isEmpty()) {
            projectsList.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }

        return rootView;
    }
}
