package io.geeteshk.hyper.fragment;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

import io.geeteshk.hyper.MainActivity;
import io.geeteshk.hyper.R;
import io.geeteshk.hyper.adapter.ProjectAdapter;
import io.geeteshk.hyper.util.DecorUtil;

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
        mProjectAdapter = new ProjectAdapter(getActivity(), objects, true);
        final RecyclerView projectsList = (RecyclerView) rootView.findViewById(R.id.project_list);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        projectsList.setLayoutManager(layoutManager);
        projectsList.addItemDecoration(new DecorUtil.GridSpacingItemDecoration(2, DecorUtil.dpToPx(getActivity(), 4), true));
        projectsList.setItemAnimator(new DefaultItemAnimator());
        projectsList.setAdapter(mProjectAdapter);

        FloatingActionButton button = (FloatingActionButton) rootView.findViewById(R.id.fab_create);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.update(getActivity(), getActivity().getSupportFragmentManager(), 0);
            }
        });

        button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getActivity(), "Create project", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        EditText projectSearch = (EditText) rootView.findViewById(R.id.project_search);
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
                for (Iterator<String> iterator = mObjectsList.iterator(); iterator.hasNext(); ) {
                    String string = iterator.next();
                    if (!string.toLowerCase(Locale.getDefault()).startsWith(s.toString())) {
                        iterator.remove();
                    }
                }

                mProjectAdapter = new ProjectAdapter(getActivity(), (String[]) mObjectsList.toArray(new String[0]), true);
                projectsList.setAdapter(mProjectAdapter);
            }
        });

        return rootView;
    }
}
